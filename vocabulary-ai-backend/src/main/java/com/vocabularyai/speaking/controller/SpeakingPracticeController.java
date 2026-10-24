package com.vocabularyai.speaking.controller;

import com.vocabularyai.common.validator.ValidAudioFile;
import com.vocabularyai.speaking.dto.response.SpeakingPracticeResponse;
import com.vocabularyai.speaking.service.SpeakingPracticeService;
import com.vocabularyai.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * SpeakingPracticeController
 * 
 * REST API Controller cho Speaking Practice
 * 
 * Chịu trách nhiệm:
 * - Tiếp nhận request upload audio từ client
 * - Validate request
 * - Gọi SpeakingPracticeService để xử lý toàn bộ flow
 * - Trả về response với scores, feedback, pronunciation analysis
 * 
 * Endpoints:
 * - POST /api/ai/speaking/attempts - Submit speaking attempt (upload audio)
 * - GET /api/ai/speaking/attempts/{sessionId} - Get practice session info
 * 
 * Flow:
 * 1. Client gửi audio file + sessionId
 * 2. Controller validate ownership (user_id từ JWT)
 * 3. SpeakingPracticeService xử lý: STT → AI Analysis → Save Feedback → Update Progress
 * 4. Trả về SpeakingPracticeResponse với full details
 * 
 * Security:
 * - Require JWT authentication (@PreAuthorize class-level)
 * - Verify user ownership (can only submit attempts for their own sessions) - Service layer
 * - Rate limiting via RateLimitService
 * - All 2 endpoints restricted to authenticated users only
 * 
 * Authorization Flow:
 * 1. JWT Filter: Extract authentication
 * 2. @PreAuthorize("isAuthenticated()"): Class-level authorization
 * 3. Controller: Receives authenticated user
 * 4. Service: Verify resource ownership (userId == session.userId)
 */
@Slf4j
@RestController
@RequestMapping("/api/ai/speaking/attempts")
@RequiredArgsConstructor
@Tag(name = "Speaking Practice", description = "API cho AI Speaking Practice - upload audio, analyze, get feedback")
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("isAuthenticated()")
public class SpeakingPracticeController {
    
    private final SpeakingPracticeService speakingPracticeService;
    private final com.vocabularyai.payment.service.FeatureUsageService featureUsageService;
    
