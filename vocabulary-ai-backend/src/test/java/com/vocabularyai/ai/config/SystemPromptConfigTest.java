package com.vocabularyai.ai.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SystemPromptConfig
 * 
 * Verifies that system prompts are correctly loaded from application.yml
 */
@SpringBootTest
public class SystemPromptConfigTest {
    
    @Autowired
    private SystemPromptConfig systemPromptConfig;
    
    /**
     * Test that vocabulary system prompt is loaded and not empty
     */
    @Test
    public void testVocabularyPromptLoaded() {
        assertNotNull(systemPromptConfig, "SystemPromptConfig should be injected");
        
        String vocabularyPrompt = systemPromptConfig.getVocabularySystemPrompt();
        assertNotNull(vocabularyPrompt, "Vocabulary prompt should not be null");
        assertFalse(vocabularyPrompt.isEmpty(), "Vocabulary prompt should not be empty");
        assertTrue(vocabularyPrompt.contains("vocabulary"), "Vocabulary prompt should contain 'vocabulary'");
        
        // Check for key markers from expected prompt
        assertTrue(
            vocabularyPrompt.contains("English vocabulary tutor") || vocabularyPrompt.contains("tutor"),
            "Vocabulary prompt should contain role definition"
        );
    }
    
    /**
     * Test that grammar system prompt is loaded and not empty
     */
    @Test
    public void testGrammarPromptLoaded() {
        String grammarPrompt = systemPromptConfig.getGrammarSystemPrompt();
        assertNotNull(grammarPrompt, "Grammar prompt should not be null");
        assertFalse(grammarPrompt.isEmpty(), "Grammar prompt should not be empty");
        assertTrue(grammarPrompt.contains("grammar"), "Grammar prompt should contain 'grammar'");
    }
    
    /**
     * Test that hint system prompt is loaded and not empty
     */
    @Test
    public void testHintPromptLoaded() {
        String hintPrompt = systemPromptConfig.getHintSystemPrompt();
        assertNotNull(hintPrompt, "Hint prompt should not be null");
        assertFalse(hintPrompt.isEmpty(), "Hint prompt should not be empty");
        assertTrue(
            hintPrompt.contains("hint") || hintPrompt.contains("tutor"),
            "Hint prompt should contain 'hint' or reference to tutor role"
        );
    }
    
    /**
     * Test that chat system prompt is loaded and not empty
     */
    @Test
    public void testChatPromptLoaded() {
        String chatPrompt = systemPromptConfig.getChatSystemPrompt();
        assertNotNull(chatPrompt, "Chat prompt should not be null");
        assertFalse(chatPrompt.isEmpty(), "Chat prompt should not be empty");
        assertTrue(
            chatPrompt.contains("assistant") || chatPrompt.contains("learning"),
            "Chat prompt should contain role or learning reference"
        );
    }
    
    /**
     * Test that speaking analysis system prompt is loaded and not empty
     */
    @Test
    public void testSpeakingAnalysisPromptLoaded() {
        String speakingAnalysisPrompt = systemPromptConfig.getSpeakingAnalysisSystemPrompt();
        assertNotNull(speakingAnalysisPrompt, "Speaking analysis prompt should not be null");
        assertFalse(speakingAnalysisPrompt.isEmpty(), "Speaking analysis prompt should not be empty");
    }
    
    /**
     * Test that speaking feedback system prompt is loaded and not empty
     */
    @Test
    public void testSpeakingFeedbackPromptLoaded() {
        String speakingFeedbackPrompt = systemPromptConfig.getSpeakingFeedbackSystemPrompt();
        assertNotNull(speakingFeedbackPrompt, "Speaking feedback prompt should not be null");
        assertFalse(speakingFeedbackPrompt.isEmpty(), "Speaking feedback prompt should not be empty");
    }
    
    /**
     * Test that speaking correction system prompt is loaded and not empty
     */
    @Test
    public void testSpeakingCorrectionPromptLoaded() {
        String speakingCorrectionPrompt = systemPromptConfig.getSpeakingCorrectionSystemPrompt();
        assertNotNull(speakingCorrectionPrompt, "Speaking correction prompt should not be null");
        assertFalse(speakingCorrectionPrompt.isEmpty(), "Speaking correction prompt should not be empty");
    }
    
    /**
     * Test that all required prompts are available
     */
    @Test
    public void testAllPromptsAvailable() {
        assertNotNull(systemPromptConfig.getVocabularySystemPrompt());
        assertNotNull(systemPromptConfig.getGrammarSystemPrompt());
        assertNotNull(systemPromptConfig.getHintSystemPrompt());
        assertNotNull(systemPromptConfig.getChatSystemPrompt());
        assertNotNull(systemPromptConfig.getSpeakingAnalysisSystemPrompt());
        assertNotNull(systemPromptConfig.getSpeakingFeedbackSystemPrompt());
        assertNotNull(systemPromptConfig.getSpeakingCorrectionSystemPrompt());
    }
    
    /**
     * Test toString method for debugging
     */
    @Test
    public void testToString() {
        String configStr = systemPromptConfig.toString();
        assertNotNull(configStr, "toString should return a string");
        assertTrue(configStr.contains("SystemPromptConfig"), "toString should contain class name");
        assertTrue(configStr.contains("vocabulary"), "toString should reference vocabulary");
        assertTrue(configStr.contains("grammar"), "toString should reference grammar");
    }
}
