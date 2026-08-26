package com.vocabularyai.quiz.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Question Entity
 * 
 * Maps to 'questions' table in MySQL
 */
@Entity
@Table(name = "questions", indexes = {
    @Index(name = "idx_questions_quiz_id", columnList = "quiz_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"quiz", "createdAt"})
public class Question {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    @JsonIgnore
    private Quiz quiz;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(nullable = false, columnDefinition = "JSON")
    private String options; // JSON array of options
    
    @Column(name = "correct_answer", nullable = false)
    private Integer correctAnswer; // Index of correct option (0-based)
    
    @Column(columnDefinition = "TEXT")
    private String explanation;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    /**
     * Parse options JSON to List
     */
    @Transient
    public List<String> getOptionsList() {
        // Parse JSON array to List<String>
        if (options == null || options.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(options, new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
