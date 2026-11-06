-- Migration V118: Create content_reports table for error reporting on vocabulary
CREATE TABLE IF NOT EXISTS content_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    cefr_word_id VARCHAR(100) NOT NULL,
    lemma_snapshot VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    description VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    admin_note VARCHAR(500) NULL,
    resolved_by BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at DATETIME NULL,
    INDEX idx_content_reports_user_id (user_id),
    INDEX idx_content_reports_status (status),
    INDEX idx_content_reports_cefr_word_id (cefr_word_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
