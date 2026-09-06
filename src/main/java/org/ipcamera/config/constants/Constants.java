package org.ipcamera.config.constants;

public class Constants {
    public static final String GOOGLE_NTP_TIME = "216.239.35.12";//"time.google.com";

    public static final String PATTERN_DATE_FORMAT_IPCAMERA = "yyyy.MM.dd.HH.mm.ss";
    public static final String PATTERN_DATE_FORMAT_IPCAMERA_FROM_SERVER = "yyyyMMddHHmmss";
//    public static final String PATTERN_DATE_FORMAT_AXIS = "dd.MM.yyyy";
    public static final String PATTERN_DATE_FORMAT_AXIS = "yyyy-MM-dd";
    public static final String PATTERN_TIME_FORMAT_AXIS = "HH:mm:ss";
    public static final String PATTERN_DATE_FORMAT_FOR_FILE = "dd.MM.yyyy HH:mm:ss";
    public static final String TIME_ZONE = "Europe/Kyiv";
    public static final String TIME_ZONE_IP = "Europe/Helsinki";
    public static final String TIME_ZONE_AXIS = "EET-2EEST,M3.5.0/3,M10.5.0/4";   //Europe/Helsinki

    public static final String PROPERTIES_FILE = "db.properties";
    public static final String CRITICAL_ERROR_FILE = "critical_errors.txt";

    public static final String REGEX_IP = "\\d{1,3}(?:\\.\\d{1,3}){3}";
    public static final String REGEX_PORT = ":(\\d+)";
    public static final String REGEX_DATE_IPCAMERA = ".*var time=\"(\\d+)\";.*";
    public static final String REGEX_VERSION_IPCAMERA = "softVersion=\"([^\"]+)\"";
    public static final String REGEX_TIMEZONE_IPCAMERA = "var timeZone=\\\"([^\\\"]+)\\\"";

    public static final int PORT_DEFAULT = 554;

    public static final String DEFAULT_PRELOADER = "Не нервуйте \uD83D\uDE09 \nПросто ви ламаєте те, що працює, " +
            "\nколи вносите зміни під час внесення змін \n¯\\_(ツ)_/¯";
    public static final String THREAD_PRELOADER = "Упс! Прямо зараз відбувається \nоновлення данних. Почекайте \uD83D\uDE09";

    public static final String CONFIRM_ACTION = "Підтвердіть дію";
    public static final String ADD_OBJECT_MANUAL_UPDATE_MODE = "Додати/вилучити об'єкт до/з режиму ручного оновлення?";
    public static final String BAN_ON_AUTO_UPDATES = "Заборонити/дозволити автооновлення?";
}
