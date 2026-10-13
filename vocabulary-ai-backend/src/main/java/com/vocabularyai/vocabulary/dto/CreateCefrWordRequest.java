package com.vocabularyai.vocabulary.dto;

import com.vocabularyai.vocabulary.entity.CEFRLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Create CEFR Word Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCefrWordRequest {
    
    @NotBlank(message = "Word lemma is required")
    @Size(min = 1, max = 100, message = "Word must be between 1 and 100 characters")
    private String lemma;
    
    @NotNull(message = "CEFR level is required")
    private CEFRLevel cefrLevel;
    
    @NotBlank(message = "Part of speech is required")
    private String partOfSpeech;
    
    private String pronunciation;
    private String definition;
    private String source;
    private String language;
}
