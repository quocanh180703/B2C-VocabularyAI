package com.vocabularyai.speaking.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ScoreTrendResponse - Trend analysis cho score speaking
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Score trend analysis response")
public class ScoreTrendResponse {
    
    @Schema(description = "Number of days analyzed", example = "7")
    private Integer days;
    
    @Schema(description = "Score trend direction (Improving/Stable/Declining)", example = "Improving")
    private String scoreTrend;
    
    @Schema(description = "Improvement percentage compared to previous period", example = "5.2")
    private Double improvementPercentage;
    
    @Schema(description = "Average score in this period", example = "82.5")
    private Double averageScore;
    
    @Schema(description = "Timestamp when analysis was performed")
    private LocalDateTime analyzedAt;
}
