package com.vocabularyai.learning.entity;

import com.vocabularyai.user.entity.User;
import com.vocabularyai.vocabulary.entity.Word;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * LearningProgress Entity
 * 
 * Maps to 'learning_progress' table in MySQL
 * Tracks user's progress on each word (Spaced Repetition System)
 */
@Entity
@Table(name = "learning_progress", indexes = {
    @Index(name = "idx_learning_progress_user_id", columnList = "user_id"),
    @Index(name = "idx_learning_progress_word_id", columnList = "word_id"),
    @Index(name = "idx_learning_progress_status", columnList = "status"),
    @Index(name = "idx_learning_progress_next_review", columnList = "next_review_at"),
    @Index(name = "uk_learning_progress_user_word", columnList = "user_id, word_id", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"user", "word", "createdAt", "updatedAt"})
public class LearningProgress {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private LearningStatus status = LearningStatus.NEW;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer correctCount = 0;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer wrongCount = 0;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer reviewCount = 0;
    
    @Column(name = "last_reviewed_at")
    private LocalDateTime lastReviewedAt;
    
    @Column(name = "next_review_at")
    private LocalDateTime nextReviewAt;
    
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
     * Learning status enum
     */
    public enum LearningStatus {
        NEW,      // Never learned
        LEARNING, // Currently learning
        MASTERED  // Fully mastered
    }
    
    /**
     * Calculate accuracy percentage
     */
    public double getAccuracy() {
        int total = correctCount + wrongCount;
        if (total == 0) {
            return 0.0;
        }
        return (double) correctCount / total * 100;
    }
    
    /**
     * Check if word needs review
     */
    public boolean needsReview() {
        if (nextReviewAt == null) {
            return status != LearningStatus.NEW;
        }
        return LocalDateTime.now().isAfter(nextReviewAt);
    }
}
