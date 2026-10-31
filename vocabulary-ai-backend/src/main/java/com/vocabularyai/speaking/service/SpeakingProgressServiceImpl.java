package com.vocabularyai.speaking.service;

import com.vocabularyai.speaking.dto.response.SpeakingProgressResponse;
import com.vocabularyai.speaking.dto.response.SpeakingProgressResponse.*;
import com.vocabularyai.speaking.entity.SpeakingFeedback;
import com.vocabularyai.speaking.entity.SpeakingSession;
import com.vocabularyai.speaking.entity.SpeakingStatus;
import com.vocabularyai.speaking.repository.SpeakingAttemptRepository;
import com.vocabularyai.speaking.repository.SpeakingFeedbackRepository;
import com.vocabularyai.speaking.repository.SpeakingSessionRepository;
import com.vocabularyai.speaking.service.analysis.dto.SpeakingAnalysisResult;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SpeakingProgressServiceImpl
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SpeakingProgressServiceImpl implements SpeakingProgressService {
    
    private final SpeakingSessionRepository sessionRepository;
    private final SpeakingAttemptRepository attemptRepository;
    private final SpeakingFeedbackRepository feedbackRepository;
    private final SpeakingFeedbackService feedbackService;
    private final UserRepository userRepository;
    
    @Override
    public void updateSpeakingProgress(Long userId, Long sessionId, SpeakingAnalysisResult analysisResult) {
        log.info("Updating speaking progress - userId: {}, sessionId: {}", userId, sessionId);
        
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
            
            SpeakingSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                    .orElseThrow(() -> new IllegalArgumentException("Session not found or not authorized"));
            
            session.setOverallScore(analysisResult.getAnalysisScores().getOverall());
            sessionRepository.save(session);
            
            log.debug("Updated speaking progress - userId: {}, score: {}", 
                    userId, analysisResult.getAnalysisScores().getOverall());
            
        } catch (Exception e) {
            log.error("Failed to update speaking progress: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update speaking progress: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public SpeakingProgressResponse getSpeakingProgress(Long userId) {
        log.debug("Getting speaking progress - userId: {}", userId);
        
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        List<SpeakingSession> sessions = sessionRepository.findByUserId(userId, PageRequest.of(0, Integer.MAX_VALUE))
                .getContent();
        
        List<SpeakingSession> completedSessions = sessions.stream()
                .filter(s -> s.getStatus() == SpeakingStatus.COMPLETED)
                .collect(Collectors.toList());
        
        StatisticsData statistics = buildStatistics(userId, completedSessions);
        ScoresData averageScores = buildAverageScores(userId);
        ScoresData bestScores = buildBestScores(userId);
        RecentPerformanceData recentPerformance = buildRecentPerformance(userId);
        StreakData streaks = buildStreakData(userId);
        List<String> achievements = getMilestones(userId);
        String scoreTrend = calculateScoreTrend(userId, 7);
        
        return SpeakingProgressResponse.builder()
                .statistics(statistics)
                .averageScores(averageScores)
                .bestScores(bestScores)
                .recentPerformance(recentPerformance)
                .streaks(streaks)
                .achievements(achievements)
                .scoreTrend(scoreTrend)
                .lastUpdateAt(LocalDateTime.now())
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public String calculateScoreTrend(Long userId, int days) {
        log.debug("Calculating score trend - userId: {}, days: {}", userId, days);
        
        try {
            LocalDateTime startDate = LocalDateTime.now().minusDays(days);
            
            List<SpeakingSession> recentSessions = sessionRepository
                    .findByUserIdAndCreatedAtBetween(userId, startDate, LocalDateTime.now(), 
                            PageRequest.of(0, Integer.MAX_VALUE))
                    .getContent()
                    .stream()
                    .filter(s -> s.getStatus() == SpeakingStatus.COMPLETED && s.getOverallScore() != null)
                    .collect(Collectors.toList());
            
            if (recentSessions.size() < 2) {
                return "Insufficient data";
            }
            
            Double firstScore = recentSessions.get(0).getOverallScore();
            Double lastScore = recentSessions.get(recentSessions.size() - 1).getOverallScore();
            
            double improvement = lastScore - firstScore;
            
            if (improvement > 5) {
                return "Improving";
            } else if (improvement < -5) {
                return "Declining";
            } else {
                return "Stable";
            }
        } catch (Exception e) {
            log.warn("Failed to calculate score trend: {}", e.getMessage());
            return "Unable to calculate";
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<String> getMilestones(Long userId) {
        log.debug("Getting milestones - userId: {}", userId);
        
        List<String> milestones = new ArrayList<>();
        
        try {
            long totalSessions = sessionRepository.countByUserId(userId);
            long completedSessions = sessionRepository.countByUserIdAndStatus(userId, SpeakingStatus.COMPLETED);
            
            if (completedSessions >= 1) {
                milestones.add("🎯 First speaking session completed");
            }
            
            if (completedSessions >= 5) {
                milestones.add("🔥 5 speaking sessions completed");
            }
            
            if (completedSessions >= 10) {
                milestones.add("⭐ 10 speaking sessions completed");
            }
            
            if (completedSessions >= 30) {
                milestones.add("🏆 30 speaking sessions completed");
            }
            
            Double avgScore = feedbackRepository.calculateAverageOverallScoreByUserId(userId)
                    .orElseGet(() -> sessionRepository.findAverageOverallScoreByUserId(userId));
            if (avgScore != null && avgScore > 80) {
                milestones.add("💯 Average score over 80");
            }
            
            List<SpeakingSession> allSessions = sessionRepository
                    .findByUserId(userId, PageRequest.of(0, Integer.MAX_VALUE))
                    .getContent();
            
            boolean hasGreat90 = allSessions.stream()
                    .anyMatch(s -> s.getOverallScore() != null && s.getOverallScore() > 90);
            
            if (hasGreat90) {
                milestones.add("🌟 Achieved score over 90");
            }
            
            if (hasStreak(userId, 7)) {
                milestones.add("🔥 7-day practice streak");
            }
            
            if (hasStreak(userId, 30)) {
                milestones.add("🏅 30-day practice streak");
            }
            
        } catch (Exception e) {
            log.warn("Failed to get milestones: {}", e.getMessage());
        }
        
        return milestones;
    }
    
    @Override
    public void resetSpeakingProgress(Long userId) {
        log.warn("Resetting speaking progress - userId: {}", userId);
        
        try {
            userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
            
            List<SpeakingSession> sessions = sessionRepository
                    .findByUserId(userId, PageRequest.of(0, Integer.MAX_VALUE))
                    .getContent();
            
            for (SpeakingSession session : sessions) {
                session.setOverallScore(null);
                session.setStatus(SpeakingStatus.CANCELLED);
                sessionRepository.save(session);
            }
            
            log.info("Speaking progress reset - userId: {}", userId);
        } catch (Exception e) {
            log.error("Failed to reset speaking progress: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to reset speaking progress: " + e.getMessage(), e);
        }
    }
    
    private StatisticsData buildStatistics(Long userId, List<SpeakingSession> completedSessions) {
        try {
            long totalSessions = sessionRepository.countByUserId(userId);
            long totalAttempts = attemptRepository.countByUserId(userId);
            if (totalAttempts == 0) {
                for (SpeakingSession session : completedSessions) {
                    Integer count = session.getAttemptCount();
                    if (count != null && count > 0) {
                        totalAttempts += count.longValue();
                    }
                }
            }
            if (totalAttempts == 0 && !completedSessions.isEmpty()) {
                totalAttempts = completedSessions.size();
            }
            
            long totalTimeSeconds = totalAttempts * 30;
            long avgSessionDuration = completedSessions.isEmpty() ? 0 : 
                    (totalTimeSeconds / Math.max(1, completedSessions.size()));
            
            return StatisticsData.builder()
                    .totalSessions(totalSessions)
                    .completedSessions((long) completedSessions.size())
                    .totalAttempts(totalAttempts)
                    .totalTimeSpentSeconds(totalTimeSeconds)
                    .averageSessionDurationSeconds(avgSessionDuration)
                    .build();
        } catch (Exception e) {
            log.warn("Failed to build statistics: {}", e.getMessage());
            return StatisticsData.builder()
                    .totalSessions(0L)
                    .completedSessions(0L)
                    .totalAttempts(0L)
                    .build();
        }
    }
    
    private ScoresData buildAverageScores(Long userId) {
        try {
            Double overallAvg = feedbackRepository.calculateAverageOverallScoreByUserId(userId).orElse(null);
            if (overallAvg == null) {
                overallAvg = sessionRepository.findAverageOverallScoreByUserId(userId);
            }
            Double grammarAvg = feedbackRepository.calculateAverageGrammarScoreByUserId(userId).orElse(null);
            Double vocabAvg = feedbackRepository.calculateAverageVocabularyScoreByUserId(userId).orElse(null);
            Double fluencyAvg = feedbackRepository.calculateAverageFluencyScoreByUserId(userId).orElse(null);
            Double contentAvg = feedbackRepository.calculateAverageContentScoreByUserId(userId).orElse(null);
            
            return ScoresData.builder()
                    .overallScore(overallAvg)
                    .grammarScore(grammarAvg)
                    .vocabularyScore(vocabAvg)
                    .fluencyScore(fluencyAvg)
                    .contentScore(contentAvg)
                    .build();
        } catch (Exception e) {
            log.warn("Failed to build average scores: {}", e.getMessage());
            return ScoresData.builder().build();
        }
    }
    
    private ScoresData buildBestScores(Long userId) {
        try {
            List<SpeakingSession> sessions = sessionRepository
                    .findByUserId(userId, PageRequest.of(0, Integer.MAX_VALUE))
                    .getContent();
            
            Double bestOverall = sessions.stream()
                    .map(SpeakingSession::getOverallScore)
                    .filter(s -> s != null)
                    .max(Double::compareTo)
                    .orElse(null);
            
            return ScoresData.builder()
                    .overallScore(bestOverall)
                    .build();
        } catch (Exception e) {
            log.warn("Failed to build best scores: {}", e.getMessage());
            return ScoresData.builder().build();
        }
    }
    
    private RecentPerformanceData buildRecentPerformance(Long userId) {
        try {
            LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
            LocalDateTime monthAgo = LocalDateTime.now().minusDays(30);
            
            List<SpeakingSession> recentSessions = sessionRepository
                    .findByUserIdAndCreatedAtBetween(userId, weekAgo, LocalDateTime.now(),
                            PageRequest.of(0, Integer.MAX_VALUE))
                    .getContent()
                    .stream()
                    .filter(s -> s.getStatus() == SpeakingStatus.COMPLETED && s.getOverallScore() != null)
                    .collect(Collectors.toList());
            
            List<SpeakingSession> monthlySessions = sessionRepository
                    .findByUserIdAndCreatedAtBetween(userId, monthAgo, LocalDateTime.now(),
                            PageRequest.of(0, Integer.MAX_VALUE))
                    .getContent()
                    .stream()
                    .filter(s -> s.getStatus() == SpeakingStatus.COMPLETED && s.getOverallScore() != null)
                    .collect(Collectors.toList());
            
            Double lastScore = recentSessions.isEmpty() ? null : recentSessions.get(0).getOverallScore();
            LocalDateTime lastSessionDate = recentSessions.isEmpty() ? null : recentSessions.get(0).getStartedAt();
            
            Double weekAvg = recentSessions.isEmpty() ? 0.0 : 
                    recentSessions.stream().mapToDouble(s -> s.getOverallScore()).average().orElse(0.0);
            
            Double monthAvg = monthlySessions.isEmpty() ? 0.0 :
                    monthlySessions.stream().mapToDouble(s -> s.getOverallScore()).average().orElse(0.0);
            
            Double improvement7 = null;
            Double improvement30 = null;
            
            if (recentSessions.size() >= 2) {
                Double firstScore = recentSessions.get(recentSessions.size() - 1).getOverallScore();
                improvement7 = lastScore - firstScore;
            }
            
            if (monthlySessions.size() >= 2) {
                Double firstScore = monthlySessions.get(monthlySessions.size() - 1).getOverallScore();
                improvement30 = lastScore - firstScore;
            }
            
            return RecentPerformanceData.builder()
                    .lastSessionDate(lastSessionDate)
                    .lastSessionScore(lastScore)
                    .thisWeekAverageScore(weekAvg)
                    .thisMonthAverageScore(monthAvg)
                    .improvement7Days(improvement7)
                    .improvement30Days(improvement30)
                    .build();
        } catch (Exception e) {
            log.warn("Failed to build recent performance: {}", e.getMessage());
            return RecentPerformanceData.builder().build();
        }
    }
    
    private StreakData buildStreakData(Long userId) {
        try {
            List<SpeakingSession> allSessions = sessionRepository
                    .findByUserId(userId, PageRequest.of(0, Integer.MAX_VALUE))
                    .getContent()
                    .stream()
                    .filter(s -> s.getStatus() == SpeakingStatus.COMPLETED)
                    .sorted((s1, s2) -> s2.getStartedAt().compareTo(s1.getStartedAt()))
                    .collect(Collectors.toList());
            
            if (allSessions.isEmpty()) {
                return StreakData.builder().currentStreak(0).longestStreak(0).build();
            }
            
            int currentStreak = calculateCurrentStreak(allSessions);
            int longestStreak = findLongestStreak(allSessions);
            LocalDateTime lastPracticeDate = allSessions.get(0).getStartedAt();
            
            return StreakData.builder()
                    .currentStreak(currentStreak)
                    .longestStreak(longestStreak)
                    .lastPracticeDate(lastPracticeDate)
                    .build();
        } catch (Exception e) {
            log.warn("Failed to build streak data: {}", e.getMessage());
            return StreakData.builder().currentStreak(0).longestStreak(0).build();
        }
    }
    
    private int calculateCurrentStreak(List<SpeakingSession> sortedSessions) {
        if (sortedSessions.isEmpty()) {
            return 0;
        }
        
        int streak = 1;
        
        for (int i = 0; i < sortedSessions.size() - 1; i++) {
            LocalDate sessionDate = sortedSessions.get(i).getStartedAt().toLocalDate();
            LocalDate nextSessionDate = sortedSessions.get(i + 1).getStartedAt().toLocalDate();
            
            long daysDifference = ChronoUnit.DAYS.between(nextSessionDate, sessionDate);
            
            if (daysDifference == 1) {
                streak++;
            } else {
                break;
            }
        }
        
        return streak;
    }
    
    private int findLongestStreak(List<SpeakingSession> sortedSessions) {
        if (sortedSessions.isEmpty()) {
            return 0;
        }
        
        int maxStreak = 1;
        int currentStreak = 1;
        
        for (int i = 0; i < sortedSessions.size() - 1; i++) {
            LocalDate sessionDate = sortedSessions.get(i).getStartedAt().toLocalDate();
            LocalDate nextSessionDate = sortedSessions.get(i + 1).getStartedAt().toLocalDate();
            
            long daysDifference = ChronoUnit.DAYS.between(nextSessionDate, sessionDate);
            
            if (daysDifference == 1) {
                currentStreak++;
                maxStreak = Math.max(maxStreak, currentStreak);
            } else {
                currentStreak = 1;
            }
        }
        
        return maxStreak;
    }
    
    private boolean hasStreak(Long userId, int days) {
        try {
            LocalDateTime startDate = LocalDateTime.now().minusDays(days);
            
            List<SpeakingSession> sessions = sessionRepository
                    .findByUserIdAndCreatedAtBetween(userId, startDate, LocalDateTime.now(),
                            PageRequest.of(0, Integer.MAX_VALUE))
                    .getContent()
                    .stream()
                    .filter(s -> s.getStatus() == SpeakingStatus.COMPLETED)
                    .sorted((s1, s2) -> s2.getStartedAt().compareTo(s1.getStartedAt()))
                    .collect(Collectors.toList());
            
            if (sessions.size() < days) {
                return false;
            }
            
            int streak = calculateCurrentStreak(sessions);
            return streak >= days;
        } catch (Exception e) {
            log.warn("Failed to check streak: {}", e.getMessage());
            return false;
        }
    }
}
