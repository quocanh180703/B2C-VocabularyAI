package com.vocabularyai.ai.repository;

import com.vocabularyai.ai.entity.RateLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for RateLimit entity
 * 
 * Handles database operations for tracking API usage and token consumption per user
 */
@Repository
public interface RateLimitRepository extends JpaRepository<RateLimit, Long> {
    
    /**
     * Find the current rate limit record for a user within a specific time period
     * 
     * Used to check if user has exceeded daily limits
     * 
     * @param userId ID of the user
     * @param periodStart Start of the time period
     * @param periodEnd End of the time period
     * @return Optional containing rate limit if found
     */
    Optional<RateLimit> findByUserIdAndPeriodStartBeforeAndPeriodEndAfter(
        Long userId,
        LocalDateTime periodStart,
        LocalDateTime periodEnd
    );
    
    /**
     * Find the most recent rate limit record for a user
     * 
     * Used to get latest usage statistics
     * 
     * @param userId ID of the user
     * @return Optional containing most recent rate limit
     */
    @Query("SELECT r FROM RateLimit r WHERE r.user.id = :userId ORDER BY r.createdAt DESC LIMIT 1")
    Optional<RateLimit> findLatestByUserId(@Param("userId") Long userId);
    
    /**
     * Sum total tokens used by a user within a time period
     * 
     * Used for analytics and quota tracking
     * 
     * @param userId ID of the user
     * @param periodStart Start of the time period
     * @param periodEnd End of the time period
     * @return Total tokens used in the period
     */
    @Query("SELECT COALESCE(SUM(r.tokensUsed), 0) FROM RateLimit r " +
           "WHERE r.user.id = :userId AND r.periodStart >= :periodStart AND r.periodEnd <= :periodEnd")
    long sumTokensUsedByUserInPeriod(
        @Param("userId") Long userId,
        @Param("periodStart") LocalDateTime periodStart,
        @Param("periodEnd") LocalDateTime periodEnd
    );
    
    /**
     * Sum total requests made by a user within a time period
     * 
     * Used for request rate limiting
     * 
     * @param userId ID of the user
     * @param periodStart Start of the time period
     * @param periodEnd End of the time period
     * @return Total requests in the period
     */
    @Query("SELECT COALESCE(SUM(r.requestCount), 0) FROM RateLimit r " +
           "WHERE r.user.id = :userId AND r.periodStart >= :periodStart AND r.periodEnd <= :periodEnd")
    long sumRequestCountByUserInPeriod(
        @Param("userId") Long userId,
        @Param("periodStart") LocalDateTime periodStart,
        @Param("periodEnd") LocalDateTime periodEnd
    );
    
    /**
     * Delete expired rate limit records (older than specified date)
     * 
     * Used for database cleanup (scheduled task)
     * 
     * @param expiredBefore Delete records with period_end before this date
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM RateLimit r WHERE r.periodEnd < :expiredBefore")
    void deleteExpiredRecords(@Param("expiredBefore") LocalDateTime expiredBefore);
    
    /**
     * Check if rate limit exists for user in current period
     * 
     * @param userId ID of the user
     * @param now Current time
     * @return true if rate limit exists and is valid for current period
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM RateLimit r " +
           "WHERE r.user.id = :userId AND r.periodStart <= :now AND r.periodEnd > :now")
    boolean existsValidRateLimitForUser(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}
