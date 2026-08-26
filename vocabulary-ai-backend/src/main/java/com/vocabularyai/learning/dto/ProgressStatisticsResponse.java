package com.vocabularyai.learning.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Progress Statistics Response DTO
 * 
 * Summary statistics about user's learning progress
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProgressStatisticsResponse {
    
    private Long totalWords;
    private Long newWords;
    private Long learningWords;
    private Long masteredWords;
    private Long wordsNeedingReview;
    private Double averageAccuracy;
    private Long totalActivities;
    private Long correctAnswers;
    private Double overallAccuracyRate;
    
    /**
     * Calculate learning percentage
     */
    public Double getLearningPercentage() {
        if (totalWords == 0) {
            return 0.0;
        }
        return ((learningWords + masteredWords) * 100.0) / totalWords;
    }
    
    /**
     * Calculate mastery percentage
     */
    public Double getMasteryPercentage() {
        if (totalWords == 0) {
            return 0.0;
        }
        return (masteredWords * 100.0) / totalWords;
    }
}
