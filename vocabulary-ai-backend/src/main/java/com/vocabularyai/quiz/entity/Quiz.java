package com.vocabularyai.quiz.entity;

import com.vocabularyai.vocabulary.entity.Category;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Quiz Entity
 * 
 * Maps to 'quizzes' table in MySQL
 */
@Entity
@Table(name = "quizzes", indexes = {
    @Index(name = "idx_quizzes_category_id", columnList = "category_id"),
    @Index(name = "idx_quizzes_level", columnList = "level"),
    @Index(name = "idx_quizzes_status", columnList = "status"),
    @Index(name = "idx_quizzes_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"category", "createdAt", "updatedAt"})
public class Quiz {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    
    @Column(nullable = false, length = 255)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    private QuizLevel level;
    
    @Column(name = "time_limit")
    private Integer timeLimit; // in seconds
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private QuizStatus status = QuizStatus.DRAFT;
    
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
     * Quiz level enum
     */
    public enum QuizLevel {
        A1, A2, B1, B2, C1, C2
    }
    
    /**
     * Quiz status enum
     */
    public enum QuizStatus {
        DRAFT,     // Being created
        PUBLISHED, // Available for taking
        ARCHIVED   // No longer available
    }
}
