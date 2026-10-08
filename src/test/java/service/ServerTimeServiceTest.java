package service;

import org.ipcamera.config.entity.Camera;
import org.ipcamera.config.service.time.ServerTimeService;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ServerTimeServiceTest {
    private final ServerTimeService service = new ServerTimeService();

    @Test
    void compareTime_shouldReturnTrueAndSetMinusOne_whenCameraDateIsNull() {
        Camera camera = mock(Camera.class);

        Date serverDate = new Date();

        boolean result = service.compareTime(
                null,
                serverDate,
                60,
                camera
        );

        assertTrue(result);

        verify(camera).setDifferenceTime(-1);
    }

    @Test
    void compareTime_shouldReturnFalse_whenDifferenceIsLessThanDelta() {
        Camera camera = mock(Camera.class);

        Date cameraDate = new Date(1_000_000L);
        Date serverDate = new Date(1_000_000L + 30 * 60 * 1000L);

        boolean result = service.compareTime(
                cameraDate,
                serverDate,
                60,
                camera
        );

        assertFalse(result);

        verify(camera).setDifferenceTime(30);
    }

    @Test
    void compareTime_shouldReturnTrue_whenDifferenceEqualsDelta() {
        Camera camera = mock(Camera.class);

        Date cameraDate = new Date(1_000_000L);
        Date serverDate = new Date(1_000_000L + 60 * 60 * 1000L);

        boolean result = service.compareTime(
                cameraDate,
                serverDate,
                60,
                camera
        );

        assertTrue(result);

        verify(camera).setDifferenceTime(60);
    }

    @Test
    void compareTime_shouldReturnTrue_whenDifferenceIsGreaterThanDelta() {
        Camera camera = mock(Camera.class);

        Date cameraDate = new Date(1_000_000L);
        Date serverDate = new Date(1_000_000L + 120 * 60 * 1000L);

        boolean result = service.compareTime(
                cameraDate,
                serverDate,
                60,
                camera
        );

        assertTrue(result);

        verify(camera).setDifferenceTime(120);
    }

    @Test
    void compareTime_shouldUseAbsoluteDifference() {
        Camera camera = mock(Camera.class);

        Date cameraDate = new Date(10_000_000L);
        Date serverDate = new Date(
                10_000_000L - 90 * 60 * 1000L
        );

        boolean result = service.compareTime(
                cameraDate,
                serverDate,
                60,
                camera
        );

        assertTrue(result);

        verify(camera).setDifferenceTime(90);
    }

    @Test
    void compareTime_shouldReturnFalse_whenDatesAreEqual() {
        Camera camera = mock(Camera.class);

        Date date = new Date();

        boolean result = service.compareTime(
                date,
                date,
                1,
                camera
        );

        assertFalse(result);

        verify(camera).setDifferenceTime(0);
    }
}
