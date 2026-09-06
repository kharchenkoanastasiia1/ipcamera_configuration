package org.ipcamera.config;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import org.ipcamera.config.controller.*;
import org.ipcamera.config.db.H2Connector;
import org.ipcamera.config.entity.Camera;
import org.ipcamera.config.entity.ConfigProcessVariable;
import org.ipcamera.config.entity.Line;
import org.ipcamera.config.service.AlertForConfirmation;
import org.ipcamera.config.service.FileService;
import org.ipcamera.config.service.InfoService;
import org.ipcamera.config.service.StyleCameraInCollection;
import org.ipcamera.config.service.update_collections.LineUpdateService;
import org.ipcamera.config.service.util.CameraUtilsService;
import org.ipcamera.config.service.update_collections.CameraUpdateService;
import org.ipcamera.config.service.comparator.IPComparator;
import org.ipcamera.config.service.time.NTPTimeService;
import org.ipcamera.config.service.converter.TimeConverter;
import org.ipcamera.config.service.converter.URLConverter;
//import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
//import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static org.ipcamera.config.constants.Constants.*;
import static org.ipcamera.config.constants.ConstantsStyle.*;


public class IPCamerasGUIController {
    @FXML
    private GridPane gridPane;
    @FXML
    private HBox hbox;
    @FXML
    private HBox hboxForButtons;
    @FXML
    private HBox hboxForButtonsChangeParametersCamera;
    @FXML
    private ListView<String> cameraListViewSuccess;
    @FXML
    private ListView<String> cameraListViewNotSuccessGetRequest;
    @FXML
    private ListView<String> cameraListViewNotSuccessPing;
    @FXML
    private Label ipLabel;
    @FXML
    private Label lineLabel;
    @FXML
    private Label nicknameLabel;
    @FXML
    private Label pingLabel;
    @FXML
    private Label rtspLabel;
    @FXML
    private Label deltaTimeLabel;
    @FXML
    private Label versionLabel;
    @FXML
    private Label dateCheck;
    @FXML
    private Label dateUpdateSuccess;
    @FXML
    private Label currentTimeNtpLabel;
    @FXML
    private Label regnameLabel;
    @FXML
    private Label typeLabel;
    @FXML
    private Button runButton;
    @FXML
    private Button stopButton;
    @FXML
    private ImageView videoRtspView;
    @FXML
    private StackPane stackPane;
    @FXML
    private Button showVideoButton;
    @FXML
    private Button backButton;
    @FXML
    private Button forthButton;
    @FXML
    private Label descriptionLabel;
    @FXML
    private CheckBox manualCheckBox;
    @FXML
    private CheckBox banAutoUpdateCheckBox;
    @FXML
    private TextArea criticalErrorsTextArea;

    private Stage loadingStage;
    private ObservableList<String> camerasSuccess = FXCollections.observableArrayList();
    private ObservableList<String> camerasNotSuccessGetRequest = FXCollections.observableArrayList();
    private ObservableList<String> camerasNotSuccessPing = FXCollections.observableArrayList();
    private List<Camera> cameraList;
    private List<Line> lineList;
    @Getter
    @Setter
    private List<String> nicknameList;
    private String currentIPAddress;
    private int currentIndexVideo = 0;
    @Setter
    private String currentNickname;
    private Map<String, Camera> cameraMap;
    @Setter
    public static Boolean statusRtsp = false;
    @Setter
    public static Boolean rebootThread = false;
    @Setter
    public Boolean searchByNicknameStatus = false;
    private Boolean statusShowVideo = false;
    @Setter
    public Boolean manualStatusThread = false;

    @Getter
    private ConfigProcessVariable configProcessVariable;
    private TimeScheduler timeScheduler;
    private VideoRtspController videoRtspController;

    public IPCamerasGUIController() {}
    
