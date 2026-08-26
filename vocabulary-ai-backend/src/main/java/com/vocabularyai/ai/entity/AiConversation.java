package com.vocabularyai.ai.entity;

import com.vocabularyai.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * AiConversation Entity
 * 
 * Maps to 'ai_conversations' table in MySQL
 * Represents a conversation session between user and AI
 */
@Entity
@Table(name = "ai_conversations", indexes = {
    @Index(name = "idx_ai_conversations_user_id", columnList = "user_id"),
    @Index(name = "idx_ai_conversations_type", columnList = "type"),
    @Index(name = "idx_ai_conversations_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"user", "messages", "createdAt", "updatedAt"})
@ToString(exclude = {"messages"})
public class AiConversation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AiConversationType type;
    
    @Column(length = 255)
    private String title;
    
    @Column(columnDefinition = "LONGTEXT")
    private String summary;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @Column(name = "is_archived", nullable = false)
    @Builder.Default
    private Boolean isArchived = false;
    
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AiMessage> messages = new ArrayList<>();
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Add message to conversation
     */
    public void addMessage(AiMessage message) {
        messages.add(message);
        message.setConversation(this);
    }
    
    /**
     * Remove message from conversation
     */
    public void removeMessage(AiMessage message) {
        messages.remove(message);
        message.setConversation(null);
    }
    
    /**
     * Get message count
     */
    public int getMessageCount() {
        return messages != null ? messages.size() : 0;
    }
}
