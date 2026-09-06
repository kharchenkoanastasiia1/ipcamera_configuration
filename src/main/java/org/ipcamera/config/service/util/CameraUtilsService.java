package org.ipcamera.config.service.util;

import org.ipcamera.config.entity.Camera;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CameraUtilsService {
    public static Camera getCameraByUrl(List<Camera> cameras, String url){
        if (cameras == null || url == null) {
            return null;
        }

        for (Camera camera : cameras) {
            if (camera.getUrl().equals(url)) {
                return camera;
            }
        }
        return null;
    }

    public static Camera getCameraByIp(List<Camera> cameras, String ipAddress) {
        if (cameras == null || ipAddress == null) {
            return null;
        }

        for (Camera camera : cameras) {
            if (ipAddress.equals(camera.getIpAddress())) {
                return camera;
            }
        }

        return null;
    }

    public static Camera getCameraByNickname(List<Camera> cameras, String nickname) {
        if (cameras == null || nickname == null) {
            return null;
        }

        for (Camera camera : cameras) {
            for (String nick : camera.getNickname()){
                if (nickname.equals(nick)) {
                    return camera;
                }
            }
        }

        return null;
    }

    public static List<String> getListNickname(List<Camera> cameras) {
        if (cameras == null) {
            return null;
        }

        List<String> nicknames = new ArrayList<>();

        for (Camera camera : cameras) {
            nicknames.addAll(Arrays.asList(camera.getNickname()));
        }

        return nicknames;
    }

    public static String getListLineAndNickname(Camera camera) {
        StringBuilder str = new StringBuilder();
        if(camera == null || camera.getLine() == null || camera.getNickname() == null) {
            return "";
        }
        for (int i = 0; i < camera.getLine().length; i++) {
            str.append(camera.getLine()[i]).append(" (").append(camera.getNickname()[i]).append(")");
            if(i != camera.getLine().length - 1){
                str.append(", ");
            }
        }
        return str.toString();
    }
}
