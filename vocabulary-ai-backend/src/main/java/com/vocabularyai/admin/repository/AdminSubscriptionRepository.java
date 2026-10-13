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
 * Admin Subscription Repository
 * 
 * Provides queries for subscription management (admin system-wide view)
 */
@Repository
public interface AdminSubscriptionRepository extends JpaRepository<AdminQueryResult, Long> {
    
    // ==================== Subscription List with Pagination ====================
    
    /**
     * Get paginated list of all subscriptions with enriched data
     * 
     * Includes user, package, and status information
     * 
     * @param pageable Pagination and sorting
     * @return Page of subscription data
     */
    @Query("SELECT new map(" +
           "sub.id as subscriptionId, " +
           "sub.userId as userId, " +
           "u.email as userEmail, " +
           "u.fullName as userFullName, " +
           "pkg.name as packageName, " +
           "sub.status as status, " +
           "sub.startAt as startAt, " +
           "sub.endAt as endAt, " +
           "sub.createdAt as createdAt) " +
           "FROM Subscription sub " +
           "JOIN Package pkg ON sub.packageId = pkg.id " +
           "JOIN User u ON sub.userId = u.id " +
           "ORDER BY sub.createdAt DESC")
    Page<?> getSubscriptionList(Pageable pageable);
    
    /**
     * Get paginated list of subscriptions filtered by status
     * 
     * @param status Subscription status (ACTIVE, CANCELLED, EXPIRED)
     * @param pageable Pagination
     * @return Page of filtered subscriptions
     */
    @Query("SELECT new map(" +
           "sub.id as subscriptionId, " +
           "sub.userId as userId, " +
           "u.email as userEmail, " +
           "u.fullName as userFullName, " +
           "pkg.name as packageName, " +
           "sub.status as status, " +
           "sub.startAt as startAt, " +
           "sub.endAt as endAt, " +
           "sub.createdAt as createdAt) " +
           "FROM Subscription sub " +
           "JOIN Package pkg ON sub.packageId = pkg.id " +
           "JOIN User u ON sub.userId = u.id " +
           "WHERE sub.status = :status " +
           "ORDER BY sub.createdAt DESC")
    Page<?> getSubscriptionListByStatus(@Param("status") String status, Pageable pageable);
    
    /**
     * Get paginated list of active subscriptions (not expired)
     * 
     * Status = ACTIVE AND endAt > now
     * 
     * @param pageable Pagination
     * @return Page of active subscriptions
     */
    @Query("SELECT new map(" +
           "sub.id as subscriptionId, " +
           "sub.userId as userId, " +
           "u.email as userEmail, " +
           "u.fullName as userFullName, " +
           "pkg.name as packageName, " +
           "sub.status as status, " +
           "sub.startAt as startAt, " +
           "sub.endAt as endAt, " +
           "sub.createdAt as createdAt) " +
           "FROM Subscription sub " +
           "JOIN Package pkg ON sub.packageId = pkg.id " +
           "JOIN User u ON sub.userId = u.id " +
           "WHERE sub.status = 'ACTIVE' AND sub.endAt > CURRENT_TIMESTAMP " +
           "ORDER BY sub.endAt ASC")
    Page<?> getActiveSubscriptions(Pageable pageable);
    
    /**
     * Get paginated list of subscriptions from a specific user
     * 
     * @param userId User ID to filter by
     * @param pageable Pagination
     * @return Page of user's subscriptions
     */
    @Query("SELECT new map(" +
           "sub.id as subscriptionId, " +
           "sub.userId as userId, " +
           "u.email as userEmail, " +
           "u.fullName as userFullName, " +
           "pkg.name as packageName, " +
           "sub.status as status, " +
           "sub.startAt as startAt, " +
           "sub.endAt as endAt, " +
           "sub.createdAt as createdAt) " +
           "FROM Subscription sub " +
           "JOIN Package pkg ON sub.packageId = pkg.id " +
           "JOIN User u ON sub.userId = u.id " +
           "WHERE sub.userId = :userId " +
           "ORDER BY sub.createdAt DESC")
    Page<?> getSubscriptionListByUser(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Search subscriptions by user email or package name
     * 
     * @param searchTerm Email or package name fragment
     * @param pageable Pagination
     * @return Page of matching subscriptions
     */
    @Query("SELECT new map(" +
           "sub.id as subscriptionId, " +
           "sub.userId as userId, " +
           "u.email as userEmail, " +
           "u.fullName as userFullName, " +
           "pkg.name as packageName, " +
           "sub.status as status, " +
           "sub.startAt as startAt, " +
           "sub.endAt as endAt, " +
           "sub.createdAt as createdAt) " +
           "FROM Subscription sub " +
           "JOIN Package pkg ON sub.packageId = pkg.id " +
           "JOIN User u ON sub.userId = u.id " +
           "WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(pkg.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY sub.createdAt DESC")
    Page<?> searchSubscriptions(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // ==================== Subscription Detail ====================
    
    /**
     * Get complete subscription information including related data
     * 
     * @param subscriptionId Subscription ID
     * @return Map with complete subscription details
     */
    @Query("SELECT new map(" +
           "sub.id as subscriptionId, " +
           "sub.status as status, " +
           "sub.startAt as startAt, " +
           "sub.endAt as endAt, " +
           "sub.createdAt as createdAt, " +
           "u.id as userId, " +
           "u.username as username, " +
           "u.email as userEmail, " +
           "u.fullName as userFullName, " +
           "pkg.id as packageId, " +
           "pkg.name as packageName, " +
           "pkg.code as packageCode, " +
           "pkg.price as packagePrice, " +
           "pkg.durationDays as packageDurationDays, " +
           "pkg.description as packageDescription, " +
           "p.id as paymentId, " +
           "p.transactionCode as paymentTransactionCode, " +
           "p.amount as paymentAmount, " +
           "p.status as paymentStatus, " +
           "p.paidAt as paymentPaidAt) " +
           "FROM Subscription sub " +
           "JOIN User u ON sub.userId = u.id " +
           "JOIN Package pkg ON sub.packageId = pkg.id " +
           "LEFT JOIN Payment p ON sub.paymentId = p.id " +
           "WHERE sub.id = :subscriptionId")
    List<?> getSubscriptionDetail(@Param("subscriptionId") Long subscriptionId);
}
