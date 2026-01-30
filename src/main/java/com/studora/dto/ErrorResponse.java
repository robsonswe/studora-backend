package com.studora.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "DTO para representar uma resposta de erro", name = "ErrorResponse")
public class ErrorResponse {

    @Schema(description = "Código do erro HTTP")
    private int statusCode;

    @Schema(description = "Mensagem de erro")
    private String message;

    @Schema(description = "Timestamp do erro", example = "2023-06-15T10:30:00")
    private LocalDateTime timestamp;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(int statusCode, String message) {
        this();
        this.statusCode = statusCode;
        this.message = message;
    }

    // Getters and Setters
    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}