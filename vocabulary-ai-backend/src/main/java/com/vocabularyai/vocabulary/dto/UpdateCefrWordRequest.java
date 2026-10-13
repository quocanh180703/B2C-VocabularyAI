package com.vocabularyai.vocabulary.dto;

import com.vocabularyai.vocabulary.entity.CEFRLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Update CEFR Word Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCefrWordRequest {
    
    private CEFRLevel cefrLevel;
    private String partOfSpeech;
    private String pronunciation;
    private String definition;
    private String source;
}
