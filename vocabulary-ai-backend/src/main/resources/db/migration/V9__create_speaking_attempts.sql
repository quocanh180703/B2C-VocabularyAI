-- ============================================================
-- V9__create_speaking_attempts.sql
-- Tạo bảng Speaking Attempts (Phase 8)
-- ============================================================

-- ============================================================
-- 1. Tạo bảng speaking_attempts nếu chưa có
-- ============================================================
CREATE TABLE IF NOT EXISTS speaking_attempts (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID lần ghi âm',
  session_id BIGINT NOT NULL COMMENT 'ID phiên luyện tập',
  audio_url VARCHAR(500) NOT NULL COMMENT 'URL của file audio',
  audio_duration_seconds INT COMMENT 'Độ dài audio (giây)',
  transcript LONGTEXT COMMENT 'Transcript từ Speech-to-Text',
  transcript_confidence DECIMAL(5,2) COMMENT 'Độ tin cậy transcript (0-100)',
  attempt_number INT NOT NULL COMMENT 'Số thứ tự lần ghi âm',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời gian ghi âm',
  
  CONSTRAINT fk_speaking_attempts_session FOREIGN KEY (session_id) 
    REFERENCES speaking_sessions(id) ON DELETE CASCADE ON UPDATE CASCADE,
  
  INDEX idx_speaking_attempts_session_id (session_id),
  INDEX idx_speaking_attempts_attempt_number (session_id, attempt_number),
  INDEX idx_speaking_attempts_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT 'Lưu từng lần ghi âm trong phiên luyện tập';

-- ============================================================
-- 2. Tạo bảng speaking_feedback_v8 nếu chưa có
-- ============================================================
CREATE TABLE IF NOT EXISTS speaking_feedback_v8 (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID feedback',
  attempt_id BIGINT UNIQUE COMMENT 'ID attempt',
  session_id BIGINT UNIQUE COMMENT 'ID session',
  
  overall_score DECIMAL(5,2) COMMENT 'Điểm tổng thể (0-100)',
  grammar_score DECIMAL(5,2) COMMENT 'Điểm ngữ pháp (0-100)',
  vocabulary_score DECIMAL(5,2) COMMENT 'Điểm từ vựng (0-100)',
  fluency_score DECIMAL(5,2) COMMENT 'Điểm trôi chảy (0-100)',
  content_score DECIMAL(5,2) COMMENT 'Điểm nội dung (0-100)',
  pronunciation_score DECIMAL(5,2) COMMENT 'Điểm phát âm (0-100)',
  
  feedback LONGTEXT COMMENT 'Feedback text',
  corrections JSON COMMENT 'Các lỗi (JSON)',
  suggestions JSON COMMENT 'Các gợi ý (JSON)',
  strengths JSON COMMENT 'Điểm mạnh (JSON)',
  weaknesses JSON COMMENT 'Điểm yếu (JSON)',
  
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời gian tạo',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Thời gian cập nhật',
  
  CONSTRAINT fk_speaking_feedback_v8_attempt FOREIGN KEY (attempt_id) 
    REFERENCES speaking_attempts(id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_speaking_feedback_v8_session FOREIGN KEY (session_id) 
    REFERENCES speaking_sessions(id) ON DELETE CASCADE ON UPDATE CASCADE,
  
  CONSTRAINT chk_feedback_v8_overall_score CHECK (overall_score IS NULL OR (overall_score >= 0 AND overall_score <= 100)),
  CONSTRAINT chk_feedback_v8_grammar_score CHECK (grammar_score IS NULL OR (grammar_score >= 0 AND grammar_score <= 100)),
  CONSTRAINT chk_feedback_v8_vocabulary_score CHECK (vocabulary_score IS NULL OR (vocabulary_score >= 0 AND vocabulary_score <= 100)),
  CONSTRAINT chk_feedback_v8_fluency_score CHECK (fluency_score IS NULL OR (fluency_score >= 0 AND fluency_score <= 100)),
  CONSTRAINT chk_feedback_v8_content_score CHECK (content_score IS NULL OR (content_score >= 0 AND content_score <= 100)),
  CONSTRAINT chk_feedback_v8_pronunciation_score CHECK (pronunciation_score IS NULL OR (pronunciation_score >= 0 AND pronunciation_score <= 100)),
  
  INDEX idx_speaking_feedback_v8_attempt (attempt_id),
  INDEX idx_speaking_feedback_v8_session (session_id),
  INDEX idx_speaking_feedback_v8_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT 'Lưu đánh giá chi tiết cho speaking';

-- ============================================================
-- Summary
-- ============================================================
-- ✓ speaking_attempts: Bảng mới để lưu từng lần ghi âm
-- ✓ speaking_feedback_v8: Bảng mới với toàn bộ score fields
-- ============================================================
