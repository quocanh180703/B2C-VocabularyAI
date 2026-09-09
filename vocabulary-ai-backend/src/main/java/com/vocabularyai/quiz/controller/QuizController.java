package com.vocabularyai.quiz.controller;

import com.vocabularyai.common.response.ApiResponse;
import com.vocabularyai.common.util.PaginationUtils;
import com.vocabularyai.quiz.dto.*;
import com.vocabularyai.quiz.service.QuizService;
import com.vocabularyai.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Quiz Controller
 * 
 * API endpoints for quiz management and taking
 */
@Slf4j
@RestController
@RequestMapping("/quizzes")
@RequiredArgsConstructor
@Tag(name = "Quizzes", description = "Quiz management and taking endpoints")
public class QuizController {
    
    private final QuizService quizService;
    private final UserService userService;
    
    /**
     * Get all published quizzes
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get published quizzes", description = "Retrieve all published quizzes")
    public ResponseEntity<ApiResponse<Page<QuizResponse>>> getPublishedQuizzes(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        log.info("Getting published quizzes");
        
        Pageable pageable = PaginationUtils.createPageableDesc(page, size, "createdAt");
        Page<QuizResponse> quizzes = quizService.getPublishedQuizzes(pageable);
        
        return ResponseEntity.ok(
            ApiResponse.success("Quizzes retrieved successfully", quizzes)
        );
    }
    
    /**
     * Get quiz by ID (with questions for taking)
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get quiz with questions", description = "Get quiz details and questions for taking")
    public ResponseEntity<ApiResponse<QuizWithQuestionsResponse>> getQuizForTaking(
            @PathVariable Long id) {
        log.info("Getting quiz for taking: {}", id);
        
        QuizWithQuestionsResponse response = quizService.getQuizForTaking(id);
        
        return ResponseEntity.ok(
            ApiResponse.success("Quiz retrieved successfully", response)
        );
    }
    
    /**
     * Start quiz attempt (authenticated)
     */
    @PostMapping("/{quizId}/start")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Start quiz attempt",
        description = "Begin taking a quiz and get the attempt ID"
    )
    public ResponseEntity<ApiResponse<QuizAttemptResponse>> startQuizAttempt(
            @PathVariable Long quizId) {
        log.info("Starting quiz attempt for quiz: {}", quizId);
        
        var user = userService.getCurrentUser();
        QuizAttemptResponse response = quizService.startQuizAttempt(user, quizId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.success("Quiz attempt started successfully", response)
        );
    }
    
    /**
     * Submit quiz answers (authenticated)
     */
    @PostMapping("/{attemptId}/submit")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Submit quiz answers",
        description = "Submit quiz answers and get the score"
    )
    public ResponseEntity<ApiResponse<QuizAttemptResponse>> submitQuiz(
            @PathVariable Long attemptId,
            @Valid @RequestBody SubmitQuizRequest request,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.vocabularyai.user.entity.User user) {
        log.info("Submitting quiz attempt: {} for user: {}", attemptId, user.getId());
        
        QuizAttemptResponse response = quizService.submitQuiz(attemptId, user.getId(), request);
        
        return ResponseEntity.ok(
            ApiResponse.success("Quiz submitted successfully", response)
        );
    }
    
    /**
     * Get quiz attempt with answers (authenticated)
     */
    @GetMapping("/attempts/{attemptId}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Get quiz attempt details",
        description = "Get quiz attempt with all answers and explanations"
    )
    public ResponseEntity<ApiResponse<QuizAttemptWithAnswersResponse>> getQuizAttemptDetails(
            @PathVariable Long attemptId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.vocabularyai.user.entity.User user) {
        log.info("Getting quiz attempt details: {} for user: {}", attemptId, user.getId());
        
        QuizAttemptWithAnswersResponse response = quizService.getQuizAttemptWithAnswers(attemptId, user.getId());
        
        return ResponseEntity.ok(
            ApiResponse.success("Quiz attempt retrieved successfully", response)
        );
    }
    
    /**
     * Get user's quiz history (authenticated)
     */
    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Get user's quiz history",
        description = "Get all quiz attempts by current user"
    )
    public ResponseEntity<ApiResponse<Page<QuizAttemptResponse>>> getQuizHistory(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        log.info("Getting quiz history");
        
        Long userId = userService.getCurrentUserId();
        Pageable pageable = PaginationUtils.createPageableDesc(page, size, "createdAt");
        Page<QuizAttemptResponse> history = quizService.getUserQuizHistory(userId, pageable);
        
        return ResponseEntity.ok(
            ApiResponse.success("Quiz history retrieved successfully", history)
        );
    }
}
