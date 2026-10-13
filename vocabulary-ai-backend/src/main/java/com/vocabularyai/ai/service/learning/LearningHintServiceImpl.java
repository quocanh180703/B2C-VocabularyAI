package com.vocabularyai.ai.service.learning;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabularyai.ai.dto.response.HintResponse;
import com.vocabularyai.ai.service.cache.CacheService;
import com.vocabularyai.ai.service.gemini.GeminiAIClientService;
import com.vocabularyai.ai.service.prompt.PromptEngineeringService;
import com.vocabularyai.learning.entity.LearningProgress;
import com.vocabularyai.learning.repository.LearningProgressRepository;
import com.vocabularyai.vocabulary.entity.Word;
import com.vocabularyai.vocabulary.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * LearningHintService Implementation
 * 
 * Triển khai gợi ý học tập:
 * - Single progress hint với progressive difficulty
 * - Batch hints generation
 * - Smart caching (30 phút TTL)
 * - Difficulty-aware prompts
 * 
 * Dependencies:
 * - LearningProgressRepository: Load progress data
 * - WordRepository: Load word data
 * - GeminiAIClientService: Call AI API
 * - PromptEngineeringService: Build prompts
 * - CacheService: Cache hints
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LearningHintServiceImpl implements LearningHintService {
    
    private final LearningProgressRepository progressRepository;
    private final WordRepository wordRepository;
    private final GeminiAIClientService geminiService;
    private final PromptEngineeringService promptService;
    private final CacheService cacheService;
    private final ObjectMapper objectMapper;
    
    @Value("${ai.hint.cache-ttl-minutes:30}")
    private long hintCacheTtlMinutes;
    
    @Value("${ai.hint.batch-max-hints:50}")
    private int batchMaxHints;
    
    // Cache key prefix
    private static final String CACHE_KEY_HINT = "ai:hint:progress:%d:attempt:%d";
    
    // Difficulty levels
    private static final int LEVEL_1_MAX_ATTEMPTS = 1;
    private static final int LEVEL_2_MAX_ATTEMPTS = 2;
    
    // ==================== Single Progress Hint ====================
    
    @Override
    public HintResponse generateHint(Long progressId, Integer attemptCount, Long userId) {
        log.info("Generating hint for progress: {}, attempt: {}, user: {}", 
            progressId, attemptCount, userId);
        
        // Validate
        if (progressId == null || progressId <= 0) {
            throw new IllegalArgumentException("Progress ID must be positive");
        }
        
        if (attemptCount == null) {
            attemptCount = 0;
        }
        
        long startTime = System.currentTimeMillis();
        
        // 1. Load LearningProgress
        LearningProgress progress = progressRepository.findById(progressId)
            .orElseThrow(() -> {
                log.error("Learning progress not found: {}", progressId);
                return new IllegalArgumentException("Learning progress not found: " + progressId);
            });
        
        // 2. Load Word
        Word word = progress.getWord();
        if (word == null) {
            log.error("Word not found for progress: {}", progressId);
            throw new IllegalArgumentException("Word not found for progress: " + progressId);
        }
        
        // 3. Xác định difficulty level
        int difficultyLevel = calculateDifficultyLevel(attemptCount);
        
        // 4. Kiểm tra cache
        String cacheKey = String.format(CACHE_KEY_HINT, progressId, attemptCount);
        Optional<HintResponse> cachedHint = cacheService.get(cacheKey, HintResponse.class);
        
        if (cachedHint.isPresent()) {
            log.debug("Cache hit for hint, progress: {}", progressId);
            return cachedHint.get();
        }
        
        log.debug("Cache miss for hint, building response");
        
        // 5. Xây dựng hint prompt với difficulty level
        String hintPrompt = promptService.buildHintPrompt(progress, attemptCount);
        
        log.debug("Built hint prompt (level {}), estimated tokens: {}", 
            difficultyLevel, promptService.estimateTokenCount(hintPrompt));
        
        // 6. Gọi Gemini API
        String aiResponse;
        try {
            aiResponse = geminiService.generateResponse(hintPrompt, userId);
            log.debug("AI response received, length: {}", aiResponse.length());
        } catch (Exception e) {
            log.error("Error calling Gemini API for hint generation: {}", e.getMessage());
            throw e;
        }
        
        // 7. Parse response
        HintResponse response = parseHintResponse(progressId, difficultyLevel, aiResponse);
        
        // 8. Cache kết quả
        long cacheTtlSeconds = hintCacheTtlMinutes * 60;
        cacheService.set(cacheKey, response, cacheTtlSeconds);
        
        log.info("Hint generated and cached, took: {}ms", System.currentTimeMillis() - startTime);
        
        return response;
    }
    
    // ==================== Batch Hints Generation ====================
    
    @Override
    public List<HintResponse> generateHintsBatch(List<Long> progressIds, List<Integer> attemptCounts, Long userId) {
        log.info("Generating {} hints batch, user: {}", progressIds.size(), userId);
        
        // Validate
        if (progressIds == null || progressIds.isEmpty()) {
            throw new IllegalArgumentException("Progress IDs list cannot be empty");
        }
        
        if (progressIds.size() > batchMaxHints) {
            throw new IllegalArgumentException("Cannot generate hints for more than " + batchMaxHints + " progresses");
        }
        
        if (attemptCounts == null || attemptCounts.size() != progressIds.size()) {
            throw new IllegalArgumentException("Attempt counts list must match progress IDs size");
        }
        
        long startTime = System.currentTimeMillis();
        
        // 1. Load all progresses
        List<LearningProgress> progresses = progressRepository.findAllById(progressIds);
        
        if (progresses.size() != progressIds.size()) {
            Set<Long> foundIds = progresses.stream()
                .map(LearningProgress::getId)
                .collect(Collectors.toSet());
            Set<Long> missingIds = new HashSet<>(progressIds);
            missingIds.removeAll(foundIds);
            
            log.error("Some progresses not found: {}", missingIds);
            throw new IllegalArgumentException("Some progresses not found: " + missingIds);
        }
        
        // 2. Kiểm tra cache cho từng progress
        List<HintResponse> results = new ArrayList<>();
        List<Integer> indicesNeedGeneration = new ArrayList<>();
        
        for (int i = 0; i < progressIds.size(); i++) {
            Long progressId = progressIds.get(i);
            Integer attemptCount = attemptCounts.get(i);
            
            String cacheKey = String.format(CACHE_KEY_HINT, progressId, attemptCount);
            Optional<HintResponse> cached = cacheService.get(cacheKey, HintResponse.class);
            
            if (cached.isPresent()) {
                results.add(cached.get());
                log.debug("Cache hit for hint, progress: {}", progressId);
            } else {
                indicesNeedGeneration.add(i);
                log.debug("Cache miss for hint, progress: {}", progressId);
            }
        }
        
        // 3. Nếu có hints cần generate, gọi batch API
        if (!indicesNeedGeneration.isEmpty()) {
            log.debug("Need to generate {} hints from AI", indicesNeedGeneration.size());
            
            List<HintResponse> newHints = generateHintsBatchInternal(
                progresses, 
                progressIds, 
                attemptCounts, 
                indicesNeedGeneration, 
                userId
            );
            
            // 4. Cache tất cả results
            for (HintResponse hint : newHints) {
                // Find original index
                Long progressId = hint.getLearningProgressId();
                int originalIndex = progressIds.indexOf(progressId);
                Integer attemptCount = attemptCounts.get(originalIndex);
                
                String cacheKey = String.format(CACHE_KEY_HINT, progressId, attemptCount);
                long cacheTtlSeconds = hintCacheTtlMinutes * 60;
                cacheService.set(cacheKey, hint, cacheTtlSeconds);
            }
            
            results.addAll(newHints);
        }
        
        log.info("Batch hint generation completed: {} hints, took: {}ms", 
            progressIds.size(), System.currentTimeMillis() - startTime);
        
        return results;
    }
    
    /**
     * Internal batch hint generation (avoid recursion)
     */
    private List<HintResponse> generateHintsBatchInternal(
        List<LearningProgress> allProgresses,
        List<Long> progressIds,
        List<Integer> attemptCounts,
        List<Integer> indicesNeedGeneration,
        Long userId
    ) {
        List<HintResponse> responses = new ArrayList<>();
        
        // Tạo batch prompt
        StringBuilder batchPrompt = new StringBuilder();
        batchPrompt.append("Generate learning hints for the following ").append(indicesNeedGeneration.size())
            .append(" words:\n\n");
        
        for (Integer idx : indicesNeedGeneration) {
            LearningProgress progress = allProgresses.get(idx);
            Word word = progress.getWord();
            Integer attemptCount = attemptCounts.get(idx);
            int difficultyLevel = calculateDifficultyLevel(attemptCount);
            
            batchPrompt.append(idx + 1).append(". ")
                .append(word.getWord()).append(" (")
                .append("attempt: ").append(attemptCount).append(", ")
                .append("level: ").append(difficultyLevel).append(")\n");
        }
        
        batchPrompt.append("\nFor each word, provide:\n");
        batchPrompt.append("- Hint (appropriate to difficulty level)\n");
        batchPrompt.append("- Learning strategies\n");
        batchPrompt.append("- Brief explanation\n");
        
        log.debug("Built batch hint prompt for {} hints, tokens: {}", 
            indicesNeedGeneration.size(), promptService.estimateTokenCount(batchPrompt.toString()));
        
        try {
            // Gọi API
            String aiResponse = geminiService.generateResponse(batchPrompt.toString(), userId);
            
            // Parse batch response
            responses = parseBatchHintResponse(
                allProgresses, 
                progressIds, 
                attemptCounts, 
                indicesNeedGeneration, 
                aiResponse
            );
            
            log.debug("Batch hint API call succeeded, got {} hints", responses.size());
            
        } catch (Exception e) {
            log.error("Error calling Gemini API for batch hint generation: {}", e.getMessage());
            throw e;
        }
        
        return responses;
    }
    
    // ==================== Cache Operations ====================
    
    @Override
    public void invalidateHintCache(Long progressId, Integer attemptCount) {
        log.info("Invalidating hint cache for progress: {}, attempt: {}", progressId, attemptCount);
        
        String cacheKey = String.format(CACHE_KEY_HINT, progressId, attemptCount);
        cacheService.delete(cacheKey);
        
        log.info("Hint cache invalidated");
    }
    
    @Override
    public void invalidateAllHintCache() {
        log.warn("Invalidating ALL hint caches");
        
        cacheService.deletePattern("ai:hint:progress:*");
        
        log.warn("All hint caches invalidated");
    }
    
    @Override
    public boolean isHintCached(Long progressId, Integer attemptCount) {
        if (attemptCount == null) {
            attemptCount = 0;
        }
        
        String cacheKey = String.format(CACHE_KEY_HINT, progressId, attemptCount);
        Optional<HintResponse> cached = cacheService.get(cacheKey, HintResponse.class);
        return cached.isPresent();
    }
    
    @Override
    public HintResponse getCachedHint(Long progressId, Integer attemptCount) {
        if (attemptCount == null) {
            attemptCount = 0;
        }
        
        String cacheKey = String.format(CACHE_KEY_HINT, progressId, attemptCount);
        Optional<HintResponse> cached = cacheService.get(cacheKey, HintResponse.class);
        return cached.orElse(null);
    }
    
    // ==================== Helper Methods ====================
    
    /**
     * Calculate difficulty level dựa trên attempt count
     * 
     * Level 1: attempt 0-1 (minimal hint)
     * Level 2: attempt 2 (moderate hint)
     * Level 3: attempt 3+ (detailed hint)
     */
    private int calculateDifficultyLevel(Integer attemptCount) {
        if (attemptCount == null || attemptCount <= LEVEL_1_MAX_ATTEMPTS) {
            return 1;
        } else if (attemptCount <= LEVEL_2_MAX_ATTEMPTS) {
            return 2;
        } else {
            return 3;
        }
    }
    
    /**
     * Parse AI response thành HintResponse
     * 
     * AI response format (JSON):
     * {
     *   "hint": "...",
     *   "strategies": ["strategy1", "strategy2"],
     *   "explanation": "..."
     * }
     */
    private HintResponse parseHintResponse(Long progressId, Integer difficultyLevel, String aiResponse) {
        try {
            // Try to parse as JSON
            Map<String, Object> parsed = objectMapper.readValue(aiResponse, Map.class);
            
            String hint = (String) parsed.getOrDefault("hint", aiResponse);
            
            @SuppressWarnings("unchecked")
            List<String> strategies = (List<String>) parsed.getOrDefault("strategies", Collections.emptyList());
            
            String explanation = (String) parsed.get("explanation");
            
            return HintResponse.builder()
                .learningProgressId(progressId)
                .hint(hint)
                .hintLevel(difficultyLevel)
                .strategies(strategies)
                .explanation(explanation)
                .build();
                
        } catch (Exception e) {
            log.warn("Error parsing AI response as JSON: {}, using fallback", e.getMessage());
            
            // Fallback: use raw response as hint
            return HintResponse.builder()
                .learningProgressId(progressId)
                .hint(aiResponse)
                .hintLevel(difficultyLevel)
                .strategies(Collections.emptyList())
                .explanation(null)
                .build();
        }
    }
    
    /**
     * Parse batch hint response
     */
    private List<HintResponse> parseBatchHintResponse(
        List<LearningProgress> allProgresses,
        List<Long> progressIds,
        List<Integer> attemptCounts,
        List<Integer> indicesNeedGeneration,
        String aiResponse
    ) {
        List<HintResponse> responses = new ArrayList<>();
        
        try {
            // Split response by number pattern "1.", "2.", etc.
            String[] lines = aiResponse.split("\n");
            int responseIndex = 0;
            StringBuilder currentHintText = new StringBuilder();
            
            for (String line : lines) {
                // Check if line starts with number pattern
                if (line.matches("^\\d+\\..*")) {
                    // Start of new hint
                    if (responseIndex > 0 && responseIndex <= indicesNeedGeneration.size()) {
                        int originalIdx = indicesNeedGeneration.get(responseIndex - 1);
                        int difficultyLevel = calculateDifficultyLevel(attemptCounts.get(originalIdx));
                        HintResponse hint = parseHintResponse(
                            progressIds.get(originalIdx), 
                            difficultyLevel, 
                            currentHintText.toString().trim()
                        );
                        responses.add(hint);
                    }
                    
                    // Extract number
                    String[] parts = line.split("\\.", 2);
                    if (parts.length > 1) {
                        currentHintText = new StringBuilder(parts[1].trim());
                        responseIndex++;
                    }
                } else {
                    currentHintText.append("\n").append(line);
                }
            }
            
            // Add last hint
            if (responseIndex > 0 && responseIndex <= indicesNeedGeneration.size()) {
                int originalIdx = indicesNeedGeneration.get(responseIndex - 1);
                int difficultyLevel = calculateDifficultyLevel(attemptCounts.get(originalIdx));
                HintResponse hint = parseHintResponse(
                    progressIds.get(originalIdx), 
                    difficultyLevel, 
                    currentHintText.toString().trim()
                );
                responses.add(hint);
            }
            
        } catch (Exception e) {
            log.warn("Error parsing batch hint response: {}, using fallback", e.getMessage());
            
            // Fallback: create simple hints
            responses = indicesNeedGeneration.stream()
                .map(idx -> {
                    int difficultyLevel = calculateDifficultyLevel(attemptCounts.get(idx));
                    return HintResponse.builder()
                        .learningProgressId(progressIds.get(idx))
                        .hint("Try again and think carefully")
                        .hintLevel(difficultyLevel)
                        .strategies(Collections.emptyList())
                        .explanation(null)
                        .build();
                })
                .collect(Collectors.toList());
        }
        
        return responses;
    }
}
