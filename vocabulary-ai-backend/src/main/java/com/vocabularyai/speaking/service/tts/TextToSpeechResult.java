package com.vocabularyai.speaking.service.tts;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of Text-to-Speech conversion.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextToSpeechResult {

    /**
     * Original text that was converted to speech
     */
    @JsonProperty("text")
    private String text;

    /**
     * URL to the generated audio file
     */
    @JsonProperty("audioUrl")
    private String audioUrl;

    /**
     * MIME type of audio file (audio/mp3, audio/wav, etc.)
     */
    @JsonProperty("audioFormat")
    private String audioFormat;

    /**
     * Duration of generated audio in seconds
     */
    @JsonProperty("durationSeconds")
    private Double durationSeconds;

    /**
     * Voice used for TTS (e.g., en-US-Neural2-C, en-US-Standard-A)
     */
    @JsonProperty("voice")
    private String voice;

    /**
     * Language code (e.g., en-US, vi-VN)
     */
    @JsonProperty("languageCode")
    private String languageCode;

    /**
     * TTS provider used (google_cloud, gemini, azure, aws)
     */
    @JsonProperty("provider")
    private String provider;

    /**
     * Processing time in milliseconds
     */
    @JsonProperty("processingTimeMs")
    private Long processingTimeMs;

    /**
     * Whether TTS generation is successful
     */
    @JsonProperty("success")
    private Boolean success;

    /**
     * Error message if TTS generation failed
     */
    @JsonProperty("errorMessage")
    private String errorMessage;
}
