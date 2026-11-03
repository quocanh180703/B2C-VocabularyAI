package com.vocabularyai.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.vocabularyai.user.entity.User;

import java.util.List;

/**
 * Admin Reports Repository
 * 
 * Provides queries for reporting and analytics
 */
@Repository
public interface AdminReportsRepository extends JpaRepository<User, Long> {
    
    /**
     * Get user engagement report (daily active users)
     * 
     * @return Map with date and active user counts
     */
    @Query("SELECT new map(" +
           "u.createdAt as date, " +
           "COUNT(u) as activeUsers) " +
           "FROM User u " +
           "WHERE u.createdAt >= CURRENT_TIMESTAMP - 30 DAY " +
           "GROUP BY DATE(u.createdAt) " +
           "ORDER BY DATE(u.createdAt)")
    List<?> getUserEngagementReport();
    
    /**
     * Get learning progress report (quiz attempt statistics)
     * 
     * @return Map with learning progress data
     */
    @Query("SELECT new map(" +
           "COUNT(DISTINCT qa.id) as totalAttempts, " +
           "COALESCE(AVG(qa.score), 0.0) as avgScore, " +
           "0 as avgCompletionTime, " +
           "COUNT(DISTINCT qa.user.id) as uniqueUsers) " +
           "FROM QuizAttempt qa")
    List<?> getLearningProgressReport();
    
    /**
     * Get content performance report (all quizzes with real attempt count and real avg score)
     * 
     * @return Map with quiz performance data
     */
    @Query("SELECT new map(" +
           "q.id as quizId, " +
           "q.title as quizTitle, " +
           "q.level as level, " +
           "COUNT(qa.id) as attemptCount, " +
           "COALESCE(AVG(qa.score), 0.0) as avgScore, " +
           "COALESCE(SUM(CASE WHEN qa.submittedAt IS NOT NULL THEN 1.0 ELSE 0.0 END), 0.0) as completedCount) " +
           "FROM Quiz q " +
           "LEFT JOIN QuizAttempt qa ON qa.quiz = q " +
           "GROUP BY q.id, q.title, q.level " +
           "ORDER BY COUNT(qa.id) DESC, q.id DESC")
    List<?> getContentPerformanceReport();
    
    /**
     * Get revenue report (payment statistics)
     * 
     * @return Map with revenue data
     */
    @Query("SELECT new map(" +
           "COUNT(p.id) as totalTransactions, " +
           "COALESCE(SUM(p.amount), 0.0) as totalRevenue, " +
           "COALESCE(AVG(p.amount), 0.0) as avgTransactionValue, " +
           "0 as uniqueCustomers) " +
           "FROM Payment p " +
           "WHERE p.status = 'SUCCESS'")
    List<?> getRevenueReport();
}
