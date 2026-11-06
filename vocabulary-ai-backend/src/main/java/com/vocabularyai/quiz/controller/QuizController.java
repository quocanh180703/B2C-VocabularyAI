package com.vocabularyai.quiz.controller;

import com.vocabularyai.common.response.ApiResponse;
import com.vocabularyai.common.util.PaginationUtils;
import com.vocabularyai.quiz.dto.*;
import com.vocabularyai.quiz.service.QuizService;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
     * Get all published quizzes (Public only)
     */
    @GetMapping
    @Operation(summary = "Get published quizzes", description = "Retrieve all published public quizzes")
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
     * Get quizzes created by authenticated user (My Quizzes)
     */
    @GetMapping("/mine")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get my quizzes", description = "Retrieve quizzes created by current user")
    public ResponseEntity<ApiResponse<Page<QuizResponse>>> getMyQuizzes(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        Long userId = userService.getCurrentUserId();
        log.info("Getting quizzes created by user: {}", userId);

        Pageable pageable = PaginationUtils.createPageableDesc(page, size, "createdAt");
        Page<QuizResponse> quizzes = quizService.getUserQuizzes(userId, pageable);

        return ResponseEntity.ok(
            ApiResponse.success("Lấy danh sách bài tập của bạn thành công", quizzes)
        );
    }

    /**
     * Get my quiz detail with correct answers (Owner only)
     */
    @GetMapping("/mine/{id}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get my quiz detail", description = "Retrieve quiz details and questions with correct answers for owner")
    public ResponseEntity<ApiResponse<QuizWithQuestionsResponse>> getMyQuizDetail(
            @PathVariable Long id) {
        Long userId = userService.getCurrentUserId();
        log.info("Getting my quiz detail: quizId={}, userId={}", id, userId);
        QuizWithQuestionsResponse response = quizService.getMyQuizDetail(id, userId);
        return ResponseEntity.ok(
            ApiResponse.success("Lấy chi tiết bài tập thành công", response)
        );
    }

    /**
     * Update my quiz (Owner only, PRIVATE or REJECTED)
     */
    @PutMapping("/mine/{id}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update my quiz", description = "Update quiz details and questions (owner only, PRIVATE or REJECTED)")
    public ResponseEntity<ApiResponse<QuizResponse>> updateMyQuiz(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMyQuizRequest request) {
        Long userId = userService.getCurrentUserId();
        log.info("Updating my quiz: quizId={}, userId={}", id, userId);
        QuizResponse quiz = quizService.updateUserQuiz(id, userId, request);
        return ResponseEntity.ok(
            ApiResponse.success("Cập nhật bài tập thành công", quiz)
        );
    }

    /**
     * Delete my quiz (Owner only, PRIVATE or REJECTED)
     */
    @DeleteMapping("/mine/{id}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete my quiz", description = "Delete quiz and its questions (owner only, PRIVATE or REJECTED)")
    public ResponseEntity<ApiResponse<Void>> deleteMyQuiz(
            @PathVariable Long id) {
        Long userId = userService.getCurrentUserId();
        log.info("Deleting my quiz: quizId={}, userId={}", id, userId);
        quizService.deleteUserQuiz(id, userId);
        return ResponseEntity.ok(
            ApiResponse.success("Xóa bài tập thành công", null)
        );
    }

    /**
     * Request publication of user quiz (PRIVATE/REJECTED -> PENDING)
     */
    @PostMapping("/mine/{id}/publish-request")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Request publication of quiz", description = "Send user quiz for review by instructors")
    public ResponseEntity<ApiResponse<QuizResponse>> publishRequest(
            @PathVariable Long id) {
        Long userId = userService.getCurrentUserId();
        log.info("User {} requesting publication for quiz {}", userId, id);
        QuizResponse quiz = quizService.publishRequest(id, userId);
        return ResponseEntity.ok(
            ApiResponse.success("Đã gửi yêu cầu duyệt bài tập tới giảng viên", quiz)
        );
    }

    /**
     * Withdraw publication request (PENDING -> PRIVATE)
     */
    @PostMapping("/mine/{id}/withdraw")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Withdraw publication request", description = "Withdraw pending quiz review back to private")
    public ResponseEntity<ApiResponse<QuizResponse>> withdrawPublishRequest(
            @PathVariable Long id) {
        Long userId = userService.getCurrentUserId();
        log.info("User {} withdrawing publication request for quiz {}", userId, id);
        QuizResponse quiz = quizService.withdrawPublishRequest(id, userId);
        return ResponseEntity.ok(
            ApiResponse.success("Đã rút yêu cầu duyệt bài tập", quiz)
        );
    }

    /**
     * Hide published quiz back to private (PUBLIC -> PRIVATE)
     */
    @PostMapping("/mine/{id}/hide")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Hide published quiz", description = "Revert published quiz back to private")
    public ResponseEntity<ApiResponse<QuizResponse>> hideQuiz(
            @PathVariable Long id) {
        Long userId = userService.getCurrentUserId();
        log.info("User {} hiding quiz {}", userId, id);
        QuizResponse quiz = quizService.hideQuiz(id, userId);
        return ResponseEntity.ok(
            ApiResponse.success("Đã chuyển bài tập về chế độ riêng tư", quiz)
        );
    }

    /**
     * Get all quizzes including drafts (Admin)
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get all quizzes (admin)", description = "Retrieve all quizzes including draft and published")
    public ResponseEntity<ApiResponse<Page<QuizResponse>>> getAllQuizzes(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "50") Integer size) {
        log.info("Getting all quizzes for admin/instructor: {}", user != null ? user.getUsername() : "anonymous");
        Pageable pageable = PaginationUtils.createPageableDesc(page, size, "createdAt");
        User.UserRole role = user != null ? user.getRole() : null;
        Page<QuizResponse> quizzes = quizService.getAllQuizzes(pageable, role);
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
        Long currentUserId = null;
        try {
            currentUserId = userService.getCurrentUserId();
        } catch (Exception ignored) {}
        
        QuizWithQuestionsResponse response = quizService.getQuizForTaking(id, currentUserId);
        
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
     * Get user's quiz attempt history (authenticated)
     */
    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Get user's quiz attempt history",
        description = "Retrieve all quiz attempts for the authenticated user"
    )
    public ResponseEntity<ApiResponse<Page<QuizAttemptResponse>>> getUserQuizHistory(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        log.info("Getting quiz history for current user");
        
        Long userId = userService.getCurrentUserId();
        Pageable pageable = PaginationUtils.createPageableDesc(page, size, "createdAt");
        Page<QuizAttemptResponse> history = quizService.getUserQuizHistory(userId, pageable);
        
        return ResponseEntity.ok(
            ApiResponse.success("Quiz history retrieved successfully", history)
        );
    }
    
    /**
     * Get quiz attempt details with answers (authenticated)
     */
    @GetMapping("/attempts/{attemptId}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Get quiz attempt details with answers",
        description = "Retrieve detailed results of a quiz attempt including questions and answers"
    )
    public ResponseEntity<ApiResponse<QuizAttemptWithAnswersResponse>> getQuizAttempt(
            @PathVariable Long attemptId) {
        log.info("Getting quiz attempt details: {}", attemptId);
        
        Long userId = userService.getCurrentUserId();
        QuizAttemptWithAnswersResponse response = quizService.getQuizAttemptWithAnswers(attemptId, userId);
        
        return ResponseEntity.ok(
            ApiResponse.success("Quiz attempt details retrieved successfully", response)
        );
    }
}
