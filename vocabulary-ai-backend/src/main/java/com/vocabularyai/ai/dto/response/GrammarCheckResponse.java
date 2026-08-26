package com.vocabularyai.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for grammar checking result
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrammarCheckResponse {
    
    private String originalText;
    private String correctedText;
    private List<GrammarError> errors;
    private List<GrammarSuggestion> suggestions;
}
