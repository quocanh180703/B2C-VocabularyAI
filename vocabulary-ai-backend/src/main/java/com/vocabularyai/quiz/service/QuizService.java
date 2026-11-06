package com.vocabularyai.quiz.service;

import com.vocabularyai.common.exception.BadRequestException;
import com.vocabularyai.common.exception.ConflictException;
import com.vocabularyai.common.exception.ForbiddenException;
import com.vocabularyai.common.exception.ResourceNotFoundException;
import com.vocabularyai.quiz.dto.*;
import com.vocabularyai.quiz.entity.*;
import com.vocabularyai.quiz.entity.QuizVisibility;
import com.vocabularyai.quiz.repository.*;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.vocabulary.repository.CategoryRepository;
import com.vocabularyai.notification.service.NotificationService;
import com.vocabularyai.notification.entity.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Quiz Service
 * 
 * Business logic for quiz management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuizAttemptRepository attemptRepository;
    private final QuizAnswerRepository answerRepository;
    private final CategoryRepository categoryRepository;
    private final NotificationService notificationService;

    /**
     * Get all published quizzes
     */
    @Transactional(readOnly = true)
    public Page<QuizResponse> getPublishedQuizzes(Pageable pageable) {
        log.info("Getting published quizzes");
        Page<Quiz> quizzes = quizRepository.findByVisibilityAndStatus(QuizVisibility.PUBLIC, Quiz.QuizStatus.PUBLISHED,
                pageable);
        Map<Long, Integer> countMap = questionRepository.countQuestionsGroupByQuizId().stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Number) row[1]).intValue(),
                        (existing, replacement) -> existing));
        return new PageImpl<>(
                quizzes.getContent().stream()
                        .map(q -> {
                            QuizResponse resp = QuizResponse.fromEntity(q);
                            resp.setQuestionCount(countMap.getOrDefault(q.getId(), 0));
                            return resp;
                        })
                        .collect(Collectors.toList()),
                pageable,
                quizzes.getTotalElements());
    }

    /**
     * Get quizzes created by current user (My Quizzes)
     */
    @Transactional(readOnly = true)
    public Page<QuizResponse> getUserQuizzes(Long userId, Pageable pageable) {
        log.info("Getting quizzes created by user: {}", userId);
        Page<Quiz> quizzes = quizRepository.findByCreatedById(userId, pageable);
        Map<Long, Integer> countMap = questionRepository.countQuestionsGroupByQuizId().stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Number) row[1]).intValue(),
                        (existing, replacement) -> existing));
        return new PageImpl<>(
                quizzes.getContent().stream()
                        .map(q -> {
                            QuizResponse resp = QuizResponse.fromEntity(q);
                            resp.setQuestionCount(countMap.getOrDefault(q.getId(), 0));
                            return resp;
                        })
                        .collect(Collectors.toList()),
                pageable,
                quizzes.getTotalElements());
    }

    /**
     * Get quiz by ID
     */
    @Transactional(readOnly = true)
    public QuizResponse getQuizById(Long quizId) {
        log.info("Getting quiz with id: {}", quizId);
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> ResourceNotFoundException.of("Quiz", "id", quizId));
        QuizResponse resp = QuizResponse.fromEntity(quiz);
        resp.setQuestionCount((int) questionRepository.countByQuizId(quizId));
        return resp;
    }

    /**
     * Get all quizzes (Admin)
     */
    @Transactional(readOnly = true)
    public Page<QuizResponse> getAllQuizzes(Pageable pageable) {
        return getAllQuizzes(pageable, null);
    }

    @Transactional(readOnly = true)
    public Page<QuizResponse> getAllQuizzes(Pageable pageable, User.UserRole role) {
        log.info("Getting all quizzes (role={})", role);
        Page<Quiz> quizzes;
        if (role == User.UserRole.INSTRUCTOR) {
            quizzes = quizRepository.findByVisibilityIn(
                    java.util.List.of(QuizVisibility.PUBLIC, QuizVisibility.PENDING),
                    pageable);
        } else {
            quizzes = quizRepository.findAll(pageable);
        }

        Map<Long, Integer> countMap = questionRepository.countQuestionsGroupByQuizId().stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Number) row[1]).intValue(),
                        (existing, replacement) -> existing));
        return new PageImpl<>(
                quizzes.getContent().stream()
                        .map(q -> {
                            QuizResponse resp = QuizResponse.fromEntity(q);
                            resp.setQuestionCount(countMap.getOrDefault(q.getId(), 0));
                            return resp;
                        })
                        .collect(Collectors.toList()),
                pageable,
                quizzes.getTotalElements());
    }

    /**
     * Create new quiz (Admin)
     */
    @Transactional
    public QuizResponse createQuiz(CreateQuizRequest request) {
        log.info("Creating new quiz: {}", request.getTitle());

        Quiz quiz = Quiz.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .level(request.getLevel())
                .timeLimit(request.getTimeLimit() != null ? request.getTimeLimit() : 600)
                .status(Quiz.QuizStatus.PUBLISHED)
                .build();

        if (request.getCategoryId() != null) {
            var category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Category", "id", request.getCategoryId()));
            quiz.setCategory(category);
        }

        Quiz saved = quizRepository.save(quiz);
        log.info("Quiz created successfully with id: {}", saved.getId());

        return QuizResponse.fromEntity(saved);
    }

    /**
     * Update quiz (Admin)
     */
    @Transactional
    public QuizResponse updateQuiz(Long quizId, CreateQuizRequest request, String status) {
        log.info("Updating quiz id: {}", quizId);
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> ResourceNotFoundException.of("Quiz", "id", quizId));

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            quiz.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            quiz.setDescription(request.getDescription());
        }
        if (request.getLevel() != null) {
            quiz.setLevel(request.getLevel());
        }
        if (request.getTimeLimit() != null) {
            quiz.setTimeLimit(request.getTimeLimit());
        }
        if (status != null && !status.isBlank()) {
            try {
                quiz.setStatus(Quiz.QuizStatus.valueOf(status.toUpperCase()));
            } catch (Exception ignored) {
            }
        }
        quiz.setUpdatedAt(LocalDateTime.now());
        Quiz updated = quizRepository.save(quiz);
        return QuizResponse.fromEntity(updated);
    }

    /**
     * Delete quiz (Admin)
     */
    @Transactional
    public void deleteQuiz(Long quizId) {
        log.info("Deleting quiz: {}", quizId);
        if (!quizRepository.existsById(quizId)) {
            throw ResourceNotFoundException.of("Quiz", "id", quizId);
        }
        if (attemptRepository.existsByQuizId(quizId)) {
            throw new ConflictException("Bài đã có người làm, không thể xóa — hãy chuyển bài về ARCHIVED thay vì xóa");
        }
        questionRepository.deleteByQuizId(quizId);
        quizRepository.deleteById(quizId);
    }

    /**
     * Start quiz attempt
     */
    @Transactional
    public QuizAttemptResponse startQuizAttempt(User user, Long quizId) {
        log.info("User {} starting quiz {}", user.getId(), quizId);

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> ResourceNotFoundException.of("Quiz", "id", quizId));

        boolean isPublicAndPublished = quiz.getVisibility() == QuizVisibility.PUBLIC
                && quiz.getStatus() == Quiz.QuizStatus.PUBLISHED;
        boolean isOwner = quiz.getCreatedBy() != null && user != null
                && user.getId().equals(quiz.getCreatedBy().getId());
        boolean isStaff = user != null
                && (user.getRole() == User.UserRole.ADMIN || user.getRole() == User.UserRole.INSTRUCTOR);

        if (!isPublicAndPublished && !isOwner && !isStaff) {
            throw ResourceNotFoundException.of("Quiz", "id", quizId);
        }

        if (quiz.getStatus() != Quiz.QuizStatus.PUBLISHED && !isOwner && !isStaff) {
            throw new BadRequestException("Quiz is not available");
        }

        long questionCount = questionRepository.countByQuizId(quizId);
        if (questionCount == 0) {
            throw new BadRequestException("Quiz has no questions");
        }

        QuizAttempt attempt = QuizAttempt.builder()
                .user(user)
                .quiz(quiz)
                .startedAt(LocalDateTime.now())
                .totalQuestions((int) questionCount)
                .build();

        QuizAttempt saved = attemptRepository.save(attempt);
        log.info("Quiz attempt started with id: {}", saved.getId());

        return QuizAttemptResponse.fromEntity(saved);
    }

    /**
     * Submit quiz answers and calculate score
     */
    @Transactional
    public QuizAttemptResponse submitQuiz(Long attemptId, Long userId, SubmitQuizRequest request) {
        log.info("Submitting quiz attempt: {} for user: {}", attemptId, userId);

        QuizAttempt attempt = getAttemptAndVerifyOwnership(attemptId, userId);

        if (attempt.isCompleted()) {
            throw new BadRequestException("Quiz already submitted");
        }

        int correctCount = 0;

        // Process each answer
        for (SubmitQuizRequest.QuestionAnswer qa : request.getAnswers()) {
            Question question = questionRepository.findById(qa.getQuestionId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Question", "id", qa.getQuestionId()));

            boolean isCorrect = qa.getAnswer() != null && qa.getAnswer().equals(question.getCorrectAnswer());

            QuizAnswer answer = QuizAnswer.builder()
                    .attempt(attempt)
                    .question(question)
                    .answer(qa.getAnswer())
                    .correct(isCorrect)
                    .build();

            answerRepository.save(answer);

            if (isCorrect) {
                correctCount++;
            }
        }

        // Calculate score
        double score = (double) correctCount / attempt.getTotalQuestions() * 100;

        attempt.setCorrectAnswers(correctCount);
        attempt.setScore(score);
        attempt.setSubmittedAt(LocalDateTime.now());

        QuizAttempt saved = attemptRepository.save(attempt);
        log.info("Quiz submitted with score: {}", score);

        return QuizAttemptResponse.fromEntity(saved);
    }

    /**
     * Get quiz with questions (for taking quiz)
     */
    @Transactional(readOnly = true)
    public QuizWithQuestionsResponse getQuizForTaking(Long quizId) {
        return getQuizForTaking(quizId, null);
    }

    @Transactional(readOnly = true)
    public QuizWithQuestionsResponse getQuizForTaking(Long quizId, Long currentUserId) {
        log.info("Getting quiz for taking: {}, currentUserId: {}", quizId, currentUserId);

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> ResourceNotFoundException.of("Quiz", "id", quizId));

        boolean isPublicAndPublished = quiz.getVisibility() == QuizVisibility.PUBLIC
                && quiz.getStatus() == Quiz.QuizStatus.PUBLISHED;
        boolean isOwner = currentUserId != null && quiz.getCreatedBy() != null
                && currentUserId.equals(quiz.getCreatedBy().getId());
        boolean isStaff = isStaffUser();

        if (!isPublicAndPublished && !isOwner && !isStaff) {
            throw ResourceNotFoundException.of("Quiz", "id", quizId);
        }

        var questions = questionRepository.findByQuizId(quizId).stream()
                .map(q -> QuestionResponse.fromEntity(q, false))
                .collect(Collectors.toList());

        return QuizWithQuestionsResponse.builder()
                .quiz(QuizResponse.fromEntity(quiz))
                .questions(questions)
                .build();
    }

    /**
     * Get my quiz detail with correct answers (Owner only)
     */
    @Transactional(readOnly = true)
    public QuizWithQuestionsResponse getMyQuizDetail(Long quizId, Long userId) {
        log.info("Getting my quiz detail: quizId={}, userId={}", quizId, userId);
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> ResourceNotFoundException.of("Quiz", "id", quizId));

        if (quiz.getCreatedBy() == null || !userId.equals(quiz.getCreatedBy().getId())) {
            throw ResourceNotFoundException.of("Quiz", "id", quizId);
        }

        var questions = questionRepository.findByQuizId(quizId).stream()
                .map(q -> QuestionResponse.fromEntity(q, true))
                .collect(Collectors.toList());

        QuizResponse resp = QuizResponse.fromEntity(quiz);
        resp.setQuestionCount(questions.size());

        return QuizWithQuestionsResponse.builder()
                .quiz(resp)
                .questions(questions)
                .build();
    }

    /**
     * Update quiz created by user (Owner only, PRIVATE or REJECTED)
     */
    @Transactional
    public QuizResponse updateUserQuiz(Long quizId, Long userId, UpdateMyQuizRequest request) {
        log.info("User {} updating quiz {}", userId, quizId);
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> ResourceNotFoundException.of("Quiz", "id", quizId));

        if (quiz.getCreatedBy() == null || !userId.equals(quiz.getCreatedBy().getId())) {
            throw ResourceNotFoundException.of("Quiz", "id", quizId);
        }

        if (quiz.getVisibility() == QuizVisibility.PENDING) {
            throw new BadRequestException("Bài tập đang chờ duyệt, vui lòng rút yêu cầu trước khi chỉnh sửa");
        }
        if (quiz.getVisibility() == QuizVisibility.PUBLIC) {
            throw new BadRequestException("Bài tập đã công khai, không thể chỉnh sửa trực tiếp");
        }

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            quiz.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            quiz.setDescription(request.getDescription().trim());
        }
        if (request.getLevel() != null) {
            quiz.setLevel(request.getLevel());
        }
        if (request.getTimeLimit() != null && request.getTimeLimit() > 0) {
            int timeInSeconds = request.getTimeLimit() <= 60 ? request.getTimeLimit() * 60 : request.getTimeLimit();
            quiz.setTimeLimit(timeInSeconds);
        }

        if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            questionRepository.deleteByQuizId(quizId);

            for (UpdateMyQuizRequest.QuestionDto qDto : request.getQuestions()) {
                String optionsJson;
                try {
                    optionsJson = mapper.writeValueAsString(
                            qDto.getOptions() != null ? qDto.getOptions() : java.util.Collections.emptyList());
                } catch (Exception e) {
                    optionsJson = "[]";
                }

                Question question = Question.builder()
                        .quiz(quiz)
                        .content(qDto.getContent())
                        .options(optionsJson)
                        .correctAnswer(qDto.getCorrectAnswer() != null ? qDto.getCorrectAnswer() : 0)
                        .explanation(qDto.getExplanation())
                        .build();
                questionRepository.save(question);
            }
        }

        quiz.setUpdatedAt(LocalDateTime.now());
        Quiz saved = quizRepository.save(quiz);
        QuizResponse resp = QuizResponse.fromEntity(saved);
        resp.setQuestionCount((int) questionRepository.countByQuizId(quizId));
        return resp;
    }

    /**
     * Delete quiz created by user (Owner only, PRIVATE or REJECTED)
     */
    @Transactional
    public void deleteUserQuiz(Long quizId, Long userId) {
        log.info("User {} deleting quiz {}", userId, quizId);
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> ResourceNotFoundException.of("Quiz", "id", quizId));

        if (quiz.getCreatedBy() == null || !userId.equals(quiz.getCreatedBy().getId())) {
            throw ResourceNotFoundException.of("Quiz", "id", quizId);
        }

        if (quiz.getVisibility() == QuizVisibility.PENDING) {
            throw new BadRequestException("Bài tập đang chờ duyệt, vui lòng rút yêu cầu trước khi xóa");
        }
        if (quiz.getVisibility() == QuizVisibility.PUBLIC) {
            throw new BadRequestException("Bài tập đã công khai, không thể xóa trực tiếp");
        }

        // 1. Delete quiz attempt answers
        java.util.List<QuizAttempt> attempts = attemptRepository.findByQuizId(quizId, Pageable.unpaged()).getContent();
        for (QuizAttempt attempt : attempts) {
            java.util.List<QuizAnswer> answers = answerRepository.findByAttemptId(attempt.getId());
            answerRepository.deleteAll(answers);
        }
        // 2. Delete quiz attempts
        attemptRepository.deleteAll(attempts);

        // 3. Delete questions
        questionRepository.deleteByQuizId(quizId);

        // 4. Delete quiz
        quizRepository.delete(quiz);
        log.info("Deleted user quiz id: {}", quizId);
    }

    /**
     * Request publication of user quiz (PRIVATE/REJECTED -> PENDING)
     */
    @Transactional
    public QuizResponse publishRequest(Long quizId, Long userId) {
        log.info("User {} requesting publish for quiz {}", userId, quizId);
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> ResourceNotFoundException.of("Quiz", "id", quizId));

        if (quiz.getCreatedBy() == null || !userId.equals(quiz.getCreatedBy().getId())) {
            throw ResourceNotFoundException.of("Quiz", "id", quizId);
        }

        if (quiz.getVisibility() == QuizVisibility.PENDING) {
            throw new BadRequestException("Bài tập đang trong quá trình chờ duyệt");
        }
        if (quiz.getVisibility() == QuizVisibility.PUBLIC) {
            throw new BadRequestException("Bài tập đã được công khai trên hệ thống");
        }

        quiz.setVisibility(QuizVisibility.PENDING);
        quiz.setReviewNote(null);
        quiz.setUpdatedAt(LocalDateTime.now());
        Quiz saved = quizRepository.save(quiz);

        String authorName = (quiz.getCreatedBy().getUsername() != null) ? quiz.getCreatedBy().getUsername()
                : "học viên";
        notificationService.notifyInstructorsAndAdmins(
                "Bài tập mới chờ duyệt",
                "Học viên @" + authorName + " vừa gửi bài tập '" + quiz.getTitle() + "' chờ duyệt",
                NotificationType.EXERCISE_PENDING_REVIEW,
                "/admin/content-review?tab=exercise-review");

        QuizResponse resp = QuizResponse.fromEntity(saved);
        resp.setQuestionCount((int) questionRepository.countByQuizId(quizId));
        return resp;
    }

    /**
     * Withdraw publish request (PENDING -> PRIVATE)
     */
    @Transactional
    public QuizResponse withdrawPublishRequest(Long quizId, Long userId) {
        log.info("User {} withdrawing publish request for quiz {}", userId, quizId);
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> ResourceNotFoundException.of("Quiz", "id", quizId));

        if (quiz.getCreatedBy() == null || !userId.equals(quiz.getCreatedBy().getId())) {
            throw ResourceNotFoundException.of("Quiz", "id", quizId);
        }

        if (quiz.getVisibility() != QuizVisibility.PENDING) {
            throw new BadRequestException("Chỉ có thể rút yêu cầu khi bài tập đang ở trạng thái chờ duyệt");
        }

        quiz.setVisibility(QuizVisibility.PRIVATE);
        quiz.setUpdatedAt(LocalDateTime.now());
        Quiz saved = quizRepository.save(quiz);

        QuizResponse resp = QuizResponse.fromEntity(saved);
        resp.setQuestionCount((int) questionRepository.countByQuizId(quizId));
        return resp;
    }

    /**
     * Hide published quiz back to private (PUBLIC -> PRIVATE)
     */
    @Transactional
    public QuizResponse hideQuiz(Long quizId, Long userId) {
        log.info("User {} hiding quiz {}", userId, quizId);
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> ResourceNotFoundException.of("Quiz", "id", quizId));

        if (quiz.getCreatedBy() == null || !userId.equals(quiz.getCreatedBy().getId())) {
            throw ResourceNotFoundException.of("Quiz", "id", quizId);
        }

        if (quiz.getVisibility() != QuizVisibility.PUBLIC) {
            throw new BadRequestException("Chỉ có thể ẩn bài tập khi đang ở chế độ công khai");
        }

        quiz.setVisibility(QuizVisibility.PRIVATE);
        quiz.setUpdatedAt(LocalDateTime.now());
        Quiz saved = quizRepository.save(quiz);

        QuizResponse resp = QuizResponse.fromEntity(saved);
        resp.setQuestionCount((int) questionRepository.countByQuizId(quizId));
        return resp;
    }

    /**
     * Get quiz attempt with answers
     */
    @Transactional(readOnly = true)
    public QuizAttemptWithAnswersResponse getQuizAttemptWithAnswers(Long attemptId, Long userId) {
        log.info("Getting quiz attempt: {} for user: {}", attemptId, userId);

        QuizAttempt attempt = getAttemptAndVerifyOwnership(attemptId, userId);

        if (!attempt.isCompleted()) {
            throw new BadRequestException("Quiz attempt not completed");
        }

        var answers = answerRepository.findByAttemptId(attemptId).stream()
                .map(qa -> QuizAnswerDetailResponse.fromEntity(qa))
                .collect(Collectors.toList());

        return QuizAttemptWithAnswersResponse.builder()
                .attempt(QuizAttemptResponse.fromEntity(attempt))
                .answers(answers)
                .build();
    }

    /**
     * Get quiz attempt with answers (public version - no userId check)
     * Used internally for public endpoints
     */
    @Transactional(readOnly = true)
    public QuizAttemptWithAnswersResponse getQuizAttemptWithAnswers(Long attemptId) {
        log.info("Getting quiz attempt: {}", attemptId);

        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> ResourceNotFoundException.of("QuizAttempt", "id", attemptId));

        if (!attempt.isCompleted()) {
            throw new BadRequestException("Quiz attempt not completed");
        }

        var answers = answerRepository.findByAttemptId(attemptId).stream()
                .map(qa -> QuizAnswerDetailResponse.fromEntity(qa))
                .collect(Collectors.toList());

        return QuizAttemptWithAnswersResponse.builder()
                .attempt(QuizAttemptResponse.fromEntity(attempt))
                .answers(answers)
                .build();
    }

    /**
     * Get user's quiz history
     */
    @Transactional(readOnly = true)
    public Page<QuizAttemptResponse> getUserQuizHistory(Long userId, Pageable pageable) {
        log.info("Getting quiz history for user: {}", userId);

        Page<QuizAttempt> attempts = attemptRepository.findByUserIdAndSubmittedAtIsNotNull(userId, pageable);
        return attempts.map(QuizAttemptResponse::fromEntity);
    }

    /**
     * Get quiz attempt and verify ownership (IDOR prevention)
     * 
     * ✅ TASK 9: Fixed HTTP status from 400 to 403
     * Throws ForbiddenException instead of BadRequestException
     * to return proper 403 Forbidden status code
     */
    private QuizAttempt getAttemptAndVerifyOwnership(Long attemptId, Long userId) {
        return attemptRepository.findByIdAndUserId(attemptId, userId)
                .orElseThrow(() -> {
                    log.warn("IDOR prevention: Unauthorized access attempt: attemptId={}, userId={}", attemptId,
                            userId);
                    return new ForbiddenException("You don't have permission to access this quiz attempt");
                });
    }

    private boolean isStaffUser() {
        try {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                return auth.getAuthorities().stream()
                        .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())
                                || "ROLE_INSTRUCTOR".equals(a.getAuthority()));
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}
