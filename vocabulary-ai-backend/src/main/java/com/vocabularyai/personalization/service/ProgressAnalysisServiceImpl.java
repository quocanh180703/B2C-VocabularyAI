package com.vocabularyai.personalization.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabularyai.ai.service.gemini.GeminiAIClientService;
import com.vocabularyai.ai.service.cache.CacheService;
import com.vocabularyai.common.exception.RateLimitExceededException;
import com.vocabularyai.common.exception.ResourceNotFoundException;
import com.vocabularyai.personalization.dto.ProgressAnalysisDto;
import com.vocabularyai.personalization.dto.LearningPlanDto;
import com.vocabularyai.personalization.dto.LearningProfileDto;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of ProgressAnalysisService
 * 
 * Phân tích tiến độ hướng tới mục tiêu
 * 
 * Kiến trúc:
 * - Tái sử dụng GeminiAIClientService để gọi AI
 * - Sử dụng CacheService để cache Redis (khóa userId)
 * - Tích hợp với LearningPlanService để lấy kế hoạch
 * - Theo mô hình lớp: Service → DTO → API
 * 
 * Chiến lược Caching:
 * - Khóa cache: ai:progress-analysis:{userId}
 * - TTL: 1 ngày (tiến độ thay đổi hàng ngày)
 * - Vô hiệu hóa khi: Hoàn thành hoạt động, hoàn thành milestone
 * 
 * Xử lý Lỗi:
 * - ProgressAnalysisException cho lỗi phân tích
 * - RateLimitExceededException nếu vượt quota Gemini
 * - ResourceNotFoundException nếu không tìm thấy người dùng
 * - Ghi log chi tiết để debug
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProgressAnalysisServiceImpl implements ProgressAnalysisService {
    
    private final LearningPlanService learningPlanService;
    private final LearningProfileService learningProfileService;
    private final GeminiAIClientService geminiAIClientService;
    private final CacheService cacheService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    
    // Cấu hình cache
    private static final String CACHE_KEY_PREFIX = "ai:progress-analysis:";
    private static final long CACHE_TTL_SECONDS = 24 * 60 * 60; // 1 ngày
    
    /**
     * Phân tích tiến độ của người dùng
     * 
     * Quy trình:
     * 1. Xác thực người dùng tồn tại
     * 2. Kiểm tra cache
     * 3. Lấy lịch sử học tập gần đây
     * 4. Lấy kế hoạch hiện tại
     * 5. Tính toán tiến độ, velocity, dự đoán
     * 6. Gọi Gemini AI để phân tích và gợi ý
     * 7. Cache kết quả
     * 8. Trả về phân tích
     */
    @Override
    public ProgressAnalysisDto analyzeProgress(Long userId) {
        log.info("[TASK 7] Bắt đầu phân tích tiến độ cho userId: {}", userId);
        
        // Xác thực người dùng tồn tại
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + userId));
        
        // Kiểm tra cache trước
        ProgressAnalysisDto cached = getCachedAnalysis(userId);
        if (cached != null) {
            log.info("[TASK 7] Cache hit cho userId: {}", userId);
            return cached;
        }
        
        try {
            // Lấy kế hoạch học tập hiện tại
            log.debug("[TASK 7] Lấy kế hoạch học tập cho userId: {}", userId);
            LearningPlanDto plan = learningPlanService.createLearningPlan(userId);
            
            // Lấy profil hiện tại để lấy dữ liệu hiệu suất
            log.debug("[TASK 7] Lấy profil học tập cho userId: {}", userId);
            LearningProfileDto profile = learningProfileService.getLearningProfile(userId);
            
            // Kiểm tra rate limit trước khi gọi Gemini
            if (geminiAIClientService.isRateLimitExceeded(userId)) {
                log.warn("[TASK 7] Rate limit vượt quá cho userId: {}", userId);
                throw new RateLimitExceededException("Rate limit Gemini API vượt quá. Thử lại sau.",
                    System.currentTimeMillis() / 1000 + 3600, "HOURLY");
            }
            
            // Xây dựng prompt phân tích
            log.debug("[TASK 7] Xây dựng prompt phân tích tiến độ cho userId: {}", userId);
            String analysisPrompt = buildAnalysisPrompt(plan, profile, user.getFullName());
            
            // Gọi Gemini AI
            log.debug("[TASK 7] Gọi Gemini AI để phân tích tiến độ");
            String aiResponse = geminiAIClientService.generateResponse(analysisPrompt, userId);
            
            // Phân tích phản hồi
            log.debug("[TASK 7] Phân tích phản hồi JSON AI");
            ProgressAnalysisDto analysis = parseAIResponse(aiResponse, userId, user.getFullName(), plan, profile);
            
            // Cache kết quả
            log.debug("[TASK 7] Cache phân tích cho userId: {}", userId);
            cacheService.set(CACHE_KEY_PREFIX + userId, analysis, CACHE_TTL_SECONDS);
            
            log.info("[TASK 7] Phân tích tiến độ hoàn thành cho userId: {}", userId);
            return analysis;
            
        } catch (RateLimitExceededException e) {
            log.warn("[TASK 7] Rate limit exception: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[TASK 7] Phân tích thất bại cho userId: {} - Lỗi: {}", userId, e.getMessage(), e);
            throw new ProgressAnalysisException("Không thể phân tích tiến độ: " + e.getMessage(), e);
        }
    }
    
    /**
     * Lấy phân tích từ cache
     */
    @Override
    public ProgressAnalysisDto getCachedAnalysis(Long userId) {
        try {
            var cached = cacheService.get(CACHE_KEY_PREFIX + userId, ProgressAnalysisDto.class);
            if (cached.isPresent()) {
                log.debug("[TASK 7] Lấy phân tích từ cache cho userId: {}", userId);
                return cached.get();
            }
            return null;
        } catch (Exception e) {
            log.debug("[TASK 7] Lỗi lấy cache cho userId: {}: {}", userId, e.getMessage());
            return null;
        }
    }
    
    /**
     * Vô hiệu hóa cache
     */
    @Override
    public void invalidateCache(Long userId) {
        log.debug("[TASK 7] Vô hiệu hóa cache cho userId: {}", userId);
        cacheService.delete(CACHE_KEY_PREFIX + userId);
    }
    
    /**
     * Lấy tỉ lệ hoàn thành (%)
     */
    @Override
    public Double getCompletionPercentage(Long userId) {
        ProgressAnalysisDto analysis = analyzeProgress(userId);
        if (analysis == null) {
            return 0.0;
        }
        return analysis.getCompletionPercentage();
    }
    
    /**
     * Lấy ngày dự kiến hoàn thành
     */
    @Override
    public LocalDateTime getEstimatedCompletionDate(Long userId) {
        ProgressAnalysisDto analysis = analyzeProgress(userId);
        if (analysis == null) {
            return null;
        }
        return analysis.getEstimatedCompletionDate();
    }
    
    /**
     * Lấy danh sách milestone đã hoàn thành
     */
    @Override
    public List<ProgressAnalysisDto.MilestoneProgressItem> getCompletedMilestones(Long userId) {
        ProgressAnalysisDto analysis = analyzeProgress(userId);
        if (analysis == null || analysis.getCompletedMilestones() == null) {
            return List.of();
        }
        return analysis.getCompletedMilestones();
    }
    
    /**
     * Lấy milestone hiện tại
     */
    @Override
    public ProgressAnalysisDto.MilestoneProgressItem getCurrentMilestone(Long userId) {
        ProgressAnalysisDto analysis = analyzeProgress(userId);
        if (analysis == null) {
            return null;
        }
        return analysis.getCurrentMilestone();
    }
    
    /**
     * Lấy milestone tiếp theo
     */
    @Override
    public ProgressAnalysisDto.MilestoneProgressItem getNextMilestone(Long userId) {
        ProgressAnalysisDto analysis = analyzeProgress(userId);
        if (analysis == null) {
            return null;
        }
        return analysis.getNextMilestone();
    }
    
    /**
     * Tính toán velocity
     */
    @Override
    public ProgressAnalysisDto.VelocityAnalysis calculateVelocity(Long userId) {
        ProgressAnalysisDto analysis = analyzeProgress(userId);
        if (analysis == null) {
            return null;
        }
        
        // TODO: Tạo VelocityAnalysis từ dữ liệu analysis
        return null;
    }
    
    /**
     * Lấy số ngày sớm/muộn
     */
    @Override
    public Integer getDaysAheadOrBehind(Long userId) {
        ProgressAnalysisDto analysis = analyzeProgress(userId);
        if (analysis == null) {
            return 0;
        }
        return analysis.getDaysAheadOrBehind();
    }
    
    /**
     * Lấy xác suất hoàn thành đúng hạn
     */
    @Override
    public Double getOnTimeCompletionProbability(Long userId) {
        ProgressAnalysisDto analysis = analyzeProgress(userId);
        if (analysis == null) {
            return 0.0;
        }
        return analysis.getOnTimeCompletionProbability();
    }
    
    // ==================== Phương Thức Helper Riêng ====================
    
    /**
     * Xây dựng prompt Gemini để phân tích tiến độ
     */
    private String buildAnalysisPrompt(LearningPlanDto plan, LearningProfileDto profile, String userName) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Bạn là một chuyên gia phân tích tiến độ học tập.\n\n");
        
        prompt.append("Phân tích tiến độ kế hoạch 4 tuần và dự đoán ngày hoàn thành.\n");
        prompt.append("Trả về CHỈ JSON hợp lệ không có định dạng markdown.\n\n");
        
        prompt.append("KẾ HOẠCH HỌC TẬP:\n");
        prompt.append("================\n");
        if (plan != null) {
            prompt.append(String.format("Tiêu đề: %s\n", plan.getPlanTitle()));
            prompt.append(String.format("Mô tả: %s\n", plan.getPlanDescription()));
            prompt.append(String.format("Mục tiêu chính: %s\n", plan.getPrimaryGoal()));
            prompt.append(String.format("Ngày bắt đầu: %s\n", plan.getStartDate()));
            prompt.append(String.format("Ngày kết thúc: %s\n", plan.getEndDate()));
            prompt.append(String.format("Số tuần: %d\n", plan.getWeeks() != null ? plan.getWeeks().size() : 0));
            
            if (plan.getMilestones() != null) {
                prompt.append(String.format("Số cột mốc: %d\n", plan.getMilestones().size()));
                for (LearningPlanDto.MilestoneDto ms : plan.getMilestones()) {
                    prompt.append(String.format("  - Tuần %d: %s\n", ms.getWeek(), ms.getName()));
                }
            }
        }
        prompt.append("\n");
        
        prompt.append("HIỆU SUẤT HIỆN TẠI:\n");
        prompt.append("===================\n");
        if (profile != null) {
            prompt.append(String.format("Mức độ: %s\n", profile.getCurrentLevel()));
            prompt.append(String.format("Điểm tổng: %.1f/100\n", profile.getOverallScore()));
            prompt.append(String.format("Tần suất học: %.1f hoạt động/tuần\n", profile.getLearningFrequency()));
            prompt.append(String.format("Giai đoạn: %d ngày\n", profile.getCurrentStreak()));
        }
        prompt.append("\n");
        
        // Hướng dẫn
        prompt.append("HƯỚNG DẪN PHÂN TÍCH:\n");
        prompt.append("1. Tính toán tiến độ dự kiến so với kế hoạch\n");
        prompt.append("2. Dự đoán ngày hoàn thành dựa trên tốc độ hiện tại\n");
        prompt.append("3. Tính velocity (tốc độ cải thiện hàng tuần)\n");
        prompt.append("4. Phân loại tiến độ: ON_TRACK, AHEAD, BEHIND\n");
        prompt.append("5. Dự đoán xác suất hoàn thành đúng hạn\n");
        prompt.append("6. Phân tích các milestone\n");
        prompt.append("7. Cung cấp gợi ý để duy trì tốc độ\n\n");
        
        prompt.append("ĐỊNH DẠNG JSON YÊU CẦU:\n");
        prompt.append("{\n");
        prompt.append("  \"progressSummary\": \"Tóm tắt tiến độ chung\",\n");
        prompt.append("  \"completionPercentage\": 50.0,\n");
        prompt.append("  \"daysLearned\": 14,\n");
        prompt.append("  \"daysRemaining\": 14,\n");
        prompt.append("  \"totalPlanDays\": 28,\n");
        prompt.append("  \"progressTrend\": \"ON_TRACK|AHEAD|BEHIND\",\n");
        prompt.append("  \"weeklyImprovement\": 5.0,\n");
        prompt.append("  \"primaryGoal\": \"Mục tiêu chính\",\n");
        prompt.append("  \"primaryGoalProgress\": 50.0,\n");
        prompt.append("  \"primaryGoalReward\": \"Phần thưởng\",\n");
        prompt.append("  \"secondaryGoalsProgress\": {\"Goal1\": 45.0, \"Goal2\": 60.0},\n");
        prompt.append("  \"completedMilestones\": [],\n");
        prompt.append("  \"currentMilestone\": {\n");
        prompt.append("    \"week\": 2,\n");
        prompt.append("    \"name\": \"Milestone 2\",\n");
        prompt.append("    \"description\": \"Mô tả\",\n");
        prompt.append("    \"startDate\": \"2026-09-01T00:00:00\",\n");
        prompt.append("    \"endDate\": \"2026-09-07T00:00:00\",\n");
        prompt.append("    \"status\": \"IN_PROGRESS\",\n");
        prompt.append("    \"completionPercentage\": 50.0,\n");
        prompt.append("    \"successCriteria\": \"Tiêu chí thành công\",\n");
        prompt.append("    \"successfullyCompleted\": false,\n");
        prompt.append("    \"reward\": \"Phần thưởng\",\n");
        prompt.append("    \"actualCompletionDate\": null,\n");
        prompt.append("    \"daysEarlyOrLate\": null\n");
        prompt.append("  },\n");
        prompt.append("  \"nextMilestone\": {\n");
        prompt.append("    \"week\": 3,\n");
        prompt.append("    \"name\": \"Milestone 3\",\n");
        prompt.append("    \"description\": \"Mô tả\",\n");
        prompt.append("    \"startDate\": \"2026-09-08T00:00:00\",\n");
        prompt.append("    \"endDate\": \"2026-09-14T00:00:00\",\n");
        prompt.append("    \"status\": \"NOT_STARTED\",\n");
        prompt.append("    \"completionPercentage\": 0.0,\n");
        prompt.append("    \"successCriteria\": \"Tiêu chí thành công\",\n");
        prompt.append("    \"successfullyCompleted\": false,\n");
        prompt.append("    \"reward\": \"Phần thưởng\",\n");
        prompt.append("    \"actualCompletionDate\": null,\n");
        prompt.append("    \"daysEarlyOrLate\": null\n");
        prompt.append("  },\n");
        prompt.append("  \"estimatedMilestoneDate\": \"2026-09-07T00:00:00\",\n");
        prompt.append("  \"estimatedCompletionDate\": \"2026-09-23T00:00:00\",\n");
        prompt.append("  \"daysAheadOrBehind\": 0,\n");
        prompt.append("  \"onTimeCompletionProbability\": 85.0,\n");
        prompt.append("  \"requiredPacePercentage\": 50.0,\n");
        prompt.append("  \"currentPacePercentage\": 50.0,\n");
        prompt.append("  \"averagePerformance\": 75.0,\n");
        prompt.append("  \"peakPerformance\": 95.0,\n");
        prompt.append("  \"lowestPerformance\": 45.0,\n");
        prompt.append("  \"consistency\": 80.0,\n");
        prompt.append("  \"performanceTrend\": \"IMPROVING|STABLE|DECLINING\",\n");
        prompt.append("  \"tips\": [\"Lưu ý 1\", \"Lưu ý 2\"],\n");
        prompt.append("  \"areasForImprovement\": [\"Lĩnh vực 1\"],\n");
        prompt.append("  \"recommendedActions\": [\"Hành động 1\"],\n");
        prompt.append("  \"warnings\": [],\n");
        prompt.append("  \"potentialRewardPoints\": 100,\n");
        prompt.append("  \"recentAchievements\": [\"Thành tích 1\"],\n");
        prompt.append("  \"confidenceScore\": 85.0,\n");
        prompt.append("  \"daysAnalyzed\": 14,\n");
        prompt.append("  \"nextAnalysisDate\": \"2026-08-27T00:00:00\"\n");
        prompt.append("}\n");
        
        return prompt.toString();
    }
    
    /**
     * Phân tích phản hồi JSON từ Gemini thành ProgressAnalysisDto
     */
    private ProgressAnalysisDto parseAIResponse(String aiResponse, Long userId, String userName, 
                                                 LearningPlanDto plan, LearningProfileDto profile) {
        try {
            log.debug("[TASK 7] Phân tích phản hồi JSON cho userId: {}", userId);
            
            // Phân tích JSON
            ProgressAnalysisDto dto = objectMapper.readValue(aiResponse, ProgressAnalysisDto.class);
            
            // Đặt userId, userName, timestamp
            dto.setUserId(userId);
            dto.setUserName(userName);
            dto.setAnalyzedAt(LocalDateTime.now());
            
            // Tính toán các trường hợp NULL
            if (dto.getCompletedMilestones() == null) {
                dto.setCompletedMilestones(List.of());
            }
            if (dto.getTips() == null) {
                dto.setTips(List.of());
            }
            if (dto.getAreasForImprovement() == null) {
                dto.setAreasForImprovement(List.of());
            }
            if (dto.getRecommendedActions() == null) {
                dto.setRecommendedActions(List.of());
            }
            if (dto.getWarnings() == null) {
                dto.setWarnings(List.of());
            }
            if (dto.getRecentAchievements() == null) {
                dto.setRecentAchievements(List.of());
            }
            
            log.debug("[TASK 7] Phân tích thành công cho userId: {}, completion: {}%",
                userId, dto.getCompletionPercentage());
            
            return dto;
            
        } catch (Exception e) {
            log.error("[TASK 7] Lỗi phân tích phản hồi JSON: {}", e.getMessage(), e);
            throw new ProgressAnalysisException("Lỗi phân tích phản hồi AI: " + e.getMessage(), e);
        }
    }
}
