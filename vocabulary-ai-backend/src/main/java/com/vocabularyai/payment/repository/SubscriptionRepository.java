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

    /**
     * Bulk update expired active subscriptions where end_at < now
     */
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE Subscription s SET s.status = 'EXPIRED' WHERE s.status = 'ACTIVE' AND s.endAt < :now")
    int expireOutdatedSubscriptions(@org.springframework.data.repository.query.Param("now") LocalDateTime now);

    /**
     * Find active subscriptions ending in a date range (for RENEWAL_3D and RENEWAL_DUE)
     */
    @org.springframework.data.jpa.repository.Query("SELECT s FROM Subscription s " +
           "WHERE s.status = 'ACTIVE' " +
           "AND s.endAt >= :start AND s.endAt < :end")
    List<Subscription> findActiveSubscriptionsEndingBetween(
            @org.springframework.data.repository.query.Param("start") LocalDateTime start,
            @org.springframework.data.repository.query.Param("end") LocalDateTime end
    );

    /**
     * Find expired subscriptions ending on or before threshold needing RENEWAL_EXPIRED email
     */
    @org.springframework.data.jpa.repository.Query("SELECT s FROM Subscription s " +
           "WHERE s.status = 'EXPIRED' " +
           "AND s.endAt <= :threshold " +
           "AND NOT EXISTS (" +
           "   SELECT 1 FROM EmailReminderLog l " +
           "   WHERE l.subscriptionId = s.id AND l.type = com.vocabularyai.notification.entity.ReminderType.RENEWAL_EXPIRED AND l.status = 'SENT'" +
           ")")
    List<Subscription> findExpiredSubscriptionsNeedingReminder(
            @org.springframework.data.repository.query.Param("threshold") LocalDateTime threshold
    );
}
