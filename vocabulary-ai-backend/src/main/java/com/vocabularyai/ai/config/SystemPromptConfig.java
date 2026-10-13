package com.vocabularyai.ai.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for AI system prompts
 * 
 * Loads system prompts for different AI features from application.yml
 * Structure: ai.prompts.system.[feature].[language]
 * 
 * Example in application.yml:
 * ai:
 *   prompts:
 *     system:
 *       vocabulary:
 *         en: |
 *           You are an English vocabulary tutor...
 *       grammar:
 *         en: |
 *           You are an English grammar correction tutor...
 */
@Component
@ConfigurationProperties(prefix = "ai.prompts.system")
@Getter
@Setter
@Slf4j
public class SystemPromptConfig {
    
    /**
     * Vocabulary explanation system prompts
     * Default: English (en)
     */
    private Map<String, String> vocabulary = new HashMap<>();
    
    /**
     * Grammar checking system prompts
     * Default: English (en)
     */
    private Map<String, String> grammar = new HashMap<>();
    
    /**
     * Learning hint system prompts
     * Default: English (en)
     */
    private Map<String, String> hint = new HashMap<>();
    
    /**
     * Chat conversation system prompts
     * Default: English (en)
     */
    private Map<String, String> chat = new HashMap<>();
    
    /**
     * Speaking evaluation system prompts
     * Keys: analysis, feedback, correction
     */
    private Map<String, String> speaking = new HashMap<>();
    
    /**
     * Get vocabulary system prompt
     * Default language: English
     * 
     * @return System prompt for vocabulary explanation, or empty string if not found
     */
    public String getVocabularySystemPrompt() {
        return vocabulary != null ? vocabulary.getOrDefault("en", "") : "";
    }
    
    /**
     * Get grammar system prompt
     * Default language: English
     * 
     * @return System prompt for grammar checking, or empty string if not found
     */
    public String getGrammarSystemPrompt() {
        return grammar != null ? grammar.getOrDefault("en", "") : "";
    }
    
    /**
     * Get hint system prompt
     * Default language: English
     * 
     * @return System prompt for learning hints, or empty string if not found
     */
    public String getHintSystemPrompt() {
        return hint != null ? hint.getOrDefault("en", "") : "";
    }
    
    /**
     * Get chat system prompt
     * Default language: English
     * 
     * @return System prompt for chat conversations, or empty string if not found
     */
    public String getChatSystemPrompt() {
        return chat != null ? chat.getOrDefault("en", "") : "";
    }
    
    /**
     * Get speaking analysis system prompt
     * 
     * @return System prompt for speaking analysis, or empty string if not found
     */
    public String getSpeakingAnalysisSystemPrompt() {
        return speaking != null ? speaking.getOrDefault("analysis", "") : "";
    }
    
    /**
     * Get speaking feedback system prompt
     * 
     * @return System prompt for speaking feedback, or empty string if not found
     */
    public String getSpeakingFeedbackSystemPrompt() {
        return speaking != null ? speaking.getOrDefault("feedback", "") : "";
    }
    
    /**
     * Get speaking correction system prompt
     * 
     * @return System prompt for speaking correction, or empty string if not found
     */
    public String getSpeakingCorrectionSystemPrompt() {
        return speaking != null ? speaking.getOrDefault("correction", "") : "";
    }
    
    /**
     * Initialize - called by Spring after properties are set
     * Logs loaded configuration for debugging
     */
    @Override
    public String toString() {
        return "SystemPromptConfig{" +
                "vocabulary={" + (vocabulary != null ? vocabulary.size() : 0) + " languages}, " +
                "grammar={" + (grammar != null ? grammar.size() : 0) + " languages}, " +
                "hint={" + (hint != null ? hint.size() : 0) + " languages}, " +
                "chat={" + (chat != null ? chat.size() : 0) + " languages}, " +
                "speaking={" + (speaking != null ? speaking.size() : 0) + " prompts}" +
                '}';
    }
}
