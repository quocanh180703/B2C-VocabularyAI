-- Migration V110: Add topic column to cefr_words and create topic_tag_queue table
ALTER TABLE cefr_words ADD COLUMN topic VARCHAR(30) NULL;

CREATE INDEX idx_cefr_topic ON cefr_words (topic);
CREATE INDEX idx_cefr_level_topic ON cefr_words (cefr_level, topic);

CREATE TABLE IF NOT EXISTS topic_tag_queue (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cefr_word_id VARCHAR(36) NOT NULL,
    lemma VARCHAR(100) NOT NULL,
    suggested_topic VARCHAR(30) NOT NULL,
    confidence DOUBLE NOT NULL,
    reason VARCHAR(255) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, APPROVED, REJECTED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tag_queue_status (status),
    INDEX idx_tag_queue_word (cefr_word_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
