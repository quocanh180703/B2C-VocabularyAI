package com.vocabularyai.notification.service;

public interface ReminderService {

    /**
     * Daily morning job: Review due reminders (07:30 ICT)
     */
    void processReviewDueReminders();

    /**
     * Daily evening job: Streak save reminders (20:30 ICT)
     */
    void processStreakSaveReminders();

    /**
     * Daily morning job: Subscription renewal reminders (09:00 ICT)
     */
    void processRenewalReminders();

    /**
     * Generate HMAC unsubscribe token for user
     */
    String generateUnsubscribeToken(Long userId, String type);

    /**
     * Verify HMAC unsubscribe token
     */
    boolean verifyUnsubscribeToken(Long userId, String type, String token);

    /**
     * Unsubscribe user from reminders
     */
    boolean unsubscribeUser(Long userId, String type, String token);
}
