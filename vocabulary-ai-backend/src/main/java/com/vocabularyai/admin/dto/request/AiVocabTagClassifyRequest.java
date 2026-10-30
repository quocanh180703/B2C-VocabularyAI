package com.vocabularyai.admin.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiVocabTagClassifyRequest {

    @NotEmpty(message = "Lemmas list cannot be empty")
    @Size(max = 20, message = "Maximum 20 words per classification request")
    private List<String> lemmas;
}
