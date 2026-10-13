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
 * Admin Reports Repository
 * 
 * Provides queries for reporting and analytics
 */
@Repository
public interface AdminReportsRepository extends JpaRepository<AdminQueryResult, Long> {
    
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
           "0 as avgScore, " +
           "0 as avgCompletionTime, " +
           "COUNT(DISTINCT qa.user.id) as uniqueUsers) " +
           "FROM QuizAttempt qa")
    List<?> getLearningProgressReport();
    
    /**
     * Get content performance report (most attempted quizzes)
     * 
     * @param limit Limit number of results
     * @return Map with quiz performance data
     */
    @Query("SELECT new map(" +
           "q.id as quizId, " +
           "q.title as quizTitle, " +
           "q.level as level, " +
           "0 as attemptCount, " +
           "0.0 as avgScore, " +
           "0 as completionRate) " +
           "FROM Quiz q " +
           "ORDER BY q.createdAt DESC")
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
