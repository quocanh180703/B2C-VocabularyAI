package com.vocabularyai.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Dashboard Summary Response DTO
 * 
 * Provides high-level system metrics at a glance
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Dashboard summary with key metrics (total users, revenue, etc.)")
public class DashboardSummaryResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "Total number of registered users", example = "1250")
    private long totalUsers;
    
    @Schema(description = "Total number of active subscriptions", example = "450")
    private long activeSubscriptions;
    
    @Schema(description = "Total revenue in VND", example = "45000000")
    private long totalRevenue;
    
    @Schema(description = "Number of active learning sessions today", example = "320")
    private long activeLearningSessionsToday;
    
    @Schema(description = "System health status (OK, WARNING, CRITICAL)", example = "OK")
    private String systemStatus;
    
    @Schema(description = "Timestamp of last update", example = "2024-01-15T12:34:56")
    private String lastUpdatedAt;
}
