package com.vocabularyai.notification.service.impl;

import com.vocabularyai.auth.service.EmailService;
import com.vocabularyai.learning.entity.LearningProgress;
import com.vocabularyai.learning.repository.LearningHistoryRepository;
import com.vocabularyai.learning.repository.LearningProgressRepository;
import com.vocabularyai.notification.dto.OverdueWordDto;
import com.vocabularyai.notification.entity.EmailReminderLog;
import com.vocabularyai.notification.entity.ReminderType;
import com.vocabularyai.notification.repository.EmailReminderLogRepository;
import com.vocabularyai.notification.service.ReminderService;
import com.vocabularyai.payment.entity.Package;
import com.vocabularyai.payment.entity.Subscription;
import com.vocabularyai.payment.repository.PackageRepository;
import com.vocabularyai.payment.repository.SubscriptionRepository;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderServiceImpl implements ReminderService {

    private static final ZoneId ZONE_VN = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final UserRepository userRepository;
    private final LearningProgressRepository learningProgressRepository;
    private final LearningHistoryRepository learningHistoryRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PackageRepository packageRepository;
    private final EmailReminderLogRepository emailReminderLogRepository;
    private final EmailService emailService;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${app.backend-url:http://localhost:8080}")
    private String backendUrl;

    @Value("${app.notification.unsubscribe-secret:vocab-ai-unsub-secret-key-2026}")
    private String unsubscribeSecret;

    @Override
    @Transactional
    public void processReviewDueReminders() {
        LocalDate today = LocalDate.now(ZONE_VN);
        LocalDateTime now = LocalDateTime.now(ZONE_VN);
        LocalDateTime todayStart = today.atStartOfDay();

        log.info("Starting REVIEW_DUE reminder process for date: {}", today);

        List<Long> candidateUserIds = learningProgressRepository.findCandidateUserIdsForReviewDue(now, todayStart, today);
        log.info("Found {} candidate users for REVIEW_DUE", candidateUserIds.size());

        for (Long userId : candidateUserIds) {
            try {
                // Check if already sent today
                if (emailReminderLogRepository.existsByUserIdAndTypeAndSentDateAndStatus(
                        userId, ReminderType.REVIEW_DUE, today, "SENT")) {
                    continue;
                }

                // Check fatigue rule: skip if >= 2 emails in last 3 days, sent yesterday, and no activity since
                if (isFatiguedFromReviewDue(userId, today)) {
                    log.info("Skipping REVIEW_DUE for userId: {} due to fatigue rule (alternating rhythm)", userId);
                    continue;
                }

                User user = userRepository.findById(userId).orElse(null);
                if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
                    continue;
                }

                // Query most overdue words
                Page<LearningProgress> overduePage = learningProgressRepository.findWordsNeedingReview(
                        userId, now, PageRequest.of(0, 3, Sort.by(Sort.Direction.ASC, "nextReviewAt"))
                );

                int totalDue = (int) overduePage.getTotalElements();
                if (totalDue <= 0) {
                    continue;
                }

                List<OverdueWordDto> overdueWords = overduePage.getContent().stream()
                        .map(lp -> {
                            long daysLate = lp.getNextReviewAt() != null 
                                    ? Math.max(0, ChronoUnit.DAYS.between(lp.getNextReviewAt().toLocalDate(), today)) 
                                    : 0;
                            return OverdueWordDto.builder()
                                    .word(lp.getWord() != null ? lp.getWord().getWord() : "")
                                    .meaning(lp.getWord() != null ? lp.getWord().getMeaning() : "")
                                    .level(lp.getWord() != null ? lp.getWord().getLevel() : "A1")
                                    .overdueDays(daysLate)
                                    .build();
                        })
                        .collect(Collectors.toList());

                int currentStreak = calculateStreak(userId);
                String unsubscribeUrl = buildUnsubscribeUrl(userId, "LEARNING");
                String actionUrl = frontendUrl + "/review?utm_source=email&utm_medium=reminder&utm_campaign=review_due";

                try {
                    emailService.sendReviewDueEmail(
                            user.getEmail(),
                            user.getFullName(),
                            totalDue,
                            overdueWords,
                            currentStreak,
                            actionUrl,
                            unsubscribeUrl
                    );
                    logSuccess(userId, ReminderType.REVIEW_DUE, null, today);
                } catch (Exception e) {
                    logFailure(userId, ReminderType.REVIEW_DUE, null, today, e.getMessage());
                }

            } catch (Exception e) {
                log.error("Error processing REVIEW_DUE for userId: {}", userId, e);
            }
        }
    }

    @Override
    @Transactional
    public void processStreakSaveReminders() {
        LocalDate today = LocalDate.now(ZONE_VN);
        LocalDateTime now = LocalDateTime.now(ZONE_VN);
        LocalDateTime todayStart = today.atStartOfDay();

        log.info("Starting STREAK_SAVE reminder process for date: {}", today);

        List<Long> candidateUserIds = learningProgressRepository.findCandidateUserIdsForStreakSave(todayStart, today);
        log.info("Found {} candidate users for STREAK_SAVE check", candidateUserIds.size());

        for (Long userId : candidateUserIds) {
            try {
                if (emailReminderLogRepository.existsByUserIdAndTypeAndSentDateAndStatus(
                        userId, ReminderType.STREAK_SAVE, today, "SENT")) {
                    continue;
                }

                int currentStreak = calculateStreak(userId);
                if (currentStreak < 3) {
                    // STREAK_SAVE is only sent when streak is >= 3 and about to break
                    continue;
                }

                User user = userRepository.findById(userId).orElse(null);
                if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
                    continue;
                }

                // Pick 1 suggested overdue word if available
                Page<LearningProgress> wordPage = learningProgressRepository.findWordsNeedingReview(
                        userId, now, PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "nextReviewAt"))
                );
                OverdueWordDto suggestedWord = null;
                if (!wordPage.isEmpty()) {
                    LearningProgress lp = wordPage.getContent().get(0);
                    suggestedWord = OverdueWordDto.builder()
                            .word(lp.getWord() != null ? lp.getWord().getWord() : "")
                            .meaning(lp.getWord() != null ? lp.getWord().getMeaning() : "")
                            .level(lp.getWord() != null ? lp.getWord().getLevel() : "A1")
                            .build();
                }

                String unsubscribeUrl = buildUnsubscribeUrl(userId, "LEARNING");
                String actionUrl = frontendUrl + "/review?utm_source=email&utm_medium=reminder&utm_campaign=streak_save";

                try {
                    emailService.sendStreakSaveEmail(
                            user.getEmail(),
                            user.getFullName(),
                            currentStreak,
                            suggestedWord,
                            actionUrl,
                            unsubscribeUrl
                    );
                    logSuccess(userId, ReminderType.STREAK_SAVE, null, today);
                } catch (Exception e) {
                    logFailure(userId, ReminderType.STREAK_SAVE, null, today, e.getMessage());
                }

            } catch (Exception e) {
                log.error("Error processing STREAK_SAVE for userId: {}", userId, e);
            }
        }
    }

    @Override
    @Transactional
    public void processRenewalReminders() {
        LocalDate today = LocalDate.now(ZONE_VN);
        LocalDateTime now = LocalDateTime.now(ZONE_VN);

        log.info("Starting renewal reminders process for date: {}", today);

        // Step 0: Synchronize status of expired active subscriptions
        try {
            int expiredCount = subscriptionRepository.expireOutdatedSubscriptions(now);
            if (expiredCount > 0) {
                log.info("Synchronized {} expired subscriptions to EXPIRED status", expiredCount);
            }
        } catch (Exception e) {
            log.error("Error synchronizing expired subscriptions", e);
        }

        // Sub-step 1: RENEWAL_3D (end_at in 3 days)
        LocalDate targetDate3D = today.plusDays(3);
        LocalDateTime start3D = targetDate3D.atStartOfDay();
        LocalDateTime end3D = targetDate3D.atTime(23, 59, 59);

        List<Subscription> subs3D = subscriptionRepository.findActiveSubscriptionsEndingBetween(start3D, end3D);
        for (Subscription sub : subs3D) {
            processRenewalEmail(sub, ReminderType.RENEWAL_3D, today);
        }

        // Sub-step 2: RENEWAL_DUE (end_at today)
        LocalDateTime startDue = today.atStartOfDay();
        LocalDateTime endDue = today.atTime(23, 59, 59);

        List<Subscription> subsDue = subscriptionRepository.findActiveSubscriptionsEndingBetween(startDue, endDue);
        for (Subscription sub : subsDue) {
            processRenewalEmail(sub, ReminderType.RENEWAL_DUE, today);
        }

        // Sub-step 3: RENEWAL_EXPIRED (end_at <= 3 days ago, status = EXPIRED, not sent yet)
        LocalDateTime thresholdExpired = today.minusDays(3).atTime(23, 59, 59);
        List<Subscription> subsExpired = subscriptionRepository.findExpiredSubscriptionsNeedingReminder(thresholdExpired);
        for (Subscription sub : subsExpired) {
            processRenewalEmail(sub, ReminderType.RENEWAL_EXPIRED, today);
        }
    }

    private void processRenewalEmail(Subscription sub, ReminderType type, LocalDate today) {
        try {
            if (emailReminderLogRepository.existsBySubscriptionIdAndTypeAndStatus(sub.getId(), type, "SENT")) {
                return;
            }

            User user = userRepository.findById(sub.getUserId()).orElse(null);
            if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
                return;
            }

            Package pkg = packageRepository.findById(sub.getPackageId()).orElse(null);
            String packageName = pkg != null ? pkg.getName() : "Gói Premium";
            String priceStr = pkg != null && pkg.getPrice() != null 
                    ? String.format("%,dđ", pkg.getPrice().longValue()).replace(',', '.') 
                    : "99.000đ";
            String expireDateStr = sub.getEndAt() != null 
                    ? sub.getEndAt().format(DATE_FORMATTER) 
                    : today.format(DATE_FORMATTER);

            String actionUrl = frontendUrl + "/profile?utm_source=email&utm_medium=reminder&utm_campaign=" + type.name().toLowerCase();

            try {
                if (type == ReminderType.RENEWAL_3D) {
                    emailService.sendRenewal3DaysEmail(user.getEmail(), user.getFullName(), packageName, expireDateStr, priceStr, actionUrl);
                } else if (type == ReminderType.RENEWAL_DUE) {
                    emailService.sendRenewalDueEmail(user.getEmail(), user.getFullName(), packageName, expireDateStr, actionUrl);
                } else if (type == ReminderType.RENEWAL_EXPIRED) {
                    emailService.sendRenewalExpiredEmail(user.getEmail(), user.getFullName(), packageName, expireDateStr, actionUrl);
                }
                logSuccess(user.getId(), type, sub.getId(), today);
            } catch (Exception e) {
                logFailure(user.getId(), type, sub.getId(), today, e.getMessage());
            }

        } catch (Exception e) {
            log.error("Error processing renewal email type: {} for subscriptionId: {}", type, sub.getId(), e);
        }
    }

    private boolean isFatiguedFromReviewDue(Long userId, LocalDate today) {
        // (a) >= 2 sent in last 3 days
        long sentCount = emailReminderLogRepository.countSentReminders(
                userId, ReminderType.REVIEW_DUE, today.minusDays(3), today.minusDays(1)
        );
        if (sentCount < 2) {
            return false;
        }

        // (b) sent yesterday
        boolean sentYesterday = emailReminderLogRepository.existsByUserIdAndTypeAndSentDateAndStatus(
                userId, ReminderType.REVIEW_DUE, today.minusDays(1), "SENT"
        );
        if (!sentYesterday) {
            return false;
        }

        // (c) no learning activity since the latest sent reminder
        Optional<LocalDate> latestSentOpt = emailReminderLogRepository.findLatestSentDate(userId, ReminderType.REVIEW_DUE);
        if (latestSentOpt.isPresent()) {
            LocalDateTime latestSentTime = latestSentOpt.get().atStartOfDay();
            long activities = learningHistoryRepository.countTodayActivities(userId, latestSentTime);
            return activities == 0;
        }

        return false;
    }

    private int calculateStreak(Long userId) {
        List<java.sql.Date> activitySqlDates = learningHistoryRepository.findActivityDatesByUserId(userId);
        if (activitySqlDates == null || activitySqlDates.isEmpty()) {
            return 0;
        }

        Set<LocalDate> activeDateSet = activitySqlDates.stream()
                .map(java.sql.Date::toLocalDate)
                .collect(Collectors.toSet());

        LocalDate today = LocalDate.now(ZONE_VN);
        boolean todayCompleted = activeDateSet.contains(today);

        int currentStreak = 0;
        LocalDate checkDate = today;
        if (!todayCompleted) {
            LocalDate yesterday = today.minusDays(1);
            if (activeDateSet.contains(yesterday)) {
                checkDate = yesterday;
            } else {
                checkDate = null;
            }
        }

        if (checkDate != null) {
            while (activeDateSet.contains(checkDate)) {
                currentStreak++;
                checkDate = checkDate.minusDays(1);
            }
        }

        return currentStreak;
    }

    private String buildUnsubscribeUrl(Long userId, String type) {
        String token = generateUnsubscribeToken(userId, type);
        return backendUrl + "/api/v1/notifications/unsubscribe?userId=" + userId + "&type=" + type + "&token=" + token;
    }

    @Override
    public String generateUnsubscribeToken(Long userId, String type) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(unsubscribeSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            String payload = userId + ":" + (type != null ? type : "LEARNING");
            byte[] hmacBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hmacBytes);
        } catch (Exception e) {
            throw new RuntimeException("Could not generate unsubscribe token", e);
        }
    }

    @Override
    public boolean verifyUnsubscribeToken(Long userId, String type, String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            String expected = generateUnsubscribeToken(userId, type);
            return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), token.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Transactional
    public boolean unsubscribeUser(Long userId, String type, String token) {
        if (!verifyUnsubscribeToken(userId, type, token)) {
            log.warn("Invalid unsubscribe token for userId: {}, type: {}", userId, type);
            return false;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }

        user.setEmailRemindersEnabled(false);
        userRepository.save(user);
        log.info("User {} unsubscribed from reminders via 1-click token", userId);
        return true;
    }

    private void logSuccess(Long userId, ReminderType type, Long subscriptionId, LocalDate today) {
        try {
            EmailReminderLog logEntity = EmailReminderLog.builder()
                    .userId(userId)
                    .type(type)
                    .subscriptionId(subscriptionId)
                    .sentDate(today)
                    .status("SENT")
                    .createdAt(LocalDateTime.now())
                    .build();
            emailReminderLogRepository.save(logEntity);
        } catch (Exception e) {
            log.warn("Could not save SENT reminder log (possibly duplicate): {}", e.getMessage());
        }
    }

    private void logFailure(Long userId, ReminderType type, Long subscriptionId, LocalDate today, String error) {
        try {
            EmailReminderLog logEntity = EmailReminderLog.builder()
                    .userId(userId)
                    .type(type)
                    .subscriptionId(subscriptionId)
                    .sentDate(today)
                    .status("FAILED")
                    .errorMessage(error)
                    .createdAt(LocalDateTime.now())
                    .build();
            emailReminderLogRepository.save(logEntity);
        } catch (Exception e) {
            log.warn("Could not save FAILED reminder log: {}", e.getMessage());
        }
    }
}
