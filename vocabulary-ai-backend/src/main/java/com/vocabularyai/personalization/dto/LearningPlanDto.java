package com.vocabularyai.personalization.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * LearningPlanDto
 * 
 * Kế hoạch học tập 4 tuần có cấu trúc dựa trên gợi ý (TASK 3)
 * Được tạo bởi Gemini AI để tối ưu hóa tiến độ học tập
 * 
 * Input: RecommendationDto (TASK 3)
 * Output: Kế hoạch chi tiết từng ngày/tuần
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningPlanDto {
    
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
     * Thời gian tạo kế hoạch
     */
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    /**
     * Ngày bắt đầu kế hoạch
     */
    @JsonProperty("startDate")
    private LocalDateTime startDate;
    
    /**
     * Ngày kết thúc kế hoạch (4 tuần)
     */
    @JsonProperty("endDate")
    private LocalDateTime endDate;
    
    // ==================== Tóm tắt Kế Hoạch ====================
    
    /**
     * Tiêu đề kế hoạch
     */
    @JsonProperty("planTitle")
    private String planTitle;
    
    /**
     * Mô tả tổng quát kế hoạch
     */
    @JsonProperty("planDescription")
    private String planDescription;
    
    /**
     * Mục tiêu chính (1-3 câu)
     */
    @JsonProperty("primaryGoal")
    private String primaryGoal;
    
    /**
     * Mục tiêu thứ cấp
     */
    @JsonProperty("secondaryGoals")
    private List<String> secondaryGoals;
    
    /**
     * Tổng thời gian cam kết (phút/tuần)
     */
    @JsonProperty("totalCommitmentMinutesPerWeek")
    private Integer totalCommitmentMinutesPerWeek;
    
    // ==================== Chi Tiết Kế Hoạch ====================
    
    /**
     * Danh sách các tuần trong kế hoạch
     */
    @JsonProperty("weeks")
    private List<WeekPlanDto> weeks;
    
    /**
     * Các cột mốc/milestone chính
     */
    @JsonProperty("milestones")
    private List<MilestoneDto> milestones;
    
    /**
     * Các điều kiện thành công
     */
    @JsonProperty("successCriteria")
    private List<String> successCriteria;
    
    // ==================== Chiến Lược & Lưu Ý ====================
    
    /**
     * Chiến lược chung của kế hoạch
     */
    @JsonProperty("strategy")
    private String strategy;
    
    /**
     * Các tài nguyên cần thiết
     */
    @JsonProperty("requiredResources")
    private List<String> requiredResources;
    
    /**
     * Các lưu ý quan trọng
     */
    @JsonProperty("importantNotes")
    private List<String> importantNotes;
    
    /**
     * Cách đối phó với thách thức
     */
    @JsonProperty("challengeMitigation")
    private List<String> challengeMitigation;
    
    // ==================== Siêu Dữ Liệu ====================
    
    /**
     * Độ tự tin của kế hoạch (0-100)
     */
    @JsonProperty("confidenceScore")
    private Double confidenceScore;
    
    /**
     * Khó độ kế hoạch (EASY, MODERATE, CHALLENGING)
     */
    @JsonProperty("difficultyLevel")
    private String difficultyLevel;
    
    /**
     * Tần suất điều chỉnh được đề xuất (ngày)
     */
    @JsonProperty("adjustmentFrequencyDays")
    private Integer adjustmentFrequencyDays;
    
    // ==================== Nested DTOs ====================
    
    /**
     * Kế hoạch cho một tuần
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeekPlanDto {
        
        /**
         * Số tuần (1-4)
         */
        @JsonProperty("weekNumber")
        private Integer weekNumber;
        
        /**
         * Tiêu đề tuần
         */
        @JsonProperty("weekTitle")
        private String weekTitle;
        
        /**
         * Mục tiêu của tuần này
         */
        @JsonProperty("weekGoal")
        private String weekGoal;
        
        /**
         * Chủ đề chính
         */
        @JsonProperty("mainTheme")
        private String mainTheme;
        
        /**
         * Mô tả chi tiết
         */
        @JsonProperty("description")
        private String description;
        
        /**
         * Danh sách các ngày trong tuần
         */
        @JsonProperty("days")
        private List<DayPlanDto> days;
        
        /**
         * Tổng thời gian (phút)
         */
        @JsonProperty("totalMinutes")
        private Integer totalMinutes;
        
        /**
         * Các hoạt động chính
         */
        @JsonProperty("mainActivities")
        private List<String> mainActivities;
        
        /**
         * Các cột mốc trong tuần
         */
        @JsonProperty("checkpoints")
        private List<String> checkpoints;
    }
    
    /**
     * Kế hoạch cho một ngày
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayPlanDto {
        
        /**
         * Ngày trong tuần (MONDAY, TUESDAY, ..., SUNDAY)
         */
        @JsonProperty("dayOfWeek")
        private String dayOfWeek;
        
        /**
         * Ngày cụ thể
         */
        @JsonProperty("date")
        private String date;
        
        /**
         * Tiêu đề ngày
         */
        @JsonProperty("dayTitle")
        private String dayTitle;
        
        /**
         * Mục tiêu hôm nay
         */
        @JsonProperty("dailyGoal")
        private String dailyGoal;
        
        /**
         * Danh sách các nhiệm vụ
         */
        @JsonProperty("tasks")
        private List<TaskDto> tasks;
        
        /**
         * Tổng thời gian (phút)
         */
        @JsonProperty("totalMinutes")
        private Integer totalMinutes;
        
        /**
         * Độ khó dự kiến
         */
        @JsonProperty("expectedDifficulty")
        private String expectedDifficulty;
        
        /**
         * Ghi chú cho ngày này
         */
        @JsonProperty("notes")
        private String notes;
    }
    
    /**
     * Một nhiệm vụ trong ngày
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskDto {
        
        /**
         * Thứ tự nhiệm vụ
         */
        @JsonProperty("order")
        private Integer order;
        
        /**
         * Tiêu đề nhiệm vụ
         */
        @JsonProperty("title")
        private String title;
        
        /**
         * Mô tả chi tiết
         */
        @JsonProperty("description")
        private String description;
        
        /**
         * Loại (VOCABULARY, GRAMMAR, SPEAKING, PRACTICE, REVIEW, QUIZ)
         */
        @JsonProperty("type")
        private String type;
        
        /**
         * Thời gian (phút)
         */
        @JsonProperty("durationMinutes")
        private Integer durationMinutes;
        
        /**
         * Mục tiêu cụ thể
         */
        @JsonProperty("objective")
        private String objective;
        
        /**
         * Hướng dẫn thực hiện
         */
        @JsonProperty("instructions")
        private String instructions;
        
        /**
         * Tài nguyên cần thiết
         */
        @JsonProperty("resources")
        private List<String> resources;
        
        /**
         * Chỉ số đánh giá thành công
         */
        @JsonProperty("successCriteria")
        private String successCriteria;
    }
    
    /**
     * Một cột mốc trong kế hoạch
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MilestoneDto {
        
        /**
         * Tuần có cột mốc
         */
        @JsonProperty("week")
        private Integer week;
        
        /**
         * Tên cột mốc
         */
        @JsonProperty("name")
        private String name;
        
        /**
         * Mô tả
         */
        @JsonProperty("description")
        private String description;
        
        /**
         * Cách đánh giá thành công
         */
        @JsonProperty("evaluation")
        private String evaluation;
        
        /**
         * Phần thưởng/kỷ niệm
         */
        @JsonProperty("reward")
        private String reward;
    }
}
