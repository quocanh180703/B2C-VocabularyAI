package com.vocabularyai.speaking.controller;

import com.vocabularyai.speaking.dto.request.TextToSpeechRequest;
import com.vocabularyai.speaking.dto.response.TextToSpeechResponse;
import com.vocabularyai.speaking.repository.SpeakingSessionRepository;
import com.vocabularyai.speaking.service.tts.TextToSpeechService;
import com.vocabularyai.speaking.service.tts.TextToSpeechResult;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * TextToSpeechController
 * 
 * REST API Controller cho Text-to-Speech (TTS)
 * Tạo audio pronunciation reference từ text
 * 
 * Endpoints:
 * - GET  /api/ai/speaking/tts/stream - Phát trực tiếp MP3 stream cho tag audio
 * - POST /api/ai/speaking/tts/word - Generate TTS cho 1 từ
 * - POST /api/ai/speaking/tts/phrase - Generate TTS cho 1 cụm từ
 * - GET  /api/ai/speaking/tts/sessions/{sessionId}/reference - Generate TTS cho topic
 */
@Slf4j
@RestController
@RequestMapping({"/api/ai/speaking/tts", "/ai/speaking/tts"})
@RequiredArgsConstructor
@Tag(name = "Text-to-Speech", description = "API tạo audio pronunciation reference từ text")
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("isAuthenticated()")
public class TextToSpeechController {
    
    private final TextToSpeechService textToSpeechService;
    private final SpeakingSessionRepository sessionRepository;

