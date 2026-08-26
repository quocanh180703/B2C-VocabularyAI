package com.vocabularyai.personalization.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DailyPracticeDto
 * 
 * Gợi ý hoạt động học tập cho hôm nay từ kế hoạch 4 tuần
 * Được tạo bởi Gemini AI để tối ưu hóa luyện tập hàng ngày
 * 
 * Input: LearningPlanDto (TASK 4) + current date
 * Output: Danh sách hoạt động cho hôm nay
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyPracticeDto {
    
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
     * Ngày
     */
    @JsonProperty("date")
    private LocalDateTime date;
    
    /**
     * Ngày trong tuần
     */
    @JsonProperty("dayOfWeek")
    private String dayOfWeek;
    
    /**
     * Tuần số (1-4)
     */
    @JsonProperty("weekNumber")
    private Integer weekNumber;
    
    // ==================== Tóm Tắt Ngày ====================
    
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
     * Lời khuyên động lực
     */
    @JsonProperty("motivationalTip")
    private String motivationalTip;
    
    /**
     * Độ khó dự kiến
     */
    @JsonProperty("expectedDifficulty")
    private String expectedDifficulty;
    
    // ==================== Hoạt Động Luyện Tập ====================
    
    /**
     * Danh sách hoạt động (2-4 items)
     */
    @JsonProperty("activities")
    private List<ActivityDto> activities;
    
    /**
     * Tổng thời gian (phút)
     */
    @JsonProperty("totalDurationMinutes")
    private Integer totalDurationMinutes;
    
    /**
     * Hoạt động ưu tiên (bắt buộc làm)
     */
    @JsonProperty("priorityActivities")
    private List<ActivityDto> priorityActivities;
    
    /**
     * Hoạt động tuỳ chọn (nếu có thời gian)
     */
    @JsonProperty("optionalActivities")
    private List<ActivityDto> optionalActivities;
    
    // ==================== Lưu Ý & Gợi Ý ====================
    
    /**
     * Lưu ý cho ngày này
     */
    @JsonProperty("tips")
    private List<String> tips;
    
    /**
     * Thách thức dự kiến
     */
    @JsonProperty("expectedChallenges")
    private List<String> expectedChallenges;
    
    /**
     * Cách vượt qua thách thức
     */
    @JsonProperty("challengeSolutions")
    private List<String> challengeSolutions;
    
    /**
     * Thời gian tốt nhất để luyện tập
     */
    @JsonProperty("bestPracticeTime")
    private String bestPracticeTime;
    
    // ==================== Siêu Dữ Liệu ====================
    
    /**
     * Độ tự tin của gợi ý (0-100)
     */
    @JsonProperty("confidenceScore")
    private Double confidenceScore;
    
    /**
     * Kế hoạch có khả thi không
     */
    @JsonProperty("isFeasible")
    private Boolean isFeasible;
    
    /**
     * Ghi chú người dùng (lưu được)
     */
    @JsonProperty("userNotes")
    private String userNotes;
    
    /**
     * Hoàn thành hay chưa
     */
    @JsonProperty("completed")
    private Boolean completed;
    
    /**
     * Thời gian tạo gợi ý
     */
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    // ==================== Nested DTOs ====================
    
    /**
     * Một hoạt động luyện tập
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityDto {
        
        /**
         * Thứ tự hoạt động
         */
        @JsonProperty("order")
        private Integer order;
        
        /**
         * Tiêu đề hoạt động
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
         * Tiêu chí đánh giá
         */
        @JsonProperty("successCriteria")
        private String successCriteria;
        
        /**
         * Độ khó (EASY, MODERATE, CHALLENGING)
         */
        @JsonProperty("difficulty")
        private String difficulty;
        
        /**
         * Có bắt buộc không
         */
        @JsonProperty("mandatory")
        private Boolean mandatory;
    }
}
