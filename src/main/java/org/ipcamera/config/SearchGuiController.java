package org.ipcamera.config;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

public class SearchGuiController {
    @FXML
    private TextField searchTextField;
    @FXML
    private ListView<String> listView;
    @Setter
    private Stage dialogStage;
    @Setter
    private IPCamerasGUIController controller;

    @FXML
    private void initialize() {
//        searchTextField.setPromptText("Введіть nickname...");
        listView.setVisible(false);

        ChangeListener<String> listener = (obs, oldText, newText) -> {
            if (newText.isEmpty()) {
                listView.setVisible(false);
                listView.getItems().clear();
            } else {
                List<String> filtered = controller.getNicknameList().stream()
                        .filter(nick -> nick.toLowerCase().contains(newText.toLowerCase()))
                        .collect(Collectors.toList());
                if (!filtered.isEmpty()) {
                    listView.setItems(FXCollections.observableArrayList(filtered));
                    listView.setVisible(true);
                } else {
                    listView.setVisible(false);
                }
            }
        };

        searchTextField.textProperty().addListener(listener);

        listView.setOnMouseClicked(e -> {
            String selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
//                searchTextField.setText(selected);
//                listView.setVisible(false);

                controller.setCurrentNickname(selected);
                controller.setSearchByNicknameStatus(true);
                dialogStage.close();
            }
        });
    }
}
