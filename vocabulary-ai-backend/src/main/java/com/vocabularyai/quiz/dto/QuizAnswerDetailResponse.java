package com.vocabularyai.quiz.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vocabularyai.quiz.entity.QuizAnswer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Quiz Answer Detail Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuizAnswerDetailResponse {
    
    private Long questionId;
    private String content;
    private Integer selectedAnswer;
    private Integer correctAnswer;
    private Boolean isCorrect;
    private String explanation;
    
    public static QuizAnswerDetailResponse fromEntity(QuizAnswer answer) {
        if (answer == null) {
            return null;
        }
        return QuizAnswerDetailResponse.builder()
            .questionId(answer.getQuestion().getId())
            .content(answer.getQuestion().getContent())
            .selectedAnswer(answer.getAnswer())
            .correctAnswer(answer.getQuestion().getCorrectAnswer())
            .isCorrect(answer.getCorrect())
            .explanation(answer.getQuestion().getExplanation())
            .build();
    }
}
