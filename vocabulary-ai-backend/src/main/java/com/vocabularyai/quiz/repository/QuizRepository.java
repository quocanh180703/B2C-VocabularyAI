package com.vocabularyai.quiz.repository;

import com.vocabularyai.quiz.entity.Quiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Quiz entity
 */
@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    
    /**
     * Find quizzes by category
     */
    Page<Quiz> findByCategoryId(Long categoryId, Pageable pageable);
    
    /**
     * Find quizzes by status
     */
    Page<Quiz> findByStatus(Quiz.QuizStatus status, Pageable pageable);
    
    /**
     * Find quizzes by level
     */
    Page<Quiz> findByLevel(Quiz.QuizLevel level, Pageable pageable);
}
