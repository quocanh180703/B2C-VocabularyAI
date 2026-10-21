package com.vocabularyai.quiz.repository;

import com.vocabularyai.quiz.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Question entity
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    
    /**
     * Find all questions for a quiz
     */
    List<Question> findByQuizId(Long quizId);
    
    /**
     * Count questions in quiz
     */
    long countByQuizId(Long quizId);

    /**
     * Delete questions by quiz ID
     */
    void deleteByQuizId(Long quizId);
}
