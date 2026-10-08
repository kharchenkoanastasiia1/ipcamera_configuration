package repository;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

import org.ipcamera.config.repository.cameras_request.IPCameraRepository;
import org.ipcamera.config.entity.Camera;
import org.ipcamera.config.service.MessageForSeq;
import org.ipcamera.config.service.converter.TimeConverter;
import org.ipcamera.config.service.time.NTPTimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import serilogj.Log;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class IPCameraRepositoryTest {

    private IPCameraRepository repository;

    @BeforeEach
    void setUp() {
        repository = new IPCameraRepository();

        /*
         * На случай, если предыдущий тест выставил interrupt flag.
         */
        Thread.interrupted();
    }

    // =====================================================================
    // getVersion()
    // =====================================================================

    @Test
    void getVersion_shouldReturnVersion_whenResponseIsSuccessful()
            throws Exception {

        Camera camera = createCamera();

        CloseableHttpClient client =
                mock(CloseableHttpClient.class);

        HttpClientBuilder builder =
                createHttpClientBuilder(client);

        /*
         * ВАЖНО:
         * createResponse вызываем ОТДЕЛЬНО,
         * а не внутри thenReturn().
         */
        CloseableHttpResponse response =
                createResponse(
                        200,
                        "camera response with firmware version"
                );

        when(client.execute(any(HttpUriRequest.class)))
                .thenReturn(response);

        Pattern pattern =
                mock(Pattern.class);

        Matcher matcher =
                mock(Matcher.class);

        when(pattern.matcher(anyString()))
                .thenReturn(matcher);

        when(matcher.find())
                .thenReturn(true);

        when(matcher.group(1))
                .thenReturn("9.30");

        try (
                MockedStatic<HttpClients> httpClientsMock =
                        mockStatic(HttpClients.class);

                MockedStatic<Pattern> patternMock =
                        mockStatic(Pattern.class)
        ) {

            httpClientsMock.when(HttpClients::custom)
                    .thenReturn(builder);

            patternMock.when(() ->
                            Pattern.compile(anyString()))
                    .thenReturn(pattern);

            String result =
                    repository.getVersion(camera);

            assertNotNull(result);
            assertEquals("9.30", result);

            verify(client, times(1))
                    .execute(any(HttpUriRequest.class));

            verify(pattern)
                    .matcher(anyString());

            verify(matcher)
                    .find();

            verify(matcher)
                    .group(1);

            verify(client)
                    .close();
        }
    }

    @Test
    void getVersion_shouldReturnNull_whenVersionNotFound()
            throws Exception {

        Camera camera = createCamera();

        CloseableHttpClient client =
                mock(CloseableHttpClient.class);

        HttpClientBuilder builder =
                createHttpClientBuilder(client);

        /*
         * Создаем все response ДО stubbing client.execute().
         */
        CloseableHttpResponse response1 =
                createResponse(
                        200,
                        "response without version 1"
                );

        CloseableHttpResponse response2 =
                createResponse(
                        200,
                        "response without version 2"
                );

        CloseableHttpResponse response3 =
                createResponse(
                        200,
                        "response without version 3"
                );

        when(client.execute(any(HttpUriRequest.class)))
                .thenReturn(
                        response1,
                        response2,
                        response3
                );

        Pattern pattern =
                mock(Pattern.class);

        Matcher matcher =
                mock(Matcher.class);

        when(pattern.matcher(anyString()))
                .thenReturn(matcher);

        when(matcher.find())
                .thenReturn(false);

        try (
                MockedStatic<HttpClients> httpClientsMock =
                        mockStatic(HttpClients.class);

                MockedStatic<Pattern> patternMock =
                        mockStatic(Pattern.class)
        ) {

            httpClientsMock.when(HttpClients::custom)
                    .thenReturn(builder);

            patternMock.when(() ->
                            Pattern.compile(anyString()))
                    .thenReturn(pattern);

            String result =
                    repository.getVersion(camera);

            assertNull(result);

            verify(client, times(3))
                    .execute(any(HttpUriRequest.class));

            verify(matcher, times(3))
                    .find();

            verify(matcher, never())
                    .group(1);

            verify(client)
                    .close();
        }
    }

    @Test
    void getVersion_shouldReturnNull_whenServerReturns500()
            throws Exception {

        Camera camera = createCamera();

        CloseableHttpClient client =
                mock(CloseableHttpClient.class);

        HttpClientBuilder builder =
                createHttpClientBuilder(client);

        CloseableHttpResponse response1 =
                createResponse(
                        500,
                        "Internal Server Error"
                );

        CloseableHttpResponse response2 =
                createResponse(
                        500,
                        "Internal Server Error"
                );

        CloseableHttpResponse response3 =
                createResponse(
                        500,
                        "Internal Server Error"
                );

        when(client.execute(any(HttpUriRequest.class)))
                .thenReturn(
                        response1,
                        response2,
                        response3
                );

        try (
                MockedStatic<HttpClients> httpClientsMock =
                        mockStatic(HttpClients.class)
        ) {

            httpClientsMock.when(HttpClients::custom)
                    .thenReturn(builder);

            String result =
                    repository.getVersion(camera);

            assertNull(result);

            verify(client, times(3))
                    .execute(any(HttpUriRequest.class));

            verify(client)
                    .close();
        }
    }

    @Test
    void getVersion_shouldRetryAfterIOExceptionAndReturnVersion()
            throws Exception {

        Camera camera = createCamera();

        CloseableHttpClient client =
                mock(CloseableHttpClient.class);

        HttpClientBuilder builder =
                createHttpClientBuilder(client);

        CloseableHttpResponse successfulResponse =
                createResponse(
                        200,
                        "camera response"
                );

        when(client.execute(any(HttpUriRequest.class)))
                .thenThrow(
                        new IOException("First error")
                )
                .thenThrow(
                        new IOException("Second error")
                )
                .thenReturn(successfulResponse);

        Pattern pattern =
                mock(Pattern.class);

        Matcher matcher =
                mock(Matcher.class);

        when(pattern.matcher(anyString()))
                .thenReturn(matcher);

        when(matcher.find())
                .thenReturn(true);

        when(matcher.group(1))
                .thenReturn("9.30");

        try (
                MockedStatic<HttpClients> httpClientsMock =
                        mockStatic(HttpClients.class);

                MockedStatic<Pattern> patternMock =
                        mockStatic(Pattern.class)
        ) {

            httpClientsMock.when(HttpClients::custom)
                    .thenReturn(builder);

            patternMock.when(() ->
                            Pattern.compile(anyString()))
                    .thenReturn(pattern);

            String result =
                    repository.getVersion(camera);

            assertNotNull(result);
            assertEquals("9.30", result);

            verify(client, times(3))
                    .execute(any(HttpUriRequest.class));

            verify(client)
                    .close();
        }
    }

    @Test
    void getVersion_shouldReturnNull_afterThreeIOException()
            throws Exception {

        Camera camera = createCamera();

        CloseableHttpClient client =
                mock(CloseableHttpClient.class);

        HttpClientBuilder builder =
                createHttpClientBuilder(client);

        when(client.execute(any(HttpUriRequest.class)))
                .thenThrow(
                        new IOException("Connection error")
                );

        try (
                MockedStatic<HttpClients> httpClientsMock =
                        mockStatic(HttpClients.class)
        ) {

            httpClientsMock.when(HttpClients::custom)
                    .thenReturn(builder);

            String result =
                    repository.getVersion(camera);

            assertNull(result);

            verify(client, times(3))
                    .execute(any(HttpUriRequest.class));

            verify(client)
                    .close();
        }
    }

    // =====================================================================
    // getServerTime()
    // =====================================================================

    @Test
    void getServerTime_shouldReturnDate_whenRequestIsSuccessful()
            throws Exception {

        Camera camera = createCamera();

        CloseableHttpClient client =
                mock(CloseableHttpClient.class);

        HttpClientBuilder builder =
                createHttpClientBuilder(client);

        CloseableHttpResponse response =
                createResponse(
                        200,
                        "camera-date-response"
                );

        when(client.execute(any(HttpUriRequest.class)))
                .thenReturn(response);

        Date expectedDate =
                new Date(1700000000000L);

        try (
                MockedStatic<HttpClients> httpClientsMock =
                        mockStatic(HttpClients.class);

                MockedConstruction<SimpleDateFormat> dateFormatMock =
                        mockConstruction(
                                SimpleDateFormat.class,
                                (mock, context) -> {

                                    when(mock.parse(anyString()))
                                            .thenReturn(
                                                    expectedDate
                                            );
                                }
                        )
        ) {

            httpClientsMock.when(HttpClients::custom)
                    .thenReturn(builder);

            Date result =
                    repository.getServerTime(camera);

            assertNotNull(result);
            assertEquals(
                    expectedDate,
                    result
            );

            verify(camera, times(1))
                    .setStatusGetRequest(true);

            verify(client, times(1))
                    .execute(any(HttpUriRequest.class));

            verify(client)
                    .close();
        }
    }

    @Test
    void getServerTime_shouldReturnNull_whenIOExceptionOccurs()
            throws Exception {

        Camera camera = createCamera();

        CloseableHttpClient client =
                mock(CloseableHttpClient.class);

        HttpClientBuilder builder =
                createHttpClientBuilder(client);

        when(client.execute(any(HttpUriRequest.class)))
                .thenThrow(
                        new IOException(
                                "Connection error"
                        )
                );

        try (
                MockedStatic<HttpClients> httpClientsMock =
                        mockStatic(HttpClients.class)
        ) {

            httpClientsMock.when(HttpClients::custom)
                    .thenReturn(builder);

            /*
             * После первой неудачной попытки production-код
             * делает Thread.sleep(1000).
             *
             * Чтобы unit-test реально не ждал,
             * заранее выставляем interrupt flag.
             */
            Thread.currentThread().interrupt();

            try {

                Date result =
                        repository.getServerTime(camera);

                assertNull(result);

                verify(camera, never())
                        .setStatusGetRequest(true);

            } finally {

                /*
                 * Сбрасываем interrupt flag,
                 * чтобы он не повлиял на другие тесты.
                 */
                Thread.interrupted();
            }
        }
    }

    @Test
    void getServerTime_shouldReturnNull_whenServerReturns500()
            throws Exception {

        Camera camera = createCamera();

        CloseableHttpClient client =
                mock(CloseableHttpClient.class);

        HttpClientBuilder builder =
                createHttpClientBuilder(client);

        CloseableHttpResponse response =
                createResponse(
                        500,
                        "Internal Server Error"
                );

        when(client.execute(any(HttpUriRequest.class)))
                .thenReturn(response);

        try (
                MockedStatic<HttpClients> httpClientsMock =
                        mockStatic(HttpClients.class)
        ) {

            httpClientsMock.when(HttpClients::custom)
                    .thenReturn(builder);

            Thread.currentThread().interrupt();

            try {

                Date result =
                        repository.getServerTime(camera);

                assertNull(result);

                verify(camera, never())
                        .setStatusGetRequest(true);

            } finally {

                Thread.interrupted();
            }
        }
    }

    // =====================================================================
    // postServerTime()
    // =====================================================================

    @Test
    void postServerTime_shouldUpdateCamera_whenResponseIsSuccessful()
            throws Exception {

        Camera camera = createCamera();

        CloseableHttpClient client =
                mock(CloseableHttpClient.class);

        HttpClientBuilder builder =
                createHttpClientBuilder(client);

        CloseableHttpResponse response =
                createResponse(
                        200,
                        "OK"
                );

        when(client.execute(any(HttpUriRequest.class)))
                .thenReturn(response);

        Date ntpDate =
                new Date(1700000000000L);

        try (
                MockedStatic<HttpClients> httpClientsMock =
                        mockStatic(HttpClients.class);

                MockedConstruction<NTPTimeService> ntpMock =
                        mockConstruction(
                                NTPTimeService.class,
                                (mock, context) -> {

                                    when(
                                            mock.getNTPTime(
                                                    "pool.ntp.org"
                                            )
                                    ).thenReturn(
                                            ntpDate
                                    );
                                }
                        );

                MockedStatic<TimeConverter> timeConverterMock =
                        mockStatic(TimeConverter.class);

                MockedStatic<MessageForSeq> messageMock =
                        mockStatic(MessageForSeq.class);

                MockedStatic<Log> logMock =
                        mockStatic(Log.class)
        ) {

            httpClientsMock.when(HttpClients::custom)
                    .thenReturn(builder);

            timeConverterMock.when(() ->
                            TimeConverter
                                    .formatDateTimeForIPCamera(
                                            ntpDate
                                    )
                    )
                    .thenReturn(
                            "2023-11-14 22:13:20"
                    );

            messageMock.when(() ->
                            MessageForSeq
                                    .getMessageUpdateMode(
                                            camera,
                                            ntpDate,
                                            true
                                    )
                    )
                    .thenReturn(
                            "Camera time updated"
                    );

            repository.postServerTime(
                    camera,
                    "pool.ntp.org",
                    true
            );

            verify(camera, times(1))
                    .setDateUpdateSuccess(ntpDate);

            verify(camera, times(1))
                    .setDifferenceTime(0);

            verify(client, times(1))
                    .execute(any(HttpUriRequest.class));

            logMock.verify(() ->
                    Log.information(
                            "Camera time updated"
                    )
            );

            verify(client)
                    .close();
        }
    }

    @Test
    void postServerTime_shouldAcceptOkRegardlessOfCase()
            throws Exception {

        Camera camera = createCamera();

        CloseableHttpClient client =
                mock(CloseableHttpClient.class);

        HttpClientBuilder builder =
                createHttpClientBuilder(client);

        CloseableHttpResponse response =
                createResponse(
                        200,
                        "Request completed: Ok"
                );

        when(client.execute(any(HttpUriRequest.class)))
                .thenReturn(response);

        Date ntpDate =
                new Date(1700000000000L);

        try (
                MockedStatic<HttpClients> httpClientsMock =
                        mockStatic(HttpClients.class);

                MockedConstruction<NTPTimeService> ntpMock =
                        mockConstruction(
                                NTPTimeService.class,
                                (mock, context) -> {

                                    when(
                                            mock.getNTPTime(
                                                    anyString()
                                            )
                                    ).thenReturn(
                                            ntpDate
                                    );
                                }
                        );

                MockedStatic<TimeConverter> timeConverterMock =
                        mockStatic(TimeConverter.class);

                MockedStatic<MessageForSeq> messageMock =
                        mockStatic(MessageForSeq.class);

                MockedStatic<Log> logMock =
                        mockStatic(Log.class)
        ) {

            httpClientsMock.when(HttpClients::custom)
                    .thenReturn(builder);

            timeConverterMock.when(() ->
                            TimeConverter
                                    .formatDateTimeForIPCamera(
                                            ntpDate
                                    )
                    )
                    .thenReturn(
                            "2023-11-14 22:13:20"
                    );

            messageMock.when(() ->
                            MessageForSeq
                                    .getMessageUpdateMode(
                                            camera,
                                            ntpDate,
                                            false
                                    )
                    )
                    .thenReturn(
                            "Camera time updated"
                    );

            repository.postServerTime(
                    camera,
                    "pool.ntp.org",
                    false
            );

            verify(camera, times(1))
                    .setDateUpdateSuccess(ntpDate);

            verify(camera, times(1))
                    .setDifferenceTime(0);

            verify(client, times(1))
                    .execute(any(HttpUriRequest.class));
        }
    }

    @Test
    void postServerTime_shouldNotUpdateCamera_whenServerReturns500()
            throws Exception {

        Camera camera = createCamera();

        CloseableHttpClient client =
                mock(CloseableHttpClient.class);

        HttpClientBuilder builder =
                createHttpClientBuilder(client);

        /*
         * ВАЖНО:
         *
         * Сначала создаем response.
         * Только потом создаем stubbing execute().
         *
         * НЕЛЬЗЯ:
         *
         * when(client.execute(...))
         *     .thenReturn(createResponse(...));
         */
        CloseableHttpResponse response =
                createResponse(
                        500,
                        "ERROR"
                );

        when(client.execute(any(HttpUriRequest.class)))
                .thenReturn(response);

        Date ntpDate =
                new Date(1700000000000L);

        try (
                MockedStatic<HttpClients> httpClientsMock =
                        mockStatic(HttpClients.class);

                MockedConstruction<NTPTimeService> ntpMock =
                        mockConstruction(
                                NTPTimeService.class,
                                (mock, context) -> {

                                    when(
                                            mock.getNTPTime(
                                                    anyString()
                                            )
                                    ).thenReturn(
                                            ntpDate
                                    );
                                }
                        );

                MockedStatic<TimeConverter> timeConverterMock =
                        mockStatic(TimeConverter.class);

                MockedStatic<Log> logMock =
                        mockStatic(Log.class)
        ) {

            httpClientsMock.when(HttpClients::custom)
                    .thenReturn(builder);

            timeConverterMock.when(() ->
                            TimeConverter
                                    .formatDateTimeForIPCamera(
                                            ntpDate
                                    )
                    )
                    .thenReturn(
                            "2023-11-14 22:13:20"
                    );

            Thread.currentThread().interrupt();

            try {

                repository.postServerTime(
                        camera,
                        "pool.ntp.org",
                        true
                );

                verify(camera, never())
                        .setDateUpdateSuccess(
                                any(Date.class)
                        );

                verify(camera, never())
                        .setDifferenceTime(
                                anyInt()
                        );

            } finally {

                Thread.interrupted();
            }
        }
    }

    @Test
    void postServerTime_shouldNotUpdateCamera_whenResponseDoesNotContainOk()
            throws Exception {

        Camera camera = createCamera();

        CloseableHttpClient client =
                mock(CloseableHttpClient.class);

        HttpClientBuilder builder =
                createHttpClientBuilder(client);

        CloseableHttpResponse response =
                createResponse(
                        200,
                        "FAILED"
                );

        when(client.execute(any(HttpUriRequest.class)))
                .thenReturn(response);

        Date ntpDate =
                new Date(1700000000000L);

        try (
                MockedStatic<HttpClients> httpClientsMock =
                        mockStatic(HttpClients.class);

                MockedConstruction<NTPTimeService> ntpMock =
                        mockConstruction(
                                NTPTimeService.class,
                                (mock, context) -> {

                                    when(
                                            mock.getNTPTime(
                                                    anyString()
                                            )
                                    ).thenReturn(
                                            ntpDate
                                    );
                                }
                        );

                MockedStatic<TimeConverter> timeConverterMock =
                        mockStatic(TimeConverter.class);

                MockedStatic<Log> logMock =
                        mockStatic(Log.class)
        ) {

            httpClientsMock.when(HttpClients::custom)
                    .thenReturn(builder);

            timeConverterMock.when(() ->
                            TimeConverter
                                    .formatDateTimeForIPCamera(
                                            ntpDate
                                    )
                    )
                    .thenReturn(
                            "2023-11-14 22:13:20"
                    );

            Thread.currentThread().interrupt();

            try {

                repository.postServerTime(
                        camera,
                        "pool.ntp.org",
                        true
                );

                verify(camera, never())
                        .setDateUpdateSuccess(
                                any(Date.class)
                        );

                verify(camera, never())
                        .setDifferenceTime(
                                anyInt()
                        );

            } finally {

                Thread.interrupted();
            }
        }
    }

    @Test
    void postServerTime_shouldNotUpdateCamera_whenIOExceptionOccurs()
            throws Exception {

        Camera camera = createCamera();

        CloseableHttpClient client =
                mock(CloseableHttpClient.class);

        HttpClientBuilder builder =
                createHttpClientBuilder(client);

        when(client.execute(any(HttpUriRequest.class)))
                .thenThrow(
                        new IOException(
                                "Connection error"
                        )
                );

        Date ntpDate =
                new Date(1700000000000L);

        try (
                MockedStatic<HttpClients> httpClientsMock =
                        mockStatic(HttpClients.class);

                MockedConstruction<NTPTimeService> ntpMock =
                        mockConstruction(
                                NTPTimeService.class,
                                (mock, context) -> {

                                    when(
                                            mock.getNTPTime(
                                                    anyString()
                                            )
                                    ).thenReturn(
                                            ntpDate
                                    );
                                }
                        );

                MockedStatic<TimeConverter> timeConverterMock =
                        mockStatic(TimeConverter.class);

                MockedStatic<Log> logMock =
                        mockStatic(Log.class)
        ) {

            httpClientsMock.when(HttpClients::custom)
                    .thenReturn(builder);

            timeConverterMock.when(() ->
                            TimeConverter
                                    .formatDateTimeForIPCamera(
                                            ntpDate
                                    )
                    )
                    .thenReturn(
                            "2023-11-14 22:13:20"
                    );

            Thread.currentThread().interrupt();

            try {

                repository.postServerTime(
                        camera,
                        "pool.ntp.org",
                        true
                );

                verify(camera, never())
                        .setDateUpdateSuccess(
                                any(Date.class)
                        );

                verify(camera, never())
                        .setDifferenceTime(
                                anyInt()
                        );

            } finally {

                Thread.interrupted();
            }
        }
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    private Camera createCamera() {

        Camera camera =
                mock(Camera.class);

        when(camera.getIpAddress())
                .thenReturn(
                        "192.168.1.100"
                );

        when(camera.getUsername())
                .thenReturn(
                        "admin"
                );

        when(camera.getPassword())
                .thenReturn(
                        "password"
                );

        /*
         * Реальная версия ПО камеры,
         * которую вы указали.
         */
        when(camera.getVersion())
                .thenReturn(
                        "9.30"
                );

        return camera;
    }

    private HttpClientBuilder createHttpClientBuilder(
            CloseableHttpClient client
    ) {

        HttpClientBuilder builder =
                mock(HttpClientBuilder.class);

        when(
                builder.setDefaultCredentialsProvider(
                        any()
                )
        ).thenReturn(
                builder
        );

        when(builder.build())
                .thenReturn(
                        client
                );

        return builder;
    }

    /**
     * Создает именно CloseableHttpResponse.
     *
     * Здесь НЕТ:
     *
     * BasicHttpResponse
     *
     * и НЕТ:
     *
     * (CloseableHttpResponse) someBasicResponse
     */
    private CloseableHttpResponse createResponse(
            int statusCode,
            String body
    ) throws IOException {

        CloseableHttpResponse response =
                mock(
                        CloseableHttpResponse.class
                );

        StatusLine statusLine =
                mock(
                        StatusLine.class
                );

        when(
                statusLine.getStatusCode()
        ).thenReturn(
                statusCode
        );

        when(
                response.getStatusLine()
        ).thenReturn(
                statusLine
        );

        HttpEntity entity =
                new StringEntity(
                        body,
                        StandardCharsets.UTF_8
                );

        when(
                response.getEntity()
        ).thenReturn(
                entity
        );

        return response;
    }
}