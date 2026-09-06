package service;

import org.ipcamera.config.entity.Camera;
import org.ipcamera.config.entity.ConfigProcessVariable;
import org.ipcamera.config.service.StyleCameraInCollection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StyleCameraInCollectionTest {
    private ConfigProcessVariable config;

    @BeforeEach
    void setUp() {
        config = mock(ConfigProcessVariable.class);
        when(config.getDelta()).thenReturn(60);
    }

    @Test
    void styleByColumn_shouldReturnBanStyleForColumn1() {
        Camera camera = mock(Camera.class);

        when(camera.getBanOnAutoUpdate()).thenReturn(true);
        when(camera.getManualMode()).thenReturn(false);

        assertEquals(
                "-fx-background-color: #ffcfde; -fx-text-fill: #098a00;",
                StyleCameraInCollection.styleByColumn(
                        camera,
                        config,
                        1
                )
        );
    }

    @Test
    void styleByColumn_shouldReturnManualStyleForColumn1() {
        Camera camera = mock(Camera.class);

        when(camera.getBanOnAutoUpdate()).thenReturn(false);
        when(camera.getManualMode()).thenReturn(true);

        assertEquals(
                "-fx-background-color: #ded0f5; -fx-text-fill: #098a00;",
                StyleCameraInCollection.styleByColumn(
                        camera,
                        config,
                        1
                )
        );
    }

    @Test
    void styleByColumn_shouldReturnNormalStyleForColumn1() {
        Camera camera = mock(Camera.class);

        when(camera.getBanOnAutoUpdate()).thenReturn(false);
        when(camera.getManualMode()).thenReturn(false);

        assertEquals(
                "-fx-text-fill: #098a00;",
                StyleCameraInCollection.styleByColumn(
                        camera,
                        config,
                        1
                )
        );
    }

    @Test
    void styleByColumn_shouldReturnBanErrorStyleForColumn2_whenRequestFails() {
        Camera camera = mock(Camera.class);

        when(camera.getBanOnAutoUpdate()).thenReturn(true);
        when(camera.getStatusGetRequest()).thenReturn(false);

        assertEquals(
                "-fx-background-color: #ffcfde; -fx-text-fill: #fc6603",
                StyleCameraInCollection.styleByColumn(
                        camera,
                        config,
                        2
                )
        );
    }

    @Test
    void styleByColumn_shouldReturnBanTimeStyleForColumn2_whenTimeDifferenceTooLarge() {
        Camera camera = mock(Camera.class);

        when(camera.getBanOnAutoUpdate()).thenReturn(true);
        when(camera.getStatusGetRequest()).thenReturn(true);
        when(camera.getDifferenceTime()).thenReturn(100L);

        assertEquals(
                "-fx-background-color: #ffcfde; -fx-text-fill: #ff0000;",
                StyleCameraInCollection.styleByColumn(
                        camera,
                        config,
                        2
                )
        );
    }

    @Test
    void styleByColumn_shouldReturnManualErrorStyleForColumn2_whenRequestFails() {
        Camera camera = mock(Camera.class);

        when(camera.getBanOnAutoUpdate()).thenReturn(false);
        when(camera.getManualMode()).thenReturn(true);
        when(camera.getStatusGetRequest()).thenReturn(false);

        assertEquals(
                "-fx-background-color: #ded0f5; -fx-text-fill: #fc6603",
                StyleCameraInCollection.styleByColumn(
                        camera,
                        config,
                        2
                )
        );
    }

    @Test
    void styleByColumn_shouldReturnManualTimeStyleForColumn2_whenTimeDifferenceTooLarge() {
        Camera camera = mock(Camera.class);

        when(camera.getBanOnAutoUpdate()).thenReturn(false);
        when(camera.getManualMode()).thenReturn(true);
        when(camera.getStatusGetRequest()).thenReturn(true);
        when(camera.getDifferenceTime()).thenReturn(100L);

        assertEquals(
                "-fx-background-color: #ded0f5; -fx-text-fill: #ff0000;",
                StyleCameraInCollection.styleByColumn(
                        camera,
                        config,
                        2
                )
        );
    }

    @Test
    void styleByColumn_shouldReturnNormalRequestErrorStyleForColumn2() {
        Camera camera = mock(Camera.class);

        when(camera.getBanOnAutoUpdate()).thenReturn(false);
        when(camera.getManualMode()).thenReturn(false);
        when(camera.getStatusGetRequest()).thenReturn(false);

        assertEquals(
                "-fx-text-fill: #fc6603",
                StyleCameraInCollection.styleByColumn(
                        camera,
                        config,
                        2
                )
        );
    }

    @Test
    void styleByColumn_shouldReturnNormalTimeErrorStyleForColumn2() {
        Camera camera = mock(Camera.class);

        when(camera.getBanOnAutoUpdate()).thenReturn(false);
        when(camera.getManualMode()).thenReturn(false);
        when(camera.getStatusGetRequest()).thenReturn(true);
        when(camera.getDifferenceTime()).thenReturn(100L);

        assertEquals(
                "-fx-text-fill: #ff0000;",
                StyleCameraInCollection.styleByColumn(
                        camera,
                        config,
                        2
                )
        );
    }

    @Test
    void styleByColumn_shouldReturnEmptyStyleForColumn2_whenEverythingIsNormal() {
        Camera camera = mock(Camera.class);

        when(camera.getBanOnAutoUpdate()).thenReturn(false);
        when(camera.getManualMode()).thenReturn(false);
        when(camera.getStatusGetRequest()).thenReturn(true);
        when(camera.getDifferenceTime()).thenReturn(10L);

        assertEquals(
                "",
                StyleCameraInCollection.styleByColumn(
                        camera,
                        config,
                        2
                )
        );
    }

    @Test
    void styleByColumn_shouldReturnBanStyleForColumn3() {
        Camera camera = mock(Camera.class);

        when(camera.getBanOnAutoUpdate()).thenReturn(true);

        assertEquals(
                "-fx-background-color: #ffcfde; -fx-text-fill: black;",
                StyleCameraInCollection.styleByColumn(
                        camera,
                        config,
                        3
                )
        );
    }

    @Test
    void styleByColumn_shouldReturnManualStyleForColumn3() {
        Camera camera = mock(Camera.class);

        when(camera.getBanOnAutoUpdate()).thenReturn(false);
        when(camera.getManualMode()).thenReturn(true);

        assertEquals(
                "-fx-background-color: #ded0f5; -fx-text-fill: black;",
                StyleCameraInCollection.styleByColumn(
                        camera,
                        config,
                        3
                )
        );
    }

    @Test
    void styleByColumn_shouldReturnNormalStyleForColumn3() {
        Camera camera = mock(Camera.class);

        when(camera.getBanOnAutoUpdate()).thenReturn(false);
        when(camera.getManualMode()).thenReturn(false);

        assertEquals(
                "-fx-text-fill: black;",
                StyleCameraInCollection.styleByColumn(
                        camera,
                        config,
                        3
                )
        );
    }

    @Test
    void styleByColumn_shouldReturnEmptyString_forUnknownColumn() {
        Camera camera = mock(Camera.class);

        assertEquals(
                "",
                StyleCameraInCollection.styleByColumn(
                        camera,
                        config,
                        99
                )
        );
    }
}
