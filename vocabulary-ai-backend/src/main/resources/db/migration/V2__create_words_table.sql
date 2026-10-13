-- ============================================================
-- Migration: V2__create_words_table.sql
-- ============================================================
-- Create words table for CEFR vocabulary dataset
-- 4962 words (A1-C1 levels)
-- Source: Oxford 3000/5000
-- ============================================================

-- Create CEFR level enum type
CREATE TABLE IF NOT EXISTS words (
    id VARCHAR(36) PRIMARY KEY,
    lemma VARCHAR(100) NOT NULL UNIQUE,
    language VARCHAR(5) NOT NULL DEFAULT 'en',
    cefr_level VARCHAR(10) NOT NULL CHECK (cefr_level IN ('A1', 'A2', 'B1', 'B2', 'C1')),
    part_of_speech VARCHAR(20) NOT NULL,
    pronunciation VARCHAR(200),
    definition LONGTEXT,
    source VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Indexes for common queries
    INDEX idx_lemma (lemma),
    INDEX idx_cefr_level (cefr_level),
    INDEX idx_part_of_speech (part_of_speech),
    INDEX idx_language (language),
    INDEX idx_level_pos (cefr_level, part_of_speech),
    
    -- Full-text search on definition
    FULLTEXT INDEX ft_definition (definition)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- Statistics
-- ============================================================
-- Expected rows per level (approximate):
-- A1: ~600-800 words
-- A2: ~800-1000 words
-- B1: ~1000-1200 words
-- B2: ~1000-1200 words
-- C1: ~1000-1160 words
-- Total: 4962 words
-- ============================================================

-- ============================================================
-- Insert sample data (from CEFR dataset)
-- ============================================================
-- This migration creates the table structure only.
-- Actual data will be imported via WordImportService
-- in a separate admin import operation.
-- ============================================================
