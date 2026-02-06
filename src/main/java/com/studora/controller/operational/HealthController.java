package com.studora.controller.operational;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Custom Health Check Controller.
 * Provides a lightweight alternative to Spring Boot Actuator for basic health monitoring.
 */
@RestController
public class HealthController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
            "status", "UP",
            "message", "Studora API is running"
        );
    }
}
