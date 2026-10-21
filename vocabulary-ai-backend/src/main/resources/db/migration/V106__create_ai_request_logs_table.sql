-- ============================================================
-- Migration: V106__create_ai_request_logs_table.sql
-- Description: Create ai_request_logs table for AI Usage & Cost Optimizer
-- Database: MySQL 8.x+
-- ============================================================

CREATE TABLE IF NOT EXISTS ai_request_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    feature VARCHAR(50) NOT NULL,
    model VARCHAR(100) NOT NULL,
    prompt_tokens INT NOT NULL DEFAULT 0,
    completion_tokens INT NOT NULL DEFAULT 0,
    total_tokens INT NOT NULL DEFAULT 0,
    estimated_cost_usd DECIMAL(10, 6) NOT NULL DEFAULT 0.000000,
    latency_ms BIGINT NOT NULL DEFAULT 0,
    status ENUM('SUCCESS', 'FAILED', 'CIRCUIT_BREAKER_OPEN', 'FALLBACK') NOT NULL DEFAULT 'SUCCESS',
    error_message VARCHAR(500) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ai_request_logs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_ai_request_logs_created_at (created_at),
    INDEX idx_ai_request_logs_feature (feature),
    INDEX idx_ai_request_logs_status (status),
    INDEX idx_ai_request_logs_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed initial activity logs for realistic monitoring visualization
INSERT INTO ai_request_logs (user_id, feature, model, prompt_tokens, completion_tokens, total_tokens, estimated_cost_usd, latency_ms, status, created_at)
VALUES 
    (1, 'AI_TUTOR_CHAT', 'gemini-2.5-flash', 320, 150, 470, 0.000069, 1250, 'SUCCESS', DATE_SUB(NOW(), INTERVAL 6 DAY)),
    (1, 'GRAMMAR_CHECK', 'gemini-2.5-flash', 180, 85, 265, 0.000039, 980, 'SUCCESS', DATE_SUB(NOW(), INTERVAL 5 DAY)),
    (1, 'VOCAB_EXPLAIN', 'gemini-2.5-flash', 210, 110, 320, 0.000048, 1100, 'SUCCESS', DATE_SUB(NOW(), INTERVAL 5 DAY)),
    (1, 'SPEAKING_FEEDBACK', 'gemini-2.5-flash', 450, 190, 640, 0.000091, 1620, 'SUCCESS', DATE_SUB(NOW(), INTERVAL 4 DAY)),
    (1, 'AI_TUTOR_CHAT', 'gemini-2.5-flash', 380, 175, 555, 0.000081, 1340, 'SUCCESS', DATE_SUB(NOW(), INTERVAL 4 DAY)),
    (1, 'VOCAB_EXPLAIN', 'gemini-2.5-flash', 250, 120, 370, 0.000055, 1050, 'SUCCESS', DATE_SUB(NOW(), INTERVAL 3 DAY)),
    (1, 'GRAMMAR_CHECK', 'gemini-2.5-flash', 190, 95, 285, 0.000043, 920, 'SUCCESS', DATE_SUB(NOW(), INTERVAL 3 DAY)),
    (1, 'AI_TUTOR_CHAT', 'gemini-2.5-flash', 510, 240, 750, 0.000110, 1580, 'SUCCESS', DATE_SUB(NOW(), INTERVAL 2 DAY)),
    (1, 'SPEAKING_FEEDBACK', 'gemini-2.5-flash', 480, 210, 690, 0.000099, 1490, 'SUCCESS', DATE_SUB(NOW(), INTERVAL 2 DAY)),
    (1, 'VOCAB_EXPLAIN', 'gemini-2.5-flash', 290, 130, 420, 0.000061, 1150, 'SUCCESS', DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (1, 'GRAMMAR_CHECK', 'gemini-2.5-flash', 220, 105, 325, 0.000048, 1010, 'SUCCESS', DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (1, 'AI_TUTOR_CHAT', 'gemini-2.5-flash', 430, 195, 625, 0.000091, 1280, 'SUCCESS', DATE_SUB(NOW(), INTERVAL 12 HOUR)),
    (1, 'VOCAB_EXPLAIN', 'gemini-2.5-flash', 260, 115, 375, 0.000054, 990, 'SUCCESS', DATE_SUB(NOW(), INTERVAL 4 HOUR)),
    (1, 'AI_TUTOR_CHAT', 'gemini-2.5-flash', 390, 180, 570, 0.000083, 1190, 'SUCCESS', DATE_SUB(NOW(), INTERVAL 1 HOUR));
