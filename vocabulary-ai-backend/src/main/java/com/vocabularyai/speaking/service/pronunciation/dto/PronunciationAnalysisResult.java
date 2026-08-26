package com.vocabularyai.speaking.service.pronunciation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * PronunciationAnalysisResult
 * 
 * Kết quả phân tích phát âm từ PronunciationAnalysisService
 * 
 * Chứa:
 * - Pronunciation score tổng thể (0-100)
 * - Feedback về phát âm
 * - Word-level pronunciation issues (nếu provider hỗ trợ)
 * - Stress và intonation feedback
 * - Suggestions để cải thiện
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PronunciationAnalysisResult {
    
    /**
     * Pronunciation score tổng thể (0-100)
     * 
     * Dựa trên:
     * - Clarity và accuracy của từng từ
     * - Stress pattern (âm trọng)
     * - Intonation (ngữ điệu)
     * - Natural rhythm
     */
    @JsonProperty("pronunciationScore")
    private Double pronunciationScore;
    
    /**
     * Feedback văn bản về phát âm
     * 
     * Ví dụ: "Your pronunciation is clear. Pay attention to word stress in 'PREsent' vs 'preSENT'."
     */
    @JsonProperty("feedback")
    private String feedback;
    
    /**
     * Danh sách các vấn đề phát âm từng từ
     * 
     * Nếu provider hỗ trợ word-level analysis
     * Nếu không hỗ trợ, list này có thể rỗng
     */
    @JsonProperty("wordLevelIssues")
    private List<WordPronunciationIssue> wordLevelIssues;
    
    /**
     * Feedback về stress pattern (âm trọng)
     * 
     * Ví dụ: "Remember to stress the first syllable in 'comPUter', not 'comPYOOter'"
     */
    @JsonProperty("stressFeedback")
    private String stressFeedback;
    
    /**
     * Feedback về intonation (ngữ điệu)
     * 
     * Ví dụ: "Your intonation is good, but try to raise your voice at the end of questions"
     */
    @JsonProperty("intonationFeedback")
    private String intonationFeedback;
    
    /**
     * Danh sách suggestions để cải thiện phát âm
     */
    @JsonProperty("suggestions")
    private List<String> suggestions;
    
    /**
     * Confidence score của pronunciation analysis (0-100)
     * 
     * Dựa trên:
     * - Độ rõ ràng của audio
     * - Độ dài và chi tiết của transcript
     * - Khả năng của provider
     */
    @JsonProperty("analysisConfidence")
    private Double analysisConfidence;
    
    /**
     * Provider được sử dụng cho analysis này
     * 
     * Ví dụ: "gemini", "google-cloud", "azure"
     */
    @JsonProperty("provider")
    private String provider;
    
    /**
     * WordPronunciationIssue
     * 
     * Vấn đề phát âm của một từ cụ thể
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WordPronunciationIssue {
        
        /**
         * Từ được phát âm sai
         * 
         * Ví dụ: "comfortable"
         */
        @JsonProperty("word")
        private String word;
        
        /**
         * Vị trí từ trong transcript (từ thứ mấy)
         */
        @JsonProperty("position")
        private Integer position;
        
        /**
         * Cách phát âm hiện tại (IPA nếu provider hỗ trợ)
         * 
         * Ví dụ: "KUM-fer-tuh-bul"
         */
        @JsonProperty("currentPronunciation")
        private String currentPronunciation;
        
        /**
         * Cách phát âm đúng (IPA hoặc romanized)
         * 
         * Ví dụ: "KUM-for-tuh-bul" hoặc "KUM-fert-bul"
         */
        @JsonProperty("correctPronunciation")
        private String correctPronunciation;
        
        /**
         * Loại vấn đề
         * 
         * Ví dụ: "stress", "vowel", "consonant", "connected_speech"
         */
        @JsonProperty("issueType")
        private String issueType;
        
        /**
         * Giải thích chi tiết về vấn đề và cách sửa
         */
        @JsonProperty("explanation")
        private String explanation;
        
        /**
         * Tips để phát âm đúng
         */
        @JsonProperty("tips")
        private String tips;
    }
}
