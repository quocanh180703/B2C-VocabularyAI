package com.vocabularyai.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckTranslationRequest {
    @NotBlank(message = "Translation text cannot be blank")
    @Size(min = 1, max = 10000)
    private String text;

    @NotBlank(message = "Vietnamese prompt cannot be blank")
    private String viPrompt;

    @NotBlank(message = "Target word cannot be blank")
    private String targetWord;

    private String targetMeaning;

    @Builder.Default
    private String mode = "SENTENCE";
}

