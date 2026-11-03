-- V113: Create listening_exercises table for Listening Practice (Dictation)
CREATE TABLE IF NOT EXISTS listening_exercises (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    topic VARCHAR(200) NOT NULL,
    level VARCHAR(10) NOT NULL,
    voice VARCHAR(50) NOT NULL,
    passage_text TEXT NOT NULL,
    audio_path VARCHAR(500) NOT NULL,
    user_answer TEXT NULL,
    score INT NULL,
    word_results LONGTEXT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at DATETIME NULL,
    INDEX idx_listening_user_created (user_id, created_at)
);

