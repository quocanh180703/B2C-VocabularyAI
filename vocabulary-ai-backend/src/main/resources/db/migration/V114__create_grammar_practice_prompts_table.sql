-- V114: Create grammar_practice_prompts table for translation practice
CREATE TABLE IF NOT EXISTS grammar_practice_prompts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    word_id BIGINT NOT NULL,
    mode VARCHAR(20) NOT NULL DEFAULT 'SENTENCE',
    level VARCHAR(10) NULL,
    vi_text TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_grammar_prompts_word_mode (word_id, mode)
);

