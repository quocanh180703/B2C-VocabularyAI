package com.vocabularyai.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Chi tiết lần làm bài kiểm tra")
public class QuizAttemptDetailResponse {
    
    @Schema(description = "ID lần làm", example = "1")
    private Long id;
    
    @Schema(description = "User ID", example = "1")
    private Long userId;
    
    @Schema(description = "Email người dùng", example = "user@example.com")
    private String userEmail;
    
    @Schema(description = "Quiz ID", example = "5")
    private Long quizId;
    
    @Schema(description = "Tiêu đề bài kiểm tra", example = "Unit 1 Quiz")
    private String quizTitle;
    
    @Schema(description = "Điểm số (%)", example = "85.0")
    private Double score;
    
    @Schema(description = "Tổng số câu hỏi", example = "10")
    private Integer totalQuestions;
    
    @Schema(description = "Số câu trả lời đúng", example = "8")
    private Integer correctAnswers;
    
    @Schema(description = "Thời gian làm bài (giây)", example = "900")
    private Long timeTakenSeconds;
    
    @Schema(description = "Đã hoàn thành", example = "true")
    private Boolean isCompleted;
    
    @Schema(description = "Thời gian bắt đầu", example = "2026-10-03T10:00:00")
    private LocalDateTime startedAt;
    
    @Schema(description = "Thời gian nộp bài", example = "2026-10-03T10:15:00")
    private LocalDateTime submittedAt;
    
    @Schema(description = "Ngày tạo", example = "2026-10-03T10:00:00")
    private LocalDateTime createdAt;
}
