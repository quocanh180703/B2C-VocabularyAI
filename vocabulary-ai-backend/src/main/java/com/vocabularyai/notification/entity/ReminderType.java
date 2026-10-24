package com.vocabularyai.notification.entity;

/**
 * Types of email reminders sent by the system
 */
public enum ReminderType {
    /**
     * Daily morning review reminder (07:30 ICT)
     */
    REVIEW_DUE,

    /**
     * Daily evening streak preservation reminder (20:30 ICT)
     */
    STREAK_SAVE,

    /**
     * Subscription renewal reminder: 3 days before expiration (09:00 ICT)
     */
    RENEWAL_3D,

    /**
     * Subscription renewal reminder: on expiration date (09:00 ICT)
     */
    RENEWAL_DUE,

    /**
     * Subscription expired recovery reminder: 3 days after expiration (09:00 ICT)
     */
    RENEWAL_EXPIRED
}
