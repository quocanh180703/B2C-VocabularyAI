package com.vocabularyai.speaking.service.stt;

import com.vocabularyai.ai.service.whisper.WhisperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Speech-to-Text implementation using OpenAI Whisper API.
 * 
 * This service implements the SpeechToTextService interface using WhisperService
 * to transcribe audio files for Speaking Practice.
 * 
 * Configuration:
 * - whisper.api-key: OpenAI API key (required)
 * - whisper.model: Model name (default: whisper-1)
 * - speaking.speech-to-text.provider: Set to "whisper" to use this provider
 * 
 * Features:
 * - High accuracy audio transcription
 * - Supports multiple audio formats (MP3, WAV, M4A, FLAC, etc.)
 * - Language detection and optional language hint
 * - Confidence score estimation
 * - Error handling and validation
 */
@Slf4j
@Service
@Primary
@ConditionalOnProperty(name = "speaking.speech-to-text.provider", havingValue = "whisper")
@RequiredArgsConstructor
public class WhisperSpeechToTextService implements SpeechToTextService {
    
    private final WhisperService whisperService;
    
    /**
     * Transcribe audio file from MultipartFile (used during upload)
     */
    @Override
    public TranscriptResult transcribe(MultipartFile audioFile) {
        return transcribe(audioFile, "en-US");
    }
    
    /**
     * Transcribe audio file from MultipartFile with language code
     */
    @Override
    public TranscriptResult transcribe(MultipartFile audioFile, String languageCode) {
        log.info("🎤 Transcribing audio via Whisper - file: {}, language: {}", 
                audioFile.getOriginalFilename(), languageCode);
        
        try {
            // Extract language code (e.g., "vi-VN" -> "vi")
            String language = extractLanguageCode(languageCode);
            
            // Call WhisperService
            String transcript = whisperService.transcribeAudio(audioFile, language);
            
            if (transcript == null || transcript.trim().isEmpty()) {
                log.warn("⚠️  Empty transcript returned");
                throw new SpeechToTextException("Không thể nhận diện giọng nói. Vui lòng nói rõ hơn.");
            }
            
            log.info("✅ Transcription successful: {} characters", transcript.length());
            
            // Return result with confidence estimate
            return TranscriptResult.builder()
                    .transcript(transcript)
                    .confidence(0.95) // Whisper doesn't provide confidence, use high default
                    .provider("openai_whisper")
                    .languageCode(language)
                    .build();
                    
        } catch (IllegalArgumentException e) {
            log.error("❌ Audio validation failed: {}", e.getMessage());
            throw new SpeechToTextException("File audio không hợp lệ: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ Transcription failed: {}", e.getMessage(), e);
            throw new SpeechToTextException("Lỗi transcription: " + e.getMessage(), e);
        }
    }
    
    /**
     * Transcribe audio file from disk path (used after file is saved)
     */
    @Override
    public TranscriptResult transcribeFromPath(String audioFilePath) {
        return transcribeFromPath(audioFilePath, "en-US");
    }
    
    /**
     * Transcribe audio file from disk path with language code
     */
    @Override
    public TranscriptResult transcribeFromPath(String audioFilePath, String languageCode) {
        log.info("🎤 Transcribing audio from path via Whisper - path: {}, language: {}", 
                audioFilePath, languageCode);
        
        try {
            // Verify file exists
            File audioFile = new File(audioFilePath);
            if (!audioFile.exists()) {
                log.error("❌ Audio file not found: {}", audioFilePath);
                throw new SpeechToTextException("File audio không tồn tại: " + audioFilePath);
            }
            
            // Extract language code
            String language = extractLanguageCode(languageCode);
            
            // Call WhisperService directly with File object
            String transcript = whisperService.transcribeAudio(audioFile, language);
            
            if (transcript == null || transcript.trim().isEmpty()) {
                log.warn("⚠️  Empty transcript returned from file path");
                throw new SpeechToTextException("Không thể nhận diện giọng nói. Vui lòng nói rõ hơn.");
            }
            
            log.info("✅ Transcription successful from path: {} characters", transcript.length());
            
            // Return result
            return TranscriptResult.builder()
                    .transcript(transcript)
                    .confidence(0.95)
                    .provider("openai_whisper")
                    .languageCode(language)
                    .build();
                    
        } catch (SpeechToTextException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ Transcription from path failed: {}", e.getMessage(), e);
            throw new SpeechToTextException("Lỗi transcription: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get provider type
     */
    @Override
    public String getProviderType() {
        return "openai_whisper";
    }
    
    /**
     * Check if service is healthy
     */
    @Override
    public boolean isHealthy() {
        try {
            return whisperService.isConfigured();
        } catch (Exception e) {
            log.warn("Whisper service health check failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Extract language code from full locale (e.g., "vi-VN" -> "vi")
     */
    private String extractLanguageCode(String languageCode) {
        if (languageCode == null || languageCode.isEmpty()) {
            return null;
        }
        
        // Split on hyphen and take first part
        String[] parts = languageCode.split("-");
        return parts.length > 0 ? parts[0].toLowerCase() : null;
    }
}
