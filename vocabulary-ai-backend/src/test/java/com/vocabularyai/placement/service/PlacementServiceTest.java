package com.vocabularyai.placement.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabularyai.placement.dto.PlacementQuestionResponse;
import com.vocabularyai.placement.dto.PlacementStatusResponse;
import com.vocabularyai.placement.dto.PlacementSubmitRequest;
import com.vocabularyai.placement.dto.PlacementSubmitResponse;
import com.vocabularyai.placement.entity.PlacementAttempt;
import com.vocabularyai.placement.repository.PlacementAttemptRepository;
import com.vocabularyai.quiz.entity.Question;
import com.vocabularyai.quiz.entity.Quiz;
import com.vocabularyai.quiz.repository.QuestionRepository;
import com.vocabularyai.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlacementService Unit Tests")
class PlacementServiceTest {

    @Mock
    private PlacementAttemptRepository placementAttemptRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private PlacementServiceImpl placementService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id(100L)
            .email("testuser@vocabularyai.com")
            .username("testuser")
            .build();
    }

    @Test
    @DisplayName("Should return completed=false when user has no prior attempt")
    void getStatus_NoPriorAttempt_ReturnsNotCompleted() {
        when(placementAttemptRepository.findTopByUserId(100L)).thenReturn(Optional.empty());
        when(placementAttemptRepository.countByUserId(100L)).thenReturn(0L);

        PlacementStatusResponse status = placementService.getStatus(100L);

        assertNotNull(status);
        assertFalse(status.isCompleted());
        assertNull(status.getPlacementLevel());
        assertEquals(0, status.getAttemptCount());
    }

    @Test
    @DisplayName("Should return completed=true and placementLevel when user has prior attempt")
    void getStatus_HasPriorAttempt_ReturnsCompleted() {
        PlacementAttempt attempt = PlacementAttempt.builder()
            .id(1L)
            .user(testUser)
            .levelResult("B1")
            .scoreByLevel("{\"A1\":4,\"A2\":4,\"B1\":3,\"B2\":1,\"C1\":0}")
            .build();

        when(placementAttemptRepository.findTopByUserId(100L)).thenReturn(Optional.of(attempt));
        when(placementAttemptRepository.countByUserId(100L)).thenReturn(2L);

        PlacementStatusResponse status = placementService.getStatus(100L);

        assertNotNull(status);
        assertTrue(status.isCompleted());
        assertEquals("B1", status.getPlacementLevel());
        assertEquals(2, status.getAttemptCount());
        assertEquals(4, status.getScoreByLevel().get("A1"));
    }

    @Test
    @DisplayName("Should generate 20 questions in ascending CEFR difficulty order (A1 to C1)")
    void generateTest_ReturnsQuestionsOrderedByLevel() {
        List<Question> mockQuestions = new ArrayList<>();
        long idCounter = 1;
        for (Quiz.QuizLevel lvl : List.of(Quiz.QuizLevel.A1, Quiz.QuizLevel.A2, Quiz.QuizLevel.B1, Quiz.QuizLevel.B2, Quiz.QuizLevel.C1)) {
            Quiz quiz = Quiz.builder().id(idCounter).level(lvl).status(Quiz.QuizStatus.PUBLISHED).build();
            for (int i = 0; i < 6; i++) {
                Question q = Question.builder()
                    .id(idCounter * 100 + i)
                    .quiz(quiz)
                    .content("Question " + lvl + " #" + i)
                    .options("[\"A\",\"B\",\"C\",\"D\"]")
                    .correctAnswer(0)
                    .build();
                mockQuestions.add(q);
            }
            idCounter++;
        }

        when(questionRepository.findAllPublishedQuestions()).thenReturn(mockQuestions);

        List<PlacementQuestionResponse> questions = placementService.generateTest();

        assertEquals(20, questions.size());

        // Verify ordering: 4 A1, then 4 A2, then 4 B1, then 4 B2, then 4 C1
        for (int i = 0; i < 4; i++) assertEquals("A1", questions.get(i).getLevel());
        for (int i = 4; i < 8; i++) assertEquals("A2", questions.get(i).getLevel());
        for (int i = 8; i < 12; i++) assertEquals("B1", questions.get(i).getLevel());
        for (int i = 12; i < 16; i++) assertEquals("B2", questions.get(i).getLevel());
        for (int i = 16; i < 20; i++) assertEquals("C1", questions.get(i).getLevel());
    }

    @Test
    @DisplayName("Should evaluate B1 when A1, A2, B1 have >= 3/4 and B2 has < 3/4")
    void submitTest_AchievesB1_ContinuousMastery() {
        Map<Long, Question> mockQuestions = new HashMap<>();
        List<PlacementSubmitRequest.AnswerItem> answers = new ArrayList<>();

        long id = 1;
        for (Quiz.QuizLevel lvl : List.of(Quiz.QuizLevel.A1, Quiz.QuizLevel.A2, Quiz.QuizLevel.B1, Quiz.QuizLevel.B2, Quiz.QuizLevel.C1)) {
            Quiz quiz = Quiz.builder().level(lvl).build();
            for (int i = 0; i < 4; i++) {
                Question q = Question.builder().id(id).quiz(quiz).correctAnswer(0).build();
                mockQuestions.put(id, q);

                // Give 4/4 for A1, 4/4 for A2, 3/4 for B1, 1/4 for B2, 0/4 for C1
                int chosen = 1; // wrong
                if (lvl == Quiz.QuizLevel.A1 || lvl == Quiz.QuizLevel.A2) {
                    chosen = 0; // all 4 right
                } else if (lvl == Quiz.QuizLevel.B1 && i < 3) {
                    chosen = 0; // 3 right
                } else if (lvl == Quiz.QuizLevel.B2 && i == 0) {
                    chosen = 0; // 1 right
                }

                answers.add(new PlacementSubmitRequest.AnswerItem(id, chosen));
                id++;
            }
        }

        when(questionRepository.findAllById(any())).thenReturn(new ArrayList<>(mockQuestions.values()));

        PlacementSubmitResponse response = placementService.submitTest(testUser, new PlacementSubmitRequest(answers));

        assertEquals("B1", response.getLevel());
        assertEquals(4, response.getScoreByLevel().get("A1"));
        assertEquals(4, response.getScoreByLevel().get("A2"));
        assertEquals(3, response.getScoreByLevel().get("B1"));
        assertEquals(1, response.getScoreByLevel().get("B2"));
        assertEquals(0, response.getScoreByLevel().get("C1"));

        ArgumentCaptor<PlacementAttempt> captor = ArgumentCaptor.forClass(PlacementAttempt.class);
        verify(placementAttemptRepository).save(captor.capture());
        assertEquals("B1", captor.getValue().getLevelResult());
    }

    @Test
    @DisplayName("Should stop at A1 if A2 fails even if C1 is guessed correctly (Anti-guessing gate)")
    void submitTest_FailsA2WithHighC1_StopsAtA1() {
        Map<Long, Question> mockQuestions = new HashMap<>();
        List<PlacementSubmitRequest.AnswerItem> answers = new ArrayList<>();

        long id = 1;
        for (Quiz.QuizLevel lvl : List.of(Quiz.QuizLevel.A1, Quiz.QuizLevel.A2, Quiz.QuizLevel.B1, Quiz.QuizLevel.B2, Quiz.QuizLevel.C1)) {
            Quiz quiz = Quiz.builder().level(lvl).build();
            for (int i = 0; i < 4; i++) {
                Question q = Question.builder().id(id).quiz(quiz).correctAnswer(0).build();
                mockQuestions.put(id, q);

                // A1: 4 right, A2: 1 right (fail), B1: 4 right, C1: 4 right
                int chosen = 1;
                if (lvl == Quiz.QuizLevel.A1) {
                    chosen = 0;
                } else if (lvl == Quiz.QuizLevel.A2 && i == 0) {
                    chosen = 0;
                } else if (lvl == Quiz.QuizLevel.B1 || lvl == Quiz.QuizLevel.C1) {
                    chosen = 0;
                }

                answers.add(new PlacementSubmitRequest.AnswerItem(id, chosen));
                id++;
            }
        }

        when(questionRepository.findAllById(any())).thenReturn(new ArrayList<>(mockQuestions.values()));

        PlacementSubmitResponse response = placementService.submitTest(testUser, new PlacementSubmitRequest(answers));

        // Because A2 was failed (< 3), continuous mastery stops at A1!
        assertEquals("A1", response.getLevel());
        assertEquals(4, response.getScoreByLevel().get("A1"));
        assertEquals(1, response.getScoreByLevel().get("A2"));
        assertEquals(4, response.getScoreByLevel().get("C1"));
    }
}
