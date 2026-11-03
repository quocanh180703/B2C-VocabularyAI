package com.vocabularyai.listening.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateListeningResponse {
    private Long exerciseId;
    private String topic;
    private String level;
    private String voice;
    private String audioUrl;
}

