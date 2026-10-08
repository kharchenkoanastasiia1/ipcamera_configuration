package org.ipcamera.config;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import lombok.Setter;

public class InfoGuiController {
    @FXML
    private TextArea manualTextArea;
    @FXML
    private TextArea autobanTextArea;

    @Setter
    private String manualText;
    @Setter
    private String autobanText;

    @FXML
    private void initialize() {
        manualTextArea.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) autobanTextArea.deselect();
        });

        autobanTextArea.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) manualTextArea.deselect();
        });
    }

    public void initData(){
        manualTextArea.setText(manualText);
        manualTextArea.setEditable(false);
        manualTextArea.setFocusTraversable(false);
        autobanTextArea.setText(autobanText);
        autobanTextArea.setEditable(false);
        autobanTextArea.setFocusTraversable(false);
    }
}
