package com.vocabularyai.payment.controller;

import com.vocabularyai.auth.config.JwtProperties;
import com.vocabularyai.auth.security.JwtProvider;
import com.vocabularyai.common.response.ApiResponse;
import com.vocabularyai.payment.dto.CancelSubscriptionRequest;
import com.vocabularyai.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Subscription Controller - REST Endpoints for User Subscriptions
 * 
 * Endpoints:
 * - GET /api/subscriptions/status - Get user's subscription status
 * - POST /api/subscriptions/cancel - Cancel active subscription
 */
@RestController
@RequestMapping({"/subscriptions", "/api/subscriptions"})
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Subscriptions", description = "User subscription management endpoints")
public class SubscriptionController {

    private final PaymentService paymentService;
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    private final UserRepository userRepository;

    @GetMapping("/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get subscription status",
        description = "Get current subscription status for authenticated user",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
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

    @PostMapping("/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Cancel subscription",
        description = "Cancel user's active subscription",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    public ResponseEntity<ApiResponse<?>> cancelSubscription(
            @RequestBody CancelSubscriptionRequest request) {
        try {
            log.info("Cancel subscription request: subscriptionId={}", request.getSubscriptionId());
            Long userId = extractUserIdFromJwt();

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
                    "cancelledAt", LocalDateTime.now()
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

    private Long extractUserIdFromJwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }
        Object principal = auth.getPrincipal();
        
        // 1. Direct User entity set by JwtAuthenticationFilter
        if (principal instanceof User user && user.getId() != null) {
            return user.getId();
        }
        
        // 2. Spring Security UserDetails
        if (principal instanceof UserDetails userDetails) {
            return userRepository.findByUsername(userDetails.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new IllegalStateException("User not found: " + userDetails.getUsername()));
        }
        
        // 3. String principal (either raw JWT token string or username)
        if (principal instanceof String str) {
            try {
                Long userId = jwtProvider.getUserId(str, jwtProperties.getAccessToken().getSecret());
                if (userId != null) {
                    return userId;
                }
            } catch (Exception ignored) {
                // Not a JWT token string, attempt username lookup
            }
            
            return userRepository.findByUsername(str)
                .map(User::getId)
                .orElseThrow(() -> new IllegalStateException("User not found: " + str));
        }
        
        // 4. Fallback to auth.getName()
        if (auth.getName() != null && !auth.getName().isBlank()) {
            return userRepository.findByUsername(auth.getName())
                .map(User::getId)
                .orElseThrow(() -> new IllegalStateException("User not found: " + auth.getName()));
        }
        
        throw new IllegalStateException("Cannot extract user ID from authentication");
    }
}
