package com.vocabularyai.personalization.service;

import com.vocabularyai.ai.repository.GrammarPracticeHistoryRepository;
import com.vocabularyai.learning.entity.LearningProgress;
import com.vocabularyai.learning.repository.LearningHistoryRepository;
import com.vocabularyai.learning.repository.LearningProgressRepository;
import com.vocabularyai.listening.repository.ListeningExerciseRepository;
import com.vocabularyai.personalization.dto.LearningProfileDto;
import com.vocabularyai.placement.entity.PlacementAttempt;
import com.vocabularyai.placement.repository.PlacementAttemptRepository;
import com.vocabularyai.quiz.repository.QuizAttemptRepository;
import com.vocabularyai.speaking.repository.SpeakingSessionRepository;
import com.vocabularyai.speaking.service.SpeakingProgressService;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LearningProfileService Unit Tests")
public class LearningProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private LearningProgressRepository learningProgressRepository;

    @Mock
    private LearningHistoryRepository learningHistoryRepository;

    @Mock
    private SpeakingSessionRepository speakingSessionRepository;

    @Mock
    private SpeakingProgressService speakingProgressService;

    @Mock
    private PlacementAttemptRepository placementAttemptRepository;

    @Mock
    private GrammarPracticeHistoryRepository grammarPracticeHistoryRepository;

    @Mock
    private ListeningExerciseRepository listeningExerciseRepository;

    @Mock
    private QuizAttemptRepository quizAttemptRepository;

    @InjectMocks
    private LearningProfileServiceImpl learningProfileService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(10L);
        testUser.setEmail("learner@example.com");

        lenient().when(userRepository.findById(10L)).thenReturn(Optional.of(testUser));
        lenient().when(learningProgressRepository.findByUserId(eq(10L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        lenient().when(speakingSessionRepository.findByUserId(eq(10L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        lenient().when(speakingSessionRepository.findByUserIdAndCreatedAtBetween(eq(10L), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        lenient().when(placementAttemptRepository.findTopByUserId(10L))
                .thenReturn(Optional.empty());
    }

    @Test
    @DisplayName("Empty account has 0 activities across all 5 skills and defaults to A1")
    void testEmptyAccountDefaultsToA1() {
        when(grammarPracticeHistoryRepository.countByUserId(10L)).thenReturn(0L);
        when(listeningExerciseRepository.countCompletedByUserId(10L)).thenReturn(0L);
        when(quizAttemptRepository.countCompletedByUser(10L)).thenReturn(0L);

        LearningProfileDto profile = learningProfileService.getLearningProfile(10L);

        assertNotNull(profile);
        assertEquals(0.0, profile.getOverallScore());
        assertEquals("A1", profile.getCurrentLevel());
        assertNotNull(profile.getVocabulary());
        assertNotNull(profile.getGrammar());
        assertNotNull(profile.getSpeaking());
        assertNotNull(profile.getListening());
        assertNotNull(profile.getQuiz());
        assertEquals(0, profile.getVocabulary().getTotalActivities());
        assertEquals(0, profile.getGrammar().getTotalActivities());
        assertEquals(0, profile.getListening().getTotalActivities());
        assertEquals(0, profile.getQuiz().getTotalActivities());
    }

    @Test
    @DisplayName("Grammar is measured independently from GrammarPracticeHistory and not equal to vocab * 0.9")
    void testGrammarMeasuredIndependently() {
        // User has 10 grammar practices with average score 80.0
        when(grammarPracticeHistoryRepository.countByUserId(10L)).thenReturn(10L);
        when(grammarPracticeHistoryRepository.getAverageScoreByUserId(10L)).thenReturn(80.0);
        when(grammarPracticeHistoryRepository.getMaxScoreByUserId(10L)).thenReturn(95.0);
        when(grammarPracticeHistoryRepository.getAverageScoresByMode(10L)).thenReturn(List.of(
                new Object[]{"FREE", 85.0},
                new Object[]{"TRANSLATION", 70.0}
        ));

        // Vocab has 0 activities
        when(listeningExerciseRepository.countCompletedByUserId(10L)).thenReturn(0L);
        when(quizAttemptRepository.countCompletedByUser(10L)).thenReturn(0L);

        LearningProfileDto profile = learningProfileService.getLearningProfile(10L);

        assertNotNull(profile);
        assertNotNull(profile.getGrammar());
        assertEquals(10, profile.getGrammar().getTotalActivities());
        // 10 activities: volumeFactor = 0.75 -> weighted = 80.0 * 0.75 = 60.0
        assertEquals(60.0, profile.getGrammar().getScore(), 0.01);
        assertEquals(95.0, profile.getGrammar().getBestScore(), 0.01);
        // Recommended focus points to the mode with lowest score (TRANSLATION -> Luyện dịch câu)
        assertTrue(profile.getGrammar().getRecommendedFocus().contains("Luyện dịch câu"));

        // Vocab is 0, so grammar definitely does NOT equal vocab * 0.9
        assertEquals(0.0, profile.getVocabulary().getScore());
        assertNotEquals(profile.getVocabulary().getScore() * 0.9, profile.getGrammar().getScore());
    }

    @Test
    @DisplayName("Listening only counts COMPLETED exercises and applies volume factor")
    void testListeningOnlyCompletedWithVolumeFactor() {
        // User has 3 completed listening exercises with average score 90.0
        when(listeningExerciseRepository.countCompletedByUserId(10L)).thenReturn(3L);
        when(listeningExerciseRepository.getAverageScoreByUserId(10L)).thenReturn(90.0);
        when(listeningExerciseRepository.getMaxScoreByUserId(10L)).thenReturn(100.0);

        when(grammarPracticeHistoryRepository.countByUserId(10L)).thenReturn(0L);
        when(quizAttemptRepository.countCompletedByUser(10L)).thenReturn(0L);

        LearningProfileDto profile = learningProfileService.getLearningProfile(10L);

        assertNotNull(profile.getListening());
        assertEquals(3, profile.getListening().getTotalActivities());
        // 3 activities (< 5): volumeFactor = 0.50 -> weighted = 90.0 * 0.50 = 45.0
        assertEquals(45.0, profile.getListening().getScore(), 0.01);
        assertEquals(100.0, profile.getListening().getBestScore(), 0.01);
    }

    @Test
    @DisplayName("Dynamic normalization when only Quiz is active (100% weight to Quiz)")
    void testDynamicNormalization_OnlyQuizActive() {
        // Only 1 completed quiz with score 100
        when(quizAttemptRepository.countCompletedByUser(10L)).thenReturn(1L);
        when(quizAttemptRepository.getAverageScoreByUserId(10L)).thenReturn(100.0);
        when(quizAttemptRepository.getMaxScoreByUserId(10L)).thenReturn(100.0);

        when(grammarPracticeHistoryRepository.countByUserId(10L)).thenReturn(0L);
        when(listeningExerciseRepository.countCompletedByUserId(10L)).thenReturn(0L);

        LearningProfileDto profile = learningProfileService.getLearningProfile(10L);

        // 1 activity (< 5): volumeFactor = 0.50 -> weighted = 100 * 0.50 = 50.0
        assertEquals(50.0, profile.getQuiz().getScore(), 0.01);
        // Only quiz active -> overall score is 100% quiz = 50.0
        assertEquals(50.0, profile.getOverallScore(), 0.01);
        // Attempted words = 0 (< 15) -> max ceiling is A2
        assertEquals("A2", profile.getCurrentLevel());
    }

    @Test
    @DisplayName("Dynamic normalization with 4 active components matches plan example 1")
    void testDynamicNormalization_MatchesPlanExample1() {
        // Vocab: 100 words practiced, accuracy 80.0
        // Volume factor (100 words: 50 <= x < 150): 0.65, maturity 1.0 -> weighted = 80 * 0.65 = 52.0
        // But let's mock learningProgress with 100 items of 80% accuracy
        LearningProgress mockProgress = new LearningProgress();
        mockProgress.setCorrectCount(8);
        mockProgress.setWrongCount(2);
        mockProgress.setReviewCount(1);
        List<LearningProgress> list100 = Collections.nCopies(100, mockProgress);
        when(learningProgressRepository.findByUserId(eq(10L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(list100));

        // Grammar: count = 10 (volumeFactor 0.75), avg = 70.0 -> weighted = 70.0 * 0.75 = 52.5
        when(grammarPracticeHistoryRepository.countByUserId(10L)).thenReturn(10L);
        when(grammarPracticeHistoryRepository.getAverageScoreByUserId(10L)).thenReturn(70.0);

        // Speaking: 0 activities
        // Listening: count = 20 (>= 15 -> volumeFactor 1.0), avg = 60.0 -> weighted = 60.0
        when(listeningExerciseRepository.countCompletedByUserId(10L)).thenReturn(20L);
        when(listeningExerciseRepository.getAverageScoreByUserId(10L)).thenReturn(60.0);

        // Quiz: count = 3 (< 5 -> volumeFactor 0.5), avg = 90.0 -> weighted = 45.0
        when(quizAttemptRepository.countCompletedByUser(10L)).thenReturn(3L);
        when(quizAttemptRepository.getAverageScoreByUserId(10L)).thenReturn(90.0);

        LearningProfileDto profile = learningProfileService.getLearningProfile(10L);

        // Active weights: Vocab (0.25), Grammar (0.20), Listening (0.15), Quiz (0.15) -> Total = 0.75
        // Normalized weights: 0.25/0.75 = 1/3, 0.20/0.75 = 4/15, 0.15/0.75 = 0.20, 0.15/0.75 = 0.20
        // Expected overall = (vocabScore * 1/3) + (grammarScore * 4/15) + (listeningScore * 0.20) + (quizScore * 0.20)
        double expectedVocab = 80.0 * 0.65 * 0.75; // 100 items, totalLearningDays = 0 (maturityFactor = 0.75)
        double expectedGrammar = 70.0 * 0.75; // 52.5
        double expectedListening = 60.0 * 1.00; // 60.0
        double expectedQuiz = 90.0 * 0.50; // 45.0

        double expectedOverall = (expectedVocab * (0.25 / 0.75))
                + (expectedGrammar * (0.20 / 0.75))
                + (expectedListening * (0.15 / 0.75))
                + (expectedQuiz * (0.15 / 0.75));

        assertEquals(expectedOverall, profile.getOverallScore(), 0.1);
        assertNotNull(profile.getCurrentLevel());
    }

    @Test
    @DisplayName("Placement test floor is respected even if calculated score is lower")
    void testPlacementFloorRespected() {
        // Placement test result was B2
        PlacementAttempt placement = new PlacementAttempt();
        placement.setLevelResult("B2");
        when(placementAttemptRepository.findTopByUserId(10L)).thenReturn(Optional.of(placement));

        // User has only 1 quiz with score 20 (which calculates to A1)
        when(quizAttemptRepository.countCompletedByUser(10L)).thenReturn(1L);
        when(quizAttemptRepository.getAverageScoreByUserId(10L)).thenReturn(20.0);
        when(grammarPracticeHistoryRepository.countByUserId(10L)).thenReturn(0L);
        when(listeningExerciseRepository.countCompletedByUserId(10L)).thenReturn(0L);

        LearningProfileDto profile = learningProfileService.getLearningProfile(10L);

        // Effective level must be B2 due to placement floor
        assertEquals("B2", profile.getCurrentLevel());
    }
}
