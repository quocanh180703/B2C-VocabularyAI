package com.vocabularyai.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Admin Vocabulary List Response
 * 
 * Minimal DTO for vocabulary list endpoints
 * Contains essential fields for table display
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VocabularyListResponse implements Serializable {
    
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
    
    @Schema(description = "Meaning / Vietnamese meaning", example = "sự khác biệt")
    private String meaning;
    
    @Schema(description = "Vietnamese meaning", example = "sự khác biệt, điều khác nhau")
    private String vietnameseMeaning;
    
    @Schema(description = "English definition", example = "The quality of being different.")
    private String definition;
    
    @Schema(description = "Source dataset", example = "oxford_3000_5000")
    private String source;
    
    @Schema(description = "Created date (ISO 8601)", example = "2024-01-15T10:30:00")
    private String createdAt;
}
