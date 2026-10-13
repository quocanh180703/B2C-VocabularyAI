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
    
    @Schema(description = "Language code (en-US, vi-VN, etc.)", example = "en-US")
    private String language;
}
