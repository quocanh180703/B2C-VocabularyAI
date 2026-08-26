package com.vocabularyai.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for a single grammar error
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrammarError {
    
    private Integer startOffset;
    private Integer endOffset;
    private String errorType;
    private String suggestion;
    private String explanation;
}
