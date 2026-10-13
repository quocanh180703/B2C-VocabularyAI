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
@Schema(description = "Tạo tiến độ học từ vựng")
public class CreateLearningProgressRequest {
    
    @NotNull
    @Positive
    @Schema(description = "User ID", example = "1")
    private Long userId;
    
    @NotNull
    @Positive
    @Schema(description = "Word ID", example = "123")
    private Long wordId;
    
    @NotBlank
    @Pattern(regexp = "^(NEW|LEARNING|MASTERED)$")
    @Schema(description = "Trạng thái học (NEW, LEARNING, MASTERED)", example = "LEARNING")
    private String status;
    
    @Min(0)
    @Schema(description = "Số lần trả lời đúng", example = "3")
    private Integer correctCount;
    
    @Min(0)
    @Schema(description = "Số lần trả lời sai", example = "1")
    private Integer wrongCount;
    
    @Min(0)
    @Schema(description = "Số lần ôn tập", example = "2")
    private Integer reviewCount;
    
    @Schema(description = "Lần ôn tập cuối cùng")
    private LocalDateTime lastReviewedAt;
    
    @Schema(description = "Lần ôn tập tiếp theo (spaced repetition)")
    private LocalDateTime nextReviewAt;
}
