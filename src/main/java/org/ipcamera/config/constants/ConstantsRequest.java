package org.ipcamera.config.constants;

public class ConstantsRequest {
    public static final String GET_TIME_IP_CAMERA = "/param.cgi?cmd=getservertime";
    public static final String GET_VERSION_IP_CAMERA = "/web/cgi-bin/hi3510/param.cgi?cmd=getserverinfo";
    public static final String POST_TIME_IP_CAMERA = "/web/cgi-bin/hi3500/param.cgi";
    public static final String POST_TIME_IP_CAMERA_BODY = "cmd=setservertime&-time=%s&-timezone=%s";

    public static final String GET_OLD_TIME_AXIS = "/axis-cgi/date.cgi?action=gettime&timestamp=";
    public static final String GET_OLD_DATE_AXIS = "/axis-cgi/date.cgi?action=getdate&timestamp=";
    public static final String POST_OLD_DATE_TIME_AXIS = "/axis-cgi/date.cgi?action=set&year=%s&month=%s&day=%s&hour=%s&minute=%s&second=%s&timestamp=%s";
    public static final String POST_OLD_TIMEZONE_AXIS = "/axis-cgi/param.cgi?action=update&root.Time.POSIXTimeZone=";

    public static final String GET_NEW_DATE_TIME_AXIS = "{\"apiVersion\":\"1.0\",\"method\":\"getAll\"}";
    public static final String POST_NEW_DATE_TIME_AXIS = "{\"apiVersion\":\"1.0\",\"method\":\"setDateTime\",\"params\":{\"dateTime\":\"%s\"}}";;
    public static final String POST_NEW_TIMEZONE_AXIS = "{\"apiVersion\":\"1.0\",\"method\":\"setTimeZone\",\"params\":{\"timeZone\":\"Europe/Kyiv\"}}";

    public static final String POST_CONFIGURATION_SYNCSOURCE_AXIS = "/axis-cgi/param.cgi?action=update&root.Time.SyncSource=None";
    public static final String GET_ALL_CONFIGURATION_PROPERTIES_AXIS = "/axis-cgi/param.cgi?action=list&group=root.Properties.Firmware";
//    public static final String GET_ALL_CONFIGURATION_AXIS = "/axis-cgi/param.cgi?action=list&group=root.Time";
//    public static final String GET_DATE_TIME_AXIS = "/axis-cgi/param.cgi?action=update&root.Time.ServerDate=%s&root.Time.ServerTime=%s&root.Time.POSIXTimeZone=%s";
//    public static final String GET_DATE_TIME_AXIS = "/axis-cgi/param.cgi?action=update&root.Time.POSIXTimeZone=%s&root.Time.ServerDate=%s&root.Time.ServerTime=%s";
//    public static final String GET_DATE_TIME_AXIS = "/axis-cgi/param.cgi?action=update&root.Time.ServerDate=%s&root.Time.ServerTime=%s";
//    public static final String GET_DATE_AXIS = "/axis-cgi/param.cgi?action=update&root.Time.ServerDate=";
//    public static final String GET_TIME_AXIS = "/axis-cgi/param.cgi?action=update&root.Time.ServerTime=";
}
