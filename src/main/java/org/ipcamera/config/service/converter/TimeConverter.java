package org.ipcamera.config.service.converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

import static org.ipcamera.config.constants.Constants.*;

public class TimeConverter {

    public static String formatDateTimeForIPCamera(Date date) {
        if(date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_DATE_FORMAT_IPCAMERA);
            sdf.setTimeZone(TimeZone.getTimeZone(TIME_ZONE));
            return sdf.format(date);
        }
        return "";
    }

    public static String formatTimeForAxis(Date date) {
        if(date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_TIME_FORMAT_AXIS);
            sdf.setTimeZone(TimeZone.getTimeZone(TIME_ZONE));
            return sdf.format(date);
        }
        return "";
    }

    public static String formatDateForAxis(Date date) {
        if(date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_DATE_FORMAT_AXIS);
            sdf.setTimeZone(TimeZone.getTimeZone(TIME_ZONE));
            return sdf.format(date);
        }
        return "";
    }

    public static Date convertStringToDateForAxis(String date, String time) throws ParseException {
        if(date != null && !date.isEmpty() && time != null && !time.isEmpty()) {
            try{
                SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_DATE_FORMAT_AXIS + " " + PATTERN_TIME_FORMAT_AXIS);
                return sdf.parse(date + " " + time);
            } catch(ParseException e){
                throw new ParseException(date, 0);
            }
        }
        return null;
    }

    public static String convertLocalDateTimeToString(LocalDateTime date) throws ParseException {
        if(date != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PATTERN_DATE_FORMAT_FOR_FILE);
            return formatter.format(date);
        }
        return "";
    }
}
