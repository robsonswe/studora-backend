package com.studora.dto;

/**
 * Metrics serialization tier for DTOs.
 * <p>
 * When the {@code metrics} query parameter is not provided (null), the response
 * contains only structural fields (id, nome, parent references).
 * <ul>
 *   <li>{@code SUMMARY}: structural + progress/accuracy counts</li>
 *   <li>{@code FULL}: summary + time stats and difficulty distributions</li>
 * </ul>
 */
public enum MetricsLevel {
    SUMMARY,
    FULL
}
