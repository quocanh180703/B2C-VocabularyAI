package com.vocabularyai.personalization.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabularyai.ai.service.gemini.GeminiAIClientService;
import com.vocabularyai.ai.service.cache.CacheService;
import com.vocabularyai.common.exception.RateLimitExceededException;
import com.vocabularyai.common.exception.ResourceNotFoundException;
import com.vocabularyai.personalization.dto.LearningPlanDto;
import com.vocabularyai.personalization.dto.DailyPracticeDto;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

/**
 * Implementation of DailyPracticeService
 * 
 * Tạo gợi ý hoạt động học tập cho hôm nay
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DailyPracticeServiceImpl implements DailyPracticeService {
    
    private final LearningPlanService learningPlanService;
    private final GeminiAIClientService geminiAIClientService;
    private final CacheService cacheService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    
    private static final String CACHE_KEY_PREFIX = "ai:daily-practice:";
    private static final long CACHE_TTL_SECONDS = 24 * 60 * 60; // 1 ngày
    
    /**
     * Lấy gợi ý hoạt động cho hôm nay
     */
    @Override
    public DailyPracticeDto getDailyPractice(Long userId) {
        log.info("[TASK 5] Bắt đầu lấy hoạt động hôm nay cho userId: {}", userId);
        
        // Xác thực người dùng
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + userId));
        
        // Lấy ngày hôm nay
        LocalDateTime today = LocalDateTime.now();
        String dayOfWeek = today.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase();
        int currentWeek = 1;
        
        // Kiểm tra cache
        String cacheKey = CACHE_KEY_PREFIX + userId + ":" + today.toLocalDate();
        var cached = cacheService.get(cacheKey, DailyPracticeDto.class);
        if (cached.isPresent()) {
            log.info("[TASK 5] Cache hit cho userId: {}", userId);
            return cached.get();
        }
        
        try {
            // Lấy kế hoạch (TASK 4) - đã được cache trong L1/Redis từ Bước 3
            LearningPlanDto plan = learningPlanService.createLearningPlan(userId);
            
            // Tìm tuần hiện tại
            currentWeek = getCurrentWeek(plan, today);
            
            // Lấy chi tiết ngày trực tiếp từ đối tượng plan đã có trong bộ nhớ (không gọi lại LLM)
            LearningPlanDto.DayPlanDto dayPlan = extractDayPlan(plan, currentWeek, dayOfWeek);
            
            if (dayPlan != null && dayPlan.getTasks() != null && !dayPlan.getTasks().isEmpty()) {
                DailyPracticeDto practice = buildPracticeFromDayPlan(dayPlan, userId, user.getFullName(), today, currentWeek, dayOfWeek);
                cacheService.set(cacheKey, practice, CACHE_TTL_SECONDS);
                log.info("[TASK 5] Tạo hoạt động trực tiếp từ kế hoạch thành công cho userId: {} ({} hoạt động)",
                    userId, practice.getActivities().size());
                return practice;
            }
            
            // Nếu kế hoạch ngày chưa có chi tiết nhiệm vụ, trả về bài tập đề xuất tức thì để tránh timeout
            DailyPracticeDto fallback = buildFallbackPractice(userId, user.getFullName(), today, currentWeek, dayOfWeek);
            cacheService.set(cacheKey, fallback, CACHE_TTL_SECONDS);
            return fallback;
            
        } catch (Exception e) {
            log.warn("[TASK 5] AI daily practice encountered error ({}), using fallback practice", e.getMessage());
            DailyPracticeDto fallback = buildFallbackPractice(userId, user.getFullName(), today, currentWeek, dayOfWeek);
            try {
                cacheService.set(cacheKey, fallback, CACHE_TTL_SECONDS);
            } catch (Exception ce) {
                log.debug("Cache error: {}", ce.getMessage());
            }
            return fallback;
        }
    }
    
    /**
     * Lấy gợi ý cho ngày cụ thể
     */
    @Override
    @Transactional(readOnly = false)
    public DailyPracticeDto getDailyPracticeForDay(Long userId, int weekNumber, String dayOfWeek) {
        log.info("[TASK 5] Lấy hoạt động cho tuần {} ngày {} userId: {}", weekNumber, dayOfWeek, userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + userId));
        
        try {
            LearningPlanDto plan = learningPlanService.createLearningPlan(userId);
            LearningPlanDto.DayPlanDto dayPlan = extractDayPlan(plan, weekNumber, dayOfWeek);
            
            if (dayPlan != null && dayPlan.getTasks() != null && !dayPlan.getTasks().isEmpty()) {
                return buildPracticeFromDayPlan(dayPlan, userId, user.getFullName(), LocalDateTime.now(), weekNumber, dayOfWeek);
            }
            
            return buildFallbackPractice(userId, user.getFullName(), LocalDateTime.now(), weekNumber, dayOfWeek);
        } catch (Exception e) {
            return buildFallbackPractice(userId, user.getFullName(), LocalDateTime.now(), weekNumber, dayOfWeek);
        }
    }
    
    /**
     * Đánh dấu hoàn thành
     */
    @Override
    @Transactional
    public void markDayAsCompleted(Long userId, boolean completed) {
        log.info("[TASK 5] Đánh dấu hoàn thành cho userId: {} - {}", userId, completed);
        // Implementation sẽ lưu vào database (nếu cần)
    }
    
    /**
     * Lưu ghi chú
     */
    @Override
    @Transactional
    public void saveDailyNotes(Long userId, String notes) {
        log.info("[TASK 5] Lưu ghi chú cho userId: {}", userId);
        // Implementation sẽ lưu vào database
    }
    
    /**
     * TASK 11: Invalidate all cached daily practice data for a user
     * Deletes all daily-practice cache entries: "ai:daily-practice:{userId}:*"
     * Called when user status changes (BANNED/INACTIVE)
     */
    @Override
    public void invalidateCache(Long userId) {
        try {
            log.debug("[TASK 11] Invalidating daily practice cache for userId: {}", userId);
            String pattern = CACHE_KEY_PREFIX + userId + ":*";
            cacheService.deletePattern(pattern);
            log.debug("[TASK 11] Daily practice cache invalidated for userId: {}", userId);
        } catch (Exception e) {
            log.error("[TASK 11] Error invalidating daily practice cache for userId: {}: {}", 
                      userId, e.getMessage(), e);
            // Don't throw - cache is not security-critical, continue with operation
        }
    }
    
    // ==================== Private Helpers ====================
    
    private int getCurrentWeek(LearningPlanDto plan, LocalDateTime date) {
        if (plan.getStartDate() == null) {
            return 1;
        }
        
        long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(
            plan.getStartDate().toLocalDate(), date.toLocalDate());
        int week = (int) (daysDiff / 7) + 1;
        
        return Math.max(1, Math.min(4, week));
    }
    
    private String buildDailyPrompt(LearningPlanDto.DayPlanDto dayPlan, 
                                     LearningPlanDto plan, int weekNumber) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Bạn là một huấn luyện viên học tập cá nhân.\n\n");
        
        prompt.append("Tối ưu hóa chi tiết hoạt động cho hôm nay dựa trên kế hoạch.\n");
        prompt.append("Trả về CHỈ JSON hợp lệ.\n\n");
        
        prompt.append("KẾ HOẠCH HÔM NAY:\n");
        prompt.append("================\n");
        prompt.append(String.format("Tuần: %d\n", weekNumber));
        prompt.append(String.format("Ngày: %s\n", dayPlan.getDayOfWeek()));
        prompt.append(String.format("Mục tiêu: %s\n", dayPlan.getDailyGoal()));
        prompt.append(String.format("Tổng thời gian: %d phút\n", dayPlan.getTotalMinutes()));
        
        if (dayPlan.getTasks() != null) {
            prompt.append(String.format("Số nhiệm vụ: %d\n\n", dayPlan.getTasks().size()));
            for (LearningPlanDto.TaskDto task : dayPlan.getTasks()) {
                prompt.append(String.format("- %s (%d phút, %s)\n",
                    task.getTitle(), task.getDurationMinutes(), task.getType()));
            }
        }
        
        prompt.append("\nTRẢ VỀ JSON:\n");
        prompt.append("{\n");
        prompt.append("  \"dayTitle\": \"Tiêu đề ngày\",\n");
        prompt.append("  \"dailyGoal\": \"Mục tiêu hôm nay\",\n");
        prompt.append("  \"motivationalTip\": \"Lời khuyên động lực\",\n");
        prompt.append("  \"expectedDifficulty\": \"EASY|MODERATE|CHALLENGING\",\n");
        prompt.append("  \"activities\": [/* danh sách hoạt động */],\n");
        prompt.append("  \"totalDurationMinutes\": 45,\n");
        prompt.append("  \"priorityActivities\": [],\n");
        prompt.append("  \"optionalActivities\": [],\n");
        prompt.append("  \"tips\": [\"Lưu ý 1\"],\n");
        prompt.append("  \"expectedChallenges\": [\"Thách thức\"],\n");
        prompt.append("  \"challengeSolutions\": [\"Giải pháp\"],\n");
        prompt.append("  \"bestPracticeTime\": \"10:00 AM - 11:00 AM\",\n");
        prompt.append("  \"confidenceScore\": 85.0,\n");
        prompt.append("  \"isFeasible\": true\n");
        prompt.append("}\n");
        
        return prompt.toString();
    }
    
    private DailyPracticeDto parseResponse(String response, Long userId, String userName, 
                                           LocalDateTime date, int weekNumber, String dayOfWeek) {
        try {
            DailyPracticeDto dto = objectMapper.readValue(response, DailyPracticeDto.class);
            
            dto.setUserId(userId);
            dto.setUserName(userName);
            dto.setDate(date);
            dto.setWeekNumber(weekNumber);
            dto.setDayOfWeek(dayOfWeek);
            dto.setCreatedAt(LocalDateTime.now());
            dto.setCompleted(false);
            
            return dto;
        } catch (Exception e) {
            log.error("[TASK 5] Lỗi phân tích: {}", e.getMessage(), e);
            throw new DailyPracticeException("Lỗi phân tích phản hồi: " + e.getMessage(), e);
        }
    }

    private DailyPracticeDto buildFallbackPractice(Long userId, String userName, LocalDateTime date, int weekNumber, String dayOfWeek) {
        List<DailyPracticeDto.ActivityDto> acts = List.of(
            DailyPracticeDto.ActivityDto.builder()
                .order(1)
                .title("Ôn tập 5 từ vựng đến hạn (SRS)")
                .description("Luyện tập các từ bạn đang học để củng cố trí nhớ dài hạn.")
                .type("VOCABULARY")
                .durationMinutes(8)
                .objective("Củng cố trí nhớ dài hạn")
                .instructions("Lướt flashcard và kiểm tra phản xạ nghĩa")
                .difficulty("MODERATE")
                .mandatory(true)
                .build(),
            DailyPracticeDto.ActivityDto.builder()
                .order(2)
                .title("Thử sức với Quiz Business English B1")
                .description("Cải thiện kỹ năng chọn từ ngữ cảnh và ngữ pháp thương mại.")
                .type("QUIZ")
                .durationMinutes(10)
                .objective("Cải thiện kỹ năng chọn từ ngữ cảnh")
                .instructions("Hoàn thành trọn vẹn 1 bài trắc nghiệm")
                .difficulty("MODERATE")
                .mandatory(true)
                .build(),
            DailyPracticeDto.ActivityDto.builder()
                .order(3)
                .title("Luyện nói chủ đề \"Giới thiệu bản thân\"")
                .description("Nói 45 giây để AI phân tích độ trôi chảy và phát âm.")
                .type("SPEAKING")
                .durationMinutes(7)
                .objective("Luyện phản xạ và phát âm đuôi")
                .instructions("Nói vào micro theo chủ đề gợi ý")
                .difficulty("EASY")
                .mandatory(false)
                .build()
        );

        return DailyPracticeDto.builder()
            .userId(userId)
            .userName(userName != null ? userName : "Học viên")
            .date(date != null ? date : LocalDateTime.now())
            .dayOfWeek(dayOfWeek != null ? dayOfWeek : "TODAY")
            .weekNumber(weekNumber > 0 ? weekNumber : 1)
            .dayTitle("Bài tập hôm nay")
            .dailyGoal("Tích lũy 25 phút luyện tập đa kỹ năng")
            .motivationalTip("Mỗi ngày tích lũy 15 phút, sau 1 tháng bạn sẽ bất ngờ với vốn từ của mình!")
            .expectedDifficulty("MODERATE")
            .activities(acts)
            .totalDurationMinutes(25)
            .completed(false)
            .createdAt(LocalDateTime.now())
            .build();
    }

    private LearningPlanDto.DayPlanDto extractDayPlan(LearningPlanDto plan, int weekNumber, String dayOfWeek) {
        if (plan == null || plan.getWeeks() == null) return null;
        return plan.getWeeks().stream()
            .filter(w -> w.getWeekNumber() == weekNumber)
            .flatMap(w -> w.getDays().stream())
            .filter(d -> d.getDayOfWeek() != null && d.getDayOfWeek().equalsIgnoreCase(dayOfWeek))
            .findFirst()
            .orElse(null);
    }

    private DailyPracticeDto buildPracticeFromDayPlan(LearningPlanDto.DayPlanDto dayPlan, Long userId, String userName, LocalDateTime date, int weekNumber, String dayOfWeek) {
        List<DailyPracticeDto.ActivityDto> acts = new java.util.ArrayList<>();
        int order = 1;
        if (dayPlan.getTasks() != null && !dayPlan.getTasks().isEmpty()) {
            for (LearningPlanDto.TaskDto t : dayPlan.getTasks()) {
                acts.add(DailyPracticeDto.ActivityDto.builder()
                    .order(order++)
                    .title(t.getTitle() != null ? t.getTitle() : "Hoạt động học tập")
                    .description(t.getInstructions() != null ? t.getInstructions() : "Thực hành theo kế hoạch hàng ngày")
                    .type(t.getType() != null ? t.getType() : "VOCABULARY")
                    .durationMinutes(t.getDurationMinutes() != null ? t.getDurationMinutes() : 10)
                    .objective(dayPlan.getDailyGoal() != null ? dayPlan.getDailyGoal() : "Củng cố kiến thức")
                    .instructions(t.getInstructions() != null ? t.getInstructions() : "Thực hành hoàn thành bài tập")
                    .difficulty(dayPlan.getExpectedDifficulty() != null ? dayPlan.getExpectedDifficulty() : "MODERATE")
                    .mandatory(t.getOrder() == null || t.getOrder() <= 2)
                    .build());
            }
        }
        if (acts.isEmpty()) {
            return buildFallbackPractice(userId, userName, date, weekNumber, dayOfWeek);
        }
        int totalMins = acts.stream().mapToInt(a -> a.getDurationMinutes() != null ? a.getDurationMinutes() : 10).sum();
        return DailyPracticeDto.builder()
            .userId(userId)
            .userName(userName != null ? userName : "Học viên")
            .date(date != null ? date : LocalDateTime.now())
            .dayOfWeek(dayOfWeek != null ? dayOfWeek : "TODAY")
            .weekNumber(weekNumber > 0 ? weekNumber : 1)
            .dayTitle(dayPlan.getDayTitle() != null ? dayPlan.getDayTitle() : "Bài tập hôm nay")
            .dailyGoal(dayPlan.getDailyGoal() != null ? dayPlan.getDailyGoal() : "Tập trung hoàn thành bài tập ngày")
            .motivationalTip("Mỗi ngày tích lũy 15 phút, sau 1 tháng bạn sẽ bất ngờ với vốn từ của mình!")
            .expectedDifficulty(dayPlan.getExpectedDifficulty() != null ? dayPlan.getExpectedDifficulty() : "MODERATE")
            .activities(acts)
            .totalDurationMinutes(totalMins > 0 ? totalMins : 25)
            .completed(false)
            .createdAt(LocalDateTime.now())
            .build();
    }
}
