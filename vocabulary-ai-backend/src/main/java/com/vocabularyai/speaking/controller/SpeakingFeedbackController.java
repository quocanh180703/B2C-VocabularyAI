package com.vocabularyai.speaking.controller;

import com.vocabularyai.speaking.dto.response.SpeakingFeedbackResponse;
import com.vocabularyai.speaking.service.SpeakingFeedbackService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * SpeakingFeedbackController
 * 
 * REST API Controller cho Speaking Feedback
 * 
 * Chịu trách nhiệm:
 * - Lấy feedback chi tiết của attempt hoặc phiên
 * - Lấy danh sách feedback với bộ lọc
 * - Tính toán điểm số trung bình
 * - Xem attempts tốt nhất
 * 
 * Endpoints:
 * - GET /api/ai/speaking/feedback/attempts/{attemptId} - Get attempt feedback
 * - GET /api/ai/speaking/feedback/sessions/{sessionId} - Get session feedback
 * - GET /api/ai/speaking/feedback/sessions/{sessionId}/list - List all feedback in session
 * - GET /api/ai/speaking/feedback/sessions/{sessionId}/best - Get best attempt
 * - GET /api/ai/speaking/feedback/sessions/{sessionId}/sorted-by-score - Get sorted by score
 * - GET /api/ai/speaking/feedback/sessions/{sessionId}/sorted-by-grammar - Get sorted by grammar
 * - GET /api/ai/speaking/feedback/sessions/{sessionId}/sorted-by-vocabulary - Get sorted by vocabulary
 * - GET /api/ai/speaking/feedback/sessions/{sessionId}/statistics - Get average scores
 * 
 * Security:
 * - Require JWT authentication
 * - Verify user ownership (can only see their own feedback)
 */
@Slf4j
@RestController
@RequestMapping("/api/ai/speaking/feedback")
@RequiredArgsConstructor
@Tag(name = "Speaking Feedback", description = "API để lấy feedback chi tiết của speaking attempts")
@SecurityRequirement(name = "bearer-jwt")
public class SpeakingFeedbackController {
    
    private final SpeakingFeedbackService feedbackService;
    
