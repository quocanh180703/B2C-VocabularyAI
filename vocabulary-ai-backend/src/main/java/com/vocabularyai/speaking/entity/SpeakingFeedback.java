package com.vocabularyai.speaking.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * SpeakingFeedback Entity
 * 
 * Lưu đánh giá chi tiết về một lần ghi âm hoặc cả phiên
 * - Chứa các điểm số (grammar, vocabulary, fluency, content, pronunciation)
 * - Chứa feedback text, corrections và suggestions
 * 
 * Quan hệ:
 * - SpeakingAttempt 1-1 SpeakingFeedback (attempt level feedback)
 * - SpeakingSession 1-1 SpeakingFeedback (session level feedback)
 */
@Entity
@Table(name = "speaking_feedback", indexes = {
    @Index(name = "idx_speaking_feedback_attempt_id", columnList = "attempt_id"),
    @Index(name = "idx_speaking_feedback_session_id", columnList = "session_id"),
    @Index(name = "idx_speaking_feedback_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"attempt", "session", "createdAt"})
public class SpeakingFeedback {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Feedback cho một lần ghi âm cụ thể
     * Nullable vì có thể là session-level feedback
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", unique = true)
    @JsonIgnore
    private SpeakingAttempt attempt;
    
    /**
     * Feedback cho cả phiên luyện tập
     * Nullable vì có thể là attempt-level feedback
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", unique = true)
    @JsonIgnore
    private SpeakingSession session;
    
    /**
     * Điểm tổng thể (0-100)
     */
    @Column(name = "overall_score", columnDefinition = "DECIMAL(5,2)")
    private Double overallScore;
    
    /**
     * Điểm ngữ pháp (0-100)
     * Đánh giá: Sử dụng đúng thì hiện tại, cấu trúc câu, v.v.
     */
    @Column(name = "grammar_score", columnDefinition = "DECIMAL(5,2)")
    private Double grammarScore;
    
    /**
     * Điểm từ vựng (0-100)
     * Đánh giá: Phạm vi từ vựng, sử dụng từ thích hợp
     */
    @Column(name = "vocabulary_score", columnDefinition = "DECIMAL(5,2)")
    private Double vocabularyScore;
    
    /**
     * Điểm trôi chảy (0-100)
     * Đánh giá: Tốc độ nói, liên kết giữa các câu, không bị dừng quá nhiều
     */
    @Column(name = "fluency_score", columnDefinition = "DECIMAL(5,2)")
    private Double fluencyScore;
    
    /**
     * Điểm nội dung (0-100)
     * Đánh giá: Nội dung phù hợp chủ đề, logic, đầy đủ thông tin
     */
    @Column(name = "content_score", columnDefinition = "DECIMAL(5,2)")
    private Double contentScore;
    
    /**
     * Điểm phát âm (0-100)
     * Đánh giá: Phát âm từng từ, trọng âm, ngữ điệu
     * Optional - chỉ có nếu provider hỗ trợ
     */
    @Column(name = "pronunciation_score", columnDefinition = "DECIMAL(5,2)")
    private Double pronunciationScore;
    
    /**
     * Feedback text tổng quát
     * Ví dụ: "Bạn nói rất tốt! Hãy thực hành thêm về sử dụng thì quá khứ."
     */
    @Column(name = "feedback", columnDefinition = "LONGTEXT")
    private String feedback;
    
    /**
     * Các lỗi được sửa lại
     * JSON format: [{"original": "I are", "corrected": "I am", "reason": "Sai động từ"}]
     */
    @Column(name = "corrections", columnDefinition = "JSON")
    private String corrections;
    
    /**
     * Các gợi ý cải thiện
     * JSON format: [{"area": "grammar", "suggestion": "Hãy luyện tập thì hiện tại hoàn thành"}]
     */
    @Column(name = "suggestions", columnDefinition = "JSON")
    private String suggestions;
    
    /**
     * Điểm mạnh của bài nói
     * JSON format: [{"strength": "Phát âm tốt"}, {"strength": "Nói rõ ràng"}]
     */
    @Column(name = "strengths", columnDefinition = "JSON")
    private String strengths;
    
    /**
     * Các điểm yếu cần cải thiện
     * JSON format: [{"weakness": "Lỗi thì hiện tại"}, {"weakness": "Từ vựng hạn chế"}]
     */
    @Column(name = "weaknesses", columnDefinition = "JSON")
    private String weaknesses;
    
    /**
     * Thời gian tạo feedback
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
     * Check xem có phải attempt-level feedback hay không
     */
    public boolean isAttemptFeedback() {
        return attempt != null && session == null;
    }
    
    /**
     * Check xem có phải session-level feedback hay không
     */
    public boolean isSessionFeedback() {
        return session != null && attempt == null;
    }
    
    /**
     * Tính điểm tổng thể từ các điểm riêng
     * Sử dụng trọng số từ SpeakingProperties
     */
    public void calculateOverallScore(
            double grammarWeight, double vocabularyWeight,
            double fluencyWeight, double contentWeight,
            double pronunciationWeight) {
        
        double total = 0;
        int count = 0;
        
        if (grammarScore != null) {
            total += grammarScore * grammarWeight;
            count++;
        }
        if (vocabularyScore != null) {
            total += vocabularyScore * vocabularyWeight;
            count++;
        }
        if (fluencyScore != null) {
            total += fluencyScore * fluencyWeight;
            count++;
        }
        if (contentScore != null) {
            total += contentScore * contentWeight;
            count++;
        }
        if (pronunciationScore != null && pronunciationWeight > 0) {
            total += pronunciationScore * pronunciationWeight;
            count++;
        }
        
        if (count > 0) {
            this.overallScore = Math.round(total * 100.0) / 100.0;
        }
    }
}
