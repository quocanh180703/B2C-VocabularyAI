package com.vocabularyai.vocabulary.entity;

import com.vocabularyai.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * FavoriteWord Entity
 * 
 * Maps to 'favorite_words' table in MySQL
 * Represents a user's favorite words (many-to-many relationship)
 */
@Entity
@Table(name = "favorite_words", indexes = {
    @Index(name = "idx_favorite_words_user_id", columnList = "user_id"),
    @Index(name = "idx_favorite_words_word_id", columnList = "word_id"),
    @Index(name = "uk_favorite_user_word", columnList = "user_id, word_id", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"user", "word", "createdAt"})
public class FavoriteWord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
