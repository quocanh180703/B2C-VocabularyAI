package com.vocabularyai.payment.service;

import com.vocabularyai.payment.dto.SubscriptionUsageResponse;
import com.vocabularyai.payment.entity.FeatureType;

public interface FeatureUsageService {

    /**
     * Check if user has active VIP subscription or is ADMIN
     */
    boolean isVipOrAdmin(Long userId);

    /**
     * Atomically check and consume 1 quota for the given feature.
     * Throws QuotaExceededException if free user has reached limit.
     * VIP and ADMIN users bypass without consuming quota.
     */
    void checkAndConsumeQuota(Long userId, FeatureType featureType);

    /**
     * Rollback 1 quota consumption if the external AI service failed or timed out.
     */
    void rollbackQuota(Long userId, FeatureType featureType);

    /**
     * Get current daily quota usage and limits for the user.
     */
    SubscriptionUsageResponse getUsage(Long userId);

    /**
     * Check if free user has reached at least 50% of their daily free tier quota.
     * Returns false for VIP/ADMIN or users below 50% usage.
     */
    boolean isFreeUsageAtLeastHalf(Long userId);

    /**
     * Check if user is allowed to access/search by given CEFR level (A1, A2 free; B1..C1 requires VIP).
     */
    boolean canAccessCefrLevel(Long userId, String level);

    /**
     * Check if user is allowed to view specific word detail.
     * Even if B1-C1, allowed if already in learning progress or favorites.
     */
    boolean canAccessWord(Long userId, String wordIdentifier, String level);
}
