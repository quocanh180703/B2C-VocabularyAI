package com.vocabularyai.common.controller;

import com.vocabularyai.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Health check endpoints for monitoring application status
 */
@Slf4j
@RestController
@RequestMapping("/health")
@Tag(name = "Health Check", description = "Health check endpoints")
public class HealthCheckController {
    
    @Value("${spring.application.name:VocabularyAI}")
    private String appName;
    
    @Value("${spring.profiles.active:default}")
    private String activeProfile;
    
    /**
     * Simple health check endpoint
     */
    @GetMapping
    @Operation(
        summary = "Health Check",
        description = "Simple health check endpoint to verify application is running"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Application is healthy"
        )
    })
    public ResponseEntity<ApiResponse<?>> healthCheck() {
        log.info("Health check endpoint called");
        return ResponseEntity.ok(ApiResponse.success("Application is healthy"));
    }
    
    /**
     * Detailed health check endpoint
     */
    @GetMapping("/detailed")
    @Operation(
        summary = "Detailed Health Check",
        description = "Detailed health check with application information"
    )
    public ResponseEntity<ApiResponse<?>> detailedHealthCheck() {
        log.info("Detailed health check endpoint called");
        
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("application", appName);
        healthInfo.put("profile", activeProfile);
        healthInfo.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(ApiResponse.success("Application is running", healthInfo));
    }
    
    /**
     * Ready probe endpoint (Kubernetes)
     */
    @GetMapping("/ready")
    @Operation(
        summary = "Ready Probe",
        description = "Kubernetes readiness probe endpoint"
    )
    public ResponseEntity<ApiResponse<?>> readyProbe() {
        log.info("Ready probe endpoint called");
        return ResponseEntity.ok(ApiResponse.success("Application is ready"));
    }
    
    /**
     * Live probe endpoint (Kubernetes)
     */
    @GetMapping("/live")
    @Operation(
        summary = "Live Probe",
        description = "Kubernetes liveness probe endpoint"
    )
    public ResponseEntity<ApiResponse<?>> liveProbe() {
        log.info("Live probe endpoint called");
        return ResponseEntity.ok(ApiResponse.success("Application is alive"));
    }
}
