package com.vocabularyai.quiz.service;

import com.vocabularyai.common.exception.BadRequestException;
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
    public QuizResponse getQuizById(Long quizId) {
        log.info("Getting quiz with id: {}", quizId);
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> ResourceNotFoundException.of("Quiz", "id", quizId));
        return QuizResponse.fromEntity(quiz);
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
            .timeLimit(request.getTimeLimit())
            .status(Quiz.QuizStatus.DRAFT)
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
    public QuizAttemptResponse submitQuiz(Long attemptId, SubmitQuizRequest request) {
        log.info("Submitting quiz attempt: {}", attemptId);
        
        QuizAttempt attempt = attemptRepository.findById(attemptId)
            .orElseThrow(() -> ResourceNotFoundException.of("QuizAttempt", "id", attemptId));
        
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
}
