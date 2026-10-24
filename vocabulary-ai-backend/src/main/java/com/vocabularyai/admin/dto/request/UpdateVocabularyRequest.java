package com.vocabularyai.admin.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating existing vocabulary entry
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateVocabularyRequest {
    
    /**
     * The English word or phrase (optional for update)
     */
    private String word;
    
    /**
     * Meaning/translation (optional for update)
     */
    @JsonAlias({"meaning", "vietnameseMeaning"})
    private String meaning;
    
    /**
     * Part of speech (optional for update)
     */
    @Pattern(regexp = "NOUN|VERB|ADJECTIVE|ADVERB|PREPOSITION|PRONOUN|ARTICLE|CONJUNCTION|INTERJECTION", 
             message = "Invalid part of speech")
    private String partOfSpeech;
    
    /**
     * CEFR level (optional for update)
     */
    @Pattern(regexp = "A1|A2|B1|B2|C1|C2", 
             message = "CEFR level must be A1, A2, B1, B2, C1, or C2")
    @JsonAlias({"level", "cefr"})
    private String level;
    
    /**
     * IPA pronunciation or phonetic spelling
     */
    private String pronunciation;
    
    /**
     * Definition in English
     */
    private String definition;
    
    /**
     * Usage example sentence
     */
    private String example;
    
    /**
     * Audio pronunciation URL
     */
    private String audioUrl;
    
    /**
     * Associated image URL for visual learning
     */
    private String imageUrl;
}
