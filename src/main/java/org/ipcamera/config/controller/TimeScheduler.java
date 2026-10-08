package org.ipcamera.config.controller;

import javafx.application.Platform;
import org.ipcamera.config.IPCamerasGUIController;
import org.ipcamera.config.entity.Camera;
import org.ipcamera.config.entity.Line;
import org.ipcamera.config.service.util.CameraUtilsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.ipcamera.config.constants.Constants.DEFAULT_PRELOADER;
import static org.ipcamera.config.constants.ConstantsLogger.*;
import static org.ipcamera.config.constants.ConstantsType.TYPE_AXIS;

public class TimeScheduler {
    private static final Logger logger = LoggerFactory.getLogger(TimeScheduler.class);
    private ScheduledExecutorService schedulerChangeTime;
    private ScheduledExecutorService schedulerInfoTime;
    private ScheduledExecutorService schedulerInfoManual;
    private final AtomicBoolean isTaskInfoRunning = new AtomicBoolean(false);
    private final AtomicBoolean isTaskChangeRunning = new AtomicBoolean(false);
    private final AtomicBoolean isTaskInfoManualRunning = new AtomicBoolean(false);
    private final IPCamerasGUIController controller;


    public TimeScheduler(IPCamerasGUIController controller) {
        this.controller = controller;
    }

    //=======================TIME INFO=======================

    public void runInfoTimeThread(){
        timeInfoController();
    }

    public void stopInfoTimeThread() {
        stopTimeThread(schedulerInfoTime);
    }

    public void restartInfoTimeThread() {
        stopInfoTimeThread();
        runInfoTimeThread();
    }

    public Boolean isRunningInfoTimeThread() {
        if (schedulerInfoTime == null) {
            return false;
        }
        return !schedulerInfoTime.isShutdown();
    }

    public Boolean isStopInfoTimeThread() {
        return isTaskInfoRunning.get();
    }

