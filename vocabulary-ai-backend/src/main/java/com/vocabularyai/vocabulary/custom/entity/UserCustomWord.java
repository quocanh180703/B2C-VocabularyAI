package com.vocabularyai.vocabulary.custom.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_custom_words", indexes = {
    @Index(name = "idx_user_custom_words_user_id", columnList = "user_id"),
    @Index(name = "idx_user_custom_words_level", columnList = "user_id, cefr_level"),
    @Index(name = "idx_user_custom_words_topic", columnList = "user_id, topic")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_custom_words", columnNames = {"user_id", "lemma"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCustomWord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "lemma", nullable = false, length = 100)
    private String lemma;

    @Column(name = "vietnamese_meaning", nullable = false, length = 500)
    private String vietnameseMeaning;

    @Column(name = "part_of_speech", length = 50)
    private String partOfSpeech;

    @Column(name = "example", columnDefinition = "TEXT")
    private String example;

    @Column(name = "example_translation", columnDefinition = "TEXT")
    private String exampleTranslation;

    @Column(name = "cefr_level", nullable = false, length = 10)
    @Builder.Default
    private String cefrLevel = "B1";

    @Column(name = "topic", nullable = false, length = 50)
    @Builder.Default
    private String topic = "WORK";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
