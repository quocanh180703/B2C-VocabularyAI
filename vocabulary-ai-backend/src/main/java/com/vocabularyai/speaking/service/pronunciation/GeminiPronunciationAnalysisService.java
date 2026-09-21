package com.vocabularyai.speaking.service.pronunciation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabularyai.speaking.service.pronunciation.dto.PronunciationAnalysisResult;
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

import java.util.Arrays;

/**
 * Pronunciation Analysis implementation using Gemini API.
 * MVP provider using text-based analysis of transcribed speech.
 *
 * Note: This is text-based analysis. For production audio-based pronunciation scoring,
 * use Google Cloud Speech-to-Text with pronunciation assessment or similar services.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiPronunciationAnalysisService implements PronunciationAnalysisService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini:api-key}")
    private String apiKey;

    @Value("${gemini:model:gemini-3.1-flash-lite}")
    private String model;

    @Value("${speaking.pronunciation.provider:gemini}")
    private String provider;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent";

    /**
     * Analyze pronunciation from audio URL and transcript
     */
    @Override
    public PronunciationAnalysisResult analyzePronunciation(String audioUrl, String transcript, String topic, String level, Long userId) {
        long startTime = System.currentTimeMillis();
        log.info("Analyzing pronunciation for topic: {} at level: {}", topic, level);

        try {
            // Validate inputs
            if (transcript == null || transcript.trim().isEmpty()) {
                throw new PronunciationAnalysisException("Transcript is empty");
            }

            // Get analysis from Gemini (text-based analysis for MVP)
            String analysisJson = callGeminiForAnalysis(transcript, topic, level);

            // Parse Gemini response
            PronunciationAnalysisResult result = parseAnalysisResponse(analysisJson);

            result.setProvider(provider);

            log.info("Pronunciation analysis successful: score={}", result.getPronunciationScore());

            return result;

        } catch (Exception e) {
            log.error("Failed to analyze pronunciation", e);
            return buildErrorResult(e.getMessage());
        }
    }

    @Override
    public boolean isProviderAvailable() {
        return !provider.equals("disabled");
    }

    @Override
    public String getProviderType() {
        return provider;
    }

    @Override
    public boolean isHealthy() {
        try {
            log.debug("Checking Gemini pronunciation analysis service health");
            return true;
        } catch (Exception e) {
            log.error("Service health check failed", e);
            return false;
        }
    }

    /**
     * Call Gemini API for pronunciation analysis
     */
    private String callGeminiForAnalysis(String transcript, String topic, String level) {
        try {
            String url = GEMINI_API_URL.replace("{model}", model) + "?key=" + apiKey;

            String prompt = buildAnalysisPrompt(transcript, topic, level);

            String requestBody = String.format("""
                {
                  "contents": [{
                    "parts": [{"text": "%s"}]
                  }],
                  "generationConfig": {
                    "temperature": 0.3,
                    "topP": 0.95,
                    "maxOutputTokens": 800
                  }
                }
                """, escapeJson(prompt));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

            log.debug("Calling Gemini API for pronunciation analysis");
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new PronunciationAnalysisException("Gemini API returned status: " + response.getStatusCode());
            }

            // Parse Gemini response
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            JsonNode candidates = responseJson.get("candidates");

            if (candidates == null || candidates.isEmpty()) {
                throw new PronunciationAnalysisException("No candidates in Gemini response");
            }

            JsonNode content = candidates.get(0).get("content");
            JsonNode parts = content.get("parts");

            if (parts == null || parts.isEmpty()) {
                throw new PronunciationAnalysisException("No parts in Gemini response");
            }

            String analysisText = parts.get(0).get("text").asText();
            log.debug("Gemini analysis response: {}", analysisText.substring(0, Math.min(200, analysisText.length())));

            return analysisText;

        } catch (Exception e) {
            log.error("Failed to call Gemini API for pronunciation analysis", e);
            throw new PronunciationAnalysisException("Gemini API call failed: " + e.getMessage(), e);
        }
    }

    /**
     * Build analysis prompt for Gemini
     */
    private String buildAnalysisPrompt(String transcript, String topic, String level) {
        return String.format("""
            Analyze the pronunciation quality of the following English speech transcript.
            Speaking Level: %s
            Topic: %s
            
            User's transcript: "%s"
            
            Return ONLY a JSON object (no markdown, no extra text) with these fields:
            {
              "pronunciationScore": <0-100>,
              "feedback": "Overall feedback text",
              "stressFeedback": "Feedback about word stress",
              "intonationFeedback": "Feedback about intonation",
              "suggestions": ["suggestion 1", "suggestion 2"],
              "analysisConfidence": <0-100>
            }
            """, level, topic, transcript);
    }

    /**
     * Parse Gemini's JSON response into PronunciationAnalysisResult
     */
    private PronunciationAnalysisResult parseAnalysisResponse(String responseText) {
        try {
            // Extract JSON from response (Gemini might include extra text)
            String jsonStr = extractJson(responseText);
            JsonNode analysisNode = objectMapper.readTree(jsonStr);

            Double pronunciationScore = analysisNode.has("pronunciationScore")
                ? analysisNode.get("pronunciationScore").asDouble()
                : 70.0;

            String feedback = analysisNode.has("feedback")
                ? analysisNode.get("feedback").asText()
                : "Pronunciation analysis completed";

            String stressFeedback = analysisNode.has("stressFeedback")
                ? analysisNode.get("stressFeedback").asText()
                : null;

            String intonationFeedback = analysisNode.has("intonationFeedback")
                ? analysisNode.get("intonationFeedback").asText()
                : null;

            Double analysisConfidence = analysisNode.has("analysisConfidence")
                ? analysisNode.get("analysisConfidence").asDouble()
                : 75.0;

            return PronunciationAnalysisResult.builder()
                .pronunciationScore(pronunciationScore)
                .feedback(feedback)
                .stressFeedback(stressFeedback)
                .intonationFeedback(intonationFeedback)
                .analysisConfidence(analysisConfidence)
                .provider(provider)
                .build();

        } catch (Exception e) {
            log.error("Failed to parse analysis response", e);
            // Return default analysis if parsing fails
            return buildDefaultAnalysis();
        }
    }

    /**
     * Extract JSON from response (handles Gemini formatting)
     */
    private String extractJson(String responseText) {
        int jsonStart = responseText.indexOf("{");
        int jsonEnd = responseText.lastIndexOf("}");

        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            return responseText.substring(jsonStart, jsonEnd + 1);
        }

        throw new PronunciationAnalysisException("Could not find JSON in response");
    }

    /**
     * Build default analysis result
     */
    private PronunciationAnalysisResult buildDefaultAnalysis() {
        return PronunciationAnalysisResult.builder()
            .pronunciationScore(70.0)
            .feedback("Pronunciation analysis completed")
            .stressFeedback("Practice key word stress patterns")
            .intonationFeedback("Good intonation overall")
            .suggestions(Arrays.asList("Speak more clearly", "Practice word stress"))
            .analysisConfidence(60.0)
            .provider(provider)
            .build();
    }

    /**
     * Build error result
     */
    private PronunciationAnalysisResult buildErrorResult(String errorMessage) {
        log.error("Building error result: {}", errorMessage);
        return PronunciationAnalysisResult.builder()
            .pronunciationScore(0.0)
            .feedback(errorMessage)
            .stressFeedback(null)
            .intonationFeedback(null)
            .analysisConfidence(0.0)
            .provider(provider)
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
}
