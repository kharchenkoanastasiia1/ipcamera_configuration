package org.ipcamera.config.db;

import lombok.Setter;
import org.ipcamera.config.entity.Camera;
import org.ipcamera.config.entity.ConfigProcessVariable;
import org.ipcamera.config.entity.Line;
import org.ipcamera.config.factory.CameraFactory;
import org.ipcamera.config.service.ExtractFromURLService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serilogj.Log;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.ipcamera.config.constants.Constants.PROPERTIES_FILE;
import static org.ipcamera.config.constants.ConstantsDBSQL.DB_FIREBIRD_SQL_GET_EXTENDED_LIST_CAMERAS;
import static org.ipcamera.config.constants.ConstantsDBSQL.DB_FIREBIRD_SQL_GET_EXTENDED_LIST_LINES;
import static org.ipcamera.config.constants.ConstantsLogger.*;

public class FirebirdConnector {
    private static final Logger logger = LoggerFactory.getLogger(FirebirdConnector.class);
    @Setter
    private static String url;
    private static String user;
    private static String password;

    private static void loadProperties() {
        try (InputStream input = FirebirdConnector.class.getClassLoader()
                .getResourceAsStream(PROPERTIES_FILE)) {

            if (input == null) {
                logger.error(PROPERTIES_FILE_NOT_FOUND, PROPERTIES_FILE);
                throw new IOException(PROPERTIES_FILE_NOT_FOUND_SEQ + PROPERTIES_FILE);
            }

            Properties props = new Properties();
            props.load(input);

            user = props.getProperty("db.user");
            password = props.getProperty("db.password");

        } catch (IOException e) {
            logger.error(ERROR_LOAD_DB_CONFIGURATION, e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        loadProperties();
        Class.forName("org.firebirdsql.jdbc.FBDriver");
        return DriverManager.getConnection(url, user, password);
    }

    public static List<Camera> getListCameras(ConfigProcessVariable configProcessVariable) {
        url = configProcessVariable.getUrlDBFirebird();

        Map<String, Camera> cameraMap = null;

        try (Connection conn = FirebirdConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(DB_FIREBIRD_SQL_GET_EXTENDED_LIST_CAMERAS)) {

            cameraMap = new HashMap<>();

            while (rs.next()) {
                String mediaUrl = rs.getString("MEDIAURL");
                if(mediaUrl.equalsIgnoreCase("deleted")){
                    continue;
                }

                String mediaUsr = rs.getString("MEDIAUSR");
                String mediaPwd = rs.getString("MEDIAPWD");
                String line = rs.getString("LINE");
                String nickname = rs.getString("NICKNAME");
                String transmitUrl = rs.getString("TRANSMITURL");
                String transmitUsr = rs.getString("TRANSMITUSR");
                String transmitPwd = rs.getString("TRANSMITPWD");
                String regname = rs.getString("REGNAME");

                if (mediaUrl == null || mediaUsr == null || mediaPwd == null) continue;

                String ip = ExtractFromURLService.extractIP(mediaUrl);
                int port = ExtractFromURLService.extractPort(mediaUrl);

                if(ip != null){
                    Camera camera = cameraMap.get(ip);
                    if (camera == null) {
                        camera = CameraFactory.createCamera(mediaUrl, mediaUsr, mediaPwd);
                        if(camera != null){
                            camera.setIpAddress(ip);
                            camera.setPort(port);
                            camera.setLine(line != null ? new String[]{line} : new String[]{""});  //new String[0]
                            camera.setNickname(nickname != null ? new String[]{nickname} : new String[]{""});
                            camera.setTransmitUrl(transmitUrl != null ? new String[]{transmitUrl} : new String[]{""});
                            camera.setTransmitUsr(transmitUsr != null ? new String[]{transmitUsr} : new String[]{""});
                            camera.setTransmitPsw(transmitPwd != null ? new String[]{transmitPwd} : new String[]{""});
                            camera.setRegname(regname != null ? new String[]{regname} : new String[]{""});

                            cameraMap.put(ip, camera);
                        }
                    } else {
                        camera.setLine(appendAll(camera.getLine(), line));
                        camera.setNickname(appendAll(camera.getNickname(), nickname));
                        camera.setTransmitUrl(appendAll(camera.getTransmitUrl(), transmitUrl));
                        camera.setTransmitUsr(appendAll(camera.getTransmitUsr(), transmitUsr));
                        camera.setTransmitPsw(appendAll(camera.getTransmitPsw(), transmitPwd));
                        camera.setRegname(appendAll(camera.getRegname(), regname));
                    }
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            logger.error(INCORRECT_FIREBIRD_READ, e.getMessage());
            return null;
        }

        return new CopyOnWriteArrayList<>(cameraMap.values());
    }

    private static String[] appendAll(String[] existing, String value) {
        if (value == null)
            return existing != null ? existing : new String[0];
        if (existing == null)
            existing = new String[0];

        String[] result = Arrays.copyOf(existing, existing.length + 1);
        result[existing.length] = value;
        return result;
    }

    public static List<Line> getListLines(ConfigProcessVariable configProcessVariable) {
        url = configProcessVariable.getUrlDBFirebird();

        List<Line> lines = null;

        try (Connection conn = FirebirdConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(DB_FIREBIRD_SQL_GET_EXTENDED_LIST_LINES)) {

            lines = new CopyOnWriteArrayList<>();

            while (rs.next()) {
                String regname = rs.getString("REGNAME");
                String ip = rs.getString("IPADDR");
                int port = rs.getInt("IPPORT");
                String login = rs.getString("CMD_USR");
                String password = rs.getString("CMD_PWD");

                if(regname == null || ip == null || login == null || password == null)
                    continue;

                lines.add(new Line(regname, ip, port, login, password));
            }
        } catch (SQLException | ClassNotFoundException e) {
            logger.error(INCORRECT_FIREBIRD_READ, e.getMessage());
            return null;
        }

        return lines;
    }
}
