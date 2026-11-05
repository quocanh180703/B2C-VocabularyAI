package com.vocabularyai.vocabulary.custom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomWordResponse {

    private Long id;
    private Long userId;
    private String lemma;
    private String vietnameseMeaning;
    private String partOfSpeech;
    private String example;
    private String exampleTranslation;
    private String cefrLevel;
    private String topic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
