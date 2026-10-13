package com.vocabularyai.payment.repository;

import com.vocabularyai.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * Repository for Payment entity
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    /**
     * Find payment by unique transaction code
     * @param transactionCode Transaction code from PayOS
     * @return Payment if found
     */
    Optional<Payment> findByTransactionCode(String transactionCode);
    
    /**
     * Check if payment with transaction code exists
     * @param transactionCode Transaction code from PayOS
     * @return true if exists
     */
    boolean existsByTransactionCode(String transactionCode);
    
    /**
     * Find payment by payment order ID
     * @param paymentOrderId Payment order ID
     * @return Payment if found
     */
    Optional<Payment> findByPaymentOrderId(Long paymentOrderId);
    
    /**
     * Check if payment exists for order
     * @param paymentOrderId Payment order ID
     * @return true if exists
     */
    boolean existsByPaymentOrderId(Long paymentOrderId);
    
    /**
     * Find all payments by status
     * @param status Payment status
     * @return List of payments
     */
    List<Payment> findByStatus(String status);
    
    /**
     * Find all successful payments for reconciliation
     * @return List of successful payments
     */
    List<Payment> findByStatusOrderByPaidAtDesc(String status);
}
