package repository;

import org.ipcamera.config.entity.Line;
import org.ipcamera.config.repository.lines_request.LineRepository;
import org.ipcamera.config.service.FileService;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import serilogj.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LineRepositoryTest {
    private final LineRepository repository = new LineRepository();

    @Test
    void restart_shouldSendRestartCommand_whenServerReturns200()
            throws Exception {

        Line line = mock(Line.class);

        when(line.getIpAddress()).thenReturn("192.168.1.100");
        when(line.getPort()).thenReturn(1234);
        when(line.getLogin()).thenReturn("admin");
        when(line.getPassword()).thenReturn("password");

        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        /*
         * Сервер сначала отвечает SID,
         * потом тремя строками ответа.
         */
        String serverResponse =
                "SID123\r\n" +
                        "200\r\n" +
                        "OK\r\n" +
                        "DONE\r\n";

        try (MockedConstruction<Socket> mockedSocket =
                     mockConstruction(Socket.class, (socket, context) -> {

                         when(socket.getOutputStream())
                                 .thenReturn(outputStream);

                         when(socket.getInputStream())
                                 .thenReturn(new ByteArrayInputStream(
                                         serverResponse.getBytes(
                                                 StandardCharsets.US_ASCII)
                                 ));
                     });
             MockedStatic<Log> mockedLog = mockStatic(Log.class)) {

            repository.restart("Line 15", line);

            assertEquals(1, mockedSocket.constructed().size());

            Socket socket = mockedSocket.constructed().get(0);

            verify(socket).setSoTimeout(30000);
            verify(socket).close();

            String request = outputStream.toString(
                    StandardCharsets.US_ASCII.name()
            );

            assertTrue(request.startsWith("GETSID\r\n"));

            assertTrue(request.contains(
                    "EDITOR:admin:RESTARTLINE:15\r\n"
            ));

            String expectedMd5 =
                    md5("admin" + "SID123" + "password");

            assertTrue(
                    request.contains(expectedMd5 + "\r\n")
            );
        }
    }

    @Test
    void restart_shouldRemoveNonNumericCharactersFromLineId()
            throws Exception {

        Line line = mock(Line.class);

        when(line.getIpAddress()).thenReturn("192.168.1.100");
        when(line.getPort()).thenReturn(1000);
        when(line.getLogin()).thenReturn("user");
        when(line.getPassword()).thenReturn("pass");

        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        String response =
                "SID\r\n" +
                        "200\r\n" +
                        "OK\r\n" +
                        "DONE\r\n";

        try (MockedConstruction<Socket> mockedSocket =
                     mockConstruction(Socket.class, (socket, context) -> {

                         when(socket.getOutputStream())
                                 .thenReturn(outputStream);

                         when(socket.getInputStream())
                                 .thenReturn(new ByteArrayInputStream(
                                         response.getBytes(
                                                 StandardCharsets.US_ASCII)
                                 ));
                     });
             MockedStatic<Log> log = mockStatic(Log.class)) {

            repository.restart("Line №123", line);

            String request =
                    outputStream.toString(
                            StandardCharsets.US_ASCII.name());

            assertTrue(request.contains(
                    "RESTARTLINE:123"
            ));
        }
    }

    @Test
    void restart_shouldRetryThreeTimes_whenResponseIsNot200()
            throws Exception {

        Line line = mock(Line.class);

        when(line.getIpAddress()).thenReturn("192.168.1.100");
        when(line.getPort()).thenReturn(1234);
        when(line.getLogin()).thenReturn("admin");
        when(line.getPassword()).thenReturn("password");

        try (MockedConstruction<Socket> mockedSocket =
                     mockConstruction(Socket.class, (socket, context) -> {

                         when(socket.getOutputStream())
                                 .thenReturn(new ByteArrayOutputStream());

                         String response =
                                 "SID123\r\n" +
                                         "500\r\n" +
                                         "ERROR\r\n" +
                                         "FAILED\r\n";

                         when(socket.getInputStream())
                                 .thenReturn(new ByteArrayInputStream(
                                         response.getBytes(
                                                 StandardCharsets.US_ASCII)
                                 ));
                     });
             MockedStatic<Log> log = mockStatic(Log.class);
             MockedStatic<FileService> fileService =
                     mockStatic(FileService.class)) {

            repository.restart("Line 10", line);

            assertEquals(
                    3,
                    mockedSocket.constructed().size(),
                    "При неуспешном ответе должно быть 3 попытки"
            );

            for (Socket socket : mockedSocket.constructed()) {
                verify(socket).close();
            }
        }
    }

    @Test
    void restart_shouldNotCreateSocket_whenLineIsNull()
            throws Exception {

        try (MockedConstruction<Socket> mockedSocket =
                     mockConstruction(Socket.class);
             MockedStatic<Log> log = mockStatic(Log.class);
             MockedStatic<FileService> fileService =
                     mockStatic(FileService.class)) {

            repository.restart("10", null);

            assertEquals(
                    0,
                    mockedSocket.constructed().size()
            );
        }
    }

    @Test
    void restart_shouldNotCreateSocket_whenLineIdIsNull()
            throws Exception {

        Line line = mock(Line.class);

        try (MockedConstruction<Socket> mockedSocket =
                     mockConstruction(Socket.class);
             MockedStatic<Log> log = mockStatic(Log.class);
             MockedStatic<FileService> fileService =
                     mockStatic(FileService.class)) {

            repository.restart(null, line);

            assertEquals(
                    0,
                    mockedSocket.constructed().size()
            );
        }
    }

    @Test
    void restart_shouldNotCreateSocket_whenLineIdIsEmpty()
            throws Exception {

        Line line = mock(Line.class);

        try (MockedConstruction<Socket> mockedSocket =
                     mockConstruction(Socket.class);
             MockedStatic<Log> log = mockStatic(Log.class);
             MockedStatic<FileService> fileService =
                     mockStatic(FileService.class)) {

            repository.restart("", line);

            assertEquals(
                    0,
                    mockedSocket.constructed().size()
            );
        }
    }

    private String md5(String value) throws Exception {
        MessageDigest md =
                MessageDigest.getInstance("MD5");

        byte[] hash = md.digest(
                value.getBytes(StandardCharsets.US_ASCII)
        );

        StringBuilder result = new StringBuilder();

        for (byte b : hash) {
            result.append(String.format("%02x", b));
        }

        return result.toString();
    }
}
