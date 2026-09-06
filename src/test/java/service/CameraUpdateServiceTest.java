package service;

import org.ipcamera.config.entity.Camera;
import org.ipcamera.config.entity.ConfigProcessVariable;
import org.ipcamera.config.factory.CameraFactory;
import org.ipcamera.config.service.update_collections.CameraUpdateService;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.ipcamera.config.constants.ConstantsType.TYPE_AXIS;
import static org.ipcamera.config.constants.ConstantsType.TYPE_IP_CAMERA;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CameraUpdateServiceTest {

    @Test
    void syncCameraLists_shouldReturnStreamList_whenCurrentListIsNull() {
        List<Camera> streamList = new ArrayList<>();

        Camera camera = mock(Camera.class);
        streamList.add(camera);

        List<Camera> result =
                CameraUpdateService.syncCameraLists(
                        null,
                        streamList
                );

        assertSame(streamList, result);
    }

    @Test
    void syncCameraLists_shouldAddNewCamera() {
        Camera currentCamera = mock(Camera.class);
        when(currentCamera.getIpAddress()).thenReturn("192.168.1.10");

        Camera newCamera = mock(Camera.class);
        when(newCamera.getIpAddress()).thenReturn("192.168.1.20");

        List<Camera> currentList =
                new ArrayList<>(List.of(currentCamera));

        List<Camera> streamList =
                new ArrayList<>(List.of(newCamera));

        List<Camera> result =
                CameraUpdateService.syncCameraLists(
                        currentList,
                        streamList
                );

        assertEquals(1, result.size());
        assertSame(newCamera, result.get(0));
    }

    @Test
    void syncCameraLists_shouldRemoveCameraThatIsNotInStreamList() {
        Camera currentCamera = mock(Camera.class);
        when(currentCamera.getIpAddress()).thenReturn("192.168.1.10");

        Camera streamCamera = mock(Camera.class);
        when(streamCamera.getIpAddress()).thenReturn("192.168.1.20");

        List<Camera> currentList =
                new ArrayList<>(List.of(currentCamera));

        List<Camera> streamList =
                new ArrayList<>(List.of(streamCamera));

        List<Camera> result =
                CameraUpdateService.syncCameraLists(
                        currentList,
                        streamList
                );

        assertEquals(1, result.size());
        assertSame(streamCamera, result.get(0));
        assertFalse(result.contains(currentCamera));
    }

    @Test
    void syncCameraLists_shouldUpdateExistingCamera() {
        Camera currentCamera = mock(Camera.class);
        when(currentCamera.getIpAddress()).thenReturn("192.168.1.10");

        Camera streamCamera = mock(Camera.class);
        when(streamCamera.getIpAddress()).thenReturn("192.168.1.10");

        when(streamCamera.getUsername()).thenReturn("newUser");
        when(streamCamera.getPassword()).thenReturn("newPassword");
        when(streamCamera.getUrl()).thenReturn("newUrl");
        when(streamCamera.getPort()).thenReturn(554);
        when(streamCamera.getType()).thenReturn("IP");
        when(streamCamera.getNickname()).thenReturn(new String[]{"Camera 1"});
        when(streamCamera.getRegname()).thenReturn(new String[]{"REG1"});
        when(streamCamera.getTransmitUrl()).thenReturn(new String[]{"transmitUrl"});
        when(streamCamera.getTransmitUsr()).thenReturn(new String[]{"transmitUser"});
        when(streamCamera.getTransmitPsw()).thenReturn(new String[]{"transmitPassword"});
        when(streamCamera.getManualMode()).thenReturn(true);
        when(streamCamera.getBanOnAutoUpdate()).thenReturn(false);
        when(streamCamera.getStatusPing()).thenReturn(true);
        when(streamCamera.getStatusRtsp()).thenReturn(true);
        when(streamCamera.getDifferenceTime()).thenReturn(100L);
        when(streamCamera.getVersion()).thenReturn("10.0");
        when(streamCamera.getStatusGetRequest()).thenReturn(true);

        List<Camera> currentList =
                new ArrayList<>(List.of(currentCamera));

        List<Camera> streamList =
                new ArrayList<>(List.of(streamCamera));

        List<Camera> result =
                CameraUpdateService.syncCameraLists(
                        currentList,
                        streamList
                );

        assertEquals(1, result.size());
        assertSame(currentCamera, result.get(0));

        verify(currentCamera).setUsername("newUser");
        verify(currentCamera).setPassword("newPassword");
        verify(currentCamera).setUrl("newUrl");
        verify(currentCamera).setIpAddress("192.168.1.10");
        verify(currentCamera).setPort(554);
        verify(currentCamera).setType("IP");
        verify(currentCamera).setNickname(new String[]{"Camera 1"});
        when(streamCamera.getRegname()).thenReturn(new String[]{"REG1"});
        when(streamCamera.getTransmitUrl()).thenReturn(new String[]{"transmitUrl"});
        when(streamCamera.getTransmitUsr()).thenReturn(new String[]{"transmitUser"});
        when(streamCamera.getTransmitPsw()).thenReturn(new String[]{"transmitPassword"});
        verify(currentCamera).setManualMode(true);
        verify(currentCamera).setBanOnAutoUpdate(false);
        verify(currentCamera).setStatusPing(true);
        verify(currentCamera).setStatusRtsp(true);
        verify(currentCamera).setDifferenceTime(100L);
        verify(currentCamera).setVersion("10.0");
        verify(currentCamera).setStatusGetRequest(true);
    }

    @Test
    void syncCameraLists_shouldNotUpdateDifferentIp() {
        Camera currentCamera = mock(Camera.class);
        when(currentCamera.getIpAddress()).thenReturn("192.168.1.10");

        Camera streamCamera = mock(Camera.class);
        when(streamCamera.getIpAddress()).thenReturn("192.168.1.20");

        List<Camera> currentList =
                new ArrayList<>(List.of(currentCamera));

        List<Camera> streamList =
                new ArrayList<>(List.of(streamCamera));

        CameraUpdateService.syncCameraLists(
                currentList,
                streamList
        );

        verify(currentCamera, never()).setUsername(any());
    }

    @Test
    void syncCameraListsByAutoUpdate_shouldReturnStreamList_whenCurrentListIsNull() {
        List<Camera> streamList = new ArrayList<>();

        Camera camera = mock(Camera.class);
        streamList.add(camera);

        ConfigProcessVariable config =
                mock(ConfigProcessVariable.class);

        List<Camera> result =
                CameraUpdateService.syncCameraListsByAutoUpdate(
                        null,
                        streamList,
                        config
                );

        assertSame(streamList, result);
    }

    @Test
    void syncCameraListsByAutoUpdate_shouldAddNewCamera() {
        Camera newCamera = mock(Camera.class);
        when(newCamera.getIpAddress()).thenReturn("192.168.1.20");

        List<Camera> currentList = new ArrayList<>();
        List<Camera> streamList =
                new ArrayList<>(List.of(newCamera));

        ConfigProcessVariable config =
                mock(ConfigProcessVariable.class);

        List<Camera> result =
                CameraUpdateService.syncCameraListsByAutoUpdate(
                        currentList,
                        streamList,
                        config
                );

        assertEquals(1, result.size());
        assertSame(newCamera, result.get(0));
    }

    @Test
    void syncCameraListsByAutoUpdate_shouldUpdateIpCamera_whenAutoUpdateIpCameraEnabled() {
        Camera currentCamera = mock(Camera.class);
        when(currentCamera.getIpAddress()).thenReturn("192.168.1.10");

        Camera streamCamera = mock(Camera.class);
        when(streamCamera.getIpAddress()).thenReturn("192.168.1.10");
        when(streamCamera.getBanOnAutoUpdate()).thenReturn(false);
        when(streamCamera.getType()).thenReturn(TYPE_IP_CAMERA);

        when(streamCamera.getUsername()).thenReturn("updatedUser");

        ConfigProcessVariable config =
                mock(ConfigProcessVariable.class);

        when(config.getAutoUpdateIPCamera()).thenReturn(true);
        when(config.getAutoUpdateAxis()).thenReturn(false);
        when(config.getAutoUpdateManual()).thenReturn(false);

        List<Camera> currentList =
                new ArrayList<>(List.of(currentCamera));

        List<Camera> streamList =
                new ArrayList<>(List.of(streamCamera));

        CameraUpdateService.syncCameraListsByAutoUpdate(
                currentList,
                streamList,
                config
        );

        verify(currentCamera).setUsername("updatedUser");
    }

    @Test
    void syncCameraListsByAutoUpdate_shouldNotUpdateIpCamera_whenAutoUpdateDisabled() {
        Camera currentCamera = mock(Camera.class);
        when(currentCamera.getIpAddress()).thenReturn("192.168.1.10");

        Camera streamCamera = mock(Camera.class);
        when(streamCamera.getIpAddress()).thenReturn("192.168.1.10");
        when(streamCamera.getBanOnAutoUpdate()).thenReturn(false);
        when(streamCamera.getType()).thenReturn(TYPE_IP_CAMERA);

        ConfigProcessVariable config =
                mock(ConfigProcessVariable.class);

        when(config.getAutoUpdateIPCamera()).thenReturn(false);
        when(config.getAutoUpdateAxis()).thenReturn(false);
        when(config.getAutoUpdateManual()).thenReturn(false);

        List<Camera> currentList =
                new ArrayList<>(List.of(currentCamera));

        List<Camera> streamList =
                new ArrayList<>(List.of(streamCamera));

        CameraUpdateService.syncCameraListsByAutoUpdate(
                currentList,
                streamList,
                config
        );

        verify(currentCamera, never()).setUsername(any());
    }

    @Test
    void syncCameraListsByAutoUpdate_shouldUpdateAxis_whenAutoUpdateAxisEnabled() {
        Camera currentCamera = mock(Camera.class);
        when(currentCamera.getIpAddress()).thenReturn("192.168.1.10");

        Camera streamCamera = mock(Camera.class);
        when(streamCamera.getIpAddress()).thenReturn("192.168.1.10");
        when(streamCamera.getBanOnAutoUpdate()).thenReturn(false);
        when(streamCamera.getType()).thenReturn(TYPE_AXIS);
        when(streamCamera.getUsername()).thenReturn("axisUser");

        ConfigProcessVariable config =
                mock(ConfigProcessVariable.class);

        when(config.getAutoUpdateAxis()).thenReturn(true);
        when(config.getAutoUpdateIPCamera()).thenReturn(false);
        when(config.getAutoUpdateManual()).thenReturn(false);

        CameraUpdateService.syncCameraListsByAutoUpdate(
                new ArrayList<>(List.of(currentCamera)),
                new ArrayList<>(List.of(streamCamera)),
                config
        );

        verify(currentCamera).setUsername("axisUser");
    }

    @Test
    void syncCameraListsByAutoUpdate_shouldUpdateManualCamera_whenManualAutoUpdateEnabled() {
        Camera currentCamera = mock(Camera.class);
        when(currentCamera.getIpAddress()).thenReturn("192.168.1.10");

        Camera streamCamera = mock(Camera.class);
        when(streamCamera.getIpAddress()).thenReturn("192.168.1.10");
        when(streamCamera.getBanOnAutoUpdate()).thenReturn(false);
        when(streamCamera.getManualMode()).thenReturn(true);
        when(streamCamera.getType()).thenReturn("OTHER");
        when(streamCamera.getUsername()).thenReturn("manualUser");

        ConfigProcessVariable config =
                mock(ConfigProcessVariable.class);

        when(config.getAutoUpdateManual()).thenReturn(true);
        when(config.getAutoUpdateIPCamera()).thenReturn(false);
        when(config.getAutoUpdateAxis()).thenReturn(false);

        CameraUpdateService.syncCameraListsByAutoUpdate(
                new ArrayList<>(List.of(currentCamera)),
                new ArrayList<>(List.of(streamCamera)),
                config
        );

        verify(currentCamera).setUsername("manualUser");
    }

    @Test
    void syncCameraListsByAutoUpdate_shouldNotUpdate_whenBanOnAutoUpdateIsEnabled() {
        Camera currentCamera = mock(Camera.class);
        when(currentCamera.getIpAddress()).thenReturn("192.168.1.10");

        Camera streamCamera = mock(Camera.class);
        when(streamCamera.getIpAddress()).thenReturn("192.168.1.10");
        when(streamCamera.getBanOnAutoUpdate()).thenReturn(true);
        when(streamCamera.getType()).thenReturn(TYPE_IP_CAMERA);
        when(streamCamera.getUsername()).thenReturn("newUser");

        ConfigProcessVariable config =
                mock(ConfigProcessVariable.class);

        when(config.getAutoUpdateIPCamera()).thenReturn(true);

        CameraUpdateService.syncCameraListsByAutoUpdate(
                new ArrayList<>(List.of(currentCamera)),
                new ArrayList<>(List.of(streamCamera)),
                config
        );

        verify(currentCamera, never()).setUsername(any());
    }

    @Test
    void syncCameraListsByAutoUpdate_shouldRemoveCameraMissingFromStream() {
        Camera currentCamera = mock(Camera.class);
        when(currentCamera.getIpAddress()).thenReturn("192.168.1.10");

        Camera streamCamera = mock(Camera.class);
        when(streamCamera.getIpAddress()).thenReturn("192.168.1.20");

        ConfigProcessVariable config =
                mock(ConfigProcessVariable.class);

        List<Camera> currentList =
                new ArrayList<>(List.of(currentCamera));

        List<Camera> streamList =
                new ArrayList<>(List.of(streamCamera));

        CameraUpdateService.syncCameraListsByAutoUpdate(
                currentList,
                streamList,
                config
        );

        assertEquals(1, currentList.size());
        assertSame(streamCamera, currentList.get(0));
    }
}
