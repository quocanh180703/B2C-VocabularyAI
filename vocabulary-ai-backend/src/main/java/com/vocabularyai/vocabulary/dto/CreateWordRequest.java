package com.vocabularyai.vocabulary.dto;

import com.vocabularyai.common.validation.ValidationConstants;
import com.vocabularyai.vocabulary.entity.Word;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Create Word Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWordRequest {
    
    @NotNull(message = "Category ID is required")
    private Long categoryId;
    
    @NotBlank(message = ValidationConstants.WORD_NOT_BLANK)
    @Size(
        min = 1,
        max = 255,
        message = ValidationConstants.WORD_SIZE
    )
    private String word;
    
    private String pronunciation;
    private String phonetic;
    private String partOfSpeech;
    
    @NotBlank(message = ValidationConstants.WORD_MEANING_NOT_BLANK)
    private String meaning;
    
    private String definition;
    private String example;
    private String audioUrl;
    private String imageUrl;
    
    @NotNull(message = ValidationConstants.WORD_LEVEL_NOT_NULL)
    private Word.WordLevel level;
}
