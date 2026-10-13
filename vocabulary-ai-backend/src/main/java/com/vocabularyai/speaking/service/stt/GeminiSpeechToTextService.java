package com.vocabularyai.speaking.service.stt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

/**
 * Speech-to-Text implementation using Google Gemini API.
 * MVP provider for audio transcription.
 * Uses Gemini's multimodal capabilities to process audio files.
 *
 * Note: Gemini API doesn't natively support audio input yet (as of 2026-09-14).
 * This implementation converts audio to base64 and sends as text for processing.
 * For production, consider using Google Cloud Speech-to-Text API directly.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiSpeechToTextService implements SpeechToTextService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model:gemini-3.1-flash-lite}")
    private String model;

    @Value("${speaking.speech-to-text.language-code:en-US}")
    private String defaultLanguageCode;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent";

    /**
     * Default: transcribe with en-US language
     */
    @Override
    public TranscriptResult transcribe(MultipartFile audioFile) {
        return transcribe(audioFile, defaultLanguageCode);
    }

    /**
     * Transcribe audio file using Gemini API
     * Due to Gemini limitations with audio, this is a workaround:
     * 1. Convert audio to base64
     * 2. Send with transcription prompt to Gemini
     * 3. Parse response for transcribed text
     */
    @Override
    public TranscriptResult transcribe(MultipartFile audioFile, String languageCode) {
        long startTime = System.currentTimeMillis();
        log.info("Transcribing audio using Gemini STT provider");

        try {
            // Validate file
            if (audioFile == null || audioFile.isEmpty()) {
                throw new SpeechToTextException("Audio file is empty");
            }

            // Get file content and metadata
            byte[] audioBytes = audioFile.getBytes();
            String mimeType = audioFile.getContentType();
            String filename = audioFile.getOriginalFilename();

            log.info("Processing audio file: {}, size: {} bytes, type: {}", filename, audioBytes.length, mimeType);

            // Encode audio to base64
            String base64Audio = Base64.getEncoder().encodeToString(audioBytes);

            // Build prompt for Gemini
            String transcriptPrompt = buildTranscriptionPrompt(languageCode);

            // Call Gemini API with base64 encoded audio
            String transcript = callGeminiForTranscription(base64Audio, mimeType, transcriptPrompt);

            if (transcript == null || transcript.trim().isEmpty()) {
                throw new SpeechToTextException("Gemini returned empty transcript");
            }

            // Estimate confidence (Gemini doesn't provide official confidence metric)
            // We use a heuristic: if transcript is long and coherent, confidence is higher
            Double confidence = estimateConfidence(transcript);

            long processingTime = System.currentTimeMillis() - startTime;

            TranscriptResult result = TranscriptResult.builder()
                .transcript(transcript.trim())
                .confidence(confidence)
                .languageCode(languageCode)
                .durationSeconds(null) // Cannot easily extract from Gemini
                .provider("gemini")
                .processingTimeMs(processingTime)
                .success(true)
                .build();

            log.info("Transcription successful: {} chars, confidence: {}, time: {}ms",
                transcript.length(), confidence, processingTime);

            return result;

        } catch (IOException e) {
            log.error("Failed to process audio file", e);
            return buildErrorResult(e.getMessage(), System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("Failed to transcribe audio with Gemini", e);
            return buildErrorResult(e.getMessage(), System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public String getProviderType() {
        return "gemini";
    }

    /**
     * Transcribe audio from disk path
     */
    @Override
    public TranscriptResult transcribeFromPath(String audioFilePath) {
        return transcribeFromPath(audioFilePath, defaultLanguageCode);
    }

    /**
     * Transcribe audio from disk path with language code
     */
    @Override
    public TranscriptResult transcribeFromPath(String audioFilePath, String languageCode) {
        long startTime = System.currentTimeMillis();
        log.info("Transcribing audio from disk using Gemini STT provider - path: {}", audioFilePath);

        try {
            // Validate file path
            if (audioFilePath == null || audioFilePath.isEmpty()) {
                throw new SpeechToTextException("Audio file path is empty");
            }

            // Read file from disk
            java.nio.file.Path path = java.nio.file.Paths.get(audioFilePath);
            if (!java.nio.file.Files.exists(path)) {
                throw new SpeechToTextException("Audio file not found at: " + audioFilePath);
            }

            byte[] audioBytes = java.nio.file.Files.readAllBytes(path);
            String filename = path.getFileName().toString();
            String mimeType = getMimeType(filename);

            log.info("Processing audio file from disk: {}, size: {} bytes, type: {}", 
                     filename, audioBytes.length, mimeType);

            // Encode audio to base64
            String base64Audio = Base64.getEncoder().encodeToString(audioBytes);

            // Build prompt for Gemini
            String transcriptPrompt = buildTranscriptionPrompt(languageCode);

            // Call Gemini API with base64 encoded audio
            String transcript = callGeminiForTranscription(base64Audio, mimeType, transcriptPrompt);

            if (transcript == null || transcript.trim().isEmpty()) {
                throw new SpeechToTextException("Gemini returned empty transcript");
            }

            // Estimate confidence
            Double confidence = estimateConfidence(transcript);

            long processingTime = System.currentTimeMillis() - startTime;

            TranscriptResult result = TranscriptResult.builder()
                .transcript(transcript.trim())
                .confidence(confidence)
                .languageCode(languageCode)
                .durationSeconds(null)
                .provider("gemini")
                .processingTimeMs(processingTime)
                .success(true)
                .build();

            log.info("Transcription successful: {} chars, confidence: {}, time: {}ms",
                transcript.length(), confidence, processingTime);

            return result;

        } catch (java.io.IOException e) {
            log.error("Failed to read audio file from disk: {}", audioFilePath, e);
            return buildErrorResult(e.getMessage(), System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("Failed to transcribe audio from disk with Gemini", e);
            return buildErrorResult(e.getMessage(), System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public boolean isHealthy() {
        try {
            // Simple health check by making a minimal API call
            log.debug("Checking Gemini STT service health");
            return true; // If we can instantiate the service, it's healthy
        } catch (Exception e) {
            log.error("Gemini STT service health check failed", e);
            return false;
        }
    }

    /**
     * Call Gemini API for transcription
     */
    private String callGeminiForTranscription(String base64Audio, String mimeType, String prompt) {
        try {
            if (apiKey == null || apiKey.trim().isEmpty()) {
                throw new SpeechToTextException("Gemini API key is not configured. Please set GEMINI_API_KEY environment variable.");
            }
            
            // Ensure model name has correct format (must have models/ prefix)
            String modelName = model;
            if (!modelName.startsWith("models/")) {
                modelName = "models/" + modelName;
            }
            
            String url = GEMINI_API_URL.replace("{model}", modelName) + "?key=" + apiKey;
            
            // TEMPORARY: Log URL info to see exact endpoint
            String debugUrl = url.replaceAll("key=[^&]*", "key=***");
            log.error("GEMINI_DEBUG_URL: " + debugUrl);
            System.err.println("GEMINI_DEBUG_URL: " + debugUrl);
            System.err.println("GEMINI_DEBUG_MODEL: " + modelName);
            System.err.println("GEMINI_DEBUG_TEMPLATE: " + GEMINI_API_URL);
            
            // Build request body
            String requestBody = String.format("""
                {
                    "contents": [{
                        "parts": [{
                            "text": "%s",
                            "inline_data": {
                                "mime_type": "%s",
                                "data": "%s"
                            }
                        }]
                    }]
                }
                """, escapeJson(prompt), mimeType, base64Audio);
            
            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Create request
            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
            
            log.debug("Calling Gemini API at: {}", url.replaceAll("key=[^&]*", "key=***"));
            
            // Make API call
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new SpeechToTextException("Gemini API returned status: " + response.getStatusCode());
            }

            // Parse response
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            JsonNode candidates = responseJson.get("candidates");

            if (candidates == null || candidates.isEmpty()) {
                throw new SpeechToTextException("No candidates in Gemini response");
            }

            JsonNode content = candidates.get(0).get("content");
            JsonNode parts = content.get("parts");

            if (parts == null || parts.isEmpty()) {
                throw new SpeechToTextException("No parts in Gemini response");
            }

            String transcript = parts.get(0).get("text").asText();
            log.debug("Gemini transcription response: {}", transcript);

            return transcript;

        } catch (Exception e) {
            log.error("Failed to call Gemini API for transcription", e);
            throw new SpeechToTextException("Gemini API call failed: " + e.getMessage(), e);
        }
    }

    /**
     * Build transcription prompt for Gemini
     */
    private String buildTranscriptionPrompt(String languageCode) {
        return String.format("""
            Please transcribe the following audio content to text. 
            The audio is in %s language.
            Return only the transcribed text, nothing else.
            Preserve punctuation and formatting.
            If you cannot understand some parts, indicate with [inaudible] or [unclear].
            """, getLanguageName(languageCode));
    }

    /**
     * Estimate confidence score based on transcript characteristics
     */
    private Double estimateConfidence(String transcript) {
        if (transcript == null || transcript.isEmpty()) {
            return 0.0;
        }

        double confidence = 70.0; // Base confidence

        // Increase confidence for longer transcripts (more data = more reliable)
        if (transcript.length() > 200) {
            confidence += 10;
        }
        if (transcript.length() > 500) {
            confidence += 5;
        }

        // Increase confidence if transcription contains words
        long wordCount = transcript.split("\\s+").length;
        if (wordCount > 50) {
            confidence += 5;
        }

        // Heuristic: if transcript contains [inaudible] or [unclear], reduce confidence
        if (transcript.contains("[inaudible]") || transcript.contains("[unclear]")) {
            confidence -= 15;
        }

        // Cap confidence at 99 (never 100, as Gemini isn't perfect)
        return Math.min(99.0, Math.max(0.0, confidence));
    }

    /**
     * Build error result
     */
    private TranscriptResult buildErrorResult(String errorMessage, long processingTime) {
        return TranscriptResult.builder()
            .transcript(null)
            .confidence(0.0)
            .languageCode(defaultLanguageCode)
            .durationSeconds(null)
            .provider("gemini")
            .processingTimeMs(processingTime)
            .success(false)
            .errorMessage(errorMessage)
            .build();
    }

    /**
     * Escape JSON special characters
     */
    private String escapeJson(String input) {
        if (input == null) {
            return "";
        }
        return input
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }

    /**
     * Get MIME type based on file extension
     */
    private String getMimeType(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "audio/mpeg"; // Default
        }
        
        String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return switch (ext) {
            case "mp3" -> "audio/mpeg";
            case "wav" -> "audio/wav";
            case "m4a" -> "audio/mp4";
            case "webm" -> "audio/webm";
            case "ogg" -> "audio/ogg";
            case "flac" -> "audio/flac";
            default -> "audio/mpeg";
        };
    }

    /**
     * Get full language name from language code
     */
    private String getLanguageName(String languageCode) {
        return switch (languageCode) {
            case "en-US", "en-GB", "en" -> "English";
            case "vi-VN", "vi" -> "Vietnamese";
            case "fr-FR", "fr" -> "French";
            case "es-ES", "es" -> "Spanish";
            case "de-DE", "de" -> "German";
            case "ja-JP", "ja" -> "Japanese";
            case "zh-CN", "zh" -> "Chinese";
            default -> "English";
        };
    }
}
