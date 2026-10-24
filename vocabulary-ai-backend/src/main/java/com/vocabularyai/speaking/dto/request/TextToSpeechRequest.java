package com.vocabularyai.speaking.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TextToSpeechRequest - Request body cho TTS endpoints
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to generate TTS audio")
public class TextToSpeechRequest {
    
    @Schema(description = "Single word for pronunciation", example = "Vietnam")
    private String word;
    
    @Schema(description = "Phrase for pronunciation", example = "My name is John")
    private String phrase;

    @Schema(description = "Arbitrary text for synthesis", example = "Hello world")
    private String text;
    
    @Schema(description = "Language code (en-US, vi-VN, etc.)", example = "en-US")
    private String language;

    public String getEffectiveText() {
        if (text != null && !text.trim().isEmpty()) return text.trim();
        if (word != null && !word.trim().isEmpty()) return word.trim();
        return phrase != null ? phrase.trim() : "";
    }
}
