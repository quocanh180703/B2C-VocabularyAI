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
@Schema(description = "Chi tiết tiến độ học từ vựng")
public class LearningProgressDetailResponse {
    
    @Schema(description = "ID tiến độ", example = "1")
    private Long id;
    
    @Schema(description = "User ID", example = "1")
    private Long userId;
    
    @Schema(description = "Word ID", example = "123")
    private Long wordId;
    
    @Schema(description = "Tên từ vựng", example = "apple")
    private String wordText;
    
    @Schema(description = "Trạng thái học", example = "LEARNING")
    private String status;
    
    @Schema(description = "Số lần trả lời đúng", example = "3")
    private Integer correctCount;
    
    @Schema(description = "Số lần trả lời sai", example = "1")
    private Integer wrongCount;
    
    @Schema(description = "Số lần ôn tập", example = "2")
    private Integer reviewCount;
    
    @Schema(description = "Tỷ lệ chính xác (%)", example = "75.0")
    private Double accuracy;
    
    @Schema(description = "Lần ôn tập cuối", example = "2026-10-03T10:30:00")
    private LocalDateTime lastReviewedAt;
    
    @Schema(description = "Lần ôn tập tiếp", example = "2026-10-05T10:30:00")
    private LocalDateTime nextReviewAt;
    
    @Schema(description = "Có cần ôn tập không", example = "true")
    private Boolean needsReview;
    
    @Schema(description = "Ngày tạo", example = "2026-09-01T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Ngày cập nhật", example = "2026-10-03T10:35:00")
    private LocalDateTime updatedAt;
}
