package com.vocabularyai.speaking.controller;

import com.vocabularyai.speaking.dto.response.SpeakingProgressResponse;
import com.vocabularyai.speaking.service.SpeakingProgressService;
import com.vocabularyai.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * SpeakingProgressController
 * 
 * REST API Controller cho Speaking Progress Tracking
 * 
 * Chịu trách nhiệm:
 * - Lấy tiến độ học speaking của user
 * - Hiển thị thống kê, trends, achievements
 * - Track streaks, improvements
 * - Cung cấp data cho learning dashboard
 * 
 * Endpoints:
 * - GET /api/ai/speaking/progress - Get user's speaking progress
 * - GET /api/ai/speaking/progress/trend/{days} - Get score trend
 * - GET /api/ai/speaking/progress/milestones - Get achievements
 * 
 * Security:
 * - Require JWT authentication
 * - Users can only see their own progress
 */
@Slf4j
@RestController
@RequestMapping("/api/ai/speaking/progress")
@RequiredArgsConstructor
@Tag(name = "Speaking Progress", description = "API để tracking tiến độ học speaking (statistics, trends, milestones)")
@SecurityRequirement(name = "bearer-jwt")
public class SpeakingProgressController {
    
    private final SpeakingProgressService progressService;
    
    /**
     * GET /api/ai/speaking/progress
     * 
     * Lấy tiến độ learning speaking tổng hợp của user
     * 
     * Response: SpeakingProgressResponse với:
     * - Statistics (total sessions, attempts, time spent)
     * - Average scores breakdown (grammar, vocabulary, fluency, content)
     * - Best scores breakdown
     * - Recent performance (7 day, 30 day analysis)
     * - Streaks (current, longest)
     * - Achievements (milestones)
     * - Score trend (improving/stable/declining)
     * 
     * Error cases:
     * - 401 Unauthorized: JWT token không hợp lệ
     * - 500 Internal Server Error
     */
    @GetMapping("")
    @Operation(
        summary = "Get user's speaking progress",
        description = "Lấy tiến độ học speaking tổng hợp: statistics, scores, trends, achievements, streaks",
        tags = {"Speaking Progress"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Speaking progress retrieved successfully",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = SpeakingProgressResponse.class))
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    public ResponseEntity<SpeakingProgressResponse> getSpeakingProgress(
        @AuthenticationPrincipal User user
    ) {
        log.info("Get speaking progress - userId: {}", user.getId());
        
        try {
            SpeakingProgressResponse progress = progressService.getSpeakingProgress(user.getId());
            
            log.info("Speaking progress retrieved - userId: {}, totalSessions: {}, averageScore: {}", 
                     user.getId(), 
                     progress.getStatistics().getTotalSessions(),
                     progress.getAverageScores().getOverallScore());
            
            return ResponseEntity.ok(progress);
            
        } catch (Exception e) {
            log.error("Error getting speaking progress - userId: {}, error: {}", 
                      user.getId(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * GET /api/ai/speaking/progress/trend/{days}
     * 
     * Phân tích điểm trend trong N ngày gần đây
     * 
     * Path param:
     * - days: 7 (tuần) hoặc 30 (tháng)
     * 
     * Response: Object với trend analysis:
     * - scoreTrend: "Improving" / "Stable" / "Declining"
     * - improvement: % thay đổi so với kỳ trước
     * - details: Chi tiết phân tích
     * 
     * Error cases:
     * - 400 Bad Request: days không hợp lệ
     * - 401 Unauthorized
     */
    @GetMapping("/trend/{days}")
    @Operation(
        summary = "Calculate speaking score trend",
        description = "Phân tích trend điểm speaking trong N ngày (Improving/Stable/Declining)",
        tags = {"Speaking Progress"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Trend analysis completed",
        content = @Content(mediaType = "application/json")
    )
    @ApiResponse(responseCode = "400", description = "Invalid days parameter")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<?> calculateScoreTrend(
        @Parameter(description = "Number of days to analyze (7 or 30)", required = true, example = "7")
        @PathVariable int days,
        
        @AuthenticationPrincipal User user
    ) {
        log.info("Calculate score trend - userId: {}, days: {}", user.getId(), days);
        
        if (days != 7 && days != 30) {
            log.warn("Invalid days parameter - userId: {}, days: {}", user.getId(), days);
            return ResponseEntity.badRequest().body(
                java.util.Map.of("error", "Days must be 7 or 30")
            );
        }
        
        try {
            String trend = progressService.calculateScoreTrend(user.getId(), days);
            
            var response = java.util.Map.of(
                "days", days,
                "scoreTrend", trend,
                "analyzedAt", java.time.LocalDateTime.now()
            );
            
            log.info("Score trend calculated - userId: {}, days: {}, trend: {}", 
                     user.getId(), days, trend);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error calculating score trend - userId: {}, days: {}, error: {}", 
                      user.getId(), days, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * GET /api/ai/speaking/progress/milestones
     * 
     * Lấy danh sách milestones/achievements của user
     * 
     * Milestones bao gồm:
     * - First session completed
     * - 5/10/20/30/50 sessions completed
     * - Average score > 80 / > 90
     * - Best score > 90 / > 95
     * - 7-day streak / 14-day streak / 30-day streak
     * - Perfect score (100) achieved
     * 
     * Response: List<String> danh sách achievement descriptions
     * 
     * Error cases:
     * - 401 Unauthorized
     */
    @GetMapping("/milestones")
    @Operation(
        summary = "Get user's speaking milestones",
        description = "Lấy danh sách các achievements/milestones đã đạt được",
        tags = {"Speaking Progress"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Milestones retrieved successfully",
        content = @Content(mediaType = "application/json")
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<?> getMilestones(
        @AuthenticationPrincipal User user
    ) {
        log.info("Get milestones - userId: {}", user.getId());
        
        try {
            SpeakingProgressResponse progress = progressService.getSpeakingProgress(user.getId());
            
            var response = java.util.Map.of(
                "milestones", progress.getAchievements(),
                "totalMilestones", progress.getAchievements().size(),
                "retrievedAt", java.time.LocalDateTime.now()
            );
            
            log.info("Milestones retrieved - userId: {}, count: {}", 
                     user.getId(), progress.getAchievements().size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting milestones - userId: {}, error: {}", 
                      user.getId(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * GET /api/ai/speaking/progress/summary
     * 
     * Lấy summary ngắn gọn cho dashboard
     * 
     * Response: Simplified object với:
     * - totalSessions
     * - averageScore
     * - bestScore
     * - currentStreak
     * - scoreTrend
     * 
     * Dùng cho quick display trên dashboard/home page
     */
    @GetMapping("/summary")
    @Operation(
        summary = "Get speaking progress summary",
        description = "Lấy summary ngắn gọn tiến độ speaking để hiển thị trên dashboard",
        tags = {"Speaking Progress"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Summary retrieved successfully",
        content = @Content(mediaType = "application/json")
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<?> getProgressSummary(
        @AuthenticationPrincipal User user
    ) {
        log.info("Get progress summary - userId: {}", user.getId());
        
        try {
            SpeakingProgressResponse progress = progressService.getSpeakingProgress(user.getId());
            
            var summary = java.util.Map.of(
                "totalSessions", progress.getStatistics().getTotalSessions(),
                "completedSessions", progress.getStatistics().getCompletedSessions(),
                "totalAttempts", progress.getStatistics().getTotalAttempts(),
                "averageScore", progress.getAverageScores().getOverallScore(),
                "bestScore", progress.getBestScores().getOverallScore(),
                "currentStreak", progress.getStreaks().getCurrentStreak(),
                "scoreTrend", progress.getScoreTrend(),
                "nextMilestone", progress.getAchievements().isEmpty() ? 
                    "First speaking session!" : 
                    "Keep practicing!"
            );
            
            log.info("Progress summary retrieved - userId: {}, sessions: {}", 
                     user.getId(), progress.getStatistics().getTotalSessions());
            
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            log.error("Error getting progress summary - userId: {}, error: {}", 
                      user.getId(), e.getMessage(), e);
            throw e;
        }
    }
}
