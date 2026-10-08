package service;

import org.ipcamera.config.entity.Camera;
import org.ipcamera.config.service.InfoService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.ipcamera.config.constants.ConstantsType.TYPE_AXIS;
import static org.ipcamera.config.constants.ConstantsType.TYPE_IP_CAMERA;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class InfoServiceTest {
    @Test
    void getInfoByManual_shouldIncludeOnlyManualCameras() {
        Camera manualCamera = mock(Camera.class);
        when(manualCamera.getManualMode()).thenReturn(true);
        when(manualCamera.getType()).thenReturn(TYPE_AXIS);
        when(manualCamera.getNickname())
                .thenReturn(new String[]{"Front"});
        when(manualCamera.getIpAddress())
                .thenReturn("192.168.1.10");

        Camera nonManualCamera = mock(Camera.class);
        when(nonManualCamera.getManualMode()).thenReturn(false);

        String result =
                InfoService.getInfoByManual(
                        List.of(manualCamera, nonManualCamera)
                );

        assertTrue(result.contains("Front"));
        assertTrue(result.contains("192.168.1.10"));
    }

    @Test
    void getInfoByManual_shouldPutAxisCameraIntoAxisGroup() {
        Camera camera = mock(Camera.class);

        when(camera.getManualMode()).thenReturn(true);
        when(camera.getType())
                .thenReturn(TYPE_AXIS);
        when(camera.getNickname())
                .thenReturn(new String[]{"Axis camera"});
        when(camera.getIpAddress())
                .thenReturn("192.168.1.10");

        String result =
                InfoService.getInfoByManual(
                        List.of(camera)
                );

        int axisIndex =
                result.indexOf("Група \"Axis\":");

        int ipIndex =
                result.indexOf("Група \"IP-камера\":");

        int cameraIndex =
                result.indexOf("Axis camera");

        assertTrue(axisIndex >= 0);
        assertTrue(ipIndex >= 0);
        assertTrue(cameraIndex > axisIndex);
        assertTrue(cameraIndex < ipIndex);
    }

    @Test
    void getInfoByManual_shouldPutNonAxisCameraIntoIpCameraGroup() {
        Camera camera = mock(Camera.class);

        when(camera.getManualMode()).thenReturn(true);
        when(camera.getType())
                .thenReturn(TYPE_IP_CAMERA);
        when(camera.getNickname())
                .thenReturn(new String[]{"IP camera"});
        when(camera.getIpAddress())
                .thenReturn("192.168.1.20");

        String result =
                InfoService.getInfoByManual(
                        List.of(camera)
                );

        assertTrue(result.contains("IP camera"));
        assertTrue(result.contains("192.168.1.20"));
    }

    @Test
    void getInfoByManual_shouldIncludeAllNicknames() {
        Camera camera = mock(Camera.class);

        when(camera.getManualMode()).thenReturn(true);
        when(camera.getType())
                .thenReturn(TYPE_AXIS);
        when(camera.getNickname())
                .thenReturn(new String[]{"Front", "Back"});
        when(camera.getIpAddress())
                .thenReturn("192.168.1.10");

        String result =
                InfoService.getInfoByManual(
                        List.of(camera)
                );

        assertTrue(result.contains("- Front, 192.168.1.10"));
        assertTrue(result.contains("- Back, 192.168.1.10"));
    }

    @Test
    void getInfoByAutoBan_shouldIncludeOnlyBannedCameras() {
        Camera bannedCamera = mock(Camera.class);

        when(bannedCamera.getBanOnAutoUpdate())
                .thenReturn(true);
        when(bannedCamera.getType())
                .thenReturn(TYPE_AXIS);
        when(bannedCamera.getNickname())
                .thenReturn(new String[]{"Banned camera"});
        when(bannedCamera.getIpAddress())
                .thenReturn("192.168.1.30");

        Camera normalCamera = mock(Camera.class);

        when(normalCamera.getBanOnAutoUpdate())
                .thenReturn(false);
        when(normalCamera.getNickname())
                .thenReturn(new String[]{"Normal camera"});

        String result =
                InfoService.getInfoByAutoBan(
                        List.of(bannedCamera, normalCamera)
                );

        assertTrue(result.contains("Banned camera"));
        assertFalse(result.contains("Normal camera"));
    }

    @Test
    void getInfoByManual_shouldReturnHeaders_whenNoCamerasMatch() {
        Camera camera = mock(Camera.class);

        when(camera.getManualMode()).thenReturn(false);

        String result =
                InfoService.getInfoByManual(
                        List.of(camera)
                );

        assertTrue(result.contains("Група \"Axis\":"));
        assertTrue(result.contains("Група \"IP-камера\":"));
    }

    @Test
    void getInfoByAutoBan_shouldReturnHeaders_whenNoCamerasMatch() {
        Camera camera = mock(Camera.class);

        when(camera.getBanOnAutoUpdate()).thenReturn(false);

        String result =
                InfoService.getInfoByAutoBan(
                        List.of(camera)
                );

        assertTrue(result.contains("Група \"Axis\":"));
        assertTrue(result.contains("Група \"IP-камера\":"));
    }
}
