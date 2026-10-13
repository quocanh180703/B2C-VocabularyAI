package com.vocabularyai.payment.controller;

import com.vocabularyai.auth.config.JwtProperties;
import com.vocabularyai.auth.security.JwtProvider;
import com.vocabularyai.common.response.ApiResponse;
import com.vocabularyai.payment.dto.payos.PayOSCreatePaymentLinkResponse;
import com.vocabularyai.payment.dto.payos.PayOSWebhookRequest;
import com.vocabularyai.payment.exception.DuplicatePaymentException;
import com.vocabularyai.payment.exception.InvalidSignatureException;
import com.vocabularyai.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Payment Controller - REST Endpoints for Payment Operations
 * 
 * Endpoints:
 * - POST /api/payments/initiate - Create payment (requires JWT auth)
 * - POST /api/payments/webhook - PayOS webhook callback (no auth required, signature verified)
 * - GET /api/payments/{orderId} - Get payment order status (requires JWT auth)
 * - GET /api/subscriptions/status - Get user's subscription status (requires JWT auth)
 * 
 * Security:
 * - Protected endpoints: Require valid JWT token (@PreAuthorize("isAuthenticated()"))
 * - Webhook endpoint: No JWT required, validated via HMAC-SHA256 signature
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "Payment and subscription endpoints")
public class PaymentController {
    
