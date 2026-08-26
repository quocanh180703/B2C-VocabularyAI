package com.vocabularyai.learning.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vocabularyai.learning.entity.LearningProgress;
import com.vocabularyai.vocabulary.dto.WordResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Learning Progress Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LearningProgressResponse {
    
    private Long id;
    private WordResponse word;
    private String status;
    private Integer correctCount;
    private Integer wrongCount;
    private Integer reviewCount;
    private Double accuracy;
    private Boolean needsReview;
    private LocalDateTime lastReviewedAt;
    private LocalDateTime nextReviewAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static LearningProgressResponse fromEntity(LearningProgress progress) {
        if (progress == null) {
            return null;
        }
        return LearningProgressResponse.builder()
            .id(progress.getId())
            .word(WordResponse.fromEntity(progress.getWord()))
            .status(progress.getStatus().name())
            .correctCount(progress.getCorrectCount())
            .wrongCount(progress.getWrongCount())
            .reviewCount(progress.getReviewCount())
            .accuracy(progress.getAccuracy())
            .needsReview(progress.needsReview())
            .lastReviewedAt(progress.getLastReviewedAt())
            .nextReviewAt(progress.getNextReviewAt())
            .createdAt(progress.getCreatedAt())
            .updatedAt(progress.getUpdatedAt())
            .build();
    }
}
