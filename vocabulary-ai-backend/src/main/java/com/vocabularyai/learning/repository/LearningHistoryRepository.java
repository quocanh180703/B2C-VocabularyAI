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
    Page<LearningHistory> findByUserId(Long userId, Pageable pageable);
    
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
}
