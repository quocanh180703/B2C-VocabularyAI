package com.vocabularyai.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrammarHistorySummaryResponse {
    private Long id;
    private String mode; // FREE, TOPIC, TRANSLATION
    private String label;
    private Integer score;
    private String preview;
    private LocalDateTime createdAt;
}

