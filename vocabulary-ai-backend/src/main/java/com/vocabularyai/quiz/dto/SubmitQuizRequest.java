package com.vocabularyai.quiz.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Submit Quiz Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitQuizRequest {
    
    @NotEmpty(message = "Answers are required")
    @Valid
    private List<QuestionAnswer> answers;
    
    /**
     * Question answer DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionAnswer {
        
        private Long questionId;
        private Integer answer; // Selected option index (0-based)
    }
}
