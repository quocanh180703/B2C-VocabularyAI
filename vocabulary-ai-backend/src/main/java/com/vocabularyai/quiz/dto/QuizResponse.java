package com.vocabularyai.quiz.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vocabularyai.quiz.entity.Quiz;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Quiz Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuizResponse {
    
    private Long id;
    private Long categoryId;
    private String title;
    private String description;
    private String level;
    private Integer timeLimit;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static QuizResponse fromEntity(Quiz quiz) {
        if (quiz == null) {
            return null;
        }
        return QuizResponse.builder()
            .id(quiz.getId())
            .categoryId(quiz.getCategory() != null ? quiz.getCategory().getId() : null)
            .title(quiz.getTitle())
            .description(quiz.getDescription())
            .level(quiz.getLevel() != null ? quiz.getLevel().name() : null)
            .timeLimit(quiz.getTimeLimit())
            .status(quiz.getStatus().name())
            .createdAt(quiz.getCreatedAt())
            .updatedAt(quiz.getUpdatedAt())
            .build();
    }
}
