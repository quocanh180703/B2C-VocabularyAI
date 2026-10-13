package com.vocabularyai.vocabulary.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vocabularyai.vocabulary.entity.CEFRLevel;
import com.vocabularyai.vocabulary.entity.CefrWord;
import com.vocabularyai.vocabulary.entity.PartOfSpeech;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * CefrWordResponse DTO
 * 
 * API response for CEFR word details
 * Maps from CefrWord entity with optional fields
 * 
 * Implements Serializable for caching support
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "CEFR word vocabulary item with CEFR level and definitions")
public class CefrWordResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "Unique word ID (UUID)", example = "00092354-13f5-47aa-94c6-3312579db388")
    private String id;

    @Schema(description = "Word text/lemma", example = "difference")
    private String lemma;

    @Schema(description = "CEFR level (A1-C1)", example = "A1")
    private CEFRLevel cefrLevel;

    @Schema(description = "Part of speech", example = "NOUN")
    private PartOfSpeech partOfSpeech;

    @Schema(description = "IPA pronunciation", example = "/ˈdɪfrəns/")
    private String pronunciation;

    @Schema(description = "Word definition in English")
    private String definition;

    @Schema(description = "Source of definition", example = "oxford_3000_5000")
    private String source;

    @Schema(description = "Language code", example = "en")
    private String language;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Update timestamp")
    private LocalDateTime updatedAt;

    /**
     * Convert from CefrWord entity
     */
    public static CefrWordResponse fromEntity(CefrWord word) {
        return CefrWordResponse.builder()
                .id(word.getId())
                .lemma(word.getLemma())
                .cefrLevel(word.getCefrLevel())
                .partOfSpeech(word.getPartOfSpeech())
                .pronunciation(word.getPronunciation())
                .definition(word.getDefinition())
                .source(word.getSource())
                .language(word.getLanguage())
                .createdAt(word.getCreatedAt())
                .updatedAt(word.getUpdatedAt())
                .build();
    }

    /**
     * Convert from CefrWord entity (minimal fields only)
     * 
     * Used for list responses to reduce payload size
     */
    public static CefrWordResponse fromEntityMinimal(CefrWord word) {
        return CefrWordResponse.builder()
                .id(word.getId())
                .lemma(word.getLemma())
                .cefrLevel(word.getCefrLevel())
                .partOfSpeech(word.getPartOfSpeech())
                .pronunciation(word.getPronunciation())
                .build();
    }
}
