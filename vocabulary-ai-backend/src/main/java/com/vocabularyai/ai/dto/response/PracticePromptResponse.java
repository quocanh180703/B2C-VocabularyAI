package com.vocabularyai.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PracticePromptResponse {
    private Long id;
    private Long wordId;
    private String targetWord;
    private String targetMeaning;
    private String mode;
    private String viText;
}

