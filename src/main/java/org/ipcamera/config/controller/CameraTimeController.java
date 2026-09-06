package org.ipcamera.config.controller;

import lombok.Setter;
import org.ipcamera.config.entity.Camera;
import org.ipcamera.config.entity.ConfigProcessVariable;
import org.ipcamera.config.entity.Line;
import org.ipcamera.config.repository.CameraRepository;
import org.ipcamera.config.repository.cameras_request.AxisRepository;
import org.ipcamera.config.repository.cameras_request.IPCameraRepository;
import org.ipcamera.config.service.util.CameraUtilsService;
import org.ipcamera.config.service.time.NTPTimeService;
import org.ipcamera.config.service.time.ServerTimeService;
import org.ipcamera.config.service.SoftWareVersionService;
import serilogj.Log;

import java.util.Date;
import java.util.List;

import static org.ipcamera.config.constants.ConstantsLogger.NAME_PROGRAM_SEQ;
import static org.ipcamera.config.constants.ConstantsType.TYPE_AXIS;
import static org.ipcamera.config.constants.ConstantsType.TYPE_IP_CAMERA;

@Setter
public class CameraTimeController {
    private ConfigProcessVariable configProcessVariable;

    public CameraTimeController(ConfigProcessVariable configProcessVariable) {
        this.configProcessVariable = configProcessVariable;
    }

    private Boolean checkActive(Camera camera) throws Exception {
        CameraRepository cameraRepository = new CameraRepository();
        if(cameraRepository.sendPingRequest(camera.getIpAddress())){
            camera.setStatusPing(true);
            if(configProcessVariable.getCheckRTSP()){
                camera.setStatusRtsp(cameraRepository.sendPingRtsp(camera.getIpAddress(), camera.getPort(), camera.getUsername(), camera.getPassword()));
            }
            return true;
        }
        camera.setStatusPing(false);

        Log.information(NAME_PROGRAM_SEQ + "\t" + CameraUtilsService.getListLineAndNickname(camera) + "\tнедоступний.");

        return false;
    }

    public Boolean checkRelevanceTime(Camera camera, String ntpHost) throws Exception {
        if(checkActive(camera)) {
            Date dateCamera = null;
            if(camera.getType().equals(TYPE_AXIS)){
                AxisRepository axisRepository = new AxisRepository();

                String version;
                if(camera.getVersion() == null){
                    version = axisRepository.getVersion(camera);
                    camera.setVersion(version);
                } else{
                    version = camera.getVersion();
                }

                SoftWareVersionService versionService = new SoftWareVersionService();
                int versRequest = versionService.compareVersion(version);

                if(versRequest == 1){
                    dateCamera = axisRepository.getServerDateTime(camera, ntpHost);
                } else if(versRequest == 2){
                    dateCamera = axisRepository.postGetDateTime(camera);
                } else if(versRequest == 3){
                    dateCamera = axisRepository.getDateTimeForVersionBelow6(camera);
                }

            } else if(camera.getType().equals(TYPE_IP_CAMERA)){
                IPCameraRepository ipCameraRepository = new IPCameraRepository();
                if(camera.getVersion() == null){
                    camera.setVersion(ipCameraRepository.getVersion(camera));
                }
                dateCamera = ipCameraRepository.getServerTime(camera);
            } else{
                return false;
            }

            NTPTimeService time = new NTPTimeService();
            Date dateNtp = time.getNTPTime(ntpHost);
            camera.setDateCheck(dateNtp);

            ServerTimeService serverTimeService = new ServerTimeService();
            return serverTimeService.compareTime(dateCamera, dateNtp, configProcessVariable.getDelta(), camera);
        }
        return false;
    }

    public void changeTimeDate(Camera camera, String ntpHost, Boolean updateMode, List<Line> lines) throws Exception {
        if(camera.getType().equals(TYPE_AXIS)){
            AxisRepository axisRepository = new AxisRepository();

            SoftWareVersionService versionService = new SoftWareVersionService();
            int versRequest = versionService.compareVersion(camera.getVersion());
            if(versRequest == 1 || versRequest == 3){
                axisRepository.postTimeZoneOld(camera);
                axisRepository.getConfigurationDateTime(camera, ntpHost, updateMode);
            } else if(versRequest == 2){
                axisRepository.postTimeZoneNew(camera);
                axisRepository.postChangeDatetime(camera, ntpHost, updateMode);
            }

            LineUpdateController lineUpdateController = new LineUpdateController();
            lineUpdateController.updateLines(lines, camera);
        } else if(camera.getType().equals(TYPE_IP_CAMERA)){
            IPCameraRepository ipCameraRepository = new IPCameraRepository();
            ipCameraRepository.postServerTime(camera, ntpHost, updateMode);
        }
    }

    private void checkBeforeChangeTimeDate(Camera camera, String ntpHost, Boolean updateMode, List<Line> lines) throws Exception {
        if(checkRelevanceTime(camera, ntpHost)){
            changeTimeDate(camera, ntpHost, updateMode, lines);
        }
    }

    public void changeTimeDateAutoUpdate(Camera camera, String ntpHost, Boolean updateMode, List<Line> lines) throws Exception {
        if(!camera.getBanOnAutoUpdate()){
            if(configProcessVariable.getAutoUpdateIPCamera() && camera.getType().equals(TYPE_IP_CAMERA)){
                checkBeforeChangeTimeDate(camera, ntpHost, updateMode, lines);
            } else if (configProcessVariable.getAutoUpdateAxis() && camera.getType().equals(TYPE_AXIS)) {
                checkBeforeChangeTimeDate(camera, ntpHost, updateMode, lines);
            } else if(configProcessVariable.getAutoUpdateManual() && camera.getManualMode()){
                checkBeforeChangeTimeDate(camera, ntpHost, updateMode, lines);
            }
        }
    }
}