    /**
     * GET /api/ai/speaking/tts/stream
     * 
     * Phát trực tiếp file âm thanh MP3 từ text (hỗ trợ tag audio của browser không cần header auth)
     */
    @GetMapping(value = "/stream", produces = "audio/mpeg")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Stream audio MP3 directly", description = "Phát trực tiếp file âm thanh MP3 cho từ hoặc câu")
    public ResponseEntity<byte[]> streamTts(
        @Parameter(description = "Văn bản hoặc từ cần phát âm", required = true)
        @RequestParam String text,
        @Parameter(description = "Giọng đọc (mặc định: alloy)")
        @RequestParam(required = false, defaultValue = "alloy") String voice
    ) {
        if (text == null || text.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            byte[] audioBytes;
            if (textToSpeechService instanceof com.vocabularyai.speaking.service.tts.XahTextToSpeechService xahService) {
                audioBytes = xahService.synthesizeRawAudio(text, voice);
            } else {
                TextToSpeechResult result = textToSpeechService.synthesize(text, "en-US", voice);
                if (result.getAudioUrl() != null && result.getAudioUrl().startsWith("data:audio/mp3;base64,")) {
                    audioBytes = java.util.Base64.getDecoder().decode(result.getAudioUrl().substring("data:audio/mp3;base64,".length()));
                } else {
                    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
                }
            }

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"speech.mp3\"")
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .body(audioBytes);
        } catch (Exception e) {
            log.error("Failed to stream TTS audio for text: {}", text, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /api/ai/speaking/tts/synthesize
     * 
     * Tổng hợp âm thanh từ đoạn text bất kỳ
     */
    @PostMapping("/synthesize")
    @Operation(summary = "Synthesize TTS audio for general text")
    public ResponseEntity<TextToSpeechResponse> synthesizeText(
        @RequestBody TextToSpeechRequest request
    ) {
        String input = request.getEffectiveText();
        if (input.isEmpty()) {
            throw new IllegalArgumentException("Text cannot be empty");
        }

        TextToSpeechResult result = textToSpeechService.synthesize(
            input,
            request.getLanguage() != null ? request.getLanguage() : "en-US"
        );

        TextToSpeechResponse response = TextToSpeechResponse.builder()
            .phrase(input)
            .word(request.getWord())
            .audioUrl(result.getAudioUrl())
            .audioFormat(result.getAudioFormat())
            .durationSeconds(result.getDurationSeconds())
            .voice(result.getVoice())
            .language(result.getLanguageCode())
            .provider(result.getProvider())
            .processingTimeMs(result.getProcessingTimeMs())
            .success(result.getSuccess())
            .createdAt(java.time.LocalDateTime.now())
            .build();

        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/ai/speaking/tts/word
     * 
     * Tạo audio pronunciation cho 1 từ đơn lẻ
     * 
     * Request:
     * {
     *   "word": "Vietnam",
     *   "language": "en-US"
     * }
     * 
     * Response: TextToSpeechResponse
     * {
     *   "word": "Vietnam",
     *   "audioUrl": "http://localhost:8080/api/ai/speaking/audio/tts/vietnam-en-US.mp3",
     *   "audioFormat": "mp3",
     *   "durationSeconds": 1.2,
     *   "voice": "en-US-Neural2-A",
     *   "language": "en-US",
     *   "provider": "gemini",
     *   "processingTimeMs": 450,
     *   "success": true,
     *   "createdAt": "2026-08-28T10:30:00Z"
     * }
     */
    @PostMapping("/word")
    @Operation(
        summary = "Generate TTS audio for a single word",
        description = "Tạo audio pronunciation reference cho 1 từ tiếng Anh",
        tags = {"Text-to-Speech"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "TTS audio generated successfully",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = TextToSpeechResponse.class))
    )
    @ApiResponse(responseCode = "400", description = "Invalid word or language")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
    @ApiResponse(responseCode = "503", description = "TTS service unavailable")
    public ResponseEntity<TextToSpeechResponse> generateTtsForWord(
        @Parameter(description = "Request body chứa word, language", required = true)
        @RequestBody TextToSpeechRequest request,
        
        @AuthenticationPrincipal User user
    ) {
        log.info("Generate TTS for word - userId: {}, word: {}, language: {}", 
                 user.getId(), request.getWord(), request.getLanguage());
        
        try {
            // Validate input
            if (request.getWord() == null || request.getWord().trim().isEmpty()) {
                throw new IllegalArgumentException("Word cannot be empty");
            }
            if (request.getWord().length() > 100) {
                throw new IllegalArgumentException("Word must be less than 100 characters");
            }
            
            // Generate TTS
            TextToSpeechResult result = textToSpeechService.synthesize(
                request.getWord(),
                request.getLanguage() != null ? request.getLanguage() : "en-US"
            );
            
            TextToSpeechResponse response = TextToSpeechResponse.builder()
                .word(request.getWord())
                .audioUrl(result.getAudioUrl())
                .audioFormat(result.getAudioFormat())
                .durationSeconds(result.getDurationSeconds())
                .voice(result.getVoice())
                .language(result.getLanguageCode())
                .provider(result.getProvider())
                .processingTimeMs(result.getProcessingTimeMs())
                .success(result.getSuccess())
                .createdAt(java.time.LocalDateTime.now())
                .build();
            
            log.info("TTS for word generated - userId: {}, word: {}, duration: {}s", 
                     user.getId(), request.getWord(), result.getDurationSeconds());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid TTS request - userId: {}, error: {}", user.getId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error generating TTS for word - userId: {}, error: {}", 
                      user.getId(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * POST /api/ai/speaking/tts/phrase
     * 
     * Tạo audio pronunciation cho 1 cụm từ
     * 
     * Request:
     * {
     *   "phrase": "My name is John",
     *   "language": "en-US"
     * }
     * 
     * Response: TextToSpeechResponse
     * {
     *   "phrase": "My name is John",
     *   "audioUrl": "http://localhost:8080/api/ai/speaking/audio/tts/my-name-is-john-en-US.mp3",
     *   "audioFormat": "mp3",
     *   "durationSeconds": 3.5,
     *   "voice": "en-US-Neural2-A",
     *   "language": "en-US",
     *   "provider": "gemini",
     *   "processingTimeMs": 850,
     *   "success": true,
     *   "createdAt": "2026-08-28T10:30:15Z"
     * }
     */
    @PostMapping("/phrase")
    @Operation(
        summary = "Generate TTS audio for a phrase",
        description = "Tạo audio pronunciation reference cho 1 cụm từ tiếng Anh",
        tags = {"Text-to-Speech"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "TTS audio generated successfully",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = TextToSpeechResponse.class))
    )
    @ApiResponse(responseCode = "400", description = "Invalid phrase (too long, empty, etc.)")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
    @ApiResponse(responseCode = "503", description = "TTS service unavailable")
    public ResponseEntity<TextToSpeechResponse> generateTtsForPhrase(
        @Parameter(description = "Request body chứa phrase, language", required = true)
        @RequestBody TextToSpeechRequest request,
        
        @AuthenticationPrincipal User user
    ) {
        log.info("Generate TTS for phrase - userId: {}, phrase: {}, language: {}", 
                 user.getId(), request.getPhrase(), request.getLanguage());
        
        try {
            // Validate input
            if (request.getPhrase() == null || request.getPhrase().trim().isEmpty()) {
                throw new IllegalArgumentException("Phrase cannot be empty");
            }
            if (request.getPhrase().length() > 500) {
                throw new IllegalArgumentException("Phrase must be less than 500 characters");
            }
            
            // Generate TTS
            TextToSpeechResult result = textToSpeechService.synthesize(
                request.getPhrase(),
                request.getLanguage() != null ? request.getLanguage() : "en-US"
            );
            
            TextToSpeechResponse response = TextToSpeechResponse.builder()
                .phrase(request.getPhrase())
                .audioUrl(result.getAudioUrl())
                .audioFormat(result.getAudioFormat())
                .durationSeconds(result.getDurationSeconds())
                .voice(result.getVoice())
                .language(result.getLanguageCode())
                .provider(result.getProvider())
                .processingTimeMs(result.getProcessingTimeMs())
                .success(result.getSuccess())
                .createdAt(java.time.LocalDateTime.now())
                .build();
            
            log.info("TTS for phrase generated - userId: {}, phrase: {}, duration: {}s", 
                     user.getId(), request.getPhrase(), result.getDurationSeconds());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid TTS request - userId: {}, error: {}", user.getId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error generating TTS for phrase - userId: {}, error: {}", 
                      user.getId(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * GET /api/ai/speaking/tts/sessions/{sessionId}/reference
     * 
     * Tạo audio pronunciation reference cho topic của session
     * 
     * Response: TextToSpeechResponse
     * {
     *   "sessionId": 1,
     *   "topic": "Giới thiệu bản thân",
     *   "audioUrl": "http://localhost:8080/api/ai/speaking/audio/tts/session-1-topic.mp3",
     *   "audioFormat": "mp3",
     *   "durationSeconds": 2.8,
     *   "voice": "en-US-Neural2-A",
     *   "language": "en-US",
     *   "provider": "gemini",
     *   "processingTimeMs": 650,
     *   "success": true,
     *   "createdAt": "2026-08-28T10:30:00Z"
     * }
     */
    @GetMapping("/sessions/{sessionId}/reference")
    @Operation(
        summary = "Generate TTS reference for session topic",
        description = "Tạo audio pronunciation reference cho topic của speaking session",
        tags = {"Text-to-Speech"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "TTS audio generated successfully",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = TextToSpeechResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "Session not found")
    @ApiResponse(responseCode = "403", description = "Not session owner")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
    @ApiResponse(responseCode = "503", description = "TTS service unavailable")
    public ResponseEntity<TextToSpeechResponse> generateTtsForSessionTopic(
        @Parameter(description = "ID phiên speaking", required = true)
        @PathVariable Long sessionId,
        
        @AuthenticationPrincipal User user
    ) {
        log.info("Generate TTS for session topic - userId: {}, sessionId: {}", user.getId(), sessionId);
        
        try {
            // Get session from database to verify ownership and get topic
            var session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
            
            if (!session.getUser().getId().equals(user.getId())) {
                log.warn("Unauthorized session access - userId: {}, sessionId: {}", user.getId(), sessionId);
                throw new IllegalAccessException("Not session owner");
            }
            
            String topic = session.getTopic();
            
            // Generate TTS
            TextToSpeechResult result = textToSpeechService.synthesize(
                topic,
                "en-US"
            );
            
            TextToSpeechResponse response = TextToSpeechResponse.builder()
                .sessionId(sessionId)
                .topic(topic)
                .audioUrl(result.getAudioUrl())
                .audioFormat(result.getAudioFormat())
                .durationSeconds(result.getDurationSeconds())
                .voice(result.getVoice())
                .language(result.getLanguageCode())
                .provider(result.getProvider())
                .processingTimeMs(result.getProcessingTimeMs())
                .success(result.getSuccess())
                .createdAt(java.time.LocalDateTime.now())
                .build();
            
            log.info("TTS for session topic generated - userId: {}, sessionId: {}, duration: {}s", 
                     user.getId(), sessionId, result.getDurationSeconds());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error generating TTS for session topic - userId: {}, sessionId: {}, error: {}", 
                      user.getId(), sessionId, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
