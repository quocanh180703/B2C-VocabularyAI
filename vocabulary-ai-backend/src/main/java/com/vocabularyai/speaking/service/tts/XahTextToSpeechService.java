package com.vocabularyai.speaking.service.tts;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * High-quality Text-to-Speech service using OpenAI-compatible audio speech endpoint (xah.io).
 * Returns real MP3 audio data encoded as Base64 data URLs or raw byte arrays.
 */
@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class XahTextToSpeechService implements TextToSpeechService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${tts.url:https://api.xah.io/v1/audio/speech}")
    private String ttsUrl;

    @Value("${tts.api-key:sk-4858c9d3176689b889b6a9bc54d391d32502deebc79970b4e4dfb98833e3c68b}")
    private String apiKey;

    @Value("${tts.model:namth/grok-voice-latest}")
    private String model;

    @Value("${tts.voice:alloy}")
    private String defaultVoice;

    @Value("${speaking.text-to-speech.language-code:en-US}")
    private String defaultLanguageCode;

    // In-memory cache for synthesized audio bytes (keyed by voice + ":" + normalized text)
    private final Map<String, byte[]> audioCache = new ConcurrentHashMap<>();

    @Override
    public TextToSpeechResult synthesize(String text) {
        return synthesize(text, defaultLanguageCode, defaultVoice);
    }

    @Override
    public TextToSpeechResult synthesize(String text, String languageCode) {
        return synthesize(text, languageCode, defaultVoice);
    }

    @Override
    public TextToSpeechResult synthesize(String text, String languageCode, String voice) {
        long startTime = System.currentTimeMillis();

        if (text == null || text.trim().isEmpty()) {
            throw new TextToSpeechException("Text input is empty");
        }

        String targetVoice = (voice != null && !voice.trim().isEmpty()) ? voice.trim() : defaultVoice;
        String targetLang = (languageCode != null && !languageCode.trim().isEmpty()) ? languageCode.trim() : defaultLanguageCode;

        try {
            log.info("Generating TTS speech via xah.io - text: '{}', voice: '{}'", 
                text.length() > 60 ? text.substring(0, 60) + "..." : text, targetVoice);

            byte[] audioBytes = synthesizeRawAudio(text, targetVoice);
            long processingTime = System.currentTimeMillis() - startTime;

            String base64Audio = "data:audio/mp3;base64," + Base64.getEncoder().encodeToString(audioBytes);

            return TextToSpeechResult.builder()
                .text(text)
                .audioUrl(base64Audio)
                .audioFormat("audio/mp3")
                .durationSeconds(estimateDuration(text))
                .voice(targetVoice)
                .languageCode(targetLang)
                .provider("xah")
                .processingTimeMs(processingTime)
                .success(true)
                .build();

        } catch (Exception e) {
            log.error("Failed to generate TTS audio via xah.io", e);
            long processingTime = System.currentTimeMillis() - startTime;
            return TextToSpeechResult.builder()
                .text(text)
                .audioUrl(null)
                .audioFormat(null)
                .durationSeconds(null)
                .voice(targetVoice)
                .languageCode(targetLang)
                .provider("xah")
                .processingTimeMs(processingTime)
                .success(false)
                .errorMessage(e.getMessage())
                .build();
        }
    }

    /**
     * Synthesize speech directly into raw MP3 bytes (with caching).
     */
    public byte[] synthesizeRawAudio(String text, String voice) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text cannot be empty");
        }

        String targetVoice = (voice != null && !voice.trim().isEmpty()) ? voice.trim() : defaultVoice;
        String cacheKey = targetVoice + ":" + text.trim().toLowerCase();

        byte[] cached = audioCache.get(cacheKey);
        if (cached != null && cached.length > 0) {
            log.debug("TTS cache hit for key: '{}'", cacheKey);
            return cached;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey.trim());

            Map<String, Object> body = Map.of(
                "model", model,
                "input", text.trim(),
                "voice", targetVoice
            );

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<byte[]> response = restTemplate.postForEntity(ttsUrl, requestEntity, byte[].class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null || response.getBody().length == 0) {
                throw new TextToSpeechException("TTS API returned status: " + response.getStatusCode());
            }

            byte[] audioBytes = response.getBody();

            // Cache up to 1000 items in memory
            if (audioCache.size() > 1000) {
                audioCache.clear();
            }
            audioCache.put(cacheKey, audioBytes);

            return audioBytes;
        } catch (Exception e) {
            throw new TextToSpeechException("Error calling TTS API at " + ttsUrl + ": " + e.getMessage(), e);
        }
    }

    @Override
    public String getProviderType() {
        return "xah";
    }

    @Override
    public boolean isHealthy() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }

    private Double estimateDuration(String text) {
        int wordCount = text.trim().split("\\s+").length;
        return Math.max(1.0, Math.ceil(wordCount * 0.45));
    }
}
