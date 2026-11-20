package controller;

import org.ipcamera.config.controller.LineUpdateController;
import org.ipcamera.config.entity.Camera;
import org.ipcamera.config.entity.Line;
import org.ipcamera.config.repository.lines_request.LineRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class LineUpdateControllerTest {
    @Test
    void restartsEveryCameraLineUsingItsRegistrationName() {
        Camera camera = mock(Camera.class);
        when(camera.getLine()).thenReturn(new String[]{"10", "20"});
        when(camera.getRegname()).thenReturn(new String[]{"other", "entry"});
        Line entry = mock(Line.class);
        Line other = mock(Line.class);
        when(entry.getRegname()).thenReturn("entry");
        when(other.getRegname()).thenReturn("other");

        LineUpdateController controller = new LineUpdateController();
        try (var repositories = mockConstruction(LineRepository.class)) {
            controller.updateLines(null, camera);
            controller.updateLines(List.of(entry, other), null);
            assertEquals(0, repositories.constructed().size());

            controller.updateLines(List.of(entry, other), camera);

            assertEquals(2, repositories.constructed().size());
            verify(repositories.constructed().get(0)).restart("10", other);
            verify(repositories.constructed().get(1)).restart("20", entry);
        }
    }
}
