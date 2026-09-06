package org.ipcamera.config.service.update_collections;

import org.ipcamera.config.entity.Camera;
import org.ipcamera.config.entity.Line;

import java.util.List;

public class LineUpdateService {

    public static List<Line> syncLineLists(
            List<Line> currentList,
            List<Line> streamList) {

        if(currentList == null){
            return streamList;
        }

        for (Line streamLin : streamList) {
            boolean updated = false;
            for (int i = 0; i < currentList.size(); i++) {
                Line currentlin = currentList.get(i);
                if (sameLine(currentlin, streamLin)) {
                    updateCurrentLine(currentlin, streamLin);
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                currentList.add(streamLin);
            }
        }

        currentList.removeIf(currentCam ->
                streamList.stream().noneMatch(streamCam -> sameLine(currentCam, streamCam))
        );

        return currentList;
    }

    private static boolean sameLine(Line lin1, Line lin2) {
        if (lin1 == null || lin2 == null) return false;
        return lin1.getRegname().equals(lin2.getRegname());
    }

    private static void updateCurrentLine(Line currentLin, Line streamLin) {
        currentLin.setRegname(streamLin.getRegname());
        currentLin.setIpAddress(streamLin.getIpAddress());
        currentLin.setPort(streamLin.getPort());
        currentLin.setLogin(streamLin.getLogin());
        currentLin.setPassword(streamLin.getPassword());
    }
}
