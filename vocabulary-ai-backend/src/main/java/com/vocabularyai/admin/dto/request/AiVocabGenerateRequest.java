package com.vocabularyai.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiVocabGenerateRequest {
    @NotBlank(message = "Word cannot be blank")
    private String word;
    private String levelHint;
}
