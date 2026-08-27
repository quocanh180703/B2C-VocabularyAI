package com.vocabularyai.personalization.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabularyai.ai.service.gemini.GeminiAIClientService;
import com.vocabularyai.ai.service.cache.CacheService;
import com.vocabularyai.common.exception.ForbiddenException;
import com.vocabularyai.common.exception.RateLimitExceededException;
import com.vocabularyai.common.exception.ResourceNotFoundException;
import com.vocabularyai.personalization.dto.WeaknessStrengthDto;
import com.vocabularyai.personalization.dto.RecommendationDto;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of RecommendationService
 * 
 * Tạo gợi ý học tập cá nhân hóa dựa trên phân tích
 * 
 * Kiến trúc:
 * - Tái sử dụng GeminiAIClientService để gọi AI
 * - Sử dụng CacheService để cache Redis (khóa userId)
 * - Tích hợp với WeaknessStrengthService để lấy dữ liệu
 * - Theo mô hình lớp: Service → DTO → API
 * 
 * Chiến lược Caching:
 * - Khóa cache: ai:recommendations:{userId}
 * - TTL: 3 giờ (180 phút)
 * - Vô hiệu hóa khi: Tiến độ học tập thay đổi, làm mới thủ công
 * 
 * Xử lý Lỗi:
 * - RecommendationException cho lỗi phân tích
 * - RateLimitExceededException nếu vượt quota Gemini
 * - ResourceNotFoundException nếu không tìm thấy người dùng
 * - Ghi log chi tiết để debug
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationServiceImpl implements RecommendationService {
    
    private final WeaknessStrengthService weaknessStrengthService;
    private final LearningProfileService learningProfileService;
    private final GeminiAIClientService geminiAIClientService;
    private final CacheService cacheService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    
    // Cấu hình cache
    private static final String CACHE_KEY_PREFIX = "ai:recommendations:";
    private static final long CACHE_TTL_SECONDS = 3 * 60 * 60; // 3 giờ
    
    /**
     * Tạo gợi ý học tập từ phân tích
     * 
     * Quy trình:
     * 1. Xác thực người dùng tồn tại
     * 2. Xác thực quyền sở hữu (TASK 10 - Cache Authorization)
     * 3. Kiểm tra cache
     * 4. Lấy phân tích điểm yếu/điểm mạnh (TASK 2)
     * 5. Xây dựng prompt cho Gemini
     * 6. Gọi Gemini AI
     * 7. Phân tích phản hồi JSON
     * 8. Cache kết quả
     * 9. Trả về gợi ý
     */
    @Override
    public RecommendationDto generateRecommendations(Long userId) {
        log.info("[TASK 3] Bắt đầu tạo gợi ý cho userId: {}", userId);
        
        // Xác thực người dùng tồn tại
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + userId));
        
        // TASK 10: Xác thực quyền sở hữu - Verify requesting user matches requested userId
        verifyUserOwnership(userId);
        
        // Kiểm tra cache trước
        RecommendationDto cached = getCachedRecommendations(userId);
        if (cached != null) {
            log.info("[TASK 3] Cache hit cho userId: {}", userId);
            return cached;
        }
        
        try {
            // Lấy phân tích điểm yếu/điểm mạnh (TASK 2)
            log.debug("[TASK 3] Lấy phân tích điểm yếu/điểm mạnh cho userId: {}", userId);
            WeaknessStrengthDto analysis = weaknessStrengthService.analyzeWeaknessesAndStrengths(userId);
            
            // Kiểm tra rate limit trước khi gọi Gemini
            if (geminiAIClientService.isRateLimitExceeded(userId)) {
                log.warn("[TASK 3] Rate limit vượt quá cho userId: {}", userId);
                throw new RateLimitExceededException("Rate limit Gemini API vượt quá. Thử lại sau.",
                    System.currentTimeMillis() / 1000 + 3600, "HOURLY");
            }
            
            // Xây dựng prompt phân tích
            log.debug("[TASK 3] Xây dựng prompt phân tích cho userId: {}", userId);
            String recommendationPrompt = buildRecommendationPrompt(analysis);
            
            // Gọi Gemini AI
            log.debug("[TASK 3] Gọi Gemini AI để tạo gợi ý");
            String aiResponse = geminiAIClientService.generateResponse(recommendationPrompt, userId);
            
            // Phân tích phản hồi
            log.debug("[TASK 3] Phân tích phản hồi JSON AI");
            RecommendationDto recommendations = parseAIResponse(aiResponse, userId, user.getFullName());
            
            // Cache kết quả
            log.debug("[TASK 3] Cache gợi ý cho userId: {}", userId);
            cacheService.set(CACHE_KEY_PREFIX + userId, recommendations, CACHE_TTL_SECONDS);
            
            log.info("[TASK 3] Tạo gợi ý thành công cho userId: {} ({} gợi ý)", 
                userId, recommendations.getRecommendationCount());
            return recommendations;
            
        } catch (RateLimitExceededException e) {
            log.warn("[TASK 3] Lỗi rate limit: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[TASK 3] Tạo gợi ý thất bại cho userId: {} - Lỗi: {}", userId, e.getMessage(), e);
            throw new RecommendationException("Không thể tạo gợi ý: " + e.getMessage(), e);
        }
    }
    
    /**
     * Lấy gợi ý từ cache
     */
    @Override
    public RecommendationDto getCachedRecommendations(Long userId) {
        try {
            var cached = cacheService.get(CACHE_KEY_PREFIX + userId, RecommendationDto.class);
            if (cached.isPresent()) {
                log.debug("[TASK 3] Lấy gợi ý từ cache cho userId: {}", userId);
                return cached.get();
            }
            return null;
        } catch (Exception e) {
            log.debug("[TASK 3] Lỗi lấy cache cho userId: {}: {}", userId, e.getMessage());
            return null;
        }
    }
    
    /**
     * Vô hiệu hóa cache gợi ý
     */
    @Override
    public void invalidateCache(Long userId) {
        log.debug("[TASK 3] Vô hiệu hóa cache cho userId: {}", userId);
        cacheService.delete(CACHE_KEY_PREFIX + userId);
    }
    
    /**
     * Lấy gợi ý nhanh chóng
     */
    @Override
    public List<RecommendationDto.RecommendationItem> getQuickWins(Long userId) {
        RecommendationDto recommendations = generateRecommendations(userId);
        if (recommendations == null || recommendations.getQuickWins() == null) {
            return List.of();
        }
        return recommendations.getQuickWins();
    }
    
    /**
     * Lấy gợi ý dài hạn
     */
    @Override
    public List<RecommendationDto.RecommendationItem> getLongTermGoals(Long userId) {
        RecommendationDto recommendations = generateRecommendations(userId);
        if (recommendations == null || recommendations.getLongTermGoals() == null) {
            return List.of();
        }
        return recommendations.getLongTermGoals();
    }
    
    /**
     * Lấy gợi ý khẩn cấp
     */
    @Override
    public List<RecommendationDto.RecommendationItem> getCriticalRecommendations(Long userId) {
        RecommendationDto recommendations = generateRecommendations(userId);
        if (recommendations == null || recommendations.getCriticalRecommendations() == null) {
            return List.of();
        }
        return recommendations.getCriticalRecommendations();
    }
    
    /**
     * Lấy lịch trình hàng tuần
     */
    @Override
    public List<RecommendationDto.WeeklyScheduleItem> getWeeklySchedule(Long userId) {
        RecommendationDto recommendations = generateRecommendations(userId);
        if (recommendations == null || recommendations.getWeeklySchedule() == null) {
            return List.of();
        }
        return recommendations.getWeeklySchedule();
    }
    
    // ==================== Phương thức Helper Riêng ====================
    
    /**
     * Xây dựng prompt Gemini để tạo gợi ý
     * 
     * Prompt hướng dẫn Gemini:
     * - Phân tích dữ liệu phân tích
     * - Xác định 10-15 gợi ý cụ thể
     * - Xếp hạng theo ưu tiên
     * - Tạo lịch trình hàng tuần
     * - Trả về JSON có cấu trúc
     */
    private String buildRecommendationPrompt(WeaknessStrengthDto analysis) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Bạn là một chuyên gia tạo kế hoạch học tập cá nhân hóa.\n\n");
        
        prompt.append("Phân tích dữ liệu phân tích này và tạo 10-15 gợi ý học tập cụ thể.\n");
        prompt.append("Trả về CHỈ JSON hợp lệ không có định dạng markdown.\n\n");
        
        prompt.append("DỮ LIỆU PHÂN TÍCH:\n");
        prompt.append("================\n");
        prompt.append(String.format("Người dùng: %s\n", analysis.getUserName()));
        prompt.append(String.format("Mức độ: %s\n", analysis.getLearningStyle()));
        prompt.append(String.format("Động lực: %s\n", analysis.getMotivationLevel()));
        prompt.append(String.format("Độ tự tin: %.1f%%\n\n", analysis.getConfidenceScore()));
        
        // Điểm yếu
        prompt.append("ĐIỂM YẾU (Cần cải thiện):\n");
        if (analysis.getWeaknesses() != null && !analysis.getWeaknesses().isEmpty()) {
            for (WeaknessStrengthDto.WeaknessItem weakness : analysis.getWeaknesses()) {
                prompt.append(String.format("- %s (%s): %.1f%% (Khoảng cách: %.1f%%)\n",
                    weakness.getArea(), weakness.getSeverity(),
                    weakness.getPerformancePercentage(), weakness.getPerformanceGap()));
            }
        }
        prompt.append("\n");
        
        // Điểm mạnh
        prompt.append("ĐIỂM MẠNH (Có thể tận dụng):\n");
        if (analysis.getStrengths() != null && !analysis.getStrengths().isEmpty()) {
            for (WeaknessStrengthDto.StrengthItem strength : analysis.getStrengths()) {
                prompt.append(String.format("- %s (%s): %.1f%%\n",
                    strength.getArea(), strength.getExcellenceLevel(),
                    strength.getPerformancePercentage()));
            }
        }
        prompt.append("\n");
        
        // Tóm tắt
        prompt.append("TÓM TẮT:\n");
        prompt.append(String.format("- Tóm tắt: %s\n", analysis.getProfileSummary()));
        prompt.append(String.format("- Phong cách học: %s\n", analysis.getLearningStyle()));
        prompt.append(String.format("- Động lực: %s\n\n", analysis.getMotivationLevel()));
        
        // Hướng dẫn
        prompt.append("HƯỚNG DẪN TẠO GỢI Ỷ:\n");
        prompt.append("1. Tạo 10-15 gợi ý cụ thể để cải thiện lĩnh vực yếu\n");
        prompt.append("2. Ưu tiên gợi ý theo tác động và nỗ lực cần thiết\n");
        prompt.append("3. Bao gồm 3-5 'quick wins' (dễ áp dụng, tác động cao)\n");
        prompt.append("4. Bao gồm 2-3 'mục tiêu dài hạn' (cần thời gian nhưng giá trị lớn)\n");
        prompt.append("5. Tạo lịch trình hàng tuần cụ thể\n");
        prompt.append("6. Ước tính nỗ lực (phút/tuần, ngày/tuần)\n");
        prompt.append("7. Đề xuất cột mốc dự kiến\n");
        prompt.append("8. Mục tiêu 4 tuần là gì?\n\n");
        
        prompt.append("ĐỊNH DẠNG JSON YÊU CẦU (tuân thủ chặt chẽ):\n");
        prompt.append("{\n");
        prompt.append("  \"strategyOverview\": \"Tóm tắt chiến lược học tập (1-2 câu)\",\n");
        prompt.append("  \"recommendedPhase\": \"BEGINNER|INTERMEDIATE|ADVANCED\",\n");
        prompt.append("  \"primaryFocus\": \"Lĩnh vực chính cần tập trung\",\n");
        prompt.append("  \"recommendations\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"priority\": 1,\n");
        prompt.append("      \"type\": \"VOCABULARY|GRAMMAR|SPEAKING|PRACTICE|REVIEW|SUPPLEMENTARY\",\n");
        prompt.append("      \"title\": \"Tiêu đề gợi ý\",\n");
        prompt.append("      \"description\": \"Mô tả chi tiết\",\n");
        prompt.append("      \"reasoning\": \"Lý do tại sao được đề xuất\",\n");
        prompt.append("      \"targetArea\": \"Lĩnh vực mục tiêu\",\n");
        prompt.append("      \"expectedImpact\": \"HIGH|MEDIUM|LOW\",\n");
        prompt.append("      \"effortLevel\": \"EASY|MODERATE|CHALLENGING\",\n");
        prompt.append("      \"estimatedTimeMinutes\": 30,\n");
        prompt.append("      \"frequency\": \"daily|weekly|monthly\",\n");
        prompt.append("      \"resources\": [\n");
        prompt.append("        {\"title\": \"Tiêu đề\", \"type\": \"VIDEO|ARTICLE|EXERCISE|TOOL\", \"url\": \"http://...\"}\n");
        prompt.append("      ],\n");
        prompt.append("      \"successMetrics\": [\"Chỉ số 1\", \"Chỉ số 2\"],\n");
        prompt.append("      \"warnings\": []\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"quickWins\": [/* 3-5 item có impact HIGH và effort EASY */],\n");
        prompt.append("  \"longTermGoals\": [/* 2-3 item có impact HIGH và effort CHALLENGING */],\n");
        prompt.append("  \"criticalRecommendations\": [/* Gợi ý để xử lý điểm yếu CRITICAL */],\n");
        prompt.append("  \"recommendationCount\": 12,\n");
        prompt.append("  \"weeklySchedule\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"dayOfWeek\": \"MONDAY\",\n");
        prompt.append("      \"mainTask\": \"Nhiệm vụ chính\",\n");
        prompt.append("      \"recommendedTime\": \"10:00 AM\",\n");
        prompt.append("      \"duration\": 30,\n");
        prompt.append("      \"activityType\": \"VOCABULARY|GRAMMAR|SPEAKING\"\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"estimatedEffort\": {\n");
        prompt.append("    \"minutesPerWeek\": 300,\n");
        prompt.append("    \"daysPerWeek\": 5,\n");
        prompt.append("    \"distribution\": \"DISTRIBUTED\",\n");
        prompt.append("    \"difficulty\": \"MODERATE\"\n");
        prompt.append("  },\n");
        prompt.append("  \"expectedMilestones\": [\"Tuần 1: ...\", \"Tuần 2: ...\"],\n");
        prompt.append("  \"rationale\": \"Lý do tại sao những gợi ý này sẽ giúp đạt mục tiêu\",\n");
        prompt.append("  \"fourWeekGoal\": \"Mục tiêu 4 tuần là gì\",\n");
        prompt.append("  \"confidenceScore\": 85.0,\n");
        prompt.append("  \"reanalysisPeriodDays\": 30\n");
        prompt.append("}\n");
        
        return prompt.toString();
    }
    
    /**
     * Phân tích phản hồi JSON từ Gemini thành RecommendationDto
     */
    private RecommendationDto parseAIResponse(String aiResponse, Long userId, String userName) {
        try {
            log.debug("[TASK 3] Phân tích phản hồi JSON cho userId: {}", userId);
            
            // Phân tích JSON
            RecommendationDto dto = objectMapper.readValue(aiResponse, RecommendationDto.class);
            
            // Đặt userId, userName, timestamp
            dto.setUserId(userId);
            dto.setUserName(userName);
            dto.setRecommendedAt(LocalDateTime.now());
            dto.setValidUntil(LocalDateTime.now().plusHours(3)); // Hợp lệ trong 3 giờ
            
            // Đặt số đếm nếu không được cung cấp bởi AI
            if (dto.getRecommendationCount() == null && dto.getRecommendations() != null) {
                dto.setRecommendationCount(dto.getRecommendations().size());
            }
            
            log.debug("[TASK 3] Phân tích thành công: {} gợi ý, {} quick wins, {} long-term",
                dto.getRecommendationCount(),
                dto.getQuickWins() != null ? dto.getQuickWins().size() : 0,
                dto.getLongTermGoals() != null ? dto.getLongTermGoals().size() : 0);
            
            return dto;
            
        } catch (Exception e) {
            log.error("[TASK 3] Lỗi phân tích phản hồi JSON: {}", e.getMessage(), e);
            throw new RecommendationException("Lỗi phân tích phản hồi AI: " + e.getMessage(), e);
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
            throw new ForbiddenException("Authorization verification failed");
        }
    }
}
