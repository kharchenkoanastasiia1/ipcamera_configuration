package org.ipcamera.config.service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static org.ipcamera.config.constants.Constants.CRITICAL_ERROR_FILE;

public class FileService {
    private static final Path APP_DIR = Paths.get(System.getProperty("user.dir"));

    public static void write(String strError) throws IOException {
        Path file = APP_DIR.resolve(CRITICAL_ERROR_FILE);

        Files.writeString(
                file,
                strError + System.lineSeparator(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );
    }

    public static StringBuilder read() throws IOException {
        Path file = APP_DIR.resolve(CRITICAL_ERROR_FILE);
        StringBuilder sb = new StringBuilder();

        if (!Files.exists(file)) {
            return sb;
        }

        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            sb.append(line).append('\n');
        }
        return sb;
    }
}
