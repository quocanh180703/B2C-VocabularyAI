package com.vocabularyai.admin.controller;

import com.vocabularyai.admin.dto.response.*;
import com.vocabularyai.admin.service.AdminReportsService;
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
 * Admin Reports Controller
 * 
 * REST endpoints for reporting and analytics
 * Base path: /api/admin/reports
 */
@RestController
@RequestMapping("/api/admin/reports")
@Tag(name = "Admin Reports", description = "API for analytics and reporting")
@RequiredArgsConstructor
@Slf4j
public class AdminReportsController {
    
    private final AdminReportsService adminReportsService;
    
    /**
     * Get user engagement report
     * 
     * @return User engagement data
     */
    @GetMapping("/engagement")
    @Operation(summary = "Get engagement report", description = "Get user engagement metrics for last 30 days")
    public ResponseEntity<List<UserEngagementReportResponse>> getUserEngagementReport() {
        log.info("GET /api/admin/reports/engagement - Get engagement report");
        
        List<UserEngagementReportResponse> report = adminReportsService.getUserEngagementReport();
        return ResponseEntity.ok(report);
    }
    
    /**
     * Get learning progress report
     * 
     * @return Learning progress data
     */
    @GetMapping("/learning-progress")
    @Operation(summary = "Get learning progress report", description = "Get overall learning statistics")
    public ResponseEntity<LearningProgressReportResponse> getLearningProgressReport() {
        log.info("GET /api/admin/reports/learning-progress - Get learning progress report");
        
        LearningProgressReportResponse report = adminReportsService.getLearningProgressReport();
        return ResponseEntity.ok(report);
    }
    
    /**
     * Get content performance report
     * 
     * @return Content performance data
     */
    @GetMapping("/content-performance")
    @Operation(summary = "Get content performance report", description = "Get performance metrics for all content")
    public ResponseEntity<List<ContentPerformanceReportResponse>> getContentPerformanceReport() {
        log.info("GET /api/admin/reports/content-performance - Get content performance report");
        
        List<ContentPerformanceReportResponse> report = adminReportsService.getContentPerformanceReport();
        return ResponseEntity.ok(report);
    }
    
    /**
     * Get revenue report
     * 
     * @return Revenue data
     */
    @GetMapping("/revenue")
    @Operation(summary = "Get revenue report", description = "Get financial metrics and revenue data")
    public ResponseEntity<RevenueReportResponse> getRevenueReport() {
        log.info("GET /api/admin/reports/revenue - Get revenue report");
        
        RevenueReportResponse report = adminReportsService.getRevenueReport();
        return ResponseEntity.ok(report);
    }
}
