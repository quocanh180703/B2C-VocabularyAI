package com.vocabularyai.quiz.repository;

import com.vocabularyai.quiz.entity.QuizAttempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for QuizAttempt entity
 */
@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    
    /**
     * Get quiz attempts by user
     */
    Page<QuizAttempt> findByUserId(Long userId, Pageable pageable);
    
    /**
     * Get quiz attempts by quiz
     */
    Page<QuizAttempt> findByQuizId(Long quizId, Pageable pageable);
    
    /**
     * Get quiz attempts by user and quiz
     */
    Page<QuizAttempt> findByUserIdAndQuizId(Long userId, Long quizId, Pageable pageable);
    
    /**
     * Get average score for a quiz
     */
    @Query("SELECT AVG(qa.score) FROM QuizAttempt qa WHERE qa.quiz.id = :quizId")
    Double getAverageScore(@Param("quizId") Long quizId);
    
    /**
     * Count attempts by user
     */
    long countByUserId(Long userId);
    
    /**
     * Count completed attempts
     */
    @Query("SELECT COUNT(qa) FROM QuizAttempt qa WHERE qa.user.id = :userId AND qa.submittedAt IS NOT NULL")
    long countCompletedByUser(@Param("userId") Long userId);
    
    /**
     * Find quiz attempt by ID and verify user ownership
     */
    Optional<QuizAttempt> findByIdAndUserId(Long id, Long userId);
}
