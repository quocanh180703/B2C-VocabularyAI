package com.vocabularyai.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Real-time verified learning & account snapshot for a user.
 * Injected into AI Chat prompt to guarantee 100% factual accuracy without hallucinations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSnapshotDto {

    // 1. Identity
    private Long userId;
    private String displayName;
    private LocalDateTime accountCreatedAt;
    private Long daysSinceJoined;

    // 2. Proficiency Level
    private String effectiveLevel;      // e.g. "B1" (capped at C1)
    private String estimatedLevel;      // from LearningProfileService
    private String placementLevel;      // from PlacementAttempt (or null if not taken)
    private LocalDateTime placementCompletedAt;

    // 3. Activity & Streak
    private Integer activeLearningDays; // distinct days with learning activity
    private Integer currentStreak;
    private Integer longestStreak;
    private Long totalActivities;
    private Long todayTotalActivities;
    private Map<String, Long> todayActivitiesByType; // LEARN, REVIEW, QUIZ, SPEAKING
    private LocalDate lastActiveDate;

    // 4. Vocabulary (SRS)
    private Long wordsLearned;          // LEARNING + MASTERED
    private Long wordsLearning;         // LEARNING
    private Long wordsMastered;         // MASTERED
    private Long reviewsDueToday;       // nextReviewAt <= end of today
    private Long reviewsOverdue;        // nextReviewAt < start of today
    private Long favoriteWordsCount;
    private List<ForgottenWordDto> topForgottenWords; // top 5 with wrongCount > 0

    // 5. 7-Day Trend
    private Long activitiesLast7Days;
    private Long activitiesPrev7Days;
    private Double accuracyRateLast7Days;

    // 6. Speaking & Quiz
    private Long speakingSessionsCount;
    private Double latestSpeakingScore;
    private LocalDateTime latestSpeakingAt;
    private Double maxSpeakingScore;

    private Long quizzesCount;
    private Double quizAverageScore;
    private Double latestQuizScore;
    private LocalDateTime latestQuizAt;

    // 7. Subscription & Quota
    private String subscriptionPlan;    // "FREE", "PREMIUM_MONTHLY", "PREMIUM_YEARLY"
    private Boolean isVip;
    private LocalDateTime subscriptionExpiresAt;
    private Map<String, Integer> remainingQuotas; // e.g. "AI_CHAT" -> 5

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForgottenWordDto {
        private String word;
        private Integer wrongCount;
    }
}
