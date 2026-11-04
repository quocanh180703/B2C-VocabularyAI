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
public class TopicGrammarCheckResponse {
    private String originalText;
    private String correctedText;
    private Integer score;
    private List<GrammarError> errors;
    private List<GrammarSuggestion> suggestions;
    private TopicOutlineReport outlineReport;
}

