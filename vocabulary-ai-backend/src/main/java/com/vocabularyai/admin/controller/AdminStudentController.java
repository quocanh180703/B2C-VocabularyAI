package com.vocabularyai.admin.controller;

import com.vocabularyai.admin.dto.response.StudentListResponse;
import com.vocabularyai.admin.dto.response.StudentDetailResponse;
import com.vocabularyai.admin.dto.response.StudentStatsResponse;
import com.vocabularyai.admin.service.AdminStudentService;
import com.vocabularyai.common.response.ApiResponse;
import com.vocabularyai.common.util.PaginationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin Student Controller
 * 
 * Endpoints for admin student management
 * All endpoints require ADMIN role authorization
 * 
 * Endpoints:
 * - GET /api/admin/students - List all students (paginated)
 * - GET /api/admin/students/{id} - Student details
 * - GET /api/admin/students/stats/summary - Student statistics
 */
@RestController
@RequestMapping("/admin/students")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Students", description = "Admin student management endpoints")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminStudentController {
    
    private final AdminStudentService studentService;
    
    /**
     * Get paginated list of all students
     * 
     * Supports filtering and searching:
     * - Pagination
     * - Sorting by any field
     * - Filter by status
     * - Search by email or name
     * 
     * Endpoint: GET /api/admin/students
     * Query Parameters:
     * - page: Page number (0-based, default: 0)
     * - size: Page size (default: 20)
     * - sort: Sort field and direction (e.g., "createdAt,desc")
     * - status: Filter by status (ACTIVE, INACTIVE, BANNED, optional)
     * - search: Search by email or name (optional)
     * 
     * Response (200 OK):
     * {
     *   "success": true,
     *   "message": "Students retrieved successfully",
     *   "data": {
     *     "content": [
     *       {
     *         "userId": 1,
     *         "username": "user123",
     *         "email": "user@example.com",
     *         "fullName": "Nguyễn Văn A",
     *         "status": "ACTIVE",
     *         "hasActiveSubscription": true,
     *         "totalSessions": 150,
     *         "createdAt": "2024-01-15T10:30:00"
     *       }
     *     ],
     *     "totalElements": 1250,
     *     "totalPages": 63,
     *     "currentPage": 0,
     *     "pageSize": 20
     *   }
     * }
     */
    @GetMapping
    @Operation(
        summary = "Get student list",
        description = "Retrieve paginated list of students with optional filtering and search (admin only)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Students retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - invalid or missing JWT token"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Forbidden - user does not have ADMIN role"
        )
    })
    public ResponseEntity<ApiResponse<Page<StudentListResponse>>> getStudents(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(required = false, defaultValue = "0") Integer page,
            
            @Parameter(description = "Page size", example = "20")
            @RequestParam(required = false, defaultValue = "20") Integer size,
            
            @Parameter(description = "Sort field and direction (e.g., 'createdAt,desc')", example = "createdAt,desc")
            @RequestParam(required = false, defaultValue = "createdAt,desc") String sort,
            
            @Parameter(description = "Filter by user status (ACTIVE, INACTIVE, BANNED)", example = "ACTIVE")
            @RequestParam(required = false) String status,
            
            @Parameter(description = "Search by email or name", example = "nguy")
            @RequestParam(required = false) String search) {
        
        try {
            log.info("GET /api/admin/students - Fetching student list (page={}, size={}, status={}, search={})",
                page, size, status, search);
            
            Pageable pageable = PaginationUtils.createPageable(page, size, sort);
            
            Page<StudentListResponse> result;
            
            if (search != null && !search.isBlank()) {
                // Search by email or name
                result = studentService.searchStudents(search, pageable);
                log.debug("Students searched: {} results", result.getTotalElements());
            } else if (status != null && !status.isBlank()) {
                // Filter by status
                result = studentService.getStudentListByStatus(status, pageable);
                log.debug("Students filtered by status: {} results", result.getTotalElements());
            } else {
                // Get all students
                result = studentService.getStudentList(pageable);
                log.debug("All students fetched: {} total", result.getTotalElements());
            }
            
            return ResponseEntity.ok(
                ApiResponse.success("Students retrieved successfully", result)
            );
            
        } catch (Exception e) {
            log.error("Error retrieving student list", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve students: " + e.getMessage()));
        }
    }
    
    /**
     * Get complete student information
     * 
     * Returns:
     * - User details
     * - Learning statistics
     * - Subscription information
     * - Payment history
     * 
     * Endpoint: GET /api/admin/students/{userId}
     * Path Parameters:
     * - userId: Student user ID
     * 
     * Response (200 OK):
     * {
     *   "success": true,
     *   "message": "Student detail retrieved successfully",
     *   "data": {
     *     "userId": 42,
     *     "username": "user123",
     *     "email": "user@example.com",
     *     "fullName": "Nguyễn Văn A",
     *     "status": "ACTIVE",
     *     "totalSessions": 150,
     *     "wordsLearned": 1250,
     *     "accuracyRate": 87.5,
     *     "hasActiveSubscription": true,
     *     "packageName": "Premium 3 Months",
     *     "subscriptionEndAt": "2024-04-01T00:00:00",
     *     "remainingDays": 71,
     *     "totalPaid": 1500000,
     *     "paymentCount": 3
     *   }
     * }
     */
    @GetMapping("/{userId}")
    @Operation(
        summary = "Get student detail",
        description = "Retrieve complete student information including learning and subscription stats (admin only)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Student detail retrieved successfully",
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
            responseCode = "404",
            description = "Student not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Server error while fetching student"
        )
    })
    public ResponseEntity<ApiResponse<StudentDetailResponse>> getStudentDetail(
            @Parameter(description = "Student user ID", example = "42")
            @PathVariable Long userId) {
        
        try {
            log.info("GET /api/admin/students/{} - Retrieving student detail", userId);
            
            StudentDetailResponse student = studentService.getStudentDetail(userId);
            
            log.debug("Student detail retrieved: userId={}, email={}", student.getUserId(), student.getEmail());
            
            return ResponseEntity.ok(
                ApiResponse.success("Student detail retrieved successfully", student)
            );
            
        } catch (IllegalArgumentException e) {
            log.warn("Student not found: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Student not found"));
        } catch (Exception e) {
            log.error("Error retrieving student detail", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve student detail: " + e.getMessage()));
        }
    }
    
    /**
     * Get aggregate student statistics
     * 
     * Returns system-wide student metrics:
     * - Total students by status
     * - Active subscriptions
     * - Average metrics per student
     * - Activity statistics
     * - Churn rate
     * 
     * Endpoint: GET /api/admin/students/stats/summary
     * 
     * Response (200 OK):
     * {
     *   "success": true,
     *   "message": "Student statistics retrieved successfully",
     *   "data": {
     *     "totalStudents": 1250,
     *     "activeStudents": 450,
     *     "inactiveStudents": 800,
     *     "bannedStudents": 0,
     *     "avgSessionsPerStudent": 45.7,
     *     "avgWordsPerStudent": 380,
     *     "avgAccuracyRate": 75.3,
     *     "activeSubscriptionsToday": 450,
     *     "activeTodayCount": 280,
     *     "newStudentsThisMonth": 85,
     *     "churnRate": 64.0
     *   }
     * }
     */
    @GetMapping("/stats/summary")
    @Operation(
        summary = "Get student statistics",
        description = "Retrieve aggregate statistics for all students (admin only)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Student statistics retrieved successfully",
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
            description = "Server error while fetching statistics"
        )
    })
    public ResponseEntity<ApiResponse<StudentStatsResponse>> getStudentStats() {
        try {
            log.info("GET /api/admin/students/stats/summary - Retrieving student statistics");
            
            StudentStatsResponse stats = studentService.getStudentStats();
            
            log.debug("Student statistics retrieved: total={}, active={}", 
                stats.getTotalStudents(), stats.getActiveStudents());
            
            return ResponseEntity.ok(
                ApiResponse.success("Student statistics retrieved successfully", stats)
            );
            
        } catch (Exception e) {
            log.error("Error retrieving student statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve student statistics: " + e.getMessage()));
        }
    }
}
