package com.vocabularyai.payment.repository;

import com.vocabularyai.payment.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

/**
 * Repository for Subscription entity
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    
    /**
     * Find active subscription for a user
     * @param userId User ID
     * @return Subscription if found
     */
    Optional<Subscription> findByUserId(Long userId);
    
    /**
     * Check if user has subscription
     * @param userId User ID
     * @return true if subscription exists
     */
    boolean existsByUserId(Long userId);
    
    /**
     * Find subscriptions by status
     * @param status Subscription status
     * @return List of subscriptions
     */
    List<Subscription> findByStatus(String status);
    
    /**
     * Find active subscriptions by user ID and status
     * @param userId User ID
     * @param status Status
     * @return Subscription if found
     */
    Optional<Subscription> findByUserIdAndStatus(Long userId, String status);
    
    /**
     * Find expiring subscriptions (ending before given time)
     * @param endAt End time threshold
     * @return List of subscriptions
     */
    List<Subscription> findByStatusAndEndAtBefore(String status, LocalDateTime endAt);
    
    /**
     * Find subscriptions for a package
     * @param packageId Package ID
     * @return List of subscriptions
     */
    List<Subscription> findByPackageId(Long packageId);
}
