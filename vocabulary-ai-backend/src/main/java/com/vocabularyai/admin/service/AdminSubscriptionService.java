package com.vocabularyai.admin.service;

import com.vocabularyai.admin.dto.response.SubscriptionListResponse;
import com.vocabularyai.admin.dto.response.SubscriptionDetailResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Admin Subscription Service Interface
 * 
 * Provides business logic for subscription management (admin system-wide view)
 * Reuses existing Subscription entities and business logic from PaymentService
 */
public interface AdminSubscriptionService {
    
    /**
     * Get paginated list of all subscriptions
     * 
     * Supports:
     * - Pagination and sorting
     * - Complete subscription information
     * - User and package details
     * 
     * @param pageable Pagination parameters
     * @return Paginated list of subscriptions
     */
    Page<SubscriptionListResponse> getSubscriptionList(Pageable pageable);
    
    /**
     * Get paginated list of subscriptions filtered by status
     * 
     * @param status Subscription status (ACTIVE, CANCELLED, EXPIRED)
     * @param pageable Pagination parameters
     * @return Paginated filtered list
     */
    Page<SubscriptionListResponse> getSubscriptionListByStatus(String status, Pageable pageable);
    
    /**
     * Get paginated list of active (non-expired) subscriptions
     * 
     * Active = status='ACTIVE' AND endAt > now
     * 
     * @param pageable Pagination parameters
     * @return Paginated active subscriptions
     */
    Page<SubscriptionListResponse> getActiveSubscriptions(Pageable pageable);
    
    /**
     * Get paginated list of subscriptions from a specific user
     * 
     * @param userId User ID to filter by
     * @param pageable Pagination parameters
     * @return Paginated user's subscription list
     */
    Page<SubscriptionListResponse> getSubscriptionListByUser(Long userId, Pageable pageable);
    
    /**
     * Search subscriptions by user email or package name
     * 
     * @param searchTerm Email or package name fragment
     * @param pageable Pagination parameters
     * @return Paginated search results
     */
    Page<SubscriptionListResponse> searchSubscriptions(String searchTerm, Pageable pageable);
    
    /**
     * Get complete subscription information
     * 
     * Includes:
     * - Subscription details
     * - User information
     * - Package information
     * - Payment details (if created from payment)
     * 
     * @param subscriptionId Subscription ID
     * @return Subscription detail with all related data
     * @throws IllegalArgumentException if subscription not found
     */
    SubscriptionDetailResponse getSubscriptionDetail(Long subscriptionId);
}
