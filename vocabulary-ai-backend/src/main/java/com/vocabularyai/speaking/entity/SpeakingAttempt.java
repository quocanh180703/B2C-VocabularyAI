package com.vocabularyai.speaking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * SpeakingAttempt Entity
 * 
 * Lưu thông tin về một lần ghi âm trong phiên luyện nói
 * - Một phiên có thể có nhiều lần ghi âm
 * - Mỗi lần ghi âm có một file audio, transcript và feedback riêng
 * 
 * Quan hệ:
 * - SpeakingSession 1-N SpeakingAttempt
 * - SpeakingAttempt 1-1 SpeakingFeedback
 */
@Entity
@Table(name = "speaking_attempts", indexes = {
    @Index(name = "idx_speaking_attempts_session_id", columnList = "session_id"),
    @Index(name = "idx_speaking_attempts_attempt_number", columnList = "session_id, attempt_number"),
    @Index(name = "idx_speaking_attempts_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"session", "feedback", "createdAt"})
public class SpeakingAttempt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Phiên luyện nói này thuộc về
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private SpeakingSession session;
    
    /**
     * URL của file audio đã lưu
     * Có thể là:
     * - Đường dẫn local: /storage/audio/session-123/attempt-1.wav
     * - URL Cloudinary: https://res.cloudinary.com/...
     * - S3 URL: https://bucket.s3.amazonaws.com/...
     */
    @Column(name = "audio_url", nullable = false, length = 500)
    private String audioUrl;
    
    /**
     * Độ dài file audio (tính bằng giây)
     */
    @Column(name = "audio_duration_seconds")
    private Integer audioDurationSeconds;
    
    /**
     * Transcript - Nội dung lời nói được chuyển thành text
     * (kết quả từ Speech-to-Text service)
     */
    @Column(name = "transcript", columnDefinition = "LONGTEXT")
    private String transcript;
    
    /**
     * Độ tin cậy của transcript (0-100)
     * Được trả về từ Speech-to-Text provider
     */
    @Column(name = "transcript_confidence", columnDefinition = "DECIMAL(5,2)")
    private Double transcriptConfidence;
    
    /**
     * Số thứ tự lần ghi âm trong phiên
     * Lần đầu = 1, lần thứ 2 = 2, v.v.
     */
    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber;
    
    /**
     * Feedback chi tiết cho lần ghi âm này
     */
    @OneToOne(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private SpeakingFeedback feedback;
    
    /**
     * Thời gian ghi âm
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
