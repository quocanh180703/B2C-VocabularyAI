package com.vocabularyai.payment.repository;

import com.vocabularyai.payment.entity.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

/**
 * Repository for PaymentOrder entity
 */
@Repository
public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {
    
    /**
     * Find payment order by unique order code
     * @param orderCode Order code
     * @return PaymentOrder if found
     */
    Optional<PaymentOrder> findByOrderCode(String orderCode);
    
    /**
     * Check if payment order with code exists
     * @param orderCode Order code
     * @return true if exists
     */
    boolean existsByOrderCode(String orderCode);
    
    /**
     * Find all payment orders by user ID
     * @param userId User ID
     * @return List of payment orders
     */
    List<PaymentOrder> findByUserId(Long userId);
    
    /**
     * Find all payment orders by user ID and status
     * @param userId User ID
     * @param status Order status
     * @return List of payment orders
     */
    List<PaymentOrder> findByUserIdAndStatus(Long userId, String status);
    
    /**
     * Find payment order by user ID and status, ordered by creation date descending
     * @param userId User ID
     * @param status Order status
     * @return List of payment orders
     */
    List<PaymentOrder> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);
    
    /**
     * Find expired payment orders
     * @param expiredAt Current time
     * @param status Status to filter by (usually PENDING)
     * @return List of expired payment orders
     */
    List<PaymentOrder> findByStatusAndExpiredAtBefore(String status, LocalDateTime expiredAt);
}
