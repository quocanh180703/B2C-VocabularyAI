package com.vocabularyai.ai.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "grammar_practice_prompts",
    indexes = {
        @Index(name = "idx_grammar_prompts_word_mode", columnList = "word_id, mode")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrammarPracticePrompt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "word_id", nullable = false)
    private Long wordId;

    @Column(name = "mode", nullable = false, length = 20)
    @Builder.Default
    private String mode = "SENTENCE";

    @Column(name = "level", length = 10)
    private String level;

    @Column(name = "vi_text", nullable = false, columnDefinition = "TEXT")
    private String viText;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

