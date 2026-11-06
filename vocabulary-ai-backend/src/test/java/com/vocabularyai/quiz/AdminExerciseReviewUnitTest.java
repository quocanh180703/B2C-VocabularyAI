package com.vocabularyai.quiz;

import com.vocabularyai.common.exception.BadRequestException;
import com.vocabularyai.common.exception.ResourceNotFoundException;
import com.vocabularyai.notification.entity.NotificationType;
import com.vocabularyai.notification.service.NotificationService;
import com.vocabularyai.quiz.dto.QuizResponse;
import com.vocabularyai.quiz.dto.QuizWithQuestionsResponse;
import com.vocabularyai.quiz.dto.ReviewExerciseRequest;
import com.vocabularyai.quiz.entity.Question;
import com.vocabularyai.quiz.entity.Quiz;
import com.vocabularyai.quiz.entity.QuizVisibility;
import com.vocabularyai.quiz.repository.QuestionRepository;
import com.vocabularyai.quiz.repository.QuizRepository;
import com.vocabularyai.quiz.service.ExerciseReviewServiceImpl;
import com.vocabularyai.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminExerciseReviewUnitTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ExerciseReviewServiceImpl exerciseReviewService;

    private User student;
    private User instructor;
    private Quiz pendingQuiz;
    private Quiz publicQuiz;
    private Question question;

    @BeforeEach
    void setUp() {
        student = User.builder().id(10L).username("student1").role(User.UserRole.STUDENT).build();
        instructor = User.builder().id(20L).username("instructor1").role(User.UserRole.INSTRUCTOR).build();

        pendingQuiz = Quiz.builder()
                .id(100L)
                .title("Kiểm tra từ vựng AI")
                .description("Bài tập thử nghiệm")
                .level(Quiz.QuizLevel.B1)
                .status(Quiz.QuizStatus.DRAFT)
                .visibility(QuizVisibility.PENDING)
                .isAiGenerated(true)
                .createdBy(student)
                .build();

        publicQuiz = Quiz.builder()
                .id(200L)
                .title("Bài tập công khai")
                .visibility(QuizVisibility.PUBLIC)
                .status(Quiz.QuizStatus.PUBLISHED)
                .createdBy(student)
                .build();

        question = Question.builder()
                .id(1L)
                .quiz(pendingQuiz)
                .content("Từ nào đồng nghĩa với happy?")
                .options("[\"sad\", \"joyful\", \"angry\", \"tired\"]")
                .correctAnswer(1)
                .explanation("Joyful = happy")
                .build();
    }

    @Test
    @DisplayName("getPendingQuizzes returns page of quizzes with visibility PENDING")
    void testGetPendingQuizzes() {
        Pageable pageable = PageRequest.of(0, 10);
        when(quizRepository.findByVisibility(QuizVisibility.PENDING, pageable))
                .thenReturn(new PageImpl<>(List.of(pendingQuiz), pageable, 1));
        when(questionRepository.countQuestionsGroupByQuizId())
                .thenReturn(Collections.singletonList(new Object[]{100L, 5L}));

        Page<QuizResponse> page = exerciseReviewService.getPendingQuizzes(pageable);

        assertNotNull(page);
        assertEquals(1, page.getContent().size());
        assertEquals("Kiểm tra từ vựng AI", page.getContent().get(0).getTitle());
        assertEquals("PENDING", page.getContent().get(0).getVisibility());
        assertEquals(5, page.getContent().get(0).getQuestionCount());
    }

    @Test
    @DisplayName("getPendingQuizDetail returns quiz with questions including answers and explanations")
    void testGetPendingQuizDetail() {
        when(quizRepository.findById(100L)).thenReturn(Optional.of(pendingQuiz));
        when(questionRepository.findByQuizId(100L)).thenReturn(List.of(question));

        QuizWithQuestionsResponse response = exerciseReviewService.getPendingQuizDetail(100L);

        assertNotNull(response);
        assertEquals("Kiểm tra từ vựng AI", response.getQuiz().getTitle());
        assertEquals(1, response.getQuestions().size());
        assertEquals(Integer.valueOf(1), response.getQuestions().get(0).getCorrectAnswer());
        assertEquals("Joyful = happy", response.getQuestions().get(0).getExplanation());
    }

    @Test
    @DisplayName("getPendingQuizDetail throws ResourceNotFoundException if quiz is not PENDING")
    void testGetPendingQuizDetailNotPending() {
        when(quizRepository.findById(200L)).thenReturn(Optional.of(publicQuiz));

        assertThrows(ResourceNotFoundException.class, () -> exerciseReviewService.getPendingQuizDetail(200L));
    }

    @Test
    @DisplayName("reviewQuiz APPROVED makes quiz PUBLIC and sends notification")
    void testReviewQuizApproved() {
        when(quizRepository.findById(100L)).thenReturn(Optional.of(pendingQuiz));
        when(quizRepository.save(any(Quiz.class))).thenAnswer(inv -> inv.getArgument(0));
        when(questionRepository.countByQuizId(100L)).thenReturn(1L);

        ReviewExerciseRequest req = ReviewExerciseRequest.builder()
                .decision("APPROVED")
                .build();

        QuizResponse result = exerciseReviewService.reviewQuiz(100L, instructor, req);

        assertEquals("PUBLIC", result.getVisibility());
        assertEquals(Quiz.QuizStatus.PUBLISHED, pendingQuiz.getStatus());
        assertEquals(instructor, pendingQuiz.getReviewedBy());
        assertNotNull(pendingQuiz.getReviewedAt());
        assertNull(pendingQuiz.getReviewNote());

        verify(notificationService).createNotification(
                eq(student.getId()),
                eq("Bài tập đã được phê duyệt!"),
                contains("Kiểm tra từ vựng AI"),
                eq(NotificationType.EXERCISE_APPROVED),
                eq("/quiz/100")
        );
    }

    @Test
    @DisplayName("reviewQuiz REJECTED requires note and sets visibility REJECTED")
    void testReviewQuizRejected() {
        when(quizRepository.findById(100L)).thenReturn(Optional.of(pendingQuiz));
        when(quizRepository.save(any(Quiz.class))).thenAnswer(inv -> inv.getArgument(0));
        when(questionRepository.countByQuizId(100L)).thenReturn(1L);

        ReviewExerciseRequest req = ReviewExerciseRequest.builder()
                .decision("REJECTED")
                .note("Nhiều câu sai chính tả, vui lòng xem lại")
                .build();

        QuizResponse result = exerciseReviewService.reviewQuiz(100L, instructor, req);

        assertEquals("REJECTED", result.getVisibility());
        assertEquals("Nhiều câu sai chính tả, vui lòng xem lại", pendingQuiz.getReviewNote());
        assertEquals(instructor, pendingQuiz.getReviewedBy());

        verify(notificationService).createNotification(
                eq(student.getId()),
                eq("Bài tập bị từ chối duyệt"),
                contains("Nhiều câu sai chính tả"),
                eq(NotificationType.EXERCISE_REJECTED),
                eq("/quizzes?tab=my-quizzes")
        );
    }

    @Test
    @DisplayName("reviewQuiz REJECTED without note throws BadRequestException")
    void testReviewQuizRejectedEmptyNote() {
        when(quizRepository.findById(100L)).thenReturn(Optional.of(pendingQuiz));

        ReviewExerciseRequest req = ReviewExerciseRequest.builder()
                .decision("REJECTED")
                .note("   ")
                .build();

        assertThrows(BadRequestException.class, () -> exerciseReviewService.reviewQuiz(100L, instructor, req));
    }

    @Test
    @DisplayName("reviewQuiz on non-PENDING quiz throws BadRequestException")
    void testReviewQuizNonPending() {
        when(quizRepository.findById(200L)).thenReturn(Optional.of(publicQuiz));

        ReviewExerciseRequest req = ReviewExerciseRequest.builder()
                .decision("APPROVED")
                .build();

        assertThrows(BadRequestException.class, () -> exerciseReviewService.reviewQuiz(200L, instructor, req));
    }
}
