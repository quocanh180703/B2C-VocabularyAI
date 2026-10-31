package com.vocabularyai.learning.repository;

import com.vocabularyai.learning.entity.LearningProgress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for LearningProgress entity
 */
@Repository
public interface LearningProgressRepository extends JpaRepository<LearningProgress, Long> {
    
    /**
     * Find learning progress by user and word
     */
    Optional<LearningProgress> findByUserIdAndWordId(Long userId, Long wordId);
    
    /**
     * Get all learning progress for user with pagination
     */
    @Query(value = "SELECT lp FROM LearningProgress lp JOIN FETCH lp.word WHERE lp.user.id = :userId",
           countQuery = "SELECT COUNT(lp) FROM LearningProgress lp WHERE lp.user.id = :userId")
    Page<LearningProgress> findByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Get learning progress by status
     */
    @Query(value = "SELECT lp FROM LearningProgress lp JOIN FETCH lp.word WHERE lp.user.id = :userId AND lp.status = :status",
           countQuery = "SELECT COUNT(lp) FROM LearningProgress lp WHERE lp.user.id = :userId AND lp.status = :status")
    Page<LearningProgress> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") LearningProgress.LearningStatus status, Pageable pageable);
    
    /**
     * Get words that need review (next_review_at is past current time)
     */
    @Query(value = "SELECT lp FROM LearningProgress lp JOIN FETCH lp.word WHERE " +
           "lp.user.id = :userId AND " +
           "lp.nextReviewAt IS NOT NULL AND " +
           "lp.nextReviewAt <= :now",
           countQuery = "SELECT COUNT(lp) FROM LearningProgress lp WHERE " +
           "lp.user.id = :userId AND " +
           "lp.nextReviewAt IS NOT NULL AND " +
           "lp.nextReviewAt <= :now")
    Page<LearningProgress> findWordsNeedingReview(@Param("userId") Long userId, @Param("now") LocalDateTime now, Pageable pageable);
    
    /**
     * Count learning progress by status
     */
    long countByUserIdAndStatus(Long userId, LearningProgress.LearningStatus status);
    
    /**
     * Get total words learned by user
     */
    long countByUserId(Long userId);

    /**
     * Find candidate user IDs for REVIEW_DUE email:
     * - User has reminders enabled and is active
     * - User has at least 1 word needing review (next_review_at <= now)
     * - User has NOT studied today (no learning_history, quiz_attempts, speaking_sessions today)
     * - User has NOT received REVIEW_DUE today
     */
    @Query(value = "SELECT DISTINCT lp.user_id " +
           "FROM learning_progress lp " +
           "JOIN users u ON lp.user_id = u.id " +
           "WHERE u.email_reminders_enabled = true " +
           "  AND u.status = 'ACTIVE' " +
           "  AND lp.next_review_at IS NOT NULL " +
           "  AND lp.next_review_at <= :now " +
           "  AND NOT EXISTS (" +
           "      SELECT 1 FROM learning_history lh WHERE lh.user_id = u.id AND lh.created_at >= :todayStart" +
           "  ) " +
           "  AND NOT EXISTS (" +
           "      SELECT 1 FROM quiz_attempts qa WHERE qa.user_id = u.id AND qa.created_at >= :todayStart" +
           "  ) " +
           "  AND NOT EXISTS (" +
           "      SELECT 1 FROM speaking_sessions ss WHERE ss.user_id = u.id AND ss.created_at >= :todayStart" +
           "  ) " +
           "  AND NOT EXISTS (" +
           "      SELECT 1 FROM email_reminder_log erl " +
           "      WHERE erl.user_id = u.id AND erl.type = 'REVIEW_DUE' AND erl.sent_date = :todayDate AND erl.status = 'SENT'" +
           "  )",
           nativeQuery = true)
    List<Long> findCandidateUserIdsForReviewDue(
            @Param("now") LocalDateTime now,
            @Param("todayStart") LocalDateTime todayStart,
            @Param("todayDate") LocalDate todayDate
    );

    /**
     * Find candidate user IDs for STREAK_SAVE email:
     * - User has reminders enabled and is active
     * - User has NOT studied today
     * - User has NOT received STREAK_SAVE today
     */
    @Query(value = "SELECT DISTINCT u.id " +
           "FROM users u " +
           "WHERE u.email_reminders_enabled = true " +
           "  AND u.status = 'ACTIVE' " +
           "  AND NOT EXISTS (" +
           "      SELECT 1 FROM learning_history lh WHERE lh.user_id = u.id AND lh.created_at >= :todayStart" +
           "  ) " +
           "  AND NOT EXISTS (" +
           "      SELECT 1 FROM quiz_attempts qa WHERE qa.user_id = u.id AND qa.created_at >= :todayStart" +
           "  ) " +
           "  AND NOT EXISTS (" +
           "      SELECT 1 FROM speaking_sessions ss WHERE ss.user_id = u.id AND ss.created_at >= :todayStart" +
           "  ) " +
           "  AND NOT EXISTS (" +
           "      SELECT 1 FROM email_reminder_log erl " +
           "      WHERE erl.user_id = u.id AND erl.type = 'STREAK_SAVE' AND erl.sent_date = :todayDate AND erl.status = 'SENT'" +
           "  )",
           nativeQuery = true)
    List<Long> findCandidateUserIdsForStreakSave(
            @Param("todayStart") LocalDateTime todayStart,
            @Param("todayDate") LocalDate todayDate
    );

    long countByUserIdAndNextReviewAtLessThanEqual(Long userId, LocalDateTime dateTime);

    @Query("SELECT COUNT(lp) FROM LearningProgress lp WHERE lp.user.id = :userId AND lp.nextReviewAt IS NOT NULL AND lp.nextReviewAt < :startOfToday")
    long countOverdueReviews(@Param("userId") Long userId, @Param("startOfToday") LocalDateTime startOfToday);

    @Query("SELECT lp.word.word, lp.wrongCount FROM LearningProgress lp " +
           "WHERE lp.user.id = :userId AND lp.status != com.vocabularyai.learning.entity.LearningProgress.LearningStatus.MASTERED AND lp.wrongCount > 0 " +
           "ORDER BY lp.wrongCount DESC")
    List<Object[]> findTopForgottenWords(@Param("userId") Long userId, Pageable pageable);
}
