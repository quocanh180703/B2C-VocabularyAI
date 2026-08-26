package com.vocabularyai.quiz.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * QuizAnswer Entity
 * 
 * Maps to 'quiz_answers' table in MySQL
 * Stores user's answer for each question in an attempt
 */
@Entity
@Table(name = "quiz_answers", indexes = {
    @Index(name = "idx_quiz_answers_attempt_id", columnList = "attempt_id"),
    @Index(name = "idx_quiz_answers_question_id", columnList = "question_id"),
    @Index(name = "uk_quiz_answers_attempt_question", columnList = "attempt_id, question_id", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"attempt", "question", "createdAt"})
public class QuizAnswer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private QuizAttempt attempt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
    
    @Column(nullable = false)
    private Integer answer; // Index of selected option (0-based)
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean correct = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
