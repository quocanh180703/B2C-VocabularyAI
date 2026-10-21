package com.vocabularyai.learning.repository;

import com.vocabularyai.learning.entity.LearningProgress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
}
