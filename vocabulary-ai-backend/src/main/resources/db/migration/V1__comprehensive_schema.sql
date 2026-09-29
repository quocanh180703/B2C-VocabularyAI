-- ============================================================
-- VocabularyAI - Comprehensive Database Schema
-- V1__comprehensive_schema.sql
-- 
-- Complete schema with all tables:
-- - Users & Authentication (users, refresh_tokens, password_reset_tokens, otp_verifications, pending_registrations)
-- - Learning (categories, words, favorite_words, learning_progress, learning_history)
-- - Quizzes (quizzes, questions, quiz_attempts, quiz_answers)
-- - AI Integration (ai_conversations, ai_messages, rate_limits)
-- - Speaking (speaking_sessions, speaking_attempts, speaking_feedback)
--
-- Database: MySQL 8.x+
-- Engine: InnoDB
-- Charset: utf8mb4
-- ============================================================

-- ============================================================
-- 1. USERS TABLE
-- ============================================================
CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(100) NOT NULL,
  email VARCHAR(255) NOT NULL,
  password VARCHAR(255) NOT NULL,
  full_name VARCHAR(150),
  avatar_url VARCHAR(500),
  role ENUM('ADMIN', 'STUDENT') NOT NULL DEFAULT 'STUDENT',
  status ENUM('ACTIVE', 'INACTIVE', 'BANNED') NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_users_username UNIQUE (username),
  CONSTRAINT uk_users_email UNIQUE (email),
  INDEX idx_users_role (role),
  INDEX idx_users_status (status),
  INDEX idx_users_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 2. REFRESH TOKENS (JWT)
