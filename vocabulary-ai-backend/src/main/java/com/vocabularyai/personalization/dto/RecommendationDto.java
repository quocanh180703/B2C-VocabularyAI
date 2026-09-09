package com.vocabularyai.personalization.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RecommendationDto
 * 
 * Gợi ý học tập cá nhân hóa dựa trên phân tích điểm yếu/điểm mạnh (TASK 2)
 * Được tạo bởi Gemini AI để tối ưu hóa lộ trình học tập
 * 
 * Input: WeaknessStrengthDto (TASK 2)
 * Output: Danh sách gợi ý được xếp hạng theo ưu tiên
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationDto {
    
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
     * Thời gian tạo gợi ý
     */
    @JsonProperty("recommendedAt")
    private LocalDateTime recommendedAt;
    
    /**
     * Thời gian dự kiến cho kế hoạch này
     */
    @JsonProperty("validUntil")
    private LocalDateTime validUntil;
    
    // ==================== Tóm tắt Gợi ý ====================
    
    /**
     * Tổng quan chiến lược học tập (1-2 câu)
     */
    @JsonProperty("strategyOverview")
    private String strategyOverview;
    
    /**
     * Giai đoạn học đề xuất (BEGINNER, INTERMEDIATE, ADVANCED)
     */
    @JsonProperty("recommendedPhase")
    private String recommendedPhase;
    
    /**
     * Lĩnh vực tập trung chính
     */
    @JsonProperty("primaryFocus")
    private String primaryFocus;
    
    // ==================== Gợi ý Học Tập ====================
    
    /**
     * Danh sách tất cả gợi ý (10-15 item)
     */
    @JsonProperty("recommendations")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(contentUsing = RecommendationItemDeserializer.class)
    private List<RecommendationItem> recommendations;
    
    /**
     * Gợi ý nhanh chóng (dễ áp dụng, có tác động cao)
     */
    @JsonProperty("quickWins")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(contentUsing = RecommendationItemDeserializer.class)
    private List<RecommendationItem> quickWins;
    
    /**
     * Gợi ý dài hạn (cần thời gian nhưng có giá trị lớn)
     */
    @JsonProperty("longTermGoals")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(contentUsing = RecommendationItemDeserializer.class)
    private List<RecommendationItem> longTermGoals;
    
    /**
     * Gợi ý khẩn cấp (địa chỉ điểm yếu CRITICAL)
     */
    @JsonProperty("criticalRecommendations")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(contentUsing = RecommendationItemDeserializer.class)
    private List<RecommendationItem> criticalRecommendations;
    
    /**
     * Tổng số gợi ý
     */
    @JsonProperty("recommendationCount")
    private Integer recommendationCount;
    
    // ==================== Kế Hoạch Hành Động ====================
    
    /**
     * Lịch trình hàng tuần
     */
    @JsonProperty("weeklySchedule")
    private List<WeeklyScheduleItem> weeklySchedule;
    
    /**
     * Tối ưu hóa chi phí (thời gian/nỗ lực cần thiết)
     */
    @JsonProperty("estimatedEffort")
    private EstimatedEffortDto estimatedEffort;
    
    // ==================== Phần Thưởng & Động Lực ====================
    
    /**
     * Các cột mốc đến được dự kiến
     */
    @JsonProperty("expectedMilestones")
    private List<String> expectedMilestones;
    
    /**
     * Lý do tại sao những gợi ý này sẽ giúp đạt mục tiêu
     */
    @JsonProperty("rationale")
    private String rationale;
    
    /**
     * Mục tiêu cuối cùng sau 4 tuần
     */
    @JsonProperty("fourWeekGoal")
    private String fourWeekGoal;
    
    // ==================== Siêu Dữ Liệu ====================
    
    /**
     * Độ tự tin của gợi ý (0-100)
     */
    @JsonProperty("confidenceScore")
    private Double confidenceScore;
    
    /**
     * Thời gian đề xuất phân tích lại (ngày)
     */
    @JsonProperty("reanalysisPeriodDays")
    private Integer reanalysisPeriodDays;
    
    // ==================== Nested DTOs ====================
    
    /**
     * Một gợi ý học tập riêng lẻ
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationItem {
        
        /**
         * Hạng mức ưu tiên (1 = cao nhất)
         */
        @JsonProperty("priority")
        private Integer priority;
        
        /**
         * Loại gợi ý (VOCABULARY, GRAMMAR, SPEAKING, PRACTICE, REVIEW, SUPPLEMENTARY)
         */
        @JsonProperty("type")
        private String type;
        
        /**
         * Tên gợi ý/hoạt động
         */
        @JsonProperty("title")
        private String title;
        
        /**
         * Mô tả chi tiết
         */
        @JsonProperty("description")
        private String description;
        
        /**
         * Lý do tại sao được đề xuất
         */
        @JsonProperty("reasoning")
        private String reasoning;
        
        /**
         * Lĩnh vực mục tiêu (ví dụ: "Past Perfect Tense", "Academic Vocabulary")
         */
        @JsonProperty("targetArea")
        private String targetArea;
        
        /**
         * Tác động dự kiến (HIGH, MEDIUM, LOW)
         */
        @JsonProperty("expectedImpact")
        @com.fasterxml.jackson.annotation.JsonAlias({"impact"})
        private String expectedImpact;
        
        /**
         * Mức độ nỗ lực cần thiết (EASY, MODERATE, CHALLENGING)
         */
        @JsonProperty("effortLevel")
        @com.fasterxml.jackson.annotation.JsonAlias({"effort"})
        private String effortLevel;
        
        /**
         * Thời gian ước tính (phút)
         */
        @JsonProperty("estimatedTimeMinutes")
        private Integer estimatedTimeMinutes;
        
        /**
         * Tần suất thực hiện (mỗi ngày, mỗi tuần, mỗi tháng)
         */
        @JsonProperty("frequency")
        private String frequency;
        
        /**
         * Các tài nguyên được đề xuất (liên kết, công cụ)
         */
        @JsonProperty("resources")
        private List<ResourceLink> resources;
        
        /**
         * Các thước đo thành công
         */
        @JsonProperty("successMetrics")
        private List<String> successMetrics;
        
        /**
         * Cảnh báo/lưu ý
         */
        @JsonProperty("warnings")
        private List<String> warnings;
        
        /**
         * Phản hồi từ người dùng thực hiện
         */
        @JsonProperty("feedback")
        private String feedback;
    }
    
    /**
     * Liên kết tài nguyên
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceLink {
        
        /**
         * Tiêu đề tài nguyên
         */
        @JsonProperty("title")
        private String title;
        
        /**
         * Loại tài nguyên (VIDEO, ARTICLE, EXERCISE, TOOL)
         */
        @JsonProperty("type")
        private String type;
        
        /**
         * URL hoặc mô tả
         */
        @JsonProperty("url")
        private String url;
    }
    
    /**
     * Mục lịch trình hàng tuần
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeeklyScheduleItem {
        
        /**
         * Ngày trong tuần (MONDAY, TUESDAY, ..., SUNDAY)
         */
        @JsonProperty("dayOfWeek")
        private String dayOfWeek;
        
        /**
         * Nhiệm vụ chính cho ngày này
         */
        @JsonProperty("mainTask")
        private String mainTask;
        
        /**
         * Thời gian đề xuất
         */
        @JsonProperty("recommendedTime")
        private String recommendedTime;
        
        /**
         * Thời lượng (phút)
         */
        @JsonProperty("duration")
        private Integer duration;
        
        /**
         * Loại hoạt động
         */
        @JsonProperty("activityType")
        private String activityType;
    }
    
    /**
     * Ước tính nỗ lực
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EstimatedEffortDto {
        
        /**
         * Tổng phút mỗi tuần
         */
        @JsonProperty("minutesPerWeek")
        private Integer minutesPerWeek;
        
        /**
         * Ngày mỗi tuần
         */
        @JsonProperty("daysPerWeek")
        private Integer daysPerWeek;
        
        /**
         * Cách bố trí (CONCENTRATED = 1-2 ngày dài, DISTRIBUTED = mỗi ngày)
         */
        @JsonProperty("distribution")
        private String distribution;
        
        /**
         * Tên khó (dễ = nền tảng, khó = cải tiến)
         */
        @JsonProperty("difficulty")
        private String difficulty;
    }
}
