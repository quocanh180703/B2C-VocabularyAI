package com.vocabularyai.ai.service.chat;

import com.vocabularyai.ai.dto.UserSnapshotDto;
import com.vocabularyai.learning.entity.LearningHistory;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSnapshotServiceImpl implements UserSnapshotService {

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final UserRepository userRepository;
    private final LearningProfileService learningProfileService;
    private final PlacementAttemptRepository placementAttemptRepository;
    private final LearningHistoryRepository learningHistoryRepository;
    private final LearningProgressRepository learningProgressRepository;
    private final FavoriteWordRepository favoriteWordRepository;
    private final SpeakingSessionRepository speakingSessionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PackageRepository packageRepository;
    private final FeatureUsageService featureUsageService;

    @Override
    @Transactional(readOnly = true)
    public UserSnapshotDto buildSnapshot(Long userId) {
        log.debug("Building factual user snapshot for user: {}", userId);
        long startMs = System.currentTimeMillis();

        LocalDate today = LocalDate.now(VN_ZONE);
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);
        LocalDateTime sevenDaysAgo = todayStart.minusDays(7);
        LocalDateTime fourteenDaysAgo = todayStart.minusDays(14);

        // 1. Identity
        User user = userRepository.findById(userId).orElse(null);
        String displayName = "User";
        LocalDateTime accountCreatedAt = null;
        Long daysSinceJoined = 0L;

        if (user != null) {
            displayName = (user.getFullName() != null && !user.getFullName().isBlank()) 
                    ? user.getFullName().trim() 
                    : user.getUsername();
            accountCreatedAt = user.getCreatedAt();
            if (accountCreatedAt != null) {
                daysSinceJoined = ChronoUnit.DAYS.between(accountCreatedAt.toLocalDate(), today);
            }
        }

        // 2. Proficiency Level
        String estimatedLevel = "A1";
        try {
            estimatedLevel = learningProfileService.estimateCurrentLevel(userId);
            if (estimatedLevel == null || estimatedLevel.isBlank()) {
                estimatedLevel = "A1";
            }
        } catch (Exception e) {
            log.warn("Error estimating level for user {}: {}", userId, e.getMessage());
        }

        Optional<PlacementAttempt> placementOpt = placementAttemptRepository.findTopByUserId(userId);
        String placementLevel = placementOpt.map(PlacementAttempt::getLevelResult).orElse(null);
        LocalDateTime placementCompletedAt = placementOpt.map(PlacementAttempt::getCompletedAt).orElse(null);
        String effectiveLevel = calculateEffectiveLevel(estimatedLevel, placementLevel);

        // 3. Activity & Streak
        Integer currentStreak = 0;
        Integer longestStreak = 0;
        try {
            currentStreak = learningProfileService.calculateCurrentStreak(userId);
            longestStreak = learningProfileService.calculateLongestStreak(userId);
        } catch (Exception e) {
            log.warn("Error calculating streaks for user {}: {}", userId, e.getMessage());
        }

        List<java.sql.Date> activityDates = learningHistoryRepository.findActivityDatesByUserId(userId);
        int activeLearningDays = (activityDates != null) ? activityDates.size() : 0;
        LocalDate lastActiveDate = (activityDates != null && !activityDates.isEmpty()) 
                ? activityDates.get(0).toLocalDate() 
                : null;

        long totalActivities = learningHistoryRepository.countByUserId(userId);
        long todayTotalActivities = learningHistoryRepository.countTodayActivities(userId, todayStart);

        Map<String, Long> todayActivitiesByType = new LinkedHashMap<>();
        try {
            List<Object[]> todayActivityRows = learningHistoryRepository.countTodayByActivityType(userId, todayStart);
            if (todayActivityRows != null) {
                for (Object[] row : todayActivityRows) {
                    if (row != null && row.length >= 2 && row[0] != null && row[1] != null) {
                        String typeName = row[0].toString();
                        long count = ((Number) row[1]).longValue();
                        todayActivitiesByType.put(typeName, count);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error getting today activity breakdown for user {}: {}", userId, e.getMessage());
        }

        // 4. Vocabulary (SRS)
        long wordsLearning = learningProgressRepository.countByUserIdAndStatus(userId, LearningProgress.LearningStatus.LEARNING);
        long wordsMastered = learningProgressRepository.countByUserIdAndStatus(userId, LearningProgress.LearningStatus.MASTERED);
        long wordsLearned = wordsLearning + wordsMastered;
        long reviewsDueToday = learningProgressRepository.countByUserIdAndNextReviewAtLessThanEqual(userId, todayEnd);
        long reviewsOverdue = learningProgressRepository.countOverdueReviews(userId, todayStart);
        long favoriteWordsCount = favoriteWordRepository.countByUserId(userId);

        List<UserSnapshotDto.ForgottenWordDto> topForgottenWords = new ArrayList<>();
        try {
            List<Object[]> forgottenRows = learningProgressRepository.findTopForgottenWords(userId, PageRequest.of(0, 5));
            if (forgottenRows != null) {
                for (Object[] row : forgottenRows) {
                    if (row != null && row.length >= 2 && row[0] != null && row[1] != null) {
                        String word = row[0].toString();
                        int wrongCount = ((Number) row[1]).intValue();
                        topForgottenWords.add(UserSnapshotDto.ForgottenWordDto.builder()
                                .word(word)
                                .wrongCount(wrongCount)
                                .build());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error getting top forgotten words for user {}: {}", userId, e.getMessage());
        }

        // 5. 7-Day Trend
        long activitiesLast7Days = learningHistoryRepository.countActivitiesSince(userId, sevenDaysAgo);
        long activitiesPrev7Days = learningHistoryRepository.countActivitiesBetween(userId, fourteenDaysAgo, sevenDaysAgo);
        Double accuracyRateLast7Days = learningHistoryRepository.getAccuracyRateSince(userId, sevenDaysAgo);

        // 6. Speaking & Quiz
        long speakingSessionsCount = speakingSessionRepository.countByUserIdAndStatus(userId, SpeakingStatus.COMPLETED);
        Double maxSpeakingScore = speakingSessionRepository.findMaxOverallScoreByUserId(userId);
        Optional<SpeakingSession> latestSpeakingOpt = speakingSessionRepository.findLatestCompletedSession(userId);
        Double latestSpeakingScore = latestSpeakingOpt.map(SpeakingSession::getOverallScore).orElse(null);
        LocalDateTime latestSpeakingAt = latestSpeakingOpt.map(SpeakingSession::getCompletedAt).orElse(null);

        long quizzesCount = quizAttemptRepository.countCompletedByUser(userId);
        Double quizAverageScore = quizAttemptRepository.getAverageScoreByUserId(userId);
        Optional<QuizAttempt> latestQuizOpt = quizAttemptRepository.findLatestCompletedAttempt(userId);
        Double latestQuizScore = latestQuizOpt.map(QuizAttempt::getScore).orElse(null);
        LocalDateTime latestQuizAt = latestQuizOpt.map(QuizAttempt::getSubmittedAt).orElse(null);

        // 7. Subscription & Quota
        Optional<Subscription> subOpt = subscriptionRepository.findByUserId(userId);
        String subscriptionPlan = "FREE";
        boolean isVip = false;
        LocalDateTime subscriptionExpiresAt = null;

        if (subOpt.isPresent()) {
            Subscription sub = subOpt.get();
            subscriptionExpiresAt = sub.getEndAt();
            if (sub.isActive()) {
                isVip = true;
                subscriptionPlan = "PREMIUM";
                if (sub.getPackageId() != null) {
                    Optional<Package> pkgOpt = packageRepository.findById(sub.getPackageId());
                    if (pkgOpt.isPresent()) {
                        subscriptionPlan = pkgOpt.get().getCode();
                    }
                }
            } else if ("EXPIRED".equalsIgnoreCase(sub.getStatus())) {
                subscriptionPlan = "FREE (Đã hết hạn)";
            }
        }

        Map<String, Integer> remainingQuotas = new LinkedHashMap<>();
        try {
            SubscriptionUsageResponse usage = featureUsageService.getUsage(userId);
            if (usage != null && usage.getFeatures() != null) {
                usage.getFeatures().forEach((featureKey, quotaDto) -> {
                    if (quotaDto != null) {
                        remainingQuotas.put(featureKey, quotaDto.getRemaining());
                    }
                });
            }
        } catch (Exception e) {
            log.warn("Error getting feature quotas for user {}: {}", userId, e.getMessage());
        }

        long elapsedMs = System.currentTimeMillis() - startMs;
        log.debug("User snapshot built successfully in {} ms for user: {}", elapsedMs, userId);

        return UserSnapshotDto.builder()
                .userId(userId)
                .displayName(displayName)
                .accountCreatedAt(accountCreatedAt)
                .daysSinceJoined(daysSinceJoined)
                .effectiveLevel(effectiveLevel)
                .estimatedLevel(estimatedLevel)
                .placementLevel(placementLevel)
                .placementCompletedAt(placementCompletedAt)
                .activeLearningDays(activeLearningDays)
                .currentStreak(currentStreak)
                .longestStreak(longestStreak)
                .totalActivities(totalActivities)
                .todayTotalActivities(todayTotalActivities)
                .todayActivitiesByType(todayActivitiesByType)
                .lastActiveDate(lastActiveDate)
                .wordsLearned(wordsLearned)
                .wordsLearning(wordsLearning)
                .wordsMastered(wordsMastered)
                .reviewsDueToday(reviewsDueToday)
                .reviewsOverdue(reviewsOverdue)
                .favoriteWordsCount(favoriteWordsCount)
                .topForgottenWords(topForgottenWords)
                .activitiesLast7Days(activitiesLast7Days)
                .activitiesPrev7Days(activitiesPrev7Days)
                .accuracyRateLast7Days(accuracyRateLast7Days)
                .speakingSessionsCount(speakingSessionsCount)
                .latestSpeakingScore(latestSpeakingScore)
                .latestSpeakingAt(latestSpeakingAt)
                .maxSpeakingScore(maxSpeakingScore)
                .quizzesCount(quizzesCount)
                .quizAverageScore(quizAverageScore)
                .latestQuizScore(latestQuizScore)
                .latestQuizAt(latestQuizAt)
                .subscriptionPlan(subscriptionPlan)
                .isVip(isVip)
                .subscriptionExpiresAt(subscriptionExpiresAt)
                .remainingQuotas(remainingQuotas)
                .build();
    }

    private String calculateEffectiveLevel(String estimated, String placement) {
        int estRank = levelToRank(estimated);
        int plcRank = levelToRank(placement);
        int maxRank = Math.max(estRank, plcRank);
        // Capped at C1 (rank 5)
        if (maxRank > 5) {
            maxRank = 5;
        }
        return rankToLevel(maxRank);
    }

    private int levelToRank(String level) {
        if (level == null || level.isBlank()) {
            return 1;
        }
        return switch (level.trim().toUpperCase()) {
            case "A2" -> 2;
            case "B1" -> 3;
            case "B2" -> 4;
            case "C1" -> 5;
            case "C2" -> 6;
            default -> 1; // A1
        };
    }

    private String rankToLevel(int rank) {
        return switch (rank) {
            case 2 -> "A2";
            case 3 -> "B1";
            case 4 -> "B2";
            case 5 -> "C1";
            case 6 -> "C2";
            default -> "A1";
        };
    }
}
