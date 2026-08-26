package com.vocabularyai.ai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * AiMessage Entity
 * 
 * Maps to 'ai_messages' table in MySQL
 * Represents a single message in a conversation
 */
@Entity
@Table(name = "ai_messages", indexes = {
    @Index(name = "idx_ai_messages_conversation_id", columnList = "conversation_id"),
    @Index(name = "idx_ai_messages_role", columnList = "role"),
    @Index(name = "idx_ai_messages_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"conversation", "createdAt"})
@ToString(exclude = {"conversation"})
public class AiMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private AiConversation conversation;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AiMessageRole role;
    
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String content;
    
    @Column(name = "tokens_used")
    private Integer tokensUsed;
    
    @Column(name = "response_time_ms")
    private Long responseTimeMs;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;
}
