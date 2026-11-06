package com.vocabularyai.quiz.repository;

import com.vocabularyai.quiz.entity.Quiz;
import com.vocabularyai.quiz.entity.QuizVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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
     * Find quizzes by status with category eagerly loaded
     */
    @EntityGraph(attributePaths = {"category", "createdBy"})
    Page<Quiz> findByStatus(Quiz.QuizStatus status, Pageable pageable);
    
    /**
     * Find quiz by id with category and creator eagerly loaded
     */
    @EntityGraph(attributePaths = {"category", "createdBy"})
    Optional<Quiz> findById(Long id);

    /**
     * Find quizzes by level
     */
    Page<Quiz> findByLevel(Quiz.QuizLevel level, Pageable pageable);

    /**
     * Find quizzes created by user (My Quizzes)
     */
    @EntityGraph(attributePaths = {"category", "createdBy"})
    Page<Quiz> findByCreatedById(Long userId, Pageable pageable);

    /**
     * Find quiz by id and creator id
     */
    @EntityGraph(attributePaths = {"category", "createdBy"})
    Optional<Quiz> findByIdAndCreatedById(Long id, Long userId);

    /**
     * Find public published quizzes
     */
    @EntityGraph(attributePaths = {"category", "createdBy"})
    Page<Quiz> findByVisibilityAndStatus(QuizVisibility visibility, Quiz.QuizStatus status, Pageable pageable);

    /**
     * Find quizzes by visibility for review
     */
    @EntityGraph(attributePaths = {"category", "createdBy", "reviewedBy"})
    Page<Quiz> findByVisibility(QuizVisibility visibility, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "createdBy"})
    Page<Quiz> findByVisibilityIn(java.util.Collection<QuizVisibility> visibilities, Pageable pageable);
}
