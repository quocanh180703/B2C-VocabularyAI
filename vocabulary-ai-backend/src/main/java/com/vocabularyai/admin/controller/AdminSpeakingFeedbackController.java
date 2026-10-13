package com.vocabularyai.admin.controller;

import com.vocabularyai.admin.dto.request.CreateSpeakingFeedbackRequest;
import com.vocabularyai.admin.dto.request.UpdateSpeakingFeedbackRequest;
import com.vocabularyai.admin.dto.response.SpeakingFeedbackDetailResponse;
import com.vocabularyai.admin.dto.response.SpeakingFeedbackListResponse;
import com.vocabularyai.admin.service.AdminSpeakingFeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/speaking-feedback")
@RequiredArgsConstructor
@Tag(name = "Admin - Speaking Feedback Management")
public class AdminSpeakingFeedbackController {
    
    private final AdminSpeakingFeedbackService service;
    
    @GetMapping
    @Operation(summary = "List speaking feedback")
    public ResponseEntity<Page<SpeakingFeedbackListResponse>> listFeedback(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long sessionId,
            @RequestParam(required = false) Long attemptId,
            @RequestParam(required = false) String search) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<SpeakingFeedbackListResponse> result;
        
        if (search != null && !search.isBlank()) {
            result = service.searchFeedback(search, pageable);
        } else if (sessionId != null && sessionId > 0) {
            result = service.listBySessionId(sessionId, pageable);
        } else if (attemptId != null && attemptId > 0) {
            result = service.listByAttemptId(attemptId, pageable);
        } else {
            result = service.listFeedback(pageable);
        }
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/{feedbackId}")
    @Operation(summary = "Get feedback detail")
    public ResponseEntity<SpeakingFeedbackDetailResponse> getDetail(@PathVariable Long feedbackId) {
        return ResponseEntity.ok(service.getFeedbackDetail(feedbackId));
    }
    
    @PostMapping
    @Operation(summary = "Create feedback")
    public ResponseEntity<SpeakingFeedbackDetailResponse> createFeedback(@Valid @RequestBody CreateSpeakingFeedbackRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createFeedback(request));
    }
    
    @PutMapping("/{feedbackId}")
    @Operation(summary = "Update feedback")
    public ResponseEntity<SpeakingFeedbackDetailResponse> updateFeedback(
            @PathVariable Long feedbackId,
            @Valid @RequestBody UpdateSpeakingFeedbackRequest request) {
        return ResponseEntity.ok(service.updateFeedback(feedbackId, request));
    }
    
    @DeleteMapping("/{feedbackId}")
    @Operation(summary = "Delete feedback")
    public ResponseEntity<Void> deleteFeedback(@PathVariable Long feedbackId) {
        service.deleteFeedback(feedbackId);
        return ResponseEntity.noContent().build();
    }
}
