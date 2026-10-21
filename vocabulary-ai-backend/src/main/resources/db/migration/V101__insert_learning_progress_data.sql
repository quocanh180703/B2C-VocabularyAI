-- Flyway Migration: V101__insert_learning_progress_data.sql
-- Sample learning progress and history for SRS demonstration

INSERT INTO learning_progress (user_id, word_id, status, correct_count, wrong_count, review_count, last_reviewed_at, next_review_at, created_at, updated_at)
VALUES 
(2, 1, 'LEARNING', 2, 0, 2, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 HOUR, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 2 DAY),
(2, 2, 'LEARNING', 1, 1, 2, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 HOUR, NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 1 DAY),
(2, 3, 'LEARNING', 3, 0, 3, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 5 HOUR, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 3 DAY),
(2, 4, 'LEARNING', 0, 1, 1, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 30 MINUTE, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 1 DAY),
(2, 5, 'LEARNING', 2, 1, 3, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 10 MINUTE, NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 2 DAY),
(2, 6, 'LEARNING', 1, 0, 1, NOW() - INTERVAL 1 HOUR, NOW() + INTERVAL 2 DAY, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 HOUR),
(2, 7, 'MASTERED', 5, 0, 5, NOW() - INTERVAL 5 DAY, NOW() + INTERVAL 25 DAY, NOW() - INTERVAL 20 DAY, NOW() - INTERVAL 5 DAY)
ON DUPLICATE KEY UPDATE updated_at = NOW();

INSERT INTO learning_progress (user_id, word_id, status, correct_count, wrong_count, review_count, last_reviewed_at, next_review_at, created_at, updated_at)
VALUES 
(1, 1, 'LEARNING', 2, 0, 2, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 HOUR, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 2 DAY),
(1, 2, 'LEARNING', 1, 1, 2, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 HOUR, NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 1 DAY),
(1, 3, 'LEARNING', 3, 0, 3, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 5 HOUR, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 3 DAY),
(1, 4, 'LEARNING', 0, 1, 1, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 30 MINUTE, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 1 DAY),
(1, 5, 'LEARNING', 2, 1, 3, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 10 MINUTE, NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 2 DAY)
ON DUPLICATE KEY UPDATE updated_at = NOW();
