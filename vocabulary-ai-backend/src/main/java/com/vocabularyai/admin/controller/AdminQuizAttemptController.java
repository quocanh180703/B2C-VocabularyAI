package com.vocabularyai.admin.controller;

import com.vocabularyai.admin.dto.request.CreateQuizAttemptRequest;
import com.vocabularyai.admin.dto.request.UpdateQuizAttemptRequest;
import com.vocabularyai.admin.dto.response.QuizAttemptDetailResponse;
import com.vocabularyai.admin.dto.response.QuizAttemptListResponse;
import com.vocabularyai.admin.service.AdminQuizAttemptService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/admin/quiz-attempts")
@RequiredArgsConstructor
@Tag(name = "Admin - Quiz Attempt Management")
public class AdminQuizAttemptController {
    
    private final AdminQuizAttemptService service;
    
    @GetMapping
    @Operation(summary = "Danh sách lần làm bài kiểm tra")
    public ResponseEntity<Page<QuizAttemptListResponse>> listAttempts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<QuizAttemptListResponse> result = service.listAttempts(pageable);
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/{attemptId}")
    @Operation(summary = "Chi tiết lần làm bài")
    public ResponseEntity<QuizAttemptDetailResponse> getDetail(@PathVariable Long attemptId) {
        return ResponseEntity.ok(service.getAttemptDetail(attemptId));
    }
    
    @PostMapping
    @Operation(summary = "Tạo lần làm bài")
    public ResponseEntity<QuizAttemptDetailResponse> createAttempt(@Valid @RequestBody CreateQuizAttemptRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createAttempt(request));
    }
    
    @PutMapping("/{attemptId}")
    @Operation(summary = "Cập nhật lần làm bài")
    public ResponseEntity<QuizAttemptDetailResponse> updateAttempt(
            @PathVariable Long attemptId,
            @Valid @RequestBody UpdateQuizAttemptRequest request) {
        return ResponseEntity.ok(service.updateAttempt(attemptId, request));
    }
    
    @DeleteMapping("/{attemptId}")
    @Operation(summary = "Xóa lần làm bài")
    public ResponseEntity<Void> deleteAttempt(@PathVariable Long attemptId) {
        service.deleteAttempt(attemptId);
        return ResponseEntity.noContent().build();
    }
}
