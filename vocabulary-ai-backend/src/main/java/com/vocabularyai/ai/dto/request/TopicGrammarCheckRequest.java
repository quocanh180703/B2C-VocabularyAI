package com.vocabularyai.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicGrammarCheckRequest {

    @NotBlank(message = "Text cannot be blank")
    @Size(min = 1, max = 10000, message = "Text must be between 1 and 10000 characters")
    private String text;

    private String topic;

    private String level;

    private List<String> suggestedWords;

    private List<String> structures;
}

