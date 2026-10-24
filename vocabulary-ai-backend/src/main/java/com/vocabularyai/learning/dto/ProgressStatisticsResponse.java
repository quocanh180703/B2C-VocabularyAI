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
    
    // Real Streak & Gamification fields
    private Integer currentStreak;
    private Integer longestStreak;
    private Boolean todayCompleted;
    private Long todayActivitiesCount;
    private Integer dailyGoal;
    private Long totalPoints;
    private java.util.List<DailyStreakStatus> weekProgress;
    private java.util.List<RecentActivitySummary> recentActivities;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyStreakStatus {
        private String dayName;
        private String date;
        private Boolean completed;
        private Boolean isToday;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentActivitySummary {
        private String type; // VOCABULARY, QUIZ, SPEAKING
        private String title;
        private String result;
        private String timeAgo;
        private java.time.LocalDateTime createdAt;
    }
    
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
