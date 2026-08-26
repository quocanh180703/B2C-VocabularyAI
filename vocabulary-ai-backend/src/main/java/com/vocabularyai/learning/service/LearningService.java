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
import com.vocabularyai.vocabulary.entity.Word;
import com.vocabularyai.vocabulary.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Learning Service
 * 
 * Business logic for learning progress and history management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LearningService {
    
    private final LearningProgressRepository progressRepository;
    private final LearningHistoryRepository historyRepository;
    private final WordRepository wordRepository;
    
    /**
     * Start learning a word (create initial progress)
     */
    @Transactional
    public LearningProgressResponse startLearningWord(User user, Long wordId) {
        log.info("User {} starting to learn word {}", user.getId(), wordId);
        
        // Check if word exists
        Word word = wordRepository.findById(wordId)
            .orElseThrow(() -> ResourceNotFoundException.of("Word", "id", wordId));
        
        // Check if already learning
        if (progressRepository.findByUserIdAndWordId(user.getId(), wordId).isPresent()) {
            throw new BadRequestException("Already learning this word");
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
        
        log.info("Started learning word {} for user {}", wordId, user.getId());
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
        
        return ProgressStatisticsResponse.builder()
            .totalWords(totalWords)
            .newWords(newWords)
            .learningWords(learningWords)
            .masteredWords(masteredWords)
            .wordsNeedingReview(wordsNeedingReview)
            .totalActivities(totalActivities)
            .correctAnswers(correctAnswers)
            .overallAccuracyRate(accuracyRate)
            .build();
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
