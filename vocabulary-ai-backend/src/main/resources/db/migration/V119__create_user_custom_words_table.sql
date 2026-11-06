-- Migration V119: Create user_custom_words table for user-created private vocabulary
CREATE TABLE IF NOT EXISTS user_custom_words (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    lemma VARCHAR(100) NOT NULL,
    vietnamese_meaning VARCHAR(500) NOT NULL,
    part_of_speech VARCHAR(50) NULL,
    example TEXT NULL,
    example_translation TEXT NULL,
    cefr_level VARCHAR(10) NOT NULL DEFAULT 'B1',
    topic VARCHAR(50) NOT NULL DEFAULT 'WORK',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_custom_words (user_id, lemma),
    INDEX idx_user_custom_words_user_id (user_id),
    INDEX idx_user_custom_words_level (user_id, cefr_level),
    INDEX idx_user_custom_words_topic (user_id, topic)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
