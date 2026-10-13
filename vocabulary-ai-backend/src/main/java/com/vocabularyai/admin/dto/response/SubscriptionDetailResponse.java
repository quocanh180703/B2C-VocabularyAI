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
 * Subscription Detail Response DTO
 * 
 * Complete subscription information with payment and package details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Complete subscription information with payment and package details")
public class SubscriptionDetailResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // ==================== Subscription Information ====================
    
    @Schema(description = "Subscription ID", example = "1")
    private Long subscriptionId;
    
    @Schema(description = "Subscription status (ACTIVE, CANCELLED, EXPIRED)", example = "ACTIVE")
    private String status;
    
    @Schema(description = "Subscription start date", example = "2024-01-15T10:30:00")
    private LocalDateTime startAt;
    
    @Schema(description = "Subscription end date", example = "2024-04-15T10:30:00")
    private LocalDateTime endAt;
    
    @Schema(description = "Remaining days", example = "90")
    private long remainingDays;
    
    @Schema(description = "Is still active (not expired)", example = "true")
    private boolean isActive;
    
    @Schema(description = "Creation date", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
    
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
    
    @Schema(description = "Package description", example = "Access to all premium features for 3 months")
    private String packageDescription;
    
    // ==================== Payment Information ====================
    
    @Schema(description = "Payment ID that created this subscription", example = "456")
    private Long paymentId;
    
    @Schema(description = "Payment transaction code", example = "TXN_123456789")
    private String paymentTransactionCode;
    
    @Schema(description = "Payment amount", example = "499000")
    private BigDecimal paymentAmount;
    
    @Schema(description = "Payment status", example = "SUCCESS")
    private String paymentStatus;
    
    @Schema(description = "Payment date", example = "2024-01-15T10:30:00")
    private LocalDateTime paymentPaidAt;
}
