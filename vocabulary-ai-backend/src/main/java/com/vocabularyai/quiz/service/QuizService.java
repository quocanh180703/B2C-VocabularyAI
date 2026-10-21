package com.vocabularyai.quiz.service;

import com.vocabularyai.common.exception.BadRequestException;
import com.vocabularyai.common.exception.ForbiddenException;
import com.vocabularyai.common.exception.ResourceNotFoundException;
import com.vocabularyai.quiz.dto.*;
import com.vocabularyai.quiz.entity.*;
import com.vocabularyai.quiz.repository.*;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.vocabulary.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    
    /**
     * Get all published quizzes
     */
    @Transactional(readOnly = true)
    public Page<QuizResponse> getPublishedQuizzes(Pageable pageable) {
        log.info("Getting published quizzes");
        Page<Quiz> quizzes = quizRepository.findByStatus(Quiz.QuizStatus.PUBLISHED, pageable);
        return new PageImpl<>(
            quizzes.getContent().stream()
                .map(QuizResponse::fromEntity)
                .collect(Collectors.toList()),
            pageable,
            quizzes.getTotalElements()
        );
    }
    
    /**
     * Get quiz by ID
     */
    @Transactional(readOnly = true)
    public QuizResponse getQuizById(Long quizId) {
        log.info("Getting quiz with id: {}", quizId);
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> ResourceNotFoundException.of("Quiz", "id", quizId));
        return QuizResponse.fromEntity(quiz);
    }
    
    /**
     * Get all quizzes (Admin)
     */
    @Transactional(readOnly = true)
    public Page<QuizResponse> getAllQuizzes(Pageable pageable) {
        log.info("Getting all quizzes (Admin)");
        Page<Quiz> quizzes = quizRepository.findAll(pageable);
        return new PageImpl<>(
            quizzes.getContent().stream()
                .map(QuizResponse::fromEntity)
                .collect(Collectors.toList()),
            pageable,
            quizzes.getTotalElements()
        );
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
            } catch (Exception ignored) {}
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
        
        if (quiz.getStatus() != Quiz.QuizStatus.PUBLISHED) {
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
        log.info("Getting quiz for taking: {}", quizId);
        
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> ResourceNotFoundException.of("Quiz", "id", quizId));
        
        if (quiz.getStatus() != Quiz.QuizStatus.PUBLISHED) {
            throw new BadRequestException("Quiz is not available");
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
        
        Page<QuizAttempt> attempts = attemptRepository.findByUserId(userId, pageable);
        return new PageImpl<>(
            attempts.getContent().stream()
                .map(QuizAttemptResponse::fromEntity)
                .collect(Collectors.toList()),
            pageable,
            attempts.getTotalElements()
        );
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
                log.warn("IDOR prevention: Unauthorized access attempt: attemptId={}, userId={}", attemptId, userId);
                return new ForbiddenException("You don't have permission to access this quiz attempt");
            });
    }
}
