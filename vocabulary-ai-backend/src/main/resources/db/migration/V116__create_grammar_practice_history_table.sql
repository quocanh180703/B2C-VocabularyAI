-- V116: Create grammar_practice_history table for storing practice sessions across modes
CREATE TABLE IF NOT EXISTS grammar_practice_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    mode VARCHAR(32) NOT NULL,
    label VARCHAR(255) NULL,
    score INT NOT NULL DEFAULT 0,
    input_text TEXT NOT NULL,
    context_json TEXT NULL,
    result_json LONGTEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_gph_user_created (user_id, created_at),
    INDEX idx_gph_user_mode (user_id, mode)
);

