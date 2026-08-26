package com.vocabularyai.speaking.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vocabularyai.speaking.entity.SpeakingLevel;
import com.vocabularyai.speaking.entity.SpeakingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * SpeakingPracticeResponse
 * 
 * Response DTO cho SpeakingPracticeService
 * 
 * Chứa:
 * - Session info (topic, level, status)
 * - Attempt info (audio, transcript, duration)
 * - AI Analysis scores
 * - Detailed feedback
 * - Session statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeakingPracticeResponse {
    
    /**
     * Session information
     */
    @JsonProperty("session")
    private SessionInfo session;
    
    /**
     * Current attempt information
     */
    @JsonProperty("attempt")
    private AttemptInfo attempt;
    
    /**
     * Analysis scores từ AI
     */
    @JsonProperty("scores")
    private ScoresInfo scores;
    
    /**
     * Detailed feedback từ AI
     */
    @JsonProperty("feedback")
    private FeedbackInfo feedback;
    
    /**
     * Pronunciation analysis result (nếu provider hỗ trợ)
     */
    @JsonProperty("pronunciation")
    private PronunciationInfo pronunciation;
    
    /**
     * Session statistics
     */
    @JsonProperty("statistics")
    private StatisticsInfo statistics;
    
    /**
     * Session Info
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionInfo {
        @JsonProperty("id")
        private Long id;
        
        @JsonProperty("topic")
        private String topic;
        
        @JsonProperty("level")
        private SpeakingLevel level;
        
        @JsonProperty("description")
        private String description;
        
        @JsonProperty("status")
        private SpeakingStatus status;
        
        @JsonProperty("startedAt")
        private LocalDateTime startedAt;
        
        @JsonProperty("overallScore")
        private Double overallScore;
        
        @JsonProperty("attemptCount")
        private Integer attemptCount;
    }
    
    /**
     * Attempt Info
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttemptInfo {
        @JsonProperty("id")
        private Long id;
        
        @JsonProperty("attemptNumber")
        private Integer attemptNumber;
        
        @JsonProperty("audioUrl")
        private String audioUrl;
        
        @JsonProperty("audioDurationSeconds")
        private Integer audioDurationSeconds;
        
        @JsonProperty("transcript")
        private String transcript;
        
        @JsonProperty("transcriptConfidence")
        private Double transcriptConfidence;
        
        @JsonProperty("createdAt")
        private LocalDateTime createdAt;
    }
    
    /**
     * Scores Info
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScoresInfo {
        @JsonProperty("overallScore")
        private Double overallScore;
        
        @JsonProperty("grammarScore")
        private Double grammarScore;
        
        @JsonProperty("vocabularyScore")
        private Double vocabularyScore;
        
        @JsonProperty("fluencyScore")
        private Double fluencyScore;
        
        @JsonProperty("contentScore")
        private Double contentScore;
        
        @JsonProperty("pronunciationScore")
        private Double pronunciationScore;
    }
    
    /**
     * Feedback Info
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeedbackInfo {
        @JsonProperty("feedback")
        private String feedback;
        
        @JsonProperty("corrections")
        private List<CorrectionInfo> corrections;
        
        @JsonProperty("suggestions")
        private List<String> suggestions;
        
        @JsonProperty("strengths")
        private List<String> strengths;
        
        @JsonProperty("weaknesses")
        private List<String> weaknesses;
    }
    
    /**
     * Correction Info
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CorrectionInfo {
        @JsonProperty("original")
        private String original;
        
        @JsonProperty("corrected")
        private String corrected;
        
        @JsonProperty("errorType")
        private String errorType;
        
        @JsonProperty("explanation")
        private String explanation;
    }
    
    /**
     * Pronunciation Info (optional)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PronunciationInfo {
        @JsonProperty("pronunciationScore")
        private Double pronunciationScore;
        
        @JsonProperty("feedback")
        private String feedback;
        
        @JsonProperty("stressFeedback")
        private String stressFeedback;
        
        @JsonProperty("intonationFeedback")
        private String intonationFeedback;
        
        @JsonProperty("provider")
        private String provider;
    }
    
    /**
     * Statistics Info - Thống kê của phiên
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatisticsInfo {
        @JsonProperty("totalAttempts")
        private Integer totalAttempts;
        
        @JsonProperty("averageScore")
        private Double averageScore;
        
        @JsonProperty("bestScore")
        private Double bestScore;
        
        @JsonProperty("averageGrammarScore")
        private Double averageGrammarScore;
        
        @JsonProperty("averageVocabularyScore")
        private Double averageVocabularyScore;
        
        @JsonProperty("averageFluencyScore")
        private Double averageFluencyScore;
        
        @JsonProperty("averageContentScore")
        private Double averageContentScore;
        
        @JsonProperty("improvement")
        private String improvement;
    }
}
