package com.vocabularyai.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiVocabAuditResponse {
    private long totalWords;
    private long wordsMissingMeaning;
    private long wordsMissingExample;
    private long wordsMissingPronunciation;
    private long wordsMissingDefinition;
    private double completenessRate;
}
