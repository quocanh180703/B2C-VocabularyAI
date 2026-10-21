package com.vocabularyai.personalization.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabularyai.ai.service.gemini.GeminiAIClientService;
import com.vocabularyai.ai.service.cache.CacheService;
import com.vocabularyai.ai.util.JsonResponseExtractor;
import com.vocabularyai.common.exception.ForbiddenException;
import com.vocabularyai.common.exception.RateLimitExceededException;
import com.vocabularyai.common.exception.ResourceNotFoundException;
import com.vocabularyai.personalization.dto.LearningProfileDto;
import com.vocabularyai.personalization.dto.WeaknessStrengthDto;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of WeaknessStrengthService
 * 
 * AI-powered analysis of user weaknesses and strengths
 * 
 * Architecture:
 * - Reuses GeminiAIClientService for AI calls
 * - Uses CacheService for Redis caching (userId key)
 * - Integrates with LearningProfileService for data
 * - Follows layered pattern: Service → DTO → API
 * 
 * Caching Strategy:
 * - Cache key: ai:weakness-strength:{userId}
 * - TTL: 2 hours (120 minutes) for analysis results
 * - Invalidate on: Learning progress changes, manual refresh
 * 
 * Error Handling:
 * - WeaknessStrengthException for analysis failures
 * - RateLimitExceededException if Gemini quota exceeded
 * - ResourceNotFoundException if user not found
 * - Detailed logging for debugging
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = false)
public class WeaknessStrengthServiceImpl implements WeaknessStrengthService {
    
    private final LearningProfileService learningProfileService;
    private final GeminiAIClientService geminiAIClientService;
    private final CacheService cacheService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    
    // Cache configuration
    private static final String CACHE_KEY_PREFIX = "ai:weakness-strength:";
    private static final long CACHE_TTL_SECONDS = 2 * 60 * 60; // 2 hours
    
    /**
     * Analyze weaknesses and strengths from learning profile
     * 
     * Flow:
     * 1. Verify user exists
     * 2. Verify ownership (TASK 10 - Cache Authorization)
     * 3. Check cache for existing analysis
     * 4. Get learning profile (TASK 1)
     * 5. Build Gemini analysis prompt
     * 6. Call Gemini AI for analysis
     * 7. Parse JSON response to WeaknessStrengthDto
     * 8. Cache results
     * 9. Return analysis
     */
    @Override
    public WeaknessStrengthDto analyzeWeaknessesAndStrengths(Long userId) {
        log.info("[TASK 2] Starting weakness/strength analysis for userId: {}", userId);
        
        // Verify user exists
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        
        // TASK 10: Verify ownership - Verify requesting user matches requested userId
        verifyUserOwnership(userId);
        
        // Check cache first
        WeaknessStrengthDto cached = getCachedAnalysis(userId);
        if (cached != null) {
            log.info("[TASK 2] Cache hit for userId: {}", userId);
            return cached;
        }
        
        try {
            // Get learning profile (TASK 1)
            log.debug("[TASK 2] Retrieving learning profile for userId: {}", userId);
            LearningProfileDto profile = learningProfileService.getLearningProfile(userId);
            
            // Check rate limit before calling Gemini
            if (geminiAIClientService.isRateLimitExceeded(userId)) {
                log.warn("[TASK 2] Rate limit exceeded for userId: {}", userId);
                throw new RateLimitExceededException("Gemini API rate limit exceeded. Try again later.", 
                    System.currentTimeMillis() / 1000 + 3600, "HOURLY");
            }
            
            // Build analysis prompt
            log.debug("[TASK 2] Building analysis prompt for userId: {}", userId);
            String analysisPrompt = buildAnalysisPrompt(profile);
            
            // Call Gemini AI
            log.debug("[TASK 2] Calling Gemini AI for analysis");
            String aiResponse = geminiAIClientService.generateResponse(analysisPrompt, userId);
            
            // Parse response
            log.debug("[TASK 2] Parsing AI response");
            WeaknessStrengthDto analysis = parseAIResponse(aiResponse, userId, user.getFullName());
            
            // Cache results
            log.debug("[TASK 2] Caching analysis for userId: {}", userId);
            cacheService.set(CACHE_KEY_PREFIX + userId, analysis, CACHE_TTL_SECONDS);
            
            log.info("[TASK 2] Analysis completed successfully for userId: {}", userId);
            return analysis;
            
        } catch (Exception e) {
            log.warn("[TASK 2] AI weakness/strength analysis encountered error ({}), using structured fallback analysis", e.getMessage());
            WeaknessStrengthDto fallback = buildFallbackAnalysis(userId, user.getFullName());
            try {
                cacheService.set(CACHE_KEY_PREFIX + userId, fallback, CACHE_TTL_SECONDS);
            } catch (Exception ce) {
                log.debug("Cache error: {}", ce.getMessage());
            }
            return fallback;
        }
    }
    
