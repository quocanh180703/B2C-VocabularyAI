-- Migration V109: Create placement_attempts table for entrance level placement testing
CREATE TABLE IF NOT EXISTS placement_attempts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    level_result VARCHAR(5) NOT NULL COMMENT 'A1, A2, B1, B2, C1',
    score_by_level VARCHAR(255) NOT NULL COMMENT 'JSON string, e.g. {"A1":4,"A2":3,"B1":2,"B2":1,"C1":0}',
    completed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_placement_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_placement_user_completed (user_id, completed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