-- ============================================================
CREATE TABLE refresh_tokens (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  token VARCHAR(500) NOT NULL,
  expires_at DATETIME NOT NULL,
  revoked BOOLEAN NOT NULL DEFAULT FALSE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_refresh_tokens_token UNIQUE (token),
  CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
  INDEX idx_refresh_tokens_user_id (user_id),
  INDEX idx_refresh_tokens_expires_at (expires_at),
  INDEX idx_refresh_tokens_revoked (revoked)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 3. PASSWORD RESET TOKENS (OTP-based)
-- ============================================================
CREATE TABLE password_reset_tokens (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT,
  email VARCHAR(255) NOT NULL,
  reset_token VARCHAR(255) NOT NULL,
  expires_at DATETIME NOT NULL,
  used BOOLEAN NOT NULL DEFAULT FALSE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_password_reset_tokens_token UNIQUE (reset_token),
  CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE,
  INDEX idx_password_reset_tokens_email (email),
  INDEX idx_password_reset_tokens_token (reset_token),
  INDEX idx_password_reset_tokens_expires_at (expires_at),
  INDEX idx_password_reset_tokens_used (used)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 4. OTP VERIFICATIONS (Register & Forgot Password)
-- ============================================================
CREATE TABLE otp_verifications (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(255) NOT NULL,
  otp_code VARCHAR(10) NOT NULL,
  otp_type ENUM('REGISTER', 'FORGOT_PASSWORD') NOT NULL,
  attempts INT NOT NULL DEFAULT 0,
  max_attempts INT NOT NULL DEFAULT 5,
  expires_at DATETIME NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_otp_email (email),
  INDEX idx_otp_email_type (email, otp_type),
  INDEX idx_otp_expires_at (expires_at),
  INDEX idx_otp_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 4.5 PENDING REGISTRATIONS (Temporary Registration Data - Step 1)
-- ============================================================
CREATE TABLE pending_registrations (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  username VARCHAR(100) NOT NULL UNIQUE,
  full_name VARCHAR(150) NOT NULL,
  password VARCHAR(255) NOT NULL,
  expires_at DATETIME NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_pending_registrations_email (email),
  INDEX idx_pending_registrations_username (username),
  INDEX idx_pending_registrations_expires_at (expires_at),
  INDEX idx_pending_registrations_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 5. CATEGORIES (Vocabulary Categories)
-- ============================================================
CREATE TABLE categories (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  description TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_categories_name UNIQUE (name),
  INDEX idx_categories_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 6. WORDS (Vocabulary Words)
-- ============================================================
CREATE TABLE words (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  category_id BIGINT NOT NULL,
  word VARCHAR(255) NOT NULL,
  pronunciation VARCHAR(255),
  phonetic VARCHAR(255),
  part_of_speech VARCHAR(50),
  meaning TEXT,
  definition TEXT,
  example TEXT,
  audio_url VARCHAR(500),
  image_url VARCHAR(500),
  level ENUM('A1','A2','B1','B2','C1','C2') NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_words_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  INDEX idx_words_category_id (category_id),
  INDEX idx_words_word (word),
  INDEX idx_words_level (level),
  INDEX idx_words_category_level (category_id, level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 7. FAVORITE WORDS
-- ============================================================
CREATE TABLE favorite_words (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  word_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_favorite_user_word UNIQUE (user_id, word_id),
  CONSTRAINT fk_favorite_words_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_favorite_words_word FOREIGN KEY (word_id) REFERENCES words(id) ON DELETE CASCADE ON UPDATE CASCADE,
  INDEX idx_favorite_words_user_id (user_id),
  INDEX idx_favorite_words_word_id (word_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 8. LEARNING PROGRESS
-- ============================================================
CREATE TABLE learning_progress (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  word_id BIGINT NOT NULL,
  status ENUM('NEW','LEARNING','MASTERED') NOT NULL DEFAULT 'NEW',
  correct_count INT NOT NULL DEFAULT 0,
  wrong_count INT NOT NULL DEFAULT 0,
  review_count INT NOT NULL DEFAULT 0,
  last_reviewed_at DATETIME NULL,
  next_review_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_learning_progress_user_word UNIQUE (user_id, word_id),
  CONSTRAINT fk_learning_progress_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_learning_progress_word FOREIGN KEY (word_id) REFERENCES words(id) ON DELETE CASCADE ON UPDATE CASCADE,
  INDEX idx_learning_progress_user_id (user_id),
  INDEX idx_learning_progress_word_id (word_id),
  INDEX idx_learning_progress_status (status),
  INDEX idx_learning_progress_next_review (next_review_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 9. LEARNING HISTORY
-- ============================================================
CREATE TABLE learning_history (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  word_id BIGINT NOT NULL,
  activity_type ENUM('LEARN','REVIEW','QUIZ','SPEAKING') NOT NULL,
  is_correct BOOLEAN NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_learning_history_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_learning_history_word FOREIGN KEY (word_id) REFERENCES words(id) ON DELETE CASCADE ON UPDATE CASCADE,
  INDEX idx_learning_history_user_id (user_id),
  INDEX idx_learning_history_word_id (word_id),
  INDEX idx_learning_history_activity_type (activity_type),
  INDEX idx_learning_history_created_at (created_at),
  INDEX idx_learning_history_user_created (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 10. QUIZZES
-- ============================================================
CREATE TABLE quizzes (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  category_id BIGINT NULL,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  level ENUM('A1','A2','B1','B2','C1','C2'),
  time_limit INT NULL,
  status ENUM('DRAFT','PUBLISHED','ARCHIVED') NOT NULL DEFAULT 'DRAFT',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_quizzes_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL ON UPDATE CASCADE,
  INDEX idx_quizzes_category_id (category_id),
  INDEX idx_quizzes_level (level),
  INDEX idx_quizzes_status (status),
  INDEX idx_quizzes_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 11. QUESTIONS
-- ============================================================
CREATE TABLE questions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  quiz_id BIGINT NOT NULL,
  content TEXT NOT NULL,
  options JSON NOT NULL,
  correct_answer INT NOT NULL,
  explanation TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_questions_quiz FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT chk_questions_correct_answer CHECK (correct_answer >= 0),
  INDEX idx_questions_quiz_id (quiz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 12. QUIZ ATTEMPTS
-- ============================================================
CREATE TABLE quiz_attempts (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  quiz_id BIGINT NOT NULL,
  score DECIMAL(5,2) NOT NULL DEFAULT 0.00,
  total_questions INT NOT NULL DEFAULT 0,
  correct_answers INT NOT NULL DEFAULT 0,
  started_at DATETIME NOT NULL,
  submitted_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_quiz_attempts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_quiz_attempts_quiz FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT chk_quiz_attempts_score CHECK (score >= 0 AND score <= 100),
  CONSTRAINT chk_quiz_attempts_total_questions CHECK (total_questions >= 0),
  CONSTRAINT chk_quiz_attempts_correct_answers CHECK (correct_answers >= 0 AND correct_answers <= total_questions),
  INDEX idx_quiz_attempts_user_id (user_id),
  INDEX idx_quiz_attempts_quiz_id (quiz_id),
  INDEX idx_quiz_attempts_user_quiz (user_id, quiz_id),
  INDEX idx_quiz_attempts_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 13. QUIZ ANSWERS
-- ============================================================
CREATE TABLE quiz_answers (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  attempt_id BIGINT NOT NULL,
  question_id BIGINT NOT NULL,
  answer INT NOT NULL,
  correct BOOLEAN NOT NULL DEFAULT FALSE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_quiz_answers_attempt_question UNIQUE (attempt_id, question_id),
  CONSTRAINT fk_quiz_answers_attempt FOREIGN KEY (attempt_id) REFERENCES quiz_attempts(id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_quiz_answers_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT chk_quiz_answers_answer CHECK (answer >= 0),
  INDEX idx_quiz_answers_attempt_id (attempt_id),
  INDEX idx_quiz_answers_question_id (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 14. AI CONVERSATIONS
-- ============================================================
CREATE TABLE ai_conversations (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  type ENUM('GENERAL','VOCABULARY','GRAMMAR','LEARNING') NOT NULL DEFAULT 'GENERAL',
  title VARCHAR(255),
  summary LONGTEXT,
  is_archived BOOLEAN NOT NULL DEFAULT FALSE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_ai_conversations_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
  INDEX idx_ai_conversations_user_id (user_id),
  INDEX idx_ai_conversations_type (type),
  INDEX idx_ai_conversations_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 15. AI MESSAGES
-- ============================================================
CREATE TABLE ai_messages (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  conversation_id BIGINT NOT NULL,
  role ENUM('USER','ASSISTANT') NOT NULL,
  content LONGTEXT NOT NULL,
  tokens_used INT,
  response_time_ms BIGINT,
  metadata JSON,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_ai_messages_conversation FOREIGN KEY (conversation_id) REFERENCES ai_conversations(id) ON DELETE CASCADE ON UPDATE CASCADE,
  INDEX idx_ai_messages_conversation_id (conversation_id),
  INDEX idx_ai_messages_role (role),
  INDEX idx_ai_messages_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 16. RATE LIMITS (API/AI Rate Limiting)
-- ============================================================
CREATE TABLE rate_limits (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  tokens_used BIGINT NOT NULL DEFAULT 0,
  request_count INT NOT NULL DEFAULT 0,
  period_start DATETIME NOT NULL,
  period_end DATETIME NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_rate_limits_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
  INDEX idx_rate_limits_user_id (user_id),
  INDEX idx_rate_limits_user_period (user_id, period_start),
  INDEX idx_rate_limits_period (period_start, period_end)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 17. SPEAKING SESSIONS
-- ============================================================
CREATE TABLE speaking_sessions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  topic VARCHAR(255) NOT NULL,
  level ENUM('A1','A2','B1','B2','C1','C2') NOT NULL,
  started_at DATETIME NOT NULL,
  ended_at DATETIME NULL,
  overall_score DECIMAL(5,2) NULL,
  status ENUM('IN_PROGRESS','COMPLETED','CANCELLED') NOT NULL DEFAULT 'IN_PROGRESS',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_speaking_sessions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT chk_speaking_sessions_score CHECK (overall_score IS NULL OR (overall_score >= 0 AND overall_score <= 100)),
  INDEX idx_speaking_sessions_user_id (user_id),
  INDEX idx_speaking_sessions_status (status),
  INDEX idx_speaking_sessions_created_at (created_at),
  INDEX idx_speaking_sessions_user_created (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 18. SPEAKING ATTEMPTS
-- ============================================================
CREATE TABLE speaking_attempts (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  session_id BIGINT NOT NULL,
  audio_url VARCHAR(500) NOT NULL,
  audio_duration_seconds INT,
  transcript LONGTEXT,
  transcript_confidence DECIMAL(5,2),
  attempt_number INT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_speaking_attempts_session FOREIGN KEY (session_id) REFERENCES speaking_sessions(id) ON DELETE CASCADE ON UPDATE CASCADE,
  INDEX idx_speaking_attempts_session_id (session_id),
  INDEX idx_speaking_attempts_attempt_number (session_id, attempt_number),
  INDEX idx_speaking_attempts_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 19. SPEAKING FEEDBACK
-- ============================================================
CREATE TABLE speaking_feedback (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  attempt_id BIGINT UNIQUE,
  session_id BIGINT UNIQUE,
  overall_score DECIMAL(5,2),
  grammar_score DECIMAL(5,2),
  vocabulary_score DECIMAL(5,2),
  fluency_score DECIMAL(5,2),
  content_score DECIMAL(5,2),
  pronunciation_score DECIMAL(5,2),
  feedback LONGTEXT,
  corrections JSON,
  suggestions JSON,
  strengths JSON,
  weaknesses JSON,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_speaking_feedback_attempt FOREIGN KEY (attempt_id) REFERENCES speaking_attempts(id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_speaking_feedback_session FOREIGN KEY (session_id) REFERENCES speaking_sessions(id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT chk_feedback_overall_score CHECK (overall_score IS NULL OR (overall_score >= 0 AND overall_score <= 100)),
  CONSTRAINT chk_feedback_grammar_score CHECK (grammar_score IS NULL OR (grammar_score >= 0 AND grammar_score <= 100)),
  CONSTRAINT chk_feedback_vocabulary_score CHECK (vocabulary_score IS NULL OR (vocabulary_score >= 0 AND vocabulary_score <= 100)),
  CONSTRAINT chk_feedback_fluency_score CHECK (fluency_score IS NULL OR (fluency_score >= 0 AND fluency_score <= 100)),
  CONSTRAINT chk_feedback_content_score CHECK (content_score IS NULL OR (content_score >= 0 AND content_score <= 100)),
  CONSTRAINT chk_feedback_pronunciation_score CHECK (pronunciation_score IS NULL OR (pronunciation_score >= 0 AND pronunciation_score <= 100)),
  INDEX idx_speaking_feedback_attempt (attempt_id),
  INDEX idx_speaking_feedback_session (session_id),
  INDEX idx_speaking_feedback_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- END OF SCHEMA
-- ============================================================
