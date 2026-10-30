package com.vocabularyai.admin.dto.request;

import jakarta.validation.Valid;
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
public class AiVocabTagApproveRequest {

    @NotEmpty(message = "Items list cannot be empty")
    @Valid
    private List<AiVocabTagApproveItem> items;

    @Builder.Default
    private boolean overwrite = true;
}
