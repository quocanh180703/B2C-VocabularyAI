package com.vocabularyai.learning.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vocabularyai.learning.entity.LearningHistory;
import com.vocabularyai.vocabulary.dto.WordResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Learning History Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LearningHistoryResponse {
    
    private Long id;
    private WordResponse word;
    private String activityType;
    private Boolean isCorrect;
    private LocalDateTime createdAt;
    
    public static LearningHistoryResponse fromEntity(LearningHistory history) {
        if (history == null) {
            return null;
        }
        return LearningHistoryResponse.builder()
            .id(history.getId())
            .word(WordResponse.fromEntity(history.getWord()))
            .activityType(history.getActivityType().name())
            .isCorrect(history.getIsCorrect())
            .createdAt(history.getCreatedAt())
            .build();
    }
}
