package com.vocabularyai.vocabulary.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Word Entity
 * 
 * Maps to 'words' table in MySQL
 * Represents a vocabulary word with pronunciation, definition, examples, etc.
 */
@Entity
@Table(name = "words", indexes = {
    @Index(name = "idx_words_category_id", columnList = "category_id"),
    @Index(name = "idx_words_word", columnList = "word"),
    @Index(name = "idx_words_level", columnList = "level"),
    @Index(name = "idx_words_category_level", columnList = "category_id, level")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"category", "createdAt", "updatedAt"})
public class Word {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    @Column(nullable = false, length = 255)
    private String word;
    
    @Column(length = 255)
    private String pronunciation;
    
    @Column(length = 255)
    private String phonetic;
    
    @Column(name = "part_of_speech", length = 50)
    private String partOfSpeech;
    
    @Column(columnDefinition = "TEXT")
    private String meaning;
    
    @Column(columnDefinition = "TEXT")
    private String definition;
    
    @Column(columnDefinition = "TEXT")
    private String example;
    
    @Column(name = "audio_url", length = 500)
    private String audioUrl;
    
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WordLevel level;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Word level enum (CEFR - Common European Framework of Reference)
     */
    public enum WordLevel {
        A1, A2, B1, B2, C1, C2
    }
}
