package com.vocabularyai.speaking.controller;

import com.vocabularyai.speaking.dto.request.CreateSpeakingSessionRequest;
import com.vocabularyai.speaking.dto.response.SpeakingSessionResponse;
import com.vocabularyai.speaking.dto.response.SpeakingSessionSummaryResponse;
import com.vocabularyai.speaking.entity.SpeakingLevel;
import com.vocabularyai.speaking.entity.SpeakingStatus;
import com.vocabularyai.speaking.service.SpeakingSessionService;
import com.vocabularyai.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * SpeakingSessionController
 * 
 * REST API endpoints cho Speaking Session management
 * 
 * Base URL: /api/ai/speaking/sessions
 * 
 * Endpoints:
 * - POST /api/ai/speaking/sessions - Tạo phiên mới
 * - GET /api/ai/speaking/sessions - Danh sách phiên của user
 * - GET /api/ai/speaking/sessions/{id} - Chi tiết phiên
 * - PUT /api/ai/speaking/sessions/{id}/complete - Hoàn thành phiên
 * - PUT /api/ai/speaking/sessions/{id}/cancel - Hủy phiên
 * - DELETE /api/ai/speaking/sessions/{id} - Xóa phiên
 * 
 * Security:
 * - Require JWT authentication (@PreAuthorize class-level)
 * - User can only manage their own sessions - Service layer responsibility
 * - All 10 endpoints restricted to authenticated users only
 * 
 * Authorization Flow:
 * 1. JWT Filter: Extract authentication
 * 2. @PreAuthorize("isAuthenticated()"): Class-level authorization
 * 3. Controller: Receives authenticated user
 * 4. Service: Verify resource ownership (userId == session.userId)
 */
@RestController
@RequestMapping("/api/ai/speaking/sessions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Speaking Sessions", description = "API cho quản lý Speaking Sessions")
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("isAuthenticated()")
public class SpeakingSessionController {
    
    private final SpeakingSessionService sessionService;
    