    /**
     * GET /api/ai/speaking/feedback/attempts/{attemptId}
     * 
     * Lấy feedback của một lần ghi âm
     * 
     * Response: SpeakingFeedbackResponse với:
     * - Scores (overall, grammar, vocabulary, fluency, content, pronunciation)
     * - Feedback text
     * - Corrections, suggestions, strengths, weaknesses
     * 
     * Error cases:
     * - 401 Unauthorized
     * - 403 Forbidden: User doesn't own this attempt
     * - 404 Not Found: Attempt not found
     * - 500 Internal Server Error
     */
    @GetMapping("/attempts/{attemptId}")
    @Operation(
        summary = "Get feedback of a speaking attempt",
        description = "Lấy feedback chi tiết của một lần ghi âm (scores, corrections, suggestions, strengths, weaknesses)",
        tags = {"Speaking Feedback"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Feedback retrieved successfully",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = SpeakingFeedbackResponse.class))
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden - user doesn't own this attempt")
    @ApiResponse(responseCode = "404", description = "Attempt not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    public ResponseEntity<SpeakingFeedbackResponse> getAttemptFeedback(
        @Parameter(description = "ID attempt", required = true)
        @PathVariable Long attemptId,
        
        @AuthenticationPrincipal User user
    ) {
        log.info("Get attempt feedback - attemptId: {}, userId: {}", attemptId, user.getId());
        
        try {
            SpeakingFeedbackResponse feedback = feedbackService.getAttemptFeedback(attemptId, user.getId());
            log.info("Attempt feedback retrieved - attemptId: {}, score: {}", attemptId, feedback.getOverallScore());
            return ResponseEntity.ok(feedback);
            
        } catch (Exception e) {
            log.error("Error getting attempt feedback - attemptId: {}, userId: {}, error: {}", 
                      attemptId, user.getId(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * GET /api/ai/speaking/feedback/sessions/{sessionId}
     * 
     * Lấy feedback tổng hợp của phiên
     * 
     * Response: Optional<SpeakingFeedbackResponse> (nullable)
     * 
     * Error cases:
     * - 401 Unauthorized
     * - 403 Forbidden: User doesn't own this session
     * - 404 Not Found: Session not found or no feedback
     * - 500 Internal Server Error
     */
    @GetMapping("/sessions/{sessionId}")
    @Operation(
        summary = "Get session feedback summary",
        description = "Lấy feedback tổng hợp của phiên luyện tập",
        tags = {"Speaking Feedback"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Session feedback retrieved successfully",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = SpeakingFeedbackResponse.class))
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden - user doesn't own this session")
    @ApiResponse(responseCode = "404", description = "Session not found or no feedback")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    public ResponseEntity<?> getSessionFeedback(
        @Parameter(description = "ID phiên", required = true)
        @PathVariable Long sessionId,
        
        @AuthenticationPrincipal User user
    ) {
        log.info("Get session feedback - sessionId: {}, userId: {}", sessionId, user.getId());
        
        try {
            Optional<SpeakingFeedbackResponse> feedback = feedbackService.getSessionFeedback(sessionId, user.getId());
            
            if (feedback.isPresent()) {
                log.info("Session feedback retrieved - sessionId: {}, score: {}", 
                         sessionId, feedback.get().getOverallScore());
                return ResponseEntity.ok(feedback.get());
            } else {
                log.info("No feedback found for session - sessionId: {}", sessionId);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Error getting session feedback - sessionId: {}, userId: {}, error: {}", 
                      sessionId, user.getId(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * GET /api/ai/speaking/feedback/sessions/{sessionId}/list
     * 
     * Lấy danh sách feedback của tất cả attempts trong phiên
     * 
     * Query params:
     * - page: Trang (default 0)
     * - size: Kích thước trang (default 20)
     * - sort: Sắp xếp (default createdAt,desc)
     * 
     * Response: Page<SpeakingFeedbackResponse> (paginated)
     */
    @GetMapping("/sessions/{sessionId}/list")
    @Operation(
        summary = "List all feedback in a session",
        description = "Lấy danh sách feedback của tất cả attempts trong phiên (paginated)",
        tags = {"Speaking Feedback"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Feedback list retrieved successfully",
        content = @Content(mediaType = "application/json")
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden - user doesn't own this session")
    @ApiResponse(responseCode = "404", description = "Session not found")
    public ResponseEntity<Page<SpeakingFeedbackResponse>> listFeedbackBySession(
        @Parameter(description = "ID phiên", required = true)
        @PathVariable Long sessionId,
        
        @PageableDefault(size = 20, page = 0, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable,
        
        @AuthenticationPrincipal User user
    ) {
        log.info("List feedback by session - sessionId: {}, userId: {}, page: {}", 
                 sessionId, user.getId(), pageable.getPageNumber());
        
        try {
            Page<SpeakingFeedbackResponse> feedbackPage = feedbackService.listFeedbackBySession(
                sessionId, 
                user.getId(), 
                pageable
            );
            
            log.info("Feedback list retrieved - sessionId: {}, total: {}", 
                     sessionId, feedbackPage.getTotalElements());
            return ResponseEntity.ok(feedbackPage);
            
        } catch (Exception e) {
            log.error("Error listing feedback - sessionId: {}, userId: {}, error: {}", 
                      sessionId, user.getId(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * GET /api/ai/speaking/feedback/sessions/{sessionId}/best
     * 
     * Lấy attempt tốt nhất (điểm cao nhất) của phiên
     * 
     * Response: SpeakingFeedbackResponse của attempt tốt nhất
     */
    @GetMapping("/sessions/{sessionId}/best")
    @Operation(
        summary = "Get best attempt in session",
        description = "Lấy attempt có điểm cao nhất trong phiên",
        tags = {"Speaking Feedback"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Best feedback retrieved successfully",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = SpeakingFeedbackResponse.class))
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden - user doesn't own this session")
    @ApiResponse(responseCode = "404", description = "Session not found or no feedback")
    public ResponseEntity<?> getBestFeedback(
        @Parameter(description = "ID phiên", required = true)
        @PathVariable Long sessionId,
        
        @AuthenticationPrincipal User user
    ) {
        log.info("Get best feedback - sessionId: {}, userId: {}", sessionId, user.getId());
        
        try {
            Optional<SpeakingFeedbackResponse> bestFeedback = feedbackService.getBestFeedbackInSession(
                sessionId, 
                user.getId()
            );
            
            if (bestFeedback.isPresent()) {
                log.info("Best feedback retrieved - sessionId: {}, score: {}", 
                         sessionId, bestFeedback.get().getOverallScore());
                return ResponseEntity.ok(bestFeedback.get());
            } else {
                log.info("No feedback found for session - sessionId: {}", sessionId);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Error getting best feedback - sessionId: {}, userId: {}, error: {}", 
                      sessionId, user.getId(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * GET /api/ai/speaking/feedback/sessions/{sessionId}/sorted-by-score
     * 
     * Lấy danh sách feedback sắp xếp theo điểm tổng thể (cao nhất trước)
     * 
     * Query params:
     * - page, size, sort (standard pagination)
     * 
     * Response: Page<SpeakingFeedbackResponse>
     */
    @GetMapping("/sessions/{sessionId}/sorted-by-score")
    @Operation(
        summary = "List feedback sorted by overall score",
        description = "Lấy danh sách feedback sắp xếp theo điểm tổng thể (cao nhất trước)",
        tags = {"Speaking Feedback"}
    )
    @ApiResponse(responseCode = "200", description = "Feedback list retrieved")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden - user doesn't own this session")
    public ResponseEntity<Page<SpeakingFeedbackResponse>> listFeedbackSortedByScore(
        @Parameter(description = "ID phiên", required = true)
        @PathVariable Long sessionId,
        
        @PageableDefault(size = 20, page = 0)
        Pageable pageable,
        
        @AuthenticationPrincipal User user
    ) {
        log.info("List feedback sorted by score - sessionId: {}, userId: {}", sessionId, user.getId());
        
        Page<SpeakingFeedbackResponse> feedbackPage = feedbackService.listFeedbackBySessionSortedByScore(
            sessionId, 
            user.getId(), 
            pageable
        );
        
        return ResponseEntity.ok(feedbackPage);
    }
    
    /**
     * GET /api/ai/speaking/feedback/sessions/{sessionId}/sorted-by-grammar
     * 
     * Lấy danh sách feedback sắp xếp theo điểm ngữ pháp (cao nhất trước)
     * 
     * Response: Page<SpeakingFeedbackResponse>
     */
    @GetMapping("/sessions/{sessionId}/sorted-by-grammar")
    @Operation(
        summary = "List feedback sorted by grammar score",
        description = "Lấy danh sách feedback sắp xếp theo điểm ngữ pháp (cao nhất trước)",
        tags = {"Speaking Feedback"}
    )
    @ApiResponse(responseCode = "200", description = "Feedback list retrieved")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden - user doesn't own this session")
    public ResponseEntity<Page<SpeakingFeedbackResponse>> listFeedbackSortedByGrammar(
        @Parameter(description = "ID phiên", required = true)
        @PathVariable Long sessionId,
        
        @PageableDefault(size = 20, page = 0)
        Pageable pageable,
        
        @AuthenticationPrincipal User user
    ) {
        log.info("List feedback sorted by grammar - sessionId: {}, userId: {}", sessionId, user.getId());
        
        Page<SpeakingFeedbackResponse> feedbackPage = feedbackService.listFeedbackBySessionSortedByGrammar(
            sessionId, 
            user.getId(), 
            pageable
        );
        
        return ResponseEntity.ok(feedbackPage);
    }
    
    /**
     * GET /api/ai/speaking/feedback/sessions/{sessionId}/sorted-by-vocabulary
     * 
     * Lấy danh sách feedback sắp xếp theo điểm từ vựng (cao nhất trước)
     * 
     * Response: Page<SpeakingFeedbackResponse>
     */
    @GetMapping("/sessions/{sessionId}/sorted-by-vocabulary")
    @Operation(
        summary = "List feedback sorted by vocabulary score",
        description = "Lấy danh sách feedback sắp xếp theo điểm từ vựng (cao nhất trước)",
        tags = {"Speaking Feedback"}
    )
    @ApiResponse(responseCode = "200", description = "Feedback list retrieved")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden - user doesn't own this session")
    public ResponseEntity<Page<SpeakingFeedbackResponse>> listFeedbackSortedByVocabulary(
        @Parameter(description = "ID phiên", required = true)
        @PathVariable Long sessionId,
        
        @PageableDefault(size = 20, page = 0)
        Pageable pageable,
        
        @AuthenticationPrincipal User user
    ) {
        log.info("List feedback sorted by vocabulary - sessionId: {}, userId: {}", sessionId, user.getId());
        
        Page<SpeakingFeedbackResponse> feedbackPage = feedbackService.listFeedbackBySessionSortedByVocabulary(
            sessionId, 
            user.getId(), 
            pageable
        );
        
        return ResponseEntity.ok(feedbackPage);
    }
    
    /**
     * GET /api/ai/speaking/feedback/sessions/{sessionId}/statistics
     * 
     * Lấy thống kê điểm trung bình của phiên
     * 
     * Response: Object với các trường:
     * - overallScore: Điểm trung bình tổng thể
     * - grammarScore: Điểm ngữ pháp trung bình
     * - vocabularyScore: Điểm từ vựng trung bình
     * - fluencyScore: Điểm trôi chảy trung bình
     * - contentScore: Điểm nội dung trung bình
     * - attemptCount: Số lần ghi âm
     */
    @GetMapping("/sessions/{sessionId}/statistics")
    @Operation(
        summary = "Get session average scores",
        description = "Lấy thống kê điểm trung bình của phiên (overall, grammar, vocabulary, fluency, content)",
        tags = {"Speaking Feedback"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Statistics retrieved successfully",
        content = @Content(mediaType = "application/json")
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden - user doesn't own this session")
    @ApiResponse(responseCode = "404", description = "Session not found")
    public ResponseEntity<?> getSessionStatistics(
        @Parameter(description = "ID phiên", required = true)
        @PathVariable Long sessionId,
        
        @AuthenticationPrincipal User user
    ) {
        log.info("Get session statistics - sessionId: {}, userId: {}", sessionId, user.getId());
        
        try {
            Double overallScore = feedbackService.calculateAverageOverallScore(sessionId);
            Double grammarScore = feedbackService.calculateAverageGrammarScore(sessionId);
            Double vocabularyScore = feedbackService.calculateAverageVocabularyScore(sessionId);
            Double fluencyScore = feedbackService.calculateAverageFluencyScore(sessionId);
            Double contentScore = feedbackService.calculateAverageContentScore(sessionId);
            long attemptCount = feedbackService.countFeedbackBySession(sessionId);
            
            var statistics = java.util.Map.of(
                "overallScore", overallScore,
                "grammarScore", grammarScore,
                "vocabularyScore", vocabularyScore,
                "fluencyScore", fluencyScore,
                "contentScore", contentScore,
                "attemptCount", attemptCount
            );
            
            log.info("Session statistics retrieved - sessionId: {}, overall: {}", sessionId, overallScore);
            return ResponseEntity.ok(statistics);
            
        } catch (Exception e) {
            log.error("Error getting session statistics - sessionId: {}, error: {}", 
                      sessionId, e.getMessage(), e);
            throw e;
        }
    }
}
