package com.vocabularyai.ai.service.grammar;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabularyai.ai.dto.response.GrammarCheckResponse;
import com.vocabularyai.ai.dto.response.GrammarError;
import com.vocabularyai.ai.dto.response.GrammarSuggestion;
import com.vocabularyai.ai.service.cache.CacheService;
import com.vocabularyai.ai.service.gemini.GeminiAIClientService;
import com.vocabularyai.ai.service.prompt.PromptEngineeringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * GrammarCheckingService Implementation
 * 
 * Triển khai kiểm tra ngữ pháp:
 * - Single text grammar check với cache
 * - Batch text checking
 * - Smart caching (1 giờ TTL)
 * - Error parsing + suggestions
 * 
 * Dependencies:
 * - GeminiAIClientService: Gọi AI API
 * - PromptEngineeringService: Xây dựng prompt
 * - CacheService: Cache check results
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GrammarCheckingServiceImpl implements GrammarCheckingService {
    
    private final GeminiAIClientService geminiService;
    private final PromptEngineeringService promptService;
    private final CacheService cacheService;
    private final ObjectMapper objectMapper;
    
    @Value("${ai.grammar.cache-ttl-hours:1}")
    private long grammarCacheTtlHours;
    
    @Value("${ai.grammar.batch-max-texts:10}")
    private int batchMaxTexts;
    
    // Cache key prefix
    private static final String CACHE_KEY_GRAMMAR_CHECK = "ai:grammar:check:%s";
    
    // ==================== Single Text Grammar Check ====================
    
    @Override
    public GrammarCheckResponse checkGrammar(String text, String language, Boolean includeSuggestions, Long userId) {
        log.info("Checking grammar for text length: {}, language: {}, user: {}", 
            text.length(), language, userId);
        
        // Validate input
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text cannot be blank");
        }
        
        long startTime = System.currentTimeMillis();
        
        // Sanitize language
        if (language == null || language.trim().isEmpty()) {
            language = "English";
        }
        
        // Sanitize includeSuggestions
        if (includeSuggestions == null) {
            includeSuggestions = true;
        }
        
        // 1. Kiểm tra cache
        String textHash = hashText(text);
        String cacheKey = String.format(CACHE_KEY_GRAMMAR_CHECK, textHash);
        Optional<GrammarCheckResponse> cachedResult = cacheService.get(cacheKey, GrammarCheckResponse.class);
        
        if (cachedResult.isPresent()) {
            log.debug("Cache hit for grammar check");
            return cachedResult.get();
        }
        
        log.debug("Cache miss for grammar check, building response");
        
        // 2. Xây dựng grammar prompt
        String grammarPrompt = promptService.buildGrammarPrompt(text, language);
        
        log.debug("Built grammar prompt, estimated tokens: {}", 
            promptService.estimateTokenCount(grammarPrompt));
        
        // 3. Gọi Gemini API
        String aiResponse;
        try {
            aiResponse = geminiService.generateResponse(grammarPrompt, userId);
            log.debug("AI response received, length: {}", aiResponse.length());
        } catch (Exception e) {
            log.error("Error calling Gemini API for grammar check: {}", e.getMessage());
            throw e;
        }
        
        // 4. Parse response
        GrammarCheckResponse response = parseGrammarCheckResponse(text, aiResponse, includeSuggestions);
        
        // 5. Cache kết quả
        long cacheTtlSeconds = grammarCacheTtlHours * 3600;
        cacheService.set(cacheKey, response, cacheTtlSeconds);
        
        log.info("Grammar check completed and cached, took: {}ms", System.currentTimeMillis() - startTime);
        
        return response;
    }
    
    // ==================== Batch Text Grammar Check ====================
    
    @Override
    public List<GrammarCheckResponse> checkGrammarBatch(List<String> texts, Long userId) {
        log.info("Checking grammar for {} texts, user: {}", texts.size(), userId);
        
        // Validate
        if (texts == null || texts.isEmpty()) {
            throw new IllegalArgumentException("Texts list cannot be empty");
        }
        
        if (texts.size() > batchMaxTexts) {
            throw new IllegalArgumentException("Cannot check more than " + batchMaxTexts + " texts at once");
        }
        
        long startTime = System.currentTimeMillis();
        
        // 1. Kiểm tra cache cho từng text
        List<GrammarCheckResponse> results = new ArrayList<>();
        List<String> textsNeedCheck = new ArrayList<>();
        Map<String, String> textHashMap = new HashMap<>();
        
        for (String text : texts) {
            if (text == null || text.trim().isEmpty()) {
                continue; // Skip empty texts
            }
            
            String textHash = hashText(text);
            textHashMap.put(textHash, text);
            
            String cacheKey = String.format(CACHE_KEY_GRAMMAR_CHECK, textHash);
            Optional<GrammarCheckResponse> cached = cacheService.get(cacheKey, GrammarCheckResponse.class);
            
            if (cached.isPresent()) {
                results.add(cached.get());
                log.debug("Cache hit for text hash: {}", textHash);
            } else {
                textsNeedCheck.add(text);
                log.debug("Cache miss for text hash: {}", textHash);
            }
        }
        
        // 2. Nếu có texts cần check, gọi API batch
        if (!textsNeedCheck.isEmpty()) {
            log.debug("Need to check {} texts from AI", textsNeedCheck.size());
            
            List<GrammarCheckResponse> newChecks = checkGrammarBatchInternal(textsNeedCheck, userId);
            
            // 3. Cache tất cả results
            for (GrammarCheckResponse check : newChecks) {
                String textHash = hashText(check.getOriginalText());
                String cacheKey = String.format(CACHE_KEY_GRAMMAR_CHECK, textHash);
                long cacheTtlSeconds = grammarCacheTtlHours * 3600;
                cacheService.set(cacheKey, check, cacheTtlSeconds);
            }
            
            results.addAll(newChecks);
        }
        
        log.info("Batch grammar check completed: {} texts, took: {}ms", 
            texts.size(), System.currentTimeMillis() - startTime);
        
        return results;
    }
    
    /**
     * Internal batch checking (avoid recursion)
     */
    private List<GrammarCheckResponse> checkGrammarBatchInternal(List<String> textsToCheck, Long userId) {
        List<GrammarCheckResponse> responses = new ArrayList<>();
        
        // Tạo batch prompt cho tất cả texts
        StringBuilder batchPrompt = new StringBuilder();
        batchPrompt.append("Check grammar for the following ").append(textsToCheck.size()).append(" texts:\n\n");
        
        for (int i = 0; i < textsToCheck.size(); i++) {
            String text = textsToCheck.get(i);
            batchPrompt.append(i + 1).append(". ").append(text).append("\n");
        }
        
        batchPrompt.append("\nFor each text, provide:\n");
        batchPrompt.append("- Corrected text\n");
        batchPrompt.append("- List of errors (type, position, suggestion)\n");
        batchPrompt.append("- Overall suggestions\n");
        
        log.debug("Built batch grammar prompt for {} texts, tokens: {}", 
            textsToCheck.size(), promptService.estimateTokenCount(batchPrompt.toString()));
        
        try {
            // Gọi API
            String aiResponse = geminiService.generateResponse(batchPrompt.toString(), userId);
            
            // Parse batch response
            responses = parseBatchGrammarResponse(textsToCheck, aiResponse);
            
            log.debug("Batch grammar check API call succeeded, got {} responses", responses.size());
            
        } catch (Exception e) {
            log.error("Error calling Gemini API for batch grammar check: {}", e.getMessage());
            throw e;
        }
        
        return responses;
    }
    
    // ==================== Cache Operations ====================
    
    @Override
    public void invalidateGrammarCache(String textHash) {
        log.info("Invalidating grammar cache for text hash: {}", textHash);
        
        String cacheKey = String.format(CACHE_KEY_GRAMMAR_CHECK, textHash);
        cacheService.delete(cacheKey);
        
        log.info("Grammar cache invalidated");
    }
    
    @Override
    public void invalidateAllGrammarCache() {
        log.warn("Invalidating ALL grammar caches");
        
        cacheService.deletePattern("ai:grammar:check:*");
        
        log.warn("All grammar caches invalidated");
    }
    
    @Override
    public boolean isGrammarCheckCached(String textHash) {
        String cacheKey = String.format(CACHE_KEY_GRAMMAR_CHECK, textHash);
        Optional<GrammarCheckResponse> cached = cacheService.get(cacheKey, GrammarCheckResponse.class);
        return cached.isPresent();
    }
    
    @Override
    public GrammarCheckResponse getCachedGrammarCheck(String textHash) {
        String cacheKey = String.format(CACHE_KEY_GRAMMAR_CHECK, textHash);
        Optional<GrammarCheckResponse> cached = cacheService.get(cacheKey, GrammarCheckResponse.class);
        return cached.orElse(null);
    }
    
    // ==================== Helper Methods ====================
    
    /**
     * Hash text (SHA-256)
     */
    private String hashText(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes());
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            // Return first 32 chars
            return hexString.substring(0, Math.min(32, hexString.length()));
            
        } catch (Exception e) {
            log.warn("Error hashing text: {}, using fallback", e.getMessage());
            return Integer.toHexString(text.hashCode());
        }
    }
    
    /**
     * Parse AI response thành GrammarCheckResponse
     * 
     * AI response format (JSON):
     * {
     *   "correctedText": "...",
     *   "errors": [
     *     {
     *       "startOffset": 0,
     *       "endOffset": 5,
     *       "errorType": "GRAMMAR|SPELLING|STYLE|PUNCTUATION",
     *       "suggestion": "...",
     *       "explanation": "..."
     *     }
     *   ],
     *   "suggestions": [
     *     {
     *       "errorType": "...",
     *       "suggestion": "...",
     *       "explanation": "..."
     *     }
     *   ]
     * }
     */
    private GrammarCheckResponse parseGrammarCheckResponse(String originalText, String aiResponse, Boolean includeSuggestions) {
        try {
            // Try to parse as JSON (stripping markdown code blocks if present)
            String cleaned = cleanJson(aiResponse);
            Map<String, Object> parsed = objectMapper.readValue(cleaned, Map.class);
            
            String correctedText = (String) parsed.getOrDefault("correctedText", originalText);
            
            List<GrammarError> errors = parseErrorsList(parsed.get("errors"));
            List<GrammarSuggestion> suggestions = includeSuggestions ? 
                parseSuggestionsList(parsed.get("suggestions")) : 
                Collections.emptyList();
            
            return GrammarCheckResponse.builder()
                .originalText(originalText)
                .correctedText(correctedText)
                .errors(errors)
                .suggestions(suggestions)
                .build();
                
        } catch (Exception e) {
            log.warn("Error parsing AI response as JSON: {}, using fallback", e.getMessage());
            
            // Fallback: no errors detected
            return GrammarCheckResponse.builder()
                .originalText(originalText)
                .correctedText(originalText)
                .errors(Collections.emptyList())
                .suggestions(Collections.emptyList())
                .build();
        }
    }
    
    /**
     * Parse errors list từ response
     */
    @SuppressWarnings("unchecked")
    private List<GrammarError> parseErrorsList(Object errorsObj) {
        List<GrammarError> errors = new ArrayList<>();
        
        if (errorsObj instanceof List) {
            List<Map<String, Object>> errorsList = (List<Map<String, Object>>) errorsObj;
            
            for (Map<String, Object> errorMap : errorsList) {
                try {
                    GrammarError error = GrammarError.builder()
                        .startOffset(((Number) errorMap.get("startOffset")).intValue())
                        .endOffset(((Number) errorMap.get("endOffset")).intValue())
                        .errorType((String) errorMap.get("errorType"))
                        .suggestion((String) errorMap.get("suggestion"))
                        .explanation((String) errorMap.get("explanation"))
                        .build();
                    
                    errors.add(error);
                } catch (Exception e) {
                    log.warn("Error parsing grammar error: {}", e.getMessage());
                }
            }
        }
        
        return errors;
    }
    
    /**
     * Parse suggestions list từ response
     */
    @SuppressWarnings("unchecked")
    private List<GrammarSuggestion> parseSuggestionsList(Object suggestionsObj) {
        List<GrammarSuggestion> suggestions = new ArrayList<>();
        
        if (suggestionsObj instanceof List) {
            List<Map<String, Object>> suggestionsList = (List<Map<String, Object>>) suggestionsObj;
            
            for (Map<String, Object> suggestionMap : suggestionsList) {
                try {
                    GrammarSuggestion suggestion = GrammarSuggestion.builder()
                        .errorType((String) suggestionMap.get("errorType"))
                        .suggestion((String) suggestionMap.get("suggestion"))
                        .explanation((String) suggestionMap.get("explanation"))
                        .build();
                    
                    suggestions.add(suggestion);
                } catch (Exception e) {
                    log.warn("Error parsing grammar suggestion: {}", e.getMessage());
                }
            }
        }
        
        return suggestions;
    }
    
    /**
     * Parse batch grammar response
     */
    private List<GrammarCheckResponse> parseBatchGrammarResponse(List<String> texts, String aiResponse) {
        List<GrammarCheckResponse> responses = new ArrayList<>();
        
        try {
            // Split response by text numbers "1.", "2.", etc.
            String[] lines = aiResponse.split("\n");
            int textIndex = 0;
            StringBuilder currentResponse = new StringBuilder();
            
            for (String line : lines) {
                // Check if line starts with number pattern
                if (line.matches("^\\d+\\..*")) {
                    // Start of new text result
                    if (textIndex > 0 && textIndex <= texts.size()) {
                        String responseText = currentResponse.toString().trim();
                        responses.add(parseGrammarCheckResponse(texts.get(textIndex - 1), responseText, true));
                    }
                    
                    // Extract number
                    String[] parts = line.split("\\.", 2);
                    if (parts.length > 1) {
                        currentResponse = new StringBuilder(parts[1].trim());
                        textIndex++;
                    }
                } else {
                    currentResponse.append("\n").append(line);
                }
            }
            
            // Add last response
            if (textIndex > 0 && textIndex <= texts.size()) {
                String responseText = currentResponse.toString().trim();
                responses.add(parseGrammarCheckResponse(texts.get(textIndex - 1), responseText, true));
            }
            
        } catch (Exception e) {
            log.warn("Error parsing batch grammar response: {}, using fallback", e.getMessage());
            
            // Fallback: create simple responses from texts
            responses = texts.stream()
                .map(text -> GrammarCheckResponse.builder()
                    .originalText(text)
                    .correctedText(text)
                    .errors(Collections.emptyList())
                    .suggestions(Collections.emptyList())
                    .build())
                .collect(Collectors.toList());
        }
        
        return responses;
    }

    private String cleanJson(String raw) {
        if (raw == null || raw.trim().isEmpty()) return "{}";
        try {
            return com.vocabularyai.ai.util.JsonResponseExtractor.extractJsonString(raw);
        } catch (Exception e) {
            String trimmed = raw.trim();
            if (trimmed.startsWith("```json")) {
                trimmed = trimmed.substring(7);
            } else if (trimmed.startsWith("```")) {
                trimmed = trimmed.substring(3);
            }
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.length() - 3);
            }
            return trimmed.trim();
        }
    }
}
