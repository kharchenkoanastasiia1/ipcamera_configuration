package org.ipcamera.config;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import lombok.Setter;
import org.ipcamera.config.db.H2Connector;
import org.ipcamera.config.entity.ConfigProcessVariable;
import org.ipcamera.config.service.converter.URLConverter;
import static org.ipcamera.config.constants.ConstantsLogger.*;

public class ChangeConfigGuiController {
    @FXML
    private TextField dbHost;
    @FXML
    private TextField dbPort;
    @FXML
    private TextField dbPathToFile;
    @FXML
    private TextField ntpHost;
    @FXML
    private TextField delta;
    @FXML
    private TextField interval;
    @FXML
    private CheckBox checkRTSP;
    @FXML
    private TextField deltaManual;
    @FXML
    private TextField intervalManual;
    @FXML
    private CheckBox checkManual;
    @FXML
    private HBox hBox;
    @Setter
    private Stage dialogStage;
    @Setter
    private ConfigProcessVariable configProcessVariable;

    @FXML
    private void initialize(){
        hBox.setSpacing(10);
        hBox.setAlignment(Pos.CENTER);
    }

    @FXML
    public void handleUpdateDate(){
        IPCamerasGUIController.setRebootThread(true);
        dialogStage.close();
    }

    @FXML
    public void handleApplyChanges(){
        if (validData()) {
            configProcessVariable.setUrlDBFirebird(URLConverter.convertLabelToURLFirebird(
                    dbHost.getText(), dbPort.getText(), dbPathToFile.getText()));
            configProcessVariable.setNtpHost(ntpHost.getText());
            configProcessVariable.setDelta(Integer.parseInt(delta.getText()));
            configProcessVariable.setIntervalMinutes(Integer.parseInt(interval.getText()));
            configProcessVariable.setCheckRTSP(checkRTSP.isSelected());
            configProcessVariable.setDeltaManual(Integer.parseInt(deltaManual.getText()));
            configProcessVariable.setIntervalMinutesManual(Integer.parseInt(intervalManual.getText()));
            configProcessVariable.setCheckManual(checkManual.isSelected());

            IPCamerasGUIController.setStatusRtsp(checkRTSP.isSelected());
            IPCamerasGUIController.setRebootThread(true);

            H2Connector.addTableConfigurations(configProcessVariable);

            dialogStage.close();
        }
    }

    public void initData() {
        dialogStage.setTitle("Change settings");

        String[] urlSplit = URLConverter.convertURLFirebirdToLabel(configProcessVariable.getUrlDBFirebird());

        dbHost.setText(urlSplit[0]);
        dbPort.setText(urlSplit[1]);
        dbPathToFile.setText(urlSplit[2]);
        ntpHost.setText(configProcessVariable.getNtpHost());
        delta.setText(String.valueOf(configProcessVariable.getDelta()));
        interval.setText(String.valueOf(configProcessVariable.getIntervalMinutes()));
        checkRTSP.setSelected(configProcessVariable.getCheckRTSP());
        deltaManual.setText(String.valueOf(configProcessVariable.getDeltaManual()));
        intervalManual.setText(String.valueOf(configProcessVariable.getIntervalMinutesManual()));
        checkManual.setSelected(configProcessVariable.getCheckManual());

        IPCamerasGUIController.setStatusRtsp(checkRTSP.isSelected());
    }

    private Boolean validData(){
        StringBuilder errorMessage = new StringBuilder();

        if (dbHost.getText() == null || dbHost.getText().trim().isEmpty()) {
            errorMessage.append("Хост" + NOT_EMPTY);
        }

        if (dbPort.getText() == null || dbPort.getText().trim().isEmpty()) {
            errorMessage.append("Порт" +  NOT_EMPTY);
        }

        if (dbPathToFile.getText() == null || dbPathToFile.getText().trim().isEmpty()) {
            errorMessage.append("Шлях до файлу" +  NOT_EMPTY);
        }

        if (ntpHost.getText() == null || ntpHost.getText().trim().isEmpty()) {
            errorMessage.append("Хост" +  NOT_EMPTY);
        }

        if (delta.getText() == null || delta.getText().trim().isEmpty()) {
            errorMessage.append("Різниця часу" + NOT_EMPTY);
        } else {
            try {
                int deltaValue = Integer.parseInt(delta.getText().trim());
                if (deltaValue < 0) {
                    errorMessage.append("Різниця часу" + GREATER_THAN_0);
                }
            } catch (NumberFormatException e) {
                errorMessage.append("Різниця часу" + MUST_BE_NUMBER);
            }
        }

        long intervalValue = configProcessVariable.getIntervalMinutes();
        if (interval.getText() == null || interval.getText().trim().isEmpty()) {
            errorMessage.append("Час перезапуску" + NOT_EMPTY);
        } else {
            try {
                intervalValue = Long.parseLong(interval.getText().trim());
                if (intervalValue <= 0) {
                    errorMessage.append("Час перезапуску" + GREATER_THAN_0);
                }
            } catch (NumberFormatException e) {
                errorMessage.append("Час перезапуску" + MUST_BE_NUMBER);
            }
        }

        if(deltaManual.getText() == null || deltaManual.getText().trim().isEmpty()){
            errorMessage.append("Різниця часу для manual" + NOT_EMPTY);
        }else {
            try {
                int deltaValue = Integer.parseInt(deltaManual.getText().trim());
                if (deltaValue < 0) {
                    errorMessage.append("Різниця часу для manual" + GREATER_THAN_0);
                }
            } catch (NumberFormatException e) {
                errorMessage.append("Різниця часу для manual" + MUST_BE_NUMBER);
            }
        }

        if (intervalManual.getText() == null || intervalManual.getText().trim().isEmpty()) {
            errorMessage.append("Час перезапуску для manual" + NOT_EMPTY);
        } else {
            try {
                long intervalValueManual = Long.parseLong(intervalManual.getText().trim());
                if (intervalValueManual <= 0) {
                    errorMessage.append("Час перезапуску для manual" + GREATER_THAN_0);
                }

                if((intervalValue - intervalValueManual) < 20 && checkManual.isSelected()){
                    errorMessage.append(INCORRECT_INTERVAL_DIFFERENCE);
                }
            } catch (NumberFormatException e) {
                errorMessage.append("Час перезапуску для manual" + MUST_BE_NUMBER);
            }
        }

        if (!errorMessage.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(DATA_ENTRY_ERROR);
            alert.setHeaderText(INCORRECT_FIELD_VALUE);
            alert.setContentText(errorMessage.toString());
            alert.showAndWait();
            return false;
        }

        return true;
    }
}
