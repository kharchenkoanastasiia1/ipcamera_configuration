package service;

import org.ipcamera.config.entity.Camera;
import org.ipcamera.config.entity.ConfigProcessVariable;
import org.ipcamera.config.service.comparator.IPComparator;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class IPComparatorTest {
    @Test
    void compareIP_shouldCompareIpAddressesNumerically() {
        Comparator<String> comparator = IPComparator.compareIP();

        //вернет отрицательное число, если метод сравнивает числа:
        assertTrue(
                comparator.compare("192.168.1.2", "192.168.1.10") < 0
        );

        assertTrue(
                comparator.compare("192.168.1.10", "192.168.1.2") > 0
        );
    }

    @Test
    void compareIP_shouldReturnZero_forEqualIps() {
        Comparator<String> comparator = IPComparator.compareIP();

        assertEquals(
                0,
                comparator.compare(
                        "192.168.1.10",
                        "192.168.1.10"
                )
        );
    }

    @Test
    void compareIP_shouldCompareDifferentSubnetsCorrectly() {
        Comparator<String> comparator = IPComparator.compareIP();

        assertTrue(
                comparator.compare(
                        "192.168.1.100",
                        "192.168.2.1"
                ) < 0
        );

        assertTrue(
                comparator.compare(
                        "10.0.0.1",
                        "192.168.1.1"
                ) < 0
        );
    }

    @Test
    void compareIP_shouldSortIpAddressesCorrectly() {
        List<String> ips = new ArrayList<>(List.of(
                "192.168.1.10",
                "192.168.1.2",
                "10.0.0.1",
                "192.168.1.100",
                "192.168.1.1"
        ));

        ips.sort(IPComparator.compareIP());

        assertEquals(
                List.of(
                        "10.0.0.1",
                        "192.168.1.1",
                        "192.168.1.2",
                        "192.168.1.10",
                        "192.168.1.100"
                ),
                ips
        );
    }

    @Test
    void compareIPWithHighlight_shouldPutCameraWithLargeTimeDifferenceFirst() {
        ConfigProcessVariable config = mock(ConfigProcessVariable.class);

        when(config.getDelta()).thenReturn(60);

        Camera problematicCamera = mock(Camera.class);
        when(problematicCamera.getDifferenceTime()).thenReturn(100L);
        when(problematicCamera.getStatusGetRequest()).thenReturn(true);

        Camera normalCamera = mock(Camera.class);
        when(normalCamera.getDifferenceTime()).thenReturn(10L);
        when(normalCamera.getStatusGetRequest()).thenReturn(true);

        Map<String, Camera> cameraMap = new HashMap<>();
        cameraMap.put("192.168.1.20", problematicCamera);
        cameraMap.put("192.168.1.10", normalCamera);

        Comparator<String> comparator =
                IPComparator.compareIPWithHighlight(config, cameraMap);

        assertTrue(
                comparator.compare(
                        "192.168.1.20",
                        "192.168.1.10"
                ) < 0
        );
    }

    @Test
    void compareIPWithHighlight_shouldPutCameraWithFailedRequestBeforeNormalCamera() {
        ConfigProcessVariable config = mock(ConfigProcessVariable.class);

        when(config.getDelta()).thenReturn(60);

        Camera failedCamera = mock(Camera.class);
        when(failedCamera.getDifferenceTime()).thenReturn(10L);
        when(failedCamera.getStatusGetRequest()).thenReturn(false);

        Camera normalCamera = mock(Camera.class);
        when(normalCamera.getDifferenceTime()).thenReturn(10L);
        when(normalCamera.getStatusGetRequest()).thenReturn(true);

        Map<String, Camera> cameraMap = new HashMap<>();
        cameraMap.put("192.168.1.20", failedCamera);
        cameraMap.put("192.168.1.10", normalCamera);

        Comparator<String> comparator =
                IPComparator.compareIPWithHighlight(config, cameraMap);

        assertTrue(
                comparator.compare(
                        "192.168.1.20",
                        "192.168.1.10"
                ) < 0
        );
    }

    @Test
    void compareIPWithHighlight_shouldPutTimeDifferencePriorityBeforeFailedRequest() {
        ConfigProcessVariable config = mock(ConfigProcessVariable.class);

        when(config.getDelta()).thenReturn(60);

        Camera timeDifferenceCamera = mock(Camera.class);
        when(timeDifferenceCamera.getDifferenceTime()).thenReturn(100L);
        when(timeDifferenceCamera.getStatusGetRequest()).thenReturn(false);

        Camera failedRequestCamera = mock(Camera.class);
        when(failedRequestCamera.getDifferenceTime()).thenReturn(10L);
        when(failedRequestCamera.getStatusGetRequest()).thenReturn(false);

        Map<String, Camera> cameraMap = new HashMap<>();
        cameraMap.put("192.168.1.20", timeDifferenceCamera);
        cameraMap.put("192.168.1.10", failedRequestCamera);

        Comparator<String> comparator =
                IPComparator.compareIPWithHighlight(config, cameraMap);

        assertTrue(
                comparator.compare(
                        "192.168.1.20",
                        "192.168.1.10"
                ) < 0
        );
    }

    @Test
    void compareIPWithHighlight_shouldUseIpOrder_whenPrioritiesAreEqual() {
        ConfigProcessVariable config = mock(ConfigProcessVariable.class);

        when(config.getDelta()).thenReturn(60);

        Camera camera1 = mock(Camera.class);
        when(camera1.getDifferenceTime()).thenReturn(10L);
        when(camera1.getStatusGetRequest()).thenReturn(true);

        Camera camera2 = mock(Camera.class);
        when(camera2.getDifferenceTime()).thenReturn(10L);
        when(camera2.getStatusGetRequest()).thenReturn(true);

        Map<String, Camera> cameraMap = new HashMap<>();
        cameraMap.put("192.168.1.20", camera1);
        cameraMap.put("192.168.1.10", camera2);

        Comparator<String> comparator =
                IPComparator.compareIPWithHighlight(config, cameraMap);

        assertTrue(
                comparator.compare(
                        "192.168.1.10",
                        "192.168.1.20"
                ) < 0
        );
    }

    @Test
    void compareIPWithHighlight_shouldTreatMissingCameraAsNormalPriority() {
        ConfigProcessVariable config = mock(ConfigProcessVariable.class);

        when(config.getDelta()).thenReturn(60);

        Camera normalCamera = mock(Camera.class);
        when(normalCamera.getDifferenceTime()).thenReturn(10L);
        when(normalCamera.getStatusGetRequest()).thenReturn(true);

        Map<String, Camera> cameraMap = new HashMap<>();
        cameraMap.put("192.168.1.20", normalCamera);

        Comparator<String> comparator =
                IPComparator.compareIPWithHighlight(config, cameraMap);

        // 192.168.1.10 отсутствует в map -> priority 0
        // 192.168.1.20 тоже priority 0.
        // Поэтому используется обычное сравнение IP.
        assertTrue(
                comparator.compare(
                        "192.168.1.10",
                        "192.168.1.20"
                ) < 0
        );
    }

    @Test
    void compareIPWithHighlight_shouldSortByPriorityAndThenByIp() {
        ConfigProcessVariable config = mock(ConfigProcessVariable.class);

        when(config.getDelta()).thenReturn(60);

        Camera highPriority = mock(Camera.class);
        when(highPriority.getDifferenceTime()).thenReturn(100L);
        when(highPriority.getStatusGetRequest()).thenReturn(true);

        Camera mediumPriority = mock(Camera.class);
        when(mediumPriority.getDifferenceTime()).thenReturn(10L);
        when(mediumPriority.getStatusGetRequest()).thenReturn(false);

        Camera normal = mock(Camera.class);
        when(normal.getDifferenceTime()).thenReturn(10L);
        when(normal.getStatusGetRequest()).thenReturn(true);

        Map<String, Camera> cameraMap = new HashMap<>();

        cameraMap.put("192.168.1.30", highPriority);
        cameraMap.put("192.168.1.20", mediumPriority);
        cameraMap.put("192.168.1.10", normal);

        List<String> ips = new ArrayList<>(cameraMap.keySet());

        ips.sort(
                IPComparator.compareIPWithHighlight(
                        config,
                        cameraMap
                )
        );

        assertEquals(
                List.of(
                        "192.168.1.30", // priority 2
                        "192.168.1.20", // priority 1
                        "192.168.1.10"  // priority 0
                ),
                ips
        );
    }
}
