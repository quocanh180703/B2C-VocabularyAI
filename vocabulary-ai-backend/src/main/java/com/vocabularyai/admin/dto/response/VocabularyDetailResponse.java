package com.vocabularyai.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Admin Vocabulary Detail Response
 * 
 * Complete DTO for vocabulary detail endpoint
 * Includes all word information and metadata
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VocabularyDetailResponse implements Serializable {
    
    @Schema(description = "Word ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;
    
    @Schema(description = "Word (lemma)", example = "beautiful")
    private String word;
    
    @Schema(description = "Pronunciation (IPA)", example = "/ˈbjuːtɪfl/")
    private String pronunciation;
    
    @Schema(description = "Part of speech", example = "ADJECTIVE")
    private String partOfSpeech;
    
    @Schema(description = "CEFR level", example = "B1")
    private String cefr;
    
    @Schema(description = "English definition", example = "pleasing to look at or listen to; of a high standard")
    private String definition;
    
    @Schema(description = "Source dataset", example = "oxford_3000_5000")
    private String source;
    
    @Schema(description = "Language code", example = "en")
    private String language;
    
    @Schema(description = "Related words (synonyms)", example = "[\"gorgeous\", \"lovely\", \"attractive\"]")
    private List<String> relatedWords;
    
    @Schema(description = "Usage count in learning system", example = "1247")
    private Integer usageCount;
    
    @Schema(description = "Student mastery rate (percentage)", example = "82")
    private Integer masteryRate;
    
    @Schema(description = "Created date (ISO 8601)", example = "2024-01-15T10:30:00")
    private String createdAt;
    
    @Schema(description = "Updated date (ISO 8601)", example = "2024-02-20T15:45:30")
    private String updatedAt;
}
