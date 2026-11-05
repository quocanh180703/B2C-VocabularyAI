package com.vocabularyai.ai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "grammar_practice_history", indexes = {
    @Index(name = "idx_gph_user_created", columnList = "user_id, created_at"),
    @Index(name = "idx_gph_user_mode", columnList = "user_id, mode")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrammarPracticeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 32)
    private String mode; // FREE, TOPIC, TRANSLATION

    @Column(length = 255)
    private String label;

    @Column(nullable = false)
    @Builder.Default
    private Integer score = 0;

    @Column(name = "input_text", nullable = false, columnDefinition = "TEXT")
    private String inputText;

    @Column(name = "context_json", columnDefinition = "TEXT")
    private String contextJson;

    @Column(name = "result_json", nullable = false, columnDefinition = "LONGTEXT")
    private String resultJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}

