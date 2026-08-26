package com.vocabularyai.personalization.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * ProgressAnalysisDto
 * 
 * Phân tích tiến độ hướng tới mục tiêu (TASK 7)
 * Được tạo bởi Gemini AI để theo dõi và dự đoán tiến độ học tập
 * 
 * Input: LearningPlanDto (TASK 4) + performance history
 * Output: Phân tích tiến độ, dự đoán, và gợi ý điều chỉnh
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressAnalysisDto {
    
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
     * Thời gian tạo phân tích
     */
    @JsonProperty("analyzedAt")
    private LocalDateTime analyzedAt;
    
    // ==================== Tóm Tắt Tiến Độ ====================
    
    /**
     * Tóm tắt tiến độ chung (1-2 câu)
     */
    @JsonProperty("progressSummary")
    private String progressSummary;
    
    /**
     * Tỉ lệ hoàn thành kế hoạch (%)
     */
    @JsonProperty("completionPercentage")
    private Double completionPercentage;
    
    /**
     * Số ngày đã học
     */
    @JsonProperty("daysLearned")
    private Integer daysLearned;
    
    /**
     * Số ngày còn lại trong kế hoạch
     */
    @JsonProperty("daysRemaining")
    private Integer daysRemaining;
    
    /**
     * Tổng ngày kế hoạch (28 ngày = 4 tuần)
     */
    @JsonProperty("totalPlanDays")
    private Integer totalPlanDays;
    
    /**
     * Xu hướng tiến độ (ON_TRACK, AHEAD, BEHIND)
     */
    @JsonProperty("progressTrend")
    private String progressTrend;
    
    /**
     * Tốc độ cải thiện hàng tuần (%)
     */
    @JsonProperty("weeklyImprovement")
    private Double weeklyImprovement;
    
    // ==================== Phân Tích Mục Tiêu ====================
    
    /**
     * Mục tiêu chính của kế hoạch
     */
    @JsonProperty("primaryGoal")
    private String primaryGoal;
    
    /**
     * Tiến độ hướng tới mục tiêu chính (%)
     */
    @JsonProperty("primaryGoalProgress")
    private Double primaryGoalProgress;
    
    /**
     * Phần thưởng dự kiến tại mục tiêu
     */
    @JsonProperty("primaryGoalReward")
    private String primaryGoalReward;
    
    /**
     * Các mục tiêu thứ cấp và tiến độ của chúng
     */
    @JsonProperty("secondaryGoalsProgress")
    private Map<String, Double> secondaryGoalsProgress;
    
    // ==================== Phân Tích Milestone ====================
    
    /**
     * Các milestone đã hoàn thành
     */
    @JsonProperty("completedMilestones")
    private List<MilestoneProgressItem> completedMilestones;
    
    /**
     * Milestone hiện tại (đang tiến hành)
     */
    @JsonProperty("currentMilestone")
    private MilestoneProgressItem currentMilestone;
    
    /**
     * Milestone tiếp theo
     */
    @JsonProperty("nextMilestone")
    private MilestoneProgressItem nextMilestone;
    
    /**
     * Ngày dự kiến hoàn thành milestone tiếp theo
     */
    @JsonProperty("estimatedMilestoneDate")
    private LocalDateTime estimatedMilestoneDate;
    
    // ==================== Dự Đoán Tiến Độ ====================
    
    /**
     * Ngày dự kiến hoàn thành kế hoạch (nếu giữ tốc độ hiện tại)
     */
    @JsonProperty("estimatedCompletionDate")
    private LocalDateTime estimatedCompletionDate;
    
    /**
     * Sớm/muộn so với kế hoạch (ngày)
     */
    @JsonProperty("daysAheadOrBehind")
    private Integer daysAheadOrBehind;
    
    /**
     * Xác suất hoàn thành đúng hạn (%)
     */
    @JsonProperty("onTimeCompletionProbability")
    private Double onTimeCompletionProbability;
    
    /**
     * Tốc độ học cần thiết để hoàn thành đúng hạn (%)
     */
    @JsonProperty("requiredPacePercentage")
    private Double requiredPacePercentage;
    
    /**
     * Tốc độ học hiện tại (%)
     */
    @JsonProperty("currentPacePercentage")
    private Double currentPacePercentage;
    
    // ==================== Phân Tích Hiệu Suất ====================
    
    /**
     * Hiệu suất trung bình (%)
     */
    @JsonProperty("averagePerformance")
    private Double averagePerformance;
    
    /**
     * Hiệu suất cao nhất (%)
     */
    @JsonProperty("peakPerformance")
    private Double peakPerformance;
    
    /**
     * Hiệu suất thấp nhất (%)
     */
    @JsonProperty("lowestPerformance")
    private Double lowestPerformance;
    
    /**
     * Tính nhất quán (0-100, càng cao càng nhất quán)
     */
    @JsonProperty("consistency")
    private Double consistency;
    
    /**
     * Xu hướng hiệu suất (IMPROVING, STABLE, DECLINING)
     */
    @JsonProperty("performanceTrend")
    private String performanceTrend;
    
    // ==================== Gợi Ý & Hành Động ====================
    
    /**
     * Các gợi ý để duy trì tiến độ
     */
    @JsonProperty("tips")
    private List<String> tips;
    
    /**
     * Các lưu ý về các khu vực cần cải thiện
     */
    @JsonProperty("areasForImprovement")
    private List<String> areasForImprovement;
    
    /**
     * Các hành động được đề xuất
     */
    @JsonProperty("recommendedActions")
    private List<String> recommendedActions;
    
    /**
     * Các cảnh báo nếu có
     */
    @JsonProperty("warnings")
    private List<String> warnings;
    
    /**
     * Số điểm thưởng có thể nhận được (gamification)
     */
    @JsonProperty("potentialRewardPoints")
    private Integer potentialRewardPoints;
    
    /**
     * Các cột mốc đạt được gần đây
     */
    @JsonProperty("recentAchievements")
    private List<String> recentAchievements;
    
    // ==================== Siêu Dữ Liệu ====================
    
    /**
     * Độ tự tin của phân tích (0-100)
     */
    @JsonProperty("confidenceScore")
    private Double confidenceScore;
    
    /**
     * Số ngày dữ liệu được phân tích
     */
    @JsonProperty("daysAnalyzed")
    private Integer daysAnalyzed;
    
    /**
     * Ngày tái phân tích được đề xuất
     */
    @JsonProperty("nextAnalysisDate")
    private LocalDateTime nextAnalysisDate;
    
    // ==================== Nested DTOs ====================
    
    /**
     * Tiến độ của một milestone
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MilestoneProgressItem {
        
        /**
         * Số tuần của milestone
         */
        @JsonProperty("week")
        private Integer week;
        
        /**
         * Tên milestone
         */
        @JsonProperty("name")
        private String name;
        
        /**
         * Mô tả
         */
        @JsonProperty("description")
        private String description;
        
        /**
         * Ngày bắt đầu
         */
        @JsonProperty("startDate")
        private LocalDateTime startDate;
        
        /**
         * Ngày kết thúc (dự kiến hoặc thực tế)
         */
        @JsonProperty("endDate")
        private LocalDateTime endDate;
        
        /**
         * Trạng thái (NOT_STARTED, IN_PROGRESS, COMPLETED)
         */
        @JsonProperty("status")
        private String status;
        
        /**
         * Tiến độ hoàn thành (%)
         */
        @JsonProperty("completionPercentage")
        private Double completionPercentage;
        
        /**
         * Cách đánh giá thành công
         */
        @JsonProperty("successCriteria")
        private String successCriteria;
        
        /**
         * Đã đạt thành công hay không
         */
        @JsonProperty("successfullyCompleted")
        private Boolean successfullyCompleted;
        
        /**
         * Phần thưởng/kỷ niệm
         */
        @JsonProperty("reward")
        private String reward;
        
        /**
         * Ngày được hoàn thành thực tế (nếu đã hoàn thành)
         */
        @JsonProperty("actualCompletionDate")
        private LocalDateTime actualCompletionDate;
        
        /**
         * Sớm/muộn so với dự kiến (ngày)
         */
        @JsonProperty("daysEarlyOrLate")
        private Integer daysEarlyOrLate;
    }
    
    /**
     * Phân tích velocity (tốc độ cải thiện)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VelocityAnalysis {
        
        /**
         * Velocity tuần trước (%)
         */
        @JsonProperty("lastWeekVelocity")
        private Double lastWeekVelocity;
        
        /**
         * Velocity trung bình (%)
         */
        @JsonProperty("averageVelocity")
        private Double averageVelocity;
        
        /**
         * Velocity tối đa (%)
         */
        @JsonProperty("peakVelocity")
        private Double peakVelocity;
        
        /**
         * Xu hướng velocity (ACCELERATING, CONSTANT, DECELERATING)
         */
        @JsonProperty("velocityTrend")
        private String velocityTrend;
        
        /**
         * Tốc độ cần thiết để hoàn thành đúng hạn
         */
        @JsonProperty("requiredVelocity")
        private Double requiredVelocity;
        
        /**
         * Có khả thi hay không
         */
        @JsonProperty("isFeasible")
        private Boolean isFeasible;
    }
    
    /**
     * Phân tích chi tiết một khoảng thời gian
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeriodAnalysis {
        
        /**
         * Tên khoảng (VD: "Week 1", "Last 7 days")
         */
        @JsonProperty("periodName")
        private String periodName;
        
        /**
         * Ngày bắt đầu
         */
        @JsonProperty("startDate")
        private LocalDateTime startDate;
        
        /**
         * Ngày kết thúc
         */
        @JsonProperty("endDate")
        private LocalDateTime endDate;
        
        /**
         * Hiệu suất trung bình
         */
        @JsonProperty("averagePerformance")
        private Double averagePerformance;
        
        /**
         * Số hoạt động hoàn thành
         */
        @JsonProperty("activitiesCompleted")
        private Integer activitiesCompleted;
        
        /**
         * Số giờ học
         */
        @JsonProperty("learningHours")
        private Double learningHours;
        
        /**
         * Tiến độ
         */
        @JsonProperty("progressPercentage")
        private Double progressPercentage;
        
        /**
         * Xu hướng
         */
        @JsonProperty("trend")
        private String trend;
        
        /**
         * Ghi chú
         */
        @JsonProperty("notes")
        private String notes;
    }
}
