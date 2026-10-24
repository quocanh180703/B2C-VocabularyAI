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
@Tag(name = "Quizzes", description = "Quiz management and taking endpoints")
public class QuizController {
    
    private final QuizService quizService;
    private final UserService userService;
    
    public QuizController(QuizService quizService, UserService userService) {
        this.quizService = quizService;
        this.userService = userService;
    }
    
    /**
     * Get all published quizzes
     */
    @GetMapping
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
     * Get all quizzes including drafts (Admin)
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get all quizzes (admin)", description = "Retrieve all quizzes including draft and published")
    public ResponseEntity<ApiResponse<Page<QuizResponse>>> getAllQuizzes(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "50") Integer size) {
        log.info("Getting all quizzes for admin");
        Pageable pageable = PaginationUtils.createPageableDesc(page, size, "createdAt");
        Page<QuizResponse> quizzes = quizService.getAllQuizzes(pageable);
        return ResponseEntity.ok(ApiResponse.success("All quizzes retrieved successfully", quizzes));
    }

    /**
     * Create quiz (Admin)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create quiz", description = "Create a new quiz (admin only)")
    public ResponseEntity<ApiResponse<QuizResponse>> createQuiz(
            @Valid @RequestBody CreateQuizRequest request) {
        log.info("Admin creating quiz: {}", request.getTitle());
        QuizResponse quiz = quizService.createQuiz(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.success("Quiz created successfully", quiz)
        );
    }

    /**
     * Update quiz (Admin)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update quiz", description = "Update quiz details (admin only)")
    public ResponseEntity<ApiResponse<QuizResponse>> updateQuiz(
            @PathVariable Long id,
            @RequestBody CreateQuizRequest request,
            @RequestParam(required = false) String status) {
        log.info("Admin updating quiz: {}", id);
        QuizResponse quiz = quizService.updateQuiz(id, request, status);
        return ResponseEntity.ok(ApiResponse.success("Quiz updated successfully", quiz));
    }

    /**
     * Delete quiz (Admin)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete quiz", description = "Delete a quiz and its questions (admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteQuiz(@PathVariable Long id) {
        log.info("Admin deleting quiz: {}", id);
        quizService.deleteQuiz(id);
        return ResponseEntity.ok(ApiResponse.success("Quiz deleted successfully", null));
    }
    
    /**
     * Get quiz by ID (with questions for taking)
     */
    @GetMapping("/{id}")
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
    @Operation(
        summary = "Get quiz attempt details",
        description = "Get quiz attempt with all answers and explanations"
    )
    public ResponseEntity<ApiResponse<QuizAttemptWithAnswersResponse>> getQuizAttemptDetails(
            @PathVariable Long attemptId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.vocabularyai.user.entity.User user) {
        log.info("Getting quiz attempt details: {}", attemptId);
        
        QuizAttemptWithAnswersResponse response;
        if (user != null) {
            response = quizService.getQuizAttemptWithAnswers(attemptId, user.getId());
        } else {
            response = quizService.getQuizAttemptWithAnswers(attemptId);
        }
        
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
