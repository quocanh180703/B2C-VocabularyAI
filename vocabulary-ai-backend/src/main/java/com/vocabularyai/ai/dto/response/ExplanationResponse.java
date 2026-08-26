package com.vocabularyai.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for vocabulary explanation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExplanationResponse {
    
    private Long wordId;
    private String word;
    private String explanation;
    private List<String> examples;
    private String etymology;
    private List<String> relatedWords;
    private String partOfSpeech;
}
