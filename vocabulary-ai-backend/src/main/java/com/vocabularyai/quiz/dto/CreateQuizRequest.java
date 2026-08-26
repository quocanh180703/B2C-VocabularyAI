package com.vocabularyai.quiz.dto;

import com.vocabularyai.common.validation.ValidationConstants;
import com.vocabularyai.quiz.entity.Quiz;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Create Quiz Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuizRequest {
    
    private Long categoryId;
    
    @NotBlank(message = ValidationConstants.QUIZ_TITLE_NOT_BLANK)
    @Size(
        min = 2,
        max = 255,
        message = ValidationConstants.QUIZ_TITLE_SIZE
    )
    private String title;
    
    private String description;
    private Quiz.QuizLevel level;
    private Integer timeLimit; // in seconds
}
