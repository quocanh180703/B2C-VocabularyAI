package com.vocabularyai.speaking.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * TextToSpeechResponse - Response từ TTS endpoints
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Text-to-Speech audio generation response")
public class TextToSpeechResponse {
    
    @Schema(description = "Single word (if request was for word)", example = "Vietnam")
    private String word;
    
    @Schema(description = "Phrase (if request was for phrase)", example = "My name is John")
    private String phrase;
    
    @Schema(description = "Topic (if request was for session reference)", example = "Giới thiệu bản thân")
    private String topic;
    
    @Schema(description = "Session ID (for reference endpoint)", example = "1")
    private Long sessionId;
    
    @Schema(description = "URL to access the generated audio file")
    private String audioUrl;
    
    @Schema(description = "Audio file format", example = "mp3")
    private String audioFormat;
    
    @Schema(description = "Duration of audio in seconds", example = "1.2")
    private Double durationSeconds;
    
    @Schema(description = "Voice used for synthesis", example = "en-US-Neural2-A")
    private String voice;
    
    @Schema(description = "Language code used", example = "en-US")
    private String language;
    
    @Schema(description = "TTS provider used", example = "gemini")
    private String provider;
    
    @Schema(description = "Processing time in milliseconds", example = "450")
    private Long processingTimeMs;
    
    @Schema(description = "Whether synthesis was successful", example = "true")
    private Boolean success;
    
    @Schema(description = "Timestamp when audio was created")
    private LocalDateTime createdAt;
}
