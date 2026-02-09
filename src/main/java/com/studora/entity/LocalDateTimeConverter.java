package com.studora.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Converter to ensure LocalDateTime is stored in a consistent sortable string format in SQLite.
 * SQLite's string comparison depends on a uniform format (ISO-8601 without 'T' is standard for SQLite).
 */
@Converter(autoApply = true)
public class LocalDateTimeConverter implements AttributeConverter<LocalDateTime, String> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public String convertToDatabaseColumn(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.format(FORMATTER);
    }

    @Override
    public LocalDateTime convertToEntityAttribute(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        // Normalize: replace ISO 'T' with space and truncate any fractional seconds for consistent parsing
        String normalized = s.replace("T", " ");
        if (normalized.contains(".")) {
            normalized = normalized.substring(0, normalized.indexOf("."));
        }
        if (normalized.length() > 19) {
            normalized = normalized.substring(0, 19);
        }
        return LocalDateTime.parse(normalized, FORMATTER);
    }
}
