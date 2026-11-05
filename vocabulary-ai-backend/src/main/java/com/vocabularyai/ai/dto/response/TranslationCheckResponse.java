package com.vocabularyai.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TranslationCheckResponse {
    private String originalText;
    private String correctedText;
    private Integer score;
    private List<GrammarError> errors;
    private List<GrammarSuggestion> suggestions;
    private String targetWord;
    private Boolean isTargetWordUsed;
    private String viPrompt;
    private String feedback;
    private String meaningMatch; // "MATCH", "PARTIAL", "MISMATCH"
    private String overall;      // "PASS", "FAIL"
}

