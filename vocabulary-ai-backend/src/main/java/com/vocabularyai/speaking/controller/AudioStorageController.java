package com.vocabularyai.speaking.controller;

import com.vocabularyai.common.exception.ForbiddenException;
import com.vocabularyai.common.exception.ResourceNotFoundException;
import com.vocabularyai.speaking.entity.SpeakingAttempt;
import com.vocabularyai.speaking.entity.SpeakingSession;
import com.vocabularyai.speaking.repository.SpeakingAttemptRepository;
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
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

/**
 * AudioStorageController
 * 
 * REST API Controller để truy cập audio files từ speaking attempts
 * 
 * Endpoints:
 * - GET /api/ai/speaking/audio/{sessionId}/attempt-{attemptNumber} - Download audio
 * - GET /api/ai/speaking/audio/{sessionId}/attempt-{attemptNumber}/stream - Stream audio (playback)
 * 
 * Security:
 * - Require JWT authentication
 * - Verify user ownership of session/attempt
 */
@Slf4j
@RestController
@RequestMapping({"/api/ai/speaking/audio", "/ai/speaking/audio"})
@RequiredArgsConstructor
@Tag(name = "Audio Storage", description = "API để truy cập audio files từ speaking attempts")
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("isAuthenticated()")
public class AudioStorageController {
    
    private final AudioStorageService audioStorageService;
    private final SpeakingSessionRepository sessionRepository;
    private final SpeakingAttemptRepository attemptRepository;

    private record AudioFileResource(byte[] bytes, MediaType mediaType, String extension) {}

    /**
     * Helper resolve và xác thực quyền sở hữu file audio
     */
    private AudioFileResource resolveAudioFile(Long sessionId, int attemptNumber, Long userId) {
        // 1. Phân biệt session không tồn tại (404) và session của người khác (403)
        if (!sessionRepository.existsById(sessionId)) {
            throw new ResourceNotFoundException("Phiên luyện nói không tồn tại: " + sessionId);
        }

        SpeakingSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ForbiddenException("Bạn không có quyền truy cập bản ghi của phiên này"));

        // 2. Tìm attempt trong phiên
        SpeakingAttempt attempt = attemptRepository.findBySessionIdAndAttemptNumber(sessionId, attemptNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lần thử #" + attemptNumber + " trong phiên " + sessionId));

        // 3. Kiểm tra file có còn tồn tại trên storage không
        String audioUrl = attempt.getAudioUrl();
        if (audioUrl == null || !audioStorageService.audioExists(audioUrl)) {
            log.warn("Audio file missing for sessionId: {}, attemptNumber: {}, audioUrl: {}", 
                     sessionId, attemptNumber, audioUrl);
            throw new ResourceNotFoundException("Bản ghi âm không còn tồn tại trên hệ thống");
        }

        // 4. Đọc dữ liệu audio
        byte[] audioBytes = audioStorageService.downloadAudio(audioUrl);
        String extension = getFileExtension(audioUrl);
        MediaType mediaType = resolveMediaType(extension);

        return new AudioFileResource(audioBytes, mediaType, extension);
    }

    private String getFileExtension(String path) {
        if (path == null || !path.contains(".")) {
            return "webm";
        }
        int dotIndex = path.lastIndexOf('.');
        return path.substring(dotIndex + 1).toLowerCase();
    }

    private MediaType resolveMediaType(String ext) {
        if (ext == null) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        return switch (ext.toLowerCase()) {
            case "webm" -> MediaType.parseMediaType("audio/webm");
            case "wav" -> MediaType.parseMediaType("audio/wav");
            case "mp3" -> MediaType.parseMediaType("audio/mpeg");
            case "m4a" -> MediaType.parseMediaType("audio/mp4");
            case "ogg" -> MediaType.parseMediaType("audio/ogg");
            case "flac" -> MediaType.parseMediaType("audio/flac");
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }
    
    /**
     * GET /api/ai/speaking/audio/{sessionId}/attempt-{attemptNumber}
     * 
     * Download audio file từ attempt
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
        content = @Content(mediaType = "audio/webm")
    )
    @ApiResponse(responseCode = "404", description = "Session, attempt or audio file not found")
    @ApiResponse(responseCode = "403", description = "Not session owner")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<byte[]> downloadAudio(
        @Parameter(description = "ID phiên speaking", required = true)
        @PathVariable Long sessionId,
        
        @Parameter(description = "Số thứ tự attempt", required = true)
        @PathVariable int attemptNumber,
        
        @AuthenticationPrincipal User user
    ) {
        log.info("Download audio - userId: {}, sessionId: {}, attemptNumber: {}", 
                 user.getId(), sessionId, attemptNumber);
        
        AudioFileResource resource = resolveAudioFile(sessionId, attemptNumber, user.getId());
        String filename = String.format("attempt-%d.%s", attemptNumber, resource.extension());

        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(filename)
                .build();

        return ResponseEntity.ok()
                .contentType(resource.mediaType())
                .contentLength(resource.bytes().length)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(resource.bytes());
    }
    
    /**
     * GET /api/ai/speaking/audio/{sessionId}/attempt-{attemptNumber}/stream
     * 
     * Stream audio file (inline playback)
     */
    @GetMapping("/{sessionId}/attempt-{attemptNumber}/stream")
    @Operation(
        summary = "Stream audio file for playback",
        description = "Phát lại file audio từ attempt (inline playback)",
        tags = {"Audio Storage"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Audio file streamed successfully",
        content = @Content(mediaType = "audio/webm")
    )
    @ApiResponse(responseCode = "404", description = "Session, attempt or audio file not found")
    @ApiResponse(responseCode = "403", description = "Not session owner")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<byte[]> streamAudio(
        @Parameter(description = "ID phiên speaking", required = true)
        @PathVariable Long sessionId,
        
        @Parameter(description = "Số thứ tự attempt", required = true)
        @PathVariable int attemptNumber,
        
        @Parameter(description = "Range header (optional)", required = false)
        @RequestHeader(value = HttpHeaders.RANGE, required = false) String range,
        
        @AuthenticationPrincipal User user
    ) {
        log.info("Stream audio - userId: {}, sessionId: {}, attemptNumber: {}, range: {}", 
                 user.getId(), sessionId, attemptNumber, range);
        
        AudioFileResource resource = resolveAudioFile(sessionId, attemptNumber, user.getId());
        String filename = String.format("attempt-%d.%s", attemptNumber, resource.extension());

        ContentDisposition contentDisposition = ContentDisposition.inline()
                .filename(filename)
                .build();

        return ResponseEntity.ok()
                .contentType(resource.mediaType())
                .contentLength(resource.bytes().length)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .header(HttpHeaders.ACCEPT_RANGES, "none")
                .body(resource.bytes());
    }
}
