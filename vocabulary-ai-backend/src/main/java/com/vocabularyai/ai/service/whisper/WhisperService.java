package com.vocabularyai.ai.service.whisper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * WhisperService
 * 
 * Handles audio transcription using OpenAI Whisper API
 * Converts audio files (MP3, WAV, etc.) to text
 * 
 * Configuration:
 * - whisper.api-key: OpenAI API key
 * - whisper.model: Model (whisper-1)
 * 
 * Usage:
 * String transcript = whisperService.transcribeAudio(audioFile);
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WhisperService {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${whisper.api-key:}")
    private String apiKey;
    
    @Value("${whisper.model:tsa/groq/whisper-large-v3}")
    private String model;
    
    @Value("${whisper.url:https://api.vilao.ai/v1/audio/transcriptions}")
    private String apiUrl;
    
    private static final int MAX_FILE_SIZE = 25 * 1024 * 1024; // 25MB limit
    
    /**
     * Transcribe audio file to text using OpenAI Whisper
     * 
     * @param audioFile Audio file (MP3, WAV, M4A, FLAC, etc.)
     * @param language Optional: Language code (e.g., "en", "vi")
     * @return Transcribed text
     * @throws IllegalArgumentException if file too large or invalid
     * @throws RuntimeException if API call fails
     */
    public String transcribeAudio(MultipartFile audioFile, String language) {
        try {
            // Validate
            validateAudioFile(audioFile);
            
            log.info("🎤 Transcribing audio: {}, size: {} bytes, language: {}", 
                audioFile.getOriginalFilename(), 
                audioFile.getSize(),
                language);
            
            // Build request
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            
            // Ensure filename is preserved when uploading
            String filename = audioFile.getOriginalFilename() != null && !audioFile.getOriginalFilename().isEmpty()
                ? audioFile.getOriginalFilename()
                : "audio.mp3";
            org.springframework.core.io.ByteArrayResource fileResource = new org.springframework.core.io.ByteArrayResource(audioFile.getBytes()) {
                @Override
                public String getFilename() {
                    return filename;
                }
            };
            body.add("file", fileResource);
            body.add("model", model);
            
            if (language != null && !language.isEmpty()) {
                body.add("language", language);
            }
            
            HttpEntity<MultiValueMap<String, Object>> request = 
                new HttpEntity<>(body, headers);
            
            // Call API
            log.debug("📤 Calling Whisper API: {}", apiUrl);
            ResponseEntity<String> response = restTemplate.postForEntity(
                apiUrl,
                request,
                String.class
            );
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("❌ Whisper API error: {}", response.getStatusCode());
                throw new RuntimeException("Whisper API error: " + response.getStatusCode());
            }
            
            // Parse response
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            String transcript = responseJson.get("text").asText();
            
            log.info("✅ Transcription successful: {} characters", transcript.length());
            return transcript;
            
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            log.error("❌ Whisper API HTTP error - status: {}, body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Lỗi từ dịch vụ nhận diện giọng nói: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("❌ Transcription failed:", e);
            throw new RuntimeException("Failed to transcribe audio: " + e.getMessage(), e);
        }
    }
    
    /**
     * Overload: Transcribe without language hint
     */
    public String transcribeAudio(MultipartFile audioFile) {
        return transcribeAudio(audioFile, null);
    }
    
    /**
     * Transcribe audio file from disk path with language hint
     * 
     * @param audioFile File object (from disk)
     * @param language Optional: Language code (e.g., "en", "vi")
     * @return Transcribed text
     * @throws IllegalArgumentException if file too large or invalid
     * @throws RuntimeException if API call fails
     */
    public String transcribeAudio(java.io.File audioFile, String language) {
        try {
            log.info("🎤 Transcribing audio from file: {}, size: {} bytes, language: {}", 
                audioFile.getName(), 
                audioFile.length(),
                language);
            
            // Build request
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new org.springframework.core.io.FileSystemResource(audioFile));
            body.add("model", model);
            
            if (language != null && !language.isEmpty()) {
                body.add("language", language);
            }
            
            HttpEntity<MultiValueMap<String, Object>> request = 
                new HttpEntity<>(body, headers);
            
            // Call API
            log.debug("📤 Calling Whisper API: {}", apiUrl);
            ResponseEntity<String> response = restTemplate.postForEntity(
                apiUrl,
                request,
                String.class
            );
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("❌ Whisper API error: {}", response.getStatusCode());
                throw new RuntimeException("Whisper API error: " + response.getStatusCode());
            }
            
            // Parse response
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            String transcript = responseJson.get("text").asText();
            
            log.info("✅ Transcription successful: {} characters", transcript.length());
            return transcript;
            
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            log.error("❌ Whisper API HTTP error from path - status: {}, body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Lỗi từ dịch vụ nhận diện giọng nói: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("❌ Transcription failed:", e);
            throw new RuntimeException("Failed to transcribe audio: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validate audio file
     */
    private void validateAudioFile(MultipartFile audioFile) {
        if (audioFile == null || audioFile.isEmpty()) {
            throw new IllegalArgumentException("Audio file is empty");
        }
        
        if (audioFile.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                "Audio file too large: " + audioFile.getSize() + " bytes (max: " + MAX_FILE_SIZE + ")"
            );
        }
        
        // Validate file type
        String contentType = audioFile.getContentType();
        if (contentType != null && !isValidAudioType(contentType)) {
            log.warn("⚠️  Unusual audio content type: {}", contentType);
        }
    }
    
    /**
     * Check if content type is audio
     */
    private boolean isValidAudioType(String contentType) {
        return contentType != null && (
            contentType.startsWith("audio/") ||
            contentType.equals("application/octet-stream")
        );
    }
    
    /**
     * Check if API is configured
     */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty() && !apiKey.contains("placeholder");
    }
}
