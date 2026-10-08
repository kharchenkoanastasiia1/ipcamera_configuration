package org.ipcamera.config.db;

import org.ipcamera.config.entity.Camera;
import org.ipcamera.config.entity.ConfigProcessVariable;
import org.ipcamera.config.entity.Line;
import org.ipcamera.config.factory.CameraFactory;
import org.ipcamera.config.service.util.CameraUtilsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.ipcamera.config.constants.Constants.PROPERTIES_FILE;
import static org.ipcamera.config.constants.ConstantsDBSQL.*;
import static org.ipcamera.config.constants.ConstantsLogger.*;

public class H2Connector {
    private static final Logger logger = LoggerFactory.getLogger(H2Connector.class);
    private static String url;
    private static String user;
    private static String password;

    static {
        loadProperties();
    }

    private static void loadProperties() {
        try (InputStream input = H2Connector.class.getClassLoader()
                .getResourceAsStream(PROPERTIES_FILE)) {

            if (input == null) {
                logger.error(PROPERTIES_FILE_NOT_FOUND, PROPERTIES_FILE);
                throw new IOException(PROPERTIES_FILE_NOT_FOUND_SEQ + PROPERTIES_FILE);
            }

            Properties props = new Properties();
            props.load(input);

            url = props.getProperty("h2.url");
            user = props.getProperty("h2.user");
            password = props.getProperty("h2.password");

        } catch (IOException e) {
            logger.error(ERROR_LOAD_DB_CONFIGURATION, e.getMessage());
        }
    }

    public static Connection getConnection() {
        try {
            Class.forName("org.h2.Driver");
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            logger.error(ERROR_LOAD_DB_CONFIGURATION, e.getMessage());
            throw new RuntimeException(String.format(ERROR_LOAD_DB_CONFIGURATION_SEQ + e.getMessage(), e));
        } catch (ClassNotFoundException e) {
            logger.error(ERROR_LOAD_DB_CONFIGURATION, e.getMessage());
            throw new RuntimeException(e);
        }
    }


    public static void createTableCameras() {
        try (Connection conn = getConnection()){
            Statement stmt = conn.createStatement();
            stmt.execute(DB_H2_SQL_CREATE_TABLE_CAMERAS);
        } catch (SQLException e) {
            logger.error(ERROR_CREATE_TABLE_H2, e.getMessage());
        }
    }

