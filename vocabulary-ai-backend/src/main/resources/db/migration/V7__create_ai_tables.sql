-- ============================================================
-- PHASE 7: AI Integration Tables
-- V7__create_ai_tables.sql
-- Database: MySQL 8.x
-- Purpose: Create AI conversation, messaging, and rate limiting tables
-- ============================================================

-- ============================================================
-- 1. ALTER AI_CONVERSATIONS TABLE (recreate with additional fields)
-- ============================================================
-- Drop old table if structure doesn't match
-- (Flyway runs once, so this is safe for initial setup)
DROP TABLE IF EXISTS ai_messages;
DROP TABLE IF EXISTS ai_conversations;

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
-- 2. AI_MESSAGES TABLE
-- ============================================================
CREATE TABLE ai_messages (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  conversation_id BIGINT NOT NULL,
  role ENUM('USER','ASSISTANT') NOT NULL,
  content LONGTEXT NOT NULL,
  tokens_used INT,
  response_time_ms BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  metadata JSON,
  CONSTRAINT fk_ai_messages_conversation FOREIGN KEY (conversation_id) REFERENCES ai_conversations(id) ON DELETE CASCADE ON UPDATE CASCADE,
  INDEX idx_ai_messages_conversation_id (conversation_id),
  INDEX idx_ai_messages_role (role),
  INDEX idx_ai_messages_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 3. RATE_LIMITS TABLE
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
