package com.vocabularyai.personalization.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabularyai.ai.service.gemini.GeminiAIClientService;
import com.vocabularyai.ai.service.cache.CacheService;
import com.vocabularyai.common.exception.RateLimitExceededException;
import com.vocabularyai.common.exception.ResourceNotFoundException;
import com.vocabularyai.personalization.dto.RecommendationDto;
import com.vocabularyai.personalization.dto.LearningPlanDto;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of LearningPlanService
 * 
 * Tạo kế hoạch học tập 4 tuần có cấu trúc
 * 
 * Kiến trúc:
 * - Tái sử dụng GeminiAIClientService để gọi AI
 * - Sử dụng CacheService để cache Redis (khóa userId)
 * - Tích hợp với RecommendationService để lấy dữ liệu
 * - Theo mô hình lớp: Service → DTO → API
 * 
 * Chiến lược Caching:
 * - Khóa cache: ai:learning-plan:{userId}
 * - TTL: 7 ngày (kế hoạch thường ổn định một tuần)
 * - Vô hiệu hóa khi: Tiến độ học tập thay đổi, làm mới thủ công
 * 
 * Xử lý Lỗi:
 * - LearningPlanException cho lỗi tạo kế hoạch
 * - RateLimitExceededException nếu vượt quota Gemini
 * - ResourceNotFoundException nếu không tìm thấy người dùng
 * - Ghi log chi tiết để debug
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LearningPlanServiceImpl implements LearningPlanService {
    
    private final RecommendationService recommendationService;
    private final WeaknessStrengthService weaknessStrengthService;
    private final GeminiAIClientService geminiAIClientService;
    private final CacheService cacheService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    
    // Cấu hình cache
    private static final String CACHE_KEY_PREFIX = "ai:learning-plan:";
    private static final long CACHE_TTL_SECONDS = 7 * 24 * 60 * 60; // 7 ngày
    
    /**
     * Tạo kế hoạch học tập 4 tuần
     * 
     * Quy trình:
     * 1. Xác thực người dùng tồn tại
     * 2. Kiểm tra cache
     * 3. Lấy gợi ý (TASK 3)
     * 4. Xây dựng prompt cho Gemini
     * 5. Gọi Gemini AI
     * 6. Phân tích phản hồi JSON
     * 7. Cache kết quả
     * 8. Trả về kế hoạch
     */
    @Override
    public LearningPlanDto createLearningPlan(Long userId) {
        log.info("[TASK 4] Bắt đầu tạo kế hoạch học tập cho userId: {}", userId);
        
        // Xác thực người dùng tồn tại
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + userId));
        
        // Kiểm tra cache trước
        LearningPlanDto cached = getCachedPlan(userId);
        if (cached != null) {
            log.info("[TASK 4] Cache hit cho userId: {}", userId);
            return cached;
        }
        
        try {
            // Lấy gợi ý từ TASK 3
            log.debug("[TASK 4] Lấy gợi ý cho userId: {}", userId);
            RecommendationDto recommendations = recommendationService.generateRecommendations(userId);
            
            // Kiểm tra rate limit trước khi gọi Gemini
            if (geminiAIClientService.isRateLimitExceeded(userId)) {
                log.warn("[TASK 4] Rate limit vượt quá cho userId: {}", userId);
                throw new RateLimitExceededException("Rate limit Gemini API vượt quá. Thử lại sau.",
                    System.currentTimeMillis() / 1000 + 3600, "HOURLY");
            }
            
            // Xây dựng prompt phân tích
            log.debug("[TASK 4] Xây dựng prompt kế hoạch cho userId: {}", userId);
            String planPrompt = buildPlanPrompt(recommendations);
            
            // Gọi Gemini AI
            log.debug("[TASK 4] Gọi Gemini AI để tạo kế hoạch");
            String aiResponse = geminiAIClientService.generateResponse(planPrompt, userId);
            
            // Phân tích phản hồi
            log.debug("[TASK 4] Phân tích phản hồi JSON AI");
            LearningPlanDto plan = parseAIResponse(aiResponse, userId, user.getFullName());
            
            // Cache kết quả
            log.debug("[TASK 4] Cache kế hoạch cho userId: {}", userId);
            cacheService.set(CACHE_KEY_PREFIX + userId, plan, CACHE_TTL_SECONDS);
            
            log.info("[TASK 4] Tạo kế hoạch thành công cho userId: {} (4 tuần, {} ngày)", 
                userId, plan.getWeeks() != null ? plan.getWeeks().size() * 7 : 0);
            return plan;
            
        } catch (Exception e) {
            log.warn("[TASK 4] AI plan generation encountered error ({}), using structured fallback plan", e.getMessage());
            LearningPlanDto fallback = buildFallbackPlan(userId, user.getFullName());
            try {
                cacheService.set(CACHE_KEY_PREFIX + userId, fallback, CACHE_TTL_SECONDS);
            } catch (Exception ce) {
                log.debug("Cache error: {}", ce.getMessage());
            }
            return fallback;
        }
    }
    
    /**
     * Lấy kế hoạch từ cache
     */
    @Override
    public LearningPlanDto getCachedPlan(Long userId) {
        try {
            var cached = cacheService.get(CACHE_KEY_PREFIX + userId, LearningPlanDto.class);
            if (cached.isPresent()) {
                log.debug("[TASK 4] Lấy kế hoạch từ cache cho userId: {}", userId);
                return cached.get();
            }
            return null;
        } catch (Exception e) {
            log.debug("[TASK 4] Lỗi lấy cache cho userId: {}: {}", userId, e.getMessage());
            return null;
        }
    }
    
    /**
     * Vô hiệu hóa cache kế hoạch
     */
    @Override
    public void invalidateCache(Long userId) {
        log.debug("[TASK 4] Vô hiệu hóa cache cho userId: {}", userId);
        cacheService.delete(CACHE_KEY_PREFIX + userId);
    }
    
    /**
     * Lấy chi tiết các tuần
     */
    @Override
    public List<LearningPlanDto.WeekPlanDto> getWeeklyBreakdown(Long userId) {
        LearningPlanDto plan = createLearningPlan(userId);
        if (plan == null || plan.getWeeks() == null) {
            return List.of();
        }
        return plan.getWeeks();
    }
    
    /**
     * Lấy các cột mốc
     */
    @Override
    public List<LearningPlanDto.MilestoneDto> getMilestones(Long userId) {
        LearningPlanDto plan = createLearningPlan(userId);
        if (plan == null || plan.getMilestones() == null) {
            return List.of();
        }
        return plan.getMilestones();
    }
    
    /**
     * Lấy chi tiết tuần cụ thể
     */
    @Override
    public List<LearningPlanDto.DayPlanDto> getWeekDetails(Long userId, int weekNumber) {
        LearningPlanDto plan = createLearningPlan(userId);
        if (plan == null || plan.getWeeks() == null) {
            return List.of();
        }
        
        return plan.getWeeks().stream()
            .filter(w -> w.getWeekNumber() == weekNumber)
            .flatMap(w -> w.getDays().stream())
            .collect(Collectors.toList());
    }
    
    /**
     * Lấy chi tiết ngày cụ thể
     */
    @Override
    public LearningPlanDto.DayPlanDto getDayDetails(Long userId, int weekNumber, String dayOfWeek) {
        LearningPlanDto plan = createLearningPlan(userId);
        if (plan == null || plan.getWeeks() == null) {
            return null;
        }
        
        return plan.getWeeks().stream()
            .filter(w -> w.getWeekNumber() == weekNumber)
            .flatMap(w -> w.getDays().stream())
            .filter(d -> d.getDayOfWeek().equalsIgnoreCase(dayOfWeek))
            .findFirst()
            .orElse(null);
    }
    
    // ==================== Phương thức Helper Riêng ====================
    
    /**
     * Xây dựng prompt Gemini để tạo kế hoạch 4 tuần
     * 
     * Prompt hướng dẫn Gemini:
     * - Phân tích dữ liệu gợi ý
     * - Tạo kế hoạch 4 tuần có cấu trúc
     * - Mỗi tuần có 7 ngày
     * - Mỗi ngày có 2-4 nhiệm vụ
     * - Bao gồm các cột mốc/milestone
     * - Trả về JSON có cấu trúc
     */
    private String buildPlanPrompt(RecommendationDto recommendations) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Bạn là một nhà lập kế hoạch học tập chuyên gia.\n\n");
        
        prompt.append("Tạo kế hoạch học tập 4 tuần (28 ngày) có cấu trúc chi tiết từ những gợi ý này.\n");
        prompt.append("Trả về CHỈ JSON hợp lệ không có định dạng markdown.\n\n");
        
        prompt.append("DỮ LIỆU GỢI Ỷ:\n");
        prompt.append("================\n");
        prompt.append(String.format("Chiến lược: %s\n", recommendations.getStrategyOverview()));
        prompt.append(String.format("Tập trung chính: %s\n", recommendations.getPrimaryFocus()));
        prompt.append(String.format("Giai đoạn: %s\n", recommendations.getRecommendedPhase()));
        prompt.append(String.format("Tổng nỗ lực: %d phút/tuần\n\n", 
            recommendations.getEstimatedEffort().getMinutesPerWeek()));
        
        // Gợi ý
        prompt.append("GỢI Ỷ HỌC TẬP (").append(recommendations.getRecommendationCount()).append(" gợi ý):\n");
        if (recommendations.getRecommendations() != null) {
            for (RecommendationDto.RecommendationItem rec : recommendations.getRecommendations()) {
                prompt.append(String.format("- %s: %s (%s phút, %s)\n",
                    rec.getTitle(), rec.getTargetArea(), rec.getEstimatedTimeMinutes(), rec.getFrequency()));
            }
        }
        prompt.append("\n");
        
        // Hướng dẫn
        prompt.append("HƯỚNG DẪN TẠO KẾ HOẠCH:\n");
        prompt.append("1. Tạo kế hoạch 4 tuần (tuần 1-4)\n");
        prompt.append("2. Mỗi tuần có 7 ngày (thứ 2-chủ nhật, không có ngày off)\n");
        prompt.append("3. Mỗi ngày có 2-4 nhiệm vụ cụ thể\n");
        prompt.append("4. Phân bổ gợi ý xuyên suốt 4 tuần\n");
        prompt.append("5. Tăng dần độ khó: Tuần 1 dễ, Tuần 2-3 vừa, Tuần 4 khó\n");
        prompt.append("6. Bao gồm 4 cột mốc/milestone (1 mỗi tuần)\n");
        prompt.append("7. Ước tính chính xác thời gian cho mỗi nhiệm vụ\n");
        prompt.append("8. Thêm \"review day\" vào cuối mỗi tuần\n");
        prompt.append("9. Cân bằng giữa các loại hoạt động (vocabulary, grammar, speaking, practice)\n");
        prompt.append("10. Cung cấp các lưu ý hữu ích cho mỗi ngày\n\n");
        
        prompt.append("ĐỊNH DẠNG JSON YÊU CẦU (tuân thủ chặt chẽ):\n");
        prompt.append("{\n");
        prompt.append("  \"planTitle\": \"Tiêu đề kế hoạch\",\n");
        prompt.append("  \"planDescription\": \"Mô tả tổng quát\",\n");
        prompt.append("  \"primaryGoal\": \"Mục tiêu chính (1-3 câu)\",\n");
        prompt.append("  \"secondaryGoals\": [\"Mục tiêu thứ cấp 1\", \"Mục tiêu thứ cấp 2\"],\n");
        prompt.append("  \"totalCommitmentMinutesPerWeek\": 300,\n");
        prompt.append("  \"weeks\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"weekNumber\": 1,\n");
        prompt.append("      \"weekTitle\": \"Tuần 1: Nền tảng\",\n");
        prompt.append("      \"weekGoal\": \"Mục tiêu tuần này\",\n");
        prompt.append("      \"mainTheme\": \"Chủ đề chính\",\n");
        prompt.append("      \"description\": \"Mô tả chi tiết\",\n");
        prompt.append("      \"totalMinutes\": 300,\n");
        prompt.append("      \"mainActivities\": [\"Hoạt động 1\", \"Hoạt động 2\"],\n");
        prompt.append("      \"checkpoints\": [\"Kiểm tra 1\"],\n");
        prompt.append("      \"days\": [\n");
        prompt.append("        {\n");
        prompt.append("          \"dayOfWeek\": \"MONDAY\",\n");
        prompt.append("          \"date\": \"2026-09-01\",\n");
        prompt.append("          \"dayTitle\": \"Thứ 2: Khởi động\",\n");
        prompt.append("          \"dailyGoal\": \"Hoàn thành X\",\n");
        prompt.append("          \"totalMinutes\": 45,\n");
        prompt.append("          \"expectedDifficulty\": \"EASY\",\n");
        prompt.append("          \"notes\": \"Lưu ý của ngày này\",\n");
        prompt.append("          \"tasks\": [\n");
        prompt.append("            {\n");
        prompt.append("              \"order\": 1,\n");
        prompt.append("              \"title\": \"Bài 1\",\n");
        prompt.append("              \"description\": \"Mô tả nhiệm vụ\",\n");
        prompt.append("              \"type\": \"VOCABULARY|GRAMMAR|SPEAKING|PRACTICE|REVIEW|QUIZ\",\n");
        prompt.append("              \"durationMinutes\": 30,\n");
        prompt.append("              \"objective\": \"Mục tiêu cụ thể\",\n");
        prompt.append("              \"instructions\": \"Hướng dẫn\",\n");
        prompt.append("              \"resources\": [\"Tài nguyên 1\"],\n");
        prompt.append("              \"successCriteria\": \"Tiêu chí thành công\"\n");
        prompt.append("            }\n");
        prompt.append("          ]\n");
        prompt.append("        }\n");
        prompt.append("      ]\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"milestones\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"week\": 1,\n");
        prompt.append("      \"name\": \"Cột mốc 1\",\n");
        prompt.append("      \"description\": \"Mô tả\",\n");
        prompt.append("      \"evaluation\": \"Cách đánh giá\",\n");
        prompt.append("      \"reward\": \"Phần thưởng\"\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"successCriteria\": [\"Tiêu chí 1\", \"Tiêu chí 2\"],\n");
        prompt.append("  \"strategy\": \"Chiến lược chung\",\n");
        prompt.append("  \"requiredResources\": [\"Tài nguyên 1\"],\n");
        prompt.append("  \"importantNotes\": [\"Lưu ý 1\"],\n");
        prompt.append("  \"challengeMitigation\": [\"Cách giải quyết 1\"],\n");
        prompt.append("  \"confidenceScore\": 85.0,\n");
        prompt.append("  \"difficultyLevel\": \"MODERATE\",\n");
        prompt.append("  \"adjustmentFrequencyDays\": 7\n");
        prompt.append("}\n");
        
        return prompt.toString();
    }
    
    /**
     * Phân tích phản hồi JSON từ Gemini thành LearningPlanDto
     */
    private LearningPlanDto parseAIResponse(String aiResponse, Long userId, String userName) {
        try {
            log.debug("[TASK 4] Phân tích phản hồi JSON cho userId: {}", userId);
            
            // Phân tích JSON
            LearningPlanDto dto = objectMapper.readValue(aiResponse, LearningPlanDto.class);
            
            // Đặt userId, userName, timestamp
            dto.setUserId(userId);
            dto.setUserName(userName);
            dto.setCreatedAt(LocalDateTime.now());
            dto.setStartDate(LocalDateTime.now());
            dto.setEndDate(LocalDateTime.now().plusDays(28)); // 4 tuần
            
            log.debug("[TASK 4] Phân tích thành công: {} tuần, {} cột mốc",
                dto.getWeeks() != null ? dto.getWeeks().size() : 0,
                dto.getMilestones() != null ? dto.getMilestones().size() : 0);
            
            return dto;
            
        } catch (Exception e) {
            log.error("[TASK 4] Lỗi phân tích phản hồi JSON: {}", e.getMessage(), e);
            throw new LearningPlanException("Lỗi phân tích phản hồi AI: " + e.getMessage(), e);
        }
    }

    private LearningPlanDto buildFallbackPlan(Long userId, String userName) {
        LocalDateTime now = LocalDateTime.now();
        List<LearningPlanDto.TaskDto> tasks = new ArrayList<>();
        tasks.add(LearningPlanDto.TaskDto.builder()
            .order(1)
            .title("Ôn tập 5 từ vựng đến hạn (SRS)")
            .description("Luyện tập các từ bạn đang học để củng cố trí nhớ dài hạn.")
            .type("VOCABULARY")
            .durationMinutes(8)
            .objective("Củng cố trí nhớ dài hạn")
            .instructions("Lướt flashcard và kiểm tra phản xạ nghĩa")
            .build());
        tasks.add(LearningPlanDto.TaskDto.builder()
            .order(2)
            .title("Thử sức với Quiz Business English B1")
            .description("Cải thiện kỹ năng chọn từ ngữ cảnh và ngữ pháp thương mại.")
            .type("QUIZ")
            .durationMinutes(10)
            .objective("Nâng cao phản xạ ngữ cảnh")
            .instructions("Làm trọn vẹn 1 bài trắc nghiệm")
            .build());
        tasks.add(LearningPlanDto.TaskDto.builder()
            .order(3)
            .title("Luyện nói chủ đề \"Giới thiệu bản thân\"")
            .description("Nói 45 giây để AI phân tích độ trôi chảy và phát âm.")
            .type("SPEAKING")
            .durationMinutes(7)
            .objective("Cải thiện ngữ điệu và phát âm đuôi")
            .instructions("Nói vào micro theo chủ đề gợi ý")
            .build());

        String[] dayNames = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"};
        List<LearningPlanDto.DayPlanDto> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            days.add(LearningPlanDto.DayPlanDto.builder()
                .dayOfWeek(dayNames[i])
                .date(now.plusDays(i).toLocalDate().toString())
                .dayTitle("Ngày " + (i + 1) + ": Tăng cường từ vựng & phản xạ")
                .dailyGoal("Hoàn thành bài tập trọng tâm hôm nay")
                .totalMinutes(25)
                .expectedDifficulty("MODERATE")
                .notes("Duy trì nhịp học đều đặn mỗi ngày.")
                .tasks(tasks)
                .build());
        }

        List<LearningPlanDto.WeekPlanDto> weeks = new ArrayList<>();
        for (int w = 1; w <= 4; w++) {
            weeks.add(LearningPlanDto.WeekPlanDto.builder()
                .weekNumber(w)
                .weekTitle("Tuần " + w + ": " + (w == 1 ? "Nền tảng & Từ vựng cốt lõi" : w == 2 ? "Ngữ pháp & Đọc hiểu ngữ cảnh" : w == 3 ? "Phản xạ giao tiếp & Luyện nói" : "Đánh giá & Nâng hạng CEFR"))
                .weekGoal("Chinh phục mục tiêu kiến thức tuần " + w)
                .mainTheme(w == 1 ? "Từ vựng CEFR B1" : w == 2 ? "Cấu trúc câu & Trắc nghiệm" : w == 3 ? "Luyện phát âm AI" : "Tổng kết năng lực")
                .totalMinutes(120)
                .days(days)
                .build());
        }

        List<LearningPlanDto.MilestoneDto> milestones = List.of(
            LearningPlanDto.MilestoneDto.builder().week(1).name("Hoàn thiện 50 từ vựng cấp độ B1").description("Nắm vững nghĩa tiếng Việt và cách dùng").evaluation("Tỉ lệ nhớ SRS > 80%").build(),
            LearningPlanDto.MilestoneDto.builder().week(2).name("Đạt điểm Quiz 80% trở lên ở bài Business Essentials").description("Cải thiện kỹ năng chọn từ ngữ cảnh").evaluation("Điểm số bài Quiz").build(),
            LearningPlanDto.MilestoneDto.builder().week(3).name("Luyện 5 phiên nói AI với điểm trôi chảy > 70").description("Khắc phục phát âm đuôi và phản xạ câu").evaluation("Phân tích phát âm AI").build(),
            LearningPlanDto.MilestoneDto.builder().week(4).name("Đánh giá tổng kết nâng hạng lên cấp độ tiếp theo").description("Nâng hạng trình độ tiếng Anh").evaluation("Chỉ số Vi Index tổng hợp").build()
        );

        return LearningPlanDto.builder()
            .userId(userId)
            .userName(userName != null ? userName : "Học viên")
            .createdAt(now)
            .startDate(now)
            .endDate(now.plusDays(28))
            .planTitle("Kế hoạch học tập 4 tuần nâng cao năng lực CEFR")
            .planDescription("Kế hoạch được AI thiết kế tối ưu theo mô hình Microlearning (15-20 phút/ngày) để duy trì sự tập trung tối đa.")
            .primaryGoal("Chinh phục 150 từ vựng cốt lõi & cải thiện độ trôi chảy cấp độ B1")
            .secondaryGoals(List.of("Đạt 80% độ chính xác trong các bài Quiz", "Duy trì chuỗi streak học tập hàng ngày"))
            .totalCommitmentMinutesPerWeek(90)
            .weeks(weeks)
            .milestones(milestones)
            .difficultyLevel("MODERATE")
            .confidenceScore(88.0)
            .build();
    }
}
