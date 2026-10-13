package com.vocabularyai.admin.service;

import com.vocabularyai.admin.dto.response.PaymentListResponse;
import com.vocabularyai.admin.dto.response.PaymentDetailResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Admin Payment Service Interface
 * 
 * Provides business logic for payment management (admin system-wide view)
 * Reuses existing Payment entities and business logic from PaymentService
 */
public interface AdminPaymentService {
    
    /**
     * Get paginated list of all payments
     * 
     * Supports:
     * - Pagination and sorting
     * - Complete payment information
     * - User and package details
     * 
     * @param pageable Pagination parameters
     * @return Paginated list of payments
     */
    Page<PaymentListResponse> getPaymentList(Pageable pageable);
    
    /**
     * Get paginated list of payments filtered by status
     * 
     * @param status Payment status (SUCCESS, PENDING, CANCELLED, FAILED)
     * @param pageable Pagination parameters
     * @return Paginated filtered list
     */
    Page<PaymentListResponse> getPaymentListByStatus(String status, Pageable pageable);
    
    /**
     * Get paginated list of payments from a specific user
     * 
     * @param userId User ID to filter by
     * @param pageable Pagination parameters
     * @return Paginated user's payment list
     */
    Page<PaymentListResponse> getPaymentListByUser(Long userId, Pageable pageable);
    
    /**
     * Search payments by transaction code or order code
     * 
     * @param searchTerm Transaction or order code fragment
     * @param pageable Pagination parameters
     * @return Paginated search results
     */
    Page<PaymentListResponse> searchPayments(String searchTerm, Pageable pageable);
    
    /**
     * Get complete payment information
     * 
     * Includes:
     * - Payment details
     * - Order information
     * - User details
     * - Package information
     * - Subscription details (if created)
     * 
     * @param paymentId Payment ID
     * @return Payment detail with all related data
     * @throws IllegalArgumentException if payment not found
     */
    PaymentDetailResponse getPaymentDetail(Long paymentId);
}
