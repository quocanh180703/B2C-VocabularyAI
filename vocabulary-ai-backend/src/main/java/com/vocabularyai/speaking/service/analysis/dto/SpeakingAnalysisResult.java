package com.vocabularyai.speaking.service.analysis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * SpeakingAnalysisResult
 * 
 * Result DTO containing all AI analysis data for a speaking attempt
 * 
 * Combines:
 * - Analysis scores (grammar, vocabulary, fluency, content, overall)
 * - Detailed feedback
 * - Corrections with explanations
 * - Suggestions for improvement
 * - Strengths identified
 * - Weaknesses to focus on
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeakingAnalysisResult {
    
    /**
     * Analysis scores breakdown (0-100)
     */
    @JsonProperty("analysisScores")
    private AnalysisScores analysisScores;
    
    /**
     * Detailed feedback text
     * Ví dụ: "Good job on pronunciation! Try to use more complex sentence structures."
     */
    @JsonProperty("feedback")
    private String feedback;
    
    /**
     * List of corrections for errors found
     */
    @JsonProperty("corrections")
    private List<Correction> corrections;
    
    /**
     * List of suggestions for improvement
     */
    @JsonProperty("suggestions")
    private List<String> suggestions;
    
    /**
     * List of strengths identified
     */
    @JsonProperty("strengths")
    private List<String> strengths;
    
    /**
     * List of weaknesses to focus on
     */
    @JsonProperty("weaknesses")
    private List<String> weaknesses;
    
    /**
     * Confidence score of the analysis (0-100)
     * Based on clarity of transcript and amount of text analyzed
     */
    @JsonProperty("analysisConfidence")
    private Double analysisConfidence;
    
    /**
     * Analysis Scores Breakdown
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalysisScores {
        
        /**
         * Grammar score (0-100)
         * Evaluates: sentence structure, tense usage, subject-verb agreement
         */
        @JsonProperty("grammar")
        private Double grammar;
        
        /**
         * Vocabulary score (0-100)
         * Evaluates: word range, appropriateness, word choice accuracy
         */
        @JsonProperty("vocabulary")
        private Double vocabulary;
        
        /**
         * Fluency score (0-100)
         * Evaluates: smoothness, pace, natural rhythm
         */
        @JsonProperty("fluency")
        private Double fluency;
        
        /**
         * Content score (0-100)
         * Evaluates: relevance to topic, completeness, logical flow
         */
        @JsonProperty("content")
        private Double content;
        
        /**
         * Pronunciation score (0-100)
         * Optional - may be null if provider doesn't support
         */
        @JsonProperty("pronunciation")
        private Double pronunciation;
        
        /**
         * Overall score (0-100)
         * Weighted average: grammar*0.25 + vocabulary*0.25 + fluency*0.25 + content*0.25
         */
        @JsonProperty("overall")
        private Double overall;
    }
    
    /**
     * Correction Object
     * 
     * Represents an error and its correction
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Correction {
        
        /**
         * Original text with error
         * Ví dụ: "I are"
         */
        @JsonProperty("original")
        private String original;
        
        /**
         * Corrected text
         * Ví dụ: "I am"
         */
        @JsonProperty("corrected")
        private String corrected;
        
        /**
         * Type of error
         * Ví dụ: "grammar", "vocabulary", "spelling"
         */
        @JsonProperty("errorType")
        private String errorType;
        
        /**
         * Explanation of the error and correction
         */
        @JsonProperty("explanation")
        private String explanation;
        
        /**
         * Alternative expressions that could be used
         */
        @JsonProperty("alternatives")
        private List<String> alternatives;
    }
}
