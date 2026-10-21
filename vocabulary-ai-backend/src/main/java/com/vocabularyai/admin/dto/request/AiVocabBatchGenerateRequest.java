package com.vocabularyai.admin.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiVocabBatchGenerateRequest {
    @NotEmpty(message = "Words list cannot be empty")
    private List<String> words;
    private String levelHint;
}
