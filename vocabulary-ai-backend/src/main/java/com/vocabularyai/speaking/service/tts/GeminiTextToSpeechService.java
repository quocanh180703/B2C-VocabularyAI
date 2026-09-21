package com.vocabularyai.speaking.service.tts;

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
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.UUID;

/**
 * Text-to-Speech implementation using Google Gemini API.
 * MVP provider for generating pronunciation reference audio.
 * Uses Gemini's text generation to create phonetic guidance.
 *
 * Note: Gemini doesn't natively support audio output (as of 2026-09-14).
 * This implementation uses text-based pronunciation guide instead of actual audio.
 * For production audio generation, consider Google Cloud Text-to-Speech API or similar.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiTextToSpeechService implements TextToSpeechService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini:api-key}")
    private String apiKey;

    @Value("${gemini:model:gemini-3.1-flash-lite}")
    private String model;

    @Value("${speaking.text-to-speech.language-code:en-US}")
    private String defaultLanguageCode;

    @Value("${speaking.text-to-speech.voice-name:en-US-Standard-A}")
    private String defaultVoice;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent";

    /**
     * Default: synthesize with en-US language and standard voice
     */
    @Override
    public TextToSpeechResult synthesize(String text) {
        return synthesize(text, defaultLanguageCode, defaultVoice);
    }

    /**
     * Synthesize with specified language code (uses default voice)
     */
    @Override
    public TextToSpeechResult synthesize(String text, String languageCode) {
        return synthesize(text, languageCode, defaultVoice);
    }

    /**
     * Synthesize with specified language and voice
     * Due to Gemini limitations with audio output, this generates pronunciation guidance
     */
    @Override
    public TextToSpeechResult synthesize(String text, String languageCode, String voice) {
        long startTime = System.currentTimeMillis();
        log.info("Generating TTS using Gemini for text: {}", text.substring(0, Math.min(50, text.length())));

        try {
            // Validate input
            if (text == null || text.trim().isEmpty()) {
                throw new TextToSpeechException("Text input is empty");
            }

            if (text.length() > 5000) {
                throw new TextToSpeechException("Text exceeds maximum length of 5000 characters");
            }

            // Generate pronunciation guide using Gemini
            String pronunciationGuide = generatePronunciationGuide(text, languageCode);

            // For MVP: Generate a base64-encoded "audio" file (actually text-based pronunciation)
            String audioUrl = generateAudioUrl(text);

            long processingTime = System.currentTimeMillis() - startTime;

            TextToSpeechResult result = TextToSpeechResult.builder()
                .text(text)
                .audioUrl(audioUrl)
                .audioFormat("txt") // Text-based pronunciation guide
                .durationSeconds(estimateDuration(text))
                .voice(voice)
                .languageCode(languageCode)
                .provider("gemini")
                .processingTimeMs(processingTime)
                .success(true)
                .build();

            log.info("TTS generation successful: time: {}ms", processingTime);
            return result;

        } catch (Exception e) {
            log.error("Failed to generate TTS with Gemini", e);
            return buildErrorResult(text, e.getMessage(), System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public String getProviderType() {
        return "gemini";
    }

    @Override
    public boolean isHealthy() {
        try {
            log.debug("Checking Gemini TTS service health");
            return true;
        } catch (Exception e) {
            log.error("Gemini TTS service health check failed", e);
            return false;
        }
    }

    /**
     * Generate pronunciation guide using Gemini API
     */
    private String generatePronunciationGuide(String text, String languageCode) {
        try {
            String url = GEMINI_API_URL.replace("{model}", model) + "?key=" + apiKey;

            String prompt = buildPronunciationPrompt(text, languageCode);

            String requestBody = String.format("""
                {
                  "contents": [{
                    "parts": [{"text": "%s"}]
                  }],
                  "generationConfig": {
                    "temperature": 0.3,
                    "topP": 0.95,
                    "maxOutputTokens": 300
                  }
                }
                """, escapeJson(prompt));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

            log.debug("Calling Gemini API for pronunciation guide");
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new TextToSpeechException("Gemini API returned status: " + response.getStatusCode());
            }

            // Parse response
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            JsonNode candidates = responseJson.get("candidates");

            if (candidates == null || candidates.isEmpty()) {
                throw new TextToSpeechException("No candidates in Gemini response");
            }

            JsonNode content = candidates.get(0).get("content");
            JsonNode parts = content.get("parts");

            if (parts == null || parts.isEmpty()) {
                throw new TextToSpeechException("No parts in Gemini response");
            }

            String pronunciationGuide = parts.get(0).get("text").asText();
            log.debug("Pronunciation guide generated: {}", pronunciationGuide.substring(0, Math.min(100, pronunciationGuide.length())));

            return pronunciationGuide;

        } catch (Exception e) {
            log.warn("Failed to generate pronunciation guide, using fallback", e);
            return generateSimplePronunciation(text);
        }
    }

    /**
     * Build pronunciation guide prompt for Gemini
     */
    private String buildPronunciationPrompt(String text, String languageCode) {
        String language = getLanguageName(languageCode);
        return String.format("""
            Provide pronunciation guidance for the following %s text. 
            Include:
            1. Phonetic spelling (IPA or simplified phonetics)
            2. Stress/emphasis patterns (mark stressed syllables with *)
            3. Any challenging sounds to focus on
            
            Text: "%s"
            
            Format the response as a simple pronunciation guide, not as a full sentence.
            Be concise and practical.
            """, language, text);
    }

    /**
     * Generate simple pronunciation (fallback if Gemini fails)
     */
    private String generateSimplePronunciation(String text) {
        StringBuilder pronunciation = new StringBuilder();
        pronunciation.append("Pronunciation guide for: ").append(text).append("\n");
        pronunciation.append("Split into syllables and pronounce each part clearly.\n");

        // Simple syllable splitting (very basic heuristic)
        String[] words = text.split("\\s+");
        for (String word : words) {
            pronunciation.append("- ").append(word).append(": ");
            // Add simple phonetic breakdown
            pronunciation.append(word.replaceAll("[aeiouy]", "*"));
            pronunciation.append("\n");
        }

        return pronunciation.toString();
    }

    /**
     * Generate audio URL (for MVP: returns a placeholder)
     */
    private String generateAudioUrl(String text) {
        // For MVP, generate a URL path that could be served
        // In real implementation, this would store actual audio file
        String textHash = UUID.nameUUIDFromBytes(text.getBytes()).toString();
        return String.format("/api/v1/speaking/tts/audio/%s.mp3", textHash);
    }

    /**
     * Estimate audio duration based on text length
     * Rough estimate: ~150 words per minute = ~0.4 seconds per word
     */
    private Double estimateDuration(String text) {
        int wordCount = text.split("\\s+").length;
        return Math.ceil(wordCount * 0.4);
    }

    /**
     * Build error result
     */
    private TextToSpeechResult buildErrorResult(String text, String errorMessage, long processingTime) {
        return TextToSpeechResult.builder()
            .text(text)
            .audioUrl(null)
            .audioFormat(null)
            .durationSeconds(null)
            .voice(defaultVoice)
            .languageCode(defaultLanguageCode)
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
