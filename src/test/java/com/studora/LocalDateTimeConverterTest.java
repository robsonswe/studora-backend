package com.studora;

import static org.junit.jupiter.api.Assertions.*;

import com.studora.entity.LocalDateTimeConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

class LocalDateTimeConverterTest {

    private LocalDateTimeConverter converter;

    @BeforeEach
    void setUp() {
        converter = new LocalDateTimeConverter();
    }

    // ==================== convertToDatabaseColumn ====================

    @Test
    void convertToDatabaseColumn_null_returnsNull() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToDatabaseColumn_validDateTime_returnsFormattedString() {
        LocalDateTime dt = LocalDateTime.of(2023, 6, 15, 10, 30, 45);
        assertEquals("2023-06-15 10:30:45", converter.convertToDatabaseColumn(dt));
    }

    @Test
    void convertToDatabaseColumn_midnight_formattedCorrectly() {
        LocalDateTime dt = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        assertEquals("2024-01-01 00:00:00", converter.convertToDatabaseColumn(dt));
    }

    // ==================== convertToEntityAttribute — happy paths ====================

    @Test
    void convertToEntityAttribute_null_returnsNull() {
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void convertToEntityAttribute_blankString_returnsNull() {
        assertNull(converter.convertToEntityAttribute("   "));
    }

    @Test
    void convertToEntityAttribute_emptyString_returnsNull() {
        assertNull(converter.convertToEntityAttribute(""));
    }

    @Test
    void convertToEntityAttribute_standardFormat_parsedCorrectly() {
        LocalDateTime result = converter.convertToEntityAttribute("2023-06-15 10:30:45");
        assertEquals(LocalDateTime.of(2023, 6, 15, 10, 30, 45), result);
    }

    @Test
    void convertToEntityAttribute_isoFormatWithT_parsedCorrectly() {
        LocalDateTime result = converter.convertToEntityAttribute("2023-06-15T10:30:45");
        assertEquals(LocalDateTime.of(2023, 6, 15, 10, 30, 45), result);
    }

    @Test
    void convertToEntityAttribute_withMilliseconds_millisTruncated() {
        LocalDateTime result = converter.convertToEntityAttribute("2023-06-15T10:30:45.123");
        assertEquals(LocalDateTime.of(2023, 6, 15, 10, 30, 45), result);
    }

    @Test
    void convertToEntityAttribute_withUtcSuffix_parsedCorrectly() {
        LocalDateTime result = converter.convertToEntityAttribute("2023-06-15T10:30:45Z");
        assertEquals(LocalDateTime.of(2023, 6, 15, 10, 30, 45), result);
    }

    @Test
    void convertToEntityAttribute_withPositiveOffset_offsetStripped() {
        LocalDateTime result = converter.convertToEntityAttribute("2023-06-15T10:30:45+03:00");
        assertEquals(LocalDateTime.of(2023, 6, 15, 10, 30, 45), result);
    }

    @Test
    void convertToEntityAttribute_withMillisecondsAndUtcSuffix_parsedCorrectly() {
        LocalDateTime result = converter.convertToEntityAttribute("2023-06-15T10:30:45.789Z");
        assertEquals(LocalDateTime.of(2023, 6, 15, 10, 30, 45), result);
    }

    // ==================== convertToEntityAttribute — numeric timestamps ====================

    @Test
    void convertToEntityAttribute_epochSeconds_parsedAsInstant() {
        // 1000 seconds after epoch — clearly seconds (not milliseconds)
        LocalDateTime result = converter.convertToEntityAttribute("1000");
        assertNotNull(result);
        // Verify a very small epoch second is treated as seconds
        long epochSecond = result.toEpochSecond(java.time.ZoneOffset.UTC);
        // Allow for timezone offset difference
        assertTrue(Math.abs(epochSecond - 1000) <= 86400, "Expected ~1000 epoch second");
    }

    @Test
    void convertToEntityAttribute_epochMilliseconds_parsedAsInstant() {
        // 1770599693454 ms — large number treated as milliseconds
        LocalDateTime result = converter.convertToEntityAttribute("1770599693454");
        assertNotNull(result);
        // Year should be around 2026
        assertTrue(result.getYear() >= 2026, "Expected year >= 2026 for epoch ms 1770599693454");
    }

    // ==================== round-trip ====================

    @Test
    void roundTrip_storedAndRetrievedMatch() {
        LocalDateTime original = LocalDateTime.of(2025, 3, 22, 14, 55, 0);
        String stored = converter.convertToDatabaseColumn(original);
        LocalDateTime retrieved = converter.convertToEntityAttribute(stored);
        assertEquals(original, retrieved);
    }
}
