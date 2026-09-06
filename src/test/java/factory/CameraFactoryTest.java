package factory;

import org.ipcamera.config.entity.Axis;
import org.ipcamera.config.entity.Camera;
import org.ipcamera.config.entity.IPCamera;
import org.ipcamera.config.factory.CameraFactory;
import org.junit.jupiter.api.Test;

import static org.ipcamera.config.constants.ConstantsType.TYPE_AXIS;
import static org.ipcamera.config.constants.ConstantsType.TYPE_IP_CAMERA;
import static org.junit.jupiter.api.Assertions.*;

public class CameraFactoryTest {
    @Test
    void createCamera_shouldCreateAxisCamera_whenUrlContainsAxis() {
        String url = "http://192.168.1.100/axis-cgi/param.cgi";
        String username = "admin";
        String password = "password";

        Camera result = CameraFactory.createCamera(url, username, password);

        assertNotNull(result);
        assertInstanceOf(Axis.class, result);
    }

    @Test
    void createCamera_shouldCreateIPCamera_whenUrlContainsPort554AndPath1() {
        String url = "rtsp://192.168.1.100:554/1";

        Camera result = CameraFactory.createCamera(
                url,
                "admin",
                "password"
        );

        assertNotNull(result);
        assertInstanceOf(IPCamera.class, result);
    }

    @Test
    void createCamera_shouldCreateIPCamera_whenUrlContainsPort554AndPath2() {
        String url = "rtsp://192.168.1.100:554/2";

        Camera result = CameraFactory.createCamera(
                url,
                "admin",
                "password"
        );

        assertNotNull(result);
        assertInstanceOf(IPCamera.class, result);
    }

    @Test
    void createCamera_shouldReturnNull_whenUrlIsUnsupported() {
        String url = "http://192.168.1.100:8080/camera";

        Camera result = CameraFactory.createCamera(
                url,
                "admin",
                "password"
        );

        assertNull(result);
    }

    @Test
    void createCamera_shouldPreferAxis_whenUrlMatchesAxisAndIpCameraConditions() {
        // Проверяем порядок if/else:
        String url = "http://192.168.1.100:554/1/axis";

        Camera result = CameraFactory.createCamera(
                url,
                "admin",
                "password"
        );

        assertNotNull(result);
        assertInstanceOf(Axis.class, result);
    }

    @Test
    void createCameraByType_shouldCreateAxisCamera() {
        Camera result = CameraFactory.createCameraByType(
                "admin",
                "password",
                "http://camera/axis",
                "192.168.1.100",
                80,
                TYPE_AXIS
        );

        assertNotNull(result);
        assertInstanceOf(Axis.class, result);
    }

    @Test
    void createCameraByType_shouldCreateIPCamera() {
        Camera result = CameraFactory.createCameraByType(
                "admin",
                "password",
                "rtsp://192.168.1.100:554/1",
                "192.168.1.100",
                554,
                TYPE_IP_CAMERA
        );

        assertNotNull(result);
        assertInstanceOf(IPCamera.class, result);
    }

    @Test
    void createCameraByType_shouldReturnNull_whenTypeIsUnknown() {
        Camera result = CameraFactory.createCameraByType(
                "admin",
                "password",
                "http://camera",
                "192.168.1.100",
                80,
                "UNKNOWN"
        );

        assertNull(result);
    }
}
