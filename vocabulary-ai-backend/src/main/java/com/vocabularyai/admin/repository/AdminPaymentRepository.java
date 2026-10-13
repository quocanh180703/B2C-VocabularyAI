package com.vocabularyai.admin.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import com.vocabularyai.admin.entity.AdminQueryResult;

/**
 * Admin Payment Repository
 * 
 * Provides queries for payment management (admin system-wide view)
 */
@Repository
public interface AdminPaymentRepository extends JpaRepository<AdminQueryResult, Long> {
    
    // ==================== Payment List with Pagination ====================
    
    /**
     * Get paginated list of all payments with enriched data
     * 
     * Includes user, package, and order information
     * 
     * @param pageable Pagination and sorting
     * @return Page of payment data
     */
    @Query("SELECT new map(" +
           "p.id as paymentId, " +
           "p.transactionCode as transactionCode, " +
           "po.orderCode as orderCode, " +
           "po.userId as userId, " +
           "u.email as userEmail, " +
           "u.fullName as userFullName, " +
           "pkg.name as packageName, " +
           "p.amount as amount, " +
           "p.status as status, " +
           "p.paymentMethod as paymentMethod, " +
           "p.paidAt as paidAt, " +
           "p.createdAt as createdAt) " +
           "FROM Payment p " +
           "JOIN PaymentOrder po ON p.paymentOrderId = po.id " +
           "JOIN User u ON po.userId = u.id " +
           "JOIN Package pkg ON po.packageId = pkg.id " +
           "ORDER BY p.paidAt DESC")
    Page<?> getPaymentList(Pageable pageable);
    
    /**
     * Get paginated list of payments filtered by status
     * 
     * @param status Payment status (SUCCESS, PENDING, CANCELLED, FAILED)
     * @param pageable Pagination
     * @return Page of filtered payments
     */
    @Query("SELECT new map(" +
           "p.id as paymentId, " +
           "p.transactionCode as transactionCode, " +
           "po.orderCode as orderCode, " +
           "po.userId as userId, " +
           "u.email as userEmail, " +
           "u.fullName as userFullName, " +
           "pkg.name as packageName, " +
           "p.amount as amount, " +
           "p.status as status, " +
           "p.paymentMethod as paymentMethod, " +
           "p.paidAt as paidAt, " +
           "p.createdAt as createdAt) " +
           "FROM Payment p " +
           "JOIN PaymentOrder po ON p.paymentOrderId = po.id " +
           "JOIN User u ON po.userId = u.id " +
           "JOIN Package pkg ON po.packageId = pkg.id " +
           "WHERE p.status = :status " +
           "ORDER BY p.paidAt DESC")
    Page<?> getPaymentListByStatus(@Param("status") String status, Pageable pageable);
    
    /**
     * Get paginated list of payments from a specific user
     * 
     * @param userId User ID to filter by
     * @param pageable Pagination
     * @return Page of user's payments
     */
    @Query("SELECT new map(" +
           "p.id as paymentId, " +
           "p.transactionCode as transactionCode, " +
           "po.orderCode as orderCode, " +
           "po.userId as userId, " +
           "u.email as userEmail, " +
           "u.fullName as userFullName, " +
           "pkg.name as packageName, " +
           "p.amount as amount, " +
           "p.status as status, " +
           "p.paymentMethod as paymentMethod, " +
           "p.paidAt as paidAt, " +
           "p.createdAt as createdAt) " +
           "FROM Payment p " +
           "JOIN PaymentOrder po ON p.paymentOrderId = po.id " +
           "JOIN User u ON po.userId = u.id " +
           "JOIN Package pkg ON po.packageId = pkg.id " +
           "WHERE po.userId = :userId " +
           "ORDER BY p.paidAt DESC")
    Page<?> getPaymentListByUser(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Search payments by transaction code or order code
     * 
     * @param searchTerm Transaction or order code fragment
     * @param pageable Pagination
     * @return Page of matching payments
     */
    @Query("SELECT new map(" +
           "p.id as paymentId, " +
           "p.transactionCode as transactionCode, " +
           "po.orderCode as orderCode, " +
           "po.userId as userId, " +
           "u.email as userEmail, " +
           "u.fullName as userFullName, " +
           "pkg.name as packageName, " +
           "p.amount as amount, " +
           "p.status as status, " +
           "p.paymentMethod as paymentMethod, " +
           "p.paidAt as paidAt, " +
           "p.createdAt as createdAt) " +
           "FROM Payment p " +
           "JOIN PaymentOrder po ON p.paymentOrderId = po.id " +
           "JOIN User u ON po.userId = u.id " +
           "JOIN Package pkg ON po.packageId = pkg.id " +
           "WHERE LOWER(p.transactionCode) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(po.orderCode) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY p.paidAt DESC")
    Page<?> searchPayments(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // ==================== Payment Detail ====================
    
    /**
     * Get complete payment information including related data
     * 
     * @param paymentId Payment ID
     * @return Map with complete payment details
     */
    @Query("SELECT new map(" +
           "p.id as paymentId, " +
           "p.transactionCode as transactionCode, " +
           "p.status as status, " +
           "p.amount as amount, " +
           "p.paymentMethod as paymentMethod, " +
           "p.paidAt as paidAt, " +
           "p.createdAt as createdAt, " +
           "po.id as orderId, " +
           "po.orderCode as orderCode, " +
           "po.status as orderStatus, " +
           "po.expiredAt as orderExpiredAt, " +
           "u.id as userId, " +
           "u.username as username, " +
           "u.email as userEmail, " +
           "u.fullName as userFullName, " +
           "pkg.id as packageId, " +
           "pkg.name as packageName, " +
           "pkg.code as packageCode, " +
           "pkg.price as packagePrice, " +
           "pkg.durationDays as packageDurationDays, " +
           "sub.id as subscriptionId, " +
           "sub.status as subscriptionStatus, " +
           "sub.startAt as subscriptionStartAt, " +
           "sub.endAt as subscriptionEndAt) " +
           "FROM Payment p " +
           "JOIN PaymentOrder po ON p.paymentOrderId = po.id " +
           "JOIN User u ON po.userId = u.id " +
           "JOIN Package pkg ON po.packageId = pkg.id " +
           "LEFT JOIN Subscription sub ON p.id = sub.paymentId " +
           "WHERE p.id = :paymentId")
    List<?> getPaymentDetail(@Param("paymentId") Long paymentId);
}
