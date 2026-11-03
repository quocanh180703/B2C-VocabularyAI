package com.vocabularyai.listening.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "listening_exercises",
    indexes = {
        @Index(name = "idx_listening_user_created", columnList = "user_id, created_at")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListeningExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "topic", nullable = false, length = 200)
    private String topic;

    @Column(name = "level", nullable = false, length = 10)
    private String level;

    @Column(name = "voice", nullable = false, length = 50)
    private String voice;

    @Column(name = "passage_text", nullable = false, columnDefinition = "TEXT")
    private String passageText;

    @Column(name = "audio_path", nullable = false, length = 500)
    private String audioPath;

    @Column(name = "user_answer", columnDefinition = "TEXT")
    private String userAnswer;

    @Column(name = "score")
    private Integer score;

    @Column(name = "word_results", columnDefinition = "LONGTEXT")
    private String wordResults;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "CREATED"; // CREATED, COMPLETED

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}

