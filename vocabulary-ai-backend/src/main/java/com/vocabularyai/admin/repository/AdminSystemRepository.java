package com.vocabularyai.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.vocabularyai.admin.entity.AdminQueryResult;

import java.util.List;

/**
 * Admin System Repository
 * 
 * Provides queries for admin system management (dashboard, settings, etc.)
 */
@Repository
public interface AdminSystemRepository extends JpaRepository<AdminQueryResult, Long> {
    
    /**
     * Get system overview statistics
     * 
     * @return Map with overall system stats
     */
    @Query("SELECT new map(" +
           "COUNT(DISTINCT u.id) as totalUsers, " +
           "0 as activeUsersToday, " +
           "0 as totalSessions, " +
           "0 as totalQuizzes) " +
           "FROM User u")
    List<?> getSystemOverview();
    
    /**
     * Get user registration trend (last 30 days)
     * 
     * @return Map with user registration counts by date
     */
    @Query("SELECT new map(" +
           "u.createdAt as date, " +
           "COUNT(u) as count) " +
           "FROM User u " +
           "WHERE u.createdAt >= CURRENT_TIMESTAMP - 30 DAY " +
           "GROUP BY DATE(u.createdAt) " +
           "ORDER BY DATE(u.createdAt)")
    List<?> getUserRegistrationTrend();
    
    /**
     * Get system health metrics
     * 
     * @return Map with system health information
     */
    @Query("SELECT new map(" +
           "COUNT(DISTINCT u.id) as totalUsers, " +
           "0 as apiCallsToday, " +
           "0 as averageResponseTime, " +
           "0 as errorRate) " +
           "FROM User u")
    List<?> getSystemHealth();
}
