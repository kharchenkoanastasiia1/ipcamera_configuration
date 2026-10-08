package org.ipcamera.config;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;
import lombok.Setter;
import org.ipcamera.config.controller.DBController;
import org.ipcamera.config.db.H2Connector;
import org.ipcamera.config.entity.ConfigProcessVariable;

public class ChangeAutoUpdateGuiController {
    @FXML
    private CheckBox checkIPCamera;
    @FXML
    private CheckBox checkAxis;
    @FXML
    private CheckBox checkManual;
    @Setter
    private Stage dialogStage;
    @Setter
    private ConfigProcessVariable configProcessVariable;

    @FXML
    private void initialize() {
    }

    @FXML
    private void handleApply(){
        configProcessVariable.setAutoUpdateIPCamera(checkIPCamera.isSelected());
        configProcessVariable.setAutoUpdateAxis(checkAxis.isSelected());
        configProcessVariable.setAutoUpdateManual(checkManual.isSelected());

        H2Connector.addTableConfigurations(configProcessVariable);

        dialogStage.close();
    }

    public void initData(){
        checkIPCamera.setSelected(configProcessVariable.getAutoUpdateIPCamera());
        checkAxis.setSelected(configProcessVariable.getAutoUpdateAxis());
        checkManual.setSelected(configProcessVariable.getAutoUpdateManual());
    }
}