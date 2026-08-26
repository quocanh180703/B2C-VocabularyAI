package com.vocabularyai.personalization.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * LearningProfileDto
 * 
 * Aggregated user learning profile combining data from:
 * - Vocabulary learning (Phase 4)
 * - Grammar learning (Phase 5)
 * - Quiz/Learning Progress (Phase 3)
 * - Speaking practice (Phase 8)
 * - AI Chat interactions (Phase 7)
 * 
 * Used as input for AI analysis (Task 2) and recommendations (Task 3)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningProfileDto {
    
    /**
     * User ID
     */
    @JsonProperty("userId")
    private Long userId;
    
    /**
     * User display name
     */
    @JsonProperty("userName")
    private String userName;
    
    /**
     * Overall learning level (A1, A2, B1, B2, C1, C2)
     */
    @JsonProperty("currentLevel")
    private String currentLevel;
    
    /**
     * Overall learning score (0-100)
     * Weighted average of vocabulary, grammar, speaking
     */
    @JsonProperty("overallScore")
    private Double overallScore;
    
    // ==================== Performance Breakdown ====================
    
    /**
     * Vocabulary performance
     */
    @JsonProperty("vocabulary")
    private PerformanceMetric vocabulary;
    
    /**
     * Grammar performance
     */
    @JsonProperty("grammar")
    private PerformanceMetric grammar;
    
    /**
     * Speaking performance
     */
    @JsonProperty("speaking")
    private PerformanceMetric speaking;
    
    // ==================== Progress Trends ====================
    
    /**
     * 7-day score trend
     */
    @JsonProperty("trend7Days")
    private TrendData trend7Days;
    
    /**
     * 30-day score trend
     */
    @JsonProperty("trend30Days")
    private TrendData trend30Days;
    
    // ==================== Recent Activity ====================
    
    /**
     * Recent learning activities
     */
    @JsonProperty("recentActivities")
    private List<RecentActivityDto> recentActivities;
    
    /**
     * Last learning activity timestamp
     */
    @JsonProperty("lastActivityAt")
    private LocalDateTime lastActivityAt;
    
    /**
     * Total learning days (calendar days with activity)
     */
    @JsonProperty("totalLearningDays")
    private Integer totalLearningDays;
    
    // ==================== Streaks & Consistency ====================
    
    /**
     * Current learning streak (consecutive days)
     */
    @JsonProperty("currentStreak")
    private Integer currentStreak;
    
    /**
     * Longest learning streak
     */
    @JsonProperty("longestStreak")
    private Integer longestStreak;
    
    // ==================== Achievements ====================
    
    /**
     * List of achieved milestones
     */
    @JsonProperty("achievements")
    private List<String> achievements;
    
    /**
     * Next target milestone
     */
    @JsonProperty("nextMilestone")
    private String nextMilestone;
    
    // ==================== Learning Patterns ====================
    
    /**
     * Topics/areas where user struggles most
     */
    @JsonProperty("weakTopics")
    private List<String> weakTopics;
    
    /**
     * Topics/areas where user excels
     */
    @JsonProperty("strongTopics")
    private List<String> strongTopics;
    
    /**
     * Learning frequency (times per week)
     */
    @JsonProperty("learningFrequency")
    private Double learningFrequency;
    
    /**
     * Average session duration (minutes)
     */
    @JsonProperty("averageSessionDuration")
    private Double averageSessionDuration;
    
    /**
     * Profile generated/updated timestamp
     */
    @JsonProperty("generatedAt")
    private LocalDateTime generatedAt;
    
    // ==================== Nested DTOs ====================
    
    /**
     * Performance metric for a specific skill
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceMetric {
        
        /**
         * Current score (0-100)
         */
        @JsonProperty("score")
        private Double score;
        
        /**
         * Best score achieved
         */
        @JsonProperty("bestScore")
        private Double bestScore;
        
        /**
         * Average score
         */
        @JsonProperty("averageScore")
        private Double averageScore;
        
        /**
         * Total activities/attempts
         */
        @JsonProperty("totalActivities")
        private Integer totalActivities;
        
        /**
         * Level in this skill (A1-C2)
         */
        @JsonProperty("level")
        private String level;
        
        /**
         * Progress in current level (0-100%)
         */
        @JsonProperty("progressInLevel")
        private Double progressInLevel;
        
        /**
         * Recommended focus area within this skill
         */
        @JsonProperty("recommendedFocus")
        private String recommendedFocus;
    }
    
    /**
     * Trend data (7-day or 30-day)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendData {
        
        /**
         * Trend direction (IMPROVING, STABLE, DECLINING)
         */
        @JsonProperty("direction")
        private String direction;
        
        /**
         * Percentage change from previous period
         */
        @JsonProperty("percentageChange")
        private Double percentageChange;
        
        /**
         * Average score during this period
         */
        @JsonProperty("averageScore")
        private Double averageScore;
        
        /**
         * Highest score in period
         */
        @JsonProperty("highestScore")
        private Double highestScore;
        
        /**
         * Lowest score in period
         */
        @JsonProperty("lowestScore")
        private Double lowestScore;
        
        /**
         * Number of activities in period
         */
        @JsonProperty("activitiesCount")
        private Integer activitiesCount;
    }
    
    /**
     * Recent learning activity
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentActivityDto {
        
        /**
         * Activity type (VOCABULARY, GRAMMAR, SPEAKING, QUIZ, AI_CHAT)
         */
        @JsonProperty("type")
        private String type;
        
        /**
         * Activity score (if applicable)
         */
        @JsonProperty("score")
        private Double score;
        
        /**
         * Activity topic/title
         */
        @JsonProperty("topic")
        private String topic;
        
        /**
         * Activity timestamp
         */
        @JsonProperty("timestamp")
        private LocalDateTime timestamp;
        
        /**
         * Duration in minutes
         */
        @JsonProperty("durationMinutes")
        private Integer durationMinutes;
    }
}
