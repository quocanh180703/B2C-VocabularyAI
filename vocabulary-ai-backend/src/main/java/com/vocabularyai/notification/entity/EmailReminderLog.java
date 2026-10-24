package com.vocabularyai.notification.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity mapping to 'email_reminder_log' table.
 * Tracks sent and failed reminder emails with unique constraints to ensure idempotency.
 */
@Entity
@Table(name = "email_reminder_log", indexes = {
    @Index(name = "idx_reminder_date", columnList = "sent_date"),
    @Index(name = "idx_reminder_user_date", columnList = "user_id, sent_date"),
    @Index(name = "idx_reminder_status", columnList = "status")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_type_date", columnNames = {"user_id", "type", "sent_date"}),
    @UniqueConstraint(name = "uk_subscription_type", columnNames = {"subscription_id", "type"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailReminderLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ReminderType type;

    @Column(name = "subscription_id")
    private Long subscriptionId;

    @Column(name = "sent_date", nullable = false)
    private LocalDate sentDate;

    @Column(name = "status", nullable = false, length = 20)
    private String status; // SENT, FAILED

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
