package com.vocabularyai.document.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateQuizFromDocumentRequest {

    private String title;

    @Builder.Default
    private String level = "B1";

    @Min(value = 5, message = "Số câu hỏi tối thiểu là 5")
    @Max(value = 20, message = "Số câu hỏi tối đa là 20")
    @Builder.Default
    private Integer questionCount = 10;

    private String extraPrompt;
}
