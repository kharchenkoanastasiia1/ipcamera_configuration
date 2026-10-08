package org.ipcamera.config.repository.cameras_request;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.ipcamera.config.entity.Camera;
import org.ipcamera.config.service.MessageForSeq;
import org.ipcamera.config.service.time.NTPTimeService;
import org.ipcamera.config.service.converter.TimeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serilogj.Log;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.ipcamera.config.constants.Constants.*;
import static org.ipcamera.config.constants.ConstantsLogger.*;
import static org.ipcamera.config.constants.ConstantsRequest.*;

public class IPCameraRepository {
    private static final Logger logger = LoggerFactory.getLogger(IPCameraRepository.class);

    public IPCameraRepository() {}

    public Date getServerTime(Camera camera) {
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(camera.getUsername(), camera.getPassword()));

        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultCredentialsProvider(provider)
                .build()) {

            HttpGet request = new HttpGet("http://" + camera.getIpAddress() + GET_TIME_IP_CAMERA);


            int maxAttempts = 3;

            for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                try {
                    HttpResponse response = client.execute(request);
                    int statusCode = response.getStatusLine().getStatusCode();
                    String responseBody = EntityUtils.toString(response.getEntity());

                    if (statusCode >= 200 && statusCode < 300) {
                        String time = responseBody.replaceAll(REGEX_DATE_IPCAMERA, "$1");
                        SimpleDateFormat serverFormat = new SimpleDateFormat(PATTERN_DATE_FORMAT_IPCAMERA_FROM_SERVER);
                        Date date = serverFormat.parse(time);

                        if (date != null) {
                            camera.setStatusGetRequest(true);
                            return date;
                        }
                    }
                } catch (HttpHostConnectException | UnknownHostException e) {
                    logger.error(CONNECTION_ERROR, camera.getIpAddress(), camera.getVersion());
                } catch (ParseException e) {
                    logger.error(ERROR_PARSE_DATE, camera.getIpAddress(), camera.getVersion());
                } catch (IOException e) {
                    logger.error(ERROR_EXECUTE_REQUEST, camera.getIpAddress(), camera.getVersion());
                } catch (Exception e) {
                    logger.error(ERROR_UNEXPECTED, camera.getIpAddress(), camera.getVersion());
                }

                if (attempt < maxAttempts) {
                    long delay = (long) Math.pow(2, attempt - 1) * 1000L;
                    Thread.sleep(delay);
                }
            }

        } catch (Exception e) {
            logger.error(ERROR_UNEXPECTED, camera.getIpAddress(), camera.getVersion());
        }
        return null;
    }

    public void postServerTime(Camera camera, String ntpHost, Boolean updateMode) {
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(camera.getUsername(), camera.getPassword()));

        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultCredentialsProvider(provider)
                .build()) {

            HttpPost request = new HttpPost("http://" + camera.getIpAddress() + POST_TIME_IP_CAMERA);
            request.setHeader("Content-Type", "text/plain");
            NTPTimeService time = new NTPTimeService();
            Date date = time.getNTPTime(ntpHost);
            String body = String.format(POST_TIME_IP_CAMERA_BODY, TimeConverter.formatDateTimeForIPCamera(date), TIME_ZONE_IP);
            request.setEntity(new StringEntity(body));

            int maxAttempts = 3;

            for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                try {
                    HttpResponse response = client.execute(request);
                    int statusCode = response.getStatusLine().getStatusCode();
                    String responseBody = EntityUtils.toString(response.getEntity());

                    if (statusCode >= 200 && statusCode < 300 && responseBody.toLowerCase().contains("ok")) {
                        camera.setDateUpdateSuccess(date);
                        camera.setDifferenceTime(0);
                        Log.information(MessageForSeq.getMessageUpdateMode(camera, date, updateMode));
                        break;
                    }
                } catch (HttpHostConnectException | UnknownHostException e) {
                    logger.error(CONNECTION_ERROR, camera.getIpAddress(), camera.getVersion());
                } catch (IOException e) {
                    logger.error(ERROR_EXECUTE_REQUEST, camera.getIpAddress(), camera.getVersion());
                } catch (Exception e) {
                    logger.error(ERROR_UNEXPECTED, camera.getIpAddress(), camera.getVersion());
                }

                if (attempt < maxAttempts) {
                    long delay = (long) Math.pow(2, attempt - 1) * 1000L;
                    Thread.sleep(delay);
                }
            }

        } catch (Exception e) {
            logger.error(ERROR_UNEXPECTED, camera.getIpAddress(), camera.getVersion());
        }
    }

    public String getVersion(Camera camera) {
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(camera.getUsername(), camera.getPassword()));

        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultCredentialsProvider(provider)
                .build()) {
            HttpGet request = new HttpGet("http://" + camera.getIpAddress() + GET_VERSION_IP_CAMERA);

            int maxAttempts = 3;
            for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                try {
                    HttpResponse response = client.execute(request);
                    int statusCode = response.getStatusLine().getStatusCode();
                    String responseBody = EntityUtils.toString(response.getEntity());

                    if (statusCode >= 200 && statusCode < 300) {
                        Pattern pattern = Pattern.compile(REGEX_VERSION_IPCAMERA);
                        Matcher matcher = pattern.matcher(responseBody);

                        if (matcher.find()) {
                            return matcher.group(1);
                        }
                    }
                } catch (HttpHostConnectException | UnknownHostException e) {
                    logger.error(CONNECTION_ERROR, camera.getIpAddress(), camera.getVersion());
                } catch (IOException e) {
                    logger.error(ERROR_EXECUTE_REQUEST, camera.getIpAddress(), camera.getVersion());
                } catch (Exception e) {
                    logger.error(ERROR_UNEXPECTED, camera.getIpAddress(), camera.getVersion());
                }
            }

        } catch (Exception e) {
            logger.error(ERROR_UNEXPECTED, camera.getIpAddress(), camera.getVersion());
        }
        return null;
    }
}
