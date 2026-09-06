package org.ipcamera.config.service.update_collections;

import org.ipcamera.config.entity.Camera;
import org.ipcamera.config.entity.ConfigProcessVariable;
import org.ipcamera.config.service.util.CameraUtilsService;

import java.util.List;

import static org.ipcamera.config.constants.ConstantsType.TYPE_AXIS;
import static org.ipcamera.config.constants.ConstantsType.TYPE_IP_CAMERA;

public class CameraUpdateService {

    public static List<Camera> syncCameraLists(
            List<Camera> currentList,
            List<Camera> streamList) {

        if(currentList == null){
            return streamList;
        }

        for (Camera streamCam : streamList) {
            boolean updated = false;
            for (Camera currentCam : currentList) {
                if (sameCamera(currentCam, streamCam)) {
                    updateCamera(currentCam, streamCam);
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                currentList.add(streamCam);
            }
        }

        //удалить камеры, которых больше нет в коллекции из потока
        currentList.removeIf(currentCam ->
                streamList.stream().noneMatch(streamCam -> sameCamera(currentCam, streamCam))
        );

        return currentList;
    }

    public static List<Camera> syncCameraListsByAutoUpdate(
            List<Camera> currentList
            , List<Camera> streamList
            , ConfigProcessVariable configProcessVariable) {

        if(currentList == null){
            return streamList;
        }

        for (Camera streamCam : streamList ) {
            if(CameraUtilsService.getCameraByIp(currentList, streamCam.getIpAddress()) == null){
                currentList.add(streamCam);
            } else{
                if(cameraNeedUpdate(streamCam, configProcessVariable)){
                    for (Camera currentCam : currentList) {
                        if (sameCamera(currentCam, streamCam)) {
                            updateCamera(currentCam, streamCam);
                            break;
                        }
                    }
                }
            }
        }

        currentList.removeIf(currentCam ->
                streamList.stream().noneMatch(streamCam -> sameCamera(currentCam, streamCam))
        );

        return currentList;
    }

    private static boolean sameCamera(Camera cam1, Camera cam2) {
        if (cam1 == null || cam2 == null)
            return false;
        return cam1.getIpAddress().equals(cam2.getIpAddress());
    }

    private static void updateCamera(Camera currentCam, Camera streamCam) {
        currentCam.setUsername(streamCam.getUsername());
        currentCam.setPassword(streamCam.getPassword());
        currentCam.setUrl(streamCam.getUrl());
        currentCam.setIpAddress(streamCam.getIpAddress());
        currentCam.setPort(streamCam.getPort());
        currentCam.setType(streamCam.getType());
        currentCam.setNickname(streamCam.getNickname());
        currentCam.setRegname(streamCam.getRegname());
        currentCam.setTransmitUrl(streamCam.getTransmitUrl());
        currentCam.setTransmitUsr(streamCam.getTransmitUsr());
        currentCam.setTransmitPsw(streamCam.getTransmitPsw());
        currentCam.setManualMode(streamCam.getManualMode());
        currentCam.setBanOnAutoUpdate(streamCam.getBanOnAutoUpdate());

        currentCam.setStatusPing(streamCam.getStatusPing());
        currentCam.setStatusRtsp(streamCam.getStatusRtsp());
        currentCam.setDifferenceTime(streamCam.getDifferenceTime());
        currentCam.setVersion(streamCam.getVersion());
        currentCam.setStatusGetRequest(streamCam.getStatusGetRequest());

        if (streamCam.getDateCheck() != null) {
            currentCam.setDateCheck(streamCam.getDateCheck());
        }

        if (streamCam.getDateUpdateSuccess() != null) {
            currentCam.setDateUpdateSuccess(streamCam.getDateUpdateSuccess());
        }
    }

    private static boolean cameraNeedUpdate(Camera camera, ConfigProcessVariable configProcessVariable) {
        if(!camera.getBanOnAutoUpdate()){
            if(configProcessVariable.getAutoUpdateIPCamera() && camera.getType().equals(TYPE_IP_CAMERA)){
                return true;
            } else if (configProcessVariable.getAutoUpdateAxis() && camera.getType().equals(TYPE_AXIS)) {
                return true;
            } else if (configProcessVariable.getAutoUpdateManual() && camera.getManualMode()){
                return true;
            }
        }
        return false;
    }
}
