package org.ipcamera.config.service;

import org.ipcamera.config.entity.Camera;
import org.ipcamera.config.entity.ConfigProcessVariable;

public class StyleCameraInCollection {

    public static String styleByColumn(Camera camera, ConfigProcessVariable configProcessVariable, int column) {
        if(column == 1){
            if(camera.getBanOnAutoUpdate()){
                return "-fx-background-color: #ffcfde; -fx-text-fill: #098a00;";
            } else if(camera.getManualMode()){
                return "-fx-background-color: #ded0f5; -fx-text-fill: #098a00;";
            } else{
                return "-fx-text-fill: #098a00;";
            }
        } else if(column == 2){
            if(camera.getBanOnAutoUpdate()){
                if (!camera.getStatusGetRequest()) {
                    return "-fx-background-color: #ffcfde; -fx-text-fill: #fc6603";
                } else if (camera.getDifferenceTime() > configProcessVariable.getDelta()) {
                    return "-fx-background-color: #ffcfde; -fx-text-fill: #ff0000;";
                }
            } else if(camera.getManualMode()){
                if (!camera.getStatusGetRequest()) {
                    return "-fx-background-color: #ded0f5; -fx-text-fill: #fc6603";
                } else if (camera.getDifferenceTime() > configProcessVariable.getDelta()) {
                    return "-fx-background-color: #ded0f5; -fx-text-fill: #ff0000;";
                }
            } else{
                if (!camera.getStatusGetRequest()) {
                    return "-fx-text-fill: #fc6603";
                } else if (camera.getDifferenceTime() > configProcessVariable.getDelta()) {
                    return "-fx-text-fill: #ff0000;";
                }
            }
        } else if(column == 3){
            if(camera.getBanOnAutoUpdate()){
                return "-fx-background-color: #ffcfde; -fx-text-fill: black;";
            } else if(camera.getManualMode()){
                return "-fx-background-color: #ded0f5; -fx-text-fill: black;";
            } else{
                return "-fx-text-fill: black;";
            }
        }
        return "";
    }
}