    @FXML
    public void initialize(){
        //--------------------------Настройки графического интерфейса--------------------------
        hbox.setHgrow(cameraListViewSuccess, Priority.ALWAYS);
        hbox.setHgrow(cameraListViewNotSuccessGetRequest, Priority.ALWAYS);
        hbox.setHgrow(cameraListViewNotSuccessPing, Priority.ALWAYS);
        cameraListViewSuccess.prefWidthProperty().bind(hbox.widthProperty().multiply(0.33));
        cameraListViewNotSuccessGetRequest.prefWidthProperty().bind(hbox.widthProperty().multiply(0.33));
        cameraListViewNotSuccessPing.prefWidthProperty().bind(hbox.widthProperty().multiply(0.33));
        gridPane.setGridLinesVisible(true);

        //--------------------------Пауза при обновлении интерфейса--------------------------
        PauseTransition pause1 = new PauseTransition(Duration.millis(100));
        MultipleSelectionModel<String> successSelectionModel = cameraListViewSuccess.getSelectionModel();
        successSelectionModel.selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            pause1.stop();
            pause1.setOnFinished(event -> {
                try {
                    showDetails(newVal);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
                Platform.runLater(() -> {
                    cameraListViewNotSuccessPing.getSelectionModel().clearSelection();
                    cameraListViewNotSuccessGetRequest.getSelectionModel().clearSelection();
                    cameraListViewSuccess.getSelectionModel().select(currentIPAddress);
                });
            });
            pause1.playFromStart();
        });
        PauseTransition pause2 = new PauseTransition(Duration.millis(100));
        MultipleSelectionModel<String> notSuccessGetSelectionModel = cameraListViewNotSuccessGetRequest.getSelectionModel();
        notSuccessGetSelectionModel.selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            pause2.stop();
            pause2.setOnFinished(event -> {
                try {
                    showDetails(newVal);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
                Platform.runLater(() -> {
                    cameraListViewSuccess.getSelectionModel().clearSelection();
                    cameraListViewNotSuccessPing.getSelectionModel().clearSelection();
                    cameraListViewNotSuccessGetRequest.getSelectionModel().select(currentIPAddress);
                });
            });
            pause2.playFromStart();
        });
        PauseTransition pause3 = new PauseTransition(Duration.millis(100));
        MultipleSelectionModel<String> notSuccessSelectionModel = cameraListViewNotSuccessPing.getSelectionModel();
        notSuccessSelectionModel.selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            pause3.stop();
            pause3.setOnFinished(event -> {
                try {
                    showDetails(newVal);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
                Platform.runLater(() -> {
                    cameraListViewSuccess.getSelectionModel().clearSelection();
                    cameraListViewNotSuccessGetRequest.getSelectionModel().clearSelection();
                    cameraListViewNotSuccessPing.getSelectionModel().select(currentIPAddress);
                });
            });
            pause3.playFromStart();
        });

        //--------------------------Стилизация коллекции камер--------------------------

        cameraListViewSuccess.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle(null);
                } else {
                    Camera camera = cameraMap.get(item);
                    if(camera != null) {
                        if(!camera.getNickname()[0].isEmpty()) {
                            setText(item + ", " + camera.getNickname()[0]);
                        } else{
                            setText(item);
                        }
                        setStyle(StyleCameraInCollection.styleByColumn(camera, configProcessVariable, 1));
                    }

                }
            }
        });
        cameraListViewNotSuccessGetRequest.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String ip, boolean empty) {
                super.updateItem(ip, empty);
                if (empty || ip == null) {
                    setText(null);
                    setStyle(null);
                } else {
                    Camera camera = cameraMap.get(ip);
                    if (camera != null) {
                        if(!camera.getNickname()[0].isEmpty()) {
                            setText(ip + ", " + camera.getNickname()[0]);
                        } else{
                            setText(ip);
                        }
                        setStyle(StyleCameraInCollection.styleByColumn(camera, configProcessVariable, 2));
                    }
                }
            }
        });
        cameraListViewNotSuccessPing.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle(null);
                } else {
                    Camera camera = cameraMap.get(item);
                    if(camera != null) {
                        if(!camera.getNickname()[0].isEmpty()) {
                            setText(item  + ", " + camera.getNickname()[0]);
                        } else{
                            setText(item);
                        }
                        setStyle(StyleCameraInCollection.styleByColumn(camera, configProcessVariable, 3));
                    }
                }
            }
        });

        cameraListViewSuccess.setItems(camerasSuccess);
        cameraListViewNotSuccessGetRequest.setItems(camerasNotSuccessGetRequest);
        cameraListViewNotSuccessPing.setItems(camerasNotSuccessPing);

        //Кнопки управления центрированы:
        hboxForButtons.setSpacing(10);
        hboxForButtons.setAlignment(Pos.CENTER);

        hboxForButtonsChangeParametersCamera.setSpacing(10);
        hboxForButtonsChangeParametersCamera.setAlignment(Pos.CENTER);

        //Кнопка "Стоп" красная, поскольку потоки обновления не запущены:
        stopButton.setStyle(BUTTON_RED);
        stopButton.setDisable(true);

        //Часы:
        currentTimeNtpLabel.setStyle(CLOCK);
        Timeline timeline = getTimeline();
        timeline.play();

        //Настройка видео:
        PauseTransition pause4 = new PauseTransition(Duration.millis(100));
        stackPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            pause4.stop();
            pause4.setOnFinished(e -> {
                videoRtspView.setFitWidth(stackPane.getWidth());
                videoRtspView.setFitHeight(stackPane.getHeight() - 10);
                videoRtspView.setPreserveRatio(true);
            });
            pause4.playFromStart();
        });
        stackPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            pause4.stop();
            pause4.setOnFinished(e -> {
                videoRtspView.setFitWidth(stackPane.getWidth());
                videoRtspView.setFitHeight(stackPane.getHeight() - 10);
                videoRtspView.setPreserveRatio(true);
            });
            pause4.playFromStart();
        });

        forthButton.setVisible(false);
        backButton.setVisible(false);
        GridPane.setHalignment(forthButton, HPos.RIGHT);
        GridPane.setMargin(forthButton, new Insets(0, 10, 0, 0));
        GridPane.setHalignment(descriptionLabel, HPos.CENTER);

        manualCheckBox.setOnAction(event -> {
            if (currentIPAddress != null) {
                Camera camera = CameraUtilsService.getCameraByIp(cameraList, currentIPAddress);
                if (camera != null) {
                    boolean isSelected = manualCheckBox.isSelected();

                    Platform.runLater(() -> {
                        boolean confirmed = AlertForConfirmation.showConfirmation(
                                CONFIRM_ACTION, ADD_OBJECT_MANUAL_UPDATE_MODE
                        );

                        if (confirmed) {
                            camera.setManualMode(isSelected);
                            H2Connector.addTableManual(camera);
                            determineCameraPlaceInCollections(camera);
                        } else {
                            manualCheckBox.setSelected(!isSelected);
                        }
                    });
                }
            }
        });
        manualCheckBox.setDisable(true);

        banAutoUpdateCheckBox.setOnAction(event -> {
            if (currentIPAddress != null) {
                Camera camera = CameraUtilsService.getCameraByIp(cameraList, currentIPAddress);
                if (camera != null) {
                    boolean isSelected = banAutoUpdateCheckBox.isSelected();

                    Platform.runLater(() -> {
                        boolean confirmed = AlertForConfirmation.showConfirmation(
                                CONFIRM_ACTION, BAN_ON_AUTO_UPDATES
                        );

                        if (confirmed) {
                            camera.setBanOnAutoUpdate(isSelected);
                            H2Connector.addTableManual(camera);
                            determineCameraPlaceInCollections(camera);
                        } else {
                            banAutoUpdateCheckBox.setSelected(!isSelected);
                        }
                    });
                }
            }
        });
        banAutoUpdateCheckBox.setDisable(true);

        //--------------------------Начало логики--------------------------
        updateStrCriticalErrors();

        DBController dbController = new DBController();
        configProcessVariable = dbController.getConfiguration();

        timeScheduler = new TimeScheduler(this);
        timeScheduler.runInfoTimeThread();

        videoRtspController = new VideoRtspController();

        if(configProcessVariable.getCheckManual()){
            timeScheduler.runInfoTimeManualThread(cameraList);
        }
    }

    /**
     * Запуск автообновления коллекции камер
     * */
    @FXML
    private void handleRun(){
        runButton.setStyle(BUTTON_GREEN);
        runButton.setDisable(true);
        stopButton.setDisable(false);
        stopButton.setStyle("");

        timeScheduler.stopInfoTimeThread();
        timeScheduler.restartChangeTimeThread();
    }

    /**
     * Остановка автообновления коллекции камер
     * */
    @FXML
    private void handleStop(){
        stopButton.setStyle(BUTTON_RED);
        stopButton.setDisable(true);
        runButton.setDisable(false);
        runButton.setStyle("");

        timeScheduler.stopChangeTimeThread();
        timeScheduler.restartInfoTimeThread();
    }

    /**
     * Поиск по nickname
     * */
    @FXML
    private void handleSearch() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("/search_gui.fxml"));
        AnchorPane page = (AnchorPane) loader.load();

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Search");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(Main.getPrimaryStage());
        Scene scene = new Scene(page);
        dialogStage.setScene(scene);

        SearchGuiController controller = loader.getController();
        controller.setDialogStage(dialogStage);
        controller.setController(this);

        dialogStage.showAndWait();

        if(searchByNicknameStatus){
            Camera camera = CameraUtilsService.getCameraByNickname(cameraList, currentNickname);
            if(camera != null) {
                currentIPAddress = camera.getIpAddress();

                if(!camera.getStatusPing()) {
                    Platform.runLater(() -> {
                        cameraListViewNotSuccessPing.getSelectionModel().select(currentIPAddress);
                        cameraListViewNotSuccessPing.scrollTo(currentIPAddress);
                    });
                } else if(!camera.getStatusGetRequest() || camera.getDifferenceTime() > configProcessVariable.getDelta()){
                    Platform.runLater(() -> {
                        cameraListViewNotSuccessGetRequest.getSelectionModel().select(currentIPAddress);
                        cameraListViewNotSuccessGetRequest.scrollTo(currentIPAddress);
                    });
                } else{
                    Platform.runLater(() -> {
                        cameraListViewSuccess.getSelectionModel().select(currentIPAddress);
                        cameraListViewSuccess.scrollTo(currentIPAddress);
                    });
                }
            }
            searchByNicknameStatus = false;
        }
    }

    /**
    * Изменение параметров автообновления
    * */
    @FXML
    private void handleChangeAutoUpdate() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("/change_auto_update_gui.fxml"));
        AnchorPane page = (AnchorPane) loader.load();

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Edit configuration auto-update");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(Main.getPrimaryStage());
        Scene scene = new Scene(page);
        dialogStage.setScene(scene);

        ChangeAutoUpdateGuiController controller = loader.getController();
        controller.setDialogStage(dialogStage);
        controller.setConfigProcessVariable(configProcessVariable);
        controller.initData();

        dialogStage.showAndWait();
    }

    /**
     * Изменение параметров БД, работы потоков, разрешение rtsp
     * */
    @FXML
    private void handleChangeParameter() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("/change_config_gui.fxml"));
        AnchorPane page = (AnchorPane) loader.load();

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Edit configuration");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(Main.getPrimaryStage());
        Scene scene = new Scene(page);
        dialogStage.setScene(scene);

        ChangeConfigGuiController controller = loader.getController();
        controller.setDialogStage(dialogStage);
        controller.setConfigProcessVariable(configProcessVariable);
        controller.initData();

        dialogStage.showAndWait();

        if(rebootThread){
            if(timeScheduler.isRunningInfoTimeThread()){
                timeScheduler.restartInfoTimeThread();
            } else if(timeScheduler.isRunningChangeTimeThread()){
                timeScheduler.restartChangeTimeThread();
            }
            rebootThread = false;
        }

        if(configProcessVariable.getCheckManual()){
            timeScheduler.restartInfoTimeManualThread(cameraList);
        } else{
            timeScheduler.stopInfoTimeManualThread();
            manualStatusThread = false;
        }
    }

    @FXML
    private void handleInfo() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("/info_gui.fxml"));
        AnchorPane page = (AnchorPane) loader.load();

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Information");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(Main.getPrimaryStage());
        Scene scene = new Scene(page);
        dialogStage.setScene(scene);

        InfoGuiController controller = loader.getController();
        controller.setManualText(InfoService.getInfoByManual(cameraList));
        controller.setAutobanText(InfoService.getInfoByAutoBan(cameraList));
        controller.initData();

        dialogStage.showAndWait();
    }

    /**
     * Получение времени для текущей камеры
     * */
    @FXML
    private void handleChangeTimeThread() throws Exception {
        if(currentIPAddress != null){
            CameraTimeController cameraTimeController = new CameraTimeController(configProcessVariable);
            Camera camera = CameraUtilsService.getCameraByIp(cameraList, currentIPAddress);
            cameraTimeController.checkRelevanceTime(camera, configProcessVariable.getNtpHost());

            determineCameraPlaceInCollections(camera);
        }
    }

    /**
     * Изменение времени для текущей камеры
     * */
    @FXML
    private void handleRunThread() throws Exception {
        if(currentIPAddress != null){
            Camera camera = CameraUtilsService.getCameraByIp(cameraList, currentIPAddress);

            CameraTimeController cameraTimeController = new CameraTimeController(configProcessVariable);
            cameraTimeController.changeTimeDate(camera, configProcessVariable.getNtpHost(), false, lineList);

            determineCameraPlaceInCollections(camera);
            updateStrCriticalErrors();
        }
    }

    /**
     * Демонстрация видеопотока для текущей камеры
     * */
    @FXML
    private void handleShowVideo() throws URISyntaxException {
        if(currentIPAddress != null){
            if(!statusShowVideo){
                statusShowVideo = true;
                Camera camera = CameraUtilsService.getCameraByIp(cameraList, currentIPAddress);
                createFieldsVideo(camera);
            } else{
                statusShowVideo = false;
                clearFieldsVideo();
            }
        }
    }

    /**
     * Демонстрация предыдущего видеопотока для текущей камеры
     * */
    @FXML
    private void handleBackButton() throws URISyntaxException {
        if(currentIPAddress != null){
            Camera camera = CameraUtilsService.getCameraByIp(cameraList, currentIPAddress);

            if(camera.getTransmitUrl() != null){
                currentIndexVideo--;
                if (currentIndexVideo < 0) {
                    currentIndexVideo = camera.getTransmitUrl().length - 1;
                }
                startCurrentStream(camera);
            }
        }
    }

    /**
     * Демонстрация следующего видеопотока для текущей камеры
     * */
    @FXML
    private void handleForthButton() throws URISyntaxException {
        if(currentIPAddress != null){
            Camera camera = CameraUtilsService.getCameraByIp(cameraList, currentIPAddress);

            if(camera.getTransmitUrl() != null){
                currentIndexVideo++;
                if (currentIndexVideo >= camera.getTransmitUrl().length) {
                    currentIndexVideo = 0;
                }
                startCurrentStream(camera);
            }
        }
    }

    /**
     * Обновление коллекции камер и распределение по спискам статусов
     * */
    public void divideByStatus(List<Camera> cameras) {
        Platform.runLater(() -> {
            clearCollection();

            if(manualStatusThread){
                cameraList = cameras;
                manualStatusThread = false;
            } else if(timeScheduler.isRunningChangeTimeThread()){
                cameraList = CameraUpdateService.syncCameraListsByAutoUpdate(cameraList, cameras, configProcessVariable);
            } else{
                cameraList = CameraUpdateService.syncCameraLists(cameraList, cameras);
            }

            for (Camera camera : cameraList) {
                if(!camera.getStatusPing()) {
                    camerasNotSuccessPing.add(camera.getIpAddress());
                } else if(!camera.getStatusGetRequest() || camera.getDifferenceTime() > configProcessVariable.getDelta()){
                    camerasNotSuccessGetRequest.add(camera.getIpAddress());
                } else{
                    camerasSuccess.add(camera.getIpAddress());
                }
            }

            cameraMap = cameraList.stream()
                    .collect(Collectors.toMap(Camera::getIpAddress, c -> c));

            FXCollections.sort(camerasSuccess, IPComparator.compareIP());
            FXCollections.sort(camerasNotSuccessGetRequest, IPComparator.compareIPWithHighlight(configProcessVariable, cameraMap));
            FXCollections.sort(camerasNotSuccessPing, IPComparator.compareIP());

            if (loadingStage != null && loadingStage.isShowing()) {
                loadingStage.close();
            }
        });
    }

    /**
     * Очистка коллекций по статусу камер
     * */
    private void clearCollection() {
        cameraListViewSuccess.getSelectionModel().clearSelection();
        cameraListViewSuccess.getFocusModel().focus(-1);
        cameraListViewNotSuccessGetRequest.getSelectionModel().clearSelection();
        cameraListViewNotSuccessGetRequest.getFocusModel().focus(-1);
        cameraListViewNotSuccessPing.getSelectionModel().clearSelection();
        cameraListViewNotSuccessPing.getFocusModel().focus(-1);

        camerasSuccess.clear();
        camerasNotSuccessGetRequest.clear();
        camerasNotSuccessPing.clear();
    }

    /**
     * Определить статус/место для текущей камеры в коллекциях
     * */
    private void determineCameraPlaceInCollections(Camera camera) {
        divideByStatus(cameraList);

        if(!camera.getStatusPing()) {
            Platform.runLater(() -> {
                cameraListViewNotSuccessPing.getSelectionModel().select(currentIPAddress);
                cameraListViewNotSuccessPing.scrollTo(currentIPAddress);
            });
        } else if(!camera.getStatusGetRequest() || camera.getDifferenceTime() > configProcessVariable.getDelta()){
            Platform.runLater(() -> {
                cameraListViewNotSuccessGetRequest.getSelectionModel().select(currentIPAddress);
                cameraListViewNotSuccessGetRequest.scrollTo(currentIPAddress);
            });
        } else{
            Platform.runLater(() -> {
                cameraListViewSuccess.getSelectionModel().select(currentIPAddress);
                cameraListViewSuccess.scrollTo(currentIPAddress);
            });
        }
    }

    /**
    * Демонстрация деталей для текущей камеры
    * */
    public void showDetails(String ipAddress) throws URISyntaxException {
        Camera camera = null;
        if (ipAddress != null) {
            camera = CameraUtilsService.getCameraByIp(cameraList, ipAddress);
            currentIPAddress = ipAddress;
        } else{
            camera = CameraUtilsService.getCameraByIp(cameraList, currentIPAddress);
        }

        if(camera != null){
            ipLabel.setText(camera.getIpAddress());

            lineLabel.setText(String.join(", ", camera.getLine()));
            nicknameLabel.setText(String.join(", ", camera.getNickname()));
            regnameLabel.setText(String.join(", ", camera.getRegname()));

            pingLabel.setText(camera.getStatusPing() ? "OK" : "FAIL");
            if(statusRtsp){
                rtspLabel.setText(camera.getStatusRtsp() ? "OK" : "FAIL");
            } else{
                rtspLabel.setText(camera.getStatusRtsp() ? "OK" : "не визначено");
            }

            deltaTimeLabel.setText(camera.getDifferenceTime() != -1 ? camera.getDifferenceTime() + " хв" : "не визначено");
            versionLabel.setText(camera.getVersion() != null ? camera.getVersion() : "не визначено");
            typeLabel.setText(camera.getType());

            Date dateCh = camera.getDateCheck();
            dateParseForGUI(dateCh, dateCheck);

            Date dateUpdate = camera.getDateUpdateSuccess();
            dateParseForGUI(dateUpdate, dateUpdateSuccess);

            manualCheckBox.setDisable(false);
            manualCheckBox.setSelected(camera.getManualMode());

            banAutoUpdateCheckBox.setDisable(false);
            banAutoUpdateCheckBox.setSelected(camera.getBanOnAutoUpdate());

            if(statusShowVideo){
                createFieldsVideo(camera);
            }

//            videoRtspController.startPlaying("rtsp://root:root@192.168.1.160/axis-media/media.amp?camera=1", videoRtspView);
            //videoRtspController.startPlaying("http://localhost:8080/media", videoRtspView);
        } else{
            ipLabel.setText("");
            lineLabel.setText("");
            nicknameLabel.setText("");
            regnameLabel.setText("");
            pingLabel.setText("");
            rtspLabel.setText("");
            deltaTimeLabel.setText("");
            versionLabel.setText("");
            typeLabel.setText("");
            dateCheck.setText("");
            dateUpdateSuccess.setText("");

            manualCheckBox.setDisable(true);
            banAutoUpdateCheckBox.setDisable(true);

            clearFieldsVideo();
        }
    }

    /**
     * Конвертация даты в формат для вывода на экран
     * */
    private void dateParseForGUI(Date dateCh, Label dateCheck) {
        if(dateCh != null){
            dateCheck.setText(TimeConverter.formatDateForAxis(dateCh) + " " + TimeConverter.formatTimeForAxis(dateCh));
        } else{
            dateCheck.setText("");
        }
    }

    /**
     * Обновление текущей коллекции линий
     * */
    public void currentLineData(List<Line> lines) {
        lineList = LineUpdateService.syncLineLists(lineList, lines);
    }

    /**
     * Демонстрация часов (получение текущего времени)
     * */
    private Timeline getTimeline() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    NTPTimeService ntpTimeService = new NTPTimeService();
                    try {
                        Date date = ntpTimeService.getNTPTime(configProcessVariable.getNtpHost());
                        String formattedTime = new SimpleDateFormat(PATTERN_TIME_FORMAT_AXIS).format(date);
                        currentTimeNtpLabel.setText(formattedTime);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        return timeline;
    }

    /**
     * Всплывающее окно
     * */
    public void loadPreloader(String text)  {
        loadingStage = new Stage();
        loadingStage.initOwner(Main.getPrimaryStage());
        loadingStage.initModality(Modality.APPLICATION_MODAL);
        loadingStage.setScene(new Scene(new StackPane(new Label(text))
                , 250, 150));
        loadingStage.show();
    }

    /**
     * Запуск видеопотока для текущей камеры
     * */
    private void startCurrentStream(Camera camera) throws URISyntaxException {
        String url = camera.getTransmitUrl()[currentIndexVideo];
        String user = camera.getTransmitUsr()[currentIndexVideo];
        String pass = camera.getTransmitPsw()[currentIndexVideo];

        if(url != null && !url.isEmpty()){
            descriptionLabel.setText("Line: " + camera.getLine()[currentIndexVideo] + " Nickname: " + camera.getNickname()[currentIndexVideo]);
            videoRtspController.stop();
            videoRtspController.startPlaying(URLConverter.convertUrlToHttp(url, user, pass), videoRtspView);
//            videoRtspController.startPlaying("rtsp://root:root@192.168.1.160/axis-media/media.amp?camera=1", videoRtspView);
        }
    }

    /**
     * Управление (создание) демонстрацией полей видеопотока для текущей камеры
     * */
    private void createFieldsVideo(Camera camera) throws URISyntaxException {
        videoRtspView.setVisible(true);
        showVideoButton.setText("Приховати відео");
        descriptionLabel.setText("");
        forthButton.setVisible(true);
        backButton.setVisible(true);

        startCurrentStream(camera);
    }

    /**
     * Управление (очистка) демонстрацией полей видеопотока для текущей камеры
     * */
    private void clearFieldsVideo(){
        videoRtspController.stop();

        Platform.runLater(() -> videoRtspView.setImage(null));
        videoRtspView.setVisible(false);
        descriptionLabel.setText("Відео вимкнено");
        showVideoButton.setText("Показати відео");
        forthButton.setVisible(false);
        backButton.setVisible(false);
    }

    /**
     * Обновление текстового поля для оповещения об ошибках
     * */
    public void updateStrCriticalErrors(){
        try{
            StringBuilder errorsFromFile = FileService.read();
            Platform.runLater(() -> {
                criticalErrorsTextArea.setText(errorsFromFile.toString());
                criticalErrorsTextArea.positionCaret(criticalErrorsTextArea.getText().length());
            });
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
}
