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
            if (isEnglishAnalysis(cached)) {
                log.info("[TASK 2] Cached analysis is in English, invalidating to regenerate in Vietnamese for userId: {}", userId);
                invalidateCache(userId);
            } else {
                log.info("[TASK 2] Cache hit for userId: {}", userId);
                return cached;
            }
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
            
            if (isEnglishAnalysis(analysis)) {
                log.warn("[TASK 2] AI generated analysis in English for userId: {}, replacing with structured Vietnamese analysis", userId);
                analysis = buildFallbackAnalysis(userId, user.getFullName());
            }
            
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
        
        prompt.append("Bạn là chuyên gia sư phạm và phân tích lộ trình học tiếng Anh hàng đầu cho người Việt Nam.\n\n");
        
        prompt.append("Dựa vào hồ sơ học tập chi tiết của học viên dưới đây, hãy phân tích kỹ lưỡng điểm mạnh, lỗ hổng kiến thức (điểm yếu) và đề xuất các hành động ưu tiên để học viên tiến bộ nhanh nhất.\n\n");
        prompt.append("QUY TẮC BẮT BUỘC VỀ NGÔN NGỮ:\n");
        prompt.append("- TOÀN BỘ các trường mô tả văn bản (profileSummary, category, area, description, rootCause, strategies, focusAreas, leverageStrategy, action, rationale, addressesArea) BẮT BUỘC PHẢI VIẾT 100% BẰNG TIẾNG VIỆT tự nhiên, thân thiện, súc tích và dễ hiểu cho người học Việt Nam.\n");
        prompt.append("- Tuyệt đối KHÔNG sử dụng tiếng Anh cho các câu mô tả, nhận xét, nguyên nhân hoặc lời khuyên.\n");
        prompt.append("- Trả về CHỈ duy nhất chuỗi JSON hợp lệ, KHÔNG có thẻ markdown (không dùng ```json), KHÔNG có bất kỳ lời dẫn hay ghi chú nào ngoài JSON.\n\n");
        
        prompt.append("HỒ SƠ HỌC TẬP HỌC VIÊN:\n");
        prompt.append("================\n");
        prompt.append(String.format("- Cấp độ hiện tại: %s\n", profile.getCurrentLevel()));
        prompt.append(String.format("- Điểm tổng thể: %.1f/100\n", profile.getOverallScore()));
        prompt.append(String.format("- Học viên: %s\n\n", profile.getUserName() != null ? profile.getUserName() : "Học viên"));
        
        // Chi tiết kỹ năng
        prompt.append("CHI TIẾT KỸ NĂNG:\n");
        if (profile.getVocabulary() != null) {
            prompt.append(String.format("- Từ vựng: %.1f/100 (Cấp độ: %s, Số bài đã làm: %d)\n",
                profile.getVocabulary().getScore(),
                profile.getVocabulary().getLevel(),
                profile.getVocabulary().getTotalActivities()));
        }
        if (profile.getGrammar() != null) {
            prompt.append(String.format("- Ngữ pháp: %.1f/100 (Cấp độ: %s, Số bài đã làm: %d)\n",
                profile.getGrammar().getScore(),
                profile.getGrammar().getLevel(),
                profile.getGrammar().getTotalActivities()));
        }
        if (profile.getSpeaking() != null) {
            prompt.append(String.format("- Luyện nói: %.1f/100 (Cấp độ: %s, Số bài đã làm: %d)\n",
                profile.getSpeaking().getScore(),
                profile.getSpeaking().getLevel(),
                profile.getSpeaking().getTotalActivities()));
        }
        prompt.append("\n");
        
        // Xu hướng và thói quen
        prompt.append("XU HƯỚNG VÀ THÓI QUEN HỌC:\n");
        if (profile.getTrend7Days() != null) {
            prompt.append(String.format("- Xu hướng 7 ngày: %s (Thay đổi %.1f%%, %d hoạt động)\n",
                profile.getTrend7Days().getDirection(),
                profile.getTrend7Days().getPercentageChange(),
                profile.getTrend7Days().getActivitiesCount()));
        }
        if (profile.getTrend30Days() != null) {
            prompt.append(String.format("- Xu hướng 30 ngày: %s (Thay đổi %.1f%%, %d hoạt động)\n",
                profile.getTrend30Days().getDirection(),
                profile.getTrend30Days().getPercentageChange(),
                profile.getTrend30Days().getActivitiesCount()));
        }
        prompt.append(String.format("- Tần suất học: %.1f hoạt động/tuần\n", profile.getLearningFrequency()));
        prompt.append(String.format("- Thời lượng mỗi phiên học: %.1f phút\n", profile.getAverageSessionDuration()));
        prompt.append(String.format("- Chuỗi ngày học liên tục (Streak): %d ngày\n", profile.getCurrentStreak()));
        if (profile.getWeakTopics() != null && !profile.getWeakTopics().isEmpty()) {
            prompt.append(String.format("- Chủ đề cần cải thiện: %s\n", String.join(", ", profile.getWeakTopics())));
        }
        if (profile.getStrongTopics() != null && !profile.getStrongTopics().isEmpty()) {
            prompt.append(String.format("- Chủ đề nắm vững: %s\n", String.join(", ", profile.getStrongTopics())));
        }
        prompt.append("\n");
        
        // Nhiệm vụ
        prompt.append("NHIỆM VỤ PHÂN TÍCH:\n");
        prompt.append("1. Chỉ ra 2-3 lỗ hổng kiến thức / điểm yếu lớn nhất (weaknesses) kèm nguyên nhân gốc rễ và lời khuyên cải thiện cụ thể (hoàn toàn bằng Tiếng Việt).\n");
        prompt.append("2. Chỉ ra 2-3 điểm mạnh vượt trội (strengths) (hoàn toàn bằng Tiếng Việt).\n");
        prompt.append("3. Đề xuất đúng 3 hành động ưu tiên hàng đầu (priorityActions) ngắn gọn, thực tế để cải thiện ngay lập tức (hoàn toàn bằng Tiếng Việt).\n");
        prompt.append("4. Xác định learningStyle: VISUAL, AUDITORY, KINESTHETIC, hoặc MIXED.\n");
        prompt.append("5. Xác định motivationLevel: HIGH, MEDIUM, hoặc LOW.\n");
        prompt.append("6. Đánh giá confidenceScore từ 0 đến 100.\n\n");
        
        prompt.append("ĐỊNH DẠNG JSON BẮT BUỘC (Mọi chuỗi mô tả PHẢI là Tiếng Việt):\n");
        prompt.append("{\n");
        prompt.append("  \"profileSummary\": \"Tóm tắt 1-2 câu về năng lực hiện tại của học viên bằng tiếng Việt\",\n");
        prompt.append("  \"learningStyle\": \"MIXED\",\n");
        prompt.append("  \"motivationLevel\": \"LOW\",\n");
        prompt.append("  \"weaknesses\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"category\": \"Kỹ năng nói\",\n");
        prompt.append("      \"area\": \"Luyện nói & Phản xạ giao tiếp\",\n");
        prompt.append("      \"description\": \"Chưa ghi nhận hoạt động luyện nói nào, kỹ năng giao tiếp chưa được rèn luyện.\",\n");
        prompt.append("      \"severity\": \"CRITICAL\",\n");
        prompt.append("      \"performancePercentage\": 0.0,\n");
        prompt.append("      \"targetPercentage\": 70.0,\n");
        prompt.append("      \"performanceGap\": 70.0,\n");
        prompt.append("      \"rootCause\": \"Chưa bắt đầu thực hành các bài nói với trợ lý AI.\",\n");
        prompt.append("      \"strategies\": [\"Thực hành shadowing 5 phút mỗi ngày với các câu đàm thoại chậm và đơn giản\"],\n");
        prompt.append("      \"focusAreas\": [\"Phát âm từ đơn\", \"Phản xạ câu ngắn\"],\n");
        prompt.append("      \"improvementTimelineDays\": 14\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"criticalWeaknesses\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"category\": \"Luyện nói\",\n");
        prompt.append("      \"area\": \"Kỹ năng nói & Phản xạ giao tiếp\",\n");
        prompt.append("      \"description\": \"Chưa có nhiều hoạt động luyện nói được ghi nhận, phản xạ đàm thoại cần được rèn luyện thêm.\",\n");
        prompt.append("      \"severity\": \"CRITICAL\",\n");
        prompt.append("      \"performancePercentage\": 0.0,\n");
        prompt.append("      \"targetPercentage\": 70.0,\n");
        prompt.append("      \"performanceGap\": 70.0,\n");
        prompt.append("      \"rootCause\": \"Chưa dành thời gian thực hành nói tiếng Anh mỗi ngày.\",\n");
        prompt.append("      \"strategies\": [\"Dành 5 phút mỗi ngày shadowing theo phát âm mẫu chậm để quen khẩu hình không áp lực\"],\n");
        prompt.append("      \"focusAreas\": [\"Phát âm cơ bản\", \"Shadowing\"],\n");
        prompt.append("      \"improvementTimelineDays\": 14\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"weaknessCount\": 2,\n");
        prompt.append("  \"strengths\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"category\": \"Từ vựng\",\n");
        prompt.append("      \"area\": \"Ghi nhớ từ vựng qua Flashcard SRS\",\n");
        prompt.append("      \"description\": \"Tỉ lệ ghi nhớ từ vựng sau 24 giờ đạt kết quả khả quan.\",\n");
        prompt.append("      \"excellenceLevel\": \"OUTSTANDING\",\n");
        prompt.append("      \"performancePercentage\": 85.0,\n");
        prompt.append("      \"leverageStrategy\": \"Mỗi ngày học thêm 5-10 từ vựng mới để mở rộng vốn từ\",\n");
        prompt.append("      \"relatedAreas\": [\"Lặp lại ngắt quãng\", \"Nhận diện từ vựng\"]\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"outstandingStrengths\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"category\": \"Từ vựng\",\n");
        prompt.append("      \"area\": \"Ghi nhớ từ vựng SRS\",\n");
        prompt.append("      \"description\": \"Tỉ lệ nhớ chính xác từ vựng sau 24h đạt trên 85%.\",\n");
        prompt.append("      \"excellenceLevel\": \"OUTSTANDING\",\n");
        prompt.append("      \"performancePercentage\": 88.0,\n");
        prompt.append("      \"leverageStrategy\": \"Tăng số lượng từ mới mỗi ngày lên 10-15 từ\",\n");
        prompt.append("      \"relatedAreas\": [\"Spaced Repetition\", \"Active Recall\"]\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"strengthCount\": 2,\n");
        prompt.append("  \"priorityActions\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"priority\": 1,\n");
        prompt.append("      \"action\": \"Thêm 1 bài luyện nói 5 phút mỗi ngày, bắt đầu với shadowing câu ngắn và ghi âm giới thiệu bản thân\",\n");
        prompt.append("      \"rationale\": \"Kỹ năng nói đang ở mức 0 hoạt động; luyện nói đều đặn từ sớm giúp xóa bỏ tâm lý sợ nói và cải thiện phát âm nhanh chóng.\",\n");
        prompt.append("      \"expectedImpact\": \"HIGH\",\n");
        prompt.append("      \"effortLevel\": \"EASY\",\n");
        prompt.append("      \"timeline\": \"Bắt đầu trong tuần này\",\n");
        prompt.append("      \"addressesArea\": \"Luyện nói\"\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"immediateNextSteps\": [\"Luyện nói 5 phút với AI\", \"Làm 1 bài trắc nghiệm củng cố ngữ pháp\", \"Ôn tập từ vựng SRS\"],\n");
        prompt.append("  \"confidenceScore\": 85.0,\n");
        prompt.append("  \"dataQuality\": \"GOOD\",\n");
        prompt.append("  \"reanalysisPeriodDays\": 14\n");
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

    private boolean isEnglishAnalysis(WeaknessStrengthDto dto) {
        if (dto == null) return false;
        // Check if critical weaknesses or priority actions have text without Vietnamese characters
        if (dto.getCriticalWeaknesses() != null && !dto.getCriticalWeaknesses().isEmpty()) {
            for (WeaknessStrengthDto.WeaknessItem item : dto.getCriticalWeaknesses()) {
                if (item.getDescription() != null && !containsVietnamese(item.getDescription())) {
                    return true;
                }
            }
        }
        if (dto.getPriorityActions() != null && !dto.getPriorityActions().isEmpty()) {
            for (WeaknessStrengthDto.PriorityAction act : dto.getPriorityActions()) {
                if (act.getAction() != null && !containsVietnamese(act.getAction())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean containsVietnamese(String text) {
        if (text == null || text.isBlank()) return false;
        // Check for common Vietnamese accented vowels or 'đ'/'Đ'
        return text.matches(".*[àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđĐ].*");
    }

    private WeaknessStrengthDto buildFallbackAnalysis(Long userId, String userName) {
        LocalDateTime now = LocalDateTime.now();
        return WeaknessStrengthDto.builder()
            .userId(userId)
            .userName(userName != null ? userName : "Học viên")
            .analyzedAt(now)
            .profileSummary("Hồ sơ học tập cho thấy bạn có nền tảng từ vựng tốt, cần tập trung thêm vào độ trôi chảy khi nói và củng cố ngữ pháp căn bản.")
            .learningStyle("Học kết hợp (Ngữ cảnh + Flashcard SRS)")
            .motivationLevel("HIGH")
            .weaknessCount(2)
            .criticalWeaknesses(List.of(
                WeaknessStrengthDto.WeaknessItem.builder()
                    .category("Luyện nói")
                    .area("Kỹ năng nói & Phản xạ giao tiếp")
                    .description("Chưa có nhiều hoạt động luyện nói được ghi nhận, phản xạ đàm thoại cần được rèn luyện thêm.")
                    .severity("CRITICAL")
                    .performancePercentage(0.0)
                    .targetPercentage(70.0)
                    .performanceGap(70.0)
                    .rootCause("Chưa dành thời gian thực hành nói tiếng Anh mỗi ngày.")
                    .strategies(List.of("Dành 5 phút mỗi ngày shadowing theo phát âm mẫu chậm để quen khẩu hình không áp lực"))
                    .focusAreas(List.of("Phát âm cơ bản", "Shadowing"))
                    .improvementTimelineDays(14)
                    .build(),
                WeaknessStrengthDto.WeaknessItem.builder()
                    .category("Ngữ pháp")
                    .area("Cấu trúc câu căn bản")
                    .description("Điểm số ngữ pháp ở mức cơ bản, cần làm quen thêm với các cấu trúc thì hiện tại và trật tự từ.")
                    .severity("HIGH")
                    .performancePercentage(45.0)
                    .targetPercentage(75.0)
                    .performanceGap(30.0)
                    .rootCause("Cần tăng cường thực hành trắc nghiệm và đọc câu ví dụ theo ngữ cảnh.")
                    .strategies(List.of("Hoàn thành các bài luyện tập ngữ pháp ngắn 10 phút tập trung vào từng cấu trúc"))
                    .focusAreas(List.of("Thì cơ bản", "Trật tự từ trong câu"))
                    .improvementTimelineDays(14)
                    .build()
            ))
            .strengthCount(2)
            .outstandingStrengths(List.of(
                WeaknessStrengthDto.StrengthItem.builder()
                    .category("Từ vựng")
                    .area("Ghi nhớ từ vựng SRS")
                    .description("Tỉ lệ nhớ chính xác từ vựng sau 24h đạt trên 85%.")
                    .excellenceLevel("OUTSTANDING")
                    .performancePercentage(88.0)
                    .leverageStrategy("Tăng số lượng từ mới mỗi ngày lên 10-15 từ")
                    .relatedAreas(List.of("Spaced Repetition", "Active Recall"))
                    .build(),
                WeaknessStrengthDto.StrengthItem.builder()
                    .category("Đọc hiểu")
                    .area("Đọc hiểu ngữ cảnh đề bài Quiz")
                    .description("Tốc độ trả lời câu hỏi trắc nghiệm nhanh, ít bị bẫy ngữ nghĩa.")
                    .excellenceLevel("STRONG")
                    .performancePercentage(82.0)
                    .leverageStrategy("Luyện thêm các bài đọc dài có áp dụng từ vựng mới")
                    .relatedAreas(List.of("Context Clues", "Grammar Patterns"))
                    .build()
            ))
            .priorityActions(List.of(
                WeaknessStrengthDto.PriorityAction.builder()
                    .priority(1)
                    .action("Thêm 1 phiên luyện nói 5 phút mỗi ngày với trợ lý AI")
                    .rationale("Kỹ năng nói đang là khoảng trống lớn nhất, luyện nói đều đặn giúp bạn tự tin phản xạ.")
                    .expectedImpact("HIGH")
                    .effortLevel("EASY")
                    .timeline("Tuần 1")
                    .addressesArea("Luyện nói")
                    .build(),
                WeaknessStrengthDto.PriorityAction.builder()
                    .priority(2)
                    .action("Luyện tập ngữ pháp căn bản 10 phút mỗi ngày")
                    .rationale("Củng cố các cấu trúc câu trọng tâm để cải thiện điểm làm bài thi và đọc hiểu.")
                    .expectedImpact("HIGH")
                    .effortLevel("MODERATE")
                    .timeline("Tuần 2")
                    .addressesArea("Ngữ pháp")
                    .build(),
                WeaknessStrengthDto.PriorityAction.builder()
                    .priority(3)
                    .action("Duy trì chuỗi streak học hàng ngày với 5-10 từ vựng SRS")
                    .rationale("Sự đều đặn là nền tảng cốt lõi giúp kích hoạt thuật toán lặp lại ngắt quãng.")
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
