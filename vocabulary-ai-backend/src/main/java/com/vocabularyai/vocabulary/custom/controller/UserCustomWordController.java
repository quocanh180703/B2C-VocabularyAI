package com.vocabularyai.vocabulary.custom.controller;

import com.vocabularyai.common.response.ApiResponse;
import com.vocabularyai.common.util.PaginationUtils;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.vocabulary.custom.dto.CreateCustomWordRequest;
import com.vocabularyai.vocabulary.custom.dto.CustomWordResponse;
import com.vocabularyai.vocabulary.custom.dto.UpdateCustomWordRequest;
import com.vocabularyai.vocabulary.custom.service.UserCustomWordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/my-words")
@RequiredArgsConstructor
@Tag(name = "User Custom Words", description = "Endpoints for managing user's private vocabulary")
@PreAuthorize("isAuthenticated()")
public class UserCustomWordController {

    private final UserCustomWordService userCustomWordService;

    @GetMapping
    @Operation(summary = "Danh sách từ vựng riêng", description = "Lấy danh sách từ vựng riêng của người dùng hiện tại")
    public ResponseEntity<ApiResponse<Page<CustomWordResponse>>> listWords(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PaginationUtils.createPageableDesc(page, size, "createdAt");
        Page<CustomWordResponse> result = userCustomWordService.listWords(user, level, topic, search, pageable);
        return ResponseEntity.ok(ApiResponse.success("Danh sách từ vựng riêng", result));
    }

    @PostMapping
    @Operation(summary = "Thêm từ vựng riêng", description = "Tự thêm từ vựng mới để học và ôn tập riêng")
    public ResponseEntity<ApiResponse<CustomWordResponse>> createWord(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateCustomWordRequest request) {
        CustomWordResponse response = userCustomWordService.createWord(user, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm từ vựng riêng thành công", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết từ vựng riêng")
    public ResponseEntity<ApiResponse<CustomWordResponse>> getWord(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        CustomWordResponse response = userCustomWordService.getWord(user, id);
        return ResponseEntity.ok(ApiResponse.success("Chi tiết từ vựng riêng", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật từ vựng riêng")
    public ResponseEntity<ApiResponse<CustomWordResponse>> updateWord(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody UpdateCustomWordRequest request) {
        CustomWordResponse response = userCustomWordService.updateWord(user, id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật từ vựng riêng thành công", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa từ vựng riêng")
    public ResponseEntity<ApiResponse<Void>> deleteWord(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        userCustomWordService.deleteWord(user, id);
        return ResponseEntity.ok(ApiResponse.success("Xóa từ vựng riêng thành công"));
    }
}
