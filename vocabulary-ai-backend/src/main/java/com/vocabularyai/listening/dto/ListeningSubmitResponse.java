package com.vocabularyai.listening.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListeningSubmitResponse {
    private Long exerciseId;
    private Integer score;
    private int totalWords;
    private int correctWords;
    private int nearMissWords;
    private List<WordMatchResult> wordResults;
    private String transcript;
    private String userAnswer;
    private LocalDateTime completedAt;
}

