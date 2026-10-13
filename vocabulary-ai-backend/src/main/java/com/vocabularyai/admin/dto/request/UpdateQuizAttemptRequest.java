package com.vocabularyai.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Cập nhật lần làm bài kiểm tra")
public class UpdateQuizAttemptRequest {
    
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    @Schema(description = "Điểm số (%)", example = "87.0")
    private Double score;
    
    @Min(0)
    @Schema(description = "Tổng số câu hỏi", example = "10")
    private Integer totalQuestions;
    
    @Min(0)
    @Schema(description = "Số câu trả lời đúng", example = "9")
    private Integer correctAnswers;
    
    @Schema(description = "Thời gian nộp bài", example = "2026-10-03T10:15:00")
    private LocalDateTime submittedAt;
}
