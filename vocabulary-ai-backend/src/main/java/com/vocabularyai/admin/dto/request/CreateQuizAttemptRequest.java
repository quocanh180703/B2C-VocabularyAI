package com.vocabularyai.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tạo lần làm bài kiểm tra")
public class CreateQuizAttemptRequest {
    
    @NotNull
    @Positive
    @Schema(description = "User ID", example = "1")
    private Long userId;
    
    @NotNull
    @Positive
    @Schema(description = "Quiz ID", example = "5")
    private Long quizId;
    
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    @Schema(description = "Điểm số (%)", example = "85.0")
    private Double score;
    
    @Min(0)
    @Schema(description = "Tổng số câu hỏi", example = "10")
    private Integer totalQuestions;
    
    @Min(0)
    @Schema(description = "Số câu trả lời đúng", example = "8")
    private Integer correctAnswers;
    
    @NotNull
    @Schema(description = "Thời gian bắt đầu", example = "2026-10-03T10:00:00")
    private LocalDateTime startedAt;
    
    @Schema(description = "Thời gian nộp bài", example = "2026-10-03T10:15:00")
    private LocalDateTime submittedAt;
}