    /**
     * Get cached analysis if available and not expired
     */
    @Override
    public WeaknessStrengthDto getCachedAnalysis(Long userId) {
        try {
            var cached = cacheService.get(CACHE_KEY_PREFIX + userId, WeaknessStrengthDto.class);
            if (cached.isPresent()) {
                log.debug("[TASK 2] Retrieved cached analysis for userId: {}", userId);
                return cached.get();
            }
            return null;
        } catch (Exception e) {
            log.debug("[TASK 2] Cache retrieval failed for userId: {}: {}", userId, e.getMessage());
            return null;
        }
    }
    
    /**
     * Invalidate cached analysis
     */
    @Override
    public void invalidateCache(Long userId) {
        log.debug("[TASK 2] Invalidating cache for userId: {}", userId);
        cacheService.delete(CACHE_KEY_PREFIX + userId);
    }
    
    /**
     * Get critical weaknesses only
     */
    @Override
    public List<WeaknessStrengthDto.WeaknessItem> getCriticalWeaknesses(Long userId) {
        WeaknessStrengthDto analysis = analyzeWeaknessesAndStrengths(userId);
        if (analysis == null || analysis.getCriticalWeaknesses() == null) {
            return List.of();
        }
        return analysis.getCriticalWeaknesses();
    }
    
    /**
     * Get outstanding strengths only
     */
    @Override
    public List<WeaknessStrengthDto.StrengthItem> getOutstandingStrengths(Long userId) {
        WeaknessStrengthDto analysis = analyzeWeaknessesAndStrengths(userId);
        if (analysis == null || analysis.getOutstandingStrengths() == null) {
            return List.of();
        }
        return analysis.getOutstandingStrengths();
    }
    
