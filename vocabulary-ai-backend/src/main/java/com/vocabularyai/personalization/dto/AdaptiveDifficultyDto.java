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
 * AdaptiveDifficultyDto
 * 
 * Điều chỉnh độ khó tự động dựa trên hiệu suất người dùng (TASK 6)
 * Được tạo bởi Gemini AI để tối ưu hóa độ khó hoạt động
 * 
 * Input: DailyPracticeDto (TASK 5) + performance metrics
 * Output: Gợi ý điều chỉnh độ khó và phân tích hiệu suất
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdaptiveDifficultyDto {
    
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
    
    // ==================== Tóm Tắt Hiệu Suất ====================
    
    /**
     * Tóm tắt hiệu suất chung (1-2 câu)
     */
    @JsonProperty("performanceSummary")
    private String performanceSummary;
    
    /**
     * Hiệu suất trung bình (%)
     */
    @JsonProperty("averagePerformance")
    private Double averagePerformance;
    
    /**
     * Xu hướng hiệu suất (IMPROVING, STABLE, DECLINING)
     */
    @JsonProperty("performanceTrend")
    private String performanceTrend;
    
    /**
     * Độ khó hiện tại trung bình (1-10)
     */
    @JsonProperty("currentAverageDifficulty")
    private Double currentAverageDifficulty;
    
    /**
     * Độ khó được đề xuất (1-10)
     */
    @JsonProperty("recommendedAverageDifficulty")
    private Double recommendedAverageDifficulty;
    
    /**
     * Yếu tố điều chỉnh (0.8 = dễ hơn, 1.2 = khó hơn)
     */
    @JsonProperty("adjustmentFactor")
    private Double adjustmentFactor;
    
    // ==================== Phân Tích Theo Chủ Đề ====================
    
    /**
     * Phân tích hiệu suất theo từng chủ đề/loại
     * Key: VOCABULARY, GRAMMAR, SPEAKING, PRACTICE, REVIEW, QUIZ
     */
    @JsonProperty("performanceByTopic")
    private Map<String, TopicDifficultyAnalysis> performanceByTopic;
    
    // ==================== Gợi Ý Điều Chỉnh ====================
    
    /**
     * Danh sách gợi ý điều chỉnh độ khó
     */
    @JsonProperty("difficultyAdjustments")
    private List<DifficultyAdjustmentItem> difficultyAdjustments;
    
    /**
     * Các chủ đề nên làm dễ hơn
     */
    @JsonProperty("topicsToEase")
    private List<String> topicsToEase;
    
    /**
     * Các chủ đề nên làm khó hơn
     */
    @JsonProperty("topicsToChallenge")
    private List<String> topicsToChallenge;
    
    /**
     * Các chủ đề nên giữ nguyên
     */
    @JsonProperty("topicsToMaintain")
    private List<String> topicsToMaintain;
    
    // ==================== Lưu Ý & Gợi Ý ====================
    
    /**
     * Các lưu ý cho người dùng
     */
    @JsonProperty("tips")
    private List<String> tips;
    
    /**
     * Khoảng thời gian được đề xuất để điều chỉnh lại (ngày)
     */
    @JsonProperty("nextReviewDays")
    private Integer nextReviewDays;
    
    /**
     * Các cảnh báo nếu có
     */
    @JsonProperty("warnings")
    private List<String> warnings;
    
    // ==================== Siêu Dữ Liệu ====================
    
    /**
     * Độ tự tin của phân tích (0-100)
     */
    @JsonProperty("confidenceScore")
    private Double confidenceScore;
    
    /**
     * Số lượng hoạt động được phân tích
     */
    @JsonProperty("activitiesAnalyzed")
    private Integer activitiesAnalyzed;
    
    /**
     * Ngày cuối cùng điều chỉnh
     */
    @JsonProperty("lastAdjustmentDate")
    private LocalDateTime lastAdjustmentDate;
    
    /**
     * Số lần điều chỉnh trong 30 ngày
     */
    @JsonProperty("adjustmentsInLastMonth")
    private Integer adjustmentsInLastMonth;
    
    // ==================== Nested DTOs ====================
    
    /**
     * Phân tích hiệu suất và gợi ý độ khó cho một chủ đề
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopicDifficultyAnalysis {
        
        /**
         * Tên chủ đề (VOCABULARY, GRAMMAR, SPEAKING, ...)
         */
        @JsonProperty("topic")
        private String topic;
        
        /**
         * Hiệu suất trung bình (%)
         */
        @JsonProperty("averagePerformance")
        private Double averagePerformance;
        
        /**
         * Độ khó hiện tại (1-10)
         */
        @JsonProperty("currentDifficulty")
        private Double currentDifficulty;
        
        /**
         * Độ khó được đề xuất (1-10)
         */
        @JsonProperty("recommendedDifficulty")
        private Double recommendedDifficulty;
        
        /**
         * Sự thay đổi độ khó được đề xuất (+/- số)
         */
        @JsonProperty("difficultyChange")
        private Double difficultyChange;
        
        /**
         * Số lượng hoạt động hoàn thành
         */
        @JsonProperty("activitiesCompleted")
        private Integer activitiesCompleted;
        
        /**
         * Xu hướng hiệu suất (IMPROVING, STABLE, DECLINING)
         */
        @JsonProperty("trend")
        private String trend;
        
        /**
         * Mức độ thành thạo dự kiến (%)
         */
        @JsonProperty("masteryLevel")
        private Double masteryLevel;
        
        /**
         * Lý do đề xuất điều chỉnh
         */
        @JsonProperty("adjustmentReason")
        private String adjustmentReason;
        
        /**
         * Ngày được đề xuất thay đổi lại
         */
        @JsonProperty("nextReviewDate")
        private LocalDateTime nextReviewDate;
        
        /**
         * Các lưu ý cụ thể cho chủ đề này
         */
        @JsonProperty("notes")
        private List<String> notes;
    }
    
    /**
     * Một gợi ý điều chỉnh độ khó cụ thể
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DifficultyAdjustmentItem {
        
        /**
         * Ưu tiên (1 = cao nhất)
         */
        @JsonProperty("priority")
        private Integer priority;
        
        /**
         * Chủ đề/loại hoạt động
         */
        @JsonProperty("topic")
        private String topic;
        
        /**
         * Độ khó hiện tại
         */
        @JsonProperty("currentDifficulty")
        private Double currentDifficulty;
        
        /**
         * Độ khó được đề xuất
         */
        @JsonProperty("recommendedDifficulty")
        private Double recommendedDifficulty;
        
        /**
         * Phương hướng (INCREASE, DECREASE, MAINTAIN)
         */
        @JsonProperty("direction")
        private String direction;
        
        /**
         * Lý do điều chỉnh
         */
        @JsonProperty("reason")
        private String reason;
        
        /**
         * Hiệu suất hiện tại (%)
         */
        @JsonProperty("currentPerformance")
        private Double currentPerformance;
        
        /**
         * Hiệu suất mục tiêu (%)
         */
        @JsonProperty("targetPerformance")
        private Double targetPerformance;
        
        /**
         * Số hoạt động để kiểm tra trước khi điều chỉnh lại
         */
        @JsonProperty("activitiesBeforeNextReview")
        private Integer activitiesBeforeNextReview;
        
        /**
         * Lời khuyên cho người dùng
         */
        @JsonProperty("advice")
        private String advice;
        
        /**
         * Ngày đề xuất điều chỉnh lại
         */
        @JsonProperty("suggestedReviewDate")
        private LocalDateTime suggestedReviewDate;
    }
    
    /**
     * Lịch sử điều chỉnh độ khó
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DifficultyAdjustmentHistory {
        
        /**
         * Chủ đề
         */
        @JsonProperty("topic")
        private String topic;
        
        /**
         * Độ khó cũ
         */
        @JsonProperty("previousDifficulty")
        private Double previousDifficulty;
        
        /**
         * Độ khó mới
         */
        @JsonProperty("newDifficulty")
        private Double newDifficulty;
        
        /**
         * Thời gian điều chỉnh
         */
        @JsonProperty("adjustmentDate")
        private LocalDateTime adjustmentDate;
        
        /**
         * Lý do điều chỉnh
         */
        @JsonProperty("reason")
        private String reason;
        
        /**
         * Hiệu suất trước điều chỉnh
         */
        @JsonProperty("performanceBeforeAdjustment")
        private Double performanceBeforeAdjustment;
        
        /**
         * Hiệu suất sau điều chỉnh (nếu có)
         */
        @JsonProperty("performanceAfterAdjustment")
        private Double performanceAfterAdjustment;
    }
}
