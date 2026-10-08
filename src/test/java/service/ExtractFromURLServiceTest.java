package service;

import org.ipcamera.config.service.ExtractFromURLService;
import org.junit.jupiter.api.Test;

import static org.ipcamera.config.constants.Constants.PORT_DEFAULT;
import static org.junit.jupiter.api.Assertions.*;

class ExtractFromURLServiceTest {

    @Test
    void extractIP_shouldExtractIpAddress() {
        String result =
                ExtractFromURLService.extractIP(
                        "rtsp://192.168.1.100:554/1"
                );

        assertEquals("192.168.1.100", result);
    }

    @Test
    void extractIP_shouldExtractIpFromUrlWithPath() {
        String result =
                ExtractFromURLService.extractIP(
                        "http://admin:password@10.20.30.40:8080/camera"
                );

        assertEquals("10.20.30.40", result);
    }

    @Test
    void extractIP_shouldReturnNull_whenIpIsNotPresent() {
        assertNull(
                ExtractFromURLService.extractIP(
                        "http://camera.example.com:554/1"
                )
        );
    }

    @Test
    void extractIP_shouldReturnNull_whenInputIsInvalid() {
        assertNull(
                ExtractFromURLService.extractIP(
                        "not-an-ip"
                )
        );
    }

    @Test
    void extractPort_shouldExtractPort() {
        assertEquals(
                554,
                ExtractFromURLService.extractPort(
                        "rtsp://192.168.1.100:554/1"
                )
        );
    }

    @Test
    void extractPort_shouldExtractAnotherPort() {
        assertEquals(
                8080,
                ExtractFromURLService.extractPort(
                        "http://192.168.1.100:8080/camera"
                )
        );
    }

    @Test
    void extractPort_shouldReturnDefaultPort_whenPortIsMissing() {
        assertEquals(
                PORT_DEFAULT,
                ExtractFromURLService.extractPort(
                        "http://192.168.1.100/camera"
                )
        );
    }

    @Test
    void extractPort_shouldReturnDefaultPort_whenInputDoesNotContainPort() {
        assertEquals(
                PORT_DEFAULT,
                ExtractFromURLService.extractPort(
                        "192.168.1.100"
                )
        );
    }
}