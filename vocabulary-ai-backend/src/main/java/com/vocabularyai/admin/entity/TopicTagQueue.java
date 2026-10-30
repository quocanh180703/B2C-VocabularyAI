package com.vocabularyai.admin.entity;

import com.vocabularyai.vocabulary.entity.WordTopic;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "topic_tag_queue", indexes = {
    @Index(name = "idx_tag_queue_status", columnList = "status"),
    @Index(name = "idx_tag_queue_word", columnList = "cefr_word_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicTagQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cefr_word_id", nullable = false, length = 36)
    private String cefrWordId;

    @Column(name = "lemma", nullable = false, length = 100)
    private String lemma;

    @Column(name = "suggested_topic", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private WordTopic suggestedTopic;

    @Column(name = "confidence", nullable = false)
    private Double confidence;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
