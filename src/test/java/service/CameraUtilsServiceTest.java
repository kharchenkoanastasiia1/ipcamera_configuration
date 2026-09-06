package service;

import org.ipcamera.config.entity.Camera;
import org.ipcamera.config.service.util.CameraUtilsService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CameraUtilsServiceTest {

    @Test
    void getCameraByUrl_shouldReturnCamera() {
        Camera camera = mock(Camera.class);

        when(camera.getUrl())
                .thenReturn("http://192.168.1.10");

        Camera result =
                CameraUtilsService.getCameraByUrl(
                        List.of(camera),
                        "http://192.168.1.10"
                );

        assertSame(camera, result);
    }

    @Test
    void getCameraByUrl_shouldReturnNull_whenCameraNotFound() {
        Camera camera = mock(Camera.class);

        when(camera.getUrl())
                .thenReturn("http://192.168.1.10");

        Camera result =
                CameraUtilsService.getCameraByUrl(
                        List.of(camera),
                        "http://192.168.1.20"
                );

        assertNull(result);
    }

    @Test
    void getCameraByUrl_shouldReturnNull_whenListIsNull() {
        assertNull(
                CameraUtilsService.getCameraByUrl(
                        null,
                        "http://192.168.1.10"
                )
        );
    }

    @Test
    void getCameraByUrl_shouldReturnNull_whenUrlIsNull() {
        Camera camera = mock(Camera.class);

        assertNull(
                CameraUtilsService.getCameraByUrl(
                        List.of(camera),
                        null
                )
        );
    }

    @Test
    void getCameraByIp_shouldReturnCamera() {
        Camera camera = mock(Camera.class);

        when(camera.getIpAddress())
                .thenReturn("192.168.1.10");

        Camera result =
                CameraUtilsService.getCameraByIp(
                        List.of(camera),
                        "192.168.1.10"
                );

        assertSame(camera, result);
    }

    @Test
    void getCameraByIp_shouldReturnNull_whenCameraNotFound() {
        Camera camera = mock(Camera.class);

        when(camera.getIpAddress())
                .thenReturn("192.168.1.10");

        assertNull(
                CameraUtilsService.getCameraByIp(
                        List.of(camera),
                        "192.168.1.20"
                )
        );
    }

    @Test
    void getCameraByIp_shouldReturnNull_whenListIsNull() {
        assertNull(
                CameraUtilsService.getCameraByIp(
                        null,
                        "192.168.1.10"
                )
        );
    }

    @Test
    void getCameraByIp_shouldReturnNull_whenIpIsNull() {
        assertNull(
                CameraUtilsService.getCameraByIp(
                        List.of(mock(Camera.class)),
                        null
                )
        );
    }

    @Test
    void getCameraByNickname_shouldReturnCamera() {
        Camera camera = mock(Camera.class);

        when(camera.getNickname())
                .thenReturn(new String[]{"Front", "Main"});

        Camera result =
                CameraUtilsService.getCameraByNickname(
                        List.of(camera),
                        "Main"
                );

        assertSame(camera, result);
    }

    @Test
    void getCameraByNickname_shouldReturnNull_whenNicknameNotFound() {
        Camera camera = mock(Camera.class);

        when(camera.getNickname())
                .thenReturn(new String[]{"Front", "Main"});

        Camera result =
                CameraUtilsService.getCameraByNickname(
                        List.of(camera),
                        "Back"
                );

        assertNull(result);
    }

    @Test
    void getCameraByNickname_shouldReturnNull_whenListIsNull() {
        assertNull(
                CameraUtilsService.getCameraByNickname(
                        null,
                        "Main"
                )
        );
    }

    @Test
    void getCameraByNickname_shouldReturnNull_whenNicknameIsNull() {
        assertNull(
                CameraUtilsService.getCameraByNickname(
                        List.of(mock(Camera.class)),
                        null
                )
        );
    }

    @Test
    void getListNickname_shouldReturnAllNicknames() {
        Camera camera1 = mock(Camera.class);
        Camera camera2 = mock(Camera.class);

        when(camera1.getNickname())
                .thenReturn(new String[]{"Front", "Main"});

        when(camera2.getNickname())
                .thenReturn(new String[]{"Back"});

        List<String> result =
                CameraUtilsService.getListNickname(
                        List.of(camera1, camera2)
                );

        assertEquals(
                List.of("Front", "Main", "Back"),
                result
        );
    }

    @Test
    void getListNickname_shouldReturnEmptyList_whenInputListIsEmpty() {
        List<String> result =
                CameraUtilsService.getListNickname(List.of());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getListNickname_shouldReturnNull_whenListIsNull() {
        assertNull(
                CameraUtilsService.getListNickname(null)
        );
    }

    @Test
    void getListLineAndNickname_shouldBuildCorrectString() {
        Camera camera = mock(Camera.class);

        when(camera.getLine())
                .thenReturn(new String[]{"Line 1", "Line 2"});

        when(camera.getNickname())
                .thenReturn(new String[]{"Front", "Back"});

        String result =
                CameraUtilsService.getListLineAndNickname(camera);

        assertEquals(
                "Line 1 (Front), Line 2 (Back)",
                result
        );
    }

    @Test
    void getListLineAndNickname_shouldWorkWithOneLine() {
        Camera camera = mock(Camera.class);

        when(camera.getLine())
                .thenReturn(new String[]{"Line 1"});

        when(camera.getNickname())
                .thenReturn(new String[]{"Front"});

        assertEquals(
                "Line 1 (Front)",
                CameraUtilsService.getListLineAndNickname(camera)
        );
    }
}