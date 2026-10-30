package com.vocabularyai.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiVocabTagClassifyItemDto {

    private String existingWordId;
    private String word;
    private String topic;
    private Double confidence;
    private String reason;
}
