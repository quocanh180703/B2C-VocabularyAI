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
public class GrammarHistoryDetailResponse {
    private Long id;
    private String mode;
    private String label;
    private Integer score;
    private String inputText;
    private String contextJson;
    private String resultJson;
    private LocalDateTime createdAt;
}

