package com.vocabularyai.speaking.service.pronunciation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabularyai.ai.service.gemini.GeminiAIClientService;
import com.vocabularyai.ai.service.prompt.PromptEngineeringService;
import com.vocabularyai.speaking.service.pronunciation.dto.PronunciationAnalysisResult;
import com.vocabularyai.speaking.service.pronunciation.dto.PronunciationAnalysisResult.WordPronunciationIssue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GeminiPronunciationAnalysisService
 * 
 * MVP implementation của PronunciationAnalysisService sử dụng Gemini
 * 
 * Approach:
 * - Sử dụng Gemini để phân tích pronunciation từ transcript
 * - Dựa vào text analysis từ transcript (không thực sự nghe audio)
 * - Cung cấp pronunciation score, feedback, word-level issues và suggestions
 * 
 * Limitations:
 * - Không thực sự phân tích audio waveform
 * - Dựa vào pronunciation từ dictionary và common pronunciation patterns
 * - Có thể mở rộng trong tương lai với provider chuyên dụng
 * 
 * ConditionalOnProperty: pronunciation.provider=gemini
 */
@Service
@ConditionalOnProperty(
    name = "speaking.pronunciation.provider",
    havingValue = "gemini",
    matchIfMissing = true
)
@RequiredArgsConstructor
@Slf4j
public class GeminiPronunciationAnalysisService implements PronunciationAnalysisService {
    
    private final GeminiAIClientService geminiAIClientService;
    private final PromptEngineeringService promptEngineeringService;
    private final ObjectMapper objectMapper;
    
    private static final Pattern JSON_EXTRACT_PATTERN = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
    private static final String PROVIDER_NAME = "gemini";
    
    @Override
    public PronunciationAnalysisResult analyzePronunciation(
            String audioFilePath,
            String transcript,
            String topic,
            String level,
            Long userId) {
        
        log.info("Starting pronunciation analysis using Gemini - userId: {}, level: {}", userId, level);
        
        try {
            // STEP 1: Build pronunciation analysis prompt
            log.debug("Building pronunciation analysis prompt - transcript length: {}", transcript.length());
            
            String prompt = buildPronunciationPrompt(transcript, topic, level);
            
            // STEP 2: Call Gemini để phân tích pronunciation
            String response = geminiAIClientService.generateResponse(prompt, userId);
            log.debug("Received pronunciation analysis response from Gemini");
            
            // STEP 3: Parse Gemini response
            PronunciationAnalysisResult result = parsePronunciationResponse(response);
            
            // STEP 4: Set provider name và confidence
            result.setProvider(PROVIDER_NAME);
            if (result.getAnalysisConfidence() == null) {
                result.setAnalysisConfidence(calculateAnalysisConfidence(transcript));
            }
            
            log.info("Pronunciation analysis completed - score: {}, confidence: {}",
                    result.getPronunciationScore(), result.getAnalysisConfidence());
            
            return result;
            
        } catch (Exception e) {
            log.error("Pronunciation analysis failed - userId: {}, error: {}", userId, e.getMessage(), e);
            throw new PronunciationAnalysisException(
                    "Pronunciation analysis failed: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean isProviderAvailable() {
        // Gemini-based pronunciation analysis luôn available nếu Gemini API được config
        return true;
    }
    
    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }
    
    /**
     * Build pronunciation analysis prompt cho Gemini
     * 
     * Prompt yêu cầu Gemini phân tích:
     * - Pronunciation clarity dựa trên từng từ
     * - Stress pattern
     * - Intonation suggestions
     * - Word-level issues
     * 
     * @param transcript Nội dung transcript
     * @param topic Chủ đề bài nói
     * @param level Trình độ người dùng
     * @return Prompt string
     */
    private String buildPronunciationPrompt(String transcript, String topic, String level) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are an expert English pronunciation and phonetics specialist.\n");
        prompt.append("Your task is to analyze pronunciation quality based on a speech transcript.\n\n");
        
        prompt.append("IMPORTANT: You will analyze how the words in the transcript are typically pronounced ");
        prompt.append("and identify potential pronunciation challenges and errors.\n\n");
        
        prompt.append("Analyze and provide:\n");
        prompt.append("1. Overall pronunciation score (0-100) based on typical pronunciation of the words used\n");
        prompt.append("2. Word-level pronunciation issues (words that are commonly mispronounced)\n");
        prompt.append("3. Stress pattern feedback (which syllables to emphasize)\n");
        prompt.append("4. Intonation feedback (how to modulate voice for natural speech)\n");
        prompt.append("5. General suggestions for pronunciation improvement\n\n");
        
        prompt.append("User's English Level: ").append(level).append(" (CEFR scale A1-C2)\n");
        prompt.append("Topic: ").append(topic).append("\n\n");
        
        prompt.append("Return ONLY a JSON response with this exact structure:\n");
        prompt.append("{\n");
        prompt.append("  \"pronunciationScore\": <0-100>,\n");
        prompt.append("  \"feedback\": \"<overall feedback about pronunciation>\",\n");
        prompt.append("  \"stressFeedback\": \"<feedback about word stress and syllable emphasis>\",\n");
        prompt.append("  \"intonationFeedback\": \"<feedback about intonation and natural rhythm>\",\n");
        prompt.append("  \"wordLevelIssues\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"word\": \"<word from transcript>\",\n");
        prompt.append("      \"currentPronunciation\": \"<how it might be mispronounced>\",\n");
        prompt.append("      \"correctPronunciation\": \"<correct pronunciation>\",\n");
        prompt.append("      \"issueType\": \"stress|vowel|consonant|connected_speech\",\n");
        prompt.append("      \"explanation\": \"<why this is a common issue>\",\n");
        prompt.append("      \"tips\": \"<how to pronounce correctly>\"\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"suggestions\": [\n");
        prompt.append("    \"<suggestion 1>\",\n");
        prompt.append("    \"<suggestion 2>\"\n");
        prompt.append("  ]\n");
        prompt.append("}\n\n");
        
        prompt.append("Important:\n");
        prompt.append("- Score should reflect typical pronunciation difficulty of the words used\n");
        prompt.append("- Focus on common pronunciation challenges for non-native speakers\n");
        prompt.append("- Include 2-5 word-level issues if any common mispronunciations are likely\n");
        prompt.append("- Be constructive and supportive\n");
        prompt.append("- Consider the user's level (").append(level).append(") when assessing\n\n");
        
        prompt.append("Transcript to analyze:\n");
        prompt.append("---\n");
        prompt.append(promptEngineeringService.sanitizeInput(transcript));
        prompt.append("\n---\n\n");
        
        prompt.append("Analyze the pronunciation quality of this transcript.");
        
        log.debug("Built pronunciation analysis prompt");
        return prompt.toString();
    }
    
