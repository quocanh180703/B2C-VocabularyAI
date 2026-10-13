package com.vocabularyai.admin.service;

import com.vocabularyai.admin.dto.request.CreateQuizAttemptRequest;
import com.vocabularyai.admin.dto.request.UpdateQuizAttemptRequest;
import com.vocabularyai.admin.dto.response.QuizAttemptDetailResponse;
import com.vocabularyai.admin.dto.response.QuizAttemptListResponse;
import com.vocabularyai.common.exception.ResourceNotFoundException;
import com.vocabularyai.quiz.entity.Quiz;
import com.vocabularyai.quiz.entity.QuizAttempt;
import com.vocabularyai.quiz.repository.QuizAttemptRepository;
import com.vocabularyai.quiz.repository.QuizRepository;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminQuizAttemptServiceImpl implements AdminQuizAttemptService {
    
    private final QuizAttemptRepository attemptRepository;
    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    
    @Override
    public Page<QuizAttemptListResponse> listAttempts(Pageable pageable) {
        log.debug("Listing all quiz attempts with pagination: {}", pageable);
        return attemptRepository.findAll(pageable).map(this::toListResponse);
    }
    
    @Override
    public QuizAttemptDetailResponse getAttemptDetail(Long attemptId) {
        log.debug("Getting quiz attempt detail for ID: {}", attemptId);
        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz attempt not found: " + attemptId));
        
        return toDetailResponse(attempt);
    }
    
    @Override
    @Transactional
    public QuizAttemptDetailResponse createAttempt(CreateQuizAttemptRequest request) {
        log.debug("Creating new quiz attempt for user: {} quiz: {}", request.getUserId(), request.getQuizId());
        
        // Verify user exists
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getUserId()));
        
        // Verify quiz exists
        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found: " + request.getQuizId()));
        
        QuizAttempt attempt = QuizAttempt.builder()
                .user(user)
                .quiz(quiz)
                .score(request.getScore() != null ? request.getScore() : 0.0)
                .totalQuestions(request.getTotalQuestions() != null ? request.getTotalQuestions() : 0)
                .correctAnswers(request.getCorrectAnswers() != null ? request.getCorrectAnswers() : 0)
                .startedAt(request.getStartedAt())
                .submittedAt(request.getSubmittedAt())
                .build();
        
        QuizAttempt saved = attemptRepository.save(attempt);
        log.info("Created new quiz attempt with ID: {}", saved.getId());
        
        return toDetailResponse(saved);
    }
    
    @Override
    @Transactional
    public QuizAttemptDetailResponse updateAttempt(Long attemptId, UpdateQuizAttemptRequest request) {
        log.debug("Updating quiz attempt with ID: {}", attemptId);
        
        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz attempt not found: " + attemptId));
        
        // Update fields
        if (request.getScore() != null) {
            attempt.setScore(request.getScore());
        }
        if (request.getTotalQuestions() != null) {
            attempt.setTotalQuestions(request.getTotalQuestions());
        }
        if (request.getCorrectAnswers() != null) {
            attempt.setCorrectAnswers(request.getCorrectAnswers());
        }
        if (request.getSubmittedAt() != null) {
            attempt.setSubmittedAt(request.getSubmittedAt());
        }
        
        QuizAttempt updated = attemptRepository.save(attempt);
        log.info("Updated quiz attempt with ID: {}", updated.getId());
        
        return toDetailResponse(updated);
    }
    
    @Override
    @Transactional
    public void deleteAttempt(Long attemptId) {
        log.debug("Deleting quiz attempt with ID: {}", attemptId);
        
        if (!attemptRepository.existsById(attemptId)) {
            throw new ResourceNotFoundException("Quiz attempt not found: " + attemptId);
        }
        
        attemptRepository.deleteById(attemptId);
        log.info("Deleted quiz attempt with ID: {}", attemptId);
    }
    
    // Helper methods
    
    private QuizAttemptDetailResponse toDetailResponse(QuizAttempt attempt) {
        Long timeTaken = null;
        if (attempt.getSubmittedAt() != null) {
            timeTaken = ChronoUnit.SECONDS.between(attempt.getStartedAt(), attempt.getSubmittedAt());
        }
        
        return QuizAttemptDetailResponse.builder()
                .id(attempt.getId())
                .userId(attempt.getUser().getId())
                .userEmail(attempt.getUser().getEmail())
                .quizId(attempt.getQuiz().getId())
                .quizTitle(attempt.getQuiz().getTitle())
                .score(attempt.getScore())
                .totalQuestions(attempt.getTotalQuestions())
                .correctAnswers(attempt.getCorrectAnswers())
                .timeTakenSeconds(timeTaken)
                .isCompleted(attempt.isCompleted())
                .startedAt(attempt.getStartedAt())
                .submittedAt(attempt.getSubmittedAt())
                .createdAt(attempt.getCreatedAt())
                .build();
    }
    
    private QuizAttemptListResponse toListResponse(QuizAttempt attempt) {
        return QuizAttemptListResponse.builder()
                .id(attempt.getId())
                .userId(attempt.getUser().getId())
                .userEmail(attempt.getUser().getEmail())
                .quizId(attempt.getQuiz().getId())
                .quizTitle(attempt.getQuiz().getTitle())
                .score(attempt.getScore())
                .totalQuestions(attempt.getTotalQuestions())
                .correctAnswers(attempt.getCorrectAnswers())
                .isCompleted(attempt.isCompleted())
                .submittedAt(attempt.getSubmittedAt())
                .createdAt(attempt.getCreatedAt())
                .build();
    }
}
