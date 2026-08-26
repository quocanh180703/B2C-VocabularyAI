package com.vocabularyai.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for a grammar suggestion
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrammarSuggestion {
    
    private String errorType;
    private String suggestion;
    private String explanation;
}
