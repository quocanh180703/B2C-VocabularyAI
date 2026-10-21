package com.vocabularyai.vocabulary.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vocabularyai.vocabulary.entity.Word;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * WordResponse DTO
 * 
 * API response for user vocabulary word details
 * Maps from Word entity (user-generated vocabulary) - NOT CEFR dataset
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "User vocabulary word item with meaning and examples")
public class WordResponse {

    @Schema(description = "Unique word ID", example = "1")
    private Long id;

    @Schema(description = "Word text", example = "serendipity")
    private String word;

    @Schema(description = "Meaning/translation", example = "The occurrence of events by chance in a happy or beneficial way")
    private String meaning;

    @Schema(description = "Part of speech", example = "noun")
    private String partOfSpeech;

    @Schema(description = "Definition in English")
    private String definition;

    @Schema(description = "CEFR level", example = "B2")
    private String level;

    @Schema(description = "Usage example")
    private String example;

    @Schema(description = "Pronunciation or phonetic spelling")
    private String pronunciation;

    @Schema(description = "Audio pronunciation URL")
    private String audioUrl;

    @Schema(description = "Associated image URL")
    private String imageUrl;

    @Schema(description = "Phonetic representation")
    private String phonetic;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Update timestamp")
    private LocalDateTime updatedAt;

    /**
     * Convert from Word entity (user vocabulary)
     */
    public static WordResponse fromEntity(Word word) {
        if (word == null) {
            return null;
        }
        return WordResponse.builder()
                .id(word.getId())
                .word(word.getWord())
                .meaning(word.getMeaning())
                .partOfSpeech(word.getPartOfSpeech())
                .pronunciation(word.getPronunciation())
                .definition(word.getDefinition())
                .level(word.getLevel())
                .example(word.getExample())
                .audioUrl(word.getAudioUrl())
                .imageUrl(word.getImageUrl())
                .phonetic(word.getPhonetic())
                .createdAt(word.getCreatedAt())
                .updatedAt(word.getUpdatedAt())
                .build();
    }

    /**
     * Convert from Word entity (minimal fields only)
     * Used for list responses to reduce payload size
     */
    public static WordResponse fromEntityMinimal(Word word) {
        if (word == null) {
            return null;
        }
        return WordResponse.builder()
                .id(word.getId())
                .word(word.getWord())
                .meaning(word.getMeaning())
                .partOfSpeech(word.getPartOfSpeech())
                .level(word.getLevel())
                .build();
    }
}
