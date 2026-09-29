package com.vocabularyai.speaking.service.stt;

import org.springframework.web.multipart.MultipartFile;

/**
 * Interface for Speech-to-Text (STT) services.
 * Abstracts different STT providers (Gemini, Google Cloud, Azure, AWS).
 */
public interface SpeechToTextService {

    /**
     * Transcribe audio file to text using default language (en-US)
     *
     * @param audioFile Audio file to transcribe
     * @return TranscriptResult with transcribed text and metadata
     * @throws SpeechToTextException if transcription fails
     */
    TranscriptResult transcribe(MultipartFile audioFile);

    /**
     * Transcribe audio file to text with specified language code
     *
     * @param audioFile Audio file to transcribe
     * @param languageCode Language code (e.g., en-US, vi-VN, fr-FR)
     * @return TranscriptResult with transcribed text and metadata
     * @throws SpeechToTextException if transcription fails
     */
    TranscriptResult transcribe(MultipartFile audioFile, String languageCode);

    /**
     * Transcribe audio file from disk path to text with default language
     *
     * @param audioFilePath Path to audio file on disk
     * @return TranscriptResult with transcribed text and metadata
     * @throws SpeechToTextException if transcription fails
     */
    TranscriptResult transcribeFromPath(String audioFilePath);

    /**
     * Transcribe audio file from disk path to text with specified language code
     *
     * @param audioFilePath Path to audio file on disk
     * @param languageCode Language code (e.g., en-US, vi-VN, fr-FR)
     * @return TranscriptResult with transcribed text and metadata
     * @throws SpeechToTextException if transcription fails
     */
    TranscriptResult transcribeFromPath(String audioFilePath, String languageCode);

    /**
     * Get the STT provider type
     *
     * @return Provider name (gemini, google_cloud, azure, aws)
     */
    String getProviderType();

    /**
     * Check if STT service is healthy/available
     *
     * @return true if service is available, false otherwise
     */
    boolean isHealthy();
}
