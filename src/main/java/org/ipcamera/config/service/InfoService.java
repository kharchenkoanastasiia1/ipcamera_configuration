package org.ipcamera.config.service;

import org.ipcamera.config.entity.Camera;

import java.util.List;

import static org.ipcamera.config.constants.ConstantsType.TYPE_AXIS;

public class InfoService {

    public static String getInfoByManual(List<Camera> cameras){
        StringBuilder strAxis = new StringBuilder();
        strAxis.append("Група \"Axis\":\n");
        StringBuilder strIpCamera = new StringBuilder();
        strIpCamera.append("Група \"IP-камера\":\n");

        for (Camera camera : cameras) {
            if(camera.getManualMode()){
                sortByType(strAxis, strIpCamera, camera);
            }
        }

        strAxis.append("\n\n\n").append(strIpCamera);
        return strAxis.toString();
    }

    public static String getInfoByAutoBan(List<Camera> cameras){
        StringBuilder strAxis = new StringBuilder();
        strAxis.append("Група \"Axis\":\n");
        StringBuilder strIpCamera = new StringBuilder();
        strIpCamera.append("Група \"IP-камера\":\n");

        for (Camera camera : cameras) {
            if(camera.getBanOnAutoUpdate()){
                sortByType(strAxis, strIpCamera, camera);
            }
        }

        strAxis.append("\n\n\n").append(strIpCamera);
        return strAxis.toString();
    }

    private static void sortByType(StringBuilder strAxis, StringBuilder strIpCamera, Camera camera) {
        for (int i = 0; i < camera.getNickname().length; i++) {
            if(camera.getType().equals(TYPE_AXIS)){
                strAxis.append("- ").append(camera.getNickname()[i]).append(", ").append(camera.getIpAddress()).append("\n");
            } else{
                strIpCamera.append("- ").append(camera.getNickname()[i]).append(", ").append(camera.getIpAddress()).append("\n");
            }
        }
    }
}