    public static void addTableCameras(List<Camera> cameras) {
        //чистим перед заполнением
        truncateCameras();

        try (Connection conn = H2Connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(DB_H2_MERGE_TABLE_CAMERAS)) {

            for (Camera cam : cameras) {
                mergeLineTableCamera(cam, ps);

                ps.addBatch();
            }

            ps.executeBatch();
        } catch (SQLException e) {
            logger.error(ERROR_ALTER_TABLE_H2, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static void mergeLineTableCamera(Camera camera, PreparedStatement ps) throws SQLException {
        String lineField = camera.getLine() != null ? String.join(",", camera.getLine()) : null;
        String nicknameField = camera.getNickname() != null ? String.join(",", camera.getNickname()) : null;
        String transmitUrl = camera.getTransmitUrl() != null ? String.join(",", camera.getTransmitUrl()) : null;
        String transmitUsr = camera.getTransmitUsr() != null ? String.join(",", camera.getTransmitUsr()) : null;
        String transmitPwd = camera.getTransmitPsw() != null ? String.join(",", camera.getTransmitPsw()) : null;
        String regnameField = camera.getRegname() != null ? String.join(",", camera.getRegname()) : null;

        ps.setString(1, camera.getUrl());
        ps.setString(2, camera.getIpAddress());
        ps.setInt(3, camera.getPort());
        ps.setString(4, camera.getUsername());
        ps.setString(5, camera.getPassword());
        ps.setString(6, camera.getType());
        ps.setString(7, lineField);
        ps.setString(8, nicknameField);
        ps.setString(9, transmitUrl);
        ps.setString(10, transmitUsr);
        ps.setString(11, transmitPwd);
        ps.setString(12, regnameField);
    }

    public static List<Camera> getCameras() {
        List<Camera> cameras = new CopyOnWriteArrayList<>();

        try (Connection conn = H2Connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(DB_H2_SQL_SELECT_CAMERAS);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String username = rs.getString("username");
                String password = rs.getString("password");
                String url = rs.getString("url");
                String ipAddress = rs.getString("ip_address");
                int port = rs.getInt("port");
                String type = rs.getString("type");
                String lineStr = rs.getString("line");
                String nicknameStr = rs.getString("nickname");
                String transmitUrl = rs.getString("transmiturl");
                String transmitUsr = rs.getString("transmitusr");
                String transmitPwd = rs.getString("transmitpwd");
                String regnameStr = rs.getString("regname");

                String[] lines = (lineStr != null && !lineStr.isEmpty() && !"null".equalsIgnoreCase(lineStr))
                        ? lineStr.split(",")
                        : new String[]{""};

                String[] nicknames = (nicknameStr != null && !nicknameStr.isEmpty() && !"null".equalsIgnoreCase(nicknameStr))
                        ? nicknameStr.split(",")
                        : new String[]{""};

                String[] transmitUrls = (transmitUrl != null && !transmitUrl.isEmpty() && !"null".equalsIgnoreCase(transmitUrl))
                        ? transmitUrl.split(",")
                        : new String[]{""};

                String[] transmitUsrs = (transmitUsr != null && !transmitUsr.isEmpty() && !"null".equalsIgnoreCase(transmitUsr))
                        ? transmitUsr.split(",")
                        : new String[]{""};

                String[] transmitPwds = (transmitPwd != null && !transmitPwd.isEmpty() && !"null".equalsIgnoreCase(transmitPwd))
                        ? transmitPwd.split(",")
                        : new String[]{""};

                String[] regnames = (regnameStr != null && !regnameStr.isEmpty() && !"null".equalsIgnoreCase(regnameStr))
                        ? regnameStr.split(",")
                        : new String[]{""};

                Camera camera = CameraFactory.createCameraByType(username, password, url, ipAddress, port, type);
                if (camera != null) {
                    camera.setLine(lines);
                    camera.setNickname(nicknames);
                    camera.setTransmitUrl(transmitUrls);
                    camera.setTransmitUsr(transmitUsrs);
                    camera.setTransmitPsw(transmitPwds);
                    camera.setRegname(regnames);
                    cameras.add(camera);
                }
            }
        } catch (SQLException e) {
            logger.error(ERROR_READ_TABLE_H2, e.getMessage());
            throw new RuntimeException(ERROR_READ_TABLE_H2_SEQ + e.getMessage(), e);
        }

        return cameras;
    }

    private static void truncateCameras(){
        try (Connection conn = H2Connector.getConnection();
             Statement statement = conn.createStatement()) {
            statement.execute(DB_H2_TRUNCATE_CAMERAS);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }



    public static void createTableConfigurations() {
        try (Connection conn = getConnection()){
            Statement stmt = conn.createStatement();
            stmt.execute(DB_H2_SQL_CREATE_TABLE_CONFIGURATIONS);
        } catch (SQLException e) {
            logger.error(ERROR_CREATE_TABLE_H2, e.getMessage());
        }
    }

    public static void addTableConfigurations(ConfigProcessVariable processVariable){
        try (Connection conn = H2Connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(DB_H2_MERGE_TABLE_CONFIGURATIONS)) {

            ps.setInt(1, 1);
            ps.setString(2, processVariable.getUrlDBFirebird());
            ps.setString(3, processVariable.getNtpHost());
            ps.setInt(4, processVariable.getDelta());
            ps.setInt(5, processVariable.getThreadCount());
            ps.setLong(6, processVariable.getIntervalMinutes());
            ps.setBoolean(7, processVariable.getCheckRTSP());
            ps.setBoolean(8, processVariable.getAutoUpdateIPCamera());
            ps.setBoolean(9, processVariable.getAutoUpdateAxis());
            ps.setBoolean(10, processVariable.getAutoUpdateManual());
            ps.setLong(11, processVariable.getIntervalMinutesManual());
            ps.setInt(12, processVariable.getDeltaManual());
            ps.setBoolean(13, processVariable.getCheckManual());

            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error(ERROR_ALTER_TABLE_H2, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static ConfigProcessVariable getConfigProcessVariable() {
        try (Connection conn = H2Connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(DB_H2_SQL_SELECT_CONFIGURATIONS);
             ResultSet rs = ps.executeQuery()) {

            if(!rs.next()) {
                return null;
            }

            String url_db_firebird = rs.getString("url_db_firebird");
            String ntp_host = rs.getString("ntp_host");
            int delta = rs.getInt("delta");
            int threadCount = rs.getInt("thread_count");
            int intervalMinutes = rs.getInt("interval_minutes");
            boolean checkRTSP = rs.getBoolean("check_rtsp");
            boolean autoUpdateIPCamera = rs.getBoolean("auto_update_ip_camera");
            boolean autoUpdateAxis = rs.getBoolean("auto_update_axis");
            boolean autoUpdateManual = rs.getBoolean("auto_update_manual");
            int intervalMinutesManual = rs.getInt("interval_minutes_manual");
            int deltaManual = rs.getInt("delta_manual");
            boolean checkManual = rs.getBoolean("check_manual");

            return new ConfigProcessVariable(threadCount, intervalMinutes, checkRTSP
                    , delta, url_db_firebird, ntp_host, autoUpdateIPCamera, autoUpdateAxis, autoUpdateManual
                    , intervalMinutesManual, deltaManual, checkManual);

        } catch (SQLException e) {
            logger.error(ERROR_READ_TABLE_H2, e.getMessage());
            throw new RuntimeException(ERROR_READ_TABLE_H2_SEQ + e.getMessage(), e);
        }
    }



    public static void createTableManual() {
        try (Connection conn = getConnection()){
            Statement stmt = conn.createStatement();
            stmt.execute(DB_H2_SQL_CREATE_TABLE_MANUAL);
        } catch (SQLException e) {
            logger.error(ERROR_CREATE_TABLE_H2, e.getMessage());
        }
    }

    public static void addTableManual(Camera camera){
        try (Connection conn = H2Connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(DB_H2_MERGE_TABLE_MANUAL)) {

            ps.setString(1, camera.getUrl());
            ps.setBoolean(2, camera.getManualMode());
            ps.setBoolean(3, camera.getBanOnAutoUpdate());

            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error(ERROR_ALTER_TABLE_H2, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static List<Camera> getManual(List<Camera> cameras) {
        try (Connection conn = H2Connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(DB_H2_SQL_SELECT_MANUAL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String url = rs.getString("url");
                boolean manualMode = rs.getBoolean("manual_mode");
                boolean banOnAutoUpdate = rs.getBoolean("ban_on_autoupdate");

                Camera camera = CameraUtilsService.getCameraByUrl(cameras, url);
                if(camera != null) {
                    camera.setManualMode(manualMode);
                    camera.setBanOnAutoUpdate(banOnAutoUpdate);
                }
            }
        } catch (SQLException e) {
            logger.error(ERROR_READ_TABLE_H2, e.getMessage());
            throw new RuntimeException(ERROR_READ_TABLE_H2_SEQ + e.getMessage(), e);
        }

        return cameras;
    }



    public static void createTableLines() {
        try (Connection conn = getConnection()){
            Statement stmt = conn.createStatement();
            stmt.execute(DB_H2_SQL_CREATE_TABLE_LINES);
        } catch (SQLException e) {
            logger.error(ERROR_CREATE_TABLE_H2, e.getMessage());
        }
    }

    public static void addTableLines(List<Line> lines) {
        //чистим перед заполнением
        truncateLines();

        try (Connection conn = H2Connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(DB_H2_MERGE_TABLE_LINES)) {

            for (Line line : lines) {
                ps.setString(1, line.getRegname());
                ps.setString(2, line.getIpAddress());
                ps.setInt(3, line.getPort());
                ps.setString(4, line.getLogin());
                ps.setString(5, line.getPassword());

                ps.addBatch();
            }

            ps.executeBatch();
        } catch (SQLException e) {
            logger.error(ERROR_ALTER_TABLE_H2, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static List<Line> getLines() {
        List<Line> lines = new CopyOnWriteArrayList<>();

        try (Connection conn = H2Connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(DB_H2_SQL_SELECT_LINES);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String regname = rs.getString("regname");
                String ip = rs.getString("ip_address");
                int port = rs.getInt("port");
                String login = rs.getString("login");
                String password = rs.getString("password");

                if(regname == null || ip == null || login == null || password == null)
                    continue;

                lines.add(new Line(regname, ip, port, login, password));
            }
        } catch (SQLException e) {
            logger.error(ERROR_READ_TABLE_H2, e.getMessage());
            throw new RuntimeException(ERROR_READ_TABLE_H2_SEQ + e.getMessage(), e);
        }

        return lines;
    }

    private static void truncateLines(){
        try (Connection conn = H2Connector.getConnection();
             Statement statement = conn.createStatement()) {
            statement.execute(DB_H2_TRUNCATE_LINES);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
