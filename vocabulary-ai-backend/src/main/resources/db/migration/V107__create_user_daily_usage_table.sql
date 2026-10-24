-- ============================================================
-- Migration: V107__create_user_daily_usage_table.sql
-- Description: Create user_daily_usage table for Daily Quota & Feature Usage Management
-- Database: MySQL 8.x+
-- ============================================================

CREATE TABLE IF NOT EXISTS user_daily_usage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    feature_type VARCHAR(20) NOT NULL,
    usage_date DATE NOT NULL,
    usage_count INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_daily_usage_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_feature_date (user_id, feature_type, usage_date),
    INDEX idx_user_daily_usage_lookup (user_id, usage_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
