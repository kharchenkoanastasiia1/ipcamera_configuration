package org.ipcamera.config.service.time;

import org.ipcamera.config.entity.Camera;
import org.ipcamera.config.service.util.CameraUtilsService;
import serilogj.Log;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static org.ipcamera.config.constants.ConstantsLogger.NAME_PROGRAM_SEQ;

public class ServerTimeService {

    public ServerTimeService() {}

    public Boolean compareTime(Date dateCamera, Date dateServer, int delta, Camera camera) {
        if(dateCamera == null){
            camera.setDifferenceTime(-1);
            return true;
        }

        Instant cameraInstant = dateCamera.toInstant();
        Instant ntpInstant = dateServer.toInstant();

        long diffMinutes = Math.abs(Duration.between(cameraInstant, ntpInstant).toMinutes());
        camera.setDifferenceTime(diffMinutes);

        boolean result = diffMinutes >= delta;
        if(result){
            Log.information(NAME_PROGRAM_SEQ + "\t" + CameraUtilsService.getListLineAndNickname(camera)
                    + "\tрізниця у часі = " + diffMinutes + " більше допустимої = " + delta);
        }

        return result;
    }

//    public Boolean compareTimeZone(String timeZone){
//        if(timeZone == null){
//            return false;
//        }
//        return timeZone.equals(TIME_ZONE_AXIS);
//    }

//    public Boolean compareTime(Date dateCamera, Date dateServer, int delta) {
//        if(dateCamera == null){
//            return true;
//        }
//        Instant cameraInstant = dateCamera.toInstant();
//        Instant ntpInstant = dateServer.toInstant();
//
//        long diffMinutes = Math.abs(Duration.between(cameraInstant, ntpInstant).toMinutes());
//
//        return diffMinutes >= delta;
//    }
}
