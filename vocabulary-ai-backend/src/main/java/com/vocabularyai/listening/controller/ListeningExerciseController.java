package com.vocabularyai.listening.controller;

import com.vocabularyai.listening.dto.*;
import com.vocabularyai.listening.service.ListeningExerciseService;
import com.vocabularyai.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping({"/api/ai/listening", "/ai/listening"})
@RequiredArgsConstructor
@Tag(name = "AI Listening", description = "APIs cho Luyện nghe chép chính tả (Paragraph Dictation)")
@SecurityRequirement(name = "Bearer Authentication")
public class ListeningExerciseController {

    private final ListeningExerciseService listeningExerciseService;

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Tạo bài luyện nghe mới", description = "Sinh đoạn văn bằng Gemini, tạo audio bằng TTS và trả về thông tin bài nghe (không lộ văn bản)")
    public ResponseEntity<CreateListeningResponse> createExercise(
            @Valid @RequestBody CreateListeningRequest request,
            @AuthenticationPrincipal User user
    ) {
        log.info("Create listening exercise request - userId: {}, topic: '{}'", user.getId(), request.getTopic());
        CreateListeningResponse response = listeningExerciseService.createExercise(user.getId(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/audio/{exerciseId}/stream")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Stream file audio bài nghe", description = "Tải file audio MP3 cho bài luyện nghe của user")
    public ResponseEntity<byte[]> streamAudio(
            @PathVariable Long exerciseId,
            @AuthenticationPrincipal User user
    ) {
        byte[] audioBytes = listeningExerciseService.getAudioBytes(exerciseId, user.getId());
        ContentDisposition contentDisposition = ContentDisposition.inline()
                .filename("listening-" + exerciseId + ".mp3")
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .contentLength(audioBytes.length)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(audioBytes);
    }

    @PostMapping("/{exerciseId}/submit")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Nộp bài và chấm điểm", description = "Chấm điểm thuật toán so từ xác định, trả về kết quả từng từ và văn bản bài nghe")
    public ResponseEntity<ListeningSubmitResponse> submitExercise(
            @PathVariable Long exerciseId,
            @RequestBody ListeningSubmitRequest request,
            @AuthenticationPrincipal User user
    ) {
        log.info("Submit listening exercise - exerciseId: {}, userId: {}", exerciseId, user.getId());
        ListeningSubmitResponse response = listeningExerciseService.submitExercise(exerciseId, user.getId(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{exerciseId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Xem chi tiết bài nghe", description = "Lấy thông tin chi tiết bài nghe (đoạn văn chỉ trả về nếu bài đã nộp)")
    public ResponseEntity<ListeningSubmitResponse> getExerciseDetail(
            @PathVariable Long exerciseId,
            @AuthenticationPrincipal User user
    ) {
        ListeningSubmitResponse response = listeningExerciseService.getExerciseDetail(exerciseId, user.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lịch sử luyện nghe", description = "Lấy danh sách các bài luyện nghe đã làm của user")
    public ResponseEntity<Page<ListeningHistoryItemResponse>> getExerciseHistory(
            @AuthenticationPrincipal User user,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ListeningHistoryItemResponse> page = listeningExerciseService.getExerciseHistory(user.getId(), pageable);
        return ResponseEntity.ok(page);
    }
}

