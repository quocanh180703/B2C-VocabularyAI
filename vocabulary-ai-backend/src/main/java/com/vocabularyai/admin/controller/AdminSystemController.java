package com.vocabularyai.admin.controller;

import com.vocabularyai.admin.dto.response.SystemHealthResponse;
import com.vocabularyai.admin.dto.response.SystemOverviewResponse;
import com.vocabularyai.admin.dto.response.UserRegistrationTrendResponse;
import com.vocabularyai.admin.service.AdminSystemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin System Controller
 * 
 * REST endpoints for admin system management (dashboard, monitoring, etc.)
 * Base path: /api/admin/system
 */
@RestController
@RequestMapping("/api/admin/system")
@Tag(name = "Admin System Management", description = "API for system dashboard and monitoring")
@RequiredArgsConstructor
@Slf4j
public class AdminSystemController {
    
    private final AdminSystemService adminSystemService;
    
    /**
     * Get system overview statistics
     * 
     * @return System overview data
     */
    @GetMapping("/overview")
    @Operation(summary = "Get system overview", description = "Get overall system statistics for dashboard")
    public ResponseEntity<SystemOverviewResponse> getSystemOverview() {
        log.info("GET /api/admin/system/overview - Get system overview");
        
        SystemOverviewResponse overview = adminSystemService.getSystemOverview();
        return ResponseEntity.ok(overview);
    }
    
    /**
     * Get user registration trend
     * 
     * @return User registration trend data
     */
    @GetMapping("/users/registration-trend")
    @Operation(summary = "Get registration trend", description = "Get user registration trend for last 30 days")
    public ResponseEntity<List<UserRegistrationTrendResponse>> getUserRegistrationTrend() {
        log.info("GET /api/admin/system/users/registration-trend - Get registration trend");
        
        List<UserRegistrationTrendResponse> trend = adminSystemService.getUserRegistrationTrend();
        return ResponseEntity.ok(trend);
    }
    
    /**
     * Get system health metrics
     * 
     * @return System health information
     */
    @GetMapping("/health")
    @Operation(summary = "Get system health", description = "Get system health metrics and status")
    public ResponseEntity<SystemHealthResponse> getSystemHealth() {
        log.info("GET /api/admin/system/health - Get system health");
        
        SystemHealthResponse health = adminSystemService.getSystemHealth();
        return ResponseEntity.ok(health);
    }
}
