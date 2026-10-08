package org.ipcamera.config.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Base64;

import static org.ipcamera.config.constants.ConstantsLogger.*;

public class CameraRepository {

    private static final Logger logger = LoggerFactory.getLogger(CameraRepository.class);

    public CameraRepository() {}

    public Boolean sendPingRtsp(String ipAddress, int port, String username, String password) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            Socket socket = null;
            BufferedWriter writer = null;
            BufferedReader reader = null;

            try {
                socket = new Socket(ipAddress, port);
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String auth = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
                String request = "OPTIONS rtsp://" + ipAddress + "/ RTSP/1.0\r\n" +
                        "CSeq: 1\r\n" +
                        "Authorization: Basic " + auth + "\r\n" +
                        "\r\n";

                writer.write(request);
                writer.flush();

                String firstLine = reader.readLine();

                if (firstLine != null && firstLine.contains("200")) {
                    return true;
                }
            } catch (Exception e) {
                logger.error("{}\t{}", e.getMessage(), ipAddress);
            } finally {
                try {
                    if (reader != null) reader.close();
                    if (writer != null) writer.close();
                    if (socket != null) socket.close();
                } catch (IOException e) {
                    logger.error("{}\t{}", e.getMessage(), ipAddress);
                }
            }

            if (attempt < 3) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        logger.info(PING_RTSP_DONT_REACH, ipAddress);
        return false;
    }

    public Boolean sendPingRequest(String ipAddress) throws IOException {
        InetAddress geek = InetAddress.getByName(ipAddress);

        if (geek.isReachable(5000)){
            return true;
        } else{
            logger.info(PING_DONT_REACH, ipAddress);
            return false;
        }
    }
}
