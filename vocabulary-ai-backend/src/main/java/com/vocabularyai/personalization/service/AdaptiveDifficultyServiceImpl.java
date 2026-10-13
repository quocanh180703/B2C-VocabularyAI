package com.vocabularyai.personalization.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabularyai.ai.service.gemini.GeminiAIClientService;
import com.vocabularyai.ai.service.cache.CacheService;
import com.vocabularyai.common.exception.ForbiddenException;
import com.vocabularyai.common.exception.RateLimitExceededException;
import com.vocabularyai.common.exception.ResourceNotFoundException;
import com.vocabularyai.personalization.dto.AdaptiveDifficultyDto;
import com.vocabularyai.personalization.dto.DailyPracticeDto;
import com.vocabularyai.personalization.dto.LearningPlanDto;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of AdaptiveDifficultyService
 * 
 * Điều chỉnh độ khó tự động dựa trên hiệu suất người dùng
 * 
 * Kiến trúc:
 * - Tái sử dụng GeminiAIClientService để gọi AI
 * - Sử dụng CacheService để cache Redis (khóa userId)
 * - Tích hợp với DailyPracticeService để lấy hoạt động gần đây
 * - Theo mô hình lớp: Service → DTO → API
 * 
 * Chiến lược Caching:
 * - Khóa cache: ai:adaptive-difficulty:{userId}
 * - TTL: 30 ngày (độ khó thường ổn định lâu dài)
 * - Vô hiệu hóa khi: Điều chỉnh độ khó, hoàn thành hoạt động
 * 
 * Xử lý Lỗi:
 * - AdaptiveDifficultyException cho lỗi phân tích
 * - RateLimitExceededException nếu vượt quota Gemini
 * - ResourceNotFoundException nếu không tìm thấy người dùng
 * - Ghi log chi tiết để debug
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdaptiveDifficultyServiceImpl implements AdaptiveDifficultyService {
    
    private final LearningPlanService learningPlanService;
    private final DailyPracticeService dailyPracticeService;
    private final GeminiAIClientService geminiAIClientService;
    private final CacheService cacheService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    
    // Cấu hình cache
    private static final String CACHE_KEY_PREFIX = "ai:adaptive-difficulty:";
    private static final String DIFFICULTY_PROFILE_PREFIX = "ai:difficulty-profile:";
    private static final long CACHE_TTL_SECONDS = 30 * 24 * 60 * 60; // 30 ngày
    
    /**
     * Phân tích hiệu suất và gợi ý điều chỉnh độ khó
     * 
     * Quy trình:
     * 1. Xác thực người dùng tồn tại
     * 2. Xác thực quyền sở hữu (TASK 10 - Cache Authorization)
     * 3. Kiểm tra cache
     * 4. Lấy lịch sử hoạt động gần đây (7 ngày)
     * 5. Tính toán hiệu suất theo chủ đề
     * 6. Gọi Gemini AI để phân tích và gợi ý
     * 7. Phân tích phản hồi JSON
     * 8. Cache kết quả
     * 9. Trả về gợi ý
     */
    @Override
    public AdaptiveDifficultyDto analyzeAndRecommendDifficulty(Long userId) {
        log.info("[TASK 6] Bắt đầu phân tích độ khó tự động cho userId: {}", userId);
        
        // Xác thực người dùng tồn tại
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + userId));
        
        // TASK 10: Xác thực quyền sở hữu - Verify requesting user matches requested userId
        verifyUserOwnership(userId);
        
        // Kiểm tra cache trước
        AdaptiveDifficultyDto cached = getCachedAnalysis(userId);
        if (cached != null) {
            log.info("[TASK 6] Cache hit cho userId: {}", userId);
            return cached;
        }
        
        try {
            // Lấy kế hoạch học tập hiện tại
            log.debug("[TASK 6] Lấy kế hoạch học tập cho userId: {}", userId);
            LearningPlanDto plan = learningPlanService.createLearningPlan(userId);
            
            // Lấy hoạt động hôm nay để lấy ngữ cảnh
            DailyPracticeDto dailyPractice = dailyPracticeService.getDailyPractice(userId);
            
            // Kiểm tra rate limit trước khi gọi Gemini
            if (geminiAIClientService.isRateLimitExceeded(userId)) {
                log.warn("[TASK 6] Rate limit vượt quá cho userId: {}", userId);
                throw new RateLimitExceededException("Rate limit Gemini API vượt quá. Thử lại sau.",
                    System.currentTimeMillis() / 1000 + 3600, "HOURLY");
            }
            
            // Xây dựng prompt phân tích
            log.debug("[TASK 6] Xây dựng prompt phân tích độ khó cho userId: {}", userId);
            String analysisPrompt = buildAnalysisPrompt(plan, dailyPractice, user.getFullName());
            
            // Gọi Gemini AI
            log.debug("[TASK 6] Gọi Gemini AI để phân tích độ khó");
            String aiResponse = geminiAIClientService.generateResponse(analysisPrompt, userId);
            
            // Phân tích phản hồi
            log.debug("[TASK 6] Phân tích phản hồi JSON AI");
            AdaptiveDifficultyDto analysis = parseAIResponse(aiResponse, userId, user.getFullName());
            
            // Cache kết quả
            log.debug("[TASK 6] Cache phân tích cho userId: {}", userId);
            cacheService.set(CACHE_KEY_PREFIX + userId, analysis, CACHE_TTL_SECONDS);
            
            log.info("[TASK 6] Phân tích độ khó hoàn thành cho userId: {}", userId);
            return analysis;
            
        } catch (RateLimitExceededException e) {
            log.warn("[TASK 6] Rate limit exception: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[TASK 6] Phân tích thất bại cho userId: {} - Lỗi: {}", userId, e.getMessage(), e);
            throw new AdaptiveDifficultyException("Không thể phân tích độ khó: " + e.getMessage(), e);
        }
    }
    
    /**
     * Ghi nhận điều chỉnh độ khó
     */
    @Override
    @Transactional
    public void recordDifficultyAdjustment(Long userId, String topic, Double oldDifficulty,
                                          Double newDifficulty, String reason) {
        log.info("[TASK 6] Ghi nhận điều chỉnh độ khó cho userId: {}, topic: {} ({} → {})",
            userId, topic, oldDifficulty, newDifficulty);
        
        try {
            // Xác thực người dùng
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + userId));
            
            // Vô hiệu hóa cache
            invalidateCache(userId);
            
            // TODO: Lưu vào database (nếu cần entity DifficultyHistory)
            // difficultyHistoryRepository.save(new DifficultyHistory(...));
            
            log.info("[TASK 6] Ghi nhận thành công cho userId: {}", userId);
            
        } catch (Exception e) {
            log.error("[TASK 6] Lỗi ghi nhận điều chỉnh: {}", e.getMessage(), e);
            throw new AdaptiveDifficultyException("Lỗi ghi nhận điều chỉnh: " + e.getMessage(), e);
        }
    }
    
    /**
     * Lấy phân tích từ cache
     */
    @Override
    public AdaptiveDifficultyDto getCachedAnalysis(Long userId) {
        try {
            var cached = cacheService.get(CACHE_KEY_PREFIX + userId, AdaptiveDifficultyDto.class);
            if (cached.isPresent()) {
                log.debug("[TASK 6] Lấy phân tích từ cache cho userId: {}", userId);
                return cached.get();
            }
            return null;
        } catch (Exception e) {
            log.debug("[TASK 6] Lỗi lấy cache cho userId: {}: {}", userId, e.getMessage());
            return null;
        }
    }
    
    /**
     * Vô hiệu hóa cache
     */
    @Override
    public void invalidateCache(Long userId) {
        log.debug("[TASK 6] Vô hiệu hóa cache cho userId: {}", userId);
        cacheService.delete(CACHE_KEY_PREFIX + userId);
    }
    
    /**
     * Lấy các chủ đề nên làm dễ hơn
     */
    @Override
    public List<String> getTopicsToEase(Long userId) {
        AdaptiveDifficultyDto analysis = analyzeAndRecommendDifficulty(userId);
        if (analysis == null || analysis.getTopicsToEase() == null) {
            return List.of();
        }
        return analysis.getTopicsToEase();
    }
    
    /**
     * Lấy các chủ đề nên làm khó hơn
     */
    @Override
    public List<String> getTopicsToChallenge(Long userId) {
        AdaptiveDifficultyDto analysis = analyzeAndRecommendDifficulty(userId);
        if (analysis == null || analysis.getTopicsToChallenge() == null) {
            return List.of();
        }
        return analysis.getTopicsToChallenge();
    }
    
    /**
     * Lấy độ khó được đề xuất cho chủ đề
     */
    @Override
    public Double getRecommendedDifficultyForTopic(Long userId, String topic) {
        AdaptiveDifficultyDto analysis = analyzeAndRecommendDifficulty(userId);
        if (analysis == null || analysis.getPerformanceByTopic() == null) {
            return 5.0; // Độ khó trung bình
        }
        
        AdaptiveDifficultyDto.TopicDifficultyAnalysis topicAnalysis = 
            analysis.getPerformanceByTopic().get(topic);
        
        if (topicAnalysis == null) {
            return 5.0;
        }
        
        return topicAnalysis.getRecommendedDifficulty();
    }
    
    /**
     * Cập nhật độ khó cho chủ đề
     */
    @Override
    @Transactional
    public void updateTopicDifficulty(Long userId, String topic, Double newDifficulty) {
        log.info("[TASK 6] Cập nhật độ khó cho userId: {}, topic: {}, newDifficulty: {}",
            userId, topic, newDifficulty);
        
        // Xác thực giá trị độ khó (1-10)
        if (newDifficulty < 1.0 || newDifficulty > 10.0) {
            throw new AdaptiveDifficultyException("Độ khó phải nằm trong khoảng 1-10");
        }
        
        try {
            // Vô hiệu hóa cache để tính toán lại
            invalidateCache(userId);
            
            // TODO: Lưu vào database (nếu cần entity TopicDifficulty)
            // topicDifficultyRepository.save(new TopicDifficulty(...));
            
            log.info("[TASK 6] Cập nhật thành công cho userId: {}", userId);
            
        } catch (Exception e) {
            log.error("[TASK 6] Lỗi cập nhật độ khó: {}", e.getMessage(), e);
            throw new AdaptiveDifficultyException("Lỗi cập nhật độ khó: " + e.getMessage(), e);
        }
    }
    
    // ==================== Phương Thức Helper Riêng ====================
    
    /**
     * Xây dựng prompt Gemini để phân tích độ khó
     * 
     * Prompt hướng dẫn Gemini:
     * - Phân tích hiệu suất theo từng chủ đề
     * - Đề xuất điều chỉnh độ khó
     * - Tính toán yếu tố điều chỉnh
     * - Trả về JSON có cấu trúc
     */
    private String buildAnalysisPrompt(LearningPlanDto plan, DailyPracticeDto dailyPractice, String userName) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Bạn là một chuyên gia điều chỉnh độ khó học tập.\n\n");
        
        prompt.append("Phân tích kế hoạch học tập và hoạt động hôm nay, sau đó đề xuất điều chỉnh độ khó.\n");
        prompt.append("Trả về CHỈ JSON hợp lệ không có định dạng markdown.\n\n");
        
        prompt.append("KẾ HOẠCH HỌC TẬP:\n");
        prompt.append("================\n");
        if (plan != null) {
            prompt.append(String.format("Tiêu đề: %s\n", plan.getPlanTitle()));
            prompt.append(String.format("Mô tả: %s\n", plan.getPlanDescription()));
            prompt.append(String.format("Khó độ: %s\n", plan.getDifficultyLevel()));
            prompt.append(String.format("Số tuần: %d\n", plan.getWeeks() != null ? plan.getWeeks().size() : 0));
            
            if (plan.getWeeks() != null && !plan.getWeeks().isEmpty()) {
                prompt.append(String.format("Tuần hiện tại: 1\n"));
                LearningPlanDto.WeekPlanDto week1 = plan.getWeeks().get(0);
                if (week1 != null && week1.getDays() != null) {
                    prompt.append(String.format("Số ngày đã lập kế hoạch: %d\n", week1.getDays().size()));
                }
            }
        }
        prompt.append("\n");
        
        prompt.append("HOẠT ĐỘNG HÔM NAY:\n");
        prompt.append("==================\n");
        if (dailyPractice != null) {
            prompt.append(String.format("Mục tiêu: %s\n", dailyPractice.getDailyGoal()));
            prompt.append(String.format("Độ khó dự kiến: %s\n", dailyPractice.getExpectedDifficulty()));
            prompt.append(String.format("Tổng thời gian: %d phút\n", dailyPractice.getTotalDurationMinutes()));
            
            if (dailyPractice.getActivities() != null) {
                prompt.append(String.format("Số hoạt động: %d\n\n", dailyPractice.getActivities().size()));
                
                Map<String, Integer> activityTypes = new HashMap<>();
                for (DailyPracticeDto.ActivityDto activity : dailyPractice.getActivities()) {
                    String type = activity.getType();
                    activityTypes.put(type, activityTypes.getOrDefault(type, 0) + 1);
                }
                
                for (Map.Entry<String, Integer> entry : activityTypes.entrySet()) {
                    prompt.append(String.format("- %s: %d hoạt động\n", entry.getKey(), entry.getValue()));
                }
            }
        }
        prompt.append("\n");
        
        // Hướng dẫn
        prompt.append("HƯỚNG DẪN PHÂN TÍCH:\n");
        prompt.append("1. Phân tích hiệu suất dự kiến cho từng loại hoạt động\n");
        prompt.append("2. Đánh giá khả năng hoàn thành dựa trên độ khó hiện tại\n");
        prompt.append("3. Gợi ý điều chỉnh độ khó (1-10 scale)\n");
        prompt.append("4. Tính toán yếu tố điều chỉnh (0.8 = dễ hơn, 1.2 = khó hơn)\n");
        prompt.append("5. Phân loại chủ đề thành: ease, challenge, maintain\n");
        prompt.append("6. Ước tính ngày tái đánh giá\n");
        prompt.append("7. Cung cấp lời khuyên cụ thể\n\n");
        
        prompt.append("ĐỊNH DẠNG JSON YÊU CẦU:\n");
        prompt.append("{\n");
        prompt.append("  \"performanceSummary\": \"Tóm tắt hiệu suất chung\",\n");
        prompt.append("  \"averagePerformance\": 75.0,\n");
        prompt.append("  \"performanceTrend\": \"IMPROVING|STABLE|DECLINING\",\n");
        prompt.append("  \"currentAverageDifficulty\": 5.0,\n");
        prompt.append("  \"recommendedAverageDifficulty\": 6.0,\n");
        prompt.append("  \"adjustmentFactor\": 1.2,\n");
        prompt.append("  \"performanceByTopic\": {\n");
        prompt.append("    \"VOCABULARY\": {\n");
        prompt.append("      \"topic\": \"VOCABULARY\",\n");
        prompt.append("      \"averagePerformance\": 80.0,\n");
        prompt.append("      \"currentDifficulty\": 5.0,\n");
        prompt.append("      \"recommendedDifficulty\": 6.5,\n");
        prompt.append("      \"difficultyChange\": 1.5,\n");
        prompt.append("      \"activitiesCompleted\": 15,\n");
        prompt.append("      \"trend\": \"IMPROVING\",\n");
        prompt.append("      \"masteryLevel\": 80.0,\n");
        prompt.append("      \"adjustmentReason\": \"Hiệu suất cao, sẵn sàng chứng cự\",\n");
        prompt.append("      \"nextReviewDate\": \"2026-09-02T00:00:00\",\n");
        prompt.append("      \"notes\": [\"Lưu ý 1\", \"Lưu ý 2\"]\n");
        prompt.append("    }\n");
        prompt.append("  },\n");
        prompt.append("  \"difficultyAdjustments\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"priority\": 1,\n");
        prompt.append("      \"topic\": \"GRAMMAR\",\n");
        prompt.append("      \"currentDifficulty\": 4.0,\n");
        prompt.append("      \"recommendedDifficulty\": 4.5,\n");
        prompt.append("      \"direction\": \"INCREASE\",\n");
        prompt.append("      \"reason\": \"Lý do điều chỉnh\",\n");
        prompt.append("      \"currentPerformance\": 85.0,\n");
        prompt.append("      \"targetPerformance\": 80.0,\n");
        prompt.append("      \"activitiesBeforeNextReview\": 5,\n");
        prompt.append("      \"advice\": \"Lời khuyên cho người dùng\",\n");
        prompt.append("      \"suggestedReviewDate\": \"2026-09-02T00:00:00\"\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"topicsToEase\": [\"GRAMMAR\"],\n");
        prompt.append("  \"topicsToChallenge\": [\"VOCABULARY\", \"SPEAKING\"],\n");
        prompt.append("  \"topicsToMaintain\": [\"PRACTICE\"],\n");
        prompt.append("  \"tips\": [\"Lưu ý 1\", \"Lưu ý 2\"],\n");
        prompt.append("  \"nextReviewDays\": 7,\n");
        prompt.append("  \"warnings\": [],\n");
        prompt.append("  \"confidenceScore\": 85.0,\n");
        prompt.append("  \"activitiesAnalyzed\": 30,\n");
        prompt.append("  \"lastAdjustmentDate\": \"2026-08-26T00:00:00\",\n");
        prompt.append("  \"adjustmentsInLastMonth\": 2\n");
        prompt.append("}\n");
        
        return prompt.toString();
    }
    
    /**
     * Phân tích phản hồi JSON từ Gemini thành AdaptiveDifficultyDto
     */
    private AdaptiveDifficultyDto parseAIResponse(String aiResponse, Long userId, String userName) {
        try {
            log.debug("[TASK 6] Phân tích phản hồi JSON cho userId: {}", userId);
            
            // Phân tích JSON
            AdaptiveDifficultyDto dto = objectMapper.readValue(aiResponse, AdaptiveDifficultyDto.class);
            
            // Đặt userId, userName, timestamp
            dto.setUserId(userId);
            dto.setUserName(userName);
            dto.setAnalyzedAt(LocalDateTime.now());
            
            log.debug("[TASK 6] Phân tích thành công: {} chủ đề phân tích",
                dto.getPerformanceByTopic() != null ? dto.getPerformanceByTopic().size() : 0);
            
            return dto;
            
        } catch (Exception e) {
            log.error("[TASK 6] Lỗi phân tích phản hồi JSON: {}", e.getMessage(), e);
            throw new AdaptiveDifficultyException("Lỗi phân tích phản hồi AI: " + e.getMessage(), e);
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
