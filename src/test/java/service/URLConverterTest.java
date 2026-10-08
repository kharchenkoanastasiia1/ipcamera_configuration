package service;

import org.ipcamera.config.service.converter.URLConverter;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

public class URLConverterTest {
    @Test
    void convertLabelToURLFirebird_shouldBuildCorrectUrl() {
        String result = URLConverter.convertLabelToURLFirebird(
                "localhost",
                "3050",
                "database.fdb"
        );

        assertEquals(
                "jdbc:firebirdsql://localhost:3050/database.fdb",
                result
        );
    }

    @Test
    void convertLabelToURLFirebird_shouldWorkWithIpAddress() {
        String result = URLConverter.convertLabelToURLFirebird(
                "192.168.1.100",
                "3050",
                "C:/database/test.fdb"
        );

        assertEquals(
                "jdbc:firebirdsql://192.168.1.100:3050/C:/database/test.fdb",
                result
        );
    }

    @Test
    void convertURLFirebirdToLabel_shouldParseUrlWithPort() {
        String[] result = URLConverter.convertURLFirebirdToLabel(
                "jdbc:firebirdsql://localhost:3050/database.fdb"
        );

        assertArrayEquals(
                new String[]{
                        "localhost",
                        "3050",
                        "database.fdb"
                },
                result
        );
    }

    @Test
    void convertURLFirebirdToLabel_shouldUseDefaultPort_whenPortIsMissing() {
        String[] result = URLConverter.convertURLFirebirdToLabel(
                "jdbc:firebirdsql://localhost/database.fdb"
        );

        assertArrayEquals(
                new String[]{
                        "localhost",
                        "3050",
                        "database.fdb"
                },
                result
        );
    }

    @Test
    void convertURLFirebirdToLabel_shouldWorkWithIpAddress() {
        String[] result = URLConverter.convertURLFirebirdToLabel(
                "jdbc:firebirdsql://192.168.1.100:3051/test.fdb"
        );

        assertArrayEquals(
                new String[]{
                        "192.168.1.100",
                        "3051",
                        "test.fdb"
                },
                result
        );
    }

    @Test
    void convertURLFirebirdToLabel_shouldThrowException_whenPrefixIsInvalid() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> URLConverter.convertURLFirebirdToLabel(
                        "jdbc:mysql://localhost:3306/database"
                )
        );

        assertNotNull(exception.getMessage());
    }

    @Test
    void convertURLFirebirdToLabel_shouldThrowException_whenPathIsMissing() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> URLConverter.convertURLFirebirdToLabel(
                        "jdbc:firebirdsql://localhost:3050"
                )
        );

        assertNotNull(exception.getMessage());
    }

    @Test
    void convertURLFirebirdToLabel_shouldAllowEmptyPath() {
        String[] result = URLConverter.convertURLFirebirdToLabel(
                "jdbc:firebirdsql://localhost:3050/"
        );

        assertArrayEquals(
                new String[]{
                        "localhost",
                        "3050",
                        ""
                },
                result
        );
    }

    @Test
    void convertUrlToHttp_shouldConvertUrlWithPortAndCredentials()
            throws URISyntaxException {

        String result = URLConverter.convertUrlToHttp(
                "https://192.168.1.100:8080/axis-cgi/param.cgi",
                "admin",
                "password"
        );

        assertEquals(
                "http://admin:password@192.168.1.100:8080/axis-cgi/param.cgi",
                result
        );
    }

    @Test
    void convertUrlToHttp_shouldConvertUrlWithoutPort()
            throws URISyntaxException {

        String result = URLConverter.convertUrlToHttp(
                "https://192.168.1.100/axis-cgi/param.cgi",
                "admin",
                "password"
        );

        assertEquals(
                "http://admin:password@192.168.1.100/axis-cgi/param.cgi",
                result
        );
    }

    @Test
    void convertUrlToHttp_shouldPreservePath()
            throws URISyntaxException {

        String result = URLConverter.convertUrlToHttp(
                "https://example.com:8080/some/path/file",
                "user",
                "pass"
        );

        assertEquals(
                "http://user:pass@example.com:8080/some/path/file",
                result
        );
    }

    @Test
    void convertUrlToHttp_shouldThrowException_whenUrlIsInvalid() {
        assertThrows(
                URISyntaxException.class,
                () -> URLConverter.convertUrlToHttp(
                        "://invalid-url",
                        "admin",
                        "password"
                )
        );
    }
}
