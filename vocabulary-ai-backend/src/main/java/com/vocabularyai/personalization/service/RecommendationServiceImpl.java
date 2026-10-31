package com.vocabularyai.personalization.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabularyai.ai.service.gemini.GeminiAIClientService;
import com.vocabularyai.ai.service.cache.CacheService;
import com.vocabularyai.ai.util.JsonResponseExtractor;
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
        long startTime = System.currentTimeMillis();
        log.info("[TASK 3] Bắt đầu tạo gợi ý cho userId: {}", userId);
        
        // Xác thực người dùng tồn tại
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + userId));
        
        // TASK 10: Xác thực quyền sở hữu - Verify requesting user matches requested userId
        verifyUserOwnership(userId);
        
        // Kiểm tra cache trước
        RecommendationDto cached = getCachedRecommendations(userId);
        if (cached != null) {
            log.info("[TASK 3] Cache hit cho userId: {} ({}ms)", userId, System.currentTimeMillis() - startTime);
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
            
            long elapsedMs = System.currentTimeMillis() - startTime;
            log.info("[TASK 3] Tạo gợi ý thành công cho userId: {} ({} gợi ý) trong {}ms", 
                userId, recommendations.getRecommendationCount(), elapsedMs);
            return recommendations;
            
        } catch (RateLimitExceededException e) {
            log.warn("[TASK 3] Lỗi rate limit: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[TASK 3] Tạo gợi ý thất bại cho userId: {} sau {}ms - Lỗi: {}", 
                userId, System.currentTimeMillis() - startTime, e.getMessage(), e);
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
    @Transactional(readOnly = false)
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
    @Transactional(readOnly = false)
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
    @Transactional(readOnly = false)
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
    @Transactional(readOnly = false)
    public List<RecommendationDto.WeeklyScheduleItem> getWeeklySchedule(Long userId) {
        RecommendationDto recommendations = generateRecommendations(userId);
        if (recommendations == null || recommendations.getWeeklySchedule() == null) {
            return List.of();
        }
        return recommendations.getWeeklySchedule();
    }
    
    // ==================== Phương thức Helper Riêng ====================
    
    /**
     * Xây dựng prompt Gemini để tạo gợi ý học tập gọn (GĐ1 tối ưu hóa thời gian sinh token).
     * 
     * Prompt hướng dẫn Gemini:
     * - Phân tích dữ liệu điểm mạnh / điểm yếu
     * - Xác định 6-8 gợi ý hành động cụ thể cho lộ trình
     * - Ưu tiên theo tác động và nỗ lực
     * - Ước tính nỗ lực học tập (phút/tuần)
     * - Trả về JSON gọn hợp lệ để cấp đầu vào cho TASK 4
     */
    private String buildRecommendationPrompt(WeaknessStrengthDto analysis) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Bạn là một chuyên gia tạo kế hoạch học tập cá nhân hóa.\n\n");
        
        prompt.append("Phân tích dữ liệu phân tích này và tạo 6-8 gợi ý học tập cụ thể.\n");
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
        prompt.append("HƯỚNG DẪN TẠO GỢI Ý:\n");
        prompt.append("1. Tạo 6-8 gợi ý cụ thể để cải thiện lĩnh vực yếu\n");
        prompt.append("2. Ưu tiên gợi ý theo tác động và nỗ lực cần thiết\n");
        prompt.append("3. Ước tính nỗ lực học tập (phút/tuần)\n\n");
        
        prompt.append("ĐỊNH DẠNG JSON YÊU CẦU (tuân thủ chặt chẽ):\n");
        prompt.append("{\n");
        prompt.append("  \"strategyOverview\": \"Tóm tắt chiến lược học tập (1-2 câu)\",\n");
        prompt.append("  \"recommendedPhase\": \"BEGINNER|INTERMEDIATE|ADVANCED\",\n");
        prompt.append("  \"primaryFocus\": \"Lĩnh vực chính cần tập trung\",\n");
        prompt.append("  \"recommendationCount\": 8,\n");
        prompt.append("  \"recommendations\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"priority\": 1,\n");
        prompt.append("      \"type\": \"VOCABULARY|GRAMMAR|SPEAKING|PRACTICE|REVIEW|SUPPLEMENTARY\",\n");
        prompt.append("      \"title\": \"Tiêu đề gợi ý\",\n");
        prompt.append("      \"targetArea\": \"Lĩnh vực mục tiêu\",\n");
        prompt.append("      \"estimatedTimeMinutes\": 30,\n");
        prompt.append("      \"frequency\": \"daily|weekly|monthly\"\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"estimatedEffort\": {\n");
        prompt.append("    \"minutesPerWeek\": 300\n");
        prompt.append("  }\n");
        prompt.append("}\n");
        
        return prompt.toString();
    }
    
    /**
     * Phân tích phản hồi JSON từ Gemini thành RecommendationDto
     * 
     * Handles markdown code fences and other text wrapping
     */
    private RecommendationDto parseAIResponse(String aiResponse, Long userId, String userName) {
        try {
            log.debug("[TASK 3] Phân tích phản hồi JSON cho userId: {}", userId);
            
            // Extract JSON from response (handles markdown, embedded JSON, etc.)
            String jsonString = JsonResponseExtractor.extractJsonString(aiResponse);
            
            // Phân tích JSON
            RecommendationDto dto = objectMapper.readValue(jsonString, RecommendationDto.class);
            
            // Convert string lists to RecommendationItem objects if needed
            dto = convertStringListsToItems(dto);
            
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
     * Convert string lists to RecommendationItem objects
     * Handles case where Gemini returns string arrays instead of object arrays
     */
    private RecommendationDto convertStringListsToItems(RecommendationDto dto) {
        if (dto == null) return dto;
        
        // Convert recommendations
        if (dto.getRecommendations() != null) {
            dto.setRecommendations(convertToRecommendationItems(dto.getRecommendations()));
        }
        
        // Convert quickWins
        if (dto.getQuickWins() != null) {
            dto.setQuickWins(convertToRecommendationItems(dto.getQuickWins()));
        }
        
        // Convert longTermGoals
        if (dto.getLongTermGoals() != null) {
            dto.setLongTermGoals(convertToRecommendationItems(dto.getLongTermGoals()));
        }
        
        // Convert criticalRecommendations
        if (dto.getCriticalRecommendations() != null) {
            dto.setCriticalRecommendations(convertToRecommendationItems(dto.getCriticalRecommendations()));
        }
        
        return dto;
    }
    
    /**
     * Convert list of items to proper RecommendationItem objects
     */
    private List<RecommendationDto.RecommendationItem> convertToRecommendationItems(List<?> items) {
        if (items == null || items.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        
        List<RecommendationDto.RecommendationItem> result = new java.util.ArrayList<>();
        int priority = 1;
        
        for (Object item : items) {
            RecommendationDto.RecommendationItem recItem;
            
            if (item instanceof String) {
                // Convert string to RecommendationItem
                String text = (String) item;
                recItem = RecommendationDto.RecommendationItem.builder()
                    .priority(priority++)
                    .title(text.length() > 100 ? text.substring(0, 100) : text)
                    .description(text)
                    .type("PRACTICE")
                    .expectedImpact("MEDIUM")
                    .effortLevel("MODERATE")
                    .estimatedTimeMinutes(30)
                    .frequency("mỗi tuần")
                    .build();
            } else if (item instanceof RecommendationDto.RecommendationItem) {
                // Already proper object
                recItem = (RecommendationDto.RecommendationItem) item;
                if (recItem.getPriority() == null) {
                    recItem.setPriority(priority++);
                }
            } else if (item instanceof java.util.Map) {
                // Convert from Map (from JSON object)
                try {
                    recItem = objectMapper.convertValue(item, RecommendationDto.RecommendationItem.class);
                    if (recItem.getPriority() == null) {
                        recItem.setPriority(priority++);
                    }
                } catch (Exception e) {
                    log.warn("Failed to convert map to RecommendationItem: {}", e.getMessage());
                    continue;
                }
            } else {
                log.warn("Unknown item type: {}", item.getClass().getSimpleName());
                continue;
            }
            
            result.add(recItem);
        }
        
        return result;
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
