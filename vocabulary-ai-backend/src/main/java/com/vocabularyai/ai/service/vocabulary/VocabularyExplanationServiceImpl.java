package com.vocabularyai.ai.service.vocabulary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabularyai.ai.dto.response.ExplanationResponse;
import com.vocabularyai.ai.service.cache.CacheService;
import com.vocabularyai.ai.service.gemini.GeminiAIClientService;
import com.vocabularyai.ai.service.prompt.PromptEngineeringService;
import com.vocabularyai.vocabulary.entity.Word;
import com.vocabularyai.vocabulary.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * VocabularyExplanationService Implementation
 * 
 * Triển khai giải thích từ vựng:
 * - Single word explanation với cache
 * - Batch word explanation
 * - Context-aware explanations
 * - Smart caching (24 giờ TTL)
 * 
 * Dependencies:
 * - WordRepository: Lấy word data từ DB
 * - GeminiAIClientService: Gọi AI API
 * - PromptEngineeringService: Xây dựng prompt
 * - CacheService: Cache explanations
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class VocabularyExplanationServiceImpl implements VocabularyExplanationService {
    
    private final WordRepository wordRepository;
    private final GeminiAIClientService geminiService;
    private final PromptEngineeringService promptService;
    private final CacheService cacheService;
    private final ObjectMapper objectMapper;
    
    @Value("${ai.explanation.cache-ttl-hours:24}")
    private long explanationCacheTtlHours;
    
    @Value("${ai.explanation.batch-max-words:100}")
    private int batchMaxWords;
    
    // Cache key prefixes
    private static final String CACHE_KEY_EXPLANATION = "ai:explanation:word:%d";
    private static final String CACHE_KEY_EXPLANATION_WITH_CONTEXT = "ai:explanation:word:%d:context:%s";
    
    // ==================== Single Word Explanation ====================
    
    @Override
    @Transactional(readOnly = true)
    public ExplanationResponse explainWord(Long wordId, String context, Long userId) {
        log.info("Explaining word: {}, context: {}, user: {}", wordId, context, userId);
        
        long startTime = System.currentTimeMillis();
        
        // 1. Kiểm tra cache trước
        String cacheKey = buildCacheKey(wordId, context);
        Optional<ExplanationResponse> cachedExplanation = cacheService.get(cacheKey, ExplanationResponse.class);
        
        if (cachedExplanation.isPresent()) {
            log.debug("Cache hit for word: {}", wordId);
            return cachedExplanation.get();
        }
        
        log.debug("Cache miss for word: {}, building explanation", wordId);
        
        // 2. Load word từ DB
        Word word = wordRepository.findById(wordId)
            .orElseThrow(() -> {
                log.error("Word not found: {}", wordId);
                return new IllegalArgumentException("Word not found: " + wordId);
            });
        
        // 3. Xây dựng vocabulary prompt
        String vocabularyPrompt = promptService.buildVocabularyPrompt(word, context);
        
        log.debug("Built vocabulary prompt, estimated tokens: {}", 
            promptService.estimateTokenCount(vocabularyPrompt));
        
        // 4. Gọi Gemini API
        String aiResponse;
        try {
            aiResponse = geminiService.generateResponse(vocabularyPrompt, userId);
            log.debug("AI response received, length: {}", aiResponse.length());
        } catch (Exception e) {
            log.error("Error calling Gemini API for word explanation: {}", e.getMessage());
            throw e;
        }
        
        // 5. Parse response thành ExplanationResponse
        ExplanationResponse explanation = parseExplanationResponse(word, aiResponse);
        
        // 6. Cache kết quả
        long cacheTtlSeconds = explanationCacheTtlHours * 3600;
        cacheService.set(cacheKey, explanation, cacheTtlSeconds);
        
        log.info("Explanation created and cached for word: {}, took: {}ms", 
            wordId, System.currentTimeMillis() - startTime);
        
        return explanation;
    }
    
    // ==================== Batch Words Explanation ====================
    
    @Override
    @Transactional(readOnly = true)
    public List<ExplanationResponse> explainWords(List<Long> wordIds, Long userId) {
        log.info("Explaining {} words, user: {}", wordIds.size(), userId);
        
        // 1. Validate
        if (wordIds == null || wordIds.isEmpty()) {
            throw new IllegalArgumentException("Word IDs list cannot be empty");
        }
        
        if (wordIds.size() > batchMaxWords) {
            throw new IllegalArgumentException("Cannot explain more than " + batchMaxWords + " words at once");
        }
        
        long startTime = System.currentTimeMillis();
        
        // 2. Load all words từ DB
        List<Word> words = wordRepository.findAllById(wordIds);
        
        if (words.size() != wordIds.size()) {
            Set<Long> foundIds = words.stream()
                .map(Word::getId)
                .collect(Collectors.toSet());
            Set<Long> missingIds = new HashSet<>(wordIds);
            missingIds.removeAll(foundIds);
            
            log.error("Some words not found: {}", missingIds);
            throw new IllegalArgumentException("Some words not found: " + missingIds);
        }
        
        // 3. Kiểm tra cache cho từng word
        List<ExplanationResponse> results = new ArrayList<>();
        List<Word> wordsNeedExplanation = new ArrayList<>();
        
        for (Word word : words) {
            String cacheKey = buildCacheKey(word.getId(), null);
            Optional<ExplanationResponse> cached = cacheService.get(cacheKey, ExplanationResponse.class);
            
            if (cached.isPresent()) {
                results.add(cached.get());
                log.debug("Cache hit for word: {}", word.getId());
            } else {
                wordsNeedExplanation.add(word);
                log.debug("Cache miss for word: {}", word.getId());
            }
        }
        
        // 4. Nếu có words cần explanation, gọi API batch
        if (!wordsNeedExplanation.isEmpty()) {
            log.debug("Need to fetch {} words from AI", wordsNeedExplanation.size());
            
            List<ExplanationResponse> newExplanations = explainWordsBatch(wordsNeedExplanation, userId);
            
            // 5. Cache tất cả results
            for (ExplanationResponse explanation : newExplanations) {
                String cacheKey = buildCacheKey(explanation.getWordId(), null);
                long cacheTtlSeconds = explanationCacheTtlHours * 3600;
                cacheService.set(cacheKey, explanation, cacheTtlSeconds);
            }
            
            results.addAll(newExplanations);
        }
        
        // 6. Sort results theo order của input
        Map<Long, ExplanationResponse> explanationMap = results.stream()
            .collect(Collectors.toMap(ExplanationResponse::getWordId, e -> e));
        
        List<ExplanationResponse> sortedResults = wordIds.stream()
            .map(explanationMap::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
        log.info("Batch explanation completed: {} words, took: {}ms", 
            wordIds.size(), System.currentTimeMillis() - startTime);
        
        return sortedResults;
    }
    
    /**
     * Gọi Gemini API batch cho multiple words
     */
    private List<ExplanationResponse> explainWordsBatch(List<Word> words, Long userId) {
        List<ExplanationResponse> explanations = new ArrayList<>();
        
        // Tạo batch prompt cho tất cả words
        StringBuilder batchPrompt = new StringBuilder();
        batchPrompt.append("Explain the following ").append(words.size()).append(" English words:\n\n");
        
        for (int i = 0; i < words.size(); i++) {
            Word word = words.get(i);
            batchPrompt.append(i + 1).append(". ")
                .append(word.getWord())
                .append(" (")
                .append(word.getPartOfSpeech() != null ? word.getPartOfSpeech() : "word")
                .append(")\n");
        }
        
        batchPrompt.append("\nFor each word, provide:\n");
        batchPrompt.append("- Definition\n");
        batchPrompt.append("- 2-3 examples\n");
        batchPrompt.append("- Related words (synonyms/antonyms)\n");
        batchPrompt.append("- Etymology if interesting\n");
        
        log.debug("Built batch prompt for {} words, tokens: {}", 
            words.size(), promptService.estimateTokenCount(batchPrompt.toString()));
        
        try {
            // Gọi API
            String aiResponse = geminiService.generateResponse(batchPrompt.toString(), userId);
            
            // Parse batch response
            explanations = parseBatchExplanationResponse(words, aiResponse);
            
            log.debug("Batch explanation API call succeeded, got {} explanations", explanations.size());
            
        } catch (Exception e) {
            log.error("Error calling Gemini API for batch explanation: {}", e.getMessage());
            throw e;
        }
        
        return explanations;
    }
    
    // ==================== Cache Operations ====================
    
    @Override
    public void invalidateExplanationCache(Long wordId) {
        log.info("Invalidating cache for word: {}", wordId);
        
        String cacheKey = buildCacheKey(wordId, null);
        cacheService.delete(cacheKey);
        
        // Also invalidate context-aware cache
        cacheService.deletePattern("ai:explanation:word:" + wordId + ":context:*");
        
        log.info("Cache invalidated for word: {}", wordId);
    }
    
    @Override
    public void invalidateAllExplanationCache() {
        log.warn("Invalidating ALL explanation caches");
        
        cacheService.deletePattern("ai:explanation:word:*");
        
        log.warn("All explanation caches invalidated");
    }
    
    @Override
    public boolean isExplanationCached(Long wordId, String context) {
        String cacheKey = buildCacheKey(wordId, context);
        Optional<ExplanationResponse> cached = cacheService.get(cacheKey, ExplanationResponse.class);
        return cached.isPresent();
    }
    
    @Override
    public ExplanationResponse getCachedExplanation(Long wordId, String context) {
        String cacheKey = buildCacheKey(wordId, context);
        Optional<ExplanationResponse> cached = cacheService.get(cacheKey, ExplanationResponse.class);
        return cached.orElse(null);
    }
    
    // ==================== Helper Methods ====================
    
    /**
     * Xây dựng cache key
     * - Nếu không có context: "ai:explanation:word:123"
     * - Nếu có context: "ai:explanation:word:123:context:<hashed_context>"
     */
    private String buildCacheKey(Long wordId, String context) {
        if (context == null || context.trim().isEmpty()) {
            return String.format(CACHE_KEY_EXPLANATION, wordId);
        }
        
        String hashedContext = hashContext(context);
        return String.format(CACHE_KEY_EXPLANATION_WITH_CONTEXT, wordId, hashedContext);
    }
    
    /**
     * Hash context string (SHA-256) để tạo cache key
     */
    private String hashContext(String context) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(context.getBytes());
            
            // Convert to hex
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            // Return first 16 chars
            return hexString.substring(0, 16);
            
        } catch (Exception e) {
            log.warn("Error hashing context: {}, using fallback", e.getMessage());
            return Integer.toHexString(context.hashCode());
        }
    }
    
    /**
     * Parse AI response thành ExplanationResponse
     * 
     * AI response format (JSON):
     * {
     *   "explanation": "...",
     *   "examples": ["...", "..."],
     *   "etymology": "...",
     *   "relatedWords": ["...", "..."]
     * }
     */
    private ExplanationResponse parseExplanationResponse(Word word, String aiResponse) {
        try {
            // Try to parse as JSON
            Map<String, Object> parsed = objectMapper.readValue(aiResponse, Map.class);
            
            return ExplanationResponse.builder()
                .wordId(word.getId())
                .word(word.getWord())
                .explanation((String) parsed.getOrDefault("explanation", aiResponse))
                .examples(getListFromMap(parsed, "examples"))
                .etymology((String) parsed.get("etymology"))
                .relatedWords(getListFromMap(parsed, "relatedWords"))
                .partOfSpeech(word.getPartOfSpeech())
                .build();
                
        } catch (Exception e) {
            log.warn("Error parsing AI response as JSON: {}, using fallback", e.getMessage());
            
            // Fallback: use raw response
            return ExplanationResponse.builder()
                .wordId(word.getId())
                .word(word.getWord())
                .explanation(aiResponse)
                .examples(Collections.singletonList(word.getExample() != null ? word.getExample() : ""))
                .etymology(null)
                .relatedWords(Collections.emptyList())
                .partOfSpeech(word.getPartOfSpeech())
                .build();
        }
    }
    
    /**
     * Parse batch explanation response
     * 
     * AI response format:
     * 1. Word 1: [explanation]
     * 2. Word 2: [explanation]
     * ...
     */
    @SuppressWarnings("unchecked")
    private List<ExplanationResponse> parseBatchExplanationResponse(List<Word> words, String aiResponse) {
        List<ExplanationResponse> explanations = new ArrayList<>();
        
        try {
            // Try to split response by word numbers
            String[] lines = aiResponse.split("\n");
            int wordIndex = 0;
            StringBuilder currentExplanation = new StringBuilder();
            
            for (String line : lines) {
                // Check if line starts with number pattern "1.", "2.", etc.
                if (line.matches("^\\d+\\..*")) {
                    // Start of new word
                    if (wordIndex > 0 && wordIndex <= words.size()) {
                        String explanation = currentExplanation.toString().trim();
                        explanations.add(createExplanationFromText(words.get(wordIndex - 1), explanation));
                    }
                    
                    // Extract word number
                    String[] parts = line.split("\\.", 2);
                    if (parts.length > 1) {
                        currentExplanation = new StringBuilder(parts[1].trim());
                        wordIndex++;
                    }
                } else {
                    currentExplanation.append("\n").append(line);
                }
            }
            
            // Add last explanation
            if (wordIndex > 0 && wordIndex <= words.size()) {
                String explanation = currentExplanation.toString().trim();
                explanations.add(createExplanationFromText(words.get(wordIndex - 1), explanation));
            }
            
        } catch (Exception e) {
            log.warn("Error parsing batch explanation response: {}, using fallback", e.getMessage());
            
            // Fallback: create simple explanations from word data
            explanations = words.stream()
                .map(word -> createExplanationFromText(word, word.getDefinition() != null ? word.getDefinition() : ""))
                .collect(Collectors.toList());
        }
        
        return explanations;
    }
    
    /**
     * Tạo ExplanationResponse từ text content
     */
    private ExplanationResponse createExplanationFromText(Word word, String explanation) {
        return ExplanationResponse.builder()
            .wordId(word.getId())
            .word(word.getWord())
            .explanation(explanation)
            .examples(word.getExample() != null ? Collections.singletonList(word.getExample()) : Collections.emptyList())
            .etymology(null)
            .relatedWords(Collections.emptyList())
            .partOfSpeech(word.getPartOfSpeech())
            .build();
    }
    
    /**
     * Lấy list từ Map, fallback to empty list
     */
    @SuppressWarnings("unchecked")
    private List<String> getListFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof List) {
            return (List<String>) value;
        }
        return Collections.emptyList();
    }
}
