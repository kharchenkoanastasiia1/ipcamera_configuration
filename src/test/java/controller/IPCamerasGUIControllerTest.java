package controller;

import javafx.application.Platform;
import javafx.animation.Timeline;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import org.ipcamera.config.IPCamerasGUIController;
import org.ipcamera.config.controller.DBController;
import org.ipcamera.config.controller.TimeScheduler;
import org.ipcamera.config.entity.Camera;
import org.ipcamera.config.entity.ConfigProcessVariable;
import org.ipcamera.config.service.FileService;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static controller.JavaFxTestSupport.runOnJavaFxThread;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IPCamerasGUIControllerTest {
    private final IPCamerasGUIController controller = new IPCamerasGUIController();

    @Test
    void dashboardShowsCameraStatusAndSwitchesSchedulerModes() throws Exception {
        runOnJavaFxThread(() -> {
            ConfigProcessVariable config = mock(ConfigProcessVariable.class);
            when(config.getDelta()).thenReturn(5);
            try (var periods = mockConstruction(Timeline.class);
                 var schedulers = mockConstruction(TimeScheduler.class);
                 var databases = mockConstruction(DBController.class,
                         (db, context) -> when(db.getConfiguration()).thenReturn(config));
                 var files = mockStatic(FileService.class);
                 var platform = mockStatic(Platform.class)) {
                files.when(FileService::read).thenReturn(new StringBuilder());
                platform.when(() -> Platform.runLater(any(Runnable.class))).thenAnswer(call -> {
                    Runnable task = call.getArgument(0);
                    task.run();
                    return null;
                });
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ipcameras_gui.fxml"));
                loader.load();
                IPCamerasGUIController dashboard = loader.getController();
                var controls = loader.getNamespace();
                TimeScheduler scheduler = schedulers.constructed().getFirst();
                verify(scheduler).runInfoTimeThread();

                dashboard.setManualStatusThread(true);
                Camera online10 = camera("192.0.2.10", true, true, 5);
                Camera online2 = camera("192.0.2.2", true, true, 0);
                Camera outOfSync = camera("192.0.2.3", true, true, 6);
                Camera requestFailed = camera("192.0.2.4", true, false, 0);
                Camera offline = camera("192.0.2.5", false, false, -1);
                dashboard.divideByStatus(List.of(online10, online2, outOfSync, requestFailed, offline));

                assertEquals(List.of("192.0.2.2", "192.0.2.10"),
                        ((ListView<?>) controls.get("cameraListViewSuccess")).getItems());
                assertEquals(List.of("192.0.2.3", "192.0.2.4"),
                        ((ListView<?>) controls.get("cameraListViewNotSuccessGetRequest")).getItems());
                assertEquals(List.of("192.0.2.5"),
                        ((ListView<?>) controls.get("cameraListViewNotSuccessPing")).getItems());
                assertFalse(dashboard.manualStatusThread);

                Label ip = (Label) controls.get("ipLabel");
                Label ping = (Label) controls.get("pingLabel");
                CheckBox manual = (CheckBox) controls.get("manualCheckBox");
                dashboard.showDetails(offline.getIpAddress());
                assertEquals(offline.getIpAddress(), ip.getText());
                assertEquals("FAIL", ping.getText());
                assertFalse(manual.isDisabled());
                dashboard.showDetails("192.0.2.99");
                assertEquals("", ip.getText());
                assertTrue(manual.isDisabled());

                Button run = (Button) controls.get("runButton");
                Button stop = (Button) controls.get("stopButton");
                assertTrue(stop.isDisabled());
                run.fire();
                assertTrue(run.isDisabled());
                assertFalse(stop.isDisabled());
                stop.fire();
                assertFalse(run.isDisabled());
                assertTrue(stop.isDisabled());
                var order = inOrder(scheduler);
                order.verify(scheduler).stopInfoTimeThread();
                order.verify(scheduler).restartChangeTimeThread();
                order.verify(scheduler).stopChangeTimeThread();
                order.verify(scheduler).restartInfoTimeThread();
            }
            return null;
        });
    }

    @Test
    void publishesCriticalErrorsAndMovesCaretToEnd() throws Exception {
        runOnJavaFxThread(() -> {
            TextArea area = mock(TextArea.class);
            inject("criticalErrorsTextArea", area);
            String message = "Camera unreachable";
            when(area.getText()).thenReturn(message);

            try (var files = mockStatic(FileService.class);
                 var platform = mockStatic(Platform.class)) {
                files.when(FileService::read).thenReturn(new StringBuilder(message));
                platform.when(() -> Platform.runLater(any(Runnable.class))).thenAnswer(call -> {
                    Runnable task = call.getArgument(0);
                    task.run();
                    return null;
                });

                controller.updateStrCriticalErrors();
                var order = inOrder(area);
                order.verify(area).setText(message);
                order.verify(area).positionCaret(message.length());

                IOException failure = new IOException("log unavailable");
                files.when(FileService::read).thenThrow(failure);
                assertSame(failure, assertThrows(RuntimeException.class, controller::updateStrCriticalErrors).getCause());
            }
            return null;
        });
    }

    private void inject(String name, Object value) throws ReflectiveOperationException {
        field(name).set(controller, value);
    }

    private java.lang.reflect.Field field(String name) throws NoSuchFieldException {
        var field = IPCamerasGUIController.class.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    private Camera camera(String ip, boolean ping, boolean request, long difference) {
        Camera camera = new Camera("user", "password", "", ip, 554, "Axis");
        camera.setStatusPing(ping);
        camera.setStatusGetRequest(request);
        camera.setDifferenceTime(difference);
        camera.setLine(new String[]{"1"});
        camera.setNickname(new String[]{"Camera " + ip});
        camera.setRegname(new String[]{"Test"});
        return camera;
    }
}
