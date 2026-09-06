package org.ipcamera.config.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.ipcamera.config.constants.Constants.*;

public class ExtractFromURLService {
    public static String extractIP(String ip) {
        Pattern pattern = Pattern.compile(REGEX_IP);
        Matcher matcher = pattern.matcher(ip);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    public static int extractPort(String port) {
        Pattern pattern = Pattern.compile(REGEX_PORT);
        Matcher matcher = pattern.matcher(port);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return PORT_DEFAULT;
    }
}
