package com.vocabularyai.quiz.repository;

import com.vocabularyai.quiz.entity.QuizAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for QuizAnswer entity
 */
@Repository
public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Long> {
    
    /**
     * Get answers for an attempt
     */
    List<QuizAnswer> findByAttemptId(Long attemptId);
    
    /**
     * Get specific answer
     */
    Optional<QuizAnswer> findByAttemptIdAndQuestionId(Long attemptId, Long questionId);
    
    /**
     * Count correct answers for attempt
     */
    long countByAttemptIdAndCorrectTrue(Long attemptId);
}