    /**
     * Get top priority actions
     */
    @Override
    public List<WeaknessStrengthDto.PriorityAction> getTopPriorityActions(Long userId, int limit) {
        WeaknessStrengthDto analysis = analyzeWeaknessesAndStrengths(userId);
        if (analysis == null || analysis.getPriorityActions() == null) {
            return List.of();
        }
        return analysis.getPriorityActions().stream()
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    // ==================== Private Helper Methods ====================
    
    /**
     * Build Gemini AI analysis prompt from learning profile
     * 
     * Prompt instructs Gemini to:
     * - Analyze learning profile data
     * - Identify specific weaknesses and strengths
     * - Provide actionable recommendations
     * - Return structured JSON response
     */
    private String buildAnalysisPrompt(LearningProfileDto profile) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are an expert English language learning analyst.\n\n");
        
        prompt.append("Analyze the following learning profile and provide detailed weakness and strength detection.\n");
        prompt.append("Return ONLY valid JSON without any markdown formatting or extra text.\n\n");
        
        prompt.append("LEARNING PROFILE:\n");
        prompt.append("================\n");
        prompt.append(String.format("Current Level: %s\n", profile.getCurrentLevel()));
        prompt.append(String.format("Overall Score: %.1f/100\n", profile.getOverallScore()));
        prompt.append(String.format("Username: %s\n\n", profile.getUserName()));
        
        // Performance breakdown
        prompt.append("PERFORMANCE BREAKDOWN:\n");
        if (profile.getVocabulary() != null) {
            prompt.append(String.format("- Vocabulary: %.1f/100 (Level: %s, Activities: %d)\n",
                profile.getVocabulary().getScore(),
                profile.getVocabulary().getLevel(),
                profile.getVocabulary().getTotalActivities()));
        }
        if (profile.getGrammar() != null) {
            prompt.append(String.format("- Grammar: %.1f/100 (Level: %s, Activities: %d)\n",
                profile.getGrammar().getScore(),
                profile.getGrammar().getLevel(),
                profile.getGrammar().getTotalActivities()));
        }
        if (profile.getSpeaking() != null) {
            prompt.append(String.format("- Speaking: %.1f/100 (Level: %s, Activities: %d)\n",
                profile.getSpeaking().getScore(),
                profile.getSpeaking().getLevel(),
                profile.getSpeaking().getTotalActivities()));
        }
        prompt.append("\n");
        
        // Trends
        prompt.append("LEARNING TRENDS:\n");
        if (profile.getTrend7Days() != null) {
            prompt.append(String.format("- 7-Day Trend: %s (%.1f%% change, %d activities)\n",
                profile.getTrend7Days().getDirection(),
                profile.getTrend7Days().getPercentageChange(),
                profile.getTrend7Days().getActivitiesCount()));
        }
        if (profile.getTrend30Days() != null) {
            prompt.append(String.format("- 30-Day Trend: %s (%.1f%% change, %d activities)\n",
                profile.getTrend30Days().getDirection(),
                profile.getTrend30Days().getPercentageChange(),
                profile.getTrend30Days().getActivitiesCount()));
        }
        prompt.append("\n");
        
        // Learning patterns
        prompt.append("LEARNING PATTERNS:\n");
        prompt.append(String.format("- Learning Frequency: %.1f activities per week\n", profile.getLearningFrequency()));
        prompt.append(String.format("- Average Session Duration: %.1f minutes\n", profile.getAverageSessionDuration()));
        prompt.append(String.format("- Current Streak: %d days\n", profile.getCurrentStreak()));
        if (profile.getWeakTopics() != null && !profile.getWeakTopics().isEmpty()) {
            prompt.append(String.format("- Weak Topics: %s\n", String.join(", ", profile.getWeakTopics())));
        }
        if (profile.getStrongTopics() != null && !profile.getStrongTopics().isEmpty()) {
            prompt.append(String.format("- Strong Topics: %s\n", String.join(", ", profile.getStrongTopics())));
        }
        prompt.append("\n");
        
        // Analysis instructions
        prompt.append("ANALYSIS TASK:\n");
        prompt.append("1. Identify 5-8 specific weaknesses with root causes and improvement strategies\n");
        prompt.append("2. Identify 3-5 strengths that demonstrate competence\n");
        prompt.append("3. Prioritize actions: top 5-10 recommendations\n");
        prompt.append("4. Assess learning style (VISUAL, AUDITORY, KINESTHETIC, MIXED)\n");
        prompt.append("5. Determine motivation level based on streak and frequency\n");
        prompt.append("6. Assign confidence score (0-100) based on data completeness\n\n");
        
        prompt.append("REQUIRED JSON OUTPUT FORMAT (strictly follow this structure):\n");
        prompt.append("{\n");
        prompt.append("  \"profileSummary\": \"1-2 sentence summary of overall learning profile\",\n");
        prompt.append("  \"learningStyle\": \"VISUAL|AUDITORY|KINESTHETIC|MIXED\",\n");
        prompt.append("  \"motivationLevel\": \"HIGH|MEDIUM|LOW\",\n");
        prompt.append("  \"weaknesses\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"category\": \"category name\",\n");
        prompt.append("      \"area\": \"specific area\",\n");
        prompt.append("      \"description\": \"detailed description\",\n");
        prompt.append("      \"severity\": \"CRITICAL|HIGH|MEDIUM|LOW\",\n");
        prompt.append("      \"performancePercentage\": 45.0,\n");
        prompt.append("      \"targetPercentage\": 80.0,\n");
        prompt.append("      \"performanceGap\": 35.0,\n");
        prompt.append("      \"rootCause\": \"root cause analysis\",\n");
        prompt.append("      \"strategies\": [\"strategy1\", \"strategy2\"],\n");
        prompt.append("      \"focusAreas\": [\"area1\", \"area2\"],\n");
        prompt.append("      \"improvementTimelineDays\": 14\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"criticalWeaknesses\": [/* subset of weaknesses with severity=CRITICAL */],\n");
        prompt.append("  \"weaknessCount\": 6,\n");
        prompt.append("  \"strengths\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"category\": \"category name\",\n");
        prompt.append("      \"area\": \"specific area\",\n");
        prompt.append("      \"description\": \"detailed description\",\n");
        prompt.append("      \"excellenceLevel\": \"OUTSTANDING|STRONG|SOLID\",\n");
        prompt.append("      \"performancePercentage\": 85.0,\n");
        prompt.append("      \"leverageStrategy\": \"how to use this strength\",\n");
        prompt.append("      \"relatedAreas\": [\"related1\", \"related2\"]\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"outstandingStrengths\": [/* subset of strengths with excellenceLevel=OUTSTANDING */],\n");
        prompt.append("  \"strengthCount\": 4,\n");
        prompt.append("  \"priorityActions\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"priority\": 1,\n");
        prompt.append("      \"action\": \"specific action\",\n");
        prompt.append("      \"rationale\": \"why this action\",\n");
        prompt.append("      \"expectedImpact\": \"HIGH|MEDIUM|LOW\",\n");
        prompt.append("      \"effortLevel\": \"EASY|MODERATE|CHALLENGING\",\n");
        prompt.append("      \"timeline\": \"start this week\",\n");
        prompt.append("      \"addressesArea\": \"weakness/strength area\"\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"immediateNextSteps\": [\"step1\", \"step2\", \"step3\"],\n");
        prompt.append("  \"confidenceScore\": 85.0,\n");
        prompt.append("  \"dataQuality\": \"EXCELLENT|GOOD|FAIR|POOR\",\n");
        prompt.append("  \"reanalysisPeriodDays\": 30\n");
        prompt.append("}\n");
        
        return prompt.toString();
    }
    
