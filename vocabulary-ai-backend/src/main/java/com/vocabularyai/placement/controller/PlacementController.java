package com.vocabularyai.placement.controller;

import com.vocabularyai.common.response.ApiResponse;
import com.vocabularyai.placement.dto.PlacementQuestionResponse;
import com.vocabularyai.placement.dto.PlacementStatusResponse;
import com.vocabularyai.placement.dto.PlacementSubmitRequest;
import com.vocabularyai.placement.dto.PlacementSubmitResponse;
import com.vocabularyai.placement.service.PlacementService;
import com.vocabularyai.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/placement")
@RequiredArgsConstructor
@Tag(name = "Placement Test", description = "Placement test endpoints for initial CEFR level classification")
public class PlacementController {

    private final PlacementService placementService;

    @GetMapping("/status")
    @Operation(summary = "Get placement test status", description = "Check if user has completed placement test and retrieve current placement level")
    public ResponseEntity<ApiResponse<PlacementStatusResponse>> getStatus(@AuthenticationPrincipal User user) {
        log.info("GET /api/placement/status - userId: {}", user.getId());
        PlacementStatusResponse status = placementService.getStatus(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Placement status retrieved successfully", status));
    }

    @GetMapping("/test")
    @Operation(summary = "Get placement test questions", description = "Generate 20 placement questions (4 per CEFR level A1-C1) with answers stripped")
    public ResponseEntity<ApiResponse<List<PlacementQuestionResponse>>> getTest() {
        log.info("GET /api/placement/test");
        List<PlacementQuestionResponse> questions = placementService.generateTest();
        return ResponseEntity.ok(ApiResponse.success("Placement test questions retrieved successfully", questions));
    }

    @PostMapping("/submit")
    @Operation(summary = "Submit placement test", description = "Evaluate answers, calculate effective placement level, and record attempt")
    public ResponseEntity<ApiResponse<PlacementSubmitResponse>> submit(
            @AuthenticationPrincipal User user,
            @RequestBody PlacementSubmitRequest request) {
        log.info("POST /api/placement/submit - userId: {}", user.getId());
        PlacementSubmitResponse response = placementService.submitTest(user, request);
        return ResponseEntity.ok(ApiResponse.success("Placement test evaluated successfully", response));
    }
}
