package com.vocabularyai.admin.controller;

import com.vocabularyai.admin.dto.response.DashboardSummaryResponse;
import com.vocabularyai.admin.dto.response.DashboardRevenueResponse;
import com.vocabularyai.admin.dto.response.TopStudentResponse;
import com.vocabularyai.admin.dto.response.ActivityTrendResponse;
import com.vocabularyai.admin.service.AdminDashboardService;
import com.vocabularyai.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin Dashboard Controller
 * 
 * Endpoints for admin dashboard metrics and analytics
 * All endpoints require ADMIN role authorization
 * 
 * Endpoints:
 * - GET /api/admin/dashboard/summary - Key metrics at a glance
 * - GET /api/admin/dashboard/revenue - Detailed revenue metrics
 * - GET /api/admin/dashboard/top-students - Top performing students
 * - GET /api/admin/dashboard/activity-trend - 30-day activity trends
 */
@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Dashboard", description = "Admin dashboard analytics and metrics endpoints")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {
    
    private final AdminDashboardService dashboardService;
    
    /**
     * Get dashboard summary with key metrics
     * 
     * Returns high-level system metrics:
     * - Total registered users
     * - Active subscriptions
     * - Total revenue
     * - Learning sessions today
     * - System status
     * 
     * Endpoint: GET /api/admin/dashboard/summary
     * Auth: Required (ADMIN role)
     * 
     * Response (200 OK):
     * {
     *   "success": true,
     *   "message": "Dashboard summary retrieved successfully",
     *   "data": {
     *     "totalUsers": 1250,
     *     "activeSubscriptions": 450,
     *     "totalRevenue": 45000000,
     *     "activeLearningSessionsToday": 320,
     *     "systemStatus": "OK",
     *     "lastUpdatedAt": "2024-01-15T12:34:56"
     *   }
     * }
     */
    @GetMapping("/summary")
    @Operation(
        summary = "Get dashboard summary",
        description = "Retrieve high-level system metrics for dashboard display (admin only)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Dashboard summary retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - invalid or missing JWT token"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Forbidden - user does not have ADMIN role"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Server error while fetching metrics"
        )
    })
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getDashboardSummary() {
        try {
            log.info("GET /api/admin/dashboard/summary - Retrieving dashboard summary");
            
            DashboardSummaryResponse summary = dashboardService.getDashboardSummary();
            
            log.debug("Dashboard summary retrieved: users={}, subscriptions={}",
                summary.getTotalUsers(), summary.getActiveSubscriptions());
            
            return ResponseEntity.ok(
                ApiResponse.success("Dashboard summary retrieved successfully", summary)
            );
            
        } catch (Exception e) {
            log.error("Error retrieving dashboard summary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve dashboard summary: " + e.getMessage()));
        }
    }
    
    /**
     * Get detailed revenue metrics
     * 
     * Returns revenue-related metrics:
     * - Total all-time revenue
     * - Today's revenue
     * - This month's revenue
     * - Payment statistics
     * - Most popular package
     * 
     * Endpoint: GET /api/admin/dashboard/revenue
     * Auth: Required (ADMIN role)
     * 
     * Response (200 OK):
     * {
     *   "success": true,
     *   "message": "Revenue metrics retrieved successfully",
     *   "data": {
     *     "totalRevenue": 450000000,
     *     "revenueToday": 5000000,
     *     "revenueThisMonth": 120000000,
     *     "totalPayments": 350,
     *     "paymentsToday": 12,
     *     "averagePaymentAmount": 1285714,
     *     "popularPackageId": 3,
     *     "popularPackageName": "Premium 3 Months"
     *   }
     * }
     */
    @GetMapping("/revenue")
    @Operation(
        summary = "Get revenue metrics",
        description = "Retrieve detailed revenue metrics and payment statistics (admin only)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Revenue metrics retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - invalid or missing JWT token"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Forbidden - user does not have ADMIN role"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Server error while fetching metrics"
        )
    })
    public ResponseEntity<ApiResponse<DashboardRevenueResponse>> getRevenueMetrics() {
        try {
            log.info("GET /api/admin/dashboard/revenue - Retrieving revenue metrics");
            
            DashboardRevenueResponse revenue = dashboardService.getRevenueMetrics();
            
            log.debug("Revenue metrics retrieved: total={}, today={}",
                revenue.getTotalRevenue(), revenue.getRevenueToday());
            
            return ResponseEntity.ok(
                ApiResponse.success("Revenue metrics retrieved successfully", revenue)
            );
            
        } catch (Exception e) {
            log.error("Error retrieving revenue metrics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve revenue metrics: " + e.getMessage()));
        }
    }
    
    /**
     * Get top performing students
     * 
     * Returns top students ranked by:
     * - Learning session count
     * - Words learned
     * - Accuracy rate
     * 
     * Endpoint: GET /api/admin/dashboard/top-students
     * Auth: Required (ADMIN role)
     * Query Parameters:
     * - limit: Number of top students to return (default: 5, max: 100)
     * 
     * Response (200 OK):
     * {
     *   "success": true,
     *   "message": "Top students retrieved successfully",
     *   "data": [
     *     {
     *       "userId": 42,
     *       "fullName": "Nguyễn Văn A",
     *       "email": "student@example.com",
     *       "totalSessions": 150,
     *       "wordsLearned": 1250,
     *       "accuracyRate": 87.5,
     *       "rank": 1
     *     },
     *     ...
     *   ]
     * }
     */
    @GetMapping("/top-students")
    @Operation(
        summary = "Get top performing students",
        description = "Retrieve list of top performing students ranked by learning metrics (admin only)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Top students retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - invalid or missing JWT token"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Forbidden - user does not have ADMIN role"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Server error while fetching students"
        )
    })
    public ResponseEntity<ApiResponse<List<TopStudentResponse>>> getTopStudents() {
        try {
            log.info("GET /api/admin/dashboard/top-students - Retrieving top students");
            
            List<TopStudentResponse> topStudents = dashboardService.getTopStudents(5);
            
            log.debug("Top students retrieved: count={}", topStudents.size());
            
            return ResponseEntity.ok(
                ApiResponse.success("Top students retrieved successfully", topStudents)
            );
            
        } catch (Exception e) {
            log.error("Error retrieving top students", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve top students: " + e.getMessage()));
        }
    }
    
    /**
     * Get activity trends for last 30 days
     * 
     * Returns time-series data:
     * - Daily learning sessions count
     * - Daily active users count
     * - Daily revenue
     * - Daily payment count
     * 
     * Endpoint: GET /api/admin/dashboard/activity-trend
     * Auth: Required (ADMIN role)
     * 
     * Response (200 OK):
     * {
     *   "success": true,
     *   "message": "Activity trends retrieved successfully",
     *   "data": {
     *     "dates": ["2024-01-01", "2024-01-02", ..., "2024-01-30"],
     *     "learningSessions": [120, 150, 140, ...],
     *     "activeUsers": [80, 95, 88, ...],
     *     "dailyRevenue": [5000000, 6500000, 4200000, ...],
     *     "paymentCounts": [5, 8, 6, ...],
     *     "period": "Last 30 days"
     *   }
     * }
     */
    @GetMapping("/activity-trend")
    @Operation(
        summary = "Get activity trends",
        description = "Retrieve time-series data for system activity over the last 30 days (admin only)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Activity trends retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - invalid or missing JWT token"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Forbidden - user does not have ADMIN role"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Server error while fetching trends"
        )
    })
    public ResponseEntity<ApiResponse<ActivityTrendResponse>> getActivityTrend() {
        try {
            log.info("GET /api/admin/dashboard/activity-trend - Retrieving activity trends");
            
            ActivityTrendResponse trends = dashboardService.getActivityTrend();
            
            log.debug("Activity trends retrieved: days={}", trends.getDates().size());
            
            return ResponseEntity.ok(
                ApiResponse.success("Activity trends retrieved successfully", trends)
            );
            
        } catch (Exception e) {
            log.error("Error retrieving activity trends", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve activity trends: " + e.getMessage()));
        }
    }
}