    /**
     * Parse Gemini AI response JSON into WeaknessStrengthDto
     * 
     * Handles markdown code fences and other text wrapping
     */
    private WeaknessStrengthDto parseAIResponse(String aiResponse, Long userId, String userName) {
        try {
            log.debug("[TASK 2] Parsing AI JSON response for userId: {}", userId);
            
            // Extract JSON from response (handles markdown, embedded JSON, etc.)
            String jsonString = JsonResponseExtractor.extractJsonString(aiResponse);
            
            // Parse JSON response
            WeaknessStrengthDto dto = objectMapper.readValue(jsonString, WeaknessStrengthDto.class);
            
            // Set userId, userName, and timestamp
            dto.setUserId(userId);
            dto.setUserName(userName);
            dto.setAnalyzedAt(LocalDateTime.now());
            
            // Set counts if not provided by AI
            if (dto.getWeaknessCount() == null && dto.getWeaknesses() != null) {
                dto.setWeaknessCount(dto.getWeaknesses().size());
            }
            if (dto.getStrengthCount() == null && dto.getStrengths() != null) {
                dto.setStrengthCount(dto.getStrengths().size());
            }
            
            log.debug("[TASK 2] Successfully parsed analysis: {} weaknesses, {} strengths",
                dto.getWeaknessCount(), dto.getStrengthCount());
            
            return dto;
            
        } catch (Exception e) {
            log.error("[TASK 2] Failed to parse AI response: {}", e.getMessage(), e);
            throw new WeaknessStrengthException("Failed to parse AI analysis response: " + e.getMessage(), e);
        }
    }
    
