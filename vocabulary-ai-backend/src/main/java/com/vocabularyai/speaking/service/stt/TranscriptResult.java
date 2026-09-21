package com.vocabularyai.speaking.service.stt;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of Speech-to-Text transcription.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptResult {

    /**
     * Transcribed text from audio
     */
    @JsonProperty("transcript")
    private String transcript;

    /**
     * Confidence score (0-100)
     * Higher = more confident in the transcription
     */
    @JsonProperty("confidence")
    private Double confidence;

    /**
     * Language code detected (e.g., en-US)
     */
    @JsonProperty("languageCode")
    private String languageCode;

    /**
     * Duration of audio in seconds
     */
    @JsonProperty("durationSeconds")
    private Integer durationSeconds;

    /**
     * STT provider used (gemini, google_cloud, azure, aws)
     */
    @JsonProperty("provider")
    private String provider;

    /**
     * Processing time in milliseconds
     */
    @JsonProperty("processingTimeMs")
    private Long processingTimeMs;

    /**
     * Whether transcription is successful
     */
    @JsonProperty("success")
    private Boolean success;

    /**
     * Error message if transcription failed
     */
    @JsonProperty("errorMessage")
    private String errorMessage;
}
