package com.vocabularyai.personalization.service;

import com.vocabularyai.ai.repository.GrammarPracticeHistoryRepository;
import com.vocabularyai.common.exception.ResourceNotFoundException;
import com.vocabularyai.learning.entity.LearningProgress;
import com.vocabularyai.learning.repository.LearningHistoryRepository;
import com.vocabularyai.learning.repository.LearningProgressRepository;
import com.vocabularyai.listening.repository.ListeningExerciseRepository;
import java.time.LocalDate;
import com.vocabularyai.personalization.dto.LearningProfileDto;
import com.vocabularyai.personalization.dto.LearningProfileDto.*;
import com.vocabularyai.placement.entity.PlacementAttempt;
import com.vocabularyai.placement.repository.PlacementAttemptRepository;
import com.vocabularyai.quiz.repository.QuizAttemptRepository;
import com.vocabularyai.speaking.entity.SpeakingLevel;
import com.vocabularyai.speaking.entity.SpeakingSession;
import com.vocabularyai.speaking.entity.SpeakingStatus;
import com.vocabularyai.speaking.repository.SpeakingSessionRepository;
import com.vocabularyai.speaking.service.SpeakingProgressService;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * LearningProfileServiceImpl
 * 
 * Aggregates user learning data from all modules:
 * - Vocabulary learning (Phase 4)
 * - Grammar learning (Phase 5)
 * - Quiz results (Phase 3)
 * - Speaking practice (Phase 8)
 * - AI interactions (Phase 7)
 * 
 * Tái sử dụng:
 * - LearningProgressRepository (vocabulary/grammar data)
 * - AiSpeakingSessionRepository (speaking data)
 * - SpeakingProgressService (speaking stats)
 * - UserRepository (user info)
 * 
 * NO new entities created
 * NO duplicate code
 * Pure aggregation from existing sources
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningProfileServiceImpl implements LearningProfileService {
    
    private final UserRepository userRepository;
    private final LearningProgressRepository learningProgressRepository;
    private final LearningHistoryRepository learningHistoryRepository;
    private final SpeakingSessionRepository speakingSessionRepository;
    private final SpeakingProgressService speakingProgressService;
    private final PlacementAttemptRepository placementAttemptRepository;
    private final GrammarPracticeHistoryRepository grammarPracticeHistoryRepository;
    private final ListeningExerciseRepository listeningExerciseRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    
    // Cache constants
    private static final String CACHE_KEY_PREFIX = "ai:profile:";
    private static final long CACHE_TTL_HOURS = 1;
    private static final int WEAK_TOPICS_THRESHOLD = 60; // < 60% = weak
    private static final int STRONG_TOPICS_THRESHOLD = 80; // > 80% = strong
    
    @Override
    public LearningProfileDto getLearningProfile(Long userId, boolean useCache) {
        log.debug("Getting learning profile for user {} (cache: {})", userId, useCache);
        
        // Verify user exists
        User user = userRepository.findById(userId)
            .orElseThrow(() -> ResourceNotFoundException.of("User", "id", userId));
        
        // Build profile from existing data
        return buildLearningProfile(user);
    }
    
    @Override
    public LearningProfileDto getLearningProfile(Long userId) {
        return getLearningProfile(userId, true);
    }
    
    @Override
    @Transactional
    public boolean invalidateLearningProfile(Long userId) {
        log.debug("Invalidating learning profile cache for user {}", userId);
        // Cache invalidation will be handled by CacheService in Phase 9
        // For now, just return true
        return true;
    }
    
    @Override
    public String estimateCurrentLevel(Long userId) {
        log.debug("Estimating current level for user {}", userId);
        LearningProfileDto profile = getLearningProfile(userId);
        return (profile != null && profile.getCurrentLevel() != null) ? profile.getCurrentLevel() : "A1";
    }
    
    @Override
    public List<String> getWeakTopics(Long userId) {
        log.debug("Getting weak topics for user {}", userId);
        LearningProfileDto profile = getLearningProfile(userId);
        return profile.getWeakTopics() != null ? profile.getWeakTopics() : List.of();
    }
    
    @Override
    public List<String> getStrongTopics(Long userId) {
        log.debug("Getting strong topics for user {}", userId);
        LearningProfileDto profile = getLearningProfile(userId);
        return profile.getStrongTopics() != null ? profile.getStrongTopics() : List.of();
    }
    
    @Override
    public Double getLearningFrequency(Long userId) {
        log.debug("Calculating learning frequency for user {}", userId);
        LearningProfileDto profile = getLearningProfile(userId);
        return profile.getLearningFrequency() != null ? profile.getLearningFrequency() : 0.0;
    }
    
    @Override
    public Double getAverageSessionDuration(Long userId) {
        log.debug("Getting average session duration for user {}", userId);
        LearningProfileDto profile = getLearningProfile(userId);
        return profile.getAverageSessionDuration() != null ? profile.getAverageSessionDuration() : 0.0;
    }
    
    // ==================== PRIVATE HELPER METHODS ====================
    
    /**
     * Build comprehensive learning profile from all data sources:
     * Vocabulary (25%), Grammar (20%), Speaking (25%), Listening (15%), Quiz (15%).
     */
    private LearningProfileDto buildLearningProfile(User user) {
        log.debug("Building learning profile for user {}", user.getId());
        
        int totalLearningDays = getTotalLearningDays(user.getId());

        // 1. Vocabulary stats from LearningProgress
        PerformanceMetric vocabMetric = getVocabularyStats(user.getId(), totalLearningDays);

        // 2. Grammar stats from GrammarPracticeHistory (independent measurement)
        PerformanceMetric grammarMetric = getGrammarStats(user.getId(), totalLearningDays);
        
        // 3. Speaking progress (monologue sessions + voice chat)
        PerformanceMetric speakingStats = getSpeakingStats(user.getId(), totalLearningDays);

        // 4. Listening stats from ListeningExercise
        PerformanceMetric listeningStats = getListeningStats(user.getId(), totalLearningDays);

        // 5. Quiz stats from QuizAttempt
        PerformanceMetric quizStats = getQuizStats(user.getId(), totalLearningDays);
        
        // Calculate overall metrics with dynamic normalization across active skills
        Double overallScore = calculateOverallScore(vocabMetric, grammarMetric, speakingStats, listeningStats, quizStats);
        int totalAttemptedWords = vocabMetric != null && vocabMetric.getTotalActivities() != null
            ? vocabMetric.getTotalActivities() : 0;
        String estimatedLevel = estimateLevelFromScore(overallScore, totalAttemptedWords, totalLearningDays);
        String currentLevel = getEffectiveLevel(user.getId(), estimatedLevel);
        
        // Get trends
        TrendData trend7Days = calculateTrend(user.getId(), 7);
        TrendData trend30Days = calculateTrend(user.getId(), 30);
        
        // Get recent activities
        List<RecentActivityDto> recentActivities = getRecentActivities(user.getId(), 10);
        
        // Get streaks and achievements
        Integer currentStreak = calculateCurrentStreak(user.getId());
        Integer longestStreak = calculateLongestStreak(user.getId());
        List<String> achievements = getAchievements(user.getId());
        
        // Get weak/strong topics across all 5 skills
        Map<String, PerformanceMetric> allMetrics = new LinkedHashMap<>();
        allMetrics.put("vocabulary", vocabMetric);
        allMetrics.put("grammar", grammarMetric);
        allMetrics.put("speaking", speakingStats);
        allMetrics.put("listening", listeningStats);
        allMetrics.put("quiz", quizStats);

        List<String> weakTopics = identifyWeakTopics(allMetrics);
        List<String> strongTopics = identifyStrongTopics(allMetrics);
        
        // Get learning patterns
        Double learningFrequency = calculateLearningFrequency(user.getId());
        Double avgSessionDuration = calculateAvgSessionDuration(user.getId());
        
        return LearningProfileDto.builder()
            .userId(user.getId())
            .userName(user.getEmail())
            .currentLevel(currentLevel)
            .overallScore(overallScore)
            .vocabulary(vocabMetric)
            .grammar(grammarMetric)
            .speaking(speakingStats)
            .listening(listeningStats)
            .quiz(quizStats)
            .trend7Days(trend7Days)
            .trend30Days(trend30Days)
            .recentActivities(recentActivities)
            .lastActivityAt(getLastActivityTime(user.getId()))
            .totalLearningDays(totalLearningDays)
            .currentStreak(currentStreak)
            .longestStreak(longestStreak)
            .achievements(achievements)
            .nextMilestone(getNextMilestone(user.getId(), achievements))
            .weakTopics(weakTopics)
            .strongTopics(strongTopics)
            .learningFrequency(learningFrequency)
            .averageSessionDuration(avgSessionDuration)
            .generatedAt(LocalDateTime.now())
            .build();
    }
    
    /**
     * Get vocabulary statistics from LearningProgress.
     * Uses Method 1: Volume & Data Maturity Weighted Score to prevent brand-new accounts from scoring high CEFR levels.
     */
    private PerformanceMetric getVocabularyStats(Long userId, int totalLearningDays) {
        log.debug("Getting vocabulary stats for user {}", userId);
        
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        var allProgress = learningProgressRepository.findByUserId(userId, pageable);
        List<LearningProgress> progressList = allProgress.getContent();
        
        List<LearningProgress> attemptedProgress = progressList.stream()
            .filter(lp -> (lp.getCorrectCount() + lp.getWrongCount()) > 0)
            .collect(Collectors.toList());

        double rawAccuracy;
        if (!attemptedProgress.isEmpty()) {
            rawAccuracy = attemptedProgress.stream()
                .mapToDouble(LearningProgress::getAccuracy)
                .average()
                .orElse(0.0);
        } else if (!progressList.isEmpty()) {
            rawAccuracy = 50.0;
        } else {
            rawAccuracy = 0.0;
        }
        
        int attemptedCount = attemptedProgress.size();
        int totalActivities = (int) progressList.stream()
            .mapToLong(lp -> lp.getReviewCount())
            .sum();
        
        // Volume Factor: scales up gradually with the number of unique words practiced
        double volumeFactor;
        if (attemptedCount < 15) {
            volumeFactor = 0.35;
        } else if (attemptedCount < 50) {
            volumeFactor = 0.50;
        } else if (attemptedCount < 150) {
            volumeFactor = 0.65;
        } else if (attemptedCount < 350) {
            volumeFactor = 0.80;
        } else if (attemptedCount < 800) {
            volumeFactor = 0.90;
        } else {
            volumeFactor = 1.00;
        }

        // Maturity Factor: rewards consistency across days (retention verification)
        double maturityFactor;
        if (totalLearningDays <= 1) {
            maturityFactor = 0.75;
        } else if (totalLearningDays < 5) {
            maturityFactor = 0.85;
        } else if (totalLearningDays < 14) {
            maturityFactor = 0.95;
        } else {
            maturityFactor = 1.00;
        }

        double weightedVocabScore = Math.min(100.0, Math.max(0.0, rawAccuracy * volumeFactor * maturityFactor));
        String vocabLevel = estimateLevelFromScore(weightedVocabScore, attemptedCount, totalLearningDays);
        
        return PerformanceMetric.builder()
            .score(weightedVocabScore)
            .bestScore(attemptedProgress.stream()
                .mapToDouble(LearningProgress::getAccuracy)
                .max()
                .orElse(rawAccuracy))
            .averageScore(weightedVocabScore)
            .totalActivities(totalActivities)
            .level(vocabLevel)
            .progressInLevel(calculateProgressInLevel(weightedVocabScore))
            .recommendedFocus(getVocabularyFocus(progressList))
            .build();
    }

    /**
     * Get independent grammar statistics from GrammarPracticeHistory (FREE, TOPIC, TRANSLATION).
     */
    private PerformanceMetric getGrammarStats(Long userId, int totalLearningDays) {
        log.debug("Getting grammar stats for user {}", userId);
        
        Double avgScore = grammarPracticeHistoryRepository.getAverageScoreByUserId(userId);
        Double maxScore = grammarPracticeHistoryRepository.getMaxScoreByUserId(userId);
        long count = grammarPracticeHistoryRepository.countByUserId(userId);
        int totalActivities = (int) count;

        if (totalActivities == 0 || avgScore == null || avgScore <= 0.0) {
            return PerformanceMetric.builder()
                .score(0.0)
                .bestScore(0.0)
                .averageScore(0.0)
                .totalActivities(0)
                .level("A1")
                .progressInLevel(0.0)
                .recommendedFocus("Start grammar practice")
                .build();
        }

        double grammarVolumeFactor = totalActivities < 5 ? 0.50 : (totalActivities < 15 ? 0.75 : 1.00);
        double weightedGrammarScore = Math.min(100.0, Math.max(0.0, avgScore * grammarVolumeFactor));
        String grammarLevel = estimateLevelFromScore(weightedGrammarScore, totalActivities, totalLearningDays);

        String recommendedFocus = "Luyện viết câu và cấu trúc ngữ pháp";
        try {
            List<Object[]> modeAvgs = grammarPracticeHistoryRepository.getAverageScoresByMode(userId);
            if (modeAvgs != null && !modeAvgs.isEmpty()) {
                String lowestMode = null;
                double lowestScore = Double.MAX_VALUE;
                for (Object[] row : modeAvgs) {
                    if (row.length >= 2 && row[0] != null && row[1] instanceof Number) {
                        double s = ((Number) row[1]).doubleValue();
                        if (s < lowestScore) {
                            lowestScore = s;
                            lowestMode = String.valueOf(row[0]);
                        }
                    }
                }
                if ("FREE".equalsIgnoreCase(lowestMode)) {
                    recommendedFocus = "Rèn luyện thêm kỹ năng Viết tự do";
                } else if ("TOPIC".equalsIgnoreCase(lowestMode)) {
                    recommendedFocus = "Rèn luyện thêm kỹ năng Viết theo chủ đề";
                } else if ("TRANSLATION".equalsIgnoreCase(lowestMode)) {
                    recommendedFocus = "Rèn luyện thêm kỹ năng Luyện dịch câu";
                }
            }
        } catch (Exception e) {
            log.warn("Failed to determine recommendedFocus for grammar: {}", e.getMessage());
        }

        return PerformanceMetric.builder()
            .score(weightedGrammarScore)
            .bestScore(maxScore != null ? maxScore : weightedGrammarScore)
            .averageScore(weightedGrammarScore)
            .totalActivities(totalActivities)
            .level(grammarLevel)
            .progressInLevel(calculateProgressInLevel(weightedGrammarScore))
            .recommendedFocus(recommendedFocus)
            .build();
    }

    /**
     * Get listening statistics from completed ListeningExercise.
     */
    private PerformanceMetric getListeningStats(Long userId, int totalLearningDays) {
        log.debug("Getting listening stats for user {}", userId);
        
        Double avgScore = listeningExerciseRepository.getAverageScoreByUserId(userId);
        Double maxScore = listeningExerciseRepository.getMaxScoreByUserId(userId);
        long count = listeningExerciseRepository.countCompletedByUserId(userId);
        int totalActivities = (int) count;

        if (totalActivities == 0 || avgScore == null || avgScore <= 0.0) {
            return PerformanceMetric.builder()
                .score(0.0)
                .bestScore(0.0)
                .averageScore(0.0)
                .totalActivities(0)
                .level("A1")
                .progressInLevel(0.0)
                .recommendedFocus("Start listening practice")
                .build();
        }

        double listeningVolumeFactor = totalActivities < 5 ? 0.50 : (totalActivities < 15 ? 0.75 : 1.00);
        double weightedListeningScore = Math.min(100.0, Math.max(0.0, avgScore * listeningVolumeFactor));
        String listeningLevel = estimateLevelFromScore(weightedListeningScore, totalActivities, totalLearningDays);

        return PerformanceMetric.builder()
            .score(weightedListeningScore)
            .bestScore(maxScore != null ? maxScore : weightedListeningScore)
            .averageScore(weightedListeningScore)
            .totalActivities(totalActivities)
            .level(listeningLevel)
            .progressInLevel(calculateProgressInLevel(weightedListeningScore))
            .recommendedFocus("Luyện nghe các đoạn văn dài hơn")
            .build();
    }

    /**
     * Get quiz statistics from submitted QuizAttempt.
     */
    private PerformanceMetric getQuizStats(Long userId, int totalLearningDays) {
        log.debug("Getting quiz stats for user {}", userId);
        
        Double avgScore = quizAttemptRepository.getAverageScoreByUserId(userId);
        Double maxScore = quizAttemptRepository.getMaxScoreByUserId(userId);
        long count = quizAttemptRepository.countCompletedByUser(userId);
        int totalActivities = (int) count;

        if (totalActivities == 0 || avgScore == null || avgScore <= 0.0) {
            return PerformanceMetric.builder()
                .score(0.0)
                .bestScore(0.0)
                .averageScore(0.0)
                .totalActivities(0)
                .level("A1")
                .progressInLevel(0.0)
                .recommendedFocus("Start quiz practice")
                .build();
        }

        double quizVolumeFactor = totalActivities < 5 ? 0.50 : (totalActivities < 15 ? 0.75 : 1.00);
        double weightedQuizScore = Math.min(100.0, Math.max(0.0, avgScore * quizVolumeFactor));
        String quizLevel = estimateLevelFromScore(weightedQuizScore, totalActivities, totalLearningDays);

        return PerformanceMetric.builder()
            .score(weightedQuizScore)
            .bestScore(maxScore != null ? maxScore : weightedQuizScore)
            .averageScore(weightedQuizScore)
            .totalActivities(totalActivities)
            .level(quizLevel)
            .progressInLevel(calculateProgressInLevel(weightedQuizScore))
            .recommendedFocus("Ôn lại các chủ đề hay sai")
            .build();
    }

    /**
     * Backward-compatibility helper for learning progress stats map
     */
    private Map<String, PerformanceMetric> getLearningProgressStats(Long userId, int totalLearningDays) {
        PerformanceMetric vocab = getVocabularyStats(userId, totalLearningDays);
        PerformanceMetric grammar = getGrammarStats(userId, totalLearningDays);
        Map<String, PerformanceMetric> result = new HashMap<>();
        result.put("vocabulary", vocab);
        result.put("grammar", grammar);
        return result;
    }

    private Map<String, PerformanceMetric> getLearningProgressStats(Long userId) {
        return getLearningProgressStats(userId, getTotalLearningDays(userId));
    }
    
    /**
     * Get speaking statistics with volume weighting
     */
    private PerformanceMetric getSpeakingStats(Long userId, int totalLearningDays) {
        log.debug("Getting speaking stats for user {}", userId);
        
        try {
            var speakingProgress = speakingProgressService.getSpeakingProgress(userId);
            
            Double avgScore = speakingProgress.getAverageScores() != null 
                ? speakingProgress.getAverageScores().getOverallScore() : null;
            Double bestScore = speakingProgress.getBestScores() != null 
                ? speakingProgress.getBestScores().getOverallScore() : null;
            int totalAttempts = (speakingProgress.getStatistics() != null && speakingProgress.getStatistics().getTotalAttempts() != null)
                ? speakingProgress.getStatistics().getTotalAttempts().intValue() : 0;
            
            if (avgScore == null || avgScore <= 0.0 || totalAttempts == 0) {
                Double directAvg = speakingSessionRepository.findAverageOverallScoreByUserId(userId);
                Double directMax = speakingSessionRepository.findMaxOverallScoreByUserId(userId);
                if (directAvg != null && directAvg > 0) {
                    avgScore = directAvg;
                    bestScore = directMax != null ? directMax : directAvg;
                    if (totalAttempts == 0) {
                        long completedCount = speakingSessionRepository.countByUserIdAndStatus(userId, SpeakingStatus.COMPLETED);
                        totalAttempts = (int) Math.max(1, completedCount);
                    }
                }
            }

            if (totalAttempts == 0 || avgScore == null || avgScore <= 0.0) {
                return PerformanceMetric.builder()
                    .score(0.0)
                    .bestScore(0.0)
                    .averageScore(0.0)
                    .totalActivities(0)
                    .level("A1")
                    .progressInLevel(0.0)
                    .recommendedFocus("Start speaking practice")
                    .build();
            }

            double speakingVolumeFactor = totalAttempts < 5 ? 0.50 : (totalAttempts < 15 ? 0.75 : 1.00);
            double weightedSpeakingScore = Math.min(100.0, Math.max(0.0, avgScore * speakingVolumeFactor));
            String speakingLevel = estimateLevelFromScore(weightedSpeakingScore, totalAttempts, totalLearningDays);
            
            return PerformanceMetric.builder()
                .score(weightedSpeakingScore)
                .bestScore(bestScore != null ? bestScore : weightedSpeakingScore)
                .averageScore(weightedSpeakingScore)
                .totalActivities(totalAttempts)
                .level(speakingLevel)
                .progressInLevel(calculateProgressInLevel(weightedSpeakingScore))
                .recommendedFocus("Practice daily conversations")
                .build();
        } catch (Exception e) {
            log.warn("Could not get speaking progress for user {}: {}", userId, e.getMessage());
            return PerformanceMetric.builder()
                .score(0.0)
                .bestScore(0.0)
                .averageScore(0.0)
                .totalActivities(0)
                .level("A1")
                .progressInLevel(0.0)
                .recommendedFocus("Start speaking practice")
                .build();
        }
    }

    private PerformanceMetric getSpeakingStats(Long userId) {
        return getSpeakingStats(userId, getTotalLearningDays(userId));
    }
    
    /**
     * Calculate overall score across up to 5 skill components:
     * Vocabulary (25%), Grammar (20%), Speaking (25%), Listening (15%), Quiz (15%).
     * Uses dynamic weight normalization across active skills (totalActivities > 0)
     * so unpracticed skills do not unfairly slash proficiency.
     */
    private Double calculateOverallScore(
            PerformanceMetric vocab,
            PerformanceMetric grammar,
            PerformanceMetric speaking,
            PerformanceMetric listening,
            PerformanceMetric quiz) {

        Map<PerformanceMetric, Double> baseWeights = new LinkedHashMap<>();
        baseWeights.put(vocab, 0.25);
        baseWeights.put(grammar, 0.20);
        baseWeights.put(speaking, 0.25);
        baseWeights.put(listening, 0.15);
        baseWeights.put(quiz, 0.15);

        double totalActiveWeight = 0.0;
        for (Map.Entry<PerformanceMetric, Double> entry : baseWeights.entrySet()) {
            PerformanceMetric m = entry.getKey();
            if (m != null && m.getTotalActivities() != null && m.getTotalActivities() > 0) {
                totalActiveWeight += entry.getValue();
            }
        }

        if (totalActiveWeight <= 0.0) {
            return 0.0;
        }

        double overallScore = 0.0;
        for (Map.Entry<PerformanceMetric, Double> entry : baseWeights.entrySet()) {
            PerformanceMetric m = entry.getKey();
            if (m != null && m.getTotalActivities() != null && m.getTotalActivities() > 0) {
                double score = m.getScore() != null ? m.getScore() : 0.0;
                double normalizedWeight = entry.getValue() / totalActiveWeight;
                overallScore += (score * normalizedWeight);
            }
        }

        return Math.min(100.0, Math.max(0.0, overallScore));
    }

    /**
     * Backward-compatible overload for 2-argument calls
     */
    private Double calculateOverallScore(Map<String, PerformanceMetric> learningStats, PerformanceMetric speakingStats) {
        PerformanceMetric vocab = learningStats.get("vocabulary");
        PerformanceMetric grammar = learningStats.get("grammar");
        return calculateOverallScore(vocab, grammar, speakingStats, null, null);
    }
    
    /**
     * Calculate trend data for period
     */
    private TrendData calculateTrend(Long userId, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        LocalDateTime endDate = LocalDateTime.now();
        
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        var sessionsPage = speakingSessionRepository.findByUserIdAndCreatedAtBetween(
            userId, startDate, endDate, pageable
        );
        
        List<SpeakingSession> sessions = sessionsPage.getContent();
        
        if (sessions.isEmpty()) {
            return TrendData.builder()
                .direction("STABLE")
                .percentageChange(0.0)
                .averageScore(0.0)
                .highestScore(0.0)
                .lowestScore(0.0)
                .activitiesCount(0)
                .build();
        }
        
        double avgScore = sessions.stream()
            .mapToDouble(s -> s.getOverallScore() != null ? s.getOverallScore() : 0)
            .average()
            .orElse(0);
        
        double highScore = sessions.stream()
            .mapToDouble(s -> s.getOverallScore() != null ? s.getOverallScore() : 0)
            .max()
            .orElse(0);
        
        double lowScore = sessions.stream()
            .mapToDouble(s -> s.getOverallScore() != null ? s.getOverallScore() : 0)
            .min()
            .orElse(0);
        
        return TrendData.builder()
            .direction(highScore > avgScore ? "IMPROVING" : "STABLE")
            .percentageChange(5.0) // Placeholder
            .averageScore(avgScore)
            .highestScore(highScore)
            .lowestScore(lowScore)
            .activitiesCount(sessions.size())
            .build();
    }
    
    /**
     * Get recent learning activities
     */
    private List<RecentActivityDto> getRecentActivities(Long userId, int limit) {
        List<RecentActivityDto> activities = new ArrayList<>();
        
        // Get recent speaking activities
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Order.desc("createdAt")));
        var speakingSessions = speakingSessionRepository.findByUserId(userId, pageable);
        
        speakingSessions.getContent().forEach(session -> {
            activities.add(RecentActivityDto.builder()
                .type("SPEAKING")
                .score(session.getOverallScore())
                .topic(session.getTopic())
                .timestamp(session.getCreatedAt())
                .durationMinutes(5) // Placeholder
                .build());
        });
        
        return activities.stream()
            .sorted(Comparator.comparing(RecentActivityDto::getTimestamp).reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    /**
     * Calculate current learning streak across all activities (vocab, quizzes, speaking)
     */
    @Override
    public Integer calculateCurrentStreak(Long userId) {
        List<java.sql.Date> activitySqlDates = learningHistoryRepository.findActivityDatesByUserId(userId);
        if (activitySqlDates == null || activitySqlDates.isEmpty()) {
            return 0;
        }

        Set<LocalDate> activeDateSet = activitySqlDates.stream()
                .map(java.sql.Date::toLocalDate)
                .collect(Collectors.toSet());

        LocalDate today = LocalDate.now();
        boolean todayCompleted = activeDateSet.contains(today);

        int currentStreak = 0;
        LocalDate checkDate = today;
        if (!todayCompleted) {
            LocalDate yesterday = today.minusDays(1);
            if (activeDateSet.contains(yesterday)) {
                checkDate = yesterday;
            } else {
                checkDate = null;
            }
        }

        if (checkDate != null) {
            while (activeDateSet.contains(checkDate)) {
                currentStreak++;
                checkDate = checkDate.minusDays(1);
            }
        }

        return currentStreak;
    }
    
    /**
     * Calculate longest learning streak across all activities
     */
    @Override
    public Integer calculateLongestStreak(Long userId) {
        List<java.sql.Date> activitySqlDates = learningHistoryRepository.findActivityDatesByUserId(userId);
        if (activitySqlDates == null || activitySqlDates.isEmpty()) {
            return 0;
        }

        Set<LocalDate> activeDateSet = activitySqlDates.stream()
                .map(java.sql.Date::toLocalDate)
                .collect(Collectors.toSet());

        List<LocalDate> sortedDates = activeDateSet.stream().sorted().toList();
        int longestStreak = 0;
        int tempStreak = 0;
        LocalDate prev = null;
        for (LocalDate d : sortedDates) {
            if (prev == null || d.equals(prev.plusDays(1))) {
                tempStreak++;
            } else if (!d.equals(prev)) {
                tempStreak = 1;
            }
            longestStreak = Math.max(longestStreak, tempStreak);
            prev = d;
        }
        return Math.max(longestStreak, calculateCurrentStreak(userId));
    }
    
    /**
     * Get user achievements
     */
    private List<String> getAchievements(Long userId) {
        List<String> achievements = new ArrayList<>();
        
        // Get stats directly without calling getLearningProfile (to avoid infinite recursion)
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        var progressPage = learningProgressRepository.findByUserId(userId, pageable);
        List<LearningProgress> progressList = progressPage.getContent();
        
        long totalLearningDays = getTotalLearningDays(userId);
        int currentStreak = calculateCurrentStreak(userId);
        
        double overallScore = progressList.isEmpty() ? 0 : 
            progressList.stream()
                .mapToDouble(lp -> lp.getAccuracy())
                .average()
                .orElse(0);
        
        if (totalLearningDays > 0) {
            achievements.add("First learning session");
        }
        
        if (currentStreak >= 7) {
            achievements.add("7-day learning streak");
        }
        
        if (overallScore > 80) {
            achievements.add("High proficiency (>80%)");
        }
        
        return achievements;
    }
    
    /**
     * Identify weak topics (< 60% accuracy, only for skills with activities)
     */
    private List<String> identifyWeakTopics(Map<String, PerformanceMetric> stats) {
        List<String> weak = new ArrayList<>();
        
        stats.forEach((topic, metric) -> {
            if (metric != null && metric.getTotalActivities() != null && metric.getTotalActivities() > 0
                    && metric.getScore() != null && metric.getScore() < WEAK_TOPICS_THRESHOLD) {
                weak.add(topic);
            }
        });
        
        return weak;
    }
    
    /**
     * Identify strong topics (> 80% accuracy, only for skills with activities)
     */
    private List<String> identifyStrongTopics(Map<String, PerformanceMetric> stats) {
        List<String> strong = new ArrayList<>();
        
        stats.forEach((topic, metric) -> {
            if (metric != null && metric.getTotalActivities() != null && metric.getTotalActivities() > 0
                    && metric.getScore() != null && metric.getScore() > STRONG_TOPICS_THRESHOLD) {
                strong.add(topic);
            }
        });
        
        return strong;
    }
    
    /**
     * Calculate learning frequency (activities per week)
     */
    private Double calculateLearningFrequency(Long userId) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        LocalDateTime now = LocalDateTime.now();
        
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        var sessionsPage = speakingSessionRepository.findByUserIdAndCreatedAtBetween(
            userId, thirtyDaysAgo, now, pageable
        );
        
        List<SpeakingSession> sessions = sessionsPage.getContent();
        
        if (sessions.isEmpty()) return 0.0;
        return (double) sessions.size() / 4.3; // 30 days / ~4.3 weeks
    }
    
    /**
     * Calculate average session duration
     */
    private Double calculateAvgSessionDuration(Long userId) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        LocalDateTime now = LocalDateTime.now();
        
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        var sessionsPage = speakingSessionRepository.findByUserIdAndCreatedAtBetween(
            userId, thirtyDaysAgo, now, pageable
        );
        
        if (sessionsPage.isEmpty()) return 0.0;
        // Each session assumed to be ~10 minutes
        return 10.0;
    }
    
    /**
     * Get last activity time
     */
    private LocalDateTime getLastActivityTime(Long userId) {
        Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Order.desc("createdAt")));
        var sessions = speakingSessionRepository.findByUserId(userId, pageable);
        
        return sessions.isEmpty() ? null : sessions.getContent().get(0).getCreatedAt();
    }
    
    /**
     * Get total learning days
     */
    /**
     * Get total learning days across speaking sessions and vocabulary/quiz history
     */
    private Integer getTotalLearningDays(Long userId) {
        Set<java.time.LocalDate> learningDates = new HashSet<>();
        
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(365);
        LocalDateTime now = LocalDateTime.now();
        
        Pageable pageable = PageRequest.of(0, 500);
        try {
            var sessionsPage = speakingSessionRepository.findByUserIdAndCreatedAtBetween(
                userId, thirtyDaysAgo, now, pageable
            );
            sessionsPage.getContent().forEach(session -> {
                if (session.getCreatedAt() != null) {
                    learningDates.add(session.getCreatedAt().toLocalDate());
                }
            });
        } catch (Exception ignored) {}

        try {
            var historyPage = learningHistoryRepository.findByDateRange(userId, thirtyDaysAgo, now, pageable);
            historyPage.getContent().forEach(h -> {
                if (h.getCreatedAt() != null) {
                    learningDates.add(h.getCreatedAt().toLocalDate());
                }
            });
        } catch (Exception ignored) {}
        
        return learningDates.size();
    }
    
    /**
     * Get next milestone
     */
    private String getNextMilestone(Long userId, List<String> achievements) {
        if (achievements.isEmpty()) return "Complete first learning session";
        if (!achievements.contains("7-day learning streak")) return "Build a 7-day learning streak";
        return "Reach B2 level";
    }
    
    /**
     * Get vocabulary focus recommendation
     */
    private String getVocabularyFocus(List<LearningProgress> progressList) {
        long masteredCount = progressList.stream()
            .filter(lp -> lp.getStatus() == LearningProgress.LearningStatus.MASTERED)
            .count();
        
        if (masteredCount < 100) return "Learn more vocabulary words";
        return "Review and consolidate knowledge";
    }
    
    /**
     * Estimate CEFR level from score with hard ceiling constraints on small sample sizes.
     * Prevents brand new users (e.g. 1 day of learning, < 15 words) from prematurely reaching B2/C1/C2.
     */
    private String estimateLevelFromScore(Double score, int attemptedItems, int learningDays) {
        if (score == null || score <= 0) return "A1";

        // Hard ceiling guards based on volume and data maturity:
        // Case 1: Under 15 items OR <= 1 day of learning -> max level is A2
        if (attemptedItems < 15 || learningDays <= 1) {
            if (score < 25) return "A1";
            return "A2";
        }
        // Case 2: Under 50 items OR < 4 days of learning -> max level is B1
        if (attemptedItems < 50 || learningDays < 4) {
            if (score < 25) return "A1";
            if (score < 45) return "A2";
            return "B1";
        }
        // Case 3: Under 150 items OR < 10 days of learning -> max level is B2
        if (attemptedItems < 150 || learningDays < 10) {
            if (score < 25) return "A1";
            if (score < 45) return "A2";
            if (score < 60) return "B1";
            return "B2";
        }

        // Fully matured profile:
        if (score < 25) return "A1";
        if (score < 45) return "A2";
        if (score < 60) return "B1";
        if (score < 75) return "B2";
        if (score < 90) return "C1";
        return "C1";
    }

    private static final List<String> CEFR_LEVEL_HIERARCHY = List.of("A1", "A2", "B1", "B2", "C1", "C2");

    private String getEffectiveLevel(Long userId, String estimatedLevel) {
        if (userId == null) return estimatedLevel != null ? estimatedLevel : "A1";
        Optional<String> placementLevel = placementAttemptRepository.findTopByUserId(userId)
            .map(PlacementAttempt::getLevelResult);
        if (placementLevel.isEmpty()) {
            return estimatedLevel != null ? estimatedLevel : "A1";
        }
        return maxCefrLevel(placementLevel.get(), estimatedLevel);
    }

    private String maxCefrLevel(String l1, String l2) {
        if (l1 == null) return l2 != null ? l2.toUpperCase() : "A1";
        if (l2 == null) return l1.toUpperCase();
        int idx1 = CEFR_LEVEL_HIERARCHY.indexOf(l1.toUpperCase());
        int idx2 = CEFR_LEVEL_HIERARCHY.indexOf(l2.toUpperCase());
        return (idx1 >= idx2) ? l1.toUpperCase() : l2.toUpperCase();
    }

    private String estimateLevelFromScore(Double score) {
        return estimateLevelFromScore(score, 100, 10);
    }
    
    /**
     * Calculate progress in current level
     */
    private Double calculateProgressInLevel(Double score) {
        if (score == null) return 0.0;
        return (score % 15) / 15 * 100; // Simplified
    }
}
