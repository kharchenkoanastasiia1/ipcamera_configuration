package service;

import org.ipcamera.config.service.SoftWareVersionService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SoftWareVersionServiceTest {
    private final SoftWareVersionService service =
            new SoftWareVersionService();

    @Test
    void compareVersion_shouldReturnZero_whenVersionIsNull() {
        assertEquals(
                0,
                service.compareVersion(null)
        );
    }

    @Test
    void compareVersion_shouldReturnZero_whenVersionIsEmpty() {
        assertEquals(
                0,
                service.compareVersion("")
        );
    }

    @Test
    void compareVersion_shouldReturnZero_whenVersionIsStringNull() {
        assertEquals(
                0,
                service.compareVersion("null")
        );
    }

    @Test
    void compareVersion_shouldReturnThree_forVersionLessThan6() {
        assertEquals(3, service.compareVersion("5.20"));
        assertEquals(3, service.compareVersion("5.50"));
        assertEquals(3, service.compareVersion("5.99"));
    }

    @Test
    void compareVersion_shouldReturnOne_forVersionFrom6ToBefore930() {
        assertEquals(1, service.compareVersion("6.0"));
        assertEquals(1, service.compareVersion("7.0"));
        assertEquals(1, service.compareVersion("9.0"));
        assertEquals(1, service.compareVersion("9.29"));
    }

    @Test
    void compareVersion_shouldReturnZero_forVersion930() {
        assertEquals(
                0,
                service.compareVersion("9.30")
        );
    }

    @Test
    void compareVersion_shouldReturnTwo_forVersionAfter930AndBefore124() {
        assertEquals(2, service.compareVersion("9.31"));
        assertEquals(2, service.compareVersion("10.0"));
        assertEquals(2, service.compareVersion("12.3"));
    }

    @Test
    void compareVersion_shouldReturnZero_forVersion124() {
        assertEquals(
                0,
                service.compareVersion("12.4")
        );
    }

    @Test
    void compareVersion_shouldReturnZero_forVersionGreaterThan124() {
        assertEquals(0, service.compareVersion("13.0"));
        assertEquals(0, service.compareVersion("20.1"));
    }

    @Test
    void compareVersion_shouldCompareVersionsNumerically() {
        assertEquals(
                1,
                service.compareVersion("9.10")
        );
    }

    @Test
    void compareVersion_shouldSupportDifferentNumberOfVersionParts() {
        assertEquals(
                1,
                service.compareVersion("6")
        );

        assertEquals(
                2,
                service.compareVersion("10")
        );
    }
}
