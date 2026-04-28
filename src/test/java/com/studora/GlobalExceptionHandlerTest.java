package com.studora;

import static org.junit.jupiter.api.Assertions.*;

import com.studora.exception.ConflictException;
import com.studora.exception.GlobalExceptionHandler;
import com.studora.exception.ResourceNotFoundException;
import com.studora.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.util.ReflectionTestUtils;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        MockEnvironment env = new MockEnvironment();
        // No active profiles by default → prod mode
        ReflectionTestUtils.setField(handler, "env", env);
    }

    // ==================== ResourceNotFoundException → 404 ====================

    @Test
    void handleResourceNotFound_returns404WithDetail() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Banca", "ID", 42L);

        ProblemDetail detail = handler.handleResourceNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND.value(), detail.getStatus());
        assertTrue(detail.getDetail().contains("Banca"));
        assertEquals("Recurso não encontrado", detail.getTitle());
        assertEquals("about:blank", detail.getType().toString());
        assertNotNull(detail.getProperties().get("timestamp"));
    }

    @Test
    void handleResourceNotFound_customMessage_preserved() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Não encontrado");

        ProblemDetail detail = handler.handleResourceNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND.value(), detail.getStatus());
        assertEquals("Não encontrado", detail.getDetail());
    }

    // ==================== ConflictException → 409 ====================

    @Test
    void handleConflict_returns409WithDetail() {
        ConflictException ex = new ConflictException("Já existe uma banca com o nome 'CESPE'");

        ProblemDetail detail = handler.handleConflict(ex);

        assertEquals(HttpStatus.CONFLICT.value(), detail.getStatus());
        assertEquals("Já existe uma banca com o nome 'CESPE'", detail.getDetail());
        assertEquals("Conflito", detail.getTitle());
        assertEquals("about:blank", detail.getType().toString());
        assertNotNull(detail.getProperties().get("timestamp"));
    }

    // ==================== ValidationException → 422 ====================

    @Test
    void handleValidationException_returns422WithDetail() {
        ValidationException ex = new ValidationException("Uma questão deve ter pelo menos 2 alternativas");

        ProblemDetail detail = handler.handleValidationException(ex);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), detail.getStatus());
        assertEquals("Uma questão deve ter pelo menos 2 alternativas", detail.getDetail());
        assertEquals("Entidade não processável", detail.getTitle());
        assertEquals("about:blank", detail.getType().toString());
        assertNotNull(detail.getProperties().get("timestamp"));
    }

    // ==================== Generic Exception → 500 ====================

    @Test
    void handleGenericException_prodProfile_hidesInternalMessage() {
        RuntimeException ex = new RuntimeException("Internal DB error with secrets");

        ProblemDetail detail = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), detail.getStatus());
        assertEquals("Erro interno no servidor", detail.getTitle());
        // In prod mode the internal message must NOT be exposed
        assertFalse(detail.getDetail().contains("secrets"),
                "Internal error message must not be exposed in production");
        assertNotNull(detail.getProperties().get("timestamp"));
    }

    @Test
    void handleGenericException_devProfile_showsInternalMessage() {
        MockEnvironment devEnv = new MockEnvironment();
        devEnv.setActiveProfiles("dev");
        ReflectionTestUtils.setField(handler, "env", devEnv);

        RuntimeException ex = new RuntimeException("dev-only-detail");

        ProblemDetail detail = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), detail.getStatus());
        assertEquals("dev-only-detail", detail.getDetail());
        assertNotNull(detail.getProperties().get("exception"));
    }
}
