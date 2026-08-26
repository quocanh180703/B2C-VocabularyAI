package com.vocabularyai.learning.controller;

import com.vocabularyai.common.response.ApiResponse;
import com.vocabularyai.common.util.PaginationUtils;
import com.vocabularyai.learning.dto.LearningHistoryResponse;
import com.vocabularyai.learning.dto.LearningProgressResponse;
import com.vocabularyai.learning.dto.ProgressStatisticsResponse;
import com.vocabularyai.learning.dto.ReviewWordRequest;
import com.vocabularyai.learning.service.LearningService;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Learning Controller
 * 
 * API endpoints for learning progress and history management
 */
@Slf4j
@RestController
@RequestMapping("/learning")
@RequiredArgsConstructor
@Tag(name = "Learning", description = "Learning progress and history management endpoints")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("isAuthenticated()")
public class LearningController {
    
    private final LearningService learningService;
    private final UserService userService;
    
    /**
     * Start learning a word
     */
    @PostMapping("/words/{wordId}/start")
    @Operation(
        summary = "Start learning a word",
        description = "Begin learning a new vocabulary word"
    )
    public ResponseEntity<ApiResponse<LearningProgressResponse>> startLearningWord(
            @PathVariable Long wordId) {
        log.info("Starting to learn word: {}", wordId);
        
        User user = userService.getCurrentUser();
        LearningProgressResponse response = learningService.startLearningWord(user, wordId);
        
        return ResponseEntity.ok(
            ApiResponse.success("Started learning word successfully", response)
        );
    }
    
    /**
     * Review a word (mark correct or incorrect)
     */
    @PostMapping("/words/{wordId}/review")
    @Operation(
        summary = "Review a word",
        description = "Mark word as correct or incorrect during review"
    )
    public ResponseEntity<ApiResponse<LearningProgressResponse>> reviewWord(
            @PathVariable Long wordId,
            @Valid @RequestBody ReviewWordRequest request) {
        log.info("Reviewing word {}: {}", wordId, request.getCorrect());
        
        User user = userService.getCurrentUser();
        LearningProgressResponse response = learningService.reviewWord(user, wordId, request.getCorrect());
        
        return ResponseEntity.ok(
            ApiResponse.success("Word reviewed successfully", response)
        );
    }
    
    /**
     * Get user's learning progress
     */
    @GetMapping("/progress")
    @Operation(
        summary = "Get learning progress",
        description = "Retrieve current user's learning progress for all words"
    )
    public ResponseEntity<ApiResponse<Page<LearningProgressResponse>>> getProgress(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        log.info("Getting learning progress");
        
        Long userId = userService.getCurrentUserId();
        Pageable pageable = PaginationUtils.createPageableDesc(page, size, "createdAt");
        Page<LearningProgressResponse> progress = learningService.getUserProgress(userId, pageable);
        
        return ResponseEntity.ok(
            ApiResponse.success("Learning progress retrieved successfully", progress)
        );
    }
    
    /**
     * Get words needing review
     */
    @GetMapping("/review-queue")
    @Operation(
        summary = "Get words needing review",
        description = "Retrieve words scheduled for review (using Spaced Repetition System)"
    )
    public ResponseEntity<ApiResponse<Page<LearningProgressResponse>>> getWordsNeedingReview(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        log.info("Getting words needing review");
        
        Long userId = userService.getCurrentUserId();
        Pageable pageable = PaginationUtils.createPageableDesc(page, size, "nextReviewAt");
        Page<LearningProgressResponse> words = learningService.getWordsNeedingReview(userId, pageable);
        
        return ResponseEntity.ok(
            ApiResponse.success("Words needing review retrieved successfully", words)
        );
    }
    
    /**
     * Get learning history
     */
    @GetMapping("/history")
    @Operation(
        summary = "Get learning history",
        description = "Retrieve detailed learning history of all activities"
    )
    public ResponseEntity<ApiResponse<Page<LearningHistoryResponse>>> getLearningHistory(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        log.info("Getting learning history");
        
        Long userId = userService.getCurrentUserId();
        Pageable pageable = PaginationUtils.createPageableDesc(page, size, "createdAt");
        Page<LearningHistoryResponse> history = learningService.getLearningHistory(userId, pageable);
        
        return ResponseEntity.ok(
            ApiResponse.success("Learning history retrieved successfully", history)
        );
    }
    
    /**
     * Get progress statistics
     */
    @GetMapping("/statistics")
    @Operation(
        summary = "Get progress statistics",
        description = "Retrieve summary statistics about learning progress"
    )
    public ResponseEntity<ApiResponse<ProgressStatisticsResponse>> getStatistics() {
        log.info("Getting progress statistics");
        
        Long userId = userService.getCurrentUserId();
        ProgressStatisticsResponse statistics = learningService.getProgressStatistics(userId);
        
        return ResponseEntity.ok(
            ApiResponse.success("Progress statistics retrieved successfully", statistics)
        );
    }
}
