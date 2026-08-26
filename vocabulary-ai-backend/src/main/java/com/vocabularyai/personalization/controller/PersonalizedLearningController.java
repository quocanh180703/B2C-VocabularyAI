package com.vocabularyai.personalization.controller;

import com.vocabularyai.personalization.dto.LearningProfileResponse;
import com.vocabularyai.personalization.dto.WeaknessStrengthResponse;
import com.vocabularyai.personalization.dto.RecommendationResponse;
import com.vocabularyai.personalization.dto.LearningPlanResponse;
import com.vocabularyai.personalization.dto.DailyPracticeResponse;
import com.vocabularyai.personalization.dto.AdaptiveDifficultyResponse;
import com.vocabularyai.personalization.dto.ProgressAnalysisResponse;
import com.vocabularyai.personalization.mapper.WeaknessStrengthMapper;
import com.vocabularyai.personalization.mapper.RecommendationMapper;
import com.vocabularyai.personalization.mapper.LearningPlanMapper;
import com.vocabularyai.personalization.mapper.DailyPracticeMapper;
import com.vocabularyai.personalization.mapper.AdaptiveDifficultyMapper;
import com.vocabularyai.personalization.mapper.ProgressAnalysisMapper;
import com.vocabularyai.personalization.service.LearningProfileService;
import com.vocabularyai.personalization.service.WeaknessStrengthService;
import com.vocabularyai.personalization.service.RecommendationService;
import com.vocabularyai.personalization.service.LearningPlanService;
import com.vocabularyai.personalization.service.DailyPracticeService;
import com.vocabularyai.personalization.service.AdaptiveDifficultyService;
import com.vocabularyai.personalization.service.ProgressAnalysisService;
import com.vocabularyai.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * PersonalizedLearningController
 * 
 * REST API endpoints cho AI Personalized Learning Module
 * 
 * Base URL: /api/ai/personalized
 * 
 * Endpoints:
 * - GET /api/ai/personalized/profile - Get user learning profile
 * - GET /api/ai/personalized/analysis - Get weakness/strength analysis (Task 2)
 * - GET /api/ai/personalized/recommendations - Get learning recommendations (Task 3)
 * - GET /api/ai/personalized/plan - Get personalized learning plan (Task 4)
 * - GET /api/ai/personalized/daily-practice - Get today's practice (Task 5)
 * - GET /api/ai/personalized/progress-analysis - Get progress analysis (Task 7)
 * - GET /api/ai/personalized/dashboard - Get full dashboard (Task 8)
 * 
 * Security:
 * - JWT authentication required
 * - User can only see their own data
 * - Rate limiting on AI endpoints
 */
@Slf4j
@RestController
@RequestMapping("/api/ai/personalized")
@RequiredArgsConstructor
@Tag(name = "Personalized Learning", description = "AI-powered personalized learning recommendations and analytics")
@SecurityRequirement(name = "bearer-jwt")
public class PersonalizedLearningController {
    
    private final LearningProfileService learningProfileService;
    private final WeaknessStrengthService weaknessStrengthService;
    private final WeaknessStrengthMapper weaknessStrengthMapper;
    private final RecommendationService recommendationService;
    private final RecommendationMapper recommendationMapper;
    private final LearningPlanService learningPlanService;
    private final LearningPlanMapper learningPlanMapper;
    private final DailyPracticeService dailyPracticeService;
    private final DailyPracticeMapper dailyPracticeMapper;
    private final AdaptiveDifficultyService adaptiveDifficultyService;
    private final AdaptiveDifficultyMapper adaptiveDifficultyMapper;
    private final ProgressAnalysisService progressAnalysisService;
    private final ProgressAnalysisMapper progressAnalysisMapper;
    
