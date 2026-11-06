package com.vocabularyai.quiz;

import com.vocabularyai.common.exception.BadRequestException;
import com.vocabularyai.common.exception.ConflictException;
import com.vocabularyai.common.exception.ForbiddenException;
import com.vocabularyai.common.exception.ResourceNotFoundException;
import com.vocabularyai.quiz.dto.QuestionResponse;
import com.vocabularyai.quiz.dto.QuizResponse;
import com.vocabularyai.quiz.dto.QuizWithQuestionsResponse;
import com.vocabularyai.quiz.dto.UpdateMyQuizRequest;
import com.vocabularyai.quiz.entity.*;
import com.vocabularyai.quiz.repository.QuestionRepository;
import com.vocabularyai.quiz.repository.QuizAnswerRepository;
import com.vocabularyai.quiz.repository.QuizAttemptRepository;
import com.vocabularyai.quiz.repository.QuizRepository;
import com.vocabularyai.quiz.service.QuizService;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.vocabulary.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.vocabularyai.notification.service.NotificationService;
import com.vocabularyai.notification.entity.NotificationType;

@ExtendWith(MockitoExtension.class)
public class UserQuizMineUnitTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuizAttemptRepository attemptRepository;

    @Mock
    private QuizAnswerRepository answerRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private com.vocabularyai.notification.service.NotificationService notificationService;

    @InjectMocks
    private QuizService quizService;

    private User owner;
    private User otherUser;
    private Quiz privateQuiz;
    private Quiz publicQuiz;
    private Quiz pendingQuiz;
    private Question question1;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(10L)
                .username("quizcreator")
                .role(User.UserRole.STUDENT)
                .build();

        otherUser = User.builder()
                .id(20L)
                .username("otherstudent")
                .role(User.UserRole.STUDENT)
                .build();

        privateQuiz = Quiz.builder()
                .id(100L)
                .title("My Private English Quiz")
                .level(Quiz.QuizLevel.B1)
                .timeLimit(600)
                .status(Quiz.QuizStatus.PUBLISHED)
                .visibility(QuizVisibility.PRIVATE)
                .createdBy(owner)
                .isAiGenerated(true)
                .build();

        publicQuiz = Quiz.builder()
                .id(200L)
                .title("Public Grammar Quiz")
                .level(Quiz.QuizLevel.B2)
                .timeLimit(900)
                .status(Quiz.QuizStatus.PUBLISHED)
                .visibility(QuizVisibility.PUBLIC)
                .createdBy(owner)
                .build();

        pendingQuiz = Quiz.builder()
                .id(300L)
                .title("Pending Review Quiz")
                .level(Quiz.QuizLevel.C1)
                .timeLimit(600)
                .status(Quiz.QuizStatus.PUBLISHED)
                .visibility(QuizVisibility.PENDING)
                .createdBy(owner)
                .build();

        question1 = Question.builder()
                .id(1L)
                .quiz(privateQuiz)
                .content("What is the meaning of 'serendipity'?")
                .options("[\"Lucky discovery\",\"Bad luck\",\"Confusion\",\"Sadness\"]")
                .correctAnswer(0)
                .explanation("Serendipity means finding something good without looking for it.")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Owner can view their own private quiz for taking (answers are hidden)")
    void testOwnerCanViewPrivateQuizForTaking() {
        when(quizRepository.findById(100L)).thenReturn(Optional.of(privateQuiz));
        when(questionRepository.findByQuizId(100L)).thenReturn(List.of(question1));

        QuizWithQuestionsResponse res = quizService.getQuizForTaking(100L, owner.getId());

        assertNotNull(res);
        assertEquals("My Private English Quiz", res.getQuiz().getTitle());
        assertEquals(1, res.getQuestions().size());
        QuestionResponse qResp = res.getQuestions().get(0);
        // Correct answer and explanation must be hidden for taking
        assertNull(qResp.getCorrectAnswer());
        assertNull(qResp.getExplanation());
    }

    @Test
    @DisplayName("Other user viewing private quiz throws ResourceNotFoundException (404)")
    void testOtherUserCannotViewPrivateQuiz() {
        when(quizRepository.findById(100L)).thenReturn(Optional.of(privateQuiz));

        assertThrows(ResourceNotFoundException.class, () -> {
            quizService.getQuizForTaking(100L, otherUser.getId());
        });
    }

    @Test
    @DisplayName("Any user can view a PUBLIC and PUBLISHED quiz")
    void testAnyUserCanViewPublicQuiz() {
        when(quizRepository.findById(200L)).thenReturn(Optional.of(publicQuiz));
        when(questionRepository.findByQuizId(200L)).thenReturn(List.of(question1));

        QuizWithQuestionsResponse res = quizService.getQuizForTaking(200L, otherUser.getId());
        assertNotNull(res);
        assertEquals("Public Grammar Quiz", res.getQuiz().getTitle());
    }

    @Test
    @DisplayName("Owner can start attempt on their private quiz")
    void testOwnerCanStartQuizAttempt() {
        when(quizRepository.findById(100L)).thenReturn(Optional.of(privateQuiz));
        when(questionRepository.countByQuizId(100L)).thenReturn(1L);
        when(attemptRepository.save(any(QuizAttempt.class))).thenAnswer(i -> {
            QuizAttempt qa = i.getArgument(0);
            qa.setId(500L);
            return qa;
        });

        var res = quizService.startQuizAttempt(owner, 100L);
        assertNotNull(res);
        assertEquals(500L, res.getId());
    }

    @Test
    @DisplayName("Other user starting attempt on private quiz throws 404")
    void testOtherUserCannotStartAttemptOnPrivateQuiz() {
        when(quizRepository.findById(100L)).thenReturn(Optional.of(privateQuiz));

        assertThrows(ResourceNotFoundException.class, () -> {
            quizService.startQuizAttempt(otherUser, 100L);
        });
    }

    @Test
    @DisplayName("Owner viewing detail via getMyQuizDetail gets answers and explanations")
    void testOwnerCanViewDetailWithAnswers() {
        when(quizRepository.findById(100L)).thenReturn(Optional.of(privateQuiz));
        when(questionRepository.findByQuizId(100L)).thenReturn(List.of(question1));

        QuizWithQuestionsResponse res = quizService.getMyQuizDetail(100L, owner.getId());
        assertNotNull(res);
        assertEquals(1, res.getQuestions().size());
        QuestionResponse q = res.getQuestions().get(0);
        assertEquals(0, q.getCorrectAnswer());
        assertEquals("Serendipity means finding something good without looking for it.", q.getExplanation());
    }

    @Test
    @DisplayName("Non-owner viewing getMyQuizDetail throws 404")
    void testNonOwnerCannotViewMyQuizDetail() {
        when(quizRepository.findById(100L)).thenReturn(Optional.of(privateQuiz));

        assertThrows(ResourceNotFoundException.class, () -> {
            quizService.getMyQuizDetail(100L, otherUser.getId());
        });
    }

    @Test
    @DisplayName("Owner can update private quiz")
    void testOwnerCanUpdatePrivateQuiz() {
        when(quizRepository.findById(100L)).thenReturn(Optional.of(privateQuiz));
        when(quizRepository.save(any(Quiz.class))).thenAnswer(i -> i.getArgument(0));

        UpdateMyQuizRequest req = UpdateMyQuizRequest.builder()
                .title("Updated Title")
                .description("New Description")
                .level(Quiz.QuizLevel.B2)
                .timeLimit(15) // 15 mins -> 900 seconds
                .questions(List.of(
                        UpdateMyQuizRequest.QuestionDto.builder()
                                .content("New question?")
                                .options(List.of("A", "B", "C", "D"))
                                .correctAnswer(1)
                                .explanation("Because B")
                                .build()
                ))
                .build();

        QuizResponse resp = quizService.updateUserQuiz(100L, owner.getId(), req);

        assertEquals("Updated Title", resp.getTitle());
        assertEquals("New Description", resp.getDescription());
        assertEquals("B2", resp.getLevel());
        verify(questionRepository).deleteByQuizId(100L);
        verify(questionRepository).save(any(Question.class));
    }

    @Test
    @DisplayName("Updating a PENDING quiz throws BadRequestException")
    void testUpdatingPendingQuizThrowsBadRequest() {
        when(quizRepository.findById(300L)).thenReturn(Optional.of(pendingQuiz));

        UpdateMyQuizRequest req = UpdateMyQuizRequest.builder().title("Try Edit").build();

        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            quizService.updateUserQuiz(300L, owner.getId(), req);
        });
        assertTrue(ex.getMessage().contains("chờ duyệt"));
    }

    @Test
    @DisplayName("Updating a PUBLIC quiz throws BadRequestException")
    void testUpdatingPublicQuizThrowsBadRequest() {
        when(quizRepository.findById(200L)).thenReturn(Optional.of(publicQuiz));

        UpdateMyQuizRequest req = UpdateMyQuizRequest.builder().title("Try Edit").build();

        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            quizService.updateUserQuiz(200L, owner.getId(), req);
        });
        assertTrue(ex.getMessage().contains("công khai"));
    }

    @Test
    @DisplayName("Non-owner updating quiz throws ForbiddenException")
    void testNonOwnerUpdatingQuizThrowsForbidden() {
        when(quizRepository.findById(100L)).thenReturn(Optional.of(privateQuiz));

        UpdateMyQuizRequest req = UpdateMyQuizRequest.builder().title("Try Edit").build();

        assertThrows(ForbiddenException.class, () -> {
            quizService.updateUserQuiz(100L, otherUser.getId(), req);
        });
    }

    @Test
    @DisplayName("Owner can delete private quiz with attempts and questions")
    void testOwnerCanDeletePrivateQuiz() {
        when(quizRepository.findById(100L)).thenReturn(Optional.of(privateQuiz));
        when(attemptRepository.findByQuizId(eq(100L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        quizService.deleteUserQuiz(100L, owner.getId());

        verify(questionRepository).deleteByQuizId(100L);
        verify(quizRepository).delete(privateQuiz);
    }

    @Test
    @DisplayName("Deleting a PENDING quiz throws BadRequestException")
    void testDeletingPendingQuizThrowsBadRequest() {
        when(quizRepository.findById(300L)).thenReturn(Optional.of(pendingQuiz));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            quizService.deleteUserQuiz(300L, owner.getId());
        });
        assertTrue(ex.getMessage().contains("chờ duyệt"));
    }

    @Test
    @DisplayName("publishRequest succeeds for PRIVATE quiz and sends notifications")
    void testPublishRequestSuccess() {
        when(quizRepository.findById(100L)).thenReturn(Optional.of(privateQuiz));
        when(quizRepository.save(any(Quiz.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(questionRepository.countByQuizId(100L)).thenReturn(1L);

        QuizResponse res = quizService.publishRequest(100L, owner.getId());

        assertEquals("PENDING", res.getVisibility());
        verify(notificationService).notifyInstructorsAndAdmins(
                eq("Bài tập mới chờ duyệt"),
                contains("quizcreator"),
                eq(NotificationType.EXERCISE_PENDING_REVIEW),
                contains("tab=exercise-review")
        );
    }

    @Test
    @DisplayName("publishRequest fails if quiz already PENDING or PUBLIC")
    void testPublishRequestAlreadyPendingOrPublic() {
        when(quizRepository.findById(300L)).thenReturn(Optional.of(pendingQuiz));
        assertThrows(BadRequestException.class, () -> quizService.publishRequest(300L, owner.getId()));

        when(quizRepository.findById(200L)).thenReturn(Optional.of(publicQuiz));
        assertThrows(BadRequestException.class, () -> quizService.publishRequest(200L, owner.getId()));
    }

    @Test
    @DisplayName("withdrawPublishRequest succeeds for PENDING quiz")
    void testWithdrawPublishRequestSuccess() {
        when(quizRepository.findById(300L)).thenReturn(Optional.of(pendingQuiz));
        when(quizRepository.save(any(Quiz.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(questionRepository.countByQuizId(300L)).thenReturn(1L);

        QuizResponse res = quizService.withdrawPublishRequest(300L, owner.getId());

        assertEquals("PRIVATE", res.getVisibility());
        verify(quizRepository).save(pendingQuiz);
    }

    @Test
    @DisplayName("withdrawPublishRequest fails for non-PENDING quiz")
    void testWithdrawPublishRequestFailsNonPending() {
        when(quizRepository.findById(100L)).thenReturn(Optional.of(privateQuiz));
        assertThrows(BadRequestException.class, () -> quizService.withdrawPublishRequest(100L, owner.getId()));
    }

    @Test
    @DisplayName("hideQuiz succeeds for PUBLIC quiz")
    void testHideQuizSuccess() {
        when(quizRepository.findById(200L)).thenReturn(Optional.of(publicQuiz));
        when(quizRepository.save(any(Quiz.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(questionRepository.countByQuizId(200L)).thenReturn(1L);

        QuizResponse res = quizService.hideQuiz(200L, owner.getId());

        assertEquals("PRIVATE", res.getVisibility());
        verify(quizRepository).save(publicQuiz);
    }

    @Test
    @DisplayName("hideQuiz fails for non-PUBLIC quiz")
    void testHideQuizFailsNonPublic() {
        when(quizRepository.findById(100L)).thenReturn(Optional.of(privateQuiz));
        assertThrows(BadRequestException.class, () -> quizService.hideQuiz(100L, owner.getId()));
    }


    @Test
    @DisplayName("deleteQuiz throws ConflictException when quiz has attempts")
    void testDeleteQuizThrowsConflictWhenAttemptsExist() {
        when(quizRepository.existsById(100L)).thenReturn(true);
        when(attemptRepository.existsByQuizId(100L)).thenReturn(true);

        ConflictException ex = assertThrows(ConflictException.class, () -> quizService.deleteQuiz(100L));
        assertTrue(ex.getMessage().contains("Bài đã có người làm"));
        verify(quizRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("deleteQuiz succeeds when quiz has no attempts")
    void testDeleteQuizSucceedsWhenNoAttempts() {
        when(quizRepository.existsById(100L)).thenReturn(true);
        when(attemptRepository.existsByQuizId(100L)).thenReturn(false);

        quizService.deleteQuiz(100L);

        verify(questionRepository).deleteByQuizId(100L);
        verify(quizRepository).deleteById(100L);
    }

    @Test
    @DisplayName("getAllQuizzes for INSTRUCTOR filters to PUBLIC and PENDING only")
    void testGetAllQuizzesFiltersForInstructor() {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        when(quizRepository.findByVisibilityIn(eq(java.util.List.of(QuizVisibility.PUBLIC, QuizVisibility.PENDING)), eq(pageable)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(java.util.List.of(publicQuiz, pendingQuiz), pageable, 2));

        var res = quizService.getAllQuizzes(pageable, User.UserRole.INSTRUCTOR);

        assertEquals(2, res.getContent().size());
        verify(quizRepository).findByVisibilityIn(any(), eq(pageable));
        verify(quizRepository, never()).findAll(eq(pageable));
    }

}
