package com.vocabularyai.speaking.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Speaking Module Configuration Properties
 * 
 * Loads configuration from application.yml under 'speaking' section
 * Centralizes all Speaking-related settings
 */
@Data
@Component
@ConfigurationProperties(prefix = "speaking")
public class SpeakingProperties {
    
    private Audio audio = new Audio();
    private Storage storage = new Storage();
    private SpeechToText speechToText = new SpeechToText();
    private TextToSpeech textToSpeech = new TextToSpeech();
    private AiAnalysis aiAnalysis = new AiAnalysis();
    
    /**
     * Audio Upload Configuration
     */
    @Data
    public static class Audio {
        // 50 MB default
        private long maxFileSize = 52428800L;
        // 10 minutes default
        private int maxDurationSeconds = 600;
        // Supported formats
        private String supportedFormats = "WAV,MP3,M4A,WEBM,OGG,FLAC";
        // Supported MIME types
        private String supportedMimeTypes = "audio/wav,audio/mpeg,audio/mp4,audio/webm,audio/ogg,audio/flac";
        
        /**
         * Get supported formats as list
         */
        public List<String> getSupportedFormatsList() {
            return Arrays.asList(supportedFormats.split(","));
        }
        
        /**
         * Get supported MIME types as list
         */
        public List<String> getSupportedMimeTypesList() {
            return Arrays.asList(supportedMimeTypes.split(","));
        }
    }
    
    /**
     * Audio Storage Configuration
     */
    @Data
    public static class Storage {
        // Storage type: local, cloudinary, s3
        private String type = "local";
        // Local storage path
        private String localPath = "/tmp/vocabulary-ai/audio";
        // Cloudinary URL
        private String cloudinaryUrl;
        // AWS S3 bucket
        private String s3Bucket = "vocabulary-ai-audio";
        // AWS S3 region
        private String s3Region = "us-east-1";
    }
    
    /**
     * Speech-to-Text Provider Configuration
     */
    @Data
    public static class SpeechToText {
        // Provider: google_rest, azure, or custom
        // For MVP, we'll use Google Cloud Speech-to-Text REST API via GeminiAI
        private String provider = "google_rest";
        // Language code for STT
        private String languageCode = "en-US";
        // Speech-to-Text API key (if different from Gemini)
        private String apiKey;
    }
    
    /**
     * Text-to-Speech Provider Configuration
     */
    @Data
    public static class TextToSpeech {
        // Provider: google_rest, azure, or custom
        private String provider = "google_rest";
        // Language code
        private String languageCode = "en-US";
        // Voice name (e.g., en-US-Neural2-C)
        private String voiceName = "en-US-Neural2-C";
        // Text-to-Speech API key (if different from Gemini)
        private String apiKey;
    }
    
    /**
     * AI Analysis Configuration
     */
    @Data
    public static class AiAnalysis {
        // Score weights (must sum to 1.0)
        private double grammarWeight = 0.25;
        private double vocabularyWeight = 0.25;
        private double fluencyWeight = 0.25;
        private double contentWeight = 0.25;
        private double pronunciationWeight = 0.0;
        // Minimum confidence score to accept result (0-100)
        private int minConfidenceScore = 70;
        
        /**
         * Calculate overall score from individual scores
         */
        public double calculateOverallScore(
                double grammarScore, double vocabularyScore, 
                double fluencyScore, double contentScore, 
                double pronunciationScore) {
            double total = 
                (grammarScore * grammarWeight) +
                (vocabularyScore * vocabularyWeight) +
                (fluencyScore * fluencyWeight) +
                (contentScore * contentWeight) +
                (pronunciationScore * pronunciationWeight);
            
            return Math.round(total * 100.0) / 100.0;
        }
    }
}