    private void timeInfoController() {
        schedulerInfoTime = Executors.newScheduledThreadPool(1);

        Runnable hourlyTask = () -> {
            Platform.runLater(() -> controller.loadPreloader(DEFAULT_PRELOADER));

            logger.info(START_CHECK);

            isTaskInfoRunning.set(true);

            DBController dbController = new DBController();
            List<Camera> cameras = dbController.getCamerasList(controller.getConfigProcessVariable());
            cameras = dbController.getManual(cameras);
            List<Line> lines = dbController.getLines(controller.getConfigProcessVariable());

            controller.setNicknameList(CameraUtilsService.getListNickname(cameras));

            ExecutorService threadPool = Executors.newFixedThreadPool(controller.getConfigProcessVariable().getThreadCount());

            CameraTimeController timeController = new CameraTimeController(controller.getConfigProcessVariable());
            for (Camera camera : cameras) {
                threadPool.submit(() -> {
                    try {
                        timeController.checkRelevanceTime(camera, controller.getConfigProcessVariable().getNtpHost());
                    } catch (Exception e) {
                        logger.error("{} {}", e.getMessage(), camera.getIpAddress());
                        throw new RuntimeException(e);
                    }
                });
            }

            threadPool.shutdown();

            try {
                boolean finished = threadPool.awaitTermination(controller.getConfigProcessVariable().getIntervalMinutes(), TimeUnit.MINUTES);
                if (!finished) {
                    logger.warn(FORCED_STOP_THREADS, controller.getConfigProcessVariable().getIntervalMinutes());
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }

            logger.info(STOP_CHECK);
            controller.divideByStatus(cameras);
            controller.currentLineData(lines);

            isTaskInfoRunning.set(false);
        };

        schedulerInfoTime.scheduleWithFixedDelay(hourlyTask, 0, controller.getConfigProcessVariable().getIntervalMinutes(), TimeUnit.MINUTES);
    }

    //=======================CHANGE TIME=======================
    public void runChangeTimeThread(){
        timeChangeController();
    }

    public void stopChangeTimeThread() {
        stopTimeThread(schedulerChangeTime);
    }

    public void restartChangeTimeThread() {
        stopChangeTimeThread();
        runChangeTimeThread();
    }

    public Boolean isRunningChangeTimeThread() {
        if (schedulerChangeTime == null) {
            return false;
        }
        return !schedulerChangeTime.isShutdown();
    }

    public Boolean isStopChangeTimeThread() {
        return isTaskChangeRunning.get();
    }

    private void timeChangeController() {
        schedulerChangeTime = Executors.newScheduledThreadPool(1);

        Runnable hourlyTask = () -> {
            Platform.runLater(() -> controller.loadPreloader(DEFAULT_PRELOADER));

            logger.info(START_CHECK + START_AUTOUPDATE);

            isTaskChangeRunning.set(true);

            DBController dbController = new DBController();
            List<Camera> cameras = dbController.getCamerasList(controller.getConfigProcessVariable());
            cameras = dbController.getManual(cameras);
            List<Line> lines = dbController.getLines(controller.getConfigProcessVariable());

            controller.setNicknameList(CameraUtilsService.getListNickname(cameras));

            ExecutorService threadPool = Executors.newFixedThreadPool(controller.getConfigProcessVariable().getThreadCount());

            CameraTimeController timeController = new CameraTimeController(controller.getConfigProcessVariable());

            for (Camera camera : cameras) {
                threadPool.submit(() -> {
                    try {
                        timeController.changeTimeDateAutoUpdate(camera, controller.getConfigProcessVariable().getNtpHost(), true, lines);
                    } catch (Exception e) {
                        logger.error("{} {}", e.getMessage(), camera.getIpAddress());
                        throw new RuntimeException(e);
                    }
                });
            }

            threadPool.shutdown();

            try {
                boolean finished = threadPool.awaitTermination(controller.getConfigProcessVariable().getIntervalMinutes(), TimeUnit.MINUTES);
                if (!finished) {
                    logger.warn(FORCED_STOP_THREADS, controller.getConfigProcessVariable().getIntervalMinutes());
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }

            logger.info(STOP_CHECK + " (autoupdate)");
            controller.divideByStatus(cameras);
            controller.currentLineData(lines);
            controller.updateStrCriticalErrors();

            isTaskChangeRunning.set(false);
        };

        schedulerChangeTime.scheduleWithFixedDelay(hourlyTask, 0, controller.getConfigProcessVariable().getIntervalMinutes(), TimeUnit.MINUTES);
    }

    //=======================TIME INFO MANUAL=======================

    public void runInfoTimeManualThread(List<Camera> cameras){
        timeInfoManualController(cameras);
    }

    public void stopInfoTimeManualThread() {
        stopTimeThread(schedulerInfoManual);
    }

    public void restartInfoTimeManualThread(List<Camera> cameras) {
        if(isRunningInfoTimeManualThread()){
            stopInfoTimeManualThread();
        }
        runInfoTimeManualThread(cameras);
    }

    public Boolean isRunningInfoTimeManualThread() {
        if (schedulerInfoManual == null) {
            return false;
        }
        return !schedulerInfoManual.isShutdown();
    }

    public Boolean isStopInfoTimeManualThread() {
        return isTaskInfoManualRunning.get();
    }

    private void timeInfoManualController(List<Camera> cameras) {
        schedulerInfoManual = Executors.newScheduledThreadPool(1);

        Runnable hourlyTask = () -> {
            logger.info(START_CHECK + " (manual)");

            isTaskInfoManualRunning.set(true);

            ExecutorService threadPool = Executors.newFixedThreadPool(5);

            CameraTimeController timeController = new CameraTimeController(controller.getConfigProcessVariable());
            for (Camera camera : cameras) {
                threadPool.submit(() -> {
                    try {
                        if(camera.getManualMode()){
                            timeController.checkRelevanceTime(camera, controller.getConfigProcessVariable().getNtpHost());
                        }
                    } catch (Exception e) {
                        logger.error("{} {}", e.getMessage(), camera.getIpAddress());
                        throw new RuntimeException(e);
                    }
                });
            }

            threadPool.shutdown();

            try {
                boolean finished = threadPool.awaitTermination(controller.getConfigProcessVariable().getIntervalMinutesManual(), TimeUnit.MINUTES);
                if (!finished) {
                    logger.warn(FORCED_STOP_THREADS, controller.getConfigProcessVariable().getIntervalMinutesManual());
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }

            logger.info(STOP_CHECK + " (manual)");
            controller.setManualStatusThread(true);
            controller.divideByStatus(cameras);

            isTaskInfoManualRunning.set(false);
        };

        //задержка в минуту для первого запуска
        schedulerInfoManual.scheduleWithFixedDelay(hourlyTask, 1, controller.getConfigProcessVariable().getIntervalMinutesManual(), TimeUnit.MINUTES);
    }

    //===============================================================

    /**
     * Остановить запущенный процесс
     * */
    private void stopTimeThread(ScheduledExecutorService schedulerChangeTime) {
        if (schedulerChangeTime != null && !schedulerChangeTime.isShutdown()) {
            schedulerChangeTime.shutdownNow();
            try {
                if (!schedulerChangeTime.awaitTermination(5, TimeUnit.SECONDS)) {
                    schedulerChangeTime.shutdownNow();
                }
            } catch (InterruptedException e) {
                logger.error(INTERRUPT_WHILE_STOP, e);
                schedulerChangeTime.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
