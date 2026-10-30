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
public class AiVocabTagApproveItem {

    @NotBlank(message = "wordId is required")
    private String wordId;

    @NotBlank(message = "topic is required")
    private String topic;
}
