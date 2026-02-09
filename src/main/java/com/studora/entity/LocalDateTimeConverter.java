package com.studora.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

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

        try {
            // Try the standard format first
            return LocalDateTime.parse(s, FORMATTER);
        } catch (DateTimeParseException e) {
            // Check if it's a numeric timestamp (could be in milliseconds)
            try {
                long timestamp;
                if (s.matches("\\d+")) { // Check if string contains only digits
                    timestamp = Long.parseLong(s);
                    
                    // Determine if it's in seconds or milliseconds based on magnitude
                    // If the number is smaller, it's likely seconds; if larger, it's likely milliseconds
                    if (timestamp > 1e11) { // If it's a large number, treat as milliseconds (like 1770599693454)
                        return LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(timestamp), 
                                java.time.ZoneId.systemDefault());
                    } else { // Treat as seconds
                        return LocalDateTime.ofInstant(java.time.Instant.ofEpochSecond(timestamp), 
                                java.time.ZoneId.systemDefault());
                    }
                }
            } catch (NumberFormatException nfe) {
                // Not a numeric timestamp, continue with string parsing
            }

            // If that fails, try to normalize and parse as string
            String normalized = s.replace("T", " ");

            // Remove fractional seconds if present
            if (normalized.contains(".")) {
                int dotIndex = normalized.indexOf(".");
                normalized = normalized.substring(0, dotIndex);
            }

            // Remove timezone offset if present (e.g., +00:00, Z, etc.)
            if (normalized.contains("+")) {
                int plusIndex = normalized.indexOf("+");
                normalized = normalized.substring(0, plusIndex);
            } else if (normalized.endsWith("Z")) {
                normalized = normalized.substring(0, normalized.length() - 1);
            } else if (normalized.contains("-") && normalized.length() > 20) { // Check if it's a timezone offset like 2023-01-01T10:00:00-03:00
                // Find the last occurrence of "-" which could be a timezone indicator
                for (int i = normalized.length() - 3; i > 10; i--) {
                    if (normalized.charAt(i) == '-' && Character.isDigit(normalized.charAt(i + 1))) {
                        normalized = normalized.substring(0, i);
                        break;
                    }
                }
            }

            // Trim any extra characters beyond expected length
            normalized = normalized.trim();
            if (normalized.length() > 19) {
                normalized = normalized.substring(0, 19);
            }

            try {
                return LocalDateTime.parse(normalized, FORMATTER);
            } catch (DateTimeParseException ex) {
                // Log the error and return null if parsing still fails
                System.err.println("Failed to parse date: " + s + ", normalized: " + normalized);
                ex.printStackTrace();
                return null;
            }
        }
    }
}
