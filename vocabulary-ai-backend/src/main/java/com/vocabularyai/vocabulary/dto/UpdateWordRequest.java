package com.vocabularyai.vocabulary.dto;

import com.vocabularyai.vocabulary.entity.CEFRLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Update Word Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWordRequest {
    
    private String pronunciation;
    private String phonetic;
    private String partOfSpeech;
    private String meaning;
    private String definition;
    private String example;
    private String audioUrl;
    private String imageUrl;
    private CEFRLevel level;
}