    private final PaymentService paymentService;
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    
    /**
     * Initiate a payment for a subscription package
     * 
     * Endpoint: POST /api/payments/initiate
     * Auth: Required (JWT token)
     * 
     * Request:
     * {
     *   "packageId": 1,
     *   "returnUrl": "https://app.example.com/payment/success",
     *   "cancelUrl": "https://app.example.com/payment/cancel"
     * }
     * 
     * Response (200 OK):
     * {
     *   "success": true,
     *   "data": {
     *     "code": "00",
     *     "desc": "success",
     *     "data": {
     *       "checkoutUrl": "https://checkout.payos.vn/...",
     *       "transactionCode": "TXN_...",
     *       "qrCode": "...",
     *       ...
     *     }
     *   }
     * }
     */
    @PostMapping("/initiate")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Initiate payment",
        description = "Create a payment link for a subscription package. User will be redirected to checkout URL.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Payment link created successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid package ID or user already has active subscription"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - invalid or missing JWT token"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Server error or payment gateway error"
        )
    })
    public ResponseEntity<ApiResponse<PayOSCreatePaymentLinkResponse>> initiatePayment(
            @Parameter(description = "Package ID to purchase", example = "1")
            @RequestParam Long packageId) {
        
        try {
            log.info("Payment initiation request received for packageId: {}", packageId);
            
            // Extract user ID from JWT token
            Long userId = extractUserIdFromJwt();
            
            // Call payment service
            // Return/Cancel URLs are configured in backend, not provided by client
            PayOSCreatePaymentLinkResponse response = paymentService.initiatePayment(
                userId,
                packageId
            );
            
            log.info("Payment initiated successfully for userId: {}, packageId: {}", userId, packageId);
            
            return ResponseEntity.ok(ApiResponse.success("Payment link created successfully", response));
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid payment initiation request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            log.warn("Payment initiation failed due to business rule: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Payment initiation failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Payment initiation failed: " + e.getMessage()));
        }
    }
    
    /**
     * PayOS Webhook Callback
     * 
     * Endpoint: POST /api/payments/webhook
     * Auth: None (signature verification only)
     * 
     * PayOS calls this endpoint when payment status changes
     * 
     * Headers:
     * - x-payos-signature: HMAC-SHA256 signature
     * 
     * Request Body: PayOSWebhookRequest with payment data
     * 
     * Response (200 OK): { "success": true }
     * Response (400 Bad Request): { "success": false, "error": "..." }
     */
    @PostMapping("/webhook")
    @Operation(
        summary = "PayOS Webhook Callback",
        description = "Receives webhook notifications from PayOS when payment status changes. " +
                      "Signature is verified to ensure authenticity. No JWT authentication required."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Webhook processed successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid webhook signature or data"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Server error while processing webhook"
        )
    })
    public ResponseEntity<ApiResponse<?>> handleWebhook(
            @RequestBody PayOSWebhookRequest webhook,
            
            @Parameter(description = "Webhook signature from PayOS (HMAC-SHA256)")
            @RequestHeader(value = "x-payos-signature", required = false) String signature) {
        
        try {
            log.info("Webhook received for orderCode: {}", webhook.getData().getOrderCode());
            
            // Validate signature
            if (signature == null || signature.isEmpty()) {
                log.warn("Webhook missing signature");
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Missing webhook signature"));
            }
            
            // Process webhook
            paymentService.handlePaymentWebhook(webhook, signature);
            
            log.info("Webhook processed successfully for orderCode: {}", webhook.getData().getOrderCode());
            
            return ResponseEntity.ok(ApiResponse.success("Webhook processed successfully"));
            
        } catch (InvalidSignatureException e) {
            log.warn("Webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Invalid webhook signature"));
        } catch (DuplicatePaymentException e) {
            log.info("Duplicate webhook received (idempotency): {}", e.getMessage());
            // Return success even for duplicates (idempotent)
            return ResponseEntity.ok(ApiResponse.success("Webhook processed (duplicate)"));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid webhook data: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Invalid webhook data: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Webhook processing failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Webhook processing failed: " + e.getMessage()));
        }
    }
    
    /**
     * Get Payment Order Status
     * 
     * Endpoint: GET /api/payments/{orderId}
     * Auth: Required (JWT token)
     * 
     * Response:
     * {
     *   "success": true,
     *   "data": {
     *     "id": 1,
     *     "userId": 10,
     *     "packageId": 1,
     *     "orderCode": "ORD_1694000001_12345",
     *     "amount": 99000,
     *     "status": "PENDING|PAID|FAILED|CANCELLED",
     *     "paymentUrl": "...",
     *     "paidAt": "2024-01-01T12:00:00",
     *     "createdAt": "2024-01-01T11:45:00"
     *   }
     * }
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get payment order status",
        description = "Retrieve details and status of a specific payment order. User can only access their own payment orders.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Payment order details retrieved"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Forbidden - payment order does not belong to authenticated user"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Payment order not found"
        )
    })
    public ResponseEntity<ApiResponse<?>> getPaymentOrder(
            @Parameter(description = "Payment Order ID", example = "1")
            @PathVariable Long orderId) {
        
        try {
            log.info("Getting payment order: {}", orderId);
            
            // Extract user ID from JWT token
            Long userId = extractUserIdFromJwt();
            
            var paymentOrder = paymentService.getPaymentOrder(orderId);
            
            // Ownership validation - user can only access their own payment orders
            if (!paymentOrder.getUserId().equals(userId)) {
                log.warn("Unauthorized access attempt: user {} tried to access payment order {} of user {}", 
                    userId, orderId, paymentOrder.getUserId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: This payment order does not belong to you"));
            }
            
            return ResponseEntity.ok(ApiResponse.success("Payment order retrieved successfully", paymentOrder));
            
        } catch (IllegalArgumentException e) {
            log.warn("Payment order not found: {}", orderId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Payment order not found"));
        } catch (Exception e) {
            log.error("Failed to get payment order: {}", orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve payment order"));
        }
    }
    
    /**
     * Get User's Subscription Status
     * 
     * Endpoint: GET /api/subscriptions/status
     * Auth: Required (JWT token)
     * 
     * Response (if active subscription):
     * {
     *   "success": true,
     *   "data": {
     *     "subscriptionId": 1,
     *     "userId": 10,
     *     "packageName": "Premium Monthly",
     *     "status": "ACTIVE",
     *     "remainingDays": 25,
     *     "endDate": "2024-01-26T11:45:00"
     *   }
     * }
     * 
     * Response (if no subscription):
     * {
     *   "success": true,
     *   "data": null
     * }
     */
    @GetMapping("/subscriptions/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get subscription status",
        description = "Get current subscription status for authenticated user",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Subscription status retrieved"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized"
        )
    })
    public ResponseEntity<ApiResponse<?>> getSubscriptionStatus() {
        
        try {
            Long userId = extractUserIdFromJwt();
            
            log.info("Getting subscription status for userId: {}", userId);
            
            var subscriptionStatus = paymentService.getSubscriptionStatus(userId);
            
            return ResponseEntity.ok(ApiResponse.success("Subscription status retrieved successfully", subscriptionStatus));
            
        } catch (Exception e) {
            log.error("Failed to get subscription status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve subscription status"));
        }
    }
    
    /**
     * Get Available Packages
     * 
     * Endpoint: GET /api/payments/packages
     * Auth: Optional (can be called by anonymous users)
     * 
     * Returns list of all active subscription packages
     * 
     * Response (200 OK):
     * {
     *   "success": true,
     *   "data": [
     *     {
     *       "id": 1,
     *       "code": "PREMIUM_MONTHLY",
     *       "name": "Premium Monthly",
     *       "price": 99000,
     *       "durationDays": 30,
     *       "description": "..."
     *     },
     *     {
     *       "id": 2,
     *       "code": "PREMIUM_YEARLY",
     *       "name": "Premium Yearly",
     *       "price": 899000,
     *       "durationDays": 365,
     *       "description": "..."
     *     }
     *   ]
     * }
     */
    @GetMapping("/packages")
    @Operation(
        summary = "Get available packages",
        description = "Retrieve list of all active subscription packages"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Packages retrieved successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Server error"
        )
    })
    public ResponseEntity<ApiResponse<java.util.List<?>>> getPackages() {
        try {
            log.info("Getting available packages");
            
            var packages = paymentService.getActivePackages();
            
            log.info("Retrieved {} packages", packages.size());
            
            return ResponseEntity.ok(ApiResponse.success("Packages retrieved successfully", packages));
            
        } catch (Exception e) {
            log.error("Failed to get packages", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve packages"));
        }
    }
    
    /**
     * Cancel Subscription
     * 
     * Endpoint: POST /api/subscriptions/cancel
     * Auth: Required (JWT token)
     * 
     * Request:
     * {
     *   "subscriptionId": 1
     * }
     * 
     * Response (200 OK):
     * {
     *   "success": true,
     *   "data": {
     *     "subscriptionId": 1,
     *     "status": "CANCELLED",
     *     "cancelledAt": "2024-01-15T12:00:00"
     *   }
     * }
     */
    @PostMapping("/subscriptions/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Cancel subscription",
        description = "Cancel user's active subscription",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Subscription cancelled successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid subscription or user not authorized"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Subscription not found"
        )
    })
    public ResponseEntity<ApiResponse<?>> cancelSubscription(
            @RequestBody CancelSubscriptionRequest request) {
        
        try {
            log.info("Cancel subscription request: subscriptionId={}", request.getSubscriptionId());
            
            Long userId = extractUserIdFromJwt();
            
            // Verify user owns this subscription
            var subscription = paymentService.getSubscription(request.getSubscriptionId());
            if (!subscription.getUserId().equals(userId)) {
                log.warn("Unauthorized cancellation attempt: user {} tried to cancel subscription {} of user {}", 
                    userId, request.getSubscriptionId(), subscription.getUserId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Not authorized to cancel this subscription"));
            }
            
            paymentService.cancelSubscription(request.getSubscriptionId());
            
            log.info("Subscription cancelled successfully: {}", request.getSubscriptionId());
            
            return ResponseEntity.ok(ApiResponse.success("Subscription cancelled successfully", 
                Map.of(
                    "subscriptionId", request.getSubscriptionId(),
                    "status", "CANCELLED",
                    "cancelledAt", java.time.LocalDateTime.now()
                )));
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid cancellation request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to cancel subscription", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to cancel subscription"));
        }
    }
    
    /**
     * Get Payment History
     * 
     * Endpoint: GET /api/payments/history?page=0&size=10
     * Auth: Required (JWT token)
     * 
     * Returns paginated list of user's payment orders
     * 
     * Response (200 OK):
     * {
     *   "success": true,
     *   "data": {
     *     "content": [
     *       {
     *         "id": 1,
     *         "orderCode": "ORD_1694000001_12345",
     *         "amount": 99000,
     *         "status": "PAID",
     *         "paidAt": "2024-01-01T12:00:00",
     *         "packageName": "Premium Monthly",
     *         "transactionCode": "TXN_..."
     *       }
     *     ],
     *     "totalElements": 1,
     *     "totalPages": 1,
     *     "currentPage": 0,
     *     "pageSize": 10
     *   }
     * }
     */
    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get payment history",
        description = "Retrieve paginated payment order history for authenticated user",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Payment history retrieved successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized"
        )
    })
    public ResponseEntity<ApiResponse<?>> getPaymentHistory(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Long userId = extractUserIdFromJwt();
            
            log.info("Getting payment history for userId: {}, page: {}, size: {}", userId, page, size);
            
            var paymentHistory = paymentService.getPaymentHistory(userId, page, size);
            
            log.info("Retrieved payment orders for user: {}", userId);
            
            return ResponseEntity.ok(ApiResponse.success("Payment history retrieved successfully", paymentHistory));
            
        } catch (Exception e) {
            log.error("Failed to get payment history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve payment history"));
        }
    }
    
    /**
     * Extract user ID from JWT token
     * 
     * @return User ID from JWT
     * @throws IllegalStateException if no authentication found
     */
    private Long extractUserIdFromJwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }
        
        // Principal is the JWT token string
        Object principal = auth.getPrincipal();
        
        if (principal instanceof String) {
            String token = (String) principal;
            Long userId = jwtProvider.getUserId(token, jwtProperties.getAccessToken().getSecret());
            
            if (userId == null) {
                throw new IllegalStateException("User ID not found in JWT");
            }
            
            return userId;
        }
        
        throw new IllegalStateException("Cannot extract user ID from authentication");
    }
}

/**
 * DTO for cancel subscription request
 */
@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
class CancelSubscriptionRequest {
    private Long subscriptionId;
}
