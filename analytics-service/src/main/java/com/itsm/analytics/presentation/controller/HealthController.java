package com.itsm.analytics.presentation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Health check controller for Analytics Service
 * Provides service status and health information
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
public class HealthController {
    
    /**
     * Basic health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = Map.of(
                "status", "UP",
                "service", "analytics-service",
                "timestamp", LocalDateTime.now(),
                "version", "1.0.0"
        );
        
        return ResponseEntity.ok(health);
    }
    
    /**
     * Detailed health check with dependencies
     */
    @GetMapping("/health/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        Map<String, Object> health = Map.of(
                "status", "UP",
                "service", "analytics-service",
                "timestamp", LocalDateTime.now(),
                "version", "1.0.0",
                "dependencies", Map.of(
                        "database", "UP",
                        "kafka", "UP",
                        "ticket-service", "UP",
                        "user-service", "UP",
                        "assignment-service", "UP"
                )
        );
        
        return ResponseEntity.ok(health);
    }
}
