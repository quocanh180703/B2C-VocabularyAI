package com.vocabularyai.ai.service.grammar;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabularyai.ai.dto.response.GrammarCheckResponse;
import com.vocabularyai.ai.dto.response.GrammarError;
import com.vocabularyai.ai.dto.response.GrammarSuggestion;
import com.vocabularyai.ai.dto.request.TopicGrammarCheckRequest;
import com.vocabularyai.ai.dto.response.TopicGrammarCheckResponse;
import com.vocabularyai.ai.dto.request.GeneratePracticePromptRequest;
import com.vocabularyai.ai.dto.response.PracticePromptResponse;
import com.vocabularyai.ai.dto.request.CheckTranslationRequest;
import com.vocabularyai.ai.dto.response.TranslationCheckResponse;
import com.vocabularyai.ai.entity.GrammarPracticePrompt;
import com.vocabularyai.ai.repository.GrammarPracticePromptRepository;
import com.vocabularyai.vocabulary.entity.Word;
import com.vocabularyai.vocabulary.repository.WordRepository;
import com.vocabularyai.ai.dto.response.TopicOutlineReport;
import com.vocabularyai.ai.util.GrammarWordMatchHelper;
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

    private final GrammarPracticePromptRepository promptRepository;
    private final WordRepository wordRepository;

    
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

    @Override
    public TopicGrammarCheckResponse checkTopicGrammar(TopicGrammarCheckRequest request, Long userId) {
        log.info("Checking topic grammar for topic: {}, level: {}, user: {}", 
            request.getTopic(), request.getLevel(), userId);

        String text = request.getText();
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text cannot be blank");
        }

        List<String> suggestedWords = request.getSuggestedWords() != null ? request.getSuggestedWords() : Collections.emptyList();
        List<String> structures = request.getStructures() != null ? request.getStructures() : Collections.emptyList();

        List<String> usedWords = new ArrayList<>();
        List<String> unusedWords = new ArrayList<>();
        for (String word : suggestedWords) {
            if (GrammarWordMatchHelper.containsWord(text, word)) {
                usedWords.add(word);
            } else {
                unusedWords.add(word);
            }
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an expert English teacher evaluating a student's writing assignment.\n");
        prompt.append("Topic: ").append(request.getTopic() != null ? request.getTopic() : "General").append("\n");
        prompt.append("CEFR Level: ").append(request.getLevel() != null ? request.getLevel() : "B1").append("\n");
        prompt.append("Suggested Vocabulary List: ").append(String.join(", ", suggestedWords)).append("\n");
        prompt.append("Target Grammar Structures: ").append(String.join("; ", structures)).append("\n\n");
        prompt.append("Student Writing:\n\"\"\"\n").append(text).append("\n\"\"\"\n\n");
        prompt.append("Tasks:\n");
        prompt.append("1. Check grammar, spelling, punctuation, and style.\n");
        prompt.append("2. Provide the corrected text.\n");
        prompt.append("3. Calculate a score from 0 to 100 based on grammatical accuracy, natural flow, and vocabulary usage.\n");
        prompt.append("4. Identify which of the Target Grammar Structures were used correctly (usedStructures) and which were not (unusedStructures).\n");
        prompt.append("5. Provide outlineFeedback (in Vietnamese, warm and encouraging coaching tone, 1-2 sentences) about how well they applied the suggested words and structures.\n\n");
        prompt.append("Respond STRICTLY in JSON format with no markdown wrappers:\n");
        prompt.append("{\n");
        prompt.append("  \"correctedText\": \"...\",\n");
        prompt.append("  \"score\": 85,\n");
        prompt.append("  \"errors\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"startOffset\": 0,\n");
        prompt.append("      \"endOffset\": 5,\n");
        prompt.append("      \"errorType\": \"GRAMMAR\",\n");
        prompt.append("      \"suggestion\": \"...\",\n");
        prompt.append("      \"explanation\": \"...\"\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"suggestions\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"errorType\": \"STYLE\",\n");
        prompt.append("      \"suggestion\": \"...\",\n");
        prompt.append("      \"explanation\": \"...\"\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"usedStructures\": [\"...\"],\n");
        prompt.append("  \"unusedStructures\": [\"...\"],\n");
        prompt.append("  \"outlineFeedback\": \"...\"\n");
        prompt.append("}\n");

        String aiResponse = geminiService.generateResponse(prompt.toString(), userId);
        return parseTopicGrammarResponse(text, aiResponse, suggestedWords, usedWords, unusedWords, structures);
    }

    @SuppressWarnings("unchecked")
    private TopicGrammarCheckResponse parseTopicGrammarResponse(
            String originalText,
            String aiResponse,
            List<String> suggestedWords,
            List<String> usedWords,
            List<String> unusedWords,
            List<String> originalStructures) {
        try {
            String cleaned = cleanJson(aiResponse);
            Map<String, Object> parsed = objectMapper.readValue(cleaned, Map.class);

            String correctedText = (String) parsed.getOrDefault("correctedText", originalText);
            List<GrammarError> errors = parseErrorsList(parsed.get("errors"));
            List<GrammarSuggestion> suggestions = parseSuggestionsList(parsed.get("suggestions"));

            Integer score = null;
            if (parsed.get("score") instanceof Number) {
                score = ((Number) parsed.get("score")).intValue();
            }
            if (score == null) {
                score = Math.max(0, 100 - errors.size() * 15);
            }

            List<String> usedStructures = new ArrayList<>();
            if (parsed.get("usedStructures") instanceof List) {
                for (Object item : (List<?>) parsed.get("usedStructures")) {
                    if (item != null) usedStructures.add(item.toString());
                }
            }

            List<String> unusedStructures = new ArrayList<>();
            if (parsed.get("unusedStructures") instanceof List) {
                for (Object item : (List<?>) parsed.get("unusedStructures")) {
                    if (item != null) unusedStructures.add(item.toString());
                }
            }
            if (usedStructures.isEmpty() && unusedStructures.isEmpty() && !originalStructures.isEmpty()) {
                unusedStructures.addAll(originalStructures);
            }

            String feedback = (String) parsed.get("outlineFeedback");
            if (feedback == null || feedback.trim().isEmpty()) {
                if (usedWords.size() == suggestedWords.size() && !suggestedWords.isEmpty()) {
                    feedback = "Xuất sắc! Bạn đã sử dụng đầy đủ toàn bộ từ vựng gợi ý của chủ đề.";
                } else if (!usedWords.isEmpty()) {
                    feedback = String.format("Khá tốt! Bạn đã áp dụng %d/%d từ vựng gợi ý. Hãy thử kết hợp thêm các từ còn lại nhé.",
                        usedWords.size(), suggestedWords.size());
                } else {
                    feedback = "Bạn đã hoàn thành bài viết. Hãy thử áp dụng các từ vựng và cấu trúc gợi ý để câu văn phong phú hơn nhé.";
                }
            }

            TopicOutlineReport outlineReport = TopicOutlineReport.builder()
                .targetWordCount(suggestedWords.size())
                .usedWordCount(usedWords.size())
                .usedWords(usedWords)
                .unusedWords(unusedWords)
                .usedStructures(usedStructures)
                .unusedStructures(unusedStructures)
                .outlineFeedback(feedback)
                .build();

            return TopicGrammarCheckResponse.builder()
                .originalText(originalText)
                .correctedText(correctedText)
                .score(score)
                .errors(errors)
                .suggestions(suggestions)
                .outlineReport(outlineReport)
                .build();

        } catch (Exception e) {
            log.warn("Error parsing topic grammar AI response: {}, using fallback", e.getMessage());

            int fallbackScore = Math.max(50, 100 - (suggestedWords.size() - usedWords.size()) * 10);
            TopicOutlineReport outlineReport = TopicOutlineReport.builder()
                .targetWordCount(suggestedWords.size())
                .usedWordCount(usedWords.size())
                .usedWords(usedWords)
                .unusedWords(unusedWords)
                .usedStructures(Collections.emptyList())
                .unusedStructures(originalStructures)
                .outlineFeedback(String.format("Bạn đã sử dụng %d/%d từ gợi ý.", usedWords.size(), suggestedWords.size()))
                .build();

            return TopicGrammarCheckResponse.builder()
                .originalText(originalText)
                .correctedText(originalText)
                .score(fallbackScore)
                .errors(Collections.emptyList())
                .suggestions(Collections.emptyList())
                .outlineReport(outlineReport)
                .build();
        }
    }


    @Override
    public PracticePromptResponse getExistingPracticePrompt(Long wordId, String mode) {
        if (wordId == null) {
            return null;
        }
        String m = (mode != null && !mode.trim().isEmpty()) ? mode.trim().toUpperCase() : "SENTENCE";
        Word word = wordRepository.findById(wordId).orElse(null);
        if (word == null) {
            return null;
        }

        List<GrammarPracticePrompt> existingPrompts = promptRepository.findByWordIdAndMode(word.getId(), m);
        if (existingPrompts != null && !existingPrompts.isEmpty()) {
            List<GrammarPracticePrompt> shuffled = new ArrayList<>(existingPrompts);
            Collections.shuffle(shuffled);
            GrammarPracticePrompt selected = shuffled.get(0);
            return PracticePromptResponse.builder()
                .id(selected.getId())
                .wordId(word.getId())
                .targetWord(word.getWord())
                .targetMeaning(word.getMeaning())
                .mode(m)
                .viText(selected.getViText())
                .build();
        }
        return null;
    }

    @Override
    public PracticePromptResponse generatePracticePrompt(GeneratePracticePromptRequest request, Long userId) {
        log.info("Generating grammar practice prompt for wordId: {}, mode: {}, forceNew: {}, user: {}",
            request.getWordId(), request.getMode(), request.getForceNew(), userId);

        String mode = request.getMode() != null && !request.getMode().trim().isEmpty() 
            ? request.getMode().trim().toUpperCase() : "SENTENCE";

        Word word = wordRepository.findById(request.getWordId())
            .orElseThrow(() -> new IllegalArgumentException("Word not found with id: " + request.getWordId()));

        boolean forceNew = Boolean.TRUE.equals(request.getForceNew());
        List<GrammarPracticePrompt> existingPrompts = promptRepository.findByWordIdAndMode(word.getId(), mode);

        // 1. Nếu không yêu cầu tạo mới (forceNew != true): chỉ cần có >= 1 đề trong DB là dùng lại ngay (tốn 0 token)
        if (!forceNew && existingPrompts != null && !existingPrompts.isEmpty()) {
            List<GrammarPracticePrompt> shuffled = new ArrayList<>(existingPrompts);
            Collections.shuffle(shuffled);
            GrammarPracticePrompt selected = shuffled.get(0);
            return PracticePromptResponse.builder()
                .id(selected.getId())
                .wordId(word.getId())
                .targetWord(word.getWord())
                .targetMeaning(word.getMeaning())
                .mode(mode)
                .viText(selected.getViText())
                .build();
        }

        // 2. Nếu forceNew == true nhưng DB đã có >= 3 đề: không cần gọi AI nữa, chỉ cần bốc ngẫu nhiên 1 đề trong DB
        if (forceNew && existingPrompts != null && existingPrompts.size() >= 3) {
            List<GrammarPracticePrompt> shuffled = new ArrayList<>(existingPrompts);
            Collections.shuffle(shuffled);
            GrammarPracticePrompt selected = shuffled.get(0);
            return PracticePromptResponse.builder()
                .id(selected.getId())
                .wordId(word.getId())
                .targetWord(word.getWord())
                .targetMeaning(word.getMeaning())
                .mode(mode)
                .viText(selected.getViText())
                .build();
        }

        // 3. Chưa có đề nào (size == 0) hoặc forceNew == true và size < 3: Gọi AI tạo đề mới
        try {
            StringBuilder prompt = new StringBuilder();
            prompt.append("Bạn là một chuyên gia khảo thí và biên soạn đề thi tiếng Anh chuyên nghiệp.\n");
            prompt.append("Nhiệm vụ: Hãy tạo đúng 1 đề bài dịch tiếng Việt tự nhiên, phù hợp để người học dịch sang tiếng Anh sử dụng từ vựng mục tiêu sau:\n");
            prompt.append("- Từ tiếng Anh mục tiêu: ").append(word.getWord()).append("\n");
            prompt.append("- Nghĩa tiếng Việt: ").append(word.getMeaning() != null ? word.getMeaning() : "").append("\n");
            prompt.append("- Cấp độ: ").append(word.getLevel() != null ? word.getLevel() : "B1").append("\n");
            if ("PARAGRAPH".equals(mode)) {
                prompt.append("- Hình thức: Đoạn văn ngắn 2-3 câu tiếng Việt tự nhiên, liền mạch về một ngữ cảnh đời sống hoặc công việc.\n");
            } else {
                prompt.append("- Hình thức: Đúng 1 câu tiếng Việt tự nhiên đời thường, giàu ngữ cảnh.\n");
            }
            prompt.append("QUY TẮC BẮT BUỘC:\n");
            prompt.append("1. Đề bài PHẢI HOÀN TOÀN BẰNG TIẾNG VIỆT.\n");
            prompt.append("2. TUYỆT ĐỐI KHÔNG xuất hiện câu tiếng Anh mẫu, không gợi ý đáp án tiếng Anh, không kèm bản dịch tiếng Anh.\n");
            prompt.append("3. Chỉ trả về duy nhất nội dung câu/đoạn tiếng Việt, không kèm lời chào hay giải thích thêm.\n");

            String aiResponse = geminiService.generateResponse(prompt.toString(), userId);
            String viText = aiResponse != null ? aiResponse.trim() : "";
            if (viText.startsWith("\"") && viText.endsWith("\"") && viText.length() > 2) {
                viText = viText.substring(1, viText.length() - 1).trim();
            }

            GrammarPracticePrompt savedPrompt = promptRepository.save(GrammarPracticePrompt.builder()
                .wordId(word.getId())
                .mode(mode)
                .level(word.getLevel())
                .viText(viText)
                .build());

            return PracticePromptResponse.builder()
                .id(savedPrompt.getId())
                .wordId(word.getId())
                .targetWord(word.getWord())
                .targetMeaning(word.getMeaning())
                .mode(mode)
                .viText(viText)
                .build();
        } catch (Exception e) {
            log.error("Error generating prompt with AI: {}", e.getMessage());
            // Fallback: nếu gọi AI lỗi nhưng DB đã có đề cũ, trả về đề cũ để người dùng không bị gián đoạn
            if (existingPrompts != null && !existingPrompts.isEmpty()) {
                GrammarPracticePrompt fallback = existingPrompts.get(0);
                return PracticePromptResponse.builder()
                    .id(fallback.getId())
                    .wordId(word.getId())
                    .targetWord(word.getWord())
                    .targetMeaning(word.getMeaning())
                    .mode(mode)
                    .viText(fallback.getViText())
                    .build();
            }
            throw e;
        }
    }

    @Override
    public TranslationCheckResponse checkTranslation(CheckTranslationRequest request, Long userId) {
        log.info("Checking translation for targetWord: {}, user: {}", request.getTargetWord(), userId);

        String text = request.getText();
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Translation text cannot be blank");
        }

        String targetWord = request.getTargetWord() != null ? request.getTargetWord().trim() : "";
        boolean isUsed = GrammarWordMatchHelper.containsWord(text, targetWord);

        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an expert English teacher evaluating a student's Vietnamese-to-English translation.\n\n");
        prompt.append("Original Vietnamese text to translate:\n\"\"\"\n").append(request.getViPrompt()).append("\n\"\"\"\n\n");
        prompt.append("Target English Vocabulary to use: ").append(targetWord);
        if (request.getTargetMeaning() != null) {
            prompt.append(" (").append(request.getTargetMeaning()).append(")");
        }
        prompt.append("\n\nStudent's English Translation:\n\"\"\"\n").append(text).append("\n\"\"\"\n\n");
        prompt.append("Tasks:\n");
        prompt.append("1. Check if the translation accurately conveys the original Vietnamese meaning (catch mistranslation, wrong meaning, unnatural phrasing).\n");
        prompt.append("2. Check grammar, spelling, punctuation, and style errors.\n");
        prompt.append("3. Verify if target vocabulary '").append(targetWord).append("' was used appropriately.\n");
        prompt.append("4. Provide the ideal, natural corrected English translation.\n");
        prompt.append("5. Calculate a score (0 to 100) combining translation fidelity, grammar correctness, and target word usage.\n");
        prompt.append("6. Provide friendly Vietnamese feedback (1-2 sentences) explaining whether they captured the meaning and used the target word well.\n\n");
        prompt.append("Respond STRICTLY in JSON format with no markdown wrappers:\n");
        prompt.append("{\n");
        prompt.append("  \"correctedText\": \"...\",\n");
        prompt.append("  \"score\": 88,\n");
        prompt.append("  \"errors\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"startOffset\": 0,\n");
        prompt.append("      \"endOffset\": 5,\n");
        prompt.append("      \"errorType\": \"GRAMMAR\",\n");
        prompt.append("      \"suggestion\": \"...\",\n");
        prompt.append("      \"explanation\": \"...\"\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"suggestions\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"errorType\": \"STYLE\",\n");
        prompt.append("      \"suggestion\": \"...\",\n");
        prompt.append("      \"explanation\": \"...\"\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"feedback\": \"...\"\n");
        prompt.append("}\n");

        String aiResponse = geminiService.generateResponse(prompt.toString(), userId);
        return parseTranslationCheckResponse(text, aiResponse, request.getViPrompt(), targetWord, isUsed);
    }

    @SuppressWarnings("unchecked")
    private TranslationCheckResponse parseTranslationCheckResponse(
            String originalText,
            String aiResponse,
            String viPrompt,
            String targetWord,
            boolean isUsed) {
        try {
            String cleaned = cleanJson(aiResponse);
            Map<String, Object> parsed = objectMapper.readValue(cleaned, Map.class);

            String correctedText = (String) parsed.getOrDefault("correctedText", originalText);
            List<GrammarError> errors = parseErrorsList(parsed.get("errors"));
            List<GrammarSuggestion> suggestions = parseSuggestionsList(parsed.get("suggestions"));

            Integer score = null;
            if (parsed.get("score") instanceof Number) {
                score = ((Number) parsed.get("score")).intValue();
            }
            if (score == null) {
                score = Math.max(0, 100 - errors.size() * 15);
            }

            String feedback = (String) parsed.get("feedback");
            if (feedback == null || feedback.trim().isEmpty()) {
                if (isUsed) {
                    feedback = "Bạn đã dịch rất tốt và áp dụng chuẩn xác từ vựng mục tiêu '" + targetWord + "'.";
                } else {
                    feedback = "Bạn đã hoàn thành bản dịch, tuy nhiên chưa áp dụng từ vựng mục tiêu '" + targetWord + "'. Hãy thử kết hợp từ này nhé!";
                }
            }

            return TranslationCheckResponse.builder()
                .originalText(originalText)
                .correctedText(correctedText)
                .score(score)
                .errors(errors)
                .suggestions(suggestions)
                .targetWord(targetWord)
                .isTargetWordUsed(isUsed)
                .viPrompt(viPrompt)
                .feedback(feedback)
                .build();
        } catch (Exception e) {
            log.warn("Error parsing translation check AI response: {}, using fallback", e.getMessage());

            return TranslationCheckResponse.builder()
                .originalText(originalText)
                .correctedText(originalText)
                .score(isUsed ? 80 : 60)
                .errors(Collections.emptyList())
                .suggestions(Collections.emptyList())
                .targetWord(targetWord)
                .isTargetWordUsed(isUsed)
                .viPrompt(viPrompt)
                .feedback(isUsed ? "Bản dịch đã dùng từ mục tiêu '" + targetWord + "'." : "Chưa tìm thấy từ mục tiêu '" + targetWord + "' trong câu dịch.")
                .build();
        }
    }

}
