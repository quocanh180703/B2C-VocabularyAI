package com.vocabularyai.personalization.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * PersonalizationDashboardDto
 * 
 * Dashboard tổng hợp tất cả dữ liệu personalization (TASK 8)
 * Tập hợp tất cả insights từ TASK 1-7 vào một single view
 * 
 * Data Sources:
 * - TASK 1: LearningProfileDto (learning profile)
 * - TASK 2: WeaknessStrengthDto (analysis)
 * - TASK 3: RecommendationDto (recommendations)
 * - TASK 4: LearningPlanDto (learning plan)
 * - TASK 5: DailyPracticeDto (today's practice)
 * - TASK 6: AdaptiveDifficultyDto (difficulty analysis)
 * - TASK 7: ProgressAnalysisDto (progress analysis)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalizationDashboardDto {
    
    /**
     * ID người dùng
     */
    @JsonProperty("userId")
    private Long userId;
    
    /**
     * Tên người dùng
     */
    @JsonProperty("userName")
    private String userName;
    
    /**
     * Thời gian tạo dashboard
     */
    @JsonProperty("generatedAt")
    private LocalDateTime generatedAt;
    
    // ==================== TASK 1: Learning Profile ====================
    
    /**
     * Profil học tập (TASK 1)
     * Bao gồm: điểm chung, mức độ, xu hướng, streaks, achievements
     */
    @JsonProperty("learningProfile")
    private LearningProfileDto learningProfile;
    
    // ==================== TASK 2: Weakness & Strength ====================
    
    /**
     * Phân tích điểm yếu/mạnh (TASK 2)
     * Bao gồm: weaknesses, strengths, priority actions, learning style
     */
    @JsonProperty("analysis")
    private WeaknessStrengthDto analysis;
    
    // ==================== TASK 3: Recommendations ====================
    
    /**
     * Gợi ý học tập (TASK 3)
     * Bao gồm: quick wins, long-term goals, critical recommendations
     */
    @JsonProperty("recommendations")
    private RecommendationDto recommendations;
    
    // ==================== TASK 4: Learning Plan ====================
    
    /**
     * Kế hoạch học tập 4 tuần (TASK 4)
     * Bao gồm: weekly breakdown, milestones, daily tasks
     */
    @JsonProperty("learningPlan")
    private LearningPlanDto learningPlan;
    
    // ==================== TASK 5: Daily Practice ====================
    
    /**
     * Hoạt động hôm nay (TASK 5)
     * Bao gồm: activities, goals, motivation tips, challenges
     */
    @JsonProperty("dailyPractice")
    private DailyPracticeDto dailyPractice;
    
    // ==================== TASK 6: Adaptive Difficulty ====================
    
    /**
     * Phân tích độ khó tự động (TASK 6)
     * Bao gồm: performance by topic, difficulty adjustments, recommendations
     */
    @JsonProperty("adaptiveDifficulty")
    private AdaptiveDifficultyDto adaptiveDifficulty;
    
    // ==================== TASK 7: Progress Analysis ====================
    
    /**
     * Phân tích tiến độ (TASK 7)
     * Bao gồm: completion %, velocity, milestones, predictions
     */
    @JsonProperty("progressAnalysis")
    private ProgressAnalysisDto progressAnalysis;
    
    // ==================== Dashboard Summary ====================
    
    /**
     * Tóm tắt dashboard
     * Gợi ý hành động chính cho hôm nay
     */
    @JsonProperty("todaysFocus")
    private String todaysFocus;
    
    /**
     * Mục tiêu chính tuần này
     */
    @JsonProperty("weeklyGoal")
    private String weeklyGoal;
    
    /**
     * Tiến độ tổng quát
     */
    @JsonProperty("overallProgress")
    private String overallProgress;
    
    /**
     * Các cột mốc gần nhất
     */
    @JsonProperty("nearestMilestone")
    private String nearestMilestone;
}
