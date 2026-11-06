package com.vocabularyai.quiz.controller;

import com.vocabularyai.common.response.ApiResponse;
import com.vocabularyai.common.util.PaginationUtils;
import com.vocabularyai.quiz.dto.QuizResponse;
import com.vocabularyai.quiz.dto.QuizWithQuestionsResponse;
import com.vocabularyai.quiz.dto.ReviewExerciseRequest;
import com.vocabularyai.quiz.service.ExerciseReviewService;
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
@RequestMapping({"/admin/exercise-reviews", "/api/admin/exercise-reviews"})
@RequiredArgsConstructor
@Tag(name = "Admin Exercise Review", description = "Instructor and Admin endpoints for reviewing user-submitted exercises")
@PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
public class AdminExerciseReviewController {

    private final ExerciseReviewService exerciseReviewService;

    @GetMapping
    @Operation(summary = "Lấy danh sách bài tập đang chờ duyệt (PENDING)")
    public ResponseEntity<ApiResponse<Page<QuizResponse>>> listPendingQuizzes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PaginationUtils.createPageableDesc(page, size, "createdAt");
        Page<QuizResponse> result = exerciseReviewService.getPendingQuizzes(pageable);
        return ResponseEntity.ok(ApiResponse.success("Danh sách bài tập chờ duyệt", result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết bài tập kèm câu hỏi và đáp án đúng để duyệt")
    public ResponseEntity<ApiResponse<QuizWithQuestionsResponse>> getPendingQuizDetail(
            @PathVariable Long id) {
        QuizWithQuestionsResponse response = exerciseReviewService.getPendingQuizDetail(id);
        return ResponseEntity.ok(ApiResponse.success("Chi tiết bài tập chờ duyệt", response));
    }

    @PostMapping("/{id}/review")
    @Operation(summary = "Phê duyệt hoặc từ chối bài tập")
    public ResponseEntity<ApiResponse<QuizResponse>> reviewQuiz(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ReviewExerciseRequest request) {
        QuizResponse response = exerciseReviewService.reviewQuiz(id, user, request);
        return ResponseEntity.ok(ApiResponse.success("Xử lý duyệt bài tập thành công", response));
    }
}

