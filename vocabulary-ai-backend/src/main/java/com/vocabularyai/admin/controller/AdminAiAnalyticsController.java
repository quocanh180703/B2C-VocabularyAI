package com.vocabularyai.admin.controller;

import com.vocabularyai.admin.dto.response.AiAnalyticsSummaryResponse;
import com.vocabularyai.admin.dto.response.AiRequestLogResponse;
import com.vocabularyai.admin.service.AdminAiAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/ai-analytics")
@Tag(name = "Admin AI Analytics & Cost Optimizer", description = "Monitor AI token usage, costs, performance, and SLA")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminAiAnalyticsController {

    private final AdminAiAnalyticsService adminAiAnalyticsService;

    @GetMapping("/summary")
    @Operation(summary = "Get AI Analytics Summary", description = "Aggregated token consumption, cost, latency, feature breakdown, and tips")
    public ResponseEntity<AiAnalyticsSummaryResponse> getSummary(
            @Parameter(description = "Time range filter: 24h, 7d, 30d, all")
            @RequestParam(defaultValue = "7d") String timeRange
    ) {
        log.info("GET /api/admin/ai-analytics/summary?timeRange={}", timeRange);
        AiAnalyticsSummaryResponse summary = adminAiAnalyticsService.getAnalyticsSummary(timeRange);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/logs")
    @Operation(summary = "Get AI Request Logs", description = "Paginated list of historical AI calls with status and token details")
    public ResponseEntity<Page<AiRequestLogResponse>> getLogs(
            @Parameter(description = "Time range: 24h, 7d, 30d, all")
            @RequestParam(defaultValue = "7d") String timeRange,
            @Parameter(description = "Filter by feature code (e.g., AI_TUTOR_CHAT, GRAMMAR_CHECK)")
            @RequestParam(required = false) String feature,
            @Parameter(description = "Filter by status (e.g., SUCCESS, FAILED)")
            @RequestParam(required = false) String status,
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("GET /api/admin/ai-analytics/logs - timeRange={}, feature={}, status={}", timeRange, feature, status);
        Page<AiRequestLogResponse> logs = adminAiAnalyticsService.getLogs(timeRange, feature, status, pageable);
        return ResponseEntity.ok(logs);
    }
}
