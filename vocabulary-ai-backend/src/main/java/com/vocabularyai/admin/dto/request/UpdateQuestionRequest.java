package com.vocabularyai.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Cập nhật câu hỏi")
public class UpdateQuestionRequest {
    
    @Size(min = 5, max = 2000)
    @Schema(description = "Nội dung câu hỏi", example = "Thủ đô của Pháp là?")
    private String content;
    
    @Size(min = 2, max = 10)
    @Schema(description = "Danh sách lựa chọn", example = "[\"Paris\", \"London\", \"Berlin\"]")
    private List<String> options;
    
    @Min(0)
    @Schema(description = "Index đáp án đúng", example = "0")
    private Integer correctAnswer;
    
    @Size(max = 1000)
    @Schema(description = "Giải thích đáp án")
    private String explanation;
}
