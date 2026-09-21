package com.vocabularyai.speaking.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ProgressSummaryResponse - Summary ngắn gọn tiến độ speaking cho dashboard
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Summary of user's speaking progress for dashboard display")
public class ProgressSummaryResponse {
    
    @Schema(description = "Total number of speaking sessions started", example = "15")
    private Long totalSessions;
    
    @Schema(description = "Number of completed sessions", example = "14")
    private Long completedSessions;
    
    @Schema(description = "Total number of attempts across all sessions", example = "42")
    private Long totalAttempts;
    
    @Schema(description = "Average score across all attempts", example = "82.3")
    private Double averageScore;
    
    @Schema(description = "Best score achieved", example = "95.8")
    private Double bestScore;
    
    @Schema(description = "Current practice streak (days)", example = "5")
    private Integer currentStreak;
    
    @Schema(description = "Score trend direction (Improving/Stable/Declining)", example = "Improving")
    private String scoreTrend;
    
    @Schema(description = "Next milestone description", example = "Keep practicing!")
    private String nextMilestone;
}
