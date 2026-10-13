package com.vocabularyai.admin.controller;

import com.vocabularyai.admin.dto.response.PaymentListResponse;
import com.vocabularyai.admin.dto.response.PaymentDetailResponse;
import com.vocabularyai.admin.service.AdminPaymentService;
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
 * Admin Payment Controller
 * 
 * Endpoints for admin payment management (system-wide view)
 * All endpoints require ADMIN role authorization
 * 
 * Endpoints:
 * - GET /api/admin/payments - List all payments (paginated)
 * - GET /api/admin/payments/{id} - Payment details
 */
@RestController
@RequestMapping("/admin/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Payments", description = "Admin payment management endpoints")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPaymentController {
    
    private final AdminPaymentService paymentService;
    
    /**
     * Get paginated list of all payments
     * 
     * Supports filtering and searching:
     * - Pagination
     * - Sorting by any field
     * - Filter by status
     * - Filter by user
     * - Search by transaction or order code
     * 
     * Endpoint: GET /api/admin/payments
     * Query Parameters:
     * - page: Page number (0-based, default: 0)
     * - size: Page size (default: 20)
     * - sort: Sort field and direction (e.g., "paidAt,desc")
     * - status: Filter by payment status (SUCCESS, PENDING, CANCELLED, FAILED, optional)
     * - userId: Filter by user ID (optional)
     * - search: Search by transaction or order code (optional)
     * 
     * Response (200 OK):
     * {
     *   "success": true,
     *   "message": "Payments retrieved successfully",
     *   "data": {
     *     "content": [
     *       {
     *         "paymentId": 1,
     *         "transactionCode": "TXN_123456789",
     *         "orderCode": "ORD_...",
     *         "userId": 42,
     *         "userEmail": "user@example.com",
     *         "userFullName": "Nguyễn Văn A",
     *         "packageName": "Premium 3 Months",
     *         "amount": 499000,
     *         "status": "SUCCESS",
     *         "paymentMethod": "ATM",
     *         "paidAt": "2024-01-15T10:30:00"
     *       }
     *     ],
     *     "totalElements": 350,
     *     "totalPages": 18,
     *     "currentPage": 0,
     *     "pageSize": 20
     *   }
     * }
     */
    @GetMapping
    @Operation(
        summary = "Get payment list",
        description = "Retrieve paginated list of all payments with optional filtering and search (admin only)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Payments retrieved successfully",
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
    public ResponseEntity<ApiResponse<Page<PaymentListResponse>>> getPayments(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(required = false, defaultValue = "0") Integer page,
            
            @Parameter(description = "Page size", example = "20")
            @RequestParam(required = false, defaultValue = "20") Integer size,
            
            @Parameter(description = "Sort field and direction (e.g., 'paidAt,desc')", example = "paidAt,desc")
            @RequestParam(required = false, defaultValue = "paidAt,desc") String sort,
            
            @Parameter(description = "Filter by payment status (SUCCESS, PENDING, CANCELLED, FAILED)", example = "SUCCESS")
            @RequestParam(required = false) String status,
            
            @Parameter(description = "Filter by user ID", example = "42")
            @RequestParam(required = false) Long userId,
            
            @Parameter(description = "Search by transaction or order code", example = "TXN_")
            @RequestParam(required = false) String search) {
        
        try {
            log.info("GET /api/admin/payments - Fetching payment list (page={}, size={}, status={}, userId={}, search={})",
                page, size, status, userId, search);
            
            Pageable pageable = PaginationUtils.createPageable(page, size, sort);
            
            Page<PaymentListResponse> result;
            
            if (search != null && !search.isBlank()) {
                // Search by transaction or order code
                result = paymentService.searchPayments(search, pageable);
                log.debug("Payments searched: {} results", result.getTotalElements());
            } else if (userId != null) {
                // Filter by user
                result = paymentService.getPaymentListByUser(userId, pageable);
                log.debug("Payments filtered by user: {} results", result.getTotalElements());
            } else if (status != null && !status.isBlank()) {
                // Filter by status
                result = paymentService.getPaymentListByStatus(status, pageable);
                log.debug("Payments filtered by status: {} results", result.getTotalElements());
            } else {
                // Get all payments
                result = paymentService.getPaymentList(pageable);
                log.debug("All payments fetched: {} total", result.getTotalElements());
            }
            
            return ResponseEntity.ok(
                ApiResponse.success("Payments retrieved successfully", result)
            );
            
        } catch (Exception e) {
            log.error("Error retrieving payment list", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve payments: " + e.getMessage()));
        }
    }
    
    /**
     * Get complete payment information
     * 
     * Returns:
     * - Payment details
     * - Order information
     * - User details
     * - Package information
     * - Subscription details (if created)
     * 
     * Endpoint: GET /api/admin/payments/{paymentId}
     * Path Parameters:
     * - paymentId: Payment ID
     * 
     * Response (200 OK):
     * {
     *   "success": true,
     *   "message": "Payment detail retrieved successfully",
     *   "data": {
     *     "paymentId": 1,
     *     "transactionCode": "TXN_123456789",
     *     "status": "SUCCESS",
     *     "amount": 499000,
     *     "paymentMethod": "ATM",
     *     "paidAt": "2024-01-15T10:30:00",
     *     "orderId": 789,
     *     "orderCode": "ORD_...",
     *     "orderStatus": "PAID",
     *     "userId": 42,
     *     "userEmail": "user@example.com",
     *     "userFullName": "Nguyễn Văn A",
     *     "packageName": "Premium 3 Months",
     *     "packageDurationDays": 90,
     *     "subscriptionId": 456,
     *     "subscriptionStatus": "ACTIVE",
     *     "subscriptionEndAt": "2024-04-15T10:30:00",
     *     "remainingDays": 90
     *   }
     * }
     */
    @GetMapping("/{paymentId}")
    @Operation(
        summary = "Get payment detail",
        description = "Retrieve complete payment information including order and subscription details (admin only)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Payment detail retrieved successfully",
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
            description = "Payment not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Server error while fetching payment"
        )
    })
    public ResponseEntity<ApiResponse<PaymentDetailResponse>> getPaymentDetail(
            @Parameter(description = "Payment ID", example = "1")
            @PathVariable Long paymentId) {
        
        try {
            log.info("GET /api/admin/payments/{} - Retrieving payment detail", paymentId);
            
            PaymentDetailResponse payment = paymentService.getPaymentDetail(paymentId);
            
            log.debug("Payment detail retrieved: paymentId={}, transactionCode={}", 
                payment.getPaymentId(), payment.getTransactionCode());
            
            return ResponseEntity.ok(
                ApiResponse.success("Payment detail retrieved successfully", payment)
            );
            
        } catch (IllegalArgumentException e) {
            log.warn("Payment not found: {}", paymentId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Payment not found"));
        } catch (Exception e) {
            log.error("Error retrieving payment detail", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve payment detail: " + e.getMessage()));
        }
    }
}
