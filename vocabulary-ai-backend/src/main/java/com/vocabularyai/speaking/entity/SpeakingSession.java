package com.vocabularyai.speaking.entity;

import com.vocabularyai.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * SpeakingSession Entity
 * 
 * Lưu thông tin về một phiên luyện nói
 * - Mỗi phiên có thể chứa nhiều lần ghi âm (attempts)
 * - Mỗi attempt có một feedback riêng
 * - User chỉ có thể xem/edit session của chính mình
 * 
 * Quan hệ:
 * - User 1-N SpeakingSession (cascade delete)
 * - SpeakingSession 1-N SpeakingAttempt (cascade delete)
 * - SpeakingSession 1-1 SpeakingFeedback (cascade delete)
 */
@Entity
@Table(name = "speaking_sessions", indexes = {
    @Index(name = "idx_speaking_sessions_user_id", columnList = "user_id"),
    @Index(name = "idx_speaking_sessions_status", columnList = "status"),
    @Index(name = "idx_speaking_sessions_level", columnList = "level"),
    @Index(name = "idx_speaking_sessions_created_at", columnList = "created_at"),
    @Index(name = "idx_speaking_sessions_user_created", columnList = "user_id, created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"user", "attempts", "feedback", "createdAt", "completedAt"})
public class SpeakingSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Người dùng sở hữu phiên này
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /**
     * Chủ đề luyện nói
     * Ví dụ: "Tự giới thiệu", "Mô tả gia đình", "Thảo luận về công việc"
     */
    @Column(nullable = false, length = 255)
    private String topic;
    
    /**
     * Mức độ khó (A1-C2)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SpeakingLevel level;
    
    /**
     * Mô tả/hướng dẫn cho phiên luyện tập
     * (nếu có, ví dụ: "Mô tả một ngày thông thường của bạn")
     */
    @Column(columnDefinition = "LONGTEXT")
    private String description;
    
    /**
     * Trạng thái phiên
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SpeakingStatus status = SpeakingStatus.IN_PROGRESS;
    
    /**
     * Thời gian bắt đầu phiên
     */
    @Column(name = "started_at", nullable = false)
    @Builder.Default
    private LocalDateTime startedAt = LocalDateTime.now();
    
    /**
     * Thời gian hoàn thành phiên
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    /**
     * Điểm tổng thể của phiên (được tính từ attempts)
     * Giá trị: 0-100
     */
    @Column(name = "overall_score", columnDefinition = "DECIMAL(5,2)")
    private Double overallScore;
    
    /**
     * Danh sách các lần ghi âm trong phiên này
     */
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SpeakingAttempt> attempts = new ArrayList<>();
    
    /**
     * Feedback tổng hợp cho cả phiên
     */
    @OneToOne(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private SpeakingFeedback feedback;
    
    /**
     * Thời gian tạo
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    /**
     * Thời gian cập nhật lần cuối
     */
    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Hoàn thành phiên
     */
    public void complete() {
        this.status = SpeakingStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
    
    /**
     * Hủy phiên
     */
    public void cancel() {
        this.status = SpeakingStatus.CANCELLED;
        this.completedAt = LocalDateTime.now();
    }
    
    /**
     * Lấy số lần ghi âm
     */
    public int getAttemptCount() {
        return attempts != null ? attempts.size() : 0;
    }
}