    /**
     * Tạo phiên luyện nói mới
     * 
     * @param request CreateSpeakingSessionRequest (topic, level, description)
     * @param user Authenticated user từ JWT
     * @return SpeakingSessionResponse của phiên vừa tạo
     */
    @PostMapping
    @Operation(summary = "Tạo phiên luyện nói mới", description = "Tạo một phiên Speaking mới cho user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Phiên được tạo thành công",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SpeakingSessionResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<SpeakingSessionResponse> createSession(
            @Valid @RequestBody CreateSpeakingSessionRequest request,
            @AuthenticationPrincipal User user) {
        
        log.info("Creating speaking session - userId: {}, topic: {}, level: {}", 
                user.getId(), request.getTopic(), request.getLevel());
        
        SpeakingSessionResponse response = sessionService.createSession(user.getId(), request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Lấy danh sách phiên của user (tất cả trạng thái)
     * 
     * @param user Authenticated user
     * @param pageable Pagination parameters (page, size, sort)
     * @return Page<SpeakingSessionResponse> danh sách phiên
     */
    @GetMapping
    @Operation(summary = "Lấy danh sách phiên", description = "Lấy tất cả phiên Speaking của user (phân trang)")
    @ApiResponse(responseCode = "200", description = "Danh sách phiên")
    public ResponseEntity<Page<SpeakingSessionResponse>> listSessions(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.debug("Getting sessions - userId: {}", user.getId());
        
        Page<SpeakingSessionResponse> sessions = sessionService.listSessions(user.getId(), pageable);
        
        return ResponseEntity.ok(sessions);
    }
    
    /**
     * Lấy danh sách phiên theo trạng thái cụ thể
     * 
     * @param status SpeakingStatus (IN_PROGRESS, COMPLETED, CANCELLED)
     * @param user Authenticated user
     * @param pageable Pagination
     * @return Page<SpeakingSessionResponse> phiên theo trạng thái
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Lấy phiên theo trạng thái", 
        description = "Lấy phiên theo trạng thái cụ thể (IN_PROGRESS, COMPLETED, CANCELLED)")
    @ApiResponse(responseCode = "200", description = "Danh sách phiên theo trạng thái")
    public ResponseEntity<Page<SpeakingSessionResponse>> listSessionsByStatus(
            @Parameter(description = "Status (IN_PROGRESS, COMPLETED, CANCELLED)")
            @PathVariable SpeakingStatus status,
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.debug("Getting sessions by status - userId: {}, status: {}", user.getId(), status);
        
        Page<SpeakingSessionResponse> sessions = sessionService.listSessionsByStatus(user.getId(), status, pageable);
        
        return ResponseEntity.ok(sessions);
    }
    
    /**
     * Lấy danh sách phiên theo mức độ (level)
     * 
     * @param level SpeakingLevel (A1-C2)
     * @param user Authenticated user
     * @param pageable Pagination
     * @return Page<SpeakingSessionResponse> phiên theo mức độ
     */
    @GetMapping("/level/{level}")
    @Operation(summary = "Lấy phiên theo mức độ", description = "Lấy phiên theo mức độ tiếng Anh (A1-C2)")
    @ApiResponse(responseCode = "200", description = "Danh sách phiên theo mức độ")
    public ResponseEntity<Page<SpeakingSessionResponse>> listSessionsByLevel(
            @Parameter(description = "Level (A1, A2, B1, B2, C1, C2)")
            @PathVariable SpeakingLevel level,
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.debug("Getting sessions by level - userId: {}, level: {}", user.getId(), level);
        
        Page<SpeakingSessionResponse> sessions = sessionService.listSessionsByLevel(user.getId(), level, pageable);
        
        return ResponseEntity.ok(sessions);
    }
    
    /**
     * Lấy chi tiết phiên luyện nói (kèm danh sách attempts + feedback)
     * 
     * @param sessionId Session ID
     * @param user Authenticated user
     * @return SpeakingSessionSummaryResponse chi tiết phiên
     */
    @GetMapping("/{sessionId}")
    @Operation(summary = "Lấy chi tiết phiên", description = "Lấy thông tin chi tiết phiên kèm attempts và feedback")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Chi tiết phiên",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SpeakingSessionSummaryResponse.class))),
        @ApiResponse(responseCode = "404", description = "Phiên không tìm thấy"),
        @ApiResponse(responseCode = "403", description = "Bạn không có quyền truy cập")
    })
    public ResponseEntity<SpeakingSessionSummaryResponse> getSessionDetail(
            @Parameter(description = "Session ID") @PathVariable Long sessionId,
            @AuthenticationPrincipal User user) {
        
        log.debug("Getting session detail - sessionId: {}, userId: {}", sessionId, user.getId());
        
        SpeakingSessionSummaryResponse session = sessionService.getSessionDetail(sessionId, user.getId());
        
        return ResponseEntity.ok(session);
    }
    
    /**
     * Lấy phiên gần nhất chưa hoàn thành (để "Tiếp tục phiên lần cuối")
     * 
     * @param user Authenticated user
     * @return SpeakingSessionResponse phiên gần nhất IN_PROGRESS, hoặc 404 nếu không có
     */
    @GetMapping("/latest/in-progress")
    @Operation(summary = "Lấy phiên gần nhất chưa hoàn thành", 
        description = "Lấy phiên luyện nói gần nhất có trạng thái IN_PROGRESS để tiếp tục")
    @ApiResponse(responseCode = "200", description = "Phiên gần nhất")
    public ResponseEntity<SpeakingSessionResponse> getLatestInProgressSession(
            @AuthenticationPrincipal User user) {
        
        log.debug("Getting latest in-progress session - userId: {}", user.getId());
        
        SpeakingSessionResponse session = sessionService.getLatestInProgressSession(user.getId());
        
        if (session == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(session);
    }
    
    /**
     * Hoàn thành phiên luyện nói
     * 
     * @param sessionId Session ID
     * @param user Authenticated user
     * @return SpeakingSessionResponse phiên đã cập nhật (status = COMPLETED)
     */
    @PutMapping("/{sessionId}/complete")
    @Operation(summary = "Hoàn thành phiên", description = "Đánh dấu phiên luyện nói là đã hoàn thành")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Phiên được hoàn thành",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SpeakingSessionResponse.class))),
        @ApiResponse(responseCode = "404", description = "Phiên không tìm thấy"),
        @ApiResponse(responseCode = "403", description = "Bạn không có quyền truy cập")
    })
    public ResponseEntity<SpeakingSessionResponse> completeSession(
            @Parameter(description = "Session ID") @PathVariable Long sessionId,
            @AuthenticationPrincipal User user) {
        
        log.info("Completing session - sessionId: {}, userId: {}", sessionId, user.getId());
        
        SpeakingSessionResponse response = sessionService.completeSession(sessionId, user.getId());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Hủy phiên luyện nói
     * 
     * @param sessionId Session ID
     * @param user Authenticated user
     * @return SpeakingSessionResponse phiên đã cập nhật (status = CANCELLED)
     */
    @PutMapping("/{sessionId}/cancel")
    @Operation(summary = "Hủy phiên", description = "Hủy phiên luyện nói")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Phiên được hủy",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SpeakingSessionResponse.class))),
        @ApiResponse(responseCode = "404", description = "Phiên không tìm thấy"),
        @ApiResponse(responseCode = "403", description = "Bạn không có quyền truy cập")
    })
    public ResponseEntity<SpeakingSessionResponse> cancelSession(
            @Parameter(description = "Session ID") @PathVariable Long sessionId,
            @AuthenticationPrincipal User user) {
        
        log.info("Cancelling session - sessionId: {}, userId: {}", sessionId, user.getId());
        
        SpeakingSessionResponse response = sessionService.cancelSession(sessionId, user.getId());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Xóa phiên (xóa toàn bộ attempts và feedback)
     * 
     * @param sessionId Session ID
     * @param user Authenticated user
     * @return 204 No Content nếu xóa thành công
     */
    @DeleteMapping("/{sessionId}")
    @Operation(summary = "Xóa phiên", description = "Xóa phiên luyện nói (xóa tất cả attempts và feedback)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Phiên được xóa thành công"),
        @ApiResponse(responseCode = "404", description = "Phiên không tìm thấy"),
        @ApiResponse(responseCode = "403", description = "Bạn không có quyền truy cập")
    })
    public ResponseEntity<Void> deleteSession(
            @Parameter(description = "Session ID") @PathVariable Long sessionId,
            @AuthenticationPrincipal User user) {
        
        log.info("Deleting session - sessionId: {}, userId: {}", sessionId, user.getId());
        
        sessionService.deleteSession(sessionId, user.getId());
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Lấy danh sách phiên hoàn thành sắp xếp theo điểm cao nhất
     * 
     * @param user Authenticated user
     * @param pageable Pagination
     * @return Page<SpeakingSessionResponse> phiên sắp xếp theo score
     */
    @GetMapping("/completed/sorted-by-score")
    @Operation(summary = "Lấy phiên hoàn thành (sắp xếp theo điểm)", 
        description = "Lấy danh sách phiên hoàn thành sắp xếp theo điểm cao nhất")
    @ApiResponse(responseCode = "200", description = "Danh sách phiên")
    public ResponseEntity<Page<SpeakingSessionResponse>> getCompletedSessionsSortedByScore(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20, sort = "overallScore", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.debug("Getting completed sessions sorted by score - userId: {}", user.getId());
        
        Page<SpeakingSessionResponse> sessions = sessionService.listCompletedSessionsSortedByScore(user.getId(), pageable);
        
        return ResponseEntity.ok(sessions);
    }
}
