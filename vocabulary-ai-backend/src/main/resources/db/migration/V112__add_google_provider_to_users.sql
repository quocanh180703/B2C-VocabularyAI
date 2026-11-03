-- Migration V112: Add Google OAuth provider columns to users table
ALTER TABLE users
    ADD COLUMN provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL',
    ADD COLUMN provider_id VARCHAR(255) NULL,
    ADD UNIQUE INDEX uk_users_provider_id (provider_id);

