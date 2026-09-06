package org.ipcamera.config;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.Getter;

import javafx.application.Application;
import org.ipcamera.config.controller.DBController;
import org.ipcamera.config.entity.Camera;
import org.ipcamera.config.entity.ConfigProcessVariable;
import org.ipcamera.config.entity.Line;
import org.ipcamera.config.repository.lines_request.LineRepository;
import serilogj.Log;
import serilogj.LoggerConfiguration;
import serilogj.events.LogEventLevel;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static serilogj.sinks.seq.SeqSinkConfigurator.seq;

public class Main extends Application{

    @Getter
    private static Stage primaryStage;
    private static ServerSocket lockSocket;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        primaryStage.setTitle("IPCamera Configuration");

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("/ipcameras_gui.fxml"));
        AnchorPane grp = (AnchorPane) loader.load();

        Scene scene = new Scene(grp);
        primaryStage.setScene(scene);
        primaryStage.show();

        stage.setOnCloseRequest(event -> {
            Platform.exit();   // завершает JavaFX runtime
            System.exit(0);    // завершает JVM
        });
    }

    public static void main(String[] args) throws Exception {
//        Log.setLogger(new LoggerConfiguration()
//                .writeTo(seq("http://localhost:5341", "zYIEEfhv8tqX0QyUkAtm"))
//                .setMinimumLevel(LogEventLevel.Verbose)
//                .createLogger());

        Log.setLogger(new LoggerConfiguration()
                .writeTo(seq("http://192.168.20.1:5342", "qWdyGKLS4vWOrwAEyvH7"))
                .setMinimumLevel(LogEventLevel.Verbose)
                .createLogger());

        if (!lockInstance()) {
            return;
        }

        launch(args);
    }

    private static boolean lockInstance() {
        try {
            lockSocket = new ServerSocket(44555);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}