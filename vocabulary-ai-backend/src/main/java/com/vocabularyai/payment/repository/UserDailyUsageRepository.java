package com.vocabularyai.payment.repository;

import com.vocabularyai.payment.entity.FeatureType;
import com.vocabularyai.payment.entity.UserDailyUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserDailyUsageRepository extends JpaRepository<UserDailyUsage, Long> {

    List<UserDailyUsage> findByUserIdAndUsageDate(Long userId, LocalDate usageDate);

    Optional<UserDailyUsage> findByUserIdAndFeatureTypeAndUsageDate(Long userId, FeatureType featureType, LocalDate usageDate);

    /**
     * Insert initial record if not exists (using ON DUPLICATE KEY UPDATE)
     */
    @Modifying
    @Query(value = "INSERT INTO user_daily_usage (user_id, feature_type, usage_date, usage_count, created_at, updated_at) " +
                   "VALUES (:userId, :featureType, :usageDate, 0, NOW(), NOW()) " +
                   "ON DUPLICATE KEY UPDATE id = id", nativeQuery = true)
    int insertIfNotExists(@Param("userId") Long userId,
                          @Param("featureType") String featureType,
                          @Param("usageDate") LocalDate usageDate);

    /**
     * Atomically increment usage_count only if below quota limit
     * Returns 1 if successful, 0 if quota exceeded
     */
    @Modifying
    @Query(value = "UPDATE user_daily_usage " +
                   "SET usage_count = usage_count + 1, updated_at = NOW() " +
                   "WHERE user_id = :userId " +
                   "  AND feature_type = :featureType " +
                   "  AND usage_date = :usageDate " +
                   "  AND usage_count < :limit", nativeQuery = true)
    int incrementUsageAtomic(@Param("userId") Long userId,
                             @Param("featureType") String featureType,
                             @Param("usageDate") LocalDate usageDate,
                             @Param("limit") int limit);

    /**
     * Atomically decrement usage_count for rollback on failure
     */
    @Modifying
    @Query(value = "UPDATE user_daily_usage " +
                   "SET usage_count = usage_count - 1, updated_at = NOW() " +
                   "WHERE user_id = :userId " +
                   "  AND feature_type = :featureType " +
                   "  AND usage_date = :usageDate " +
                   "  AND usage_count > 0", nativeQuery = true)
    int decrementUsageAtomic(@Param("userId") Long userId,
                             @Param("featureType") String featureType,
                             @Param("usageDate") LocalDate usageDate);
}
