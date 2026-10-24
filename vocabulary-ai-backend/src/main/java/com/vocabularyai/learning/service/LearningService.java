package com.vocabularyai.learning.service;

import com.vocabularyai.common.exception.BadRequestException;
import com.vocabularyai.common.exception.ResourceNotFoundException;
import com.vocabularyai.learning.dto.LearningHistoryResponse;
import com.vocabularyai.learning.dto.LearningProgressResponse;
import com.vocabularyai.learning.dto.ProgressStatisticsResponse;
import com.vocabularyai.learning.entity.LearningHistory;
import com.vocabularyai.learning.entity.LearningProgress;
import com.vocabularyai.learning.repository.LearningHistoryRepository;
import com.vocabularyai.learning.repository.LearningProgressRepository;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.vocabulary.entity.Category;
import com.vocabularyai.vocabulary.entity.Word;
import com.vocabularyai.vocabulary.repository.CategoryRepository;
import com.vocabularyai.vocabulary.repository.CefrWordRepository;
import com.vocabularyai.quiz.repository.QuizAttemptRepository;
import com.vocabularyai.speaking.repository.SpeakingSessionRepository;
import com.vocabularyai.vocabulary.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Learning Service
 * 
 * Business logic for learning progress and history management
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class LearningService {
    
    private final LearningProgressRepository progressRepository;
    private final LearningHistoryRepository historyRepository;
    private final WordRepository wordRepository;
    private final CefrWordRepository cefrWordRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final SpeakingSessionRepository speakingSessionRepository;
    private final CategoryRepository categoryRepository;

    public LearningService(
            LearningProgressRepository progressRepository,
            LearningHistoryRepository historyRepository,
            WordRepository wordRepository,
            CefrWordRepository cefrWordRepository,
            QuizAttemptRepository quizAttemptRepository,
            SpeakingSessionRepository speakingSessionRepository,
            CategoryRepository categoryRepository) {
        this.progressRepository = progressRepository;
        this.historyRepository = historyRepository;
        this.wordRepository = wordRepository;
        this.cefrWordRepository = cefrWordRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.speakingSessionRepository = speakingSessionRepository;
        this.categoryRepository = categoryRepository;
    }
    
    @Transactional
    public LearningProgressResponse startLearningWord(User user, Long wordId) {
        return startLearningWord(user, String.valueOf(wordId));
    }

    /**
     * Start learning a word (supports numeric ID, CEFR UUID or lemma)
     */
    @Transactional
    public LearningProgressResponse startLearningWord(User user, String wordIdentifier) {
        log.info("User {} starting to learn word {}", user.getId(), wordIdentifier);
        Word word = null;

        try {
            Long numericId = Long.parseLong(wordIdentifier);
            word = wordRepository.findById(numericId).orElse(null);
        } catch (NumberFormatException ignored) {}

        if (word == null) {
            var cefrOpt = cefrWordRepository.findById(wordIdentifier);
            if (cefrOpt.isEmpty()) {
                cefrOpt = cefrWordRepository.findByLemma(wordIdentifier);
            }
            if (cefrOpt.isPresent()) {
                var cefr = cefrOpt.get();
                word = wordRepository.findByWord(cefr.getLemma()).orElse(null);
                if (word == null) {
                    Category defaultCat = categoryRepository.findAll().stream().findFirst().orElse(null);
                    word = wordRepository.save(Word.builder()
                        .word(cefr.getLemma())
                        .category(defaultCat)
                        .meaning(cefr.getDefinition() != null ? cefr.getDefinition() : cefr.getLemma())
                        .definition(cefr.getDefinition())
                        .pronunciation(cefr.getPronunciation())
                        .level(cefr.getCefrLevel() != null ? cefr.getCefrLevel().toString() : "A1")
                        .partOfSpeech(cefr.getPartOfSpeech() != null ? cefr.getPartOfSpeech().toString() : "")
                        .build());
                }
            }
        }

        if (word == null) {
            throw ResourceNotFoundException.of("Word", "id", wordIdentifier);
        }
        
        // Check if already learning - if yes, return existing
        var existing = progressRepository.findByUserIdAndWordId(user.getId(), word.getId());
        if (existing.isPresent()) {
            return LearningProgressResponse.fromEntity(existing.get());
        }
        
        // Create new learning progress
        LearningProgress progress = LearningProgress.builder()
            .user(user)
            .word(word)
            .status(LearningProgress.LearningStatus.LEARNING)
            .nextReviewAt(calculateNextReviewDate(0))
            .build();
        
        LearningProgress saved = progressRepository.save(progress);
        
        // Log activity
        recordActivity(user, word, LearningHistory.ActivityType.LEARN, null);
        
        log.info("Started learning word {} for user {}", word.getId(), user.getId());
        return LearningProgressResponse.fromEntity(saved);
    }
    
    /**
     * Review a word (mark correct or incorrect)
     */
    @Transactional
    public LearningProgressResponse reviewWord(User user, Long wordId, Boolean isCorrect) {
        log.info("User {} reviewing word {}: {}", user.getId(), wordId, isCorrect);
        
        // Get progress
        LearningProgress progress = progressRepository.findByUserIdAndWordId(user.getId(), wordId)
            .orElseThrow(() -> new BadRequestException("Not learning this word yet"));
        
        Word word = progress.getWord();
        
        // Update progress
        progress.setReviewCount(progress.getReviewCount() + 1);
        progress.setLastReviewedAt(LocalDateTime.now());
        
        if (isCorrect) {
            progress.setCorrectCount(progress.getCorrectCount() + 1);
        } else {
            progress.setWrongCount(progress.getWrongCount() + 1);
        }
        
        // Update status and schedule next review
        if (progress.getAccuracy() >= 80 && progress.getReviewCount() >= 5) {
            progress.setStatus(LearningProgress.LearningStatus.MASTERED);
            progress.setNextReviewAt(null);
        } else {
            progress.setNextReviewAt(calculateNextReviewDate(progress.getReviewCount()));
        }
        
        LearningProgress updated = progressRepository.save(progress);
        
        // Log activity
        recordActivity(user, word, LearningHistory.ActivityType.REVIEW, isCorrect);
        
        log.info("Word {} reviewed for user {}", wordId, user.getId());
        return LearningProgressResponse.fromEntity(updated);
    }
    
    /**
     * Get user's learning progress
     */
    public Page<LearningProgressResponse> getUserProgress(Long userId, Pageable pageable) {
        log.info("Getting learning progress for user {}", userId);
        
        Page<LearningProgress> progress = progressRepository.findByUserId(userId, pageable);
        return new PageImpl<>(
            progress.getContent().stream()
                .map(LearningProgressResponse::fromEntity)
                .collect(Collectors.toList()),
            pageable,
            progress.getTotalElements()
        );
    }
    
    /**
     * Get words that need review
     */
    public Page<LearningProgressResponse> getWordsNeedingReview(Long userId, Pageable pageable) {
        log.info("Getting words needing review for user {}", userId);
        
        Page<LearningProgress> progress = progressRepository.findWordsNeedingReview(
            userId, LocalDateTime.now(), pageable
        );
        return new PageImpl<>(
            progress.getContent().stream()
                .map(LearningProgressResponse::fromEntity)
                .collect(Collectors.toList()),
            pageable,
            progress.getTotalElements()
        );
    }
    
    /**
     * Get learning history
     */
    public Page<LearningHistoryResponse> getLearningHistory(Long userId, Pageable pageable) {
        log.info("Getting learning history for user {}", userId);
        
        Page<LearningHistory> history = historyRepository.findByUserId(userId, pageable);
        return new PageImpl<>(
            history.getContent().stream()
                .map(LearningHistoryResponse::fromEntity)
                .collect(Collectors.toList()),
            pageable,
            history.getTotalElements()
        );
    }
    
    /**
     * Get progress statistics
     */
    public ProgressStatisticsResponse getProgressStatistics(Long userId) {
        log.info("Getting progress statistics for user {}", userId);
        
        long totalWords = progressRepository.countByUserId(userId);
        long newWords = progressRepository.countByUserIdAndStatus(userId, LearningProgress.LearningStatus.NEW);
        long learningWords = progressRepository.countByUserIdAndStatus(userId, LearningProgress.LearningStatus.LEARNING);
        long masteredWords = progressRepository.countByUserIdAndStatus(userId, LearningProgress.LearningStatus.MASTERED);
        long wordsNeedingReview = progressRepository.findWordsNeedingReview(userId, LocalDateTime.now(), Pageable.unpaged()).getTotalElements();
        long totalActivities = historyRepository.countByUserId(userId);
        long correctAnswers = historyRepository.countCorrectByUser(userId);
        double accuracyRate = historyRepository.getAccuracyRate(userId);

        // Real Streak Calculation from unified database activities
        List<java.sql.Date> activitySqlDates = historyRepository.findActivityDatesByUserId(userId);
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

        // Longest Streak
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
        longestStreak = Math.max(longestStreak, currentStreak);

        // 7-day week progress (Mon to Sun or last 7 days)
        String[] dayNames = {"CN", "T2", "T3", "T4", "T5", "T6", "T7"};
        List<ProgressStatisticsResponse.DailyStreakStatus> weekProgress = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            int dayOfWeek = d.getDayOfWeek().getValue() % 7;
            weekProgress.add(ProgressStatisticsResponse.DailyStreakStatus.builder()
                .date(d.toString())
                .dayName(dayNames[dayOfWeek])
                .completed(activeDateSet.contains(d))
                .isToday(d.equals(today))
                .build());
        }

        // Today activities count & daily goal
        long todayActivitiesCount = historyRepository.countTodayActivities(userId, today.atStartOfDay());
        int dailyGoal = 10;

        // Real Points (Diamonds)
        long quizCount = quizAttemptRepository.findByUserId(userId, Pageable.unpaged()).getTotalElements();
        long speakingCount = speakingSessionRepository.findByUserId(userId, Pageable.unpaged()).getTotalElements();
        long totalPoints = (masteredWords * 20) + (learningWords * 10) + (quizCount * 15) + (speakingCount * 20);

        // Recent Activities Feed (Top recent items across quiz, speaking, vocab)
        final List<ProgressStatisticsResponse.RecentActivitySummary> recentActivities = new ArrayList<>();
        
        try {
            quizAttemptRepository.findByUserId(userId, PageRequest.of(0, 3)).forEach(qa -> {
                String qTitle = qa.getQuiz() != null ? qa.getQuiz().getTitle() : "Bài trắc nghiệm";
                recentActivities.add(ProgressStatisticsResponse.RecentActivitySummary.builder()
                    .type("QUIZ")
                    .title("Trắc nghiệm: " + qTitle)
                    .result("Điểm: " + String.format("%.0f", qa.getScore()) + "%")
                    .timeAgo(formatTimeAgo(qa.getCreatedAt()))
                    .createdAt(qa.getCreatedAt())
                    .build());
            });
        } catch (Exception e) {
            log.warn("Error fetching recent quizzes: {}", e.getMessage());
        }

        try {
            speakingSessionRepository.findByUserId(userId, PageRequest.of(0, 3)).forEach(ss -> {
                recentActivities.add(ProgressStatisticsResponse.RecentActivitySummary.builder()
                    .type("SPEAKING")
                    .title("Luyện nói: " + (ss.getTopic() != null ? ss.getTopic() : "Chủ đề tự do"))
                    .result(ss.getStatus() != null ? ss.getStatus().name() : "Luyện tập")
                    .timeAgo(formatTimeAgo(ss.getCreatedAt()))
                    .createdAt(ss.getCreatedAt())
                    .build());
            });
        } catch (Exception e) {
            log.warn("Error fetching recent speaking sessions: {}", e.getMessage());
        }

        try {
            historyRepository.findByUserId(userId, PageRequest.of(0, 3)).forEach(lh -> {
                String wordText = lh.getWord() != null ? lh.getWord().getWord() : "Từ vựng";
                String actName = lh.getActivityType() == LearningHistory.ActivityType.LEARN ? "Học từ mới: " : "Ôn tập: ";
                recentActivities.add(ProgressStatisticsResponse.RecentActivitySummary.builder()
                    .type("VOCABULARY")
                    .title(actName + wordText)
                    .result(Boolean.TRUE.equals(lh.getIsCorrect()) ? "Chính xác" : "Cần ôn thêm")
                    .timeAgo(formatTimeAgo(lh.getCreatedAt()))
                    .createdAt(lh.getCreatedAt())
                    .build());
            });
        } catch (Exception e) {
            log.warn("Error fetching recent vocab history: {}", e.getMessage());
        }

        recentActivities.sort((a, b) -> {
            if (a.getCreatedAt() == null || b.getCreatedAt() == null) return 0;
            return b.getCreatedAt().compareTo(a.getCreatedAt());
        });
        
        List<ProgressStatisticsResponse.RecentActivitySummary> displayedActivities = recentActivities.size() > 5
                ? recentActivities.subList(0, 5)
                : recentActivities;

        return ProgressStatisticsResponse.builder()
            .totalWords(totalWords)
            .newWords(newWords)
            .learningWords(learningWords)
            .masteredWords(masteredWords)
            .wordsNeedingReview(wordsNeedingReview)
            .totalActivities(totalActivities)
            .correctAnswers(correctAnswers)
            .overallAccuracyRate(accuracyRate)
            .currentStreak(currentStreak)
            .longestStreak(longestStreak)
            .todayCompleted(todayCompleted)
            .todayActivitiesCount(todayActivitiesCount)
            .dailyGoal(dailyGoal)
            .totalPoints(totalPoints)
            .weekProgress(weekProgress)
            .recentActivities(displayedActivities)
            .build();
    }

    private String formatTimeAgo(LocalDateTime dt) {
        if (dt == null) return "Gần đây";
        long minutes = java.time.Duration.between(dt, LocalDateTime.now()).toMinutes();
        if (minutes < 1) return "Vừa xong";
        if (minutes < 60) return minutes + " phút trước";
        long hours = minutes / 60;
        if (hours < 24) return hours + " giờ trước";
        long days = hours / 24;
        if (days == 1) return "Hôm qua";
        return days + " ngày trước";
    }
    
    /**
     * Record learning activity
     */
    @Transactional
    public void recordActivity(User user, Word word, LearningHistory.ActivityType activityType, Boolean isCorrect) {
        LearningHistory history = LearningHistory.builder()
            .user(user)
            .word(word)
            .activityType(activityType)
            .isCorrect(isCorrect)
            .build();
        
        historyRepository.save(history);
    }
    
    /**
     * Calculate next review date using Spaced Repetition System (SRS)
     * 
     * Intervals:
     * - Review 0: 1 day
     * - Review 1: 3 days
     * - Review 2: 7 days
     * - Review 3: 14 days
     * - Review 4+: 30 days
     */
    private LocalDateTime calculateNextReviewDate(Integer reviewCount) {
        int days = switch (reviewCount) {
            case 0 -> 1;
            case 1 -> 3;
            case 2 -> 7;
            case 3 -> 14;
            default -> 30;
        };
        return LocalDateTime.now().plusDays(days);
    }
}
