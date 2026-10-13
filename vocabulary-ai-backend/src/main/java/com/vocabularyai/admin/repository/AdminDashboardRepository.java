package com.vocabularyai.admin.repository;

import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.math.BigDecimal;
import java.util.List;

/**
 * Admin Dashboard Repository
 * 
 * Provides aggregation queries for dashboard metrics
 * All queries return DTOs or primitive types suitable for dashboard display
 * 
 * Query Types:
 * - System-wide metrics (COUNT, SUM, AVG)
 * - Time-series data (GROUP BY DATE)
 * - Top performers (ORDER BY DESC LIMIT)
 */
@Repository
public class AdminDashboardRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    // ==================== System-Wide Metrics ====================
    
    /**
     * Get total count of registered users
     * 
     * @return Total user count
     */
    public long countTotalStudents() {
        Query query = entityManager.createQuery("SELECT COUNT(u) FROM User u WHERE u.role = 'STUDENT'");
        return (long) query.getSingleResult();
    }
    
    /**
     * Get count of active subscriptions
     * 
     * Active = status='ACTIVE' AND endAt > now
     * 
     * @return Active subscription count
     */
    public long countActiveSubscriptions() {
        Query query = entityManager.createQuery(
            "SELECT COUNT(s) FROM Subscription s WHERE s.status = 'ACTIVE' AND s.endAt > CURRENT_TIMESTAMP");
        return (long) query.getSingleResult();
    }
    
    /**
     * Get total revenue from all successful payments
     * 
     * @return Sum of all payment amounts
     */
    public BigDecimal getTotalRevenue() {
        Query query = entityManager.createQuery(
            "SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'SUCCESS'");
        Object result = query.getSingleResult();
        return result instanceof BigDecimal ? (BigDecimal) result : BigDecimal.valueOf(((Number) result).doubleValue());
    }
    
    /**
     * Get today's revenue (since 00:00:00)
     * 
     * @return Sum of today's payment amounts
     */
    public BigDecimal getRevenueToday() {
        Query query = entityManager.createQuery(
            "SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'SUCCESS' AND CAST(p.paidAt AS DATE) = CURRENT_DATE");
        Object result = query.getSingleResult();
        return result instanceof BigDecimal ? (BigDecimal) result : BigDecimal.valueOf(((Number) result).doubleValue());
    }
    
    /**
     * Get this month's revenue (from first day to now)
     * 
     * @return Sum of this month's payment amounts
     */
    public BigDecimal getRevenueThisMonth() {
        Query query = entityManager.createQuery(
            "SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'SUCCESS' AND " +
            "YEAR(p.paidAt) = YEAR(CURRENT_DATE) AND MONTH(p.paidAt) = MONTH(CURRENT_DATE)");
        Object result = query.getSingleResult();
        return result instanceof BigDecimal ? (BigDecimal) result : BigDecimal.valueOf(((Number) result).doubleValue());
    }
    
    /**
     * Get today's active learning sessions count
     * 
     * @return Count of learning activities today
     */
    public long countActiveLearningSessionsToday() {
        Query query = entityManager.createQuery(
            "SELECT COUNT(lh) FROM LearningHistory lh WHERE CAST(lh.createdAt AS DATE) = CURRENT_DATE");
        return (long) query.getSingleResult();
    }
    
    /**
     * Get total count of successful payments
     * 
     * @return Total payment count
     */
    public long countTotalPayments() {
        Query query = entityManager.createQuery("SELECT COUNT(p) FROM Payment p WHERE p.status = 'SUCCESS'");
        return (long) query.getSingleResult();
    }
    
    /**
     * Get count of payments today
     * 
     * @return Today's payment count
     */
    public long countPaymentsToday() {
        Query query = entityManager.createQuery(
            "SELECT COUNT(p) FROM Payment p WHERE p.status = 'SUCCESS' AND CAST(p.paidAt AS DATE) = CURRENT_DATE");
        return (long) query.getSingleResult();
    }
    
    /**
     * Get average payment amount
     * 
     * @return Average payment amount
     */
    public BigDecimal getAveragePaymentAmount() {
        Query query = entityManager.createQuery(
            "SELECT COALESCE(AVG(p.amount), 0) FROM Payment p WHERE p.status = 'SUCCESS'");
        Object result = query.getSingleResult();
        return result instanceof BigDecimal ? (BigDecimal) result : BigDecimal.valueOf(((Number) result).doubleValue());
    }

    // ==================== Top Performers ====================
    
    /**
     * Get top N students by learning session count
     * 
     * Format: [userId, fullName, email, sessionCount]
     * 
     * @param limit Number of top students to return
     * @return List of top students
     */
    @SuppressWarnings("unchecked")
    public List<Object> getTopStudentsBySessionCount(int limit) {
        String jpql = "SELECT new map(u.id as userId, u.fullName as fullName, u.email as email, " +
                      "COUNT(lh) as totalSessions) " +
                      "FROM LearningHistory lh " +
                      "JOIN User u ON lh.user.id = u.id " +
                      "WHERE u.role = 'STUDENT' " +
                      "GROUP BY u.id, u.fullName, u.email " +
                      "ORDER BY COUNT(lh) DESC";
        return entityManager.createQuery(jpql).setMaxResults(limit).getResultList();
    }
    
    /**
     * Get top N students by words learned
     * 
     * @param limit Number of top students
     * @return List of top students with word counts
     */
    @SuppressWarnings("unchecked")
    public List<Object> getTopStudentsByWordsLearned(int limit) {
        String jpql = "SELECT new map(u.id as userId, u.fullName as fullName, u.email as email, " +
                      "COUNT(DISTINCT fw.id) as wordsLearned) " +
                      "FROM FavoriteWord fw " +
                      "JOIN User u ON fw.user.id = u.id " +
                      "WHERE u.role = 'STUDENT' " +
                      "GROUP BY u.id, u.fullName, u.email " +
                      "ORDER BY COUNT(DISTINCT fw.id) DESC";
        return entityManager.createQuery(jpql).setMaxResults(limit).getResultList();
    }
    
    /**
     * Get top students by accuracy rate
     * 
     * @param limit Number of top students
     * @return List of top students with accuracy rates
     */
    @SuppressWarnings("unchecked")
    public List<Object> getTopStudentsByAccuracy(int limit) {
        String jpql = "SELECT new map(u.id as userId, u.fullName as fullName, u.email as email, " +
                      "CAST(SUM(CASE WHEN lh.isCorrect = true THEN 1 ELSE 0 END) * 100.0 / " +
                      "COUNT(lh) AS java.math.BigDecimal) as accuracyRate) " +
                      "FROM LearningHistory lh " +
                      "JOIN User u ON lh.user.id = u.id " +
                      "WHERE u.role = 'STUDENT' " +
                      "GROUP BY u.id, u.fullName, u.email " +
                      "HAVING COUNT(lh) > 10 " +
                      "ORDER BY accuracyRate DESC";
        return entityManager.createQuery(jpql).setMaxResults(limit).getResultList();
    }

    // ==================== Time-Series Data ====================
    
    /**
     * Get daily learning sessions for last N days
     * 
     * Format: [date, sessionCount]
     * 
     * @param days Number of days to look back
     * @return List of [date, count] pairs
     */
    @SuppressWarnings("unchecked")
    public List<Object> getDailyLearningSessions(int days) {
        String jpql = "SELECT new map(CAST(lh.createdAt AS DATE) as date, COUNT(lh) as sessionCount) " +
                      "FROM LearningHistory lh " +
                      "WHERE lh.createdAt > CURRENT_TIMESTAMP - " + days + " DAY " +
                      "GROUP BY CAST(lh.createdAt AS DATE) " +
                      "ORDER BY CAST(lh.createdAt AS DATE) ASC";
        return entityManager.createQuery(jpql).getResultList();
    }
    
    /**
     * Get daily active users for last N days
     * 
     * Active = has learning activity
     * 
     * @param days Number of days to look back
     * @return List of [date, activeUserCount] pairs
     */
    @SuppressWarnings("unchecked")
    public List<Object> getDailyActiveUsers(int days) {
        String jpql = "SELECT new map(CAST(lh.createdAt AS DATE) as date, " +
                      "COUNT(DISTINCT lh.user.id) as activeUsers) " +
                      "FROM LearningHistory lh " +
                      "WHERE lh.createdAt > CURRENT_TIMESTAMP - " + days + " DAY " +
                      "GROUP BY CAST(lh.createdAt AS DATE) " +
                      "ORDER BY CAST(lh.createdAt AS DATE) ASC";
        return entityManager.createQuery(jpql).getResultList();
    }
    
    /**
     * Get daily revenue for last N days
     * 
     * @param days Number of days to look back
     * @return List of [date, dailyRevenue] pairs
     */
    @SuppressWarnings("unchecked")
    public List<Object> getDailyRevenue(int days) {
        String jpql = "SELECT new map(CAST(p.paidAt AS DATE) as date, " +
                      "COALESCE(SUM(p.amount), 0) as dailyRevenue) " +
                      "FROM Payment p " +
                      "WHERE p.status = 'SUCCESS' AND p.paidAt > CURRENT_TIMESTAMP - " + days + " DAY " +
                      "GROUP BY CAST(p.paidAt AS DATE) " +
                      "ORDER BY CAST(p.paidAt AS DATE) ASC";
        return entityManager.createQuery(jpql).getResultList();
    }
    
    /**
     * Get daily payment counts for last N days
     * 
     * @param days Number of days to look back
     * @return List of [date, paymentCount] pairs
     */
    @SuppressWarnings("unchecked")
    public List<Object> getDailyPaymentCounts(int days) {
        String jpql = "SELECT new map(CAST(p.paidAt AS DATE) as date, COUNT(p) as paymentCount) " +
                      "FROM Payment p " +
                      "WHERE p.status = 'SUCCESS' AND p.paidAt > CURRENT_TIMESTAMP - " + days + " DAY " +
                      "GROUP BY CAST(p.paidAt AS DATE) " +
                      "ORDER BY CAST(p.paidAt AS DATE) ASC";
        return entityManager.createQuery(jpql).getResultList();
    }

    // ==================== Popular Items ====================
    
    /**
     * Get most popular package by sales count
     * 
     * @return Map with packageId and packageName
     */
    @SuppressWarnings("unchecked")
    public List<Object> getMostPopularPackage() {
        String jpql = "SELECT new map(p.id as packageId, p.name as packageName, " +
                      "COUNT(s.id) as salesCount) " +
                      "FROM Subscription s " +
                      "JOIN Package p ON s.packageId = p.id " +
                      "WHERE s.status = 'ACTIVE' " +
                      "GROUP BY p.id, p.name " +
                      "ORDER BY COUNT(s.id) DESC";
        return entityManager.createQuery(jpql).setMaxResults(1).getResultList();
    }
}
