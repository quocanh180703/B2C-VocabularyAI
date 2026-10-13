package com.vocabularyai.admin.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vocabularyai.admin.entity.AdminQueryResult;

import java.util.List;

/**
 * Admin Quiz Repository
 * 
 * Provides queries for quiz management (admin system-wide view)
 * Supports listing, searching, and filtering quizzes
 */
@Repository
public interface AdminQuizRepository extends JpaRepository<AdminQueryResult, Long> {
    
    // ==================== Quiz List ====================
    
    /**
     * Get paginated list of all quizzes
     * 
     * @param pageable Pagination and sorting
     * @return Page of quizzes
     */
    @Query("SELECT new map(" +
           "q.id as id, " +
           "q.title as title, " +
           "q.description as description, " +
           "q.level as level, " +
           "c.name as category, " +
           "q.timeLimit as timeLimit, " +
           "q.status as status, " +
           "0 as questionCount, " +
           "0 as attemptCount, " +
           "0.0 as avgScore, " +
           "q.createdAt as createdAt, " +
           "q.updatedAt as updatedAt) " +
           "FROM Quiz q " +
           "LEFT JOIN q.category c " +
           "ORDER BY q.createdAt DESC")
    Page<?> getQuizList(Pageable pageable);
    
    /**
     * Get quizzes filtered by level
     * 
     * @param level Quiz level (A1, A2, B1, B2, C1, C2)
     * @param pageable Pagination
     * @return Page of quizzes at this level
     */
    @Query("SELECT new map(" +
           "q.id as id, " +
           "q.title as title, " +
           "q.description as description, " +
           "q.level as level, " +
           "c.name as category, " +
           "q.timeLimit as timeLimit, " +
           "q.status as status, " +
           "0 as questionCount, " +
           "0 as attemptCount, " +
           "0.0 as avgScore, " +
           "q.createdAt as createdAt, " +
           "q.updatedAt as updatedAt) " +
           "FROM Quiz q " +
           "LEFT JOIN q.category c " +
           "WHERE q.level = :level " +
           "ORDER BY q.createdAt DESC")
    Page<?> getQuizzesByLevel(@Param("level") String level, Pageable pageable);
    
    /**
     * Search quizzes by title or description
     * 
     * @param searchTerm Search query (case-insensitive)
     * @param pageable Pagination
     * @return Page of matching quizzes
     */
    @Query("SELECT new map(" +
           "q.id as id, " +
           "q.title as title, " +
           "q.description as description, " +
           "q.level as level, " +
           "c.name as category, " +
           "q.timeLimit as timeLimit, " +
           "q.status as status, " +
           "0 as questionCount, " +
           "0 as attemptCount, " +
           "0.0 as avgScore, " +
           "q.createdAt as createdAt, " +
           "q.updatedAt as updatedAt) " +
           "FROM Quiz q " +
           "LEFT JOIN q.category c " +
           "WHERE LOWER(q.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(q.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY q.createdAt DESC")
    Page<?> searchQuizzes(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Get quizzes by status (DRAFT, PUBLISHED, ARCHIVED)
     * 
     * @param status Quiz status
     * @param pageable Pagination
     * @return Page of quizzes with this status
     */
    @Query("SELECT new map(" +
           "q.id as id, " +
           "q.title as title, " +
           "q.description as description, " +
           "q.level as level, " +
           "c.name as category, " +
           "q.timeLimit as timeLimit, " +
           "q.status as status, " +
           "0 as questionCount, " +
           "0 as attemptCount, " +
           "0.0 as avgScore, " +
           "q.createdAt as createdAt, " +
           "q.updatedAt as updatedAt) " +
           "FROM Quiz q " +
           "LEFT JOIN q.category c " +
           "WHERE q.status = :status " +
           "ORDER BY q.createdAt DESC")
    Page<?> getQuizzesByStatus(@Param("status") String status, Pageable pageable);
    
    // ==================== Quiz Detail ====================
    
    /**
     * Get complete quiz information
     * 
     * @param quizId Quiz ID
     * @return Map with quiz details and statistics
     */
    @Query("SELECT new map(" +
           "q.id as id, " +
           "q.title as title, " +
           "q.description as description, " +
           "q.level as level, " +
           "c.name as category, " +
           "c.id as categoryId, " +
           "q.timeLimit as timeLimit, " +
           "q.status as status, " +
           "0 as questionCount, " +
           "0 as totalAttempts, " +
           "0 as completedAttempts, " +
           "0.0 as avgScore, " +
           "0.0 as passRate, " +
           "q.createdAt as createdAt, " +
           "q.updatedAt as updatedAt) " +
           "FROM Quiz q " +
           "LEFT JOIN q.category c " +
           "WHERE q.id = :quizId")
    List<?> getQuizDetail(@Param("quizId") Long quizId);
    
    // ==================== Statistics ====================
    
    /**
     * Get quiz count by status
     * 
     * @return Map with counts per status
     */
    @Query("SELECT new map(" +
           "q.status as status, " +
           "COUNT(q) as count) " +
           "FROM Quiz q " +
           "GROUP BY q.status")
    List<?> getQuizCountByStatus();
    
    /**
     * Get quiz count by level
     * 
     * @return Map with counts per level
     */
    @Query("SELECT new map(" +
           "q.level as level, " +
           "COUNT(q) as count) " +
           "FROM Quiz q " +
           "GROUP BY q.level")
    List<?> getQuizCountByLevel();
}
