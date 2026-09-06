package service;

import org.ipcamera.config.service.converter.TimeConverter;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

public class TimeConverterTest {
    @Test
    void formatDateTimeForIPCamera_shouldReturnFormattedDate() {
        Calendar calendar = Calendar.getInstance(
                TimeZone.getTimeZone("UTC")
        );

        calendar.set(
                2024,
                Calendar.JANUARY,
                15,
                12,
                30,
                45
        );
        calendar.set(Calendar.MILLISECOND, 0);

        Date date = calendar.getTime();

        String result =
                TimeConverter.formatDateTimeForIPCamera(date);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void formatDateTimeForIPCamera_shouldReturnEmptyString_whenDateIsNull() {
        assertEquals(
                "",
                TimeConverter.formatDateTimeForIPCamera(null)
        );
    }

    @Test
    void formatTimeForAxis_shouldReturnFormattedTime() {
        Calendar calendar = Calendar.getInstance(
                TimeZone.getTimeZone("UTC")
        );

        calendar.set(
                2024,
                Calendar.JANUARY,
                15,
                12,
                30,
                45
        );
        calendar.set(Calendar.MILLISECOND, 0);

        String result =
                TimeConverter.formatTimeForAxis(
                        calendar.getTime()
                );

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void formatTimeForAxis_shouldReturnEmptyString_whenDateIsNull() {
        assertEquals(
                "",
                TimeConverter.formatTimeForAxis(null)
        );
    }

    @Test
    void formatDateForAxis_shouldReturnFormattedDate() {
        Calendar calendar = Calendar.getInstance(
                TimeZone.getTimeZone("UTC")
        );

        calendar.set(
                2024,
                Calendar.JANUARY,
                15,
                12,
                30,
                45
        );
        calendar.set(Calendar.MILLISECOND, 0);

        String result =
                TimeConverter.formatDateForAxis(
                        calendar.getTime()
                );

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void formatDateForAxis_shouldReturnEmptyString_whenDateIsNull() {
        assertEquals(
                "",
                TimeConverter.formatDateForAxis(null)
        );
    }

    @Test
    void convertStringToDateForAxis_shouldConvertValidDateAndTime()
            throws ParseException {

        Date result = TimeConverter.convertStringToDateForAxis(
                "2025-01-15",
                "12:30:45"
        );

        assertNotNull(result);
    }

    @Test
    void convertStringToDateForAxis_shouldReturnNull_whenDateIsNull()
            throws ParseException {

        assertNull(
                TimeConverter.convertStringToDateForAxis(
                        null,
                        "12:30:45"
                )
        );
    }

    @Test
    void convertStringToDateForAxis_shouldReturnNull_whenTimeIsNull()
            throws ParseException {

        assertNull(
                TimeConverter.convertStringToDateForAxis(
                        "15.01.2024",
                        null
                )
        );
    }

    @Test
    void convertStringToDateForAxis_shouldReturnNull_whenDateIsEmpty()
            throws ParseException {

        assertNull(
                TimeConverter.convertStringToDateForAxis(
                        "",
                        "12:30:45"
                )
        );
    }

    @Test
    void convertStringToDateForAxis_shouldReturnNull_whenTimeIsEmpty()
            throws ParseException {

        assertNull(
                TimeConverter.convertStringToDateForAxis(
                        "15.01.2024",
                        ""
                )
        );
    }

    @Test
    void convertStringToDateForAxis_shouldThrowParseException_whenDateIsInvalid() {
        assertThrows(
                ParseException.class,
                () -> TimeConverter.convertStringToDateForAxis(
                        "invalid-date",
                        "12:30:45"
                )
        );
    }

    @Test
    void convertStringToDateForAxis_shouldThrowParseException_whenTimeIsInvalid() {
        assertThrows(
                ParseException.class,
                () -> TimeConverter.convertStringToDateForAxis(
                        "15.01.2024",
                        "invalid-time"
                )
        );
    }

    @Test
    void convertLocalDateTimeToString_shouldReturnFormattedString()
            throws ParseException {

        LocalDateTime dateTime = LocalDateTime.of(
                2024,
                1,
                15,
                12,
                30,
                45
        );

        String result =
                TimeConverter.convertLocalDateTimeToString(dateTime);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void convertLocalDateTimeToString_shouldReturnEmptyString_whenDateIsNull()
            throws ParseException {

        assertEquals(
                "",
                TimeConverter.convertLocalDateTimeToString(null)
        );
    }
}
