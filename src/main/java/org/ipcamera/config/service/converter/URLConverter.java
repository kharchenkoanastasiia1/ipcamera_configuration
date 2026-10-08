package org.ipcamera.config.service.converter;

import java.net.URI;
import java.net.URISyntaxException;

import static org.ipcamera.config.constants.ConstantsLogger.INCORRECT_FIREBIRD_URL_FORMAT;
import static org.ipcamera.config.constants.ConstantsLogger.INCORRECT_FIREBIRD_URL_FORMAT_MISSING;

public class URLConverter {

    public static String convertLabelToURLFirebird(String host, String port, String pathToFile){
        return "jdbc:firebirdsql://" + host + ":" + port + "/" + pathToFile;
    }

    public static String[] convertURLFirebirdToLabel(String url){
        String prefix = "jdbc:firebirdsql://";
        if (!url.startsWith(prefix)) {
            throw new IllegalArgumentException(INCORRECT_FIREBIRD_URL_FORMAT + url);
        }

        String withoutPrefix = url.substring(prefix.length());

        int slashIndex = withoutPrefix.indexOf('/');
        if (slashIndex == -1) {
            throw new IllegalArgumentException(INCORRECT_FIREBIRD_URL_FORMAT_MISSING);
        }

        String hostPortPart = withoutPrefix.substring(0, slashIndex);
        String pathToFile = withoutPrefix.substring(slashIndex + 1);

        String host;
        String port;
        int colonIndex = hostPortPart.indexOf(':');
        if (colonIndex != -1) {
            host = hostPortPart.substring(0, colonIndex);
            port = hostPortPart.substring(colonIndex + 1);
        } else {
            host = hostPortPart;
            port = "3050"; // значение по умолчанию для Firebird
        }

        return new String[]{host, port, pathToFile};
    }

    public static String convertUrlToHttp(String url, String username, String password) throws URISyntaxException {
        URI uri = new URI(url);
//        String newUrl = url.substring(url.lastIndexOf('/'));
        return "http://" + username + ":" + password + "@" + uri.getHost()
                + (uri.getPort() != -1 ? ":" + uri.getPort() : "")
                + uri.getPath();
    }
}
