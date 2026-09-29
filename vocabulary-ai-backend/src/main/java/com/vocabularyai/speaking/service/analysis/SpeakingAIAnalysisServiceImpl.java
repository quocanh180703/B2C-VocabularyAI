package com.vocabularyai.speaking.service.analysis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabularyai.ai.service.gemini.GeminiAIClientService;
import com.vocabularyai.ai.service.prompt.PromptEngineeringService;
import com.vocabularyai.speaking.entity.SpeakingAttempt;
import com.vocabularyai.speaking.entity.SpeakingFeedback;
import com.vocabularyai.speaking.entity.SpeakingSession;
import com.vocabularyai.speaking.repository.SpeakingAttemptRepository;
import com.vocabularyai.speaking.repository.SpeakingFeedbackRepository;
import com.vocabularyai.speaking.repository.SpeakingSessionRepository;
import com.vocabularyai.speaking.service.analysis.dto.SpeakingAnalysisResult;
import com.vocabularyai.speaking.service.analysis.dto.SpeakingAnalysisResult.AnalysisScores;
import com.vocabularyai.speaking.service.analysis.dto.SpeakingAnalysisResult.Correction;
import com.vocabularyai.speaking.config.SpeakingProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SpeakingAIAnalysisServiceImpl
 * 
 * Orchestration service for Speaking AI Analysis
 * 
 * Orchestrates complete flow:
 * 1. Accept transcript from SpeechToTextService
 * 2. Call buildSpeakingAnalysisPrompt() and send to GeminiAIClientService
 * 3. Parse AI scores response (grammar, vocabulary, fluency, content, overall)
 * 4. Call buildSpeakingFeedbackPrompt() with scores as context
 * 5. Parse detailed feedback response
 * 6. Combine all results and save to SpeakingFeedback entity
 * 7. Return orchestration result DTO
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SpeakingAIAnalysisServiceImpl implements SpeakingAIAnalysisService {
    
    private final GeminiAIClientService geminiAIClientService;
    private final PromptEngineeringService promptEngineeringService;
    private final SpeakingAttemptRepository speakingAttemptRepository;
    private final SpeakingSessionRepository speakingSessionRepository;
    private final SpeakingFeedbackRepository speakingFeedbackRepository;
    private final SpeakingProperties speakingProperties;
    private final ObjectMapper objectMapper;
    
    private static final Pattern JSON_EXTRACT_PATTERN = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
    
    @Override
    @Transactional
    public SpeakingAnalysisResult analyzeSpeakingAttempt(
            String transcript,
            String topic,
            String level,
            Long attemptId,
            Long sessionId,
            Long userId) {
        
        log.info("Starting speaking analysis - attemptId: {}, sessionId: {}, userId: {}", 
                 attemptId, sessionId, userId);
        
        // Validate input transcript
        if (transcript == null || transcript.trim().isEmpty()) {
            log.error("Invalid transcript for analysis - attemptId: {}", attemptId);
            throw new SpeakingAIAnalysisException(
                    "Transcript không hợp lệ hoặc rỗng");
        }
        
        try {
            // STEP 1: Validate inputs and fetch entities with ownership verification
            // First verify the session exists and belongs to user
            SpeakingSession session = speakingSessionRepository.findByIdAndUserId(sessionId, userId)
                    .orElseThrow(() -> new SpeakingAIAnalysisException(
                            "Speaking session not found or not authorized: " + sessionId));
            
            // Fetch attempt and verify it belongs to the session
            SpeakingAttempt attempt = speakingAttemptRepository.findById(attemptId)
                    .orElseThrow(() -> new SpeakingAIAnalysisException(
                            "Speaking attempt not found: " + attemptId));
            
            // Verify attempt belongs to the specified session
            if (!attempt.getSession().getId().equals(sessionId)) {
                throw new SpeakingAIAnalysisException(
                        "Speaking attempt does not belong to the specified session");
            }
            
            // STEP 2: Build analysis prompt and get scores from Gemini
            log.debug("Building speaking analysis prompt - topic: {}, level: {}, transcript length: {}", 
                     topic, level, transcript.length());
            
            String analysisPrompt = promptEngineeringService.buildSpeakingAnalysisPrompt(
                    transcript, topic, level);
            
            String analysisResponse = geminiAIClientService.generateResponse(analysisPrompt, userId);
            log.debug("Received analysis response from Gemini");
            
            // STEP 3: Parse analysis scores from JSON response
            AnalysisScores analysisScores = parseAnalysisScoresFromResponse(analysisResponse);
            log.debug("Parsed analysis scores - grammar: {}, vocabulary: {}, fluency: {}, content: {}, overall: {}",
                     analysisScores.getGrammar(), analysisScores.getVocabulary(), 
                     analysisScores.getFluency(), analysisScores.getContent(),
                     analysisScores.getOverall());
            
            // STEP 4: Build feedback prompt with scores as context and get detailed feedback
            log.debug("Building speaking feedback prompt with analysis scores");
            
            String scoresJson = objectMapper.writeValueAsString(analysisScores);
            String feedbackPrompt = promptEngineeringService.buildSpeakingFeedbackPrompt(
                    transcript, topic, level, scoresJson);
            
            String feedbackResponse = geminiAIClientService.generateResponse(feedbackPrompt, userId);
            log.debug("Received feedback response from Gemini");
            
            // STEP 5: Parse detailed feedback response
            FeedbackResult feedbackResult = parseFeedbackFromResponse(feedbackResponse);
            log.debug("Parsed feedback - corrections: {}, suggestions: {}, strengths: {}, weaknesses: {}",
                     feedbackResult.corrections.size(), feedbackResult.suggestions.size(),
                     feedbackResult.strengths.size(), feedbackResult.weaknesses.size());
            
            // STEP 6: Create SpeakingAnalysisResult DTO
            SpeakingAnalysisResult analysisResult = SpeakingAnalysisResult.builder()
                    .analysisScores(analysisScores)
                    .feedback(feedbackResult.feedback)
                    .corrections(feedbackResult.corrections)
                    .suggestions(feedbackResult.suggestions)
                    .strengths(feedbackResult.strengths)
                    .weaknesses(feedbackResult.weaknesses)
                    .analysisConfidence(calculateAnalysisConfidence(transcript))
                    .build();
            
            // STEP 7: Save results to SpeakingFeedback entity
            SpeakingFeedback feedback = SpeakingFeedback.builder()
                    .attempt(attempt)
                    .grammarScore(analysisScores.getGrammar())
                    .vocabularyScore(analysisScores.getVocabulary())
                    .fluencyScore(analysisScores.getFluency())
                    .contentScore(analysisScores.getContent())
                    .pronunciationScore(analysisScores.getPronunciation())
                    .overallScore(analysisScores.getOverall())
                    .feedback(feedbackResult.feedback)
                    .corrections(objectMapper.writeValueAsString(feedbackResult.corrections))
                    .suggestions(objectMapper.writeValueAsString(feedbackResult.suggestions))
                    .strengths(objectMapper.writeValueAsString(feedbackResult.strengths))
                    .weaknesses(objectMapper.writeValueAsString(feedbackResult.weaknesses))
                    .build();
            
            feedback.calculateOverallScore(
                    speakingProperties.getAiAnalysis().getGrammarWeight(),
                    speakingProperties.getAiAnalysis().getVocabularyWeight(),
                    speakingProperties.getAiAnalysis().getFluencyWeight(),
                    speakingProperties.getAiAnalysis().getContentWeight(),
                    speakingProperties.getAiAnalysis().getPronunciationWeight());
            
            speakingFeedbackRepository.save(feedback);
            log.info("Successfully saved speaking feedback - attemptId: {}, overallScore: {}",
                    attemptId, feedback.getOverallScore());
            
            return analysisResult;
            
        } catch (SpeakingAIAnalysisException e) {
            log.error("Speaking analysis failed: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during speaking analysis - attemptId: {}, error: {}", 
                     attemptId, e.getMessage(), e);
            throw new SpeakingAIAnalysisException(
                    "Speaking analysis failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Parse analysis scores from Gemini JSON response
     * 
     * Extracts scores: grammar, vocabulary, fluency, content, overall, pronunciation
     * 
     * @param response JSON response from Gemini
     * @return AnalysisScores object
     * @throws SpeakingAIAnalysisException if parsing fails or invalid scores
     */
    private AnalysisScores parseAnalysisScoresFromResponse(String response) {
        try {
            JsonNode root = extractJsonFromResponse(response);
            
            AnalysisScores scores = AnalysisScores.builder()
                    .grammar(safeParseScore(root, "grammarScore"))
                    .vocabulary(safeParseScore(root, "vocabularyScore"))
                    .fluency(safeParseScore(root, "fluencyScore"))
                    .content(safeParseScore(root, "contentScore"))
                    .pronunciation(safeParseScore(root, "pronunciationScore"))
                    .overall(safeParseScore(root, "overallScore"))
                    .build();
            
            // Validate scores are in valid range
            validateScores(scores);
            
            return scores;
        } catch (SpeakingAIAnalysisException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse analysis scores: {}", e.getMessage(), e);
            throw new SpeakingAIAnalysisException(
                    "Failed to parse analysis scores from Gemini response: " + e.getMessage(), e);
        }
    }
    
    /**
     * Parse detailed feedback from Gemini response
     * 
     * Extracts: feedback, corrections, suggestions, strengths, weaknesses
     * 
     * @param response JSON response from Gemini
     * @return FeedbackResult object
     */
    private FeedbackResult parseFeedbackFromResponse(String response) {
        try {
            JsonNode root = extractJsonFromResponse(response);
            
            FeedbackResult result = new FeedbackResult();
            
            // Parse feedback text
            if (root.has("feedback")) {
                result.feedback = root.get("feedback").asText("No feedback available");
            }
            
            // Parse corrections array
            if (root.has("corrections") && root.get("corrections").isArray()) {
                root.get("corrections").forEach(node -> {
                    Correction correction = Correction.builder()
                            .original(node.get("original").asText())
                            .corrected(node.get("corrected").asText())
                            .errorType(node.get("errorType").asText("grammar"))
                            .explanation(node.get("explanation").asText())
                            .alternatives(parseStringArray(node.get("alternatives")))
                            .build();
                    result.corrections.add(correction);
                });
            }
            
            // Parse suggestions array
            if (root.has("suggestions") && root.get("suggestions").isArray()) {
                root.get("suggestions").forEach(node -> {
                    result.suggestions.add(node.asText());
                });
            }
            
            // Parse strengths array
            if (root.has("strengths") && root.get("strengths").isArray()) {
                root.get("strengths").forEach(node -> {
                    result.strengths.add(node.asText());
                });
            }
            
            // Parse weaknesses array
            if (root.has("weaknesses") && root.get("weaknesses").isArray()) {
                root.get("weaknesses").forEach(node -> {
                    result.weaknesses.add(node.asText());
                });
            }
            
            return result;
        } catch (Exception e) {
            log.error("Failed to parse feedback response: {}", e.getMessage(), e);
            // Return empty result rather than throwing - feedback is nice-to-have
            return new FeedbackResult();
        }
    }
    
    /**
     * Extract JSON object from Gemini response text
     * 
     * Gemini sometimes returns additional text around JSON, so we extract the JSON using regex
     * 
     * @param response Full text response from Gemini
     * @return Parsed JSON root node
     * @throws SpeakingAIAnalysisException if no JSON found
     */
    private JsonNode extractJsonFromResponse(String response) throws SpeakingAIAnalysisException {
        try {
            // First try direct parsing
            try {
                return objectMapper.readTree(response);
            } catch (Exception e) {
                // If direct parsing fails, extract JSON using regex
                Matcher matcher = JSON_EXTRACT_PATTERN.matcher(response);
                if (matcher.find()) {
                    String jsonStr = matcher.group();
                    return objectMapper.readTree(jsonStr);
                } else {
                    throw new SpeakingAIAnalysisException("No JSON found in Gemini response");
                }
            }
        } catch (SpeakingAIAnalysisException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to extract JSON from response: {}", response.substring(0, Math.min(200, response.length())));
            throw new SpeakingAIAnalysisException("Failed to parse Gemini JSON response: " + e.getMessage(), e);
        }
    }
    
    /**
     * Safely parse score value from JSON node
     * 
     * Returns null if field missing or not a number
     * 
     * @param node JSON node
     * @param fieldName Field name to parse
     * @return Score value (0-100) or null
     */
    private Double safeParseScore(JsonNode node, String fieldName) {
        if (node.has(fieldName)) {
            JsonNode valueNode = node.get(fieldName);
            if (valueNode.isNumber()) {
                return valueNode.asDouble();
            }
        }
        return null;
    }
    
    /**
     * Parse string array from JSON node
     * 
     * @param node JSON array node
     * @return List of strings
     */
    private List<String> parseStringArray(JsonNode node) {
        List<String> result = new ArrayList<>();
        if (node != null && node.isArray()) {
            node.forEach(item -> result.add(item.asText()));
        }
        return result;
    }
    
    /**
     * Validate that analysis scores are in valid range (0-100)
     * 
     * @param scores Scores to validate
     * @throws SpeakingAIAnalysisException if any score is invalid
     */
    private void validateScores(AnalysisScores scores) throws SpeakingAIAnalysisException {
        if (scores.getGrammar() != null && (scores.getGrammar() < 0 || scores.getGrammar() > 100)) {
            throw new SpeakingAIAnalysisException("Invalid grammar score: " + scores.getGrammar());
        }
        if (scores.getVocabulary() != null && (scores.getVocabulary() < 0 || scores.getVocabulary() > 100)) {
            throw new SpeakingAIAnalysisException("Invalid vocabulary score: " + scores.getVocabulary());
        }
        if (scores.getFluency() != null && (scores.getFluency() < 0 || scores.getFluency() > 100)) {
            throw new SpeakingAIAnalysisException("Invalid fluency score: " + scores.getFluency());
        }
        if (scores.getContent() != null && (scores.getContent() < 0 || scores.getContent() > 100)) {
            throw new SpeakingAIAnalysisException("Invalid content score: " + scores.getContent());
        }
        if (scores.getOverall() != null && (scores.getOverall() < 0 || scores.getOverall() > 100)) {
            throw new SpeakingAIAnalysisException("Invalid overall score: " + scores.getOverall());
        }
    }
    
    /**
     * Calculate analysis confidence based on transcript quality
     * 
     * Confidence factors:
     * - Length of transcript (longer = more confident)
     * - Presence of detailed content
     * 
     * @param transcript The analyzed transcript
     * @return Confidence score (0-100)
     */
    private Double calculateAnalysisConfidence(String transcript) {
        if (transcript == null || transcript.isEmpty()) {
            return 0.0;
        }
        
        int length = transcript.length();
        double confidence = Math.min(100.0, (length / 500.0) * 100);
        
        log.debug("Calculated analysis confidence: {} based on transcript length: {}", 
                 confidence, length);
        
        return confidence;
    }
    
    /**
     * Internal class to hold parsed feedback result
     */
    private static class FeedbackResult {
        String feedback = "Your speaking was evaluated. Keep practicing!";
        List<Correction> corrections = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        List<String> strengths = new ArrayList<>();
        List<String> weaknesses = new ArrayList<>();
    }
}
