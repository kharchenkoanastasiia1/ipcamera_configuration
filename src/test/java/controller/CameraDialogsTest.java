package controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.ipcamera.config.IPCamerasGUIController;
import org.ipcamera.config.InfoGuiController;
import org.ipcamera.config.SearchGuiController;
import org.junit.jupiter.api.Test;

import java.util.List;

import static controller.JavaFxTestSupport.runOnJavaFxThread;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CameraDialogsTest {
    @Test
    void searchFiltersNicknamesAndReturnsSelectedCamera() throws Exception {
        runOnJavaFxThread(() -> {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/search_gui.fxml"));
            loader.load();
            SearchGuiController search = loader.getController();
            Stage stage = mock(Stage.class);
            IPCamerasGUIController dashboard = mock(IPCamerasGUIController.class);
            when(dashboard.getNicknameList()).thenReturn(List.of("North Gate", "Loading Bay", "South Gate"));
            search.setDialogStage(stage);
            search.setController(dashboard);
            TextField input = (TextField) loader.getNamespace().get("searchTextField");
            ListView<?> results = (ListView<?>) loader.getNamespace().get("listView");

            assertFalse(results.isVisible());
            input.setText("missing");
            assertFalse(results.isVisible());
            input.setText("GATE");
            assertTrue(results.isVisible());
            assertEquals(List.of("North Gate", "South Gate"), results.getItems());
            input.clear();
            assertFalse(results.isVisible());
            assertTrue(results.getItems().isEmpty());
            results.getOnMouseClicked().handle(null);
            verify(stage, never()).close();

            input.setText("south");
            results.getSelectionModel().selectFirst();
            results.getOnMouseClicked().handle(null);
            verify(dashboard).setCurrentNickname("South Gate");
            verify(dashboard).setSearchByNicknameStatus(true);
            verify(stage).close();
            return null;
        });
    }

    @Test
    void informationDialogDisplaysReadOnlyManualAndAutoUpdateLists() throws Exception {
        runOnJavaFxThread(() -> {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/info_gui.fxml"));
            loader.load();
            InfoGuiController info = loader.getController();
            info.setManualText("North Gate");
            info.setAutobanText("Loading Bay");

            info.initData();

            TextArea manual = (TextArea) loader.getNamespace().get("manualTextArea");
            TextArea blocked = (TextArea) loader.getNamespace().get("autobanTextArea");
            assertEquals("North Gate", manual.getText());
            assertEquals("Loading Bay", blocked.getText());
            assertFalse(manual.isEditable());
            assertFalse(blocked.isEditable());
            return null;
        });
    }
}
