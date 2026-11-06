package com.vocabularyai.report.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "content_reports", indexes = {
    @Index(name = "idx_content_reports_user_id", columnList = "user_id"),
    @Index(name = "idx_content_reports_status", columnList = "status"),
    @Index(name = "idx_content_reports_cefr_word_id", columnList = "cefr_word_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "cefr_word_id", nullable = false, length = 100)
    private String cefrWordId;

    @Column(name = "lemma_snapshot", nullable = false, length = 255)
    private String lemmaSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private ContentReportType type;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ContentReportStatus status = ContentReportStatus.PENDING;

    @Column(name = "admin_note", length = 500)
    private String adminNote;

    @Column(name = "resolved_by")
    private Long resolvedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}
