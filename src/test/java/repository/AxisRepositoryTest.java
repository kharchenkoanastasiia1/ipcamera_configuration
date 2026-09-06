package repository;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

import org.ipcamera.config.entity.Camera;
import org.ipcamera.config.repository.cameras_request.AxisRepository;
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
import java.time.OffsetDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AxisRepositoryTest {

    private AxisRepository repository;

    @BeforeEach
    void setUp() {
        repository = new AxisRepository();

        // Сбрасываем interrupt flag на случай,
        // если предыдущий тест его установил.
        Thread.interrupted();
    }

    // =====================================================================
    // getVersion()
    // =====================================================================

    @Test
    void getVersion_shouldReturnVersion_whenResponseContainsFirmwareVersion()
            throws Exception {

        Camera camera = createCamera();

        CloseableHttpClient client =
                mock(CloseableHttpClient.class);

        HttpClientBuilder builder =
                createHttpClientBuilder(client);

        String body =
                "root.Properties.API.HTTP.Version=3\n" +
                        "root.Properties.Firmware.Version=9.30\n" +
                        "root.Properties.System.SerialNumber=123456\n";

        CloseableHttpResponse response =
                createResponse(200, body);

        when(client.execute(any(HttpUriRequest.class)))
                .thenReturn(response);

        try (MockedStatic<HttpClients> httpClients =
                     mockStatic(HttpClients.class)) {

            httpClients.when(HttpClients::custom)
                    .thenReturn(builder);

            String result =
                    repository.getVersion(camera);

            assertNotNull(result);
            assertEquals("9.30", result);

            verify(client, times(1))
                    .execute(any(HttpUriRequest.class));

            verify(client)
                    .close();
        }
    }

    @Test
    void getVersion_shouldTrimVersion()
            throws Exception {

        Camera camera = createCamera();

        CloseableHttpClient client =
                mock(CloseableHttpClient.class);

        HttpClientBuilder builder =
                createHttpClientBuilder(client);

        CloseableHttpResponse response =
                createResponse(
                        200,
                        "root.Properties.Firmware.Version=9.30   \n"
                );

        when(client.execute(any(HttpUriRequest.class)))
                .thenReturn(response);

        try (MockedStatic<HttpClients> httpClients =
                     mockStatic(HttpClients.class)) {

            httpClients.when(HttpClients::custom)
                    .thenReturn(builder);

            String result =
                    repository.getVersion(camera);

            assertEquals("9.30", result);
        }
    }

    @Test
    void getVersion_shouldReturnNull_whenFirmwareVersionIsAbsent()
            throws Exception {

        Camera camera = createCamera();

        CloseableHttpClient client =
                mock(CloseableHttpClient.class);

        HttpClientBuilder builder =
                createHttpClientBuilder(client);

        CloseableHttpResponse response =
                createResponse(
                        200,
                        "root.Properties.API.HTTP.Version=3\n"
                );

        when(client.execute(any(HttpUriRequest.class)))
                .thenReturn(response);

        try (MockedStatic<HttpClients> httpClients =
                     mockStatic(HttpClients.class)) {

            httpClients.when(HttpClients::custom)
                    .thenReturn(builder);

            /*
             * После первой попытки production-код делает sleep.
             * Прерываем поток, чтобы тест не ждал 1 + 2 сек.
             */
            Thread.currentThread().interrupt();

            try {
                String result =
                        repository.getVersion(camera);

                assertNull(result);
            } finally {
                Thread.interrupted();
            }
        }
    }

    // =====================================================================
    // getDateTimeForVersionBelow6()
    // =====================================================================

    @Test
    void getDateTimeForVersionBelow6_shouldReturnDateFromHtml()
            throws Exception {

        Camera camera = createCamera();

        CloseableHttpClient client =
                mock(CloseableHttpClient.class);

        HttpClientBuilder builder =
                createHttpClientBuilder(client);

        String html =
                "<html>" +
                        "<body>" +
                        "<input name=\"CurrentServerDate\" value=\"2026-09-06\" />" +
                        "<input name=\"CurrentServerTime\" value=\"12:30:45\" />" +
                        "</body>" +
                        "</html>";

        CloseableHttpResponse response =
                createResponse(200, html);

        when(client.execute(any(HttpUriRequest.class)))
                .thenReturn(response);

        Date expectedDate =
                new Date(1788690645000L);

        try (MockedStatic<HttpClients> httpClients =
                     mockStatic(HttpClients.class);

             MockedStatic<TimeConverter> timeConverter =
                     mockStatic(TimeConverter.class)) {

            httpClients.when(HttpClients::custom)
                    .thenReturn(builder);

            timeConverter.when(() ->
                            TimeConverter.convertStringToDateForAxis(
                                    "2026-09-06",
                                    "12:30:45"
                            ))
                    .thenReturn(expectedDate);

            Date result =
                    repository.getDateTimeForVersionBelow6(camera);

            assertNotNull(result);
            assertEquals(expectedDate, result);

            verify(camera)
                    .setStatusGetRequest(true);

            timeConverter.verify(() ->
                    TimeConverter.convertStringToDateForAxis(
                            "2026-09-06",
                            "12:30:45"
                    ));

            verify(client)
                    .close();
        }
    }

    @Test
    void getDateTimeForVersionBelow6_shouldReturnNull_whenHtmlHasNoDateFields()
            throws Exception {

        Camera camera = createCamera();

        CloseableHttpClient client =
                mock(CloseableHttpClient.class);

        HttpClientBuilder builder =
                createHttpClientBuilder(client);

        CloseableHttpResponse response =
                createResponse(
                        200,
                        "<html><body>No date here</body></html>"
                );

        when(client.execute(any(HttpUriRequest.class)))
                .thenReturn(response);

        try (MockedStatic<HttpClients> httpClients =
                     mockStatic(HttpClients.class)) {

            httpClients.when(HttpClients::custom)
                    .thenReturn(builder);

            Thread.currentThread().interrupt();

            try {
                Date result =
                        repository.getDateTimeForVersionBelow6(camera);

                assertNull(result);

                verify(camera, never())
                        .setStatusGetRequest(true);
            } finally {
                Thread.interrupted();
            }
        }
    }

    // =====================================================================
    // getServerDateTime()
    // =====================================================================

    @Test
    void getServerDateTime_shouldReturnConvertedDate()
            throws Exception {

        Camera camera = createCamera();

        CloseableHttpClient client =
                mock(CloseableHttpClient.class);

        HttpClientBuilder builder =
                createHttpClientBuilder(client);

        CloseableHttpResponse timeResponse =
                createResponse(
                        200,
                        "12:35:20"
                );

        CloseableHttpResponse dateResponse =
                createResponse(
                        200,
                        "2026-09-06"
                );

        when(client.execute(any(HttpUriRequest.class)))
                .thenReturn(
                        timeResponse,
                        dateResponse
                );

        Date ntpDate =
                new Date(1788690920000L);

        Date expectedDate =
                new Date(1788690920000L);

        try (MockedStatic<HttpClients> httpClients =
                     mockStatic(HttpClients.class);

             MockedConstruction<NTPTimeService> ntpServices =
                     mockConstruction(
                             NTPTimeService.class,
                             (mock, context) -> {

                                 when(mock.getNTPTime("pool.ntp.org"))
                                         .thenReturn(ntpDate);
                             });

             MockedStatic<TimeConverter> timeConverter =
                     mockStatic(TimeConverter.class)) {

            httpClients.when(HttpClients::custom)
                    .thenReturn(builder);

            timeConverter.when(() ->
                            TimeConverter.convertStringToDateForAxis(
                                    "2026-09-06",
                                    "12:35:20"
                            ))
                    .thenReturn(expectedDate);

            Date result =
                    repository.getServerDateTime(
                            camera,
                            "pool.ntp.org"
                    );

            assertNotNull(result);
            assertEquals(expectedDate, result);

            verify(camera)
                    .setStatusGetRequest(true);

            verify(client, times(2))
                    .execute(any(HttpUriRequest.class));

            assertEquals(
                    1,
                    ntpServices.constructed().size()
            );

            verify(ntpServices.constructed().get(0))
                    .getNTPTime("pool.ntp.org");
        }
    }

    // =====================================================================
    // postTimeZoneOld()
    // =====================================================================

    @Test
    void postTimeZoneOld_shouldSendTwoRequests_whenSuccessful()
            throws Exception {

        Camera camera = createCamera();

        CloseableHttpClient client =
                mock(CloseableHttpClient.class);

        HttpClientBuilder builder =
                createHttpClientBuilder(client);

        CloseableHttpResponse syncResponse =
                createResponse(
                        200,
                        ""
                );

        CloseableHttpResponse timezoneResponse =
                createResponse(
                        200,
                        "OK"
                );

        when(client.execute(any(HttpUriRequest.class)))
                .thenReturn(
                        syncResponse,
                        timezoneResponse
                );

        try (MockedStatic<HttpClients> httpClients =
                     mockStatic(HttpClients.class)) {

            httpClients.when(HttpClients::custom)
                    .thenReturn(builder);

            repository.postTimeZoneOld(camera);

            verify(client, times(2))
                    .execute(any(HttpUriRequest.class));

            verify(client)
                    .close();
        }
    }

    @Test
    void postTimeZoneOld_shouldNotSendTimezoneRequest_whenSyncSourceFails()
            throws Exception {

        Camera camera = createCamera();

        CloseableHttpClient client =
                mock(CloseableHttpClient.class);

        HttpClientBuilder builder =
                createHttpClientBuilder(client);

        CloseableHttpResponse syncResponse =
                createResponse(
                        500,
                        "ERROR"
                );

        when(client.execute(any(HttpUriRequest.class)))
                .thenReturn(syncResponse);

        try (MockedStatic<HttpClients> httpClients =
                     mockStatic(HttpClients.class)) {

            httpClients.when(HttpClients::custom)
                    .thenReturn(builder);

            Thread.currentThread().interrupt();

            try {
                repository.postTimeZoneOld(camera);

                verify(client, times(1))
                        .execute(any(HttpUriRequest.class));

            } finally {
                Thread.interrupted();
            }
        }
    }

    // =====================================================================
    // getConfigurationDateTime()
    // =====================================================================

    @Test
    void getConfigurationDateTime_shouldUpdateCamera_whenSuccessful()
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
                new Date(1788690920000L);

        try (MockedStatic<HttpClients> httpClients =
                     mockStatic(HttpClients.class);

             MockedConstruction<NTPTimeService> ntpServices =
                     mockConstruction(
                             NTPTimeService.class,
                             (mock, context) -> {

                                 when(mock.getNTPTime("pool.ntp.org"))
                                         .thenReturn(ntpDate);
                             });

             MockedStatic<MessageForSeq> messages =
                     mockStatic(MessageForSeq.class);

             MockedStatic<Log> log =
                     mockStatic(Log.class)) {

            httpClients.when(HttpClients::custom)
                    .thenReturn(builder);

            messages.when(() ->
                            MessageForSeq.getMessageUpdateMode(
                                    camera,
                                    ntpDate,
                                    true
                            ))
                    .thenReturn(
                            "Camera time updated"
                    );

            repository.getConfigurationDateTime(
                    camera,
                    "pool.ntp.org",
                    true
            );

            verify(camera)
                    .setDateUpdateSuccess(ntpDate);

            verify(camera)
                    .setStatusGetRequest(true);

            verify(camera)
                    .setDifferenceTime(0);

            log.verify(() ->
                    Log.information(
                            "Camera time updated"
                    ));

            verify(client, times(1))
                    .execute(any(HttpUriRequest.class));
        }
    }

    // =====================================================================
    // postTimeZoneNew()
    // =====================================================================

    @Test
    void postTimeZoneNew_shouldSendSyncAndTimezoneRequests()
            throws Exception {

        Camera camera = createCamera();

        CloseableHttpClient client =
                mock(CloseableHttpClient.class);

        HttpClientBuilder builder =
                createHttpClientBuilder(client);

        CloseableHttpResponse syncResponse =
                createResponse(
                        200,
                        ""
                );

        CloseableHttpResponse timezoneResponse =
                createResponse(
                        200,
                        ""
                );

        when(client.execute(any(HttpUriRequest.class)))
                .thenReturn(
                        syncResponse,
                        timezoneResponse
                );

        try (MockedStatic<HttpClients> httpClients =
                     mockStatic(HttpClients.class)) {

            httpClients.when(HttpClients::custom)
                    .thenReturn(builder);

            repository.postTimeZoneNew(camera);

            verify(client, times(2))
                    .execute(any(HttpUriRequest.class));

            verify(client)
                    .close();
        }
    }

    @Test
    void postTimeZoneNew_shouldNotSendPost_whenSyncRequestFails()
            throws Exception {

        Camera camera = createCamera();

        CloseableHttpClient client =
                mock(CloseableHttpClient.class);

        HttpClientBuilder builder =
                createHttpClientBuilder(client);

        CloseableHttpResponse response =
                createResponse(
                        500,
                        "ERROR"
                );

        when(client.execute(any(HttpUriRequest.class)))
                .thenReturn(response);

        try (MockedStatic<HttpClients> httpClients =
                     mockStatic(HttpClients.class)) {

            httpClients.when(HttpClients::custom)
                    .thenReturn(builder);

            Thread.currentThread().interrupt();

            try {
                repository.postTimeZoneNew(camera);

                verify(client, times(1))
                        .execute(any(HttpUriRequest.class));

            } finally {
                Thread.interrupted();
            }
        }
    }

    // =====================================================================
    // postGetDateTime()
    // =====================================================================

    @Test
    void postGetDateTime_shouldParseAxisJsonAndReturnDate()
            throws Exception {

        Camera camera = createCamera();

        CloseableHttpClient client =
                mock(CloseableHttpClient.class);

        HttpClientBuilder builder =
                createHttpClientBuilder(client);

        String json =
                "{" +
                        "\"data\":{" +
                        "\"dateTime\":\"2026-09-06T12:30:00+03:00\"" +
                        "}" +
                        "}";

        CloseableHttpResponse response =
                createResponse(
                        200,
                        json
                );

        when(client.execute(any(HttpUriRequest.class)))
                .thenReturn(response);

        Date expected =
                Date.from(
                        OffsetDateTime.parse(
                                "2026-09-06T12:30:00+03:00"
                        ).toInstant()
                );

        try (MockedStatic<HttpClients> httpClients =
                     mockStatic(HttpClients.class)) {

            httpClients.when(HttpClients::custom)
                    .thenReturn(builder);

            Date result =
                    repository.postGetDateTime(camera);

            assertNotNull(result);
            assertEquals(expected, result);

            verify(camera)
                    .setStatusGetRequest(true);

            verify(client, times(1))
                    .execute(any(HttpUriRequest.class));

            verify(client)
                    .close();
        }
    }

    @Test
    void postGetDateTime_shouldParseJsonAfterPrefix()
            throws Exception {

        Camera camera = createCamera();

        CloseableHttpClient client =
                mock(CloseableHttpClient.class);

        HttpClientBuilder builder =
                createHttpClientBuilder(client);

        /*
         * Ваш production-код специально ищет первый '{',
         * поэтому проверяем вариант с текстом перед JSON.
         */
        String body =
                "Axis response:\n" +
                        "{\"data\":{" +
                        "\"dateTime\":\"2026-09-06T14:45:30+03:00\"" +
                        "}}";

        CloseableHttpResponse response =
                createResponse(
                        200,
                        body
                );

        when(client.execute(any(HttpUriRequest.class)))
                .thenReturn(response);

        Date expected =
                Date.from(
                        OffsetDateTime.parse(
                                "2026-09-06T14:45:30+03:00"
                        ).toInstant()
                );

        try (MockedStatic<HttpClients> httpClients =
                     mockStatic(HttpClients.class)) {

            httpClients.when(HttpClients::custom)
                    .thenReturn(builder);

            Date result =
                    repository.postGetDateTime(camera);

            assertEquals(expected, result);

            verify(camera)
                    .setStatusGetRequest(true);
        }
    }

    @Test
    void postGetDateTime_shouldReturnNull_whenJsonIsMissing()
            throws Exception {

        Camera camera = createCamera();

        CloseableHttpClient client =
                mock(CloseableHttpClient.class);

        HttpClientBuilder builder =
                createHttpClientBuilder(client);

        CloseableHttpResponse response =
                createResponse(
                        200,
                        "Response without JSON"
                );

        when(client.execute(any(HttpUriRequest.class)))
                .thenReturn(response);

        try (MockedStatic<HttpClients> httpClients =
                     mockStatic(HttpClients.class)) {

            httpClients.when(HttpClients::custom)
                    .thenReturn(builder);

            Thread.currentThread().interrupt();

            try {

                Date result =
                        repository.postGetDateTime(camera);

                assertNull(result);

                verify(camera, never())
                        .setStatusGetRequest(true);

            } finally {
                Thread.interrupted();
            }
        }
    }

    // =====================================================================
    // postChangeDatetime()
    // =====================================================================

    @Test
    void postChangeDatetime_shouldUpdateCamera_whenSuccessful()
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
                Date.from(
                        OffsetDateTime.parse(
                                "2026-09-06T12:30:00+03:00"
                        ).toInstant()
                );

        try (MockedStatic<HttpClients> httpClients =
                     mockStatic(HttpClients.class);

             MockedConstruction<NTPTimeService> ntpServices =
                     mockConstruction(
                             NTPTimeService.class,
                             (mock, context) -> {

                                 when(mock.getNTPTime("pool.ntp.org"))
                                         .thenReturn(ntpDate);
                             });

             MockedStatic<MessageForSeq> messages =
                     mockStatic(MessageForSeq.class);

             MockedStatic<Log> log =
                     mockStatic(Log.class)) {

            httpClients.when(HttpClients::custom)
                    .thenReturn(builder);

            messages.when(() ->
                            MessageForSeq.getMessageUpdateMode(
                                    camera,
                                    ntpDate,
                                    true
                            ))
                    .thenReturn(
                            "Axis datetime updated"
                    );

            repository.postChangeDatetime(
                    camera,
                    "pool.ntp.org",
                    true
            );

            verify(camera)
                    .setDateUpdateSuccess(ntpDate);

            verify(camera)
                    .setStatusGetRequest(true);

            verify(camera)
                    .setDifferenceTime(0);

            log.verify(() ->
                    Log.information(
                            "Axis datetime updated"
                    ));

            verify(client, times(1))
                    .execute(any(HttpUriRequest.class));
        }
    }

    @Test
    void postChangeDatetime_shouldSetStatusFalse_whenRequestFails()
            throws Exception {

        Camera camera = createCamera();

        CloseableHttpClient client =
                mock(CloseableHttpClient.class);

        HttpClientBuilder builder =
                createHttpClientBuilder(client);

        CloseableHttpResponse response1 =
                createResponse(
                        500,
                        "ERROR"
                );

        CloseableHttpResponse response2 =
                createResponse(
                        500,
                        "ERROR"
                );

        CloseableHttpResponse response3 =
                createResponse(
                        500,
                        "ERROR"
                );

        when(client.execute(any(HttpUriRequest.class)))
                .thenReturn(
                        response1,
                        response2,
                        response3
                );

        Date ntpDate =
                new Date(1788690920000L);

        try (
                MockedStatic<HttpClients> httpClients =
                        mockStatic(HttpClients.class);

                MockedConstruction<NTPTimeService> ntpServices =
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
                        )
        ) {

            httpClients.when(HttpClients::custom)
                    .thenReturn(builder);

            repository.postChangeDatetime(
                    camera,
                    "pool.ntp.org",
                    true
            );

            /*
             * Было 3 неудачных HTTP-запроса.
             */
            verify(client, times(3))
                    .execute(any(HttpUriRequest.class));

            /*
             * При полном провале status должен стать false.
             */
            verify(camera, times(1))
                    .setStatusGetRequest(false);

            /*
             * Успешные поля меняться не должны.
             */
            verify(camera, never())
                    .setDateUpdateSuccess(any(Date.class));

            verify(camera, never())
                    .setStatusGetRequest(true);

            verify(camera, never())
                    .setDifferenceTime(anyInt());

            /*
             * NTP вызывается только один раз до retry-цикла.
             */
            assertEquals(
                    1,
                    ntpServices.constructed().size()
            );

            verify(ntpServices.constructed().get(0), times(1))
                    .getNTPTime("pool.ntp.org");

            verify(client)
                    .close();
        }
    }

    // =====================================================================
    // IOException
    // =====================================================================

    @Test
    void getVersion_shouldReturnNull_whenExecuteThrowsIOException()
            throws Exception {

        Camera camera = createCamera();

        CloseableHttpClient client =
                mock(CloseableHttpClient.class);

        HttpClientBuilder builder =
                createHttpClientBuilder(client);

        when(client.execute(any(HttpUriRequest.class)))
                .thenThrow(
                        new IOException(
                                "Connection failed"
                        )
                );

        try (MockedStatic<HttpClients> httpClients =
                     mockStatic(HttpClients.class)) {

            httpClients.when(HttpClients::custom)
                    .thenReturn(builder);

            Thread.currentThread().interrupt();

            try {

                String result =
                        repository.getVersion(camera);

                assertNull(result);

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
         * Реальная версия ПО Axis,
         * которую вы приводили ранее.
         */
        when(camera.getVersion())
                .thenReturn(
                        "9.30"
                );

        return camera;
    }

    /**
     * Создает цепочку:
     *
     * HttpClients.custom()
     *      .setDefaultCredentialsProvider(...)
     *      .build()
     *
     * -> mocked CloseableHttpClient
     */
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
     * ВАЖНО:
     *
     * AxisRepository использует CloseableHttpClient.
     * Поэтому execute() фактически возвращает
     * CloseableHttpResponse.
     *
     * Здесь вообще нет BasicHttpResponse
     * и нет никакого приведения типов.
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

        when(statusLine.getStatusCode())
                .thenReturn(
                        statusCode
                );

        when(response.getStatusLine())
                .thenReturn(
                        statusLine
                );

        HttpEntity entity =
                new StringEntity(
                        body,
                        StandardCharsets.UTF_8
                );

        when(response.getEntity())
                .thenReturn(
                        entity
                );

        return response;
    }
}