package org.ipcamera.config.repository.lines_request;

import org.ipcamera.config.entity.Line;
import org.ipcamera.config.repository.cameras_request.IPCameraRepository;
import org.ipcamera.config.service.FileService;
import org.ipcamera.config.service.converter.TimeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serilogj.Log;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.ipcamera.config.constants.ConstantsLogger.*;

public class LineRepository {
    private static final Logger logger = LoggerFactory.getLogger(LineRepository.class);

    public LineRepository() {}

    public void restart(String lineId, Line line){
        for (int i = 0; i < 3; i++) {
            String response = updateLine(lineId, line);
            if(response != null && response.equals("200")){
                Log.error(NAME_PROGRAM_SEQ + String.format(SUCCESS_UPDATE_LINE_SEQ, lineId));
                logger.info(SUCCESS_UPDATE_LINE, lineId);
                break;
            }
            Log.error(NAME_PROGRAM_SEQ + String.format(FAILED_UPDATE_LINE_SEQ, lineId, i + 1));
            logger.info(FAILED_UPDATE_LINE, lineId, " Спроба " + (i + 1));
            try{
                FileService.write(TimeConverter.convertLocalDateTimeToString(LocalDateTime.now()) + " "
                        + String.format(FAILED_UPDATE_LINE_SEQ, lineId, i + 1));
            }catch (IOException | ParseException e){
                throw new RuntimeException(e);
            }
        }
    }

    private String updateLine(String lineId, Line line) {
        if (line == null || lineId == null || lineId.isEmpty()) {
            return null;
        }
        try {
            Socket socket = new Socket(line.getIpAddress(), line.getPort());
            socket.setSoTimeout(30000);

            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.US_ASCII));

            // GETSID
            String request = "GETSID\r\n";
            byte[] requestBytes = request.getBytes(StandardCharsets.US_ASCII);
            out.write(requestBytes);
            out.flush();

            String sid = reader.readLine();

            lineId = lineId.replaceAll("\\D", "");

            // Формування команди
            request = "";
            request += "EDITOR:" + line.getLogin() + ":RESTARTLINE:" + Integer.parseInt(lineId) + "\r\n";
            request += getMD5(line.getLogin() + sid + line.getPassword()) + "\r\n";
            request += "\r\n";

            requestBytes = request.getBytes(StandardCharsets.US_ASCII);
            out.write(requestBytes);
            out.flush();

            List<String> response = new ArrayList<>();

            // код 200 успішно
            response.add(reader.readLine());
            response.add(reader.readLine());
            response.add(reader.readLine());

            reader.close();
            out.close();
            socket.close();

            return response.get(0);

        } catch (Exception e) {
            logger.info(FAILED_UPDATE_LINE, lineId, e.getMessage());
        }
        return null;
    }

    private String getMD5(String str) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] hash = md5.digest(str.getBytes(StandardCharsets.US_ASCII));

        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

    private void rebootLine(Line line){
        if (line == null) {
            return;
        }
        try {
            Socket socket = new Socket(line.getIpAddress(), line.getPort());
            socket.setSoTimeout(30000);

            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.US_ASCII));

            // GETSID
            String request = "GETSID\r\n";
            byte[] requestBytes = request.getBytes(StandardCharsets.US_ASCII);
            out.write(requestBytes);
            out.flush();

            String sid = reader.readLine();

            // Формування команди
            request = "";
            request += "EDITOR:" + line.getLogin() + ":UPDATE:" + "\r\n";
            request += getMD5(line.getLogin() + sid + line.getPassword()) + "\r\n";
            request += "\r\n";

            requestBytes = request.getBytes(StandardCharsets.US_ASCII);
            out.write(requestBytes);
            out.flush();

            List<String> response = new ArrayList<>();

            // код 200 успішно
            response.add(reader.readLine());
            response.add(reader.readLine());
            response.add(reader.readLine());

            System.out.println(response.get(0) + " Code result");
            System.out.println("Done");

            reader.close();
            out.close();
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
