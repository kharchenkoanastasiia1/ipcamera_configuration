package service;

import org.ipcamera.config.service.FileService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.ipcamera.config.constants.Constants.CRITICAL_ERROR_FILE;
import static org.junit.jupiter.api.Assertions.*;

public class FileServiceTest {
    private final Path file =
            Paths.get(System.getProperty("user.dir"))
                    .resolve(CRITICAL_ERROR_FILE);

    @AfterEach
    void cleanup() throws IOException {
        Files.deleteIfExists(file);
    }

    @Test
    void read_shouldReturnEmptyStringBuilder_whenFileDoesNotExist()
            throws IOException {

        Files.deleteIfExists(file);

        StringBuilder result = FileService.read();

        assertNotNull(result);
        assertEquals("", result.toString());
    }

    @Test
    void write_shouldCreateFile() throws IOException {
        Files.deleteIfExists(file);

        FileService.write("Test error");

        assertTrue(Files.exists(file));

        String content =
                Files.readString(
                        file,
                        StandardCharsets.UTF_8
                );

        assertEquals(
                "Test error" + System.lineSeparator(),
                content
        );
    }

    @Test
    void read_shouldReturnWrittenContent() throws IOException {
        Files.deleteIfExists(file);

        FileService.write("First error");

        StringBuilder result = FileService.read();

        assertEquals(
                "First error\n",
                result.toString()
        );
    }

    @Test
    void write_shouldAppendToExistingFile() throws IOException {
        Files.deleteIfExists(file);

        FileService.write("First error");
        FileService.write("Second error");

        StringBuilder result = FileService.read();

        assertEquals(
                "First error\nSecond error\n",
                result.toString()
        );
    }

    @Test
    void write_shouldSupportUtf8Text() throws IOException {
        Files.deleteIfExists(file);

        String text = "Помилка камери — тест UTF-8";

        FileService.write(text);

        StringBuilder result = FileService.read();

        assertEquals(
                text + "\n",
                result.toString()
        );
    }
}