    /**
     * GET /api/ai/personalized/profile
     * 
     * Get comprehensive user learning profile
     * 
     * Aggregates data from:
     * - Vocabulary learning (Phase 4)
     * - Grammar learning (Phase 5)
     * - Quiz results (Phase 3)
     * - Speaking practice (Phase 8)
     * - AI interactions (Phase 7)
     * 
     * Response includes:
     * - Current level (A1-C2)
     * - Overall score
     * - Performance breakdown (vocabulary, grammar, speaking)
     * - 7-day and 30-day trends
     * - Recent activities
     * - Streaks and achievements
     * - Weak and strong topics
     * - Learning patterns
     * 
     * Cache:
     * - Key: ai:profile:{userId}
     * - TTL: 1 hour
     * - Invalidated on activity update
     * 
     * Error cases:
     * - 401 Unauthorized: Invalid JWT
     * - 404 Not Found: User not found
     * - 500 Internal Server Error: Service error
     */
    @GetMapping("/profile")
    @Operation(
        summary = "Get user's learning profile",
        description = "Get comprehensive aggregated learning profile from all modules",
        tags = {"Personalized Learning"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Learning profile retrieved successfully",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = LearningProfileResponse.class))
    )
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized - invalid JWT token"
    )
    @ApiResponse(
        responseCode = "404",
        description = "User not found"
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error"
    )
    public ResponseEntity<LearningProfileResponse> getLearningProfile(
        @AuthenticationPrincipal User user
    ) {
        log.info("GET /api/ai/personalized/profile - userId: {}", user.getId());
        
        try {
            var profile = learningProfileService.getLearningProfile(user.getId());
            
            LearningProfileResponse response = LearningProfileResponse.builder()
                .profile(profile)
                .fromCache(false) // Will be updated when Phase 9 Redis integration is done
                .cacheTtlSeconds(3600L) // 1 hour
                .timestamp(LocalDateTime.now())
                .build();
            
            log.info("Learning profile retrieved successfully for user {}, score: {}",
                     user.getId(), profile.getOverallScore());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error retrieving learning profile for user {}: {}",
                      user.getId(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * GET /api/ai/personalized/level
     * 
     * Get estimated current English level
     * 
     * Returns: A1, A2, B1, B2, C1, or C2
     * Based on: Overall score, speaking performance, vocabulary, grammar
     */
    @GetMapping("/level")
    @Operation(
        summary = "Get estimated English level",
        description = "Get user's estimated current English proficiency level (A1-C2)",
        tags = {"Personalized Learning"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Level estimated successfully"
    )
    public ResponseEntity<?> getCurrentLevel(
        @AuthenticationPrincipal User user
    ) {
        log.info("GET /api/ai/personalized/level - userId: {}", user.getId());
        
        try {
            String level = learningProfileService.estimateCurrentLevel(user.getId());
            
            var response = java.util.Map.of(
                "level", level,
                "estimatedAt", LocalDateTime.now()
            );
            
            log.info("Current level estimated for user {}: {}", user.getId(), level);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error estimating level for user {}: {}", user.getId(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * GET /api/ai/personalized/weak-topics
     * 
     * Get weak topics/areas where user struggles
     * 
     * Topics with < 60% accuracy
     */
    @GetMapping("/weak-topics")
    @Operation(
        summary = "Get weak learning topics",
        description = "Get list of topics where user has <60% accuracy",
        tags = {"Personalized Learning"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Weak topics retrieved"
    )
    public ResponseEntity<?> getWeakTopics(
        @AuthenticationPrincipal User user
    ) {
        log.info("GET /api/ai/personalized/weak-topics - userId: {}", user.getId());
        
        try {
            var weakTopics = learningProfileService.getWeakTopics(user.getId());
            
            var response = java.util.Map.of(
                "weakTopics", weakTopics,
                "count", weakTopics.size(),
                "retrievedAt", LocalDateTime.now()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting weak topics for user {}: {}", user.getId(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * GET /api/ai/personalized/strong-topics
     * 
     * Get strong topics/areas where user excels
     * 
     * Topics with > 80% accuracy
     */
    @GetMapping("/strong-topics")
    @Operation(
        summary = "Get strong learning topics",
        description = "Get list of topics where user has >80% accuracy",
        tags = {"Personalized Learning"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Strong topics retrieved"
    )
    public ResponseEntity<?> getStrongTopics(
        @AuthenticationPrincipal User user
    ) {
        log.info("GET /api/ai/personalized/strong-topics - userId: {}", user.getId());
        
        try {
            var strongTopics = learningProfileService.getStrongTopics(user.getId());
            
            var response = java.util.Map.of(
                "strongTopics", strongTopics,
                "count", strongTopics.size(),
                "retrievedAt", LocalDateTime.now()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting strong topics for user {}: {}", user.getId(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * GET /api/ai/personalized/frequency
     * 
     * Get learning frequency (activities per week)
     */
    @GetMapping("/frequency")
    @Operation(
        summary = "Get learning frequency",
        description = "Get user's learning frequency (activities per week)",
        tags = {"Personalized Learning"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Frequency calculated"
    )
    public ResponseEntity<?> getLearningFrequency(
        @AuthenticationPrincipal User user
    ) {
        log.info("GET /api/ai/personalized/frequency - userId: {}", user.getId());
        
        try {
            Double frequency = learningProfileService.getLearningFrequency(user.getId());
            
            var response = java.util.Map.of(
                "activitiesPerWeek", frequency,
                "calculatedAt", LocalDateTime.now()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting learning frequency for user {}: {}", user.getId(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * GET /api/ai/personalized/session-duration
     * 
     * Get average session duration (minutes)
     */
    @GetMapping("/session-duration")
    @Operation(
        summary = "Get average session duration",
        description = "Get user's average learning session duration in minutes",
        tags = {"Personalized Learning"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Duration retrieved"
    )
    public ResponseEntity<?> getAvgSessionDuration(
        @AuthenticationPrincipal User user
    ) {
        log.info("GET /api/ai/personalized/session-duration - userId: {}", user.getId());
        
        try {
            Double duration = learningProfileService.getAverageSessionDuration(user.getId());
            
            var response = java.util.Map.of(
                "averageDurationMinutes", duration,
                "calculatedAt", LocalDateTime.now()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting session duration for user {}: {}", user.getId(), e.getMessage(), e);
            throw e;
        }
    }
    
    // ==================== TASK 2: Weakness & Strength Detection ====================
    
    /**
     * GET /api/ai/personalized/analysis
     * 
     * Get AI-powered weakness and strength analysis
     * 
     * TASK 2 ENDPOINT
     * 
     * AI analysis of:
     * - Specific weaknesses with root causes
     * - Strengths that demonstrate competence
     * - Priority actions for improvement
     * - Learning style identification
     * - Motivation assessment
     * - Confidence score
     * 
     * Process:
     * 1. Get user learning profile (TASK 1)
     * 2. Send to Gemini AI for analysis
     * 3. Parse JSON response
     * 4. Cache results (2 hours)
     * 5. Return analysis
     * 
     * Cache:
     * - Key: ai:weakness-strength:{userId}
     * - TTL: 2 hours
     * - Invalidated on learning progress changes
     * 
     * Rate Limiting:
     * - Subject to Gemini API rate limits
     * - 401 if rate limit exceeded
     * 
     * Error cases:
     * - 401 Unauthorized: Invalid JWT or rate limit exceeded
     * - 404 Not Found: User not found
     * - 500 Service Unavailable: Gemini API error
     * - 500 Internal Server Error: Analysis parsing error
     */
    @GetMapping("/analysis")
    @Operation(
        summary = "AI weakness and strength analysis",
        description = "Get AI-powered analysis of user's weaknesses, strengths, and priority actions (TASK 2)",
        tags = {"Personalized Learning - Task 2"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Analysis completed successfully",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = WeaknessStrengthResponse.class))
    )
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized - invalid JWT or rate limit exceeded"
    )
    @ApiResponse(
        responseCode = "404",
        description = "User not found"
    )
    @ApiResponse(
        responseCode = "500",
        description = "Service error (Gemini API unavailable or parsing failed)"
    )
    public ResponseEntity<WeaknessStrengthResponse> analyzeWeaknessesAndStrengths(
        @AuthenticationPrincipal User user
    ) {
        log.info("[TASK 2] GET /api/ai/personalized/analysis - userId: {}", user.getId());
        
        try {
            var analysis = weaknessStrengthService.analyzeWeaknessesAndStrengths(user.getId());
            
            WeaknessStrengthResponse response = weaknessStrengthMapper.toResponse(analysis);
            
            log.info("[TASK 2] Analysis completed for user {}: {} weaknesses, {} strengths",
                     user.getId(), analysis.getWeaknessCount(), analysis.getStrengthCount());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("[TASK 2] Error analyzing weaknesses/strengths for user {}: {}",
                      user.getId(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * GET /api/ai/personalized/analysis/critical-weaknesses
     * 
     * Get only critical weaknesses from analysis
     * 
     * Returns subset of analysis with severity = CRITICAL
     * 
     * Error cases:
     * - 401 Unauthorized: Invalid JWT
     * - 404 Not Found: User not found
     * - 500 Service Error: Analysis failed
     */
    @GetMapping("/analysis/critical-weaknesses")
    @Operation(
        summary = "Get critical weaknesses only",
        description = "Get critical weaknesses (severity=CRITICAL) from weakness/strength analysis",
        tags = {"Personalized Learning - Task 2"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Critical weaknesses retrieved"
    )
    public ResponseEntity<?> getCriticalWeaknesses(
        @AuthenticationPrincipal User user
    ) {
        log.info("[TASK 2] GET /api/ai/personalized/analysis/critical-weaknesses - userId: {}", user.getId());
        
        try {
            var critical = weaknessStrengthService.getCriticalWeaknesses(user.getId());
            
            var response = java.util.Map.of(
                "criticalWeaknesses", critical,
                "count", critical.size(),
                "retrievedAt", LocalDateTime.now()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("[TASK 2] Error getting critical weaknesses for user {}: {}", user.getId(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * GET /api/ai/personalized/analysis/outstanding-strengths
     * 
     * Get only outstanding strengths from analysis
     * 
     * Returns subset of analysis with excellence level = OUTSTANDING
     * 
     * Error cases:
     * - 401 Unauthorized: Invalid JWT
     * - 404 Not Found: User not found
     * - 500 Service Error: Analysis failed
     */
    @GetMapping("/analysis/outstanding-strengths")
    @Operation(
        summary = "Get outstanding strengths only",
        description = "Get outstanding strengths (excellenceLevel=OUTSTANDING) from analysis",
        tags = {"Personalized Learning - Task 2"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Outstanding strengths retrieved"
    )
    public ResponseEntity<?> getOutstandingStrengths(
        @AuthenticationPrincipal User user
    ) {
        log.info("[TASK 2] GET /api/ai/personalized/analysis/outstanding-strengths - userId: {}", user.getId());
        
        try {
            var outstanding = weaknessStrengthService.getOutstandingStrengths(user.getId());
            
            var response = java.util.Map.of(
                "outstandingStrengths", outstanding,
                "count", outstanding.size(),
                "retrievedAt", LocalDateTime.now()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("[TASK 2] Error getting outstanding strengths for user {}: {}", user.getId(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * GET /api/ai/personalized/analysis/priority-actions
     * 
     * Get top priority actions from analysis
     * 
     * Query parameter:
     * - limit: Number of actions to return (default: 5, max: 10)
     * 
     * Error cases:
     * - 401 Unauthorized: Invalid JWT
     * - 404 Not Found: User not found
     * - 500 Service Error: Analysis failed
     */
    @GetMapping("/analysis/priority-actions")
    @Operation(
        summary = "Get priority actions",
        description = "Get top priority actions from weakness/strength analysis",
        tags = {"Personalized Learning - Task 2"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Priority actions retrieved"
    )
    public ResponseEntity<?> getPriorityActions(
        @AuthenticationPrincipal User user,
        @RequestParam(defaultValue = "5", required = false) int limit
    ) {
        log.info("[TASK 2] GET /api/ai/personalized/analysis/priority-actions - userId: {}, limit: {}", user.getId(), limit);
        
        // Cap limit at 10
        limit = Math.min(limit, 10);
        
        try {
            var actions = weaknessStrengthService.getTopPriorityActions(user.getId(), limit);
            
            var response = java.util.Map.of(
                "priorityActions", actions,
                "count", actions.size(),
                "retrievedAt", LocalDateTime.now()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("[TASK 2] Error getting priority actions for user {}: {}", user.getId(), e.getMessage(), e);
            throw e;
        }
    }
    
    // ==================== TASK 3: AI Learning Recommendation Engine ====================
    
    /**
     * GET /api/ai/personalized/recommendations
     * 
     * Get AI-powered personalized learning recommendations
     * 
     * TASK 3 MAIN ENDPOINT
     * 
     * AI Recommendations bao gồm:
     * - 10-15 gợi ý học tập cụ thể
     * - Xếp hạng theo ưu tiên (priority 1-15)
     * - Phân loại (quick wins, long-term, critical)
     * - Ước tính nỗ lực (phút/tuần)
     * - Lịch trình hàng tuần
     * - Cột mốc dự kiến
     * - Mục tiêu 4 tuần
     * 
     * Quy trình:
     * 1. Lấy phân tích điểm yếu/điểm mạnh (TASK 2)
     * 2. Gửi đến Gemini AI để tạo gợi ý
     * 3. Phân tích phản hồi JSON
     * 4. Cache kết quả (3 giờ)
     * 5. Trả về gợi ý
     * 
     * Cache:
     * - Key: ai:recommendations:{userId}
     * - TTL: 3 giờ
     * - Vô hiệu hóa khi tiến độ thay đổi
     * 
     * Rate Limiting:
     * - Theo rate limit Gemini API
     * - 429 nếu vượt quá
     * 
     * Error cases:
     * - 401 Unauthorized: JWT không hợp lệ hoặc rate limit vượt quá
     * - 404 Not Found: Không tìm thấy người dùng
     * - 500 Service Unavailable: Lỗi Gemini API
     * - 500 Internal Server Error: Lỗi phân tích
     */
    @GetMapping("/recommendations")
    @Operation(
        summary = "AI Learning Recommendations (TASK 3)",
        description = "Nhận gợi ý học tập cá nhân hóa từ AI dựa trên phân tích điểm yếu/điểm mạnh",
        tags = {"Personalized Learning - Task 3"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Gợi ý được tạo thành công",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = RecommendationResponse.class))
    )
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized - JWT không hợp lệ hoặc rate limit vượt quá"
    )
    @ApiResponse(
        responseCode = "404",
        description = "Không tìm thấy người dùng"
    )
    @ApiResponse(
        responseCode = "500",
        description = "Lỗi dịch vụ (Gemini API lỗi hoặc phân tích thất bại)"
    )
    public ResponseEntity<RecommendationResponse> generateRecommendations(
        @AuthenticationPrincipal User user
    ) {
        log.info("[TASK 3] GET /api/ai/personalized/recommendations - userId: {}", user.getId());
        
        try {
            var recommendations = recommendationService.generateRecommendations(user.getId());
            
            RecommendationResponse response = recommendationMapper.toResponse(recommendations);
            
            log.info("[TASK 3] Gợi ý được tạo thành công cho userId: {} ({} gợi ý)",
                     user.getId(), recommendations.getRecommendationCount());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("[TASK 3] Lỗi tạo gợi ý cho userId: {}: {}",
                      user.getId(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * GET /api/ai/personalized/recommendations/quick-wins
     * 
     * Nhận các gợi ý nhanh chóng (dễ áp dụng, tác động cao)
     * 
     * Quick wins là những gợi ý:
     * - Impact: HIGH
     * - Effort: EASY
     * - Có thể áp dụng ngay lập tức
     * 
     * Error cases:
     * - 401 Unauthorized: JWT không hợp lệ
     * - 404 Not Found: Không tìm thấy người dùng
     * - 500 Service Error: Tạo gợi ý thất bại
     */
    @GetMapping("/recommendations/quick-wins")
    @Operation(
        summary = "Gợi ý nhanh chóng (TASK 3)",
        description = "Nhận gợi ý dễ áp dụng với tác động cao (impact=HIGH, effort=EASY)",
        tags = {"Personalized Learning - Task 3"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Gợi ý nhanh chóng được truy xuất"
    )
    public ResponseEntity<?> getQuickWins(
        @AuthenticationPrincipal User user
    ) {
        log.info("[TASK 3] GET /api/ai/personalized/recommendations/quick-wins - userId: {}", user.getId());
        
        try {
            var quickWins = recommendationService.getQuickWins(user.getId());
            
            var response = java.util.Map.of(
                "quickWins", quickWins,
                "count", quickWins.size(),
                "retrievedAt", LocalDateTime.now()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("[TASK 3] Lỗi lấy quick wins cho userId: {}: {}", user.getId(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * GET /api/ai/personalized/recommendations/long-term
     * 
     * Nhận gợi ý dài hạn (cần thời gian nhưng giá trị lớn)
     * 
     * Long-term goals là những gợi ý:
     * - Impact: HIGH
     * - Effort: CHALLENGING
     * - Yêu cầu cam kết dài hạn
     * 
     * Error cases:
     * - 401 Unauthorized: JWT không hợp lệ
     * - 404 Not Found: Không tìm thấy người dùng
     * - 500 Service Error: Tạo gợi ý thất bại
     */
    @GetMapping("/recommendations/long-term")
    @Operation(
        summary = "Gợi ý dài hạn (TASK 3)",
        description = "Nhận gợi ý yêu cầu cam kết dài hạn với giá trị lớn (impact=HIGH, effort=CHALLENGING)",
        tags = {"Personalized Learning - Task 3"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Gợi ý dài hạn được truy xuất"
    )
    public ResponseEntity<?> getLongTermGoals(
        @AuthenticationPrincipal User user
    ) {
        log.info("[TASK 3] GET /api/ai/personalized/recommendations/long-term - userId: {}", user.getId());
        
        try {
            var longTermGoals = recommendationService.getLongTermGoals(user.getId());
            
            var response = java.util.Map.of(
                "longTermGoals", longTermGoals,
                "count", longTermGoals.size(),
                "retrievedAt", LocalDateTime.now()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("[TASK 3] Lỗi lấy long-term goals cho userId: {}: {}", user.getId(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * GET /api/ai/personalized/recommendations/critical
     * 
     * Nhận gợi ý khẩn cấp (xử lý điểm yếu CRITICAL)
     * 
     * Critical recommendations là những gợi ý:
     * - Địa chỉ điểm yếu có severity = CRITICAL
     * - Impact: HIGH
     * - Ưu tiên cao
     * 
     * Error cases:
     * - 401 Unauthorized: JWT không hợp lệ
     * - 404 Not Found: Không tìm thấy người dùng
     * - 500 Service Error: Tạo gợi ý thất bại
     */
    @GetMapping("/recommendations/critical")
    @Operation(
        summary = "Gợi ý khẩn cấp (TASK 3)",
        description = "Nhận gợi ý để xử lý điểm yếu khẩn cấp (severity=CRITICAL)",
        tags = {"Personalized Learning - Task 3"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Gợi ý khẩn cấp được truy xuất"
    )
    public ResponseEntity<?> getCriticalRecommendations(
        @AuthenticationPrincipal User user
    ) {
        log.info("[TASK 3] GET /api/ai/personalized/recommendations/critical - userId: {}", user.getId());
        
        try {
            var critical = recommendationService.getCriticalRecommendations(user.getId());
            
            var response = java.util.Map.of(
                "criticalRecommendations", critical,
                "count", critical.size(),
                "retrievedAt", LocalDateTime.now()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("[TASK 3] Lỗi lấy critical recommendations cho userId: {}: {}", user.getId(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * GET /api/ai/personalized/recommendations/weekly-schedule
     * 
     * Nhận lịch trình hàng tuần từ gợi ý
     * 
     * Lịch trình bao gồm:
     * - 7 ngày (thứ 2-chủ nhật)
     * - Nhiệm vụ chính cho mỗi ngày
     * - Thời gian đề xuất
     * - Thời lượng và loại hoạt động
     * 
     * Error cases:
     * - 401 Unauthorized: JWT không hợp lệ
     * - 404 Not Found: Không tìm thấy người dùng
     * - 500 Service Error: Tạo gợi ý thất bại
     */
    @GetMapping("/recommendations/weekly-schedule")
    @Operation(
        summary = "Lịch trình hàng tuần (TASK 3)",
        description = "Nhận lịch trình học tập cụ thể cho 7 ngày",
        tags = {"Personalized Learning - Task 3"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Lịch trình được truy xuất"
    )
    public ResponseEntity<?> getWeeklySchedule(
        @AuthenticationPrincipal User user
    ) {
        log.info("[TASK 3] GET /api/ai/personalized/recommendations/weekly-schedule - userId: {}", user.getId());
        
        try {
            var weeklySchedule = recommendationService.getWeeklySchedule(user.getId());
            
            var response = java.util.Map.of(
                "weeklySchedule", weeklySchedule,
                "daysPlanned", weeklySchedule.size(),
                "retrievedAt", LocalDateTime.now()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("[TASK 3] Lỗi lấy weekly schedule cho userId: {}: {}", user.getId(), e.getMessage(), e);
            throw e;
        }
    }
    
    // ==================== TASK 4: Personalized Learning Plan ====================
    
    /**
     * GET /api/ai/personalized/plan
     * 
     * Nhận kế hoạch học tập 4 tuần có cấu trúc chi tiết
     * 
     * TASK 4 MAIN ENDPOINT
     * 
     * Kế hoạch bao gồm:
     * - 4 tuần (28 ngày)
     * - Mỗi ngày có 2-4 nhiệm vụ cụ thể
     * - Tăng dần độ khó (Tuần 1 dễ → Tuần 4 khó)
     * - 4 cột mốc/milestone (1 mỗi tuần)
     * - Chi tiết thời gian cho mỗi nhiệm vụ
     * - Tài nguyên và hướng dẫn
     * - Tiêu chí đánh giá thành công
     * 
     * Quy trình:
     * 1. Lấy gợi ý từ TASK 3
     * 2. Gửi đến Gemini AI để tạo kế hoạch
     * 3. Phân tích phản hồi JSON
     * 4. Cache kết quả (7 ngày)
     * 5. Trả về kế hoạch
     * 
     * Cache:
     * - Key: ai:learning-plan:{userId}
     * - TTL: 7 ngày
     * - Vô hiệu hóa khi tiến độ thay đổi
     * 
     * Rate Limiting:
     * - Theo rate limit Gemini API
     * - 429 nếu vượt quá
     * 
     * Error cases:
     * - 401 Unauthorized: JWT không hợp lệ hoặc rate limit vượt quá
     * - 404 Not Found: Không tìm thấy người dùng
     * - 500 Service Unavailable: Lỗi Gemini API
     * - 500 Internal Server Error: Lỗi phân tích
     */
    @GetMapping("/plan")
    @Operation(
        summary = "Kế hoạch học tập 4 tuần (TASK 4)",
        description = "Nhận kế hoạch học tập chi tiết 4 tuần (28 ngày) từ gợi ý cá nhân hóa",
        tags = {"Personalized Learning - Task 4"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Kế hoạch được tạo thành công",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = LearningPlanResponse.class))
    )
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized - JWT không hợp lệ hoặc rate limit vượt quá"
    )
    @ApiResponse(
        responseCode = "404",
        description = "Không tìm thấy người dùng"
    )
    @ApiResponse(
        responseCode = "500",
        description = "Lỗi dịch vụ (Gemini API lỗi hoặc phân tích thất bại)"
    )
    public ResponseEntity<LearningPlanResponse> createLearningPlan(
        @AuthenticationPrincipal User user
    ) {
        log.info("[TASK 4] GET /api/ai/personalized/plan - userId: {}", user.getId());
        
        try {
            var plan = learningPlanService.createLearningPlan(user.getId());
            
            LearningPlanResponse response = learningPlanMapper.toResponse(plan);
            
            log.info("[TASK 4] Kế hoạch được tạo thành công cho userId: {} (4 tuần, {} ngày)",
                     user.getId(), plan.getWeeks() != null ? plan.getWeeks().size() * 7 : 28);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("[TASK 4] Lỗi tạo kế hoạch cho userId: {}: {}",
                      user.getId(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * GET /api/ai/personalized/plan/weekly-breakdown
     * 
     * Nhận chi tiết 4 tuần của kế hoạch
     * 
     * Trả về danh sách 4 tuần với:
     * - Mục tiêu tuần
     * - Chủ đề chính
     * - Danh sách 7 ngày
     * - Tổng thời gian
     * - Các cột mốc
     * 
     * Error cases:
     * - 401 Unauthorized: JWT không hợp lệ
     * - 404 Not Found: Không tìm thấy người dùng
     * - 500 Service Error: Tạo kế hoạch thất bại
     */
    @GetMapping("/plan/weekly-breakdown")
    @Operation(
        summary = "Chi tiết 4 tuần của kế hoạch (TASK 4)",
        description = "Nhận chi tiết mục tiêu, chủ đề, và hoạt động cho mỗi tuần",
        tags = {"Personalized Learning - Task 4"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Chi tiết tuần được truy xuất"
    )
    public ResponseEntity<?> getWeeklyBreakdown(
        @AuthenticationPrincipal User user
    ) {
        log.info("[TASK 4] GET /api/ai/personalized/plan/weekly-breakdown - userId: {}", user.getId());
        
        try {
            var weeks = learningPlanService.getWeeklyBreakdown(user.getId());
            
            var response = java.util.Map.of(
                "weeks", weeks,
                "totalWeeks", weeks.size(),
                "retrievedAt", LocalDateTime.now()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("[TASK 4] Lỗi lấy chi tiết tuần cho userId: {}: {}", user.getId(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * GET /api/ai/personalized/plan/milestones
     * 
     * Nhận các cột mốc (milestone) của kế hoạch
     * 
     * Cột mốc bao gồm:
     * - Tuần có cột mốc (1-4)
     * - Tên và mô tả
     * - Cách đánh giá thành công
     * - Phần thưởng/kỷ niệm
     * 
     * Error cases:
     * - 401 Unauthorized: JWT không hợp lệ
     * - 404 Not Found: Không tìm thấy người dùng
     * - 500 Service Error: Tạo kế hoạch thất bại
     */
    @GetMapping("/plan/milestones")
    @Operation(
        summary = "Các cột mốc của kế hoạch (TASK 4)",
        description = "Nhận danh sách cột mốc/milestone cần đạt được trong 4 tuần",
        tags = {"Personalized Learning - Task 4"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Cột mốc được truy xuất"
    )
    public ResponseEntity<?> getMilestones(
        @AuthenticationPrincipal User user
    ) {
        log.info("[TASK 4] GET /api/ai/personalized/plan/milestones - userId: {}", user.getId());
        
        try {
            var milestones = learningPlanService.getMilestones(user.getId());
            
            var response = java.util.Map.of(
                "milestones", milestones,
                "totalMilestones", milestones.size(),
                "retrievedAt", LocalDateTime.now()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("[TASK 4] Lỗi lấy milestones cho userId: {}: {}", user.getId(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * GET /api/ai/personalized/plan/week/{weekNumber}
     * 
     * Nhận chi tiết một tuần cụ thể
     * 
     * Path Parameter:
     * - weekNumber: Số tuần (1-4)
     * 
     * Trả về danh sách 7 ngày với:
     * - Tiêu đề ngày
     * - Mục tiêu hôm nay
     * - Danh sách 2-4 nhiệm vụ
     * - Tổng thời gian
     * - Độ khó dự kiến
     * - Ghi chú
     * 
     * Error cases:
     * - 400 Bad Request: weekNumber không hợp lệ (không 1-4)
     * - 401 Unauthorized: JWT không hợp lệ
     * - 404 Not Found: Không tìm thấy người dùng
     * - 500 Service Error: Tạo kế hoạch thất bại
     */
    @GetMapping("/plan/week/{weekNumber}")
    @Operation(
        summary = "Chi tiết tuần cụ thể (TASK 4)",
        description = "Nhận chi tiết 7 ngày và các nhiệm vụ cho một tuần cụ thể",
        tags = {"Personalized Learning - Task 4"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Chi tiết tuần được truy xuất"
    )
    @ApiResponse(
        responseCode = "400",
        description = "weekNumber không hợp lệ (phải 1-4)"
    )
    public ResponseEntity<?> getWeekDetails(
        @AuthenticationPrincipal User user,
        @RequestParam(required = true) int weekNumber
    ) {
        log.info("[TASK 4] GET /api/ai/personalized/plan/week/{} - userId: {}", weekNumber, user.getId());
        
        // Kiểm tra weekNumber hợp lệ
        if (weekNumber < 1 || weekNumber > 4) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                "error", "weekNumber phải từ 1 đến 4",
                "providedValue", weekNumber
            ));
        }
        
        try {
            var dayPlans = learningPlanService.getWeekDetails(user.getId(), weekNumber);
            
            var response = java.util.Map.of(
                "weekNumber", weekNumber,
                "days", dayPlans,
                "totalDays", dayPlans.size(),
                "retrievedAt", LocalDateTime.now()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("[TASK 4] Lỗi lấy chi tiết tuần {} cho userId: {}: {}", 
                weekNumber, user.getId(), e.getMessage(), e);
            throw e;
        }
    }
    
    // ==================== TASK 5: AI Daily Practice Recommendation ====================
    
    /**
     * GET /api/ai/personalized/daily-practice
     * 
     * Nhận gợi ý hoạt động học tập cho hôm nay (TASK 5)
     */
    @GetMapping("/daily-practice")
    @Operation(
        summary = "Hoạt động hôm nay (TASK 5)",
        description = "Nhận gợi ý hoạt động học tập được tối ưu hóa cho hôm nay",
        tags = {"Personalized Learning - Task 5"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Hoạt động được tạo thành công",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = DailyPracticeResponse.class))
    )
    public ResponseEntity<DailyPracticeResponse> getDailyPractice(
        @AuthenticationPrincipal User user
    ) {
        log.info("[TASK 5] GET /api/ai/personalized/daily-practice - userId: {}", user.getId());
        
        try {
            var practice = dailyPracticeService.getDailyPractice(user.getId());
            DailyPracticeResponse response = dailyPracticeMapper.toResponse(practice);
            
            log.info("[TASK 5] Hoạt động hôm nay cho userId: {} ({} hoạt động)",
                     user.getId(), practice.getActivities() != null ? practice.getActivities().size() : 0);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("[TASK 5] Lỗi lấy hoạt động cho userId: {}: {}",
                      user.getId(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * GET /api/ai/personalized/daily-practice/week/{weekNumber}/day/{dayOfWeek}
     * 
     * Nhận gợi ý hoạt động cho một ngày cụ thể (TASK 5)
     */
    @GetMapping("/daily-practice/week/{weekNumber}/day/{dayOfWeek}")
    @Operation(
        summary = "Hoạt động cho ngày cụ thể (TASK 5)",
        description = "Nhận gợi ý hoạt động cho một ngày cụ thể trong kế hoạch",
        tags = {"Personalized Learning - Task 5"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Hoạt động được truy xuất"
    )
    public ResponseEntity<DailyPracticeResponse> getDailyPracticeForDay(
        @AuthenticationPrincipal User user,
        @RequestParam(required = true) int weekNumber,
        @RequestParam(required = true) String dayOfWeek
    ) {
        log.info("[TASK 5] Lấy hoạt động cho tuần {} ngày {} userId: {}", 
                 weekNumber, dayOfWeek, user.getId());
        
        if (weekNumber < 1 || weekNumber > 4) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            var practice = dailyPracticeService.getDailyPracticeForDay(user.getId(), weekNumber, dayOfWeek);
            DailyPracticeResponse response = dailyPracticeMapper.toResponse(practice);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("[TASK 5] Lỗi: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * POST /api/ai/personalized/daily-practice/complete
     * 
     * Đánh dấu hoàn thành hoạt động hôm nay (TASK 5)
     */
    @PostMapping("/daily-practice/complete")
    @Operation(
        summary = "Đánh dấu hoàn thành (TASK 5)",
        description = "Đánh dấu hoạt động hôm nay đã hoàn thành",
        tags = {"Personalized Learning - Task 5"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Đánh dấu thành công"
    )
    @Transactional
    public ResponseEntity<?> completeDay(
        @AuthenticationPrincipal User user,
        @RequestParam(defaultValue = "true") boolean completed
    ) {
        log.info("[TASK 5] Đánh dấu hoàn thành cho userId: {} - {}", user.getId(), completed);
        
        try {
            dailyPracticeService.markDayAsCompleted(user.getId(), completed);
            
            return ResponseEntity.ok(java.util.Map.of(
                "message", "Đánh dấu thành công",
                "userId", user.getId(),
                "completed", completed,
                "timestamp", LocalDateTime.now()
            ));
            
        } catch (Exception e) {
            log.error("[TASK 5] Lỗi đánh dấu: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * POST /api/ai/personalized/daily-practice/notes
     * 
     * Lưu ghi chú cho hôm nay (TASK 5)
     */
    @PostMapping("/daily-practice/notes")
    @Operation(
        summary = "Lưu ghi chú (TASK 5)",
        description = "Lưu ghi chú cá nhân cho hoạt động hôm nay",
        tags = {"Personalized Learning - Task 5"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Ghi chú được lưu"
    )
    @Transactional
    public ResponseEntity<?> saveDailyNotes(
        @AuthenticationPrincipal User user,
        @RequestParam(required = true) String notes
    ) {
        log.info("[TASK 5] Lưu ghi chú cho userId: {}", user.getId());
        
        try {
            dailyPracticeService.saveDailyNotes(user.getId(), notes);
            
            return ResponseEntity.ok(java.util.Map.of(
                "message", "Ghi chú được lưu thành công",
                "userId", user.getId(),
                "timestamp", LocalDateTime.now()
            ));
            
        } catch (Exception e) {
            log.error("[TASK 5] Lỗi lưu ghi chú: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    // ==================== TASK 6: Adaptive Difficulty Adjustment ====================
    
    /**
     * GET /api/ai/personalized/adaptive-difficulty
     * 
     * Phân tích hiệu suất và gợi ý điều chỉnh độ khó (TASK 6)
     * 
     * TASK 6 MAIN ENDPOINT
     * 
     * Phân tích bao gồm:
     * - Hiệu suất hiện tại theo chủ đề
     * - Xu hướng hiệu suất (cải thiện, ổn định, giảm)
     * - Gợi ý điều chỉnh độ khó (dễ hơn, khó hơn, giữ nguyên)
     * - Yếu tố điều chỉnh (0.8 → dễ, 1.2 → khó)
     * - Danh sách chủ đề cần điều chỉnh
     * - Lịch tái đánh giá
     * - Lời khuyên cụ thể
     * 
     * Quy trình:
     * 1. Lấy kế hoạch học tập hiện tại
     * 2. Lấy hoạt động hôm nay để lấy ngữ cảnh
     * 3. Gửi đến Gemini AI để phân tích hiệu suất
     * 4. Phân tích phản hồi JSON
     * 5. Cache kết quả (30 ngày)
     * 6. Trả về phân tích
     * 
     * Cache:
     * - Key: ai:adaptive-difficulty:{userId}
     * - TTL: 30 ngày
     * - Vô hiệu hóa khi: Điều chỉnh độ khó, hoạt động hoàn thành
     * 
     * Rate Limiting:
     * - Theo rate limit Gemini API
     * - 429 nếu vượt quá
     * 
     * Error cases:
     * - 401 Unauthorized: JWT không hợp lệ hoặc rate limit vượt quá
     * - 404 Not Found: Không tìm thấy người dùng
     * - 500 Service Unavailable: Lỗi Gemini API
     * - 500 Internal Server Error: Lỗi phân tích
     */
    @GetMapping("/adaptive-difficulty")
    @Operation(
        summary = "Phân tích độ khó tự động (TASK 6)",
        description = "Phân tích hiệu suất và gợi ý điều chỉnh độ khó dựa trên hoạt động",
        tags = {"Personalized Learning - Task 6"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Phân tích được tạo thành công",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AdaptiveDifficultyResponse.class))
    )
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized - JWT không hợp lệ hoặc rate limit vượt quá"
    )
    @ApiResponse(
        responseCode = "404",
        description = "Không tìm thấy người dùng"
    )
    @ApiResponse(
        responseCode = "500",
        description = "Lỗi dịch vụ (Gemini API lỗi hoặc phân tích thất bại)"
    )
    public ResponseEntity<AdaptiveDifficultyResponse> analyzeAdaptiveDifficulty(
        @AuthenticationPrincipal User user
    ) {
        log.info("[TASK 6] GET /api/ai/personalized/adaptive-difficulty - userId: {}", user.getId());
        
        try {
            var analysis = adaptiveDifficultyService.analyzeAndRecommendDifficulty(user.getId());
            
            AdaptiveDifficultyResponse response = adaptiveDifficultyMapper.toResponse(analysis);
            
            log.info("[TASK 6] Phân tích độ khó hoàn thành cho userId: {}", user.getId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("[TASK 6] Lỗi phân tích độ khó cho userId: {}: {}",
                      user.getId(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * GET /api/ai/personalized/adaptive-difficulty/ease
     * 
     * Nhận danh sách các chủ đề nên làm dễ hơn (TASK 6)
     */
    @GetMapping("/adaptive-difficulty/ease")
    @Operation(
        summary = "Các chủ đề nên làm dễ hơn (TASK 6)",
        description = "Nhận danh sách chủ đề hiệu suất thấp cần làm dễ hơn",
        tags = {"Personalized Learning - Task 6"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Danh sách chủ đề được truy xuất"
    )
    public ResponseEntity<?> getTopicsToEase(
        @AuthenticationPrincipal User user
    ) {
        log.info("[TASK 6] GET /api/ai/personalized/adaptive-difficulty/ease - userId: {}", user.getId());
        
        try {
            var topicsToEase = adaptiveDifficultyService.getTopicsToEase(user.getId());
            
            var response = java.util.Map.of(
                "topicsToEase", topicsToEase,
                "count", topicsToEase.size(),
                "retrievedAt", LocalDateTime.now()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("[TASK 6] Lỗi lấy topics to ease cho userId: {}: {}", user.getId(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * GET /api/ai/personalized/adaptive-difficulty/challenge
     * 
     * Nhận danh sách các chủ đề nên làm khó hơn (TASK 6)
     */
    @GetMapping("/adaptive-difficulty/challenge")
    @Operation(
        summary = "Các chủ đề nên làm khó hơn (TASK 6)",
        description = "Nhận danh sách chủ đề hiệu suất cao sẵn sàng chứng cự",
        tags = {"Personalized Learning - Task 6"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Danh sách chủ đề được truy xuất"
    )
    public ResponseEntity<?> getTopicsToChallenge(
        @AuthenticationPrincipal User user
    ) {
        log.info("[TASK 6] GET /api/ai/personalized/adaptive-difficulty/challenge - userId: {}", user.getId());
        
        try {
            var topicsToChallenge = adaptiveDifficultyService.getTopicsToChallenge(user.getId());
            
            var response = java.util.Map.of(
                "topicsToChallenge", topicsToChallenge,
                "count", topicsToChallenge.size(),
                "retrievedAt", LocalDateTime.now()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("[TASK 6] Lỗi lấy topics to challenge cho userId: {}: {}", user.getId(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * GET /api/ai/personalized/adaptive-difficulty/topic/{topic}/recommended
     * 
     * Nhận độ khó được đề xuất cho một chủ đề cụ thể (TASK 6)
     * 
     * Path Parameter:
     * - topic: Tên chủ đề (VOCABULARY, GRAMMAR, SPEAKING, PRACTICE, REVIEW, QUIZ)
     * 
     * Response: Độ khó được đề xuất (1-10)
     */
    @GetMapping("/adaptive-difficulty/topic/{topic}/recommended")
    @Operation(
        summary = "Độ khó được đề xuất cho chủ đề (TASK 6)",
        description = "Nhận độ khó được đề xuất (1-10) cho một chủ đề cụ thể",
        tags = {"Personalized Learning - Task 6"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Độ khó được truy xuất"
    )
    public ResponseEntity<?> getRecommendedDifficultyForTopic(
        @AuthenticationPrincipal User user,
        @PathVariable(required = true) String topic
    ) {
        log.info("[TASK 6] GET /api/ai/personalized/adaptive-difficulty/topic/{}/recommended - userId: {}",
                 topic, user.getId());
        
        try {
            Double recommendedDifficulty = adaptiveDifficultyService.getRecommendedDifficultyForTopic(
                user.getId(), topic);
            
            var response = java.util.Map.of(
                "topic", topic,
                "recommendedDifficulty", recommendedDifficulty,
                "scale", "1-10",
                "retrievedAt", LocalDateTime.now()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("[TASK 6] Lỗi lấy recommended difficulty cho userId: {}, topic: {}: {}",
                      user.getId(), topic, e.getMessage(), e);
            throw e;
        }
    }
    
    // ==================== TASK 7: Progress Analysis ====================
    
    /**
     * GET /api/ai/personalized/progress-analysis
     * 
     * Phân tích tiến độ hướng tới mục tiêu (TASK 7)
     * 
     * TASK 7 MAIN ENDPOINT
     * 
     * Phân tích bao gồm:
     * - Tỉ lệ hoàn thành kế hoạch (%)
     * - Số ngày đã học / còn lại
     * - Xu hướng tiến độ (ON_TRACK, AHEAD, BEHIND)
     * - Tốc độ cải thiện hàng tuần
     * - Phân tích milestone (đã hoàn thành, hiện tại, tiếp theo)
     * - Dự đoán ngày hoàn thành
     * - Xác suất hoàn thành đúng hạn
     * - Hiệu suất trung bình, cao nhất, thấp nhất
     * - Gợi ý để duy trì tốc độ
     * - Cảnh báo nếu phát hiện vấn đề
     * 
     * Quy trình:
     * 1. Lấy kế hoạch học tập hiện tại
     * 2. Lấy profil hiện tại
     * 3. Tính toán tiến độ, velocity, dự đoán
     * 4. Gửi đến Gemini AI để phân tích
     * 5. Cache kết quả (1 ngày)
     * 6. Trả về phân tích
     * 
     * Cache:
     * - Key: ai:progress-analysis:{userId}
     * - TTL: 1 ngày
     * - Vô hiệu hóa khi: Hoàn thành hoạt động, hoàn thành milestone
     * 
     * Rate Limiting:
     * - Theo rate limit Gemini API
     * - 429 nếu vượt quá
     * 
     * Error cases:
     * - 401 Unauthorized: JWT không hợp lệ hoặc rate limit vượt quá
     * - 404 Not Found: Không tìm thấy người dùng
     * - 500 Service Unavailable: Lỗi Gemini API
     * - 500 Internal Server Error: Lỗi phân tích
     */
    @GetMapping("/progress-analysis")
    @Operation(
        summary = "Phân tích tiến độ học tập (TASK 7)",
        description = "Phân tích tiến độ hướng tới mục tiêu và dự đoán ngày hoàn thành",
        tags = {"Personalized Learning - Task 7"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Phân tích được tạo thành công",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProgressAnalysisResponse.class))
    )
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized - JWT không hợp lệ hoặc rate limit vượt quá"
    )
    @ApiResponse(
        responseCode = "404",
        description = "Không tìm thấy người dùng"
    )
    @ApiResponse(
        responseCode = "500",
        description = "Lỗi dịch vụ (Gemini API lỗi hoặc phân tích thất bại)"
    )
    public ResponseEntity<ProgressAnalysisResponse> analyzeProgress(
        @AuthenticationPrincipal User user
    ) {
        log.info("[TASK 7] GET /api/ai/personalized/progress-analysis - userId: {}", user.getId());
        
        try {
            var analysis = progressAnalysisService.analyzeProgress(user.getId());
            
            ProgressAnalysisResponse response = progressAnalysisMapper.toResponse(analysis);
            
            log.info("[TASK 7] Phân tích tiến độ hoàn thành cho userId: {}, completion: {}%",
                     user.getId(), analysis.getCompletionPercentage());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("[TASK 7] Lỗi phân tích tiến độ cho userId: {}: {}",
                      user.getId(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * GET /api/ai/personalized/progress-analysis/milestones
     * 
     * Nhận danh sách milestone và tiến độ của chúng (TASK 7)
     */
    @GetMapping("/progress-analysis/milestones")
    @Operation(
        summary = "Các milestone và tiến độ (TASK 7)",
        description = "Nhận danh sách milestone đã hoàn thành, hiện tại, và tiếp theo",
        tags = {"Personalized Learning - Task 7"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Danh sách milestone được truy xuất"
    )
    public ResponseEntity<?> getMilestoneProgress(
        @AuthenticationPrincipal User user
    ) {
        log.info("[TASK 7] GET /api/ai/personalized/progress-analysis/milestones - userId: {}", user.getId());
        
        try {
            var completed = progressAnalysisService.getCompletedMilestones(user.getId());
            var current = progressAnalysisService.getCurrentMilestone(user.getId());
            var next = progressAnalysisService.getNextMilestone(user.getId());
            
            var response = java.util.Map.of(
                "completedMilestones", completed,
                "currentMilestone", current,
                "nextMilestone", next,
                "retrievedAt", LocalDateTime.now()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("[TASK 7] Lỗi lấy milestone progress cho userId: {}: {}", user.getId(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * GET /api/ai/personalized/progress-analysis/prediction
     * 
     * Nhận dự đoán ngày hoàn thành và xác suất (TASK 7)
     */
    @GetMapping("/progress-analysis/prediction")
    @Operation(
        summary = "Dự đoán ngày hoàn thành (TASK 7)",
        description = "Nhận dự đoán ngày hoàn thành kế hoạch và xác suất hoàn thành đúng hạn",
        tags = {"Personalized Learning - Task 7"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Dự đoán được truy xuất"
    )
    public ResponseEntity<?> getPrediction(
        @AuthenticationPrincipal User user
    ) {
        log.info("[TASK 7] GET /api/ai/personalized/progress-analysis/prediction - userId: {}", user.getId());
        
        try {
            var completionDate = progressAnalysisService.getEstimatedCompletionDate(user.getId());
            var onTimeProbability = progressAnalysisService.getOnTimeCompletionProbability(user.getId());
            var daysAheadOrBehind = progressAnalysisService.getDaysAheadOrBehind(user.getId());
            
            var response = java.util.Map.of(
                "estimatedCompletionDate", completionDate,
                "onTimeCompletionProbability", onTimeProbability,
                "daysAheadOrBehind", daysAheadOrBehind,
                "status", daysAheadOrBehind > 0 ? "AHEAD" : (daysAheadOrBehind < 0 ? "BEHIND" : "ON_TRACK"),
                "retrievedAt", LocalDateTime.now()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("[TASK 7] Lỗi lấy prediction cho userId: {}: {}", user.getId(), e.getMessage(), e);
            throw e;
        }
    }
}