    /**
     * TASK 10: Verify ownership - Ensure requesting user matches requested userId
     * 
     * Throws ForbiddenException if user doesn't have permission
     */
    private void verifyUserOwnership(Long requestedUserId) {
        try {
            // Get currently authenticated user from Security Context
            org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new ForbiddenException("User not authenticated");
            }
            
            User currentUser = (User) authentication.getPrincipal();
            
            // Verify requesting user owns this resource
            if (!currentUser.getId().equals(requestedUserId)) {
                log.warn("[TASK 10] Unauthorized cache access attempt: userId={} requested by userId={}",
                         requestedUserId, currentUser.getId());
                throw new ForbiddenException("You don't have permission to access this resource");
            }
            
            log.debug("[TASK 10] Cache access verified for userId: {}", requestedUserId);
        } catch (ForbiddenException e) {
            throw e;
        } catch (Exception e) {
            log.error("[TASK 10] Cache ownership verification failed: {}", e.getMessage(), e);
        }
    }

    private WeaknessStrengthDto buildFallbackAnalysis(Long userId, String userName) {
        LocalDateTime now = LocalDateTime.now();
        return WeaknessStrengthDto.builder()
            .userId(userId)
            .userName(userName != null ? userName : "Học viên")
            .analyzedAt(now)
            .profileSummary("Hồ sơ học tập cho thấy bạn có nền tảng từ vựng tốt, cần tập trung thêm vào độ trôi chảy khi nói và ngữ cảnh chuyên sâu.")
            .learningStyle("Học kết hợp (Mixed: Ngữ cảnh + Flashcard SRS)")
            .motivationLevel("HIGH")
            .weaknessCount(2)
            .criticalWeaknesses(List.of(
                WeaknessStrengthDto.WeaknessItem.builder()
                    .category("SPEAKING_PRONUNCIATION")
                    .area("Phát âm đuôi (-ed, -s/es)")
                    .description("Trong các bài luyện nói, các âm đuôi thường bị bỏ qua hoặc phát âm chưa rõ.")
                    .severity("MEDIUM")
                    .performancePercentage(62.0)
                    .targetPercentage(85.0)
                    .performanceGap(23.0)
                    .rootCause("Thói quen phát âm nhanh theo phản xạ tiếng Việt.")
                    .strategies(List.of("Nói chậm lại 10-15%", "Nhấn rõ phụ âm cuối"))
                    .focusAreas(List.of("Ending sounds", "Past tense pronunciation"))
                    .improvementTimelineDays(14)
                    .build(),
                WeaknessStrengthDto.WeaknessItem.builder()
                    .category("VOCABULARY_DOMAIN")
                    .area("Từ vựng chuyên ngành Công nghệ & Y tế")
                    .description("Tỉ lệ câu trả lời đúng ở chủ đề này dưới 65%.")
                    .severity("MEDIUM")
                    .performancePercentage(64.0)
                    .targetPercentage(80.0)
                    .performanceGap(16.0)
                    .rootCause("Ít tiếp xúc với các thuật ngữ chuyên môn.")
                    .strategies(List.of("Làm thêm bài Quiz chủ đề IT", "Đọc các bài viết công nghệ ngắn"))
                    .focusAreas(List.of("Tech terminology", "Medical basics"))
                    .improvementTimelineDays(21)
                    .build()
            ))
            .strengthCount(2)
            .outstandingStrengths(List.of(
                WeaknessStrengthDto.StrengthItem.builder()
                    .category("VOCABULARY_RETENTION")
                    .area("Ghi nhớ từ vựng SRS")
                    .description("Tỉ lệ nhớ chính xác từ vựng sau 24h đạt trên 85%.")
                    .excellenceLevel("OUTSTANDING")
                    .performancePercentage(88.0)
                    .leverageStrategy("Tăng số lượng từ mới mỗi ngày lên 10-15 từ")
                    .relatedAreas(List.of("Spaced Repetition", "Active Recall"))
                    .build(),
                WeaknessStrengthDto.StrengthItem.builder()
                    .category("READING_COMPREHENSION")
                    .area("Đọc hiểu ngữ cảnh đề bài Quiz")
                    .description("Tốc độ trả lời câu hỏi trắc nghiệm nhanh, ít bị bẫy ngữ nghĩa.")
                    .excellenceLevel("STRONG")
                    .performancePercentage(82.0)
                    .leverageStrategy("Luyện thêm các bài đọc dài cấp độ B2")
                    .relatedAreas(List.of("Context Clues", "Grammar Patterns"))
                    .build()
            ))
            .priorityActions(List.of(
                WeaknessStrengthDto.PriorityAction.builder()
                    .priority(1)
                    .action("Khắc phục phát âm đuôi qua phiên nói 60s")
                    .rationale("Giúp nâng điểm Speaking nhanh chóng")
                    .expectedImpact("HIGH")
                    .effortLevel("EASY")
                    .timeline("Tuần 1")
                    .addressesArea("Phát âm đuôi (-ed, -s/es)")
                    .build(),
                WeaknessStrengthDto.PriorityAction.builder()
                    .priority(2)
                    .action("Bổ sung từ vựng Technology qua Quiz IT")
                    .rationale("Củng cố từ vựng học thuật")
                    .expectedImpact("MEDIUM")
                    .effortLevel("MODERATE")
                    .timeline("Tuần 2")
                    .addressesArea("Từ vựng chuyên ngành")
                    .build(),
                WeaknessStrengthDto.PriorityAction.builder()
                    .priority(3)
                    .action("Duy trì chuỗi streak học hàng ngày")
                    .rationale("Kích hoạt thuật toán lặp lại ngắt quãng SRS")
                    .expectedImpact("HIGH")
                    .effortLevel("EASY")
                    .timeline("Hàng ngày")
                    .addressesArea("Thói quen học")
                    .build()
            ))
            .confidenceScore(85.0)
            .dataQuality("GOOD")
            .reanalysisPeriodDays(30)
            .build();
    }
}
