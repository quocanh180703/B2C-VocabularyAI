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
     * Count questions grouped by quiz ID
     */
    @org.springframework.data.jpa.repository.Query("SELECT q.quiz.id, count(q) FROM Question q GROUP BY q.quiz.id")
    List<Object[]> countQuestionsGroupByQuizId();

    /**
     * Delete questions by quiz ID
     */
    void deleteByQuizId(Long quizId);

    /**
     * Find all questions belonging to PUBLISHED quizzes for placement testing
     */
    @org.springframework.data.jpa.repository.Query("SELECT q FROM Question q JOIN FETCH q.quiz quiz WHERE quiz.status = com.vocabularyai.quiz.entity.Quiz.QuizStatus.PUBLISHED")
    List<Question> findAllPublishedQuestions();
}
