package service;

import org.ipcamera.config.entity.Line;
import org.ipcamera.config.service.update_collections.LineUpdateService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LineUpdateServiceTest {
    @Test
    void syncLineLists_shouldReturnStreamList_whenCurrentListIsNull() {
        Line line = mock(Line.class);

        List<Line> streamList =
                new ArrayList<>(List.of(line));

        List<Line> result =
                LineUpdateService.syncLineLists(
                        null,
                        streamList
                );

        assertSame(streamList, result);
    }

    @Test
    void syncLineLists_shouldAddNewLine() {
        Line currentLine = mock(Line.class);
        when(currentLine.getRegname()).thenReturn("LINE1");

        Line newLine = mock(Line.class);
        when(newLine.getRegname()).thenReturn("LINE2");

        List<Line> currentList =
                new ArrayList<>(List.of(currentLine));

        List<Line> streamList =
                new ArrayList<>(List.of(newLine));

        List<Line> result =
                LineUpdateService.syncLineLists(
                        currentList,
                        streamList
                );

        assertEquals(1, result.size());
        assertSame(newLine, result.get(0));
    }

    @Test
    void syncLineLists_shouldRemoveLineMissingFromStream() {
        Line currentLine = mock(Line.class);
        when(currentLine.getRegname()).thenReturn("LINE1");

        Line streamLine = mock(Line.class);
        when(streamLine.getRegname()).thenReturn("LINE2");

        List<Line> currentList =
                new ArrayList<>(List.of(currentLine));

        List<Line> streamList =
                new ArrayList<>(List.of(streamLine));

        LineUpdateService.syncLineLists(
                currentList,
                streamList
        );

        assertEquals(1, currentList.size());
        assertSame(streamLine, currentList.get(0));
    }

    @Test
    void syncLineLists_shouldUpdateExistingLine() {
        Line currentLine = mock(Line.class);
        when(currentLine.getRegname()).thenReturn("LINE1");

        Line streamLine = mock(Line.class);
        when(streamLine.getRegname()).thenReturn("LINE1");
        when(streamLine.getIpAddress()).thenReturn("192.168.1.100");
        when(streamLine.getPort()).thenReturn(8080);
        when(streamLine.getLogin()).thenReturn("admin");
        when(streamLine.getPassword()).thenReturn("password");

        List<Line> currentList =
                new ArrayList<>(List.of(currentLine));

        List<Line> streamList =
                new ArrayList<>(List.of(streamLine));

        List<Line> result =
                LineUpdateService.syncLineLists(
                        currentList,
                        streamList
                );

        assertEquals(1, result.size());
        assertSame(currentLine, result.get(0));

        verify(currentLine).setRegname("LINE1");
        verify(currentLine).setIpAddress("192.168.1.100");
        verify(currentLine).setPort(8080);
        verify(currentLine).setLogin("admin");
        verify(currentLine).setPassword("password");
    }

    @Test
    void syncLineLists_shouldNotReplaceExistingLineObject() {
        Line currentLine = mock(Line.class);
        when(currentLine.getRegname()).thenReturn("LINE1");

        Line streamLine = mock(Line.class);
        when(streamLine.getRegname()).thenReturn("LINE1");

        List<Line> currentList =
                new ArrayList<>(List.of(currentLine));

        LineUpdateService.syncLineLists(
                currentList,
                List.of(streamLine)
        );

        assertSame(currentLine, currentList.get(0));
    }
}
