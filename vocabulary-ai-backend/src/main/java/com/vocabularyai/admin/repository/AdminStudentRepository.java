package com.vocabularyai.admin.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vocabularyai.admin.entity.AdminQueryResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Admin Student Repository
 * 
 * Provides aggregation queries for student management
 * All queries return DTOs or primitive types for admin view
 */
@Repository
public interface AdminStudentRepository extends JpaRepository<AdminQueryResult, Long> {
    
    // ==================== Student List with Pagination ====================
    
    /**
     * Get paginated list of students with basic info
     * 
     * Format: [userId, username, email, fullName, status, createdAt]
     * 
     * @param pageable Pagination and sorting
     * @return Page of student data
     */
    @Query("SELECT new map(" +
           "u.id as userId, " +
           "u.username as username, " +
           "u.email as email, " +
           "u.fullName as fullName, " +
           "u.status as status, " +
           "COUNT(DISTINCT s.id) as totalSessions, " +
           "CASE WHEN MAX(sub.id) IS NOT NULL AND sub.status = 'ACTIVE' AND sub.endAt > CURRENT_TIMESTAMP " +
           "THEN true ELSE false END as hasActiveSubscription, " +
           "u.createdAt as createdAt) " +
           "FROM User u " +
           "LEFT JOIN LearningHistory s ON u.id = s.user.id " +
           "LEFT JOIN Subscription sub ON u.id = sub.userId " +
           "WHERE u.role = 'STUDENT' " +
           "GROUP BY u.id, u.username, u.email, u.fullName, u.status, u.createdAt")
    Page<?> getStudentList(Pageable pageable);
    
    /**
     * Get paginated list of students filtered by status
     * 
     * @param status User status (ACTIVE, INACTIVE, BANNED)
     * @param pageable Pagination
     * @return Page of filtered students
     */
    @Query("SELECT new map(" +
           "u.id as userId, " +
           "u.username as username, " +
           "u.email as email, " +
           "u.fullName as fullName, " +
           "u.status as status, " +
           "COUNT(DISTINCT s.id) as totalSessions, " +
           "u.createdAt as createdAt) " +
           "FROM User u " +
           "LEFT JOIN LearningHistory s ON u.id = s.user.id " +
           "WHERE u.role = 'STUDENT' AND u.status = :status " +
           "GROUP BY u.id, u.username, u.email, u.fullName, u.status, u.createdAt")
    Page<?> getStudentListByStatus(@Param("status") String status, Pageable pageable);
    
