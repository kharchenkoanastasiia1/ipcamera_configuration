package repository;

import org.ipcamera.config.repository.CameraRepository;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CameraRepositoryTest {
    private final CameraRepository repository = new CameraRepository();

    @Test
    void sendPingRtsp_shouldReturnTrue_whenServerReturns200() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (MockedConstruction<Socket> mockedSocket =
                     mockConstruction(Socket.class, (socket, context) -> {

                         when(socket.getOutputStream())
                                 .thenReturn(outputStream);

                         when(socket.getInputStream())
                                 .thenReturn(new ByteArrayInputStream(
                                         "RTSP/1.0 200 OK\r\n\r\n"
                                                 .getBytes(StandardCharsets.US_ASCII)
                                 ));
                     })) {

            boolean result = repository.sendPingRtsp(
                    "192.168.1.100",
                    554,
                    "admin",
                    "password"
            );

            assertTrue(result);

            assertEquals(1, mockedSocket.constructed().size());

            Socket socket = mockedSocket.constructed().get(0);
            verify(socket).close();

            String sentRequest =
                    new String(outputStream.toByteArray(), StandardCharsets.UTF_8);

            assertTrue(sentRequest.contains(
                    "OPTIONS rtsp://192.168.1.100/ RTSP/1.0"
            ));

            assertTrue(sentRequest.contains("CSeq: 1"));

            String auth = Base64.getEncoder()
                    .encodeToString(
                            "admin:password".getBytes(StandardCharsets.UTF_8)
                    );

            assertTrue(sentRequest.contains(
                    "Authorization: Basic " + auth
            ));
        }
    }

    @Test
    void sendPingRtsp_shouldReturnFalse_whenServerDoesNotReturn200() {
        try (MockedConstruction<Socket> mockedSocket =
                     mockConstruction(Socket.class, (socket, context) -> {

                         when(socket.getOutputStream())
                                 .thenReturn(new ByteArrayOutputStream());

                         when(socket.getInputStream())
                                 .thenReturn(new ByteArrayInputStream(
                                         "RTSP/1.0 401 Unauthorized\r\n\r\n"
                                                 .getBytes(StandardCharsets.US_ASCII)
                                 ));
                     })) {

            // Чтобы тест не ждал Thread.sleep(1000) три раза.
            Thread.currentThread().interrupt();

            try {
                boolean result = repository.sendPingRtsp(
                        "192.168.1.100",
                        554,
                        "admin",
                        "wrong-password"
                );

                assertFalse(result);

                // Метод делает ровно 3 попытки.
                assertEquals(3, mockedSocket.constructed().size());

            } finally {
                // Очищаем interrupt flag после теста.
                Thread.interrupted();
            }
        }
    }

    @Test
    void sendPingRtsp_shouldRetryThreeTimes_whenConnectionThrowsException() {
        try (MockedConstruction<Socket> mockedSocket =
                     mockConstruction(Socket.class, (socket, context) -> {

                         when(socket.getOutputStream())
                                 .thenThrow(new RuntimeException("Connection error"));

                     })) {

            Thread.currentThread().interrupt();

            try {
                boolean result = repository.sendPingRtsp(
                        "192.168.1.100",
                        554,
                        "admin",
                        "password"
                );

                assertFalse(result);
                assertEquals(3, mockedSocket.constructed().size());

            } finally {
                Thread.interrupted();
            }
        }
    }

    @Test
    void sendPingRequest_shouldReturnTrue_whenHostIsReachable() throws Exception {
        InetAddress address = mock(InetAddress.class);

        when(address.isReachable(5000))
                .thenReturn(true);

        try (MockedStatic<InetAddress> mocked =
                     mockStatic(InetAddress.class)) {

            mocked.when(() ->
                    InetAddress.getByName("192.168.1.100")
            ).thenReturn(address);

            boolean result =
                    repository.sendPingRequest("192.168.1.100");

            assertTrue(result);

            verify(address).isReachable(5000);
        }
    }

    @Test
    void sendPingRequest_shouldReturnFalse_whenHostIsNotReachable()
            throws Exception {

        InetAddress address = mock(InetAddress.class);

        when(address.isReachable(5000))
                .thenReturn(false);

        try (MockedStatic<InetAddress> mocked =
                     mockStatic(InetAddress.class)) {

            mocked.when(() ->
                    InetAddress.getByName("192.168.1.100")
            ).thenReturn(address);

            boolean result =
                    repository.sendPingRequest("192.168.1.100");

            assertFalse(result);

            verify(address).isReachable(5000);
        }
    }

    @Test
    void sendPingRequest_shouldThrowIOException_whenGetByNameFails() {
        try (MockedStatic<InetAddress> mocked =
                     mockStatic(InetAddress.class)) {

            mocked.when(() ->
                    InetAddress.getByName("wrong-host")
            ).thenThrow(new java.net.UnknownHostException("wrong-host"));

            assertThrows(
                    java.io.IOException.class,
                    () -> repository.sendPingRequest("wrong-host")
            );
        }
    }
}
