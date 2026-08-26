package com.vocabularyai.quiz.dto;

import com.vocabularyai.common.validation.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Create Question Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuestionRequest {
    
    @NotBlank(message = ValidationConstants.QUESTION_CONTENT_NOT_BLANK)
    private String content;
    
    @NotEmpty(message = ValidationConstants.QUESTION_OPTIONS_NOT_EMPTY)
    private List<String> options;
    
    @NotNull(message = ValidationConstants.QUESTION_CORRECT_ANSWER_NOT_NULL)
    private Integer correctAnswer;
    
    private String explanation;
}
