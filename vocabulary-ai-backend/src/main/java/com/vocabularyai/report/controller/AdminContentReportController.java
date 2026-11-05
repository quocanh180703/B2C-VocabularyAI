package com.vocabularyai.report.controller;

import com.vocabularyai.common.response.ApiResponse;
import com.vocabularyai.common.util.PaginationUtils;
import com.vocabularyai.report.dto.ContentReportResponse;
import com.vocabularyai.report.dto.ResolveContentReportRequest;
import com.vocabularyai.report.entity.ContentReportStatus;
import com.vocabularyai.report.entity.ContentReportType;
import com.vocabularyai.report.service.ContentReportService;
import com.vocabularyai.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/content-reports")
@RequiredArgsConstructor
@Tag(name = "Admin Content Reports", description = "Admin & Instructor content review and error report handling")
@PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
public class AdminContentReportController {

    private final ContentReportService contentReportService;

    @GetMapping
    @Operation(summary = "Danh sách báo cáo nội dung", description = "Xem và lọc báo cáo lỗi từ học viên")
    public ResponseEntity<ApiResponse<Page<ContentReportResponse>>> listReports(
            @RequestParam(required = false) ContentReportStatus status,
            @RequestParam(required = false) ContentReportType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PaginationUtils.createPageableDesc(page, size, "createdAt");
        Page<ContentReportResponse> result = contentReportService.listReports(status, type, pageable);
        return ResponseEntity.ok(ApiResponse.success("Danh sách báo cáo nội dung", result));
    }

    @PostMapping("/{id}/resolve")
    @Operation(summary = "Xử lý duyệt hoặc từ chối báo cáo", description = "Cập nhật trạng thái duyệt (APPROVED, REJECTED, FIXED)")
    public ResponseEntity<ApiResponse<ContentReportResponse>> resolveReport(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ResolveContentReportRequest request) {
        ContentReportResponse response = contentReportService.resolveReport(id, user, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái báo cáo thành công", response));
    }
}
