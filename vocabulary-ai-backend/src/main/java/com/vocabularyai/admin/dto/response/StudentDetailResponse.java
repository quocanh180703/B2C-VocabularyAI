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
 * Student Detail Response DTO
 * 
 * Complete student information with learning and subscription stats
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Complete student information with learning stats")
public class StudentDetailResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // ==================== User Information ====================
    
    @Schema(description = "User ID", example = "42")
    private Long userId;
    
    @Schema(description = "Username", example = "user123")
    private String username;
    
    @Schema(description = "Email address", example = "student@example.com")
    private String email;
    
    @Schema(description = "Full name", example = "Nguyễn Văn A")
    private String fullName;
    
    @Schema(description = "Avatar URL", example = "https://example.com/avatar.jpg")
    private String avatarUrl;
    
    @Schema(description = "User status (ACTIVE, INACTIVE, BANNED)", example = "ACTIVE")
    private String status;
    
    @Schema(description = "Account creation date", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Last update date", example = "2024-01-20T14:22:00")
    private LocalDateTime updatedAt;
    
    // ==================== Learning Statistics ====================
    
    @Schema(description = "Total learning sessions completed", example = "150")
    private long totalSessions;
    
    @Schema(description = "Total unique words learned", example = "1250")
    private long wordsLearned;
    
    @Schema(description = "Learning accuracy rate (0-100)", example = "87.5")
    private double accuracyRate;
    
    @Schema(description = "Streak (consecutive days with activity)", example = "15")
    private int currentStreak;
    
    @Schema(description = "Last activity date", example = "2024-01-20T14:22:00")
    private LocalDateTime lastActivityAt;
    
    // ==================== Subscription Information ====================
    
    @Schema(description = "Has active subscription", example = "true")
    private boolean hasActiveSubscription;
    
    @Schema(description = "Subscription ID if active", example = "789")
    private Long activeSubscriptionId;
    
    @Schema(description = "Package name if subscribed", example = "Premium 3 Months")
    private String packageName;
    
    @Schema(description = "Subscription start date", example = "2024-01-01T00:00:00")
    private LocalDateTime subscriptionStartAt;
    
    @Schema(description = "Subscription end date", example = "2024-04-01T00:00:00")
    private LocalDateTime subscriptionEndAt;
    
    @Schema(description = "Remaining days of subscription", example = "71")
    private long remainingDays;
    
    // ==================== Payment History ====================
    
    @Schema(description = "Total amount paid in VND", example = "1500000")
    private BigDecimal totalPaid;
    
    @Schema(description = "Number of successful payments", example = "3")
    private long paymentCount;
    
    @Schema(description = "Last payment date", example = "2024-01-10T12:30:00")
    private LocalDateTime lastPaymentAt;
}
