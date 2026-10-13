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
@Schema(description = "Cập nhật tiến độ học từ vựng")
public class UpdateLearningProgressRequest {
    
    @Pattern(regexp = "^(NEW|LEARNING|MASTERED)$")
    @Schema(description = "Trạng thái học", example = "MASTERED")
    private String status;
    
    @Min(0)
    @Schema(description = "Số lần trả lời đúng", example = "5")
    private Integer correctCount;
    
    @Min(0)
    @Schema(description = "Số lần trả lời sai", example = "1")
    private Integer wrongCount;
    
    @Min(0)
    @Schema(description = "Số lần ôn tập", example = "3")
    private Integer reviewCount;
    
    @Schema(description = "Lần ôn tập cuối cùng")
    private LocalDateTime lastReviewedAt;
    
    @Schema(description = "Lần ôn tập tiếp theo")
    private LocalDateTime nextReviewAt;
}
