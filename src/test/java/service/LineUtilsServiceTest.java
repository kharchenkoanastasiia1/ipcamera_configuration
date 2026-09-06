package service;

import org.ipcamera.config.entity.Line;
import org.ipcamera.config.service.util.LineUtilsService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LineUtilsServiceTest {

    @Test
    void getLineByRegname_shouldReturnLine() {
        Line line = mock(Line.class);

        when(line.getRegname())
                .thenReturn("LINE_1");

        Line result =
                LineUtilsService.getLineByRegname(
                        List.of(line),
                        "LINE_1"
                );

        assertSame(line, result);
    }

    @Test
    void getLineByRegname_shouldReturnNull_whenNotFound() {
        Line line = mock(Line.class);

        when(line.getRegname())
                .thenReturn("LINE_1");

        Line result =
                LineUtilsService.getLineByRegname(
                        List.of(line),
                        "LINE_2"
                );

        assertNull(result);
    }

    @Test
    void getLineByRegname_shouldReturnNull_whenListIsNull() {
        assertNull(
                LineUtilsService.getLineByRegname(
                        null,
                        "LINE_1"
                )
        );
    }

    @Test
    void getLineByRegname_shouldReturnNull_whenRegnameIsNull() {
        assertNull(
                LineUtilsService.getLineByRegname(
                        List.of(mock(Line.class)),
                        null
                )
        );
    }

    @Test
    void getLineByRegname_shouldReturnNull_whenListIsEmpty() {
        assertNull(
                LineUtilsService.getLineByRegname(
                        List.of(),
                        "LINE_1"
                )
        );
    }
}
