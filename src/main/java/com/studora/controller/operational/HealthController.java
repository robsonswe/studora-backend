package com.studora.controller.operational;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Custom Health Check Controller.
 * Provides a lightweight alternative to Spring Boot Actuator for basic health monitoring.
 */
@RestController
@Tag(name = "Operational", description = "Endpoints para monitoramento e operação do sistema")
public class HealthController {

    @Operation(
        summary = "Verificar saúde do sistema",
        description = "Retorna o status atual da API para fins de monitoramento.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Sistema operando normalmente",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = "{\"status\": \"UP\", \"message\": \"Studora API is running\"}"
                    )
                )
            )
        }
    )
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
            "status", "UP",
            "message", "Studora API is running"
        );
    }
}