    /**
     * POST /api/ai/speaking/attempts
     * 
     * Gửi bài tập speaking: upload audio và xử lý
     * 
     * Request:
     * - sessionId (form param): ID phiên luyện tập
     * - audioFile (multipart file): Audio file (MP3, WAV, WebM, OGG) max 10MB
     * 
     * Response: SpeakingPracticeResponse với:
     * - Session info
     * - Attempt info (audioUrl, transcript)
     * - AI scores (grammar, vocabulary, fluency, content)
     * - Detailed feedback (corrections, suggestions, strengths, weaknesses)
     * - Pronunciation analysis (nếu provider hỗ trợ)
     * - Session statistics (average scores, improvement)
     * 
     * Error cases:
     * - 400 Bad Request: Audio file không hợp lệ, sessionId không tồn tại
     * - 401 Unauthorized: JWT token không hợp lệ
     * - 403 Forbidden: User không sở hữu session này
     * - 429 Too Many Requests: Vượt quá rate limit
     * - 500 Internal Server Error: AI service error, STT error
     * 
     * Success response:
     * - 201 Created: Attempt được lưu, feedback được tạo
     * - Return SpeakingPracticeResponse với full details
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Submit speaking attempt - upload audio and get analysis",
        description = "Upload audio file cho một phiên luyện tập speaking. " +
                     "System sẽ transcribe audio, analyze (grammar/vocabulary/fluency/content), " +
                     "check pronunciation (nếu provider hỗ trợ), và trả về feedback chi tiết.",
        tags = {"Speaking Practice"}
    )
    @ApiResponse(
        responseCode = "201",
        description = "Attempt submitted successfully, analysis completed",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = SpeakingPracticeResponse.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = "Invalid audio file or session not found"
    )
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized - invalid JWT token"
    )
    @ApiResponse(
        responseCode = "403",
        description = "Forbidden - user does not own this session"
    )
    @ApiResponse(
        responseCode = "429",
        description = "Too many requests - rate limit exceeded"
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error - AI service or STT provider error"
    )
    public ResponseEntity<SpeakingPracticeResponse> submitSpeakingAttempt(
        @Parameter(description = "ID phiên luyện tập", required = true)
        @RequestParam(name = "sessionId") Long sessionId,
        
        @Parameter(description = "Audio file (MP3, WAV, WebM, OGG), max 10MB", required = true)
        @RequestParam(name = "audioFile") 
        @Valid @ValidAudioFile(
            maxSize = 52428800, // 50MB
            allowedMimeTypes = {"audio/wav", "audio/mpeg", "audio/mp4", "audio/webm", "audio/ogg", "audio/flac"},
            allowedExtensions = {"wav", "mp3", "m4a", "webm", "ogg", "flac"}
        )
        MultipartFile audioFile,
        
        @AuthenticationPrincipal User user
    ) {
        log.info("Submit speaking attempt - sessionId: {}, userId: {}, audioFile: {}", 
                 sessionId, user.getId(), audioFile.getOriginalFilename());
        
        Long userId = user.getId();
        featureUsageService.checkAndConsumeQuota(userId, com.vocabularyai.payment.entity.FeatureType.SPEAKING);
        try {
            SpeakingPracticeResponse response = speakingPracticeService.submitSpeakingPractice(
                sessionId, 
                userId, 
                audioFile
            );
            
            log.info("Speaking attempt submitted successfully - sessionId: {}, attemptId: {}, score: {}", 
                     sessionId, response.getAttempt().getId(), response.getScores().getOverallScore());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            featureUsageService.rollbackQuota(userId, com.vocabularyai.payment.entity.FeatureType.SPEAKING);
            log.warn("Validation error - sessionId: {}, userId: {}, message: {}", 
                     sessionId, user.getId(), e.getMessage());
            throw e;
            
        } catch (Exception e) {
            featureUsageService.rollbackQuota(userId, com.vocabularyai.payment.entity.FeatureType.SPEAKING);
            log.error("Error submitting speaking attempt - sessionId: {}, userId: {}, error: {}", 
                      sessionId, user.getId(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * GET /api/ai/speaking/attempts/{sessionId}
     * 
     * Lấy thông tin practice session hiện tại
     * 
     * Response: SpeakingPracticeResponse với:
     * - Session info (topic, level, status)
     * - Attempt list (history của user)
     * - Feedback của attempt gần nhất
     * - Session statistics (average scores, improvement, best score)
     * 
     * Error cases:
     * - 401 Unauthorized: JWT token không hợp lệ
     * - 403 Forbidden: User không sở hữu session này
     * - 404 Not Found: Session không tồn tại
     * - 500 Internal Server Error
     * 
     * Success response:
     * - 200 OK: Return SpeakingPracticeResponse
     */
    @GetMapping("/{sessionId}")
    @Operation(
        summary = "Get practice session info",
        description = "Lấy thông tin chi tiết phiên luyện tập (attempts, feedback, statistics)",
        tags = {"Speaking Practice"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Session info retrieved successfully",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = SpeakingPracticeResponse.class))
    )
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized - invalid JWT token"
    )
    @ApiResponse(
        responseCode = "403",
        description = "Forbidden - user does not own this session"
    )
    @ApiResponse(
        responseCode = "404",
        description = "Session not found"
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error"
    )
    public ResponseEntity<SpeakingPracticeResponse> getPracticeSession(
        @Parameter(description = "ID phiên luyện tập", required = true)
        @PathVariable(name = "sessionId") Long sessionId,
        
        @AuthenticationPrincipal User user
    ) {
        log.info("Get practice session - sessionId: {}, userId: {}", sessionId, user.getId());
        
        try {
            Long userId = user.getId();
            
            SpeakingPracticeResponse response = speakingPracticeService.getPracticeSession(
                sessionId, 
                userId
            );
            
            log.info("Practice session retrieved - sessionId: {}, attempts: {}", 
                     sessionId, response.getStatistics().getTotalAttempts());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Validation error - sessionId: {}, userId: {}, message: {}", 
                     sessionId, user.getId(), e.getMessage());
            throw e;
            
        } catch (Exception e) {
            log.error("Error getting practice session - sessionId: {}, userId: {}, error: {}", 
                      sessionId, user.getId(), e.getMessage(), e);
            throw e;
        }
    }
}
