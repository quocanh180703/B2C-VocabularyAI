package com.vocabularyai.ai.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verification test to show what system prompts were loaded
 * Helps verify Phase 2 completion
 */
@SpringBootTest
public class SystemPromptConfigVerificationTest {
    
    @Autowired
    private SystemPromptConfig systemPromptConfig;
    
    /**
     * Print loaded prompts for verification
     */
    @Test
    public void verifyAllPromptsLoaded() {
        System.out.println("\n" +
                "========================================\n" +
                "SYSTEM PROMPTS VERIFICATION\n" +
                "========================================\n");
        
        // Vocabulary
        String vocabularyPrompt = systemPromptConfig.getVocabularySystemPrompt();
        System.out.println("✓ Vocabulary Prompt Loaded: " + 
            (vocabularyPrompt.length() > 0 ? "YES (" + vocabularyPrompt.length() + " chars)" : "NO"));
        assertTrue(vocabularyPrompt.length() > 100, "Vocabulary prompt should be loaded");
        
        // Grammar
        String grammarPrompt = systemPromptConfig.getGrammarSystemPrompt();
        System.out.println("✓ Grammar Prompt Loaded: " + 
            (grammarPrompt.length() > 0 ? "YES (" + grammarPrompt.length() + " chars)" : "NO"));
        assertTrue(grammarPrompt.length() > 100, "Grammar prompt should be loaded");
        
        // Hint
        String hintPrompt = systemPromptConfig.getHintSystemPrompt();
        System.out.println("✓ Hint Prompt Loaded: " + 
            (hintPrompt.length() > 0 ? "YES (" + hintPrompt.length() + " chars)" : "NO"));
        assertTrue(hintPrompt.length() > 100, "Hint prompt should be loaded");
        
        // Chat
        String chatPrompt = systemPromptConfig.getChatSystemPrompt();
        System.out.println("✓ Chat Prompt Loaded: " + 
            (chatPrompt.length() > 0 ? "YES (" + chatPrompt.length() + " chars)" : "NO"));
        assertTrue(chatPrompt.length() > 100, "Chat prompt should be loaded");
        
        // Speaking - Analysis
        String speakingAnalysisPrompt = systemPromptConfig.getSpeakingAnalysisSystemPrompt();
        System.out.println("✓ Speaking Analysis Prompt Loaded: " + 
            (speakingAnalysisPrompt.length() > 0 ? "YES (" + speakingAnalysisPrompt.length() + " chars)" : "NO"));
        assertTrue(speakingAnalysisPrompt.length() > 100, "Speaking analysis prompt should be loaded");
        
        // Speaking - Feedback
        String speakingFeedbackPrompt = systemPromptConfig.getSpeakingFeedbackSystemPrompt();
        System.out.println("✓ Speaking Feedback Prompt Loaded: " + 
            (speakingFeedbackPrompt.length() > 0 ? "YES (" + speakingFeedbackPrompt.length() + " chars)" : "NO"));
        assertTrue(speakingFeedbackPrompt.length() > 100, "Speaking feedback prompt should be loaded");
        
        // Speaking - Correction
        String speakingCorrectionPrompt = systemPromptConfig.getSpeakingCorrectionSystemPrompt();
        System.out.println("✓ Speaking Correction Prompt Loaded: " + 
            (speakingCorrectionPrompt.length() > 0 ? "YES (" + speakingCorrectionPrompt.length() + " chars)" : "NO"));
        assertTrue(speakingCorrectionPrompt.length() > 100, "Speaking correction prompt should be loaded");
        
        // Summary
        System.out.println("\n" +
                "========================================\n" +
                "TOTAL PROMPTS LOADED: 7\n" +
                "TOTAL CHARACTERS: " + 
                (vocabularyPrompt.length() + grammarPrompt.length() + hintPrompt.length() + 
                 chatPrompt.length() + speakingAnalysisPrompt.length() + 
                 speakingFeedbackPrompt.length() + speakingCorrectionPrompt.length()) +
                "\n" +
                "========================================\n");
        
        // Verify each prompt contains expected content
        assertTrue(vocabularyPrompt.contains("vocabulary") || vocabularyPrompt.contains("English"), 
            "Vocabulary prompt should have proper content");
        assertTrue(grammarPrompt.contains("grammar") || grammarPrompt.contains("correction"), 
            "Grammar prompt should have proper content");
        assertTrue(hintPrompt.contains("hint") || hintPrompt.contains("learning"), 
            "Hint prompt should have proper content");
        assertTrue(chatPrompt.contains("assistant") || chatPrompt.contains("English"), 
            "Chat prompt should have proper content");
        assertTrue(speakingAnalysisPrompt.contains("analysis") || speakingAnalysisPrompt.contains("score"), 
            "Speaking analysis prompt should have proper content");
        assertTrue(speakingFeedbackPrompt.contains("feedback") || speakingFeedbackPrompt.contains("teaching"), 
            "Speaking feedback prompt should have proper content");
        assertTrue(speakingCorrectionPrompt.contains("correction") || speakingCorrectionPrompt.contains("error"), 
            "Speaking correction prompt should have proper content");
    }
}
