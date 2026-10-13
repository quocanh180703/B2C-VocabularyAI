package com.vocabularyai.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Subscription List Item Response DTO
 * 
 * Used for paginated list of subscriptions (admin system-wide view)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Subscription information for list view")
public class SubscriptionListResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "Subscription ID", example = "1")
    private Long subscriptionId;
    
    @Schema(description = "User ID", example = "42")
    private Long userId;
    
    @Schema(description = "User email", example = "user@example.com")
    private String userEmail;
    
    @Schema(description = "User full name", example = "Nguyễn Văn A")
    private String userFullName;
    
    @Schema(description = "Package name", example = "Premium 3 Months")
    private String packageName;
    
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
}
