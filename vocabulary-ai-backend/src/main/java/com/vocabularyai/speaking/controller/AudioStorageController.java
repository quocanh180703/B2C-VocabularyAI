package com.vocabularyai.speaking.controller;

import com.vocabularyai.speaking.repository.SpeakingSessionRepository;
import com.vocabularyai.speaking.service.storage.AudioStorageService;
import com.vocabularyai.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

/**
 * AudioStorageController
 * 
 * REST API Controller để truy cập audio files
 * 
 * Endpoints:
 * - GET /api/ai/speaking/audio/{sessionId}/attempt-{attemptNumber} - Download audio
 * - GET /api/ai/speaking/audio/{sessionId}/attempt-{attemptNumber}/stream - Stream audio
 * 
 * Security:
 * - Require JWT authentication
 * - Verify user ownership of session/attempt
 */
@Slf4j
@RestController
@RequestMapping("/api/ai/speaking/audio")
@RequiredArgsConstructor
@Tag(name = "Audio Storage", description = "API để truy cập audio files từ speaking attempts")
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("isAuthenticated()")
public class AudioStorageController {
    
    private final AudioStorageService audioStorageService;
    private final SpeakingSessionRepository sessionRepository;
    
    /**
     * GET /api/ai/speaking/audio/{sessionId}/attempt-{attemptNumber}
     * 
     * Download audio file từ attempt
     * 
     * Path Parameters:
     * - sessionId: Long - ID của session
     * - attemptNumber: int - Số thứ tự attempt (1, 2, 3, ...)
     * 
     * Response: 200 OK (binary audio file)
     * - Content-Type: audio/wav (hoặc audio/mpeg, etc.)
     * - Content-Length: File size
     * - Content-Disposition: attachment; filename="...wav"
     * 
     * Error:
     * - 404: Audio file not found
     * - 403: Not session owner
     * - 410: Audio file deleted
     */
    @GetMapping("/{sessionId}/attempt-{attemptNumber}")
    @Operation(
        summary = "Download audio file from attempt",
        description = "Tải xuống file audio từ một attempt",
        tags = {"Audio Storage"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Audio file downloaded successfully",
        content = @Content(mediaType = "audio/wav")
    )
    @ApiResponse(responseCode = "404", description = "Audio file not found")
    @ApiResponse(responseCode = "403", description = "Not session owner")
    @ApiResponse(responseCode = "410", description = "Audio file deleted")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<?> downloadAudio(
        @Parameter(description = "ID phiên speaking", required = true)
        @PathVariable Long sessionId,
        
        @Parameter(description = "Số thứ tự attempt", required = true)
        @PathVariable int attemptNumber,
        
        @AuthenticationPrincipal User user
    ) {
        log.info("Download audio - userId: {}, sessionId: {}, attemptNumber: {}", 
                 user.getId(), sessionId, attemptNumber);
        
        try {
            // TODO: Verify session ownership
            // TODO: Get audio file from storage service
            // TODO: Return file with proper headers
            
            // Placeholder response
            throw new IllegalArgumentException("Audio download not yet implemented");
            
        } catch (Exception e) {
            log.error("Error downloading audio - userId: {}, sessionId: {}, error: {}", 
                      user.getId(), sessionId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * GET /api/ai/speaking/audio/{sessionId}/attempt-{attemptNumber}/stream
     * 
     * Stream audio file (supports range requests)
     * Dùng cho playback với seeking capability
     * 
     * Path Parameters:
     * - sessionId: Long - ID của session
     * - attemptNumber: int - Số thứ tự attempt
     * 
     * Query Parameters:
     * - Range: bytes={start}-{end} (HTTP Range header)
     * 
     * Response: 206 Partial Content (hoặc 200 OK nếu không có Range)
     * - Accept-Ranges: bytes
     * - Content-Range: bytes {start}-{end}/{total}
     * - Content-Length: {size}
     * 
     * Error:
     * - 404: Audio file not found
     * - 403: Not session owner
     * - 416: Range not satisfiable
     */
    @GetMapping("/{sessionId}/attempt-{attemptNumber}/stream")
    @Operation(
        summary = "Stream audio file with range support",
        description = "Stream audio file với hỗ trợ Range requests cho seeking/playback",
        tags = {"Audio Storage"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Audio file streamed successfully",
        content = @Content(mediaType = "audio/wav")
    )
    @ApiResponse(
        responseCode = "206",
        description = "Partial content with range support",
        content = @Content(mediaType = "audio/wav")
    )
    @ApiResponse(responseCode = "404", description = "Audio file not found")
    @ApiResponse(responseCode = "403", description = "Not session owner")
    @ApiResponse(responseCode = "416", description = "Range not satisfiable")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<?> streamAudio(
        @Parameter(description = "ID phiên speaking", required = true)
        @PathVariable Long sessionId,
        
        @Parameter(description = "Số thứ tự attempt", required = true)
        @PathVariable int attemptNumber,
        
        @Parameter(description = "Range header for partial content", required = false)
        @RequestHeader(value = HttpHeaders.RANGE, required = false) String range,
        
        @AuthenticationPrincipal User user
    ) {
        log.info("Stream audio - userId: {}, sessionId: {}, attemptNumber: {}, range: {}", 
                 user.getId(), sessionId, attemptNumber, range);
        
        try {
            // TODO: Verify session ownership
            // TODO: Get audio file from storage service
            // TODO: Parse Range header if present
            // TODO: Return partial content with proper headers
            
            // Placeholder response
            throw new IllegalArgumentException("Audio streaming not yet implemented");
            
        } catch (Exception e) {
            log.error("Error streaming audio - userId: {}, sessionId: {}, error: {}", 
                      user.getId(), sessionId, e.getMessage(), e);
            throw e;
        }
    }
}
