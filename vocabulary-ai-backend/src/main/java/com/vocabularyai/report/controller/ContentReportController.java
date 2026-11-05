package com.vocabularyai.report.controller;

import com.vocabularyai.common.response.ApiResponse;
import com.vocabularyai.report.dto.ContentReportResponse;
import com.vocabularyai.report.dto.CreateContentReportRequest;
import com.vocabularyai.report.service.ContentReportService;
import com.vocabularyai.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/content-reports")
@RequiredArgsConstructor
@Tag(name = "Content Reports", description = "User vocabulary content error reporting endpoints")
public class ContentReportController {

    private final ContentReportService contentReportService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Gửi báo cáo lỗi từ vựng", description = "Người dùng gửi báo cáo lỗi về nghĩa, ví dụ, phát âm hoặc level của từ")
    public ResponseEntity<ApiResponse<ContentReportResponse>> createReport(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateContentReportRequest request) {
        ContentReportResponse response = contentReportService.createReport(user, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Báo cáo lỗi đã được gửi thành công. Cảm ơn bạn đã đóng góp!", response));
    }
}
