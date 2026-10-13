package com.vocabularyai.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Chi tiết câu hỏi")
public class QuestionDetailResponse {
    
    @Schema(description = "ID câu hỏi", example = "1")
    private Long id;
    
    @Schema(description = "Quiz ID", example = "5")
    private Long quizId;
    
    @Schema(description = "Nội dung câu hỏi", example = "Thủ đô của Pháp là?")
    private String content;
    
    @Schema(description = "Danh sách lựa chọn")
    private List<String> options;
    
    @Schema(description = "Index đáp án đúng", example = "0")
    private Integer correctAnswer;
    
    @Schema(description = "Đáp án đúng", example = "Paris")
    private String correctOptionText;
    
    @Schema(description = "Giải thích đáp án")
    private String explanation;
    
    @Schema(description = "Ngày tạo", example = "2026-10-01T10:30:00")
    private LocalDateTime createdAt;
}
