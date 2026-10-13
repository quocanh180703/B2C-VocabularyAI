package com.vocabularyai.admin.controller;

import com.vocabularyai.admin.dto.request.CreateQuestionRequest;
import com.vocabularyai.admin.dto.request.UpdateQuestionRequest;
import com.vocabularyai.admin.dto.response.QuestionDetailResponse;
import com.vocabularyai.admin.dto.response.QuestionListResponse;
import com.vocabularyai.admin.service.AdminQuestionService;
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
@RequestMapping("/api/admin/questions")
@RequiredArgsConstructor
@Tag(name = "Admin - Question Management")
public class AdminQuestionController {
    
    private final AdminQuestionService service;
    
    @GetMapping
    @Operation(summary = "Danh sách câu hỏi")
    public ResponseEntity<Page<QuestionListResponse>> listQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<QuestionListResponse> result = service.listQuestions(pageable);
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/{questionId}")
    @Operation(summary = "Chi tiết câu hỏi")
    public ResponseEntity<QuestionDetailResponse> getDetail(@PathVariable Long questionId) {
        return ResponseEntity.ok(service.getQuestionDetail(questionId));
    }
    
    @PostMapping
    @Operation(summary = "Tạo câu hỏi")
    public ResponseEntity<QuestionDetailResponse> createQuestion(@Valid @RequestBody CreateQuestionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createQuestion(request));
    }
    
    @PutMapping("/{questionId}")
    @Operation(summary = "Cập nhật câu hỏi")
    public ResponseEntity<QuestionDetailResponse> updateQuestion(
            @PathVariable Long questionId,
            @Valid @RequestBody UpdateQuestionRequest request) {
        return ResponseEntity.ok(service.updateQuestion(questionId, request));
    }
    
    @DeleteMapping("/{questionId}")
    @Operation(summary = "Xóa câu hỏi")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long questionId) {
        service.deleteQuestion(questionId);
        return ResponseEntity.noContent().build();
    }
}