    /**
     * Search students by email or full name
     * 
     * @param searchTerm Email or name fragment
     * @param pageable Pagination
     * @return Page of matching students
     */
    @Query("SELECT new map(" +
           "u.id as userId, " +
           "u.username as username, " +
           "u.email as email, " +
           "u.fullName as fullName, " +
           "u.status as status, " +
           "COUNT(DISTINCT s.id) as totalSessions, " +
           "u.createdAt as createdAt) " +
           "FROM User u " +
           "LEFT JOIN LearningHistory s ON u.id = s.user.id " +
           "WHERE u.role = 'STUDENT' AND " +
           "(LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "GROUP BY u.id, u.username, u.email, u.fullName, u.status, u.createdAt " +
           "ORDER BY CASE " +
           "WHEN LOWER(u.username) = LOWER(:searchTerm) OR LOWER(u.email) = LOWER(:searchTerm) OR LOWER(u.fullName) = LOWER(:searchTerm) THEN 0 " +
           "WHEN LOWER(u.username) LIKE LOWER(CONCAT(:searchTerm, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT(:searchTerm, '%')) OR LOWER(u.fullName) LIKE LOWER(CONCAT(:searchTerm, '%')) THEN 1 " +
           "ELSE 2 END, u.createdAt DESC")
    Page<?> searchStudents(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // ==================== Student Detail ====================
    
    /**
     * Get complete student information
     * 
     * Includes learning and subscription stats
     * 
     * @param userId User ID to fetch
     * @return Map with complete student data
     */
    @Query("SELECT new map(" +
           "u.id as userId, " +
           "u.username as username, " +
           "u.email as email, " +
           "u.fullName as fullName, " +
           "u.avatarUrl as avatarUrl, " +
           "u.status as status, " +
           "u.createdAt as createdAt, " +
           "u.updatedAt as updatedAt, " +
           "COUNT(DISTINCT lh.id) as totalSessions, " +
           "COUNT(DISTINCT fw.id) as wordsLearned, " +
           "COALESCE(CAST(SUM(CASE WHEN lh.isCorrect = true THEN 1 ELSE 0 END) * 100.0 / " +
           "NULLIF(COUNT(lh), 0) AS java.math.BigDecimal), 0) as accuracyRate, " +
           "COALESCE(sub.id, 0) as activeSubscriptionId, " +
           "COALESCE(sub.status, 'NONE') as subscriptionStatus, " +
           "COALESCE(sub.startAt, NULL) as subscriptionStartAt, " +
           "COALESCE(sub.endAt, NULL) as subscriptionEndAt, " +
           "COALESCE(pkg.name, '') as packageName, " +
           "COALESCE(MAX(lh.createdAt), NULL) as lastActivityAt) " +
           "FROM User u " +
           "LEFT JOIN LearningHistory lh ON u.id = lh.user.id " +
           "LEFT JOIN FavoriteWord fw ON u.id = fw.user.id " +
           "LEFT JOIN Subscription sub ON u.id = sub.userId AND sub.status = 'ACTIVE' " +
           "LEFT JOIN Package pkg ON sub.packageId = pkg.id " +
           "WHERE u.id = :userId AND u.role = 'STUDENT' " +
           "GROUP BY u.id, u.username, u.email, u.fullName, u.avatarUrl, u.status, " +
           "u.createdAt, u.updatedAt, sub.id, sub.status, sub.startAt, sub.endAt, pkg.name")
    List<?> getStudentDetail(@Param("userId") Long userId);
    
    /**
     * Get student payment history (total paid, count, last payment)
     * 
     * @param userId User ID
     * @return Map with [totalPaid, paymentCount, lastPaymentAt]
     */
    @Query("SELECT new map(" +
           "COALESCE(SUM(p.amount), 0) as totalPaid, " +
           "COUNT(p) as paymentCount, " +
           "MAX(p.paidAt) as lastPaymentAt) " +
           "FROM Payment p " +
           "JOIN PaymentOrder po ON p.paymentOrderId = po.id " +
           "WHERE po.userId = :userId AND p.status = 'SUCCESS'")
    List<?> getStudentPaymentInfo(@Param("userId") Long userId);
    
    // ==================== Student Statistics ====================
    
    /**
     * Get total number of students by status
     * 
     * @return Map with [status, count]
     */
    @Query("SELECT new map(" +
           "u.status as status, " +
           "COUNT(u) as count) " +
           "FROM User u " +
           "WHERE u.role = 'STUDENT' " +
           "GROUP BY u.status")
    List<?> getStudentCountByStatus();
    
    /**
     * Get count of students with active subscriptions
     * 
     * @return Active subscription count
     */
    @Query("SELECT COUNT(DISTINCT sub.userId) FROM Subscription sub " +
           "WHERE sub.status = 'ACTIVE' AND sub.endAt > CURRENT_TIMESTAMP")
    long countActiveSubscriptions();
    
    /**
     * Get average sessions per student
     * 
     * @return Average number of sessions
     */
    @Query("SELECT COALESCE(AVG(sessionCount), 0) FROM " +
           "(SELECT COUNT(lh) as sessionCount FROM LearningHistory lh " +
           "JOIN User u ON lh.user.id = u.id WHERE u.role = 'STUDENT' " +
           "GROUP BY u.id) subquery")
    double getAvgSessionsPerStudent();
    
    /**
     * Get average accuracy rate across students
     * 
     * @return Average accuracy (0-100)
     */
    @Query("SELECT COALESCE(AVG(accuracy), 0) FROM " +
           "(SELECT COALESCE(SUM(CASE WHEN lh.isCorrect = true THEN 1 ELSE 0 END) * 100.0 / " +
           "NULLIF(COUNT(lh), 0), 0) as accuracy " +
           "FROM LearningHistory lh " +
           "JOIN User u ON lh.user.id = u.id " +
           "WHERE u.role = 'STUDENT' " +
           "GROUP BY u.id) subquery")
    double getAvgAccuracyRate();
    
    /**
     * Get count of students active today
     * 
     * @return Number of students with activity today
     */
    @Query("SELECT COUNT(DISTINCT lh.user.id) FROM LearningHistory lh " +
           "WHERE CAST(lh.createdAt AS DATE) = CURRENT_DATE")
    long countActiveTodayStudents();
    
    /**
     * Get count of new students registered this month
     * 
     * @return New student count
     */
    @Query("SELECT COUNT(u) FROM User u " +
           "WHERE u.role = 'STUDENT' AND " +
           "YEAR(u.createdAt) = YEAR(CURRENT_DATE) AND " +
           "MONTH(u.createdAt) = MONTH(CURRENT_DATE)")
    long countNewStudentsThisMonth();
    
    /**
     * Get average words learned per student
     * 
     * @return Average number of words
     */
    @Query("SELECT COALESCE(AVG(wordCount), 0) FROM " +
           "(SELECT COUNT(DISTINCT fw.id) as wordCount FROM FavoriteWord fw " +
           "JOIN User u ON fw.user.id = u.id WHERE u.role = 'STUDENT' " +
           "GROUP BY u.id) subquery")
    long getAvgWordsPerStudent();
}
