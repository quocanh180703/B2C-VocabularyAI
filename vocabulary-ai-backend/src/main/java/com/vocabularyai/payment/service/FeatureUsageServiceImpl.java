package com.vocabularyai.payment.service;

import com.vocabularyai.common.exception.QuotaExceededException;
import com.vocabularyai.learning.repository.LearningProgressRepository;
import com.vocabularyai.payment.dto.SubscriptionUsageResponse;
import com.vocabularyai.payment.dto.SubscriptionUsageResponse.FeatureQuotaDto;
import com.vocabularyai.payment.entity.FeatureType;
import com.vocabularyai.payment.entity.UserDailyUsage;
import com.vocabularyai.payment.repository.UserDailyUsageRepository;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.entity.User.UserRole;
import com.vocabularyai.user.repository.UserRepository;
import com.vocabularyai.vocabulary.entity.Word;
import com.vocabularyai.vocabulary.repository.CefrWordRepository;
import com.vocabularyai.vocabulary.repository.FavoriteWordRepository;
import com.vocabularyai.vocabulary.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureUsageServiceImpl implements FeatureUsageService {

    public static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final UserDailyUsageRepository userDailyUsageRepository;
    private final PaymentService paymentService;
    private final UserRepository userRepository;
    private final WordRepository wordRepository;
    private final CefrWordRepository cefrWordRepository;
    private final FavoriteWordRepository favoriteWordRepository;
    private final LearningProgressRepository progressRepository;

    public static LocalDate getTodayVietnam() {
        return LocalDate.now(VIETNAM_ZONE);
    }

    public static String getNextResetIso() {
        ZonedDateTime nextMidnight = LocalDate.now(VIETNAM_ZONE)
                .plusDays(1)
                .atStartOfDay(VIETNAM_ZONE);
        return nextMidnight.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    @Override
    public boolean isVipOrAdmin(Long userId) {
        if (userId == null) {
            return false;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user != null && user.getRole() == UserRole.ADMIN) {
            return true;
        }

        return paymentService.hasActiveSubscription(userId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void checkAndConsumeQuota(Long userId, FeatureType featureType) {
        if (userId == null) {
            log.warn("checkAndConsumeQuota called with null userId for feature {}", featureType);
            return;
        }

        if (isVipOrAdmin(userId)) {
            log.debug("User {} is VIP/ADMIN, bypassing quota for {}", userId, featureType);
            return;
        }

        LocalDate today = getTodayVietnam();
        int limit = featureType.getDefaultFreeLimit();

        // 1. Ensure a row exists for today
        userDailyUsageRepository.insertIfNotExists(userId, featureType.name(), today);

        // 2. Atomically increment usage_count where usage_count < limit
        int updated = userDailyUsageRepository.incrementUsageAtomic(userId, featureType.name(), today, limit);

        if (updated == 0) {
            // Quota exceeded
            UserDailyUsage current = userDailyUsageRepository
                    .findByUserIdAndFeatureTypeAndUsageDate(userId, featureType, today)
                    .orElse(null);
            int used = current != null ? current.getUsageCount() : limit;
            String resetAt = getNextResetIso();
            String message = String.format("Bạn đã dùng hết %d lượt %s miễn phí hôm nay", limit, featureType.getDisplayName());

            log.warn("User {} exceeded quota for {}: used={}/{}", userId, featureType, used, limit);
            throw new QuotaExceededException(featureType.name(), limit, used, resetAt, message);
        }

        log.debug("User {} consumed 1 quota for {}. Limit: {}", userId, featureType, limit);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void rollbackQuota(Long userId, FeatureType featureType) {
        if (userId == null || isVipOrAdmin(userId)) {
            return;
        }

        LocalDate today = getTodayVietnam();
        userDailyUsageRepository.decrementUsageAtomic(userId, featureType.name(), today);
        log.info("Rolled back 1 quota for user {} on feature {}", userId, featureType);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionUsageResponse getUsage(Long userId) {
        boolean isVip = isVipOrAdmin(userId);
        LocalDate today = getTodayVietnam();
        String resetAt = getNextResetIso();

        Map<FeatureType, Integer> usageMap = new EnumMap<>(FeatureType.class);
        if (userId != null) {
            List<UserDailyUsage> usages = userDailyUsageRepository.findByUserIdAndUsageDate(userId, today);
            for (UserDailyUsage u : usages) {
                usageMap.put(u.getFeatureType(), u.getUsageCount());
            }
        }

        Map<String, FeatureQuotaDto> features = new LinkedHashMap<>();
        for (FeatureType type : FeatureType.values()) {
            int used = usageMap.getOrDefault(type, 0);
            if (isVip) {
                features.put(type.getKey(), FeatureQuotaDto.builder()
                        .limit(null)
                        .used(used)
                        .remaining(null)
                        .build());
            } else {
                int limit = type.getDefaultFreeLimit();
                int remaining = Math.max(0, limit - used);
                features.put(type.getKey(), FeatureQuotaDto.builder()
                        .limit(limit)
                        .used(used)
                        .remaining(remaining)
                        .build());
            }
        }

        boolean isHalfUsed = !isVip && isFreeUsageAtLeastHalf(userId);

        return SubscriptionUsageResponse.builder()
                .isVip(isVip)
                .isFreeUsageAtLeastHalf(isHalfUsed)
                .resetAt(resetAt)
                .features(features)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFreeUsageAtLeastHalf(Long userId) {
        if (userId == null || isVipOrAdmin(userId)) {
            return false;
        }

        LocalDate today = getTodayVietnam();
        List<UserDailyUsage> usages = userDailyUsageRepository.findByUserIdAndUsageDate(userId, today);
        Map<FeatureType, Integer> usageMap = new EnumMap<>(FeatureType.class);
        int totalUsed = 0;
        int totalLimit = 0;

        for (FeatureType type : FeatureType.values()) {
            totalLimit += type.getDefaultFreeLimit();
        }

        for (UserDailyUsage u : usages) {
            int count = u.getUsageCount();
            usageMap.put(u.getFeatureType(), count);
            totalUsed += count;
        }

        // Condition 1: Overall usage across all features reaches >= 50% of total daily limits (e.g. >= 14/27)
        if (totalLimit > 0 && totalUsed >= (int) Math.ceil(totalLimit * 0.5)) {
            return true;
        }

        // Condition 2: Any individual feature quota reaches >= 50% of its daily limit
        // (e.g. chat >= 3/5, speaking >= 3/5, grammar >= 3/5, explain >= 5/10, roadmap >= 1/2)
        for (FeatureType type : FeatureType.values()) {
            int used = usageMap.getOrDefault(type, 0);
            int halfLimit = (int) Math.ceil(type.getDefaultFreeLimit() * 0.5);
            if (used >= halfLimit) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean canAccessCefrLevel(Long userId, String level) {
        if (level == null || level.trim().isEmpty() || "ALL".equalsIgnoreCase(level) || "Tất cả".equalsIgnoreCase(level)) {
            return true;
        }

        String normalized = level.trim().toUpperCase();
        if ("A1".equals(normalized) || "A2".equals(normalized)) {
            return true;
        }

        if (isVipOrAdmin(userId)) {
            return true;
        }

        // Free tier users can access B1-C1 freely until they consume 50% of their free quota.
        // Once >= 50% free quota is reached, B1-C1 is locked to prompt VIP upgrade.
        return !isFreeUsageAtLeastHalf(userId);
    }

    @Override
    public boolean canAccessWord(Long userId, String wordIdentifier, String level) {
        if (canAccessCefrLevel(userId, level)) {
            return true;
        }

        if (userId == null) {
            return false;
        }

        // Check if word is already in user's learning progress or favorites
        Word word = null;
        try {
            Long numId = Long.parseLong(wordIdentifier);
            word = wordRepository.findById(numId).orElse(null);
        } catch (NumberFormatException ignored) {}

        if (word == null) {
            var cefr = cefrWordRepository.findById(wordIdentifier)
                    .or(() -> cefrWordRepository.findByLemma(wordIdentifier));
            if (cefr.isPresent()) {
                word = wordRepository.findByWord(cefr.get().getLemma()).orElse(null);
            }
        }

        if (word != null) {
            if (favoriteWordRepository.existsByUserIdAndWordId(userId, word.getId())) {
                return true;
            }
            if (progressRepository.findByUserIdAndWordId(userId, word.getId()).isPresent()) {
                return true;
            }
        }

        return false;
    }
}
