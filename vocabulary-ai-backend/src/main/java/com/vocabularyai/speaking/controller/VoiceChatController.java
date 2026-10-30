package com.vocabularyai.speaking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabularyai.speaking.dto.voicechat.*;
import com.vocabularyai.speaking.service.voicechat.VoiceChatService;
import com.vocabularyai.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/ai/speaking/voice-chat")
@RequiredArgsConstructor
@Tag(name = "Voice Chat", description = "Endpoints cho chức năng Trò chuyện giọng nói với AI trong Luyện nói")
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("isAuthenticated()")
public class VoiceChatController {

    private final VoiceChatService voiceChatService;
    private final ObjectMapper objectMapper;

    /**
     * Endpoint 1: Chuyển giọng user thành văn bản (Transcribe-only)
     */
    @PostMapping(value = "/transcribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Transcribe audio", description = "Chuyển audio lượt nói của user thành text qua Whisper STT")
    public ResponseEntity<VoiceChatTranscribeResponse> transcribe(
            @RequestParam(value = "audioFile", required = false) MultipartFile audioFile) {
        log.info("Received voice-chat transcribe request");
        VoiceChatTranscribeResponse response = voiceChatService.transcribe(audioFile);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint 2: AI hội thoại trả lời (Reply)
     */
    @PostMapping("/reply")
    @Operation(summary = "AI Reply", description = "Sinh câu trả lời từ AI theo ngữ cảnh hội thoại hoặc câu mở đầu")
    public ResponseEntity<VoiceChatReplyResponse> reply(
            @RequestBody VoiceChatReplyRequest request,
            @AuthenticationPrincipal User user) {
        log.info("Received voice-chat reply request, opening: {}, user: {}", request.isOpening(), user.getId());
        VoiceChatReplyResponse response = voiceChatService.reply(request, user);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint 3: Chấm điểm cuối phiên + trích từ mới
     */
    @PostMapping(value = "/score", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Score Voice Chat", description = "Chấm điểm 5 tiêu chí và trích xuất từ mới sau khi kết thúc phiên")
    public ResponseEntity<VoiceChatScoreResponse> score(
            @RequestPart(value = "audioFile", required = false) MultipartFile audioFile,
            @RequestParam(value = "payload", required = false) String payload,
            @AuthenticationPrincipal User user) {
        log.info("Received voice-chat score request from user: {}", user.getId());

        VoiceChatScoreRequest request = null;
        if (payload != null && !payload.isBlank()) {
            try {
                request = objectMapper.readValue(payload, VoiceChatScoreRequest.class);
            } catch (Exception e) {
                log.warn("Failed to parse score payload JSON: {}", e.getMessage());
            }
        }
        if (request == null) {
            request = new VoiceChatScoreRequest();
        }

        VoiceChatScoreResponse response = voiceChatService.score(audioFile, request, user);
        return ResponseEntity.ok(response);
    }
}
