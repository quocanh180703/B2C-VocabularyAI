package com.vocabularyai.admin.controller;

import com.vocabularyai.admin.dto.response.SubscriptionListResponse;
import com.vocabularyai.admin.dto.response.SubscriptionDetailResponse;
import com.vocabularyai.admin.service.AdminSubscriptionService;
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
 * Admin Subscription Controller
 * 
 * Endpoints for admin subscription management (system-wide view)
 * All endpoints require ADMIN role authorization
 * 
 * Endpoints:
 * - GET /api/admin/subscriptions - List all subscriptions (paginated)
 * - GET /api/admin/subscriptions/{id} - Subscription details
 */
@RestController
@RequestMapping("/admin/subscriptions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Subscriptions", description = "Admin subscription management endpoints")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSubscriptionController {
    
    private final AdminSubscriptionService subscriptionService;
    
    /**
     * Get paginated list of all subscriptions
     * 
     * Supports filtering and searching:
     * - Pagination
     * - Sorting by any field
     * - Filter by status
     * - Filter by user
     * - Search by email or package name
     * - Filter for active subscriptions only
     * 
     * Endpoint: GET /api/admin/subscriptions
     * Query Parameters:
     * - page: Page number (0-based, default: 0)
     * - size: Page size (default: 20)
     * - sort: Sort field and direction (e.g., "createdAt,desc")
     * - status: Filter by subscription status (ACTIVE, CANCELLED, EXPIRED, optional)
     * - active: Show only active (non-expired) subscriptions (optional, default: false)
     * - userId: Filter by user ID (optional)
     * - search: Search by email or package name (optional)
     * 
     * Response (200 OK):
     * {
     *   "success": true,
     *   "message": "Subscriptions retrieved successfully",
     *   "data": {
     *     "content": [
     *       {
     *         "subscriptionId": 1,
     *         "userId": 42,
     *         "userEmail": "user@example.com",
     *         "userFullName": "Nguyễn Văn A",
     *         "packageName": "Premium 3 Months",
     *         "status": "ACTIVE",
     *         "startAt": "2024-01-15T10:30:00",
     *         "endAt": "2024-04-15T10:30:00",
     *         "remainingDays": 90,
     *         "isActive": true
     *       }
     *     ],
     *     "totalElements": 450,
     *     "totalPages": 23,
     *     "currentPage": 0,
     *     "pageSize": 20
     *   }
     * }
     */
    @GetMapping
    @Operation(
        summary = "Get subscription list",
        description = "Retrieve paginated list of all subscriptions with optional filtering and search (admin only)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Subscriptions retrieved successfully",
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
    public ResponseEntity<ApiResponse<Page<SubscriptionListResponse>>> getSubscriptions(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(required = false, defaultValue = "0") Integer page,
            
            @Parameter(description = "Page size", example = "20")
            @RequestParam(required = false, defaultValue = "20") Integer size,
            
            @Parameter(description = "Sort field and direction (e.g., 'createdAt,desc')", example = "createdAt,desc")
            @RequestParam(required = false, defaultValue = "createdAt,desc") String sort,
            
            @Parameter(description = "Filter by subscription status (ACTIVE, CANCELLED, EXPIRED)", example = "ACTIVE")
            @RequestParam(required = false) String status,
            
            @Parameter(description = "Show only active (non-expired) subscriptions", example = "false")
            @RequestParam(required = false, defaultValue = "false") boolean active,
            
            @Parameter(description = "Filter by user ID", example = "42")
            @RequestParam(required = false) Long userId,
            
            @Parameter(description = "Search by email or package name", example = "Premium")
            @RequestParam(required = false) String search) {
        
        try {
            log.info("GET /api/admin/subscriptions - Fetching subscription list (page={}, size={}, status={}, active={}, userId={}, search={})",
                page, size, status, active, userId, search);
            
            Pageable pageable = PaginationUtils.createPageable(page, size, sort);
            
            Page<SubscriptionListResponse> result;
            
            if (search != null && !search.isBlank()) {
                // Search by email or package name
                result = subscriptionService.searchSubscriptions(search, pageable);
                log.debug("Subscriptions searched: {} results", result.getTotalElements());
            } else if (userId != null) {
                // Filter by user
                result = subscriptionService.getSubscriptionListByUser(userId, pageable);
                log.debug("Subscriptions filtered by user: {} results", result.getTotalElements());
            } else if (active) {
                // Get active subscriptions
                result = subscriptionService.getActiveSubscriptions(pageable);
                log.debug("Active subscriptions fetched: {} results", result.getTotalElements());
            } else if (status != null && !status.isBlank()) {
                // Filter by status
                result = subscriptionService.getSubscriptionListByStatus(status, pageable);
                log.debug("Subscriptions filtered by status: {} results", result.getTotalElements());
            } else {
                // Get all subscriptions
                result = subscriptionService.getSubscriptionList(pageable);
                log.debug("All subscriptions fetched: {} total", result.getTotalElements());
            }
            
            return ResponseEntity.ok(
                ApiResponse.success("Subscriptions retrieved successfully", result)
            );
            
        } catch (Exception e) {
            log.error("Error retrieving subscription list", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve subscriptions: " + e.getMessage()));
        }
    }
    
    /**
     * Get complete subscription information
     * 
     * Returns:
     * - Subscription details
     * - User information
     * - Package information
     * - Payment details (if created from payment)
     * 
     * Endpoint: GET /api/admin/subscriptions/{subscriptionId}
     * Path Parameters:
     * - subscriptionId: Subscription ID
     * 
     * Response (200 OK):
     * {
     *   "success": true,
     *   "message": "Subscription detail retrieved successfully",
     *   "data": {
     *     "subscriptionId": 1,
     *     "status": "ACTIVE",
     *     "startAt": "2024-01-15T10:30:00",
     *     "endAt": "2024-04-15T10:30:00",
     *     "remainingDays": 90,
     *     "isActive": true,
     *     "userId": 42,
     *     "userEmail": "user@example.com",
     *     "userFullName": "Nguyễn Văn A",
     *     "packageName": "Premium 3 Months",
     *     "packageDurationDays": 90,
     *     "packageDescription": "Access to all premium features for 3 months",
     *     "paymentId": 456,
     *     "paymentTransactionCode": "TXN_123456789",
     *     "paymentAmount": 499000,
     *     "paymentStatus": "SUCCESS"
     *   }
     * }
     */
    @GetMapping("/{subscriptionId}")
    @Operation(
        summary = "Get subscription detail",
        description = "Retrieve complete subscription information including payment and package details (admin only)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Subscription detail retrieved successfully",
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
            description = "Subscription not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Server error while fetching subscription"
        )
    })
    public ResponseEntity<ApiResponse<SubscriptionDetailResponse>> getSubscriptionDetail(
            @Parameter(description = "Subscription ID", example = "1")
            @PathVariable Long subscriptionId) {
        
        try {
            log.info("GET /api/admin/subscriptions/{} - Retrieving subscription detail", subscriptionId);
            
            SubscriptionDetailResponse subscription = subscriptionService.getSubscriptionDetail(subscriptionId);
            
            log.debug("Subscription detail retrieved: subscriptionId={}, userId={}", 
                subscription.getSubscriptionId(), subscription.getUserId());
            
            return ResponseEntity.ok(
                ApiResponse.success("Subscription detail retrieved successfully", subscription)
            );
            
        } catch (IllegalArgumentException e) {
            log.warn("Subscription not found: {}", subscriptionId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Subscription not found"));
        } catch (Exception e) {
            log.error("Error retrieving subscription detail", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve subscription detail: " + e.getMessage()));
        }
    }
}
