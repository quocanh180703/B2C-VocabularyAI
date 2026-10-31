package com.vocabularyai.ai.service.chat;

import com.vocabularyai.ai.dto.UserSnapshotDto;
import com.vocabularyai.learning.entity.LearningProgress;
import com.vocabularyai.learning.repository.LearningHistoryRepository;
import com.vocabularyai.learning.repository.LearningProgressRepository;
import com.vocabularyai.payment.dto.SubscriptionUsageResponse;
import com.vocabularyai.payment.entity.Package;
import com.vocabularyai.payment.entity.Subscription;
import com.vocabularyai.payment.repository.PackageRepository;
import com.vocabularyai.payment.repository.SubscriptionRepository;
import com.vocabularyai.payment.service.FeatureUsageService;
import com.vocabularyai.personalization.service.LearningProfileService;
import com.vocabularyai.placement.entity.PlacementAttempt;
import com.vocabularyai.placement.repository.PlacementAttemptRepository;
import com.vocabularyai.quiz.entity.QuizAttempt;
import com.vocabularyai.quiz.repository.QuizAttemptRepository;
import com.vocabularyai.speaking.entity.SpeakingSession;
import com.vocabularyai.speaking.entity.SpeakingStatus;
import com.vocabularyai.speaking.repository.SpeakingSessionRepository;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.repository.UserRepository;
import com.vocabularyai.vocabulary.repository.FavoriteWordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserSnapshotService Unit Tests")
class UserSnapshotServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private LearningProfileService learningProfileService;
    @Mock
    private PlacementAttemptRepository placementAttemptRepository;
    @Mock
    private LearningHistoryRepository learningHistoryRepository;
    @Mock
    private LearningProgressRepository learningProgressRepository;
    @Mock
    private FavoriteWordRepository favoriteWordRepository;
    @Mock
    private SpeakingSessionRepository speakingSessionRepository;
    @Mock
    private QuizAttemptRepository quizAttemptRepository;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private PackageRepository packageRepository;
    @Mock
    private FeatureUsageService featureUsageService;

    @InjectMocks
    private UserSnapshotServiceImpl userSnapshotService;

    private UserSnapshotFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new UserSnapshotFormatter();
    }

    @Test
    @DisplayName("Ca 1: New user (0 history, 0 progress) - Snapshot returns 0s/nulls safely without crashing")
    void testNewUser_ReturnsSafeDefaultSnapshot() {
        Long userId = 99L;
        User user = User.builder()
                .id(userId)
                .username("newbie")
                .fullName("New Learner")
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(learningProfileService.estimateCurrentLevel(userId)).thenReturn("A1");
        when(placementAttemptRepository.findTopByUserId(userId)).thenReturn(Optional.empty());
        when(learningProfileService.calculateCurrentStreak(userId)).thenReturn(0);
        when(learningProfileService.calculateLongestStreak(userId)).thenReturn(0);
        when(learningHistoryRepository.findActivityDatesByUserId(userId)).thenReturn(Collections.emptyList());
        when(learningHistoryRepository.countByUserId(userId)).thenReturn(0L);
        when(learningHistoryRepository.countTodayActivities(eq(userId), any())).thenReturn(0L);

        when(learningProgressRepository.countByUserIdAndStatus(userId, LearningProgress.LearningStatus.LEARNING)).thenReturn(0L);
        when(learningProgressRepository.countByUserIdAndStatus(userId, LearningProgress.LearningStatus.MASTERED)).thenReturn(0L);
        when(learningProgressRepository.countByUserIdAndNextReviewAtLessThanEqual(eq(userId), any())).thenReturn(0L);
        when(learningProgressRepository.countOverdueReviews(eq(userId), any())).thenReturn(0L);
        when(favoriteWordRepository.countByUserId(userId)).thenReturn(0L);

        when(speakingSessionRepository.countByUserIdAndStatus(userId, SpeakingStatus.COMPLETED)).thenReturn(0L);
        when(quizAttemptRepository.countCompletedByUser(userId)).thenReturn(0L);
        when(subscriptionRepository.findByUserId(userId)).thenReturn(Optional.empty());

        UserSnapshotDto snapshot = userSnapshotService.buildSnapshot(userId);

        assertNotNull(snapshot);
        assertEquals(userId, snapshot.getUserId());
        assertEquals("New Learner", snapshot.getDisplayName());
        assertEquals("A1", snapshot.getEffectiveLevel());
        assertNull(snapshot.getPlacementLevel());
        assertEquals(0, snapshot.getCurrentStreak());
        assertEquals(0L, snapshot.getWordsLearned());
        assertEquals(0L, snapshot.getSpeakingSessionsCount());
        assertEquals(0L, snapshot.getQuizzesCount());
        assertEquals("FREE", snapshot.getSubscriptionPlan());
        assertFalse(snapshot.getIsVip());

        // Test Formatter
        String formatted = formatter.format(snapshot);
        assertNotNull(formatted);
        assertTrue(formatted.contains("=== USER DATA — VERIFIED ==="));
        assertTrue(formatted.contains("Chưa làm bài kiểm tra đầu vào"));
        assertTrue(formatted.contains("0 từ"));
    }

    @Test
    @DisplayName("Ca 2: Active user with sample data - Accurate statistics in snapshot")
    void testActiveUser_ReturnsAccurateAggregateStats() {
        Long userId = 10L;
        User user = User.builder()
                .id(userId)
                .username("hoang")
                .fullName("Quốc Hoàng")
                .createdAt(LocalDateTime.now().minusDays(20))
                .build();

        PlacementAttempt placement = PlacementAttempt.builder()
                .id(1L)
                .levelResult("B1")
                .completedAt(LocalDateTime.now().minusDays(15))
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(learningProfileService.estimateCurrentLevel(userId)).thenReturn("A2");
        when(placementAttemptRepository.findTopByUserId(userId)).thenReturn(Optional.of(placement));
        when(learningProfileService.calculateCurrentStreak(userId)).thenReturn(5);
        when(learningProfileService.calculateLongestStreak(userId)).thenReturn(12);

        java.sql.Date d1 = java.sql.Date.valueOf(LocalDate.now());
        java.sql.Date d2 = java.sql.Date.valueOf(LocalDate.now().minusDays(1));
        when(learningHistoryRepository.findActivityDatesByUserId(userId)).thenReturn(List.of(d1, d2));
        when(learningHistoryRepository.countByUserId(userId)).thenReturn(85L);
        when(learningHistoryRepository.countTodayActivities(eq(userId), any())).thenReturn(8L);

        when(learningProgressRepository.countByUserIdAndStatus(userId, LearningProgress.LearningStatus.LEARNING)).thenReturn(10L);
        when(learningProgressRepository.countByUserIdAndStatus(userId, LearningProgress.LearningStatus.MASTERED)).thenReturn(5L);
        when(learningProgressRepository.countByUserIdAndNextReviewAtLessThanEqual(eq(userId), any())).thenReturn(3L);
        when(learningProgressRepository.countOverdueReviews(eq(userId), any())).thenReturn(1L);
        when(favoriteWordRepository.countByUserId(userId)).thenReturn(7L);

        List<Object[]> forgottenRows = new ArrayList<>();
        forgottenRows.add(new Object[]{"resilient", 4});
        forgottenRows.add(new Object[]{"persevere", 2});
        when(learningProgressRepository.findTopForgottenWords(eq(userId), any(Pageable.class))).thenReturn(forgottenRows);

        when(learningHistoryRepository.countActivitiesSince(eq(userId), any())).thenReturn(25L);
        when(learningHistoryRepository.countActivitiesBetween(eq(userId), any(), any())).thenReturn(18L);
        when(learningHistoryRepository.getAccuracyRateSince(eq(userId), any())).thenReturn(82.5);

        when(speakingSessionRepository.countByUserIdAndStatus(userId, SpeakingStatus.COMPLETED)).thenReturn(4L);
        when(speakingSessionRepository.findMaxOverallScoreByUserId(userId)).thenReturn(88.0);
        SpeakingSession latestSpeaking = SpeakingSession.builder()
                .overallScore(74.5)
                .completedAt(LocalDateTime.now().minusDays(1))
                .build();
        when(speakingSessionRepository.findLatestCompletedSession(userId)).thenReturn(Optional.of(latestSpeaking));

        when(quizAttemptRepository.countCompletedByUser(userId)).thenReturn(6L);
        when(quizAttemptRepository.getAverageScoreByUserId(userId)).thenReturn(79.0);
        QuizAttempt latestQuiz = QuizAttempt.builder()
                .score(85.0)
                .submittedAt(LocalDateTime.now())
                .build();
        when(quizAttemptRepository.findLatestCompletedAttempt(userId)).thenReturn(Optional.of(latestQuiz));

        Subscription sub = Subscription.builder()
                .status("ACTIVE")
                .packageId(2L)
                .startAt(LocalDateTime.now().minusDays(10))
                .endAt(LocalDateTime.now().plusDays(20))
                .build();
        when(subscriptionRepository.findByUserId(userId)).thenReturn(Optional.of(sub));
        Package pkg = Package.builder().id(2L).code("PREMIUM_MONTHLY").name("Premium Monthly").build();
        when(packageRepository.findById(2L)).thenReturn(Optional.of(pkg));

        Map<String, SubscriptionUsageResponse.FeatureQuotaDto> features = new HashMap<>();
        features.put("AI_CHAT", new SubscriptionUsageResponse.FeatureQuotaDto(null, 2, null));
        SubscriptionUsageResponse usage = SubscriptionUsageResponse.builder()
                .isVip(true)
                .features(features)
                .build();
        when(featureUsageService.getUsage(userId)).thenReturn(usage);

        UserSnapshotDto snapshot = userSnapshotService.buildSnapshot(userId);

        assertNotNull(snapshot);
        assertEquals("Quốc Hoàng", snapshot.getDisplayName());
        assertEquals("B1", snapshot.getEffectiveLevel()); // max("A2", "B1") = "B1"
        assertEquals("B1", snapshot.getPlacementLevel());
        assertEquals("A2", snapshot.getEstimatedLevel());
        assertEquals(5, snapshot.getCurrentStreak());
        assertEquals(12, snapshot.getLongestStreak());
        assertEquals(2, snapshot.getActiveLearningDays());
        assertEquals(15L, snapshot.getWordsLearned()); // 10 + 5
        assertEquals(10L, snapshot.getWordsLearning());
        assertEquals(5L, snapshot.getWordsMastered());
        assertEquals(3L, snapshot.getReviewsDueToday());
        assertEquals(1L, snapshot.getReviewsOverdue());
        assertEquals(2, snapshot.getTopForgottenWords().size());
        assertEquals("resilient", snapshot.getTopForgottenWords().get(0).getWord());
        assertEquals(4, snapshot.getTopForgottenWords().get(0).getWrongCount());

        assertEquals(4L, snapshot.getSpeakingSessionsCount());
        assertEquals(88.0, snapshot.getMaxSpeakingScore());
        assertEquals(74.5, snapshot.getLatestSpeakingScore());

        assertEquals(6L, snapshot.getQuizzesCount());
        assertEquals(79.0, snapshot.getQuizAverageScore());
        assertEquals(85.0, snapshot.getLatestQuizScore());

        assertEquals("PREMIUM_MONTHLY", snapshot.getSubscriptionPlan());
        assertTrue(snapshot.getIsVip());

        // Test Formatter
        String text = formatter.format(snapshot);
        assertTrue(text.contains("Quốc Hoàng"));
        assertTrue(text.contains("Trình độ hiệu dụng: B1"));
        assertTrue(text.contains("Chuỗi học liên tục hiện tại: 5 ngày"));
        assertTrue(text.contains("Tổng từ vựng đã học: 15 từ"));
        assertTrue(text.contains("resilient (4 lần sai)"));
        assertTrue(text.contains("PREMIUM_MONTHLY"));
    }

    @Test
    @DisplayName("Ca 3: Level Capped at C1 when placement or estimated is C2")
    void testLevelCappedAtC1() {
        Long userId = 20L;
        User user = User.builder().id(userId).username("expert").build();

        PlacementAttempt placement = PlacementAttempt.builder()
                .id(2L)
                .levelResult("C2")
                .completedAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(learningProfileService.estimateCurrentLevel(userId)).thenReturn("C2");
        when(placementAttemptRepository.findTopByUserId(userId)).thenReturn(Optional.of(placement));

        UserSnapshotDto snapshot = userSnapshotService.buildSnapshot(userId);

        assertEquals("C1", snapshot.getEffectiveLevel(), "Level should be capped at C1 as defined in placement spec");
    }
}
