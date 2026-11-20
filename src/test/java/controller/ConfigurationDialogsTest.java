package controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.ipcamera.config.ChangeAutoUpdateGuiController;
import org.ipcamera.config.ChangeConfigGuiController;
import org.ipcamera.config.IPCamerasGUIController;
import org.ipcamera.config.db.H2Connector;
import org.ipcamera.config.entity.ConfigProcessVariable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static controller.JavaFxTestSupport.runOnJavaFxThread;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfigurationDialogsTest {
    @ParameterizedTest
    @ValueSource(strings = {"", "-1", "invalid"})
    void settingsRejectInvalidFieldsBeforeSaving(String invalid) throws Exception {
        runOnJavaFxThread(() -> {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/change_config_gui.fxml"));
            loader.load();
            ChangeConfigGuiController controller = loader.getController();
            Stage stage = mock(Stage.class);
            ConfigProcessVariable config = configuration();
            controller.setConfigProcessVariable(config);
            controller.setDialogStage(stage);
            boolean originalRtsp = IPCamerasGUIController.statusRtsp;
            boolean originalReboot = IPCamerasGUIController.rebootThread;

            try (var h2 = mockStatic(H2Connector.class);
                 var warnings = mockConstruction(Alert.class)) {
                controller.initData();
                assertEquals("localhost", input(loader, "dbHost").getText());
                assertEquals("30", input(loader, "interval").getText());
                for (String name : List.of("delta", "deltaManual", "interval", "intervalManual")) {
                    input(loader, name).setText(invalid);
                }
                if (invalid.isEmpty()) {
                    for (String name : List.of("dbHost", "dbPort", "dbPathToFile", "ntpHost")) {
                        input(loader, name).clear();
                    }
                }

                controller.handleApplyChanges();

                Alert warning = warnings.constructed().getFirst();
                verify(warning).showAndWait();
                verify(stage, never()).close();
                h2.verifyNoInteractions();
                assertEquals(30, config.getIntervalMinutes());
            } finally {
                IPCamerasGUIController.setStatusRtsp(originalRtsp);
                IPCamerasGUIController.setRebootThread(originalReboot);
            }
            return null;
        });
    }

    @Test
    void settingsPersistEditedValuesAndRequestSchedulerRestart() throws Exception {
        runOnJavaFxThread(() -> {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/change_config_gui.fxml"));
            loader.load();
            ChangeConfigGuiController controller = loader.getController();
            Stage stage = mock(Stage.class);
            ConfigProcessVariable config = configuration();
            controller.setConfigProcessVariable(config);
            controller.setDialogStage(stage);
            boolean originalRtsp = IPCamerasGUIController.statusRtsp;
            boolean originalReboot = IPCamerasGUIController.rebootThread;

            try (var h2 = mockStatic(H2Connector.class)) {
                controller.initData();
                input(loader, "ntpHost").setText("192.0.2.15");
                input(loader, "delta").setText("0");
                input(loader, "interval").setText("45");
                input(loader, "intervalManual").setText("10");
                ((CheckBox) loader.getNamespace().get("checkManual")).setSelected(true);
                ((CheckBox) loader.getNamespace().get("checkRTSP")).setSelected(true);

                controller.handleApplyChanges();

                assertEquals("192.0.2.15", config.getNtpHost());
                assertEquals(0, config.getDelta());
                assertEquals(45, config.getIntervalMinutes());
                assertEquals(10, config.getIntervalMinutesManual());
                assertTrue(config.getCheckManual());
                assertTrue(config.getCheckRTSP());
                assertTrue(IPCamerasGUIController.statusRtsp);
                assertTrue(IPCamerasGUIController.rebootThread);
                h2.verify(() -> H2Connector.addTableConfigurations(config));
                verify(stage).close();
            } finally {
                IPCamerasGUIController.setStatusRtsp(originalRtsp);
                IPCamerasGUIController.setRebootThread(originalReboot);
            }
            return null;
        });
    }

    @Test
    void autoUpdateDialogLoadsAndSavesCameraTypePreferences() throws Exception {
        runOnJavaFxThread(() -> {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/change_auto_update_gui.fxml"));
            Parent root = loader.load();
            ChangeAutoUpdateGuiController controller = loader.getController();
            ConfigProcessVariable config = configuration();
            config.setAutoUpdateIPCamera(false);
            config.setAutoUpdateAxis(true);
            config.setAutoUpdateManual(false);
            Stage stage = mock(Stage.class);
            controller.setConfigProcessVariable(config);
            controller.setDialogStage(stage);
            controller.initData();
            CheckBox ip = (CheckBox) loader.getNamespace().get("checkIPCamera");
            CheckBox axis = (CheckBox) loader.getNamespace().get("checkAxis");
            CheckBox manual = (CheckBox) loader.getNamespace().get("checkManual");
            assertFalse(ip.isSelected());
            assertTrue(axis.isSelected());
            assertFalse(manual.isSelected());
            ip.setSelected(true);
            axis.setSelected(false);
            manual.setSelected(true);

            try (var h2 = mockStatic(H2Connector.class)) {
                Button apply = (Button) root.lookup(".button");
                apply.fire();
                assertTrue(config.getAutoUpdateIPCamera());
                assertFalse(config.getAutoUpdateAxis());
                assertTrue(config.getAutoUpdateManual());
                h2.verify(() -> H2Connector.addTableConfigurations(config));
                verify(stage).close();
            }
            return null;
        });
    }

    private ConfigProcessVariable configuration() {
        ConfigProcessVariable config = new ConfigProcessVariable();
        config.setUrlDBFirebird("jdbc:firebirdsql://localhost:3050/cameras.fdb");
        config.setNtpHost("127.0.0.1");
        config.setDelta(3);
        config.setDeltaManual(2);
        config.setIntervalMinutes(30);
        config.setIntervalMinutesManual(5);
        config.setCheckRTSP(false);
        config.setCheckManual(false);
        return config;
    }

    private TextField input(FXMLLoader loader, String name) {
        return (TextField) loader.getNamespace().get(name);
    }
}
