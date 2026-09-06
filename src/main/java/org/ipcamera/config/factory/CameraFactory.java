package org.ipcamera.config.factory;

import org.ipcamera.config.entity.*;

import static org.ipcamera.config.constants.ConstantsType.*;

public class CameraFactory {
    public static Camera createCamera(String url, String username, String password) {
        if (url.contains("/axis")) {
            return new Axis(username, password, url);
        } else if (url.contains(":554/1") || url.contains(":554/2")) {
            return new IPCamera(username, password, url);
        } else {
            return null;
        }
    }

    public static Camera createCameraByType(String username, String password, String url, String ipAddress, int port, String type) {
        return switch (type) {
            case TYPE_AXIS -> new Axis(username, password, url, ipAddress, port, type);
            case TYPE_IP_CAMERA -> new IPCamera(username, password, url, ipAddress, port, type);
            default -> null;
        };
    }

//    public static Camera createCamera(String url, String username, String password) {
//        if (url.contains("/axis-media/media.amp?camera=")) {
//            return new AxisNew(username, password, url);
//        } else if (url.contains("/axis-media/media.amp")) {
//            return new AxisOld(username, password, url);
//        } else if (url.contains("/cam/")) {
//            return new Hikvision(username, password, url);
//        } else if (url.contains("/live/")) {
//            return new IPCamera1(username, password, url);
//        }  else if (url.contains("/streaming/")) {
//            return new IPCamera2(username, password, url);
//        } else if (url.contains("/stream")) {
//            return new IPCamera3(username, password, url);
//        } else if (url.contains(":554/")) {
//            return new IPCamera4(username, password, url);
//        } else {
//            return new Camera(url, username, password);
//            //throw new IllegalArgumentException("Unknown camera type for url: " + url);
//        }
//    }
}
