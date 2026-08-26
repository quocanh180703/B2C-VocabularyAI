package com.vocabularyai.quiz.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vocabularyai.quiz.entity.QuizAttempt;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Quiz Attempt Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuizAttemptResponse {
    
    private Long id;
    private Long quizId;
    private Double score;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Boolean completed;
    private Long timeTaken; // in seconds
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime createdAt;
    
    public static QuizAttemptResponse fromEntity(QuizAttempt attempt) {
        if (attempt == null) {
            return null;
        }
        return QuizAttemptResponse.builder()
            .id(attempt.getId())
            .quizId(attempt.getQuiz().getId())
            .score(attempt.getScore())
            .totalQuestions(attempt.getTotalQuestions())
            .correctAnswers(attempt.getCorrectAnswers())
            .completed(attempt.isCompleted())
            .timeTaken(attempt.getTimeTaken())
            .startedAt(attempt.getStartedAt())
            .submittedAt(attempt.getSubmittedAt())
            .createdAt(attempt.getCreatedAt())
            .build();
    }
}
