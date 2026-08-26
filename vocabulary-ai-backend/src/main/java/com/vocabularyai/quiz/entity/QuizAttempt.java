package com.vocabularyai.quiz.entity;

import com.vocabularyai.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * QuizAttempt Entity
 * 
 * Maps to 'quiz_attempts' table in MySQL
 * Tracks each time a user takes a quiz
 */
@Entity
@Table(name = "quiz_attempts", indexes = {
    @Index(name = "idx_quiz_attempts_user_id", columnList = "user_id"),
    @Index(name = "idx_quiz_attempts_quiz_id", columnList = "quiz_id"),
    @Index(name = "idx_quiz_attempts_user_quiz", columnList = "user_id, quiz_id"),
    @Index(name = "idx_quiz_attempts_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"user", "quiz", "createdAt"})
public class QuizAttempt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;
    
    @Column(nullable = false, columnDefinition = "DECIMAL(5,2)")
    @Builder.Default
    private Double score = 0.0;
    
    @Column(name = "total_questions", nullable = false)
    @Builder.Default
    private Integer totalQuestions = 0;
    
    @Column(name = "correct_answers", nullable = false)
    @Builder.Default
    private Integer correctAnswers = 0;
    
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;
    
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    /**
     * Check if attempt is completed
     */
    @Transient
    public boolean isCompleted() {
        return submittedAt != null;
    }
    
    /**
     * Get time taken (in seconds)
     */
    @Transient
    public Long getTimeTaken() {
        if (submittedAt == null) {
            return null;
        }
        return java.time.temporal.ChronoUnit.SECONDS.between(startedAt, submittedAt);
    }
}
