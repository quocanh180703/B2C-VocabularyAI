package com.vocabularyai.speaking.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * SpeakingProgressResponse
 * 
 * Response DTO cho speaking progress
 * 
 * Chứa tổng hợp thông tin:
 * - Speaking statistics (total sessions, scores)
 * - Progress trends (improvements, streaks)
 * - Achievements (milestones)
 * - Recent performance
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeakingProgressResponse {
    
    /**
     * Overall statistics
     */
    @JsonProperty("statistics")
    private StatisticsData statistics;
    
    /**
     * Score breakdown (averages)
     */
    @JsonProperty("averageScores")
    private ScoresData averageScores;
    
    /**
     * Best performances
     */
    @JsonProperty("bestScores")
    private ScoresData bestScores;
    
    /**
     * Recent performance
     */
    @JsonProperty("recentPerformance")
    private RecentPerformanceData recentPerformance;
    
    /**
     * Streak and consistency
     */
    @JsonProperty("streaks")
    private StreakData streaks;
    
    /**
     * Achievements and milestones
     */
    @JsonProperty("achievements")
    private List<String> achievements;
    
    /**
     * Score trend (improving, stable, declining)
     */
    @JsonProperty("scoreTrend")
    private String scoreTrend;
    
    /**
     * Last update time
     */
    @JsonProperty("lastUpdateAt")
    private LocalDateTime lastUpdateAt;
    
    /**
     * Statistics Data
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatisticsData {
        @JsonProperty("totalSessions")
        private Long totalSessions;
        
        @JsonProperty("completedSessions")
        private Long completedSessions;
        
        @JsonProperty("totalAttempts")
        private Long totalAttempts;
        
        @JsonProperty("totalTimeSpent")
        private Long totalTimeSpentSeconds;
        
        @JsonProperty("averageSessionDuration")
        private Long averageSessionDurationSeconds;
    }
    
    /**
     * Scores Data (average or best)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScoresData {
        @JsonProperty("overallScore")
        private Double overallScore;
        
        @JsonProperty("grammarScore")
        private Double grammarScore;
        
        @JsonProperty("vocabularyScore")
        private Double vocabularyScore;
        
        @JsonProperty("fluencyScore")
        private Double fluencyScore;
        
        @JsonProperty("contentScore")
        private Double contentScore;
        
        @JsonProperty("pronunciationScore")
        private Double pronunciationScore;
    }
    
    /**
     * Recent Performance Data
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentPerformanceData {
        @JsonProperty("lastSessionDate")
        private LocalDateTime lastSessionDate;
        
        @JsonProperty("lastSessionScore")
        private Double lastSessionScore;
        
        @JsonProperty("thisWeekAverageScore")
        private Double thisWeekAverageScore;
        
        @JsonProperty("thisMonthAverageScore")
        private Double thisMonthAverageScore;
        
        @JsonProperty("improvement7Days")
        private Double improvement7Days;
        
        @JsonProperty("improvement30Days")
        private Double improvement30Days;
    }
    
    /**
     * Streak Data
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StreakData {
        @JsonProperty("currentStreak")
        private Integer currentStreak;
        
        @JsonProperty("longestStreak")
        private Integer longestStreak;
        
        @JsonProperty("lastPracticeDate")
        private LocalDateTime lastPracticeDate;
    }
}
