package com.vocabularyai.admin.dto.request;

import com.vocabularyai.admin.dto.response.AiGeneratedWordDto;
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
public class AiVocabApproveRequest {
    @NotEmpty(message = "Approved words list cannot be empty")
    private List<AiGeneratedWordDto> words;

    @Builder.Default
    private boolean overwriteExisting = true;
}
