package com.vocabularyai.vocabulary.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vocabularyai.vocabulary.entity.Word;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Word Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WordResponse {
    
    private Long id;
    private Long categoryId;
    private String word;
    private String pronunciation;
    private String phonetic;
    private String partOfSpeech;
    private String meaning;
    private String definition;
    private String example;
    private String audioUrl;
    private String imageUrl;
    private String level;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static WordResponse fromEntity(Word word) {
        if (word == null) {
            return null;
        }
        return WordResponse.builder()
            .id(word.getId())
            .categoryId(word.getCategory().getId())
            .word(word.getWord())
            .pronunciation(word.getPronunciation())
            .phonetic(word.getPhonetic())
            .partOfSpeech(word.getPartOfSpeech())
            .meaning(word.getMeaning())
            .definition(word.getDefinition())
            .example(word.getExample())
            .audioUrl(word.getAudioUrl())
            .imageUrl(word.getImageUrl())
            .level(word.getLevel().name())
            .createdAt(word.getCreatedAt())
            .updatedAt(word.getUpdatedAt())
            .build();
    }
}
