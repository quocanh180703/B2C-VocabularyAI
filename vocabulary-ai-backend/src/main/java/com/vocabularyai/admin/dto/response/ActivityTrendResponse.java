package com.vocabularyai.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Activity Trend Response DTO
 * 
 * Provides time-series data for system activity (users, sessions, payments)
 * Last 30 days of activity aggregated daily
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Activity trends for the last 30 days")
public class ActivityTrendResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "Date labels for X-axis (YYYY-MM-DD format)")
    private List<String> dates;
    
    @Schema(description = "Daily learning sessions for each date")
    private List<Long> learningSessions;
    
    @Schema(description = "Daily active users for each date")
    private List<Long> activeUsers;
    
    @Schema(description = "Daily revenue in VND for each date")
    private List<Long> dailyRevenue;
    
    @Schema(description = "Daily payment count for each date")
    private List<Long> paymentCounts;
    
    @Schema(description = "Period description", example = "Last 30 days")
    private String period;
}
