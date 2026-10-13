package com.vocabularyai.vocabulary.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * CefrWord Entity
 * 
 * Lưu trữ từ vựng tiếng Anh từ CEFR dataset
 * - 4962 từ từ A1 đến C1
 * - Structured data: lemma, pronunciation, definition, part of speech
 * - Indexed for fast search and filtering
 * 
 * Table: cefr_words
 * Storage: ~2-3 MB (4962 rows)
 */
@Entity
@Table(name = "cefr_words", indexes = {
    @Index(name = "idx_cefr_lemma", columnList = "lemma", unique = true),
    @Index(name = "idx_cefr_level", columnList = "cefr_level"),
    @Index(name = "idx_cefr_pos", columnList = "part_of_speech"),
    @Index(name = "idx_cefr_language", columnList = "language"),
    @Index(name = "idx_cefr_level_pos", columnList = "cefr_level, part_of_speech")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"createdAt", "updatedAt"})
public class CefrWord {

    /**
     * UUID from dataset
     */
    @Id
    private String id;

    /**
     * Word text (lemma/headword)
     * 
     * Unique: Each word appears only once in dataset
     * Example: "difference", "cow", "funny", "band", "breakfast"
     */
    @Column(name = "lemma", nullable = false, unique = true, length = 100)
    private String lemma;

    /**
     * Language code (ISO 639-1)
     * 
     * Always "en" for English in this dataset
     */
    @Column(name = "language", nullable = false, length = 5)
    private String language;

    /**
     * CEFR Level
     * 
     * A1: 600-700 words
     * A2: 1000-1500 words
     * B1: 1500-2000 words
     * B2: 2000-3000 words
     * C1: 3000-4000+ words
     * 
     * Distribution: A1~1000, A2~800, B1~1000, B2~1000, C1~1160 (total ~4962)
     */
    @Column(name = "cefr_level", nullable = false)
    @Enumerated(EnumType.STRING)
    private CEFRLevel cefrLevel;

    /**
     * Part of Speech
     * 
     * Examples: noun, verb, adj, adv, det, prep, conj, intj, aux, pron
     */
    @Column(name = "part_of_speech", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private PartOfSpeech partOfSpeech;

    /**
     * IPA Pronunciation
     * 
     * International Phonetic Alphabet
     * Example: /ˈdɪfrəns/, /kaʊ/, /ˈfʌni/
     * 
     * Nullable: Some words don't have pronunciation
     */
    @Column(name = "pronunciation", length = 200)
    private String pronunciation;

    /**
     * Definition in English
     * 
     * Short explanation of word meaning
     * Example: "The quality of being different."
     * 
     * Nullable: Some words don't have definitions in dataset
     */
    @Column(name = "definition", columnDefinition = "LONGTEXT")
    private String definition;

    /**
     * Source of definition
     * 
     * Usually "oxford_3000_5000" (Oxford English Dictionary)
     */
    @Column(name = "source", length = 50)
    private String source;

    /**
     * Timestamp created
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * Timestamp last updated
     */
    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // ==================== Helper Methods ====================

    /**
     * Get CEFR level as integer (for comparison)
     * 
     * A1 = 1, A2 = 2, B1 = 3, B2 = 4, C1 = 5
     */
    public int getLevelIndex() {
        return cefrLevel != null ? cefrLevel.getLevelIndex() : 0;
    }

    /**
     * Check if word is at beginner level (A1-A2)
     */
    public boolean isBeginnerLevel() {
        return cefrLevel == CEFRLevel.A1 || cefrLevel == CEFRLevel.A2;
    }

    /**
     * Check if word is at intermediate level (B1-B2)
     */
    public boolean isIntermediateLevel() {
        return cefrLevel == CEFRLevel.B1 || cefrLevel == CEFRLevel.B2;
    }

    /**
     * Check if word is at advanced level (C1+)
     */
    public boolean isAdvancedLevel() {
        return cefrLevel == CEFRLevel.C1;
    }
}
