package org.ipcamera.config.service;

import org.ipcamera.config.entity.Camera;
import org.ipcamera.config.service.converter.TimeConverter;
import org.ipcamera.config.service.util.CameraUtilsService;

import java.util.Date;

import static org.ipcamera.config.constants.ConstantsLogger.NAME_PROGRAM_SEQ;

public class MessageForSeq {

    public static String getMessageUpdateMode(Camera camera, Date date, Boolean updateMode){
        StringBuilder message = new StringBuilder();
        message.append(NAME_PROGRAM_SEQ + "\t")
                .append(CameraUtilsService.getListLineAndNickname(camera))
                .append("\tбуло встановлено час ")
                .append(TimeConverter.formatDateForAxis(date))
                .append(" ")
                .append(TimeConverter.formatTimeForAxis(date));
        if(updateMode){
            message.append(" автоматично.");
        } else{
            message.append(" вручну.");
        }
        return message.toString();
    }
}