    /**
     * Parse pronunciation analysis response từ Gemini
     * 
     * @param response JSON response từ Gemini
     * @return PronunciationAnalysisResult
     * @throws PronunciationAnalysisException nếu parsing thất bại
     */
    private PronunciationAnalysisResult parsePronunciationResponse(String response) {
        try {
            JsonNode root = extractJsonFromResponse(response);
            
            List<WordPronunciationIssue> wordIssues = new ArrayList<>();
            
            // Parse word-level issues
            if (root.has("wordLevelIssues") && root.get("wordLevelIssues").isArray()) {
                int position = 1;
                for (JsonNode issueNode : root.get("wordLevelIssues")) {
                    WordPronunciationIssue issue = WordPronunciationIssue.builder()
                            .word(issueNode.get("word").asText())
                            .position(position++)
                            .currentPronunciation(issueNode.get("currentPronunciation").asText(""))
                            .correctPronunciation(issueNode.get("correctPronunciation").asText(""))
                            .issueType(issueNode.get("issueType").asText("other"))
                            .explanation(issueNode.get("explanation").asText(""))
                            .tips(issueNode.get("tips").asText(""))
                            .build();
                    wordIssues.add(issue);
                }
            }
            
            // Parse suggestions
            List<String> suggestions = new ArrayList<>();
            if (root.has("suggestions") && root.get("suggestions").isArray()) {
                for (JsonNode suggestionNode : root.get("suggestions")) {
                    suggestions.add(suggestionNode.asText());
                }
            }
            
            // Build result
            PronunciationAnalysisResult result = PronunciationAnalysisResult.builder()
                    .pronunciationScore(safeParseScore(root, "pronunciationScore"))
                    .feedback(root.get("feedback").asText("No pronunciation feedback available"))
                    .stressFeedback(root.has("stressFeedback") ? root.get("stressFeedback").asText("") : "")
                    .intonationFeedback(root.has("intonationFeedback") ? root.get("intonationFeedback").asText("") : "")
                    .wordLevelIssues(wordIssues)
                    .suggestions(suggestions)
                    .build();
            
            // Validate score
            validateScore(result.getPronunciationScore());
            
            return result;
            
        } catch (PronunciationAnalysisException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse pronunciation response: {}", e.getMessage(), e);
            throw new PronunciationAnalysisException(
                    "Failed to parse pronunciation analysis response: " + e.getMessage(), e);
        }
    }
    
    /**
     * Extract JSON từ Gemini response text
     * 
     * Gemini có thể trả về text bao quanh JSON, nên ta dùng regex để extract
     * 
     * @param response Response text từ Gemini
     * @return Parsed JSON root node
     * @throws PronunciationAnalysisException nếu không tìm thấy JSON
     */
    private JsonNode extractJsonFromResponse(String response) throws PronunciationAnalysisException {
        try {
            // Try direct parsing first
            try {
                return objectMapper.readTree(response);
            } catch (Exception e) {
                // Extract JSON using regex
                Matcher matcher = JSON_EXTRACT_PATTERN.matcher(response);
                if (matcher.find()) {
                    String jsonStr = matcher.group();
                    return objectMapper.readTree(jsonStr);
                } else {
                    throw new PronunciationAnalysisException("No JSON found in Gemini response");
                }
            }
        } catch (PronunciationAnalysisException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to extract JSON from response");
            throw new PronunciationAnalysisException("Failed to parse JSON response: " + e.getMessage(), e);
        }
    }
    
    /**
     * Safely parse score từ JSON node
     * 
     * @param node JSON node
     * @param fieldName Field name
     * @return Score (0-100) hoặc null
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
     * Validate pronunciation score
     * 
     * @param score Score to validate
     * @throws PronunciationAnalysisException nếu score không hợp lệ
     */
    private void validateScore(Double score) throws PronunciationAnalysisException {
        if (score != null && (score < 0 || score > 100)) {
            throw new PronunciationAnalysisException("Invalid pronunciation score: " + score);
        }
    }
    
    /**
     * Calculate analysis confidence dựa trên transcript length
     * 
     * @param transcript Transcript để analyze
     * @return Confidence score (0-100)
     */
    private Double calculateAnalysisConfidence(String transcript) {
        if (transcript == null || transcript.isEmpty()) {
            return 0.0;
        }
        
        int length = transcript.length();
        double confidence = Math.min(100.0, (length / 500.0) * 100);
        
        log.debug("Calculated pronunciation analysis confidence: {} based on transcript length: {}",
                 confidence, length);
        
        return confidence;
    }
}
