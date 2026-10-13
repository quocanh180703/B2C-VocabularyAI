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
 * Admin Speaking Repository
 */
@Repository
public interface AdminSpeakingRepository extends JpaRepository<AdminQueryResult, Long> {
    
    /**
     * Get paginated list of all speaking sessions
     */
    @Query("SELECT new map(" +
           "ss.id as id, " +
           "u.id as userId, " +
           "u.email as userEmail, " +
           "ss.topic as topic, " +
           "ss.level as level, " +
           "ss.status as status, " +
           "0 as attemptCount, " +
           "ss.overallScore as overallScore, " +
           "ss.startedAt as startedAt, " +
           "ss.completedAt as completedAt, " +
           "ss.createdAt as createdAt) " +
           "FROM SpeakingSession ss " +
           "LEFT JOIN ss.user u " +
           "ORDER BY ss.createdAt DESC")
    Page<?> getSpeakingSessions(Pageable pageable);
    
    /**
     * Get speaking sessions for specific user
     */
    @Query("SELECT new map(" +
           "ss.id as id, " +
           "u.id as userId, " +
           "u.email as userEmail, " +
           "ss.topic as topic, " +
           "ss.level as level, " +
           "ss.status as status, " +
           "0 as attemptCount, " +
           "ss.overallScore as overallScore, " +
           "ss.startedAt as startedAt, " +
           "ss.completedAt as completedAt, " +
           "ss.createdAt as createdAt) " +
           "FROM SpeakingSession ss " +
           "LEFT JOIN ss.user u " +
           "WHERE u.id = :userId " +
           "ORDER BY ss.createdAt DESC")
    Page<?> getSpeakingSessionsByUser(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Get speaking session detail
     */
    @Query("SELECT new map(" +
           "ss.id as id, " +
           "u.id as userId, " +
           "u.email as userEmail, " +
           "ss.topic as topic, " +
           "ss.description as description, " +
           "ss.level as level, " +
           "ss.status as status, " +
           "0 as attemptCount, " +
           "ss.overallScore as overallScore, " +
           "ss.startedAt as startedAt, " +
           "ss.completedAt as completedAt, " +
           "ss.createdAt as createdAt) " +
           "FROM SpeakingSession ss " +
           "LEFT JOIN ss.user u " +
           "WHERE ss.id = :sessionId")
    List<?> getSpeakingSessionDetail(@Param("sessionId") Long sessionId);
    
    /**
     * Get speaking statistics by status
     */
    @Query("SELECT new map(" +
           "ss.status as status, " +
           "COUNT(ss) as count) " +
           "FROM SpeakingSession ss " +
           "GROUP BY ss.status")
    List<?> getSessionCountByStatus();
    
    /**
     * Get speaking statistics by level
     */
    @Query("SELECT new map(" +
           "ss.level as level, " +
           "COUNT(ss) as count) " +
           "FROM SpeakingSession ss " +
           "GROUP BY ss.level")
    List<?> getSessionCountByLevel();
    
    /**
     * Get average score and attempt count
     */
    @Query("SELECT new map(" +
           "COALESCE(AVG(ss.overallScore), 0.0) as avgScore, " +
           "0.0 as avgAttempts, " +
           "COUNT(DISTINCT ss.user.id) as activeUsers) " +
           "FROM SpeakingSession ss")
    List<?> getPerformanceStats();
}
