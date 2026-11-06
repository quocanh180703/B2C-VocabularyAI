package com.vocabularyai.quiz.service;

import com.vocabularyai.common.exception.BadRequestException;
import com.vocabularyai.common.exception.ResourceNotFoundException;
import com.vocabularyai.notification.entity.NotificationType;
import com.vocabularyai.notification.service.NotificationService;
import com.vocabularyai.quiz.dto.QuestionResponse;
import com.vocabularyai.quiz.dto.QuizResponse;
import com.vocabularyai.quiz.dto.QuizWithQuestionsResponse;
import com.vocabularyai.quiz.dto.ReviewExerciseRequest;
import com.vocabularyai.quiz.entity.Quiz;
import com.vocabularyai.quiz.entity.QuizVisibility;
import com.vocabularyai.quiz.repository.QuestionRepository;
import com.vocabularyai.quiz.repository.QuizRepository;
import com.vocabularyai.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExerciseReviewServiceImpl implements ExerciseReviewService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public Page<QuizResponse> getPendingQuizzes(Pageable pageable) {
        log.info("Fetching quizzes pending review");
        Page<Quiz> page = quizRepository.findByVisibility(QuizVisibility.PENDING, pageable);

        Map<Long, Integer> countMap = questionRepository.countQuestionsGroupByQuizId().stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Number) row[1]).intValue(),
                        (existing, replacement) -> existing
                ));

        List<QuizResponse> dtos = page.getContent().stream()
                .map(q -> {
                    QuizResponse dto = QuizResponse.fromEntity(q);
                    dto.setQuestionCount(countMap.getOrDefault(q.getId(), 0));
                    return dto;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public QuizWithQuestionsResponse getPendingQuizDetail(Long quizId) {
        log.info("Fetching pending quiz detail for review: {}", quizId);
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> ResourceNotFoundException.of("Quiz", "id", quizId));

        if (quiz.getVisibility() != QuizVisibility.PENDING) {
            throw ResourceNotFoundException.of("Quiz", "id", quizId);
        }

        List<QuestionResponse> questions = questionRepository.findByQuizId(quizId).stream()
                .map(q -> QuestionResponse.fromEntity(q, true))
                .collect(Collectors.toList());

        QuizResponse quizDto = QuizResponse.fromEntity(quiz);
        quizDto.setQuestionCount(questions.size());

        return QuizWithQuestionsResponse.builder()
                .quiz(quizDto)
                .questions(questions)
                .build();
    }

    @Override
    @Transactional
    public QuizResponse reviewQuiz(Long quizId, User reviewer, ReviewExerciseRequest request) {
        log.info("Instructor/Admin {} reviewing quiz {}: decision={}", reviewer.getId(), quizId, request.getDecision());

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> ResourceNotFoundException.of("Quiz", "id", quizId));

        if (quiz.getVisibility() != QuizVisibility.PENDING) {
            throw new BadRequestException("Bài tập này hiện không ở trạng thái chờ duyệt (trạng thái: " + quiz.getVisibility() + ")");
        }

        String decision = request.getDecision().trim().toUpperCase();

        if ("APPROVED".equals(decision)) {
            quiz.setVisibility(QuizVisibility.PUBLIC);
            quiz.setStatus(Quiz.QuizStatus.PUBLISHED);
            quiz.setReviewedBy(reviewer);
            quiz.setReviewedAt(LocalDateTime.now());
            quiz.setReviewNote(null);
            quiz.setUpdatedAt(LocalDateTime.now());

            Quiz saved = quizRepository.save(quiz);

            if (quiz.getCreatedBy() != null) {
                notificationService.createNotification(
                        quiz.getCreatedBy().getId(),
                        "Bài tập đã được phê duyệt!",
                        "Bài tập '" + quiz.getTitle() + "' của bạn đã được duyệt và công khai cho cộng đồng.",
                        NotificationType.EXERCISE_APPROVED,
                        "/quiz/" + quiz.getId()
                );
            }

            QuizResponse dto = QuizResponse.fromEntity(saved);
            dto.setQuestionCount((int) questionRepository.countByQuizId(quizId));
            return dto;

        } else if ("REJECTED".equals(decision)) {
            String note = request.getNote() != null ? request.getNote().trim() : "";
            if (note.isEmpty()) {
                throw new BadRequestException("Vui lòng cung cấp lý do từ chối bài tập");
            }

            quiz.setVisibility(QuizVisibility.REJECTED);
            quiz.setReviewedBy(reviewer);
            quiz.setReviewedAt(LocalDateTime.now());
            quiz.setReviewNote(note);
            quiz.setUpdatedAt(LocalDateTime.now());

            Quiz saved = quizRepository.save(quiz);

            if (quiz.getCreatedBy() != null) {
                notificationService.createNotification(
                        quiz.getCreatedBy().getId(),
                        "Bài tập bị từ chối duyệt",
                        "Bài tập '" + quiz.getTitle() + "' bị từ chối với lý do: " + note,
                        NotificationType.EXERCISE_REJECTED,
                        "/quizzes?tab=my-quizzes"
                );
            }

            QuizResponse dto = QuizResponse.fromEntity(saved);
            dto.setQuestionCount((int) questionRepository.countByQuizId(quizId));
            return dto;
        } else {
            throw new BadRequestException("Quyết định không hợp lệ: " + decision);
        }
    }
}
