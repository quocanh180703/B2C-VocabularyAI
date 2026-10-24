-- Migration V108: Create email_reminder_log table and add email_reminders_enabled to users

-- 1. Add email_reminders_enabled column to users table
ALTER TABLE users 
ADD COLUMN email_reminders_enabled BOOLEAN NOT NULL DEFAULT TRUE;

-- 2. Create email_reminder_log table
CREATE TABLE IF NOT EXISTS email_reminder_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL COMMENT 'REVIEW_DUE, STREAK_SAVE, RENEWAL_3D, RENEWAL_DUE, RENEWAL_EXPIRED',
    subscription_id BIGINT NULL COMMENT 'Subscription ID for renewal reminders',
    sent_date DATE NOT NULL COMMENT 'Date in Asia/Ho_Chi_Minh timezone',
    status VARCHAR(20) NOT NULL COMMENT 'SENT, FAILED',
    error_message TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_type_date UNIQUE (user_id, type, sent_date),
    CONSTRAINT uk_subscription_type UNIQUE (subscription_id, type),
    CONSTRAINT fk_reminder_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_reminder_date (sent_date),
    INDEX idx_reminder_user_date (user_id, sent_date),
    INDEX idx_reminder_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
