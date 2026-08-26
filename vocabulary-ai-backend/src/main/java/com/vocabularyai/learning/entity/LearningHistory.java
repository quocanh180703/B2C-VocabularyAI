package com.vocabularyai.learning.entity;

import com.vocabularyai.user.entity.User;
import com.vocabularyai.vocabulary.entity.Word;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * LearningHistory Entity
 * 
 * Maps to 'learning_history' table in MySQL
 * Tracks detailed history of all learning activities
 */
@Entity
@Table(name = "learning_history", indexes = {
    @Index(name = "idx_learning_history_user_id", columnList = "user_id"),
    @Index(name = "idx_learning_history_word_id", columnList = "word_id"),
    @Index(name = "idx_learning_history_activity_type", columnList = "activity_type"),
    @Index(name = "idx_learning_history_created_at", columnList = "created_at"),
    @Index(name = "idx_learning_history_user_created", columnList = "user_id, created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"user", "word", "createdAt"})
public class LearningHistory {
    
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
    @Column(name = "activity_type", nullable = false)
    private ActivityType activityType;
    
    @Column(name = "is_correct")
    private Boolean isCorrect;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    /**
     * Activity type enum
     */
    public enum ActivityType {
        LEARN,    // First time learning the word
        REVIEW,   // Review session
        QUIZ,     // Quiz activity
        SPEAKING  // Speaking practice
    }
}
