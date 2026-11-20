package controller;

import org.ipcamera.config.controller.DBController;
import org.ipcamera.config.db.FirebirdConnector;
import org.ipcamera.config.db.H2Connector;
import org.ipcamera.config.entity.Camera;
import org.ipcamera.config.entity.ConfigProcessVariable;
import org.ipcamera.config.entity.Line;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DBControllerTest {
    private final DBController controller = new DBController();

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void refreshesLocalCacheOrUsesItWhenFirebirdIsUnavailable(boolean online) {
        ConfigProcessVariable config = mock(ConfigProcessVariable.class);
        List<Camera> cameras = List.of(mock(Camera.class));
        List<Line> lines = List.of(mock(Line.class));
        try (var firebird = mockStatic(FirebirdConnector.class);
             var h2 = mockStatic(H2Connector.class)) {
            firebird.when(() -> FirebirdConnector.getListCameras(config)).thenReturn(online ? cameras : null);
            firebird.when(() -> FirebirdConnector.getListLines(config)).thenReturn(online ? lines : null);
            h2.when(H2Connector::getCameras).thenReturn(cameras);
            h2.when(H2Connector::getLines).thenReturn(lines);

            assertSame(cameras, controller.getCamerasList(config));
            assertSame(lines, controller.getLines(config));
            if (online) {
                h2.verify(H2Connector::createTableCameras);
                h2.verify(() -> H2Connector.addTableCameras(cameras));
                h2.verify(H2Connector::createTableLines);
                h2.verify(() -> H2Connector.addTableLines(lines));
            } else {
                h2.verify(H2Connector::getCameras);
                h2.verify(H2Connector::getLines);
            }
            h2.verifyNoMoreInteractions();
        }
    }

    @Test
    void createsDefaultConfigurationOnlyWhenNoneIsStored() {
        try (var h2 = mockStatic(H2Connector.class)) {
            ConfigProcessVariable created = controller.getConfiguration();
            assertNotNull(created);
            h2.verify(() -> H2Connector.addTableConfigurations(created));

            h2.when(H2Connector::getConfigProcessVariable).thenReturn(created);
            assertSame(created, controller.getConfiguration());
            h2.verify(H2Connector::createTableConfigurations, times(2));
            h2.verify(() -> H2Connector.addTableConfigurations(created), times(1));
        }
    }

    @Test
    void loadsManualCamerasUsingCurrentCameraList() {
        List<Camera> cameras = List.of(mock(Camera.class));
        List<Camera> withManual = List.of(cameras.getFirst(), mock(Camera.class));
        try (var h2 = mockStatic(H2Connector.class)) {
            h2.when(() -> H2Connector.getManual(cameras)).thenReturn(withManual);
            assertSame(withManual, controller.getManual(cameras));
            h2.verify(H2Connector::createTableManual);
        }
    }
}
