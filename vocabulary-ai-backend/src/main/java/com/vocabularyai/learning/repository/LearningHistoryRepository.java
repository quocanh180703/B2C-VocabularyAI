package com.vocabularyai.learning.repository;

import com.vocabularyai.learning.entity.LearningHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Repository for LearningHistory entity
 */
@Repository
public interface LearningHistoryRepository extends JpaRepository<LearningHistory, Long> {
    
    /**
     * Get learning history for user with pagination
     */
    @Query(value = "SELECT lh FROM LearningHistory lh JOIN FETCH lh.word WHERE lh.user.id = :userId",
           countQuery = "SELECT COUNT(lh) FROM LearningHistory lh WHERE lh.user.id = :userId")
    Page<LearningHistory> findByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Get learning history by activity type
     */
    Page<LearningHistory> findByUserIdAndActivityType(Long userId, LearningHistory.ActivityType activityType, Pageable pageable);
    
    /**
     * Get learning history for specific word
     */
    Page<LearningHistory> findByUserIdAndWordId(Long userId, Long wordId, Pageable pageable);
    
    /**
     * Get learning history between dates
     */
    @Query("SELECT lh FROM LearningHistory lh WHERE " +
           "lh.user.id = :userId AND " +
           "lh.createdAt BETWEEN :startDate AND :endDate")
    Page<LearningHistory> findByDateRange(@Param("userId") Long userId, 
                                         @Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate,
                                         Pageable pageable);
    
    /**
     * Count activities by user
     */
    long countByUserId(Long userId);
    
    /**
     * Count activities by user and type
     */
    long countByUserIdAndActivityType(Long userId, LearningHistory.ActivityType activityType);
    
    /**
     * Count correct answers
     */
    @Query("SELECT COUNT(lh) FROM LearningHistory lh WHERE " +
           "lh.user.id = :userId AND " +
           "lh.isCorrect = true")
    long countCorrectByUser(@Param("userId") Long userId);
    
    /**
     * Get accuracy rate
     */
    @Query("SELECT " +
           "COALESCE(SUM(CASE WHEN lh.isCorrect = true THEN 1 ELSE 0 END), 0) * 100.0 / " +
           "COALESCE(SUM(CASE WHEN lh.isCorrect IS NOT NULL THEN 1 ELSE 0 END), 1) " +
           "FROM LearningHistory lh WHERE lh.user.id = :userId")
    double getAccuracyRate(@Param("userId") Long userId);

    /**
     * Get distinct activity dates for user across vocabulary, quizzes, and speaking
     */
    @Query(value = "SELECT DISTINCT DATE(created_at) as act_date FROM (" +
           "  SELECT created_at FROM learning_history WHERE user_id = :userId " +
           "  UNION ALL " +
           "  SELECT created_at FROM quiz_attempts WHERE user_id = :userId " +
           "  UNION ALL " +
           "  SELECT created_at FROM speaking_sessions WHERE user_id = :userId" +
           ") combined ORDER BY act_date DESC", nativeQuery = true)
    java.util.List<java.sql.Date> findActivityDatesByUserId(@Param("userId") Long userId);

    /**
     * Count activities for user today
     */
    @Query(value = "SELECT COUNT(*) FROM (" +
           "  SELECT id FROM learning_history WHERE user_id = :userId AND created_at >= :startOfDay " +
           "  UNION ALL " +
           "  SELECT id FROM quiz_attempts WHERE user_id = :userId AND created_at >= :startOfDay " +
           "  UNION ALL " +
           "  SELECT id FROM speaking_sessions WHERE user_id = :userId AND created_at >= :startOfDay" +
           ") combined", nativeQuery = true)
    long countTodayActivities(@Param("userId") Long userId, @Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT COUNT(lh) FROM LearningHistory lh WHERE lh.user.id = :userId AND lh.createdAt >= :since")
    long countActivitiesSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(lh) FROM LearningHistory lh WHERE lh.user.id = :userId AND lh.createdAt >= :startDate AND lh.createdAt < :endDate")
    long countActivitiesBetween(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT " +
           "COALESCE(SUM(CASE WHEN lh.isCorrect = true THEN 1 ELSE 0 END), 0) * 100.0 / " +
           "COALESCE(SUM(CASE WHEN lh.isCorrect IS NOT NULL THEN 1 ELSE 0 END), 1) " +
           "FROM LearningHistory lh WHERE lh.user.id = :userId AND lh.createdAt >= :since")
    Double getAccuracyRateSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Query("SELECT lh.activityType, COUNT(lh) FROM LearningHistory lh WHERE lh.user.id = :userId AND lh.createdAt >= :startOfDay GROUP BY lh.activityType")
    java.util.List<Object[]> countTodayByActivityType(@Param("userId") Long userId, @Param("startOfDay") LocalDateTime startOfDay);
}
