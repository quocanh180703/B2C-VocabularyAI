package com.vocabularyai.vocabulary.entity;

import com.vocabularyai.vocabulary.entity.Category;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Word Entity
 * 
 * Stores user vocabulary - words added to learning lists
 * This is for user-generated word entries, not the CEFR dataset
 * Use CefrWord entity for CEFR dataset words
 * 
 * Fields:
 * - word: The English word/phrase
 * - meaning: Meaning/definition in user's native language
 * - partOfSpeech: Grammar role (noun, verb, etc.)
 * - level: CEFR level (A1-C1) for learning targeting
 * - example: Usage example sentence
 * - definition: English definition from reference
 * - pronunciation: IPA pronunciation
 * - audioUrl: Audio pronunciation link
 * - imageUrl: Associated image URL
 * 
 * Table: words
 * ID: Long (auto-increment) for user vocabulary tracking
 */
@Entity
@Table(name = "words", indexes = {
    @Index(name = "idx_word", columnList = "word"),
    @Index(name = "idx_level", columnList = "level"),
    @Index(name = "idx_category_id", columnList = "category_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"category", "createdAt", "updatedAt"})
public class Word {

    /**
     * Primary key - auto-increment for user vocabulary
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Category reference for word organization
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    /**
     * The English word or phrase
     */
    @Column(nullable = false, length = 255)
    private String word;

    /**
     * Meaning/translation in user's native language (Vietnamese, etc.)
     */
    @Column(columnDefinition = "LONGTEXT")
    private String meaning;

    /**
     * Part of speech (noun, verb, adjective, adverb, etc.)
     */
    @Column(name = "part_of_speech", length = 50)
    private String partOfSpeech;

    /**
     * Definition in English
     */
    @Column(columnDefinition = "LONGTEXT")
    private String definition;

    /**
     * CEFR level for learning targeting (A1, A2, B1, B2, C1)
     */
    @Column(length = 10)
    private String level;

    /**
     * Usage example sentence
     */
    @Column(columnDefinition = "LONGTEXT")
    private String example;

    /**
     * IPA pronunciation or phonetic spelling
     */
    @Column(name = "pronunciation", length = 200)
    private String pronunciation;

    /**
     * Audio pronunciation URL
     */
    @Column(name = "audio_url", columnDefinition = "LONGTEXT")
    private String audioUrl;

    /**
     * Associated image URL (for visual learning)
     */
    @Column(name = "image_url", columnDefinition = "LONGTEXT")
    private String imageUrl;

    /**
     * Phonetic representation
     */
    @Column(length = 200)
    private String phonetic;

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
     * Get the word text
     */
    public String getWord() {
        return word;
    }

    /**
     * Get the level
     */
    public String getLevel() {
        return level;
    }

    /**
     * Get the example
     */
    public String getExample() {
        return example;
    }
}
