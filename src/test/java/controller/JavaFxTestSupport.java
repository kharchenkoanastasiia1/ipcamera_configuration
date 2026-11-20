package controller;

import javafx.application.Platform;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

final class JavaFxTestSupport {
    static {
        Platform.startup(() -> Platform.setImplicitExit(false));
    }

    private JavaFxTestSupport() {}

    static void runOnJavaFxThread(Callable<Void> action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action);
        Platform.runLater(task);
        task.get(20, TimeUnit.SECONDS);
    }
}
