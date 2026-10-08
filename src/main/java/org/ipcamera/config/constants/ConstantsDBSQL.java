package org.ipcamera.config.constants;

public class ConstantsDBSQL {

    public static final String DB_FIREBIRD_SQL_GET_EXTENDED_LIST_CAMERAS =
            "SELECT L.REGID, L.LINE, L.NICKNAME, L.MEDIAURL, L.MEDIAUSR, L.MEDIAPWD, L.TRANSMITURL, L.TRANSMITUSR, L.TRANSMITPWD, R.REGNAME " +
                    "FROM LINES L " +
                    "JOIN RECORDERS R ON L.REGID = R.REGID " +
                    "WHERE L.TRANSMIT_PARAMS = 'FF_VIDEO'";



    public static final String DB_FIREBIRD_SQL_GET_EXTENDED_LIST_LINES =
            "SELECT REGNAME, IPADDR, IPPORT, CMD_USR, CMD_PWD " +
                    "FROM RECORDERS";



    public static final String DB_H2_SQL_CREATE_TABLE_CAMERAS = "CREATE TABLE IF NOT EXISTS ip_cameras (" +
            " url VARCHAR(255)," +
            " ip_address VARCHAR(50) PRIMARY KEY," +
            " port INT," +
            " username VARCHAR(100)," +
            " password VARCHAR(100)," +
            " type VARCHAR(50)," +
            " line VARCHAR(2000)," +
            " nickname VARCHAR(2000)," +
            " transmiturl VARCHAR(2000)," +
            " transmitusr VARCHAR(2000)," +
            " transmitpwd VARCHAR(2000)," +
            " regname VARCHAR(2000)" +
            ")";

    public static final String DB_H2_MERGE_TABLE_CAMERAS = "MERGE INTO ip_cameras (url, ip_address, port, username, password, type, line, nickname, transmiturl, transmitusr, transmitpwd, regname)\n" +
            "KEY(ip_address) \n" +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public static final String DB_H2_SQL_SELECT_CAMERAS = "SELECT * FROM ip_cameras";

    public static final String DB_H2_TRUNCATE_CAMERAS = "TRUNCATE TABLE ip_cameras";


//    public static final String DB_H2_SQL_CREATE_TABLE_MANUAL = "CREATE TABLE IF NOT EXISTS manual (" +
//            " url VARCHAR(255)," +
//            " manual_mode BOOLEAN" +
//            ")";
//
//    public static final String DB_H2_MERGE_TABLE_MANUAL = "MERGE INTO manual (url, manual_mode) \n" +
//            "KEY(url) \n" +
//            "VALUES (?, ?)";

    public static final String DB_H2_SQL_CREATE_TABLE_MANUAL = "CREATE TABLE IF NOT EXISTS manual (" +
            " url VARCHAR(255)," +
            " manual_mode BOOLEAN, " +
            " ban_on_autoupdate BOOLEAN" +
            ")";

    public static final String DB_H2_MERGE_TABLE_MANUAL = "MERGE INTO manual (url, manual_mode, ban_on_autoupdate) \n" +
            "KEY(url) \n" +
            "VALUES (?, ?, ?)";

    public static final String DB_H2_SQL_SELECT_MANUAL = "SELECT * FROM manual";



    public static final String DB_H2_SQL_CREATE_TABLE_CONFIGURATIONS = "CREATE TABLE IF NOT EXISTS configurations (" +
            " id INT PRIMARY KEY," +
            " url_db_firebird VARCHAR(2000)," +
            " ntp_host VARCHAR(2000)," +
            " delta INT," +
            " thread_count INT," +
            " interval_minutes BIGINT," +
            " check_rtsp BOOLEAN," +
            " auto_update_ip_camera BOOLEAN," +
            " auto_update_axis BOOLEAN," +
            " auto_update_manual BOOLEAN, " +
            " interval_minutes_manual BIGINT, " +
            " delta_manual INT," +
            " check_manual BOOLEAN" +
            ")";

    public static final String DB_H2_MERGE_TABLE_CONFIGURATIONS = "MERGE INTO configurations (id, url_db_firebird, ntp_host, delta, thread_count,\n" +
            "                           interval_minutes, check_rtsp, auto_update_ip_camera,\n" +
            "                           auto_update_axis, auto_update_manual, interval_minutes_manual, delta_manual, check_manual)\n" +
            "KEY(id) \n" +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public static final String DB_H2_SQL_SELECT_CONFIGURATIONS = "SELECT * FROM configurations WHERE id = 1";



    public static final String DB_H2_SQL_CREATE_TABLE_LINES = "CREATE TABLE IF NOT EXISTS lines (" +
            " regname VARCHAR(255) PRIMARY KEY," +
            " ip_address VARCHAR(50)," +
            " port INT," +
            " login VARCHAR(100)," +
            " password VARCHAR(100)" +
            ")";

    public static final String DB_H2_MERGE_TABLE_LINES =
            "MERGE INTO lines (regname, ip_address, port, login, password) \n" +
                    "KEY(regname) \n" +
                    "VALUES (?, ?, ?, ?, ?)";

    public static final String DB_H2_SQL_SELECT_LINES =
            "SELECT * FROM lines";

    public static final String DB_H2_TRUNCATE_LINES = "TRUNCATE TABLE lines";
}
