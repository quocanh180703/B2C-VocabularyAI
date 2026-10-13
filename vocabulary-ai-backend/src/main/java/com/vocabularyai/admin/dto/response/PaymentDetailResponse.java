package com.vocabularyai.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment Detail Response DTO
 * 
 * Complete payment information with related order and subscription details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Complete payment information with order and subscription details")
public class PaymentDetailResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // ==================== Payment Information ====================
    
    @Schema(description = "Payment ID", example = "1")
    private Long paymentId;
    
    @Schema(description = "Transaction code from payment gateway", example = "TXN_123456789")
    private String transactionCode;
    
    @Schema(description = "Payment status (SUCCESS, PENDING, CANCELLED, FAILED)", example = "SUCCESS")
    private String status;
    
    @Schema(description = "Payment amount in VND", example = "499000")
    private BigDecimal amount;
    
    @Schema(description = "Payment method (ATM, CREDIT, EWALLET, QRCODE)", example = "ATM")
    private String paymentMethod;
    
    @Schema(description = "Payment date", example = "2024-01-15T10:30:00")
    private LocalDateTime paidAt;
    
    @Schema(description = "Payment creation date", example = "2024-01-15T10:25:00")
    private LocalDateTime createdAt;
    
    // ==================== Order Information ====================
    
    @Schema(description = "Payment Order ID", example = "789")
    private Long orderId;
    
    @Schema(description = "Order code", example = "ORD_1234567890123_12345")
    private String orderCode;
    
    @Schema(description = "Order status (PENDING, PAID, EXPIRED, CANCELLED)", example = "PAID")
    private String orderStatus;
    
    @Schema(description = "Order expiration date", example = "2024-01-15T10:40:00")
    private LocalDateTime orderExpiredAt;
    
    // ==================== User Information ====================
    
    @Schema(description = "User ID", example = "42")
    private Long userId;
    
    @Schema(description = "Username", example = "user123")
    private String username;
    
    @Schema(description = "Email address", example = "user@example.com")
    private String userEmail;
    
    @Schema(description = "Full name", example = "Nguyễn Văn A")
    private String userFullName;
    
    // ==================== Package Information ====================
    
    @Schema(description = "Package ID", example = "3")
    private Long packageId;
    
    @Schema(description = "Package name", example = "Premium 3 Months")
    private String packageName;
    
    @Schema(description = "Package code", example = "PREMIUM_3M")
    private String packageCode;
    
    @Schema(description = "Package price", example = "499000")
    private BigDecimal packagePrice;
    
    @Schema(description = "Package duration in days", example = "90")
    private int packageDurationDays;
    
    // ==================== Subscription Information ====================
    
    @Schema(description = "Subscription ID if created", example = "456")
    private Long subscriptionId;
    
    @Schema(description = "Subscription status", example = "ACTIVE")
    private String subscriptionStatus;
    
    @Schema(description = "Subscription start date", example = "2024-01-15T10:30:00")
    private LocalDateTime subscriptionStartAt;
    
    @Schema(description = "Subscription end date", example = "2024-04-15T10:30:00")
    private LocalDateTime subscriptionEndAt;
    
    @Schema(description = "Remaining days in subscription", example = "90")
    private long remainingDays;
}
