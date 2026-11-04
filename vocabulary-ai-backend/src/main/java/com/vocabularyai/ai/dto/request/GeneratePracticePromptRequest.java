package com.vocabularyai.ai.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneratePracticePromptRequest {
    @NotNull(message = "wordId is required")
    private Long wordId;

    @Builder.Default
    private String mode = "SENTENCE";

    private String level;

    /**
     * Set true khi nguoi dung chu dong bam "Doi de khac"
     * Neu false/null: uu tien lay de da co trong DB neu co >= 1 de (ton 0 token)
     */
    private Boolean forceNew;
}

