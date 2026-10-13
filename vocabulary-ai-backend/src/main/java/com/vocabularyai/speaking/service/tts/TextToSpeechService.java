package com.vocabularyai.speaking.service.tts;

/**
 * Interface for Text-to-Speech (TTS) services.
 * Abstracts different TTS providers (Gemini, Google Cloud, Azure, AWS).
 */
public interface TextToSpeechService {

    /**
     * Synthesize speech from text using default language (en-US) and voice
     *
     * @param text Text to synthesize
     * @return TextToSpeechResult with audio URL and metadata
     * @throws TextToSpeechException if synthesis fails
     */
    TextToSpeechResult synthesize(String text);

    /**
     * Synthesize speech from text with specified language code
     *
     * @param text Text to synthesize
     * @param languageCode Language code (e.g., en-US, vi-VN, fr-FR)
     * @return TextToSpeechResult with audio URL and metadata
     * @throws TextToSpeechException if synthesis fails
     */
    TextToSpeechResult synthesize(String text, String languageCode);

    /**
     * Synthesize speech from text with specified language and voice
     *
     * @param text Text to synthesize
     * @param languageCode Language code (e.g., en-US, vi-VN)
     * @param voice Voice identifier (e.g., en-US-Neural2-C)
     * @return TextToSpeechResult with audio URL and metadata
     * @throws TextToSpeechException if synthesis fails
     */
    TextToSpeechResult synthesize(String text, String languageCode, String voice);

    /**
     * Get the TTS provider type
     *
     * @return Provider name (e.g., "gemini", "google-cloud", "azure", "disabled")
     */
    String getProviderType();

    /**
     * Check if TTS service is healthy and operational
     *
     * @return true if service is operational, false otherwise
     */
    boolean isHealthy();
}
