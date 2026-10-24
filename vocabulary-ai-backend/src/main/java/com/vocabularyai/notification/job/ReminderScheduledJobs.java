package com.vocabularyai.notification.job;

import com.vocabularyai.notification.service.ReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled jobs for email reminders with Asia/Ho_Chi_Minh timezone.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderScheduledJobs {

    private final ReminderService reminderService;

    /**
     * Daily morning review reminder at 07:30 ICT
     */
    @Scheduled(cron = "0 30 7 * * *", zone = "Asia/Ho_Chi_Minh")
    public void reviewDueJob() {
        log.info("Triggered scheduled job: reviewDueJob (07:30 ICT)");
        try {
            reminderService.processReviewDueReminders();
        } catch (Exception e) {
            log.error("Unhandled error in reviewDueJob", e);
        }
    }

    /**
     * Daily subscription renewal reminder at 09:00 ICT
     * Covers: RENEWAL_3D, RENEWAL_DUE, RENEWAL_EXPIRED
     */
    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Ho_Chi_Minh")
    public void renewalJob() {
        log.info("Triggered scheduled job: renewalJob (09:00 ICT)");
        try {
            reminderService.processRenewalReminders();
        } catch (Exception e) {
            log.error("Unhandled error in renewalJob", e);
        }
    }

    /**
     * Daily evening streak preservation reminder at 20:30 ICT
     */
    @Scheduled(cron = "0 30 20 * * *", zone = "Asia/Ho_Chi_Minh")
    public void streakSaveJob() {
        log.info("Triggered scheduled job: streakSaveJob (20:30 ICT)");
        try {
            reminderService.processStreakSaveReminders();
        } catch (Exception e) {
            log.error("Unhandled error in streakSaveJob", e);
        }
    }
}
