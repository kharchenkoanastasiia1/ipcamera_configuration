package org.ipcamera.config.repository.cameras_request;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.ipcamera.config.entity.Camera;
import org.ipcamera.config.service.MessageForSeq;
import org.ipcamera.config.service.time.NTPTimeService;
import org.ipcamera.config.service.converter.TimeConverter;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serilogj.Log;

import java.io.IOException;

import java.net.UnknownHostException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;


import static org.ipcamera.config.constants.Constants.TIME_ZONE_AXIS;
import static org.ipcamera.config.constants.ConstantsLogger.*;
import static org.ipcamera.config.constants.ConstantsRequest.*;

public class AxisRepository {
    private static final Logger logger = LoggerFactory.getLogger(AxisRepository.class);

    public AxisRepository() {}

    public String getVersion(Camera camera) {
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(camera.getUsername(), camera.getPassword()));

        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultCredentialsProvider(provider)
                .build()) {

            int maxAttempts = 3;
            String version = null;

            for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                try {
                    HttpGet request = new HttpGet("http://" + camera.getIpAddress() + GET_ALL_CONFIGURATION_PROPERTIES_AXIS);
                    HttpResponse response = client.execute(request);
                    int statusCode = response.getStatusLine().getStatusCode();
                    String responseBody = EntityUtils.toString(response.getEntity());

                    if (statusCode >= 200 && statusCode < 300) {
                        for (String line : responseBody.split("\n")) {
                            if (line.startsWith("root.Properties.Firmware.Version=")) {
                                version = line.split("=")[1].trim();
                                return version;
                            }
                        }
                    }
                } catch (HttpHostConnectException | UnknownHostException e) {
                    logger.error(CONNECTION_ERROR, camera.getIpAddress(), camera.getVersion());
//                    Log.error(String.format(CONNECTION_ERROR_SEQ, camera.getIpAddress(), camera.getVersion()));
                } catch (IOException e) {
                    logger.error(ERROR_EXECUTE_REQUEST, camera.getIpAddress(), camera.getVersion());
//                    Log.error(String.format(ERROR_EXECUTE_REQUEST_SEQ, camera.getIpAddress(), camera.getVersion()));
                } catch (Exception e) {
                    logger.error(ERROR_UNEXPECTED, camera.getIpAddress(), camera.getVersion());
//                    Log.error(String.format(ERROR_UNEXPECTED_SEQ, camera.getIpAddress(), camera.getVersion()));
                }

                if (attempt < maxAttempts) {
                    long delay = (long) Math.pow(2, attempt - 1) * 1000L; // 1s, 2s, 4s
                    Thread.sleep(delay);
                }
            }

        } catch (Exception e) {
            logger.error(ERROR_UNEXPECTED, camera.getIpAddress(), camera.getVersion());
//            Log.error(String.format(ERROR_UNEXPECTED_SEQ, camera.getIpAddress(), camera.getVersion()));
        }
        return null;
    }

    //==========================VERSION 5.50 <============================

    public Date getDateTimeForVersionBelow6(Camera camera){
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(camera.getUsername(), camera.getPassword()));

            try (CloseableHttpClient httpClient = HttpClients.custom()
                    .setDefaultCredentialsProvider(credsProvider)
                    .build()) {
                HttpGet request = new HttpGet("http://" + camera.getIpAddress() + "/admin/date.shtml");

                int maxAttempts = 3;
                for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                    try(CloseableHttpResponse response = httpClient.execute(request)){
                        int statusCode = response.getStatusLine().getStatusCode();
                        if (statusCode >= 200 && statusCode < 300) {
                            String html = EntityUtils.toString(response.getEntity());
                            Document doc = Jsoup.parse(html, "UTF-8");

                            Element dateEl = doc.selectFirst("input[name=CurrentServerDate]");
                            Element timeEl = doc.selectFirst("input[name=CurrentServerTime]");

                            if (dateEl != null && timeEl != null) {
                                String date = dateEl.attr("value");
                                String time = timeEl.attr("value");

                                Date dateResult = TimeConverter.convertStringToDateForAxis(date, time);
                                if (dateResult != null) {
                                    camera.setStatusGetRequest(true);
                                    return dateResult;
                                }
                            }
                        }
                    } catch (HttpHostConnectException | UnknownHostException e) {
                        logger.error(CONNECTION_ERROR, camera.getIpAddress(), camera.getVersion());
//                        Log.error(String.format(CONNECTION_ERROR_SEQ, camera.getIpAddress(), camera.getVersion()));
                    } catch (IOException e) {
                        logger.error(ERROR_EXECUTE_REQUEST, camera.getIpAddress(), camera.getVersion());
//                        Log.error(String.format(ERROR_EXECUTE_REQUEST_SEQ, camera.getIpAddress(), camera.getVersion()));
                    } catch (Exception e) {
                        logger.error(ERROR_UNEXPECTED, camera.getIpAddress(), camera.getVersion());
//                        Log.error(String.format(ERROR_UNEXPECTED_SEQ, camera.getIpAddress(), camera.getVersion()));
                    }

                    if (attempt < maxAttempts) {
                        long delay = (long) Math.pow(2, attempt - 1) * 1000L; // 1s, 2s, 4s
                        Thread.sleep(delay);
                    }
                }

            } catch (Exception e) {
                logger.error(ERROR_UNEXPECTED, camera.getIpAddress(), camera.getVersion());
//                Log.error(String.format(ERROR_UNEXPECTED_SEQ, camera.getIpAddress(), camera.getVersion()));
            }
        return null;
    }

    //==========================VERSION 5.50 > AND < 9.30============================

    public Date getServerDateTime(Camera camera, String ntpHost) throws Exception {
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(camera.getUsername(), camera.getPassword()));

        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultCredentialsProvider(provider)
                .build()) {

            int maxAttempts = 3;

            NTPTimeService timeNtp = new NTPTimeService();

            for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                try {
                    Date dateNtp = timeNtp.getNTPTime(ntpHost);

                    HttpGet requestTime = new HttpGet("http://" + camera.getIpAddress() + GET_OLD_TIME_AXIS + dateNtp.toInstant().toEpochMilli());
                    HttpResponse responseTime = client.execute(requestTime);
                    String time = EntityUtils.toString(responseTime.getEntity());

                    HttpGet requestDate = new HttpGet("http://" + camera.getIpAddress() + GET_OLD_DATE_AXIS + dateNtp.toInstant().toEpochMilli());
                    HttpResponse responseDate = client.execute(requestDate);
                    String date = EntityUtils.toString(responseDate.getEntity());

                    Date dateResult = TimeConverter.convertStringToDateForAxis(date, time);
                    if (dateResult != null) {
                        camera.setStatusGetRequest(true);
                        return dateResult;
                    }
                } catch (HttpHostConnectException | UnknownHostException e) {
                    logger.error(CONNECTION_ERROR, camera.getIpAddress(), camera.getVersion());
//                    Log.error(String.format(CONNECTION_ERROR_SEQ, camera.getIpAddress(), camera.getVersion()));
                } catch (IOException e) {
                    logger.error(ERROR_EXECUTE_REQUEST, camera.getIpAddress(), camera.getVersion());
//                    Log.error(String.format(ERROR_EXECUTE_REQUEST_SEQ, camera.getIpAddress(), camera.getVersion()));
                } catch (Exception e) {
                    logger.error(ERROR_UNEXPECTED, camera.getIpAddress(), camera.getVersion());
//                    Log.error(String.format(ERROR_UNEXPECTED_SEQ, camera.getIpAddress(), camera.getVersion()));
                }

                if (attempt < maxAttempts) {
                    long delay = (long) Math.pow(2, attempt - 1) * 1000L; // 1s, 2s, 4s
                    Thread.sleep(delay);
                }
            }

        } catch (Exception e) {
            logger.error(ERROR_UNEXPECTED, camera.getIpAddress(), camera.getVersion());
//            Log.error(String.format(ERROR_UNEXPECTED_SEQ, camera.getIpAddress(), camera.getVersion()));
        }
        return null;
    }

    public void postTimeZoneOld(Camera camera) {
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(camera.getUsername(), camera.getPassword()));

        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultCredentialsProvider(provider)
                .build()) {

            HttpGet requestSyncSource = new HttpGet("http://" + camera.getIpAddress() + POST_CONFIGURATION_SYNCSOURCE_AXIS);

            String url = String.format("http://" + camera.getIpAddress() + POST_OLD_TIMEZONE_AXIS + TIME_ZONE_AXIS);

            HttpGet request = new HttpGet(url);

            int maxAttempts = 3;
            boolean success = false;
            for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                try {
                    HttpResponse responseSyncSource = client.execute(requestSyncSource);
                    int statusCode = responseSyncSource.getStatusLine().getStatusCode();
                    if (statusCode >= 200 && statusCode < 300) {
                        HttpResponse response = client.execute(request);
                        statusCode = response.getStatusLine().getStatusCode();
                        String responseBody = EntityUtils.toString(response.getEntity());

                        if (statusCode >= 200 && statusCode < 300 && responseBody.contains("OK")) {
                            logger.info(TIMEZONE_CHANGE_SUCCESS, camera.getIpAddress());
//                            Log.information(String.format(TIMEZONE_CHANGE_SUCCESS_SEQ, camera.getIpAddress()));
                            success = true;
                            break;
                        }
                    }
                } catch (HttpHostConnectException | UnknownHostException e) {
                    logger.error(CONNECTION_ERROR, camera.getIpAddress(), camera.getVersion());
//                    Log.error(String.format(CONNECTION_ERROR_SEQ, camera.getIpAddress(), camera.getVersion()));
                } catch (IOException e) {
                    logger.error(ERROR_EXECUTE_REQUEST, camera.getIpAddress(), camera.getVersion());
//                    Log.error(String.format(ERROR_EXECUTE_REQUEST_SEQ, camera.getIpAddress(), camera.getVersion()));
                } catch (Exception e) {
                    logger.error(ERROR_UNEXPECTED, camera.getIpAddress(), camera.getVersion());
//                    Log.error(String.format(ERROR_UNEXPECTED_SEQ, camera.getIpAddress(), camera.getVersion()));
                }

                if (!success && attempt < maxAttempts) {
                    long delay = (long) Math.pow(2, attempt - 1) * 1000L; // 1s, 2s, 4s
                    Thread.sleep(delay);
                }
            }

        } catch (Exception e) {
            logger.error(ERROR_UNEXPECTED, camera.getIpAddress(), camera.getVersion());
//            Log.error(String.format(ERROR_UNEXPECTED_SEQ, camera.getIpAddress(), camera.getVersion()));
        }
    }

    public void getConfigurationDateTime(Camera camera, String ntpHost, Boolean updateMode){
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(camera.getUsername(), camera.getPassword()));

        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultCredentialsProvider(provider)
                .build()) {

            NTPTimeService time = new NTPTimeService();
            Date date = time.getNTPTime(ntpHost);
            LocalDateTime localDate = date.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            String url = String.format("http://%s" + POST_OLD_DATE_TIME_AXIS, camera.getIpAddress()
                    , localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth()
                    , localDate.getHour(), localDate.getMinute(), localDate.getSecond()
                    , date.toInstant().toEpochMilli());

            HttpGet request = new HttpGet(url);

            int maxAttempts = 3;
            boolean success = false;

            for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                try {
                    HttpResponse response = client.execute(request);
                    int statusCode = response.getStatusLine().getStatusCode();

                    if (statusCode >= 200 && statusCode < 300) {
                        camera.setDateUpdateSuccess(date);
                        camera.setStatusGetRequest(true);
                        camera.setDifferenceTime(0);
                        logger.info(TIME_CHANGE_SUCCESS, camera.getIpAddress());
//                        Log.information(String.format(TIME_CHANGE_SUCCESS_SEQ, camera.getIpAddress()));
                        Log.information(MessageForSeq.getMessageUpdateMode(camera, date, updateMode));
                        success = true;
                        break;
                    }
                } catch (HttpHostConnectException | UnknownHostException e) {
                    logger.error(CONNECTION_ERROR, camera.getIpAddress(), camera.getVersion());
//                    Log.error(String.format(CONNECTION_ERROR_SEQ, camera.getIpAddress(), camera.getVersion()));
                } catch (IOException e) {
                    logger.error(ERROR_EXECUTE_REQUEST, camera.getIpAddress(), camera.getVersion());
//                    Log.error(String.format(ERROR_EXECUTE_REQUEST_SEQ, camera.getIpAddress(), camera.getVersion()));
                } catch (Exception e) {
                    logger.error(ERROR_UNEXPECTED, camera.getIpAddress(), camera.getVersion());
//                    Log.error(String.format(ERROR_UNEXPECTED_SEQ, camera.getIpAddress(), camera.getVersion()));
                }

                if (!success && attempt < maxAttempts) {
                    long delay = (long) Math.pow(2, attempt - 1) * 1000L; // 1s, 2s, 4s
                    Thread.sleep(delay);
                }
            }

            if (!success) {
                camera.setStatusGetRequest(false);
            }

        } catch (Exception e) {
            logger.error(ERROR_UNEXPECTED, camera.getIpAddress(), camera.getVersion());
//            Log.error(String.format(ERROR_UNEXPECTED_SEQ, camera.getIpAddress(), camera.getVersion()));
        }
    }

    //===========================NEW===========================

    public void postTimeZoneNew(Camera camera){
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(camera.getUsername(), camera.getPassword()));

        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultCredentialsProvider(provider)
                .build()) {

            HttpGet requestSyncSource = new HttpGet("http://" + camera.getIpAddress() + POST_CONFIGURATION_SYNCSOURCE_AXIS);

            HttpPost request = new HttpPost("http://" + camera.getIpAddress() + "/axis-cgi/time.cgi");
            request.setHeader("Content-Type", "application/json");
            request.setEntity(new StringEntity(POST_NEW_TIMEZONE_AXIS));

            int maxAttempts = 3;
            boolean success = false;
            for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                try {
                    HttpResponse responseSyncSource = client.execute(requestSyncSource);
                    int statusCode = responseSyncSource.getStatusLine().getStatusCode();
                    if (statusCode >= 200 && statusCode < 300) {
                        HttpResponse response = client.execute(request);
                        statusCode = response.getStatusLine().getStatusCode();

                        if (statusCode >= 200 && statusCode < 300) {
                            logger.info(TIMEZONE_CHANGE_SUCCESS, camera.getIpAddress());
//                            Log.information(String.format(TIMEZONE_CHANGE_SUCCESS_SEQ, camera.getIpAddress()));
                            success = true;
                            break;
                        }
                    }
                } catch (HttpHostConnectException | UnknownHostException e) {
                    logger.error(CONNECTION_ERROR, camera.getIpAddress(), camera.getVersion());
//                    Log.error(String.format(CONNECTION_ERROR_SEQ, camera.getIpAddress(), camera.getVersion()));
                } catch (IOException e) {
                    logger.error(ERROR_EXECUTE_REQUEST, camera.getIpAddress(), camera.getVersion());
//                    Log.error(String.format(ERROR_EXECUTE_REQUEST_SEQ, camera.getIpAddress(), camera.getVersion()));
                } catch (Exception e) {
                    logger.error(ERROR_UNEXPECTED, camera.getIpAddress(), camera.getVersion());
//                    Log.error(String.format(ERROR_UNEXPECTED_SEQ, camera.getIpAddress(), camera.getVersion()));
                }

                if (!success && attempt < maxAttempts) {
                    long delay = (long) Math.pow(2, attempt - 1) * 1000L; // 1s, 2s, 4s
                    Thread.sleep(delay);
                }
            }

        } catch (Exception e) {
            logger.error(ERROR_UNEXPECTED, camera.getIpAddress(), camera.getVersion());
//            Log.error(String.format(ERROR_UNEXPECTED_SEQ, camera.getIpAddress(), camera.getVersion()));
        }
    }

    public Date postGetDateTime(Camera camera){
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(camera.getUsername(), camera.getPassword()));

        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultCredentialsProvider(provider)
                .build()) {

            HttpPost request = new HttpPost("http://" + camera.getIpAddress() + "/axis-cgi/time.cgi");
            request.setHeader("Content-Type", "application/json");
            request.setEntity(new StringEntity(GET_NEW_DATE_TIME_AXIS));

            int maxAttempts = 3;
            int attempt = 0;
            boolean success = false;
            while (attempt < maxAttempts && !success) {
                attempt++;
                try {
                    HttpResponse response = client.execute(request);
                    int statusCode = response.getStatusLine().getStatusCode();
                    String responseString = EntityUtils.toString(response.getEntity());

                    if (statusCode >= 200 && statusCode < 300) {
                        int jsonStart = responseString.indexOf('{');
                        if (jsonStart == -1) {
                            throw new JSONException("No JSON found in response: " + responseString);
                        }

                        String jsonPart = responseString.substring(jsonStart);
                        JSONObject obj = new JSONObject(jsonPart);
                        JSONObject data = obj.getJSONObject("data");
                        String dateTime = data.getString("dateTime");

                        OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                        Date date = Date.from(offsetDateTime.toInstant());

                        camera.setStatusGetRequest(true);
                        success = true;
                        return date;
                    } else {
                        if (attempt < maxAttempts) {
                            long delay = (long) Math.pow(2, attempt - 1) * 1000L; // 1s, 2s, 4s
                            Thread.sleep(delay);
                        }
                    }

                } catch (HttpHostConnectException | UnknownHostException e) {
                    logger.error(CONNECTION_ERROR, camera.getIpAddress(), camera.getVersion());
//                    Log.error(String.format(CONNECTION_ERROR_SEQ, camera.getIpAddress(), camera.getVersion()));
                    if (attempt < maxAttempts) {
                        long delay = (long) Math.pow(2, attempt - 1) * 1000L;
                        Thread.sleep(delay);
                    }
                } catch (IOException e) {
                    logger.error(ERROR_EXECUTE_REQUEST, camera.getIpAddress(), camera.getVersion());
//                    Log.error(String.format(ERROR_EXECUTE_REQUEST_SEQ, camera.getIpAddress(), camera.getVersion()));
                    if (attempt < maxAttempts) {
                        long delay = (long) Math.pow(2, attempt - 1) * 1000L;
                        Thread.sleep(delay);
                    }
                } catch (Exception e) {
                    logger.error(ERROR_UNEXPECTED, camera.getIpAddress(), camera.getVersion());
//                    Log.error(String.format(ERROR_UNEXPECTED_SEQ, camera.getIpAddress(), camera.getVersion()));
                    if (attempt < maxAttempts) {
                        long delay = (long) Math.pow(2, attempt - 1) * 1000L;
                        Thread.sleep(delay);
                    }
                }
            }

        } catch (Exception e) {
            logger.error(ERROR_UNEXPECTED, camera.getIpAddress(), camera.getVersion(), e);
//            Log.error(String.format(ERROR_UNEXPECTED_SEQ, camera.getIpAddress(), camera.getVersion()), e);
        }
        return null;
    }

    public void postChangeDatetime(Camera camera, String ntpHost, Boolean updateMode) throws InterruptedException, IOException {
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(camera.getUsername(), camera.getPassword()));


        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultCredentialsProvider(provider)
                .build()) {

            NTPTimeService time = new NTPTimeService();
            Date date = time.getNTPTime(ntpHost);

            String isoString = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX")
                    .withZone(ZoneOffset.UTC)
                    .format(date.toInstant());

            HttpPost request = new HttpPost("http://" + camera.getIpAddress() + "/axis-cgi/time.cgi");
            request.setHeader("Content-Type", "application/json");

            String body = String.format(POST_NEW_DATE_TIME_AXIS, isoString);
            request.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));

            int maxAttempts = 3;
            int attempt = 0;
            boolean success = false;
            while (attempt < maxAttempts && !success) {
                attempt++;
                try {
                    HttpResponse response = client.execute(request);
                    int statusCode = response.getStatusLine().getStatusCode();

                    if (statusCode >= 200 && statusCode < 300) {
                        camera.setDateUpdateSuccess(date);
                        camera.setStatusGetRequest(true);
                        camera.setDifferenceTime(0);
                        logger.info(TIME_CHANGE_SUCCESS, camera.getIpAddress());
//                        Log.information(String.format(TIME_CHANGE_SUCCESS_SEQ, camera.getIpAddress()));
                        Log.information(MessageForSeq.getMessageUpdateMode(camera, date, updateMode));
                        success = true;
                    } else {
                        if (attempt < maxAttempts) {
                            long delay = (long) Math.pow(2, attempt - 1) * 1000L; // 1s, 2s, 4s
                            Thread.sleep(delay);
                        }
                    }

                } catch (HttpHostConnectException | UnknownHostException e) {
                    logger.error(CONNECTION_ERROR, camera.getIpAddress(), camera.getVersion());
//                    Log.error(String.format(CONNECTION_ERROR_SEQ, camera.getIpAddress(), camera.getVersion()));
                    if (attempt < maxAttempts) {
                        long delay = (long) Math.pow(2, attempt - 1) * 1000L;
                        Thread.sleep(delay);
                    }
                } catch (IOException e) {
                    logger.error(ERROR_EXECUTE_REQUEST, camera.getIpAddress(), camera.getVersion());
//                    Log.error(String.format(ERROR_EXECUTE_REQUEST_SEQ, camera.getIpAddress(), camera.getVersion()));
                    if (attempt < maxAttempts) {
                        long delay = (long) Math.pow(2, attempt - 1) * 1000L;
                        Thread.sleep(delay);
                    }
                } catch (Exception e) {
                    logger.error(ERROR_UNEXPECTED, camera.getIpAddress(), camera.getVersion());
//                    Log.error(String.format(ERROR_UNEXPECTED_SEQ, camera.getIpAddress(), camera.getVersion()));
                    if (attempt < maxAttempts) {
                        long delay = (long) Math.pow(2, attempt - 1) * 1000L;
                        Thread.sleep(delay);
                    }
                }
            }
            if (!success) {
                camera.setStatusGetRequest(false);
            }
        } catch (Exception e) {
            logger.error(ERROR_UNEXPECTED, camera.getIpAddress(), camera.getVersion());
//            Log.error(String.format(ERROR_UNEXPECTED_SEQ, camera.getIpAddress(), camera.getVersion()));
            throw new RuntimeException(e);
        }
    }
}
