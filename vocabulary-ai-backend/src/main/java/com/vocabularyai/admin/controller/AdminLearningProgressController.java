package com.vocabularyai.admin.controller;

import com.vocabularyai.admin.dto.request.CreateLearningProgressRequest;
import com.vocabularyai.admin.dto.request.UpdateLearningProgressRequest;
import com.vocabularyai.admin.dto.response.LearningProgressDetailResponse;
import com.vocabularyai.admin.dto.response.LearningProgressListResponse;
import com.vocabularyai.admin.service.AdminLearningProgressService;
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
@RequestMapping("/api/admin/learning-progress")
@RequiredArgsConstructor
@Tag(name = "Admin - Learning Progress Management")
public class AdminLearningProgressController {
    
    private final AdminLearningProgressService service;
    
    @GetMapping
    @Operation(summary = "Danh sách tiến độ học")
    public ResponseEntity<Page<LearningProgressListResponse>> listProgress(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<LearningProgressListResponse> result = service.listProgress(pageable);
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/{progressId}")
    @Operation(summary = "Chi tiết tiến độ học")
    public ResponseEntity<LearningProgressDetailResponse> getDetail(@PathVariable Long progressId) {
        return ResponseEntity.ok(service.getProgressDetail(progressId));
    }
    
    @PostMapping
    @Operation(summary = "Tạo tiến độ học")
    public ResponseEntity<LearningProgressDetailResponse> createProgress(@Valid @RequestBody CreateLearningProgressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createProgress(request));
    }
    
    @PutMapping("/{progressId}")
    @Operation(summary = "Cập nhật tiến độ học")
    public ResponseEntity<LearningProgressDetailResponse> updateProgress(
            @PathVariable Long progressId,
            @Valid @RequestBody UpdateLearningProgressRequest request) {
        return ResponseEntity.ok(service.updateProgress(progressId, request));
    }
    
    @DeleteMapping("/{progressId}")
    @Operation(summary = "Xóa tiến độ học")
    public ResponseEntity<Void> deleteProgress(@PathVariable Long progressId) {
        service.deleteProgress(progressId);
        return ResponseEntity.noContent().build();
    }
}
