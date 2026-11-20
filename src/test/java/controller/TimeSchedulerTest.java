package controller;

import javafx.application.Platform;
import org.ipcamera.config.IPCamerasGUIController;
import org.ipcamera.config.controller.CameraTimeController;
import org.ipcamera.config.controller.DBController;
import org.ipcamera.config.controller.TimeScheduler;
import org.ipcamera.config.entity.Camera;
import org.ipcamera.config.entity.ConfigProcessVariable;
import org.ipcamera.config.entity.Line;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.ipcamera.config.constants.Constants.DEFAULT_PRELOADER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TimeSchedulerTest {
    enum Mode { CHECK, UPDATE, MANUAL }

    @ParameterizedTest
    @EnumSource(Mode.class)
    void schedulesCameraWorkPublishesResultsAndStops(Mode mode) throws Exception {
        boolean manual = mode == Mode.MANUAL;
        Camera camera = mock(Camera.class);
        when(camera.getManualMode()).thenReturn(true);
        when(camera.getNickname()).thenReturn(new String[]{"Camera A"});
        List<Camera> cameras = List.of(camera);
        List<Line> lines = List.of();
        ConfigProcessVariable config = mock(ConfigProcessVariable.class);
        when(config.getThreadCount()).thenReturn(2);
        when(config.getIntervalMinutes()).thenReturn(10L);
        when(config.getIntervalMinutesManual()).thenReturn(3L);
        when(config.getNtpHost()).thenReturn("127.0.0.1");
        IPCamerasGUIController gui = mock(IPCamerasGUIController.class);
        when(gui.getConfigProcessVariable()).thenReturn(config);
        TimeScheduler scheduler = new TimeScheduler(gui);
        ScheduledExecutorService timer = mock(ScheduledExecutorService.class);
        ExecutorService workers = mock(ExecutorService.class);
        long interval = manual ? 3 : 10;

        when(workers.submit(any(Runnable.class))).thenAnswer(call -> {
            Runnable task = call.getArgument(0);
            task.run();
            return null;
        });
        when(workers.awaitTermination(interval, MINUTES)).thenReturn(!manual);
        when(timer.awaitTermination(5, SECONDS)).thenReturn(true);

        try (var executors = mockStatic(Executors.class);
             var platform = mockStatic(Platform.class);
             var database = mockConstruction(DBController.class, (db, context) -> {
                 when(db.getCamerasList(config)).thenReturn(cameras);
                 when(db.getManual(cameras)).thenReturn(cameras);
                 when(db.getLines(config)).thenReturn(lines);
             });
             var cameraControllers = mockConstruction(CameraTimeController.class)) {
            executors.when(() -> Executors.newScheduledThreadPool(1)).thenReturn(timer);
            executors.when(() -> Executors.newFixedThreadPool(manual ? 5 : 2)).thenReturn(workers);
            platform.when(() -> Platform.runLater(any(Runnable.class))).thenAnswer(call -> {
                Runnable task = call.getArgument(0);
                task.run();
                return null;
            });

            switch (mode) {
                case CHECK -> {
                    assertFalse(scheduler.isRunningInfoTimeThread());
                    scheduler.restartInfoTimeThread();
                    assertTrue(scheduler.isRunningInfoTimeThread());
                }
                case UPDATE -> {
                    assertFalse(scheduler.isRunningChangeTimeThread());
                    scheduler.restartChangeTimeThread();
                    assertTrue(scheduler.isRunningChangeTimeThread());
                }
                case MANUAL -> {
                    assertFalse(scheduler.isRunningInfoTimeManualThread());
                    scheduler.restartInfoTimeManualThread(cameras);
                    assertTrue(scheduler.isRunningInfoTimeManualThread());
                }
            }

            ArgumentCaptor<Runnable> task = ArgumentCaptor.forClass(Runnable.class);
            verify(timer).scheduleWithFixedDelay(task.capture(), eq(manual ? 1L : 0L), eq(interval), eq(MINUTES));
            task.getValue().run();

            CameraTimeController cameraController = cameraControllers.constructed().getFirst();
            verify(gui).divideByStatus(cameras);
            verify(workers).shutdown();
            if (manual) {
                assertTrue(database.constructed().isEmpty());
                verify(workers).shutdownNow();
                verify(gui).setManualStatusThread(true);
            } else {
                verify(gui).loadPreloader(DEFAULT_PRELOADER);
                verify(gui).setNicknameList(List.of("Camera A"));
                verify(gui).currentLineData(lines);
            }
            if (mode == Mode.UPDATE) {
                verify(cameraController).changeTimeDateAutoUpdate(camera, "127.0.0.1", true, lines);
                verify(gui).updateStrCriticalErrors();
            } else {
                verify(cameraController).checkRelevanceTime(camera, "127.0.0.1");
            }

            switch (mode) {
                case CHECK -> {
                    assertFalse(scheduler.isStopInfoTimeThread());
                    scheduler.stopInfoTimeThread();
                }
                case UPDATE -> {
                    assertFalse(scheduler.isStopChangeTimeThread());
                    scheduler.stopChangeTimeThread();
                }
                case MANUAL -> {
                    assertFalse(scheduler.isStopInfoTimeManualThread());
                    scheduler.stopInfoTimeManualThread();
                }
            }
            verify(timer).shutdownNow();
        }
    }
}
