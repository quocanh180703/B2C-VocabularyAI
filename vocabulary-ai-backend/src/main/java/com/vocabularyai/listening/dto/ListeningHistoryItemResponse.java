package com.vocabularyai.listening.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListeningHistoryItemResponse {
    private Long id;
    private String topic;
    private String level;
    private String voice;
    private Integer score;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}

