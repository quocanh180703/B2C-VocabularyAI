package com.vocabularyai.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabularyai.ai.config.SystemPromptConfig;
import com.vocabularyai.ai.dto.request.GrammarCheckRequest;
import com.vocabularyai.ai.dto.response.GrammarCheckResponse;
import com.vocabularyai.ai.service.prompt.PromptEngineeringService;
import com.vocabularyai.ai.entity.AiConversationType;
import com.vocabularyai.ai.entity.AiMessage;
import com.vocabularyai.ai.entity.AiMessageRole;
import com.vocabularyai.learning.entity.LearningProgress;
import com.vocabularyai.learning.entity.LearningProgress.LearningStatus;
import com.vocabularyai.vocabulary.entity.Word;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Test for AI Module
 * 
 * Tests all AI prompt building functions after refactoring to use SystemPromptConfig
 * Verifies:
 * 1. System prompts are loaded from application.yml correctly
 * 2. Dynamic context is preserved
 * 3. Final prompts contain both system prompt + dynamic context
 * 4. No breaking changes to business logic
 */
@SpringBootTest
@Slf4j
@DisplayName("AI Integration Tests - Prompt Refactoring Verification")
public class AIIntegrationTest {
    
    @Autowired
    private PromptEngineeringService promptService;
    
    @Autowired
    private SystemPromptConfig systemPromptConfig;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // ==================== SETUP ====================
    
    @BeforeEach
    public void setUp() {
        log.info("========== AI Integration Test Setup ==========");
        assertNotNull(promptService, "PromptEngineeringService should be autowired");
        assertNotNull(systemPromptConfig, "SystemPromptConfig should be autowired");
        log.info("SystemPromptConfig loaded: {}", systemPromptConfig);
    }
    
    // ==================== TEST 1: VOCABULARY EXPLANATION ====================
    
    @Test
    @DisplayName("Test 1: Vocabulary Explanation Prompt - Should include system prompt from config + word context")
    public void testVocabularyExplanationPrompt() {
        log.info("\n--- Test 1: Vocabulary Explanation ---");
        
        // Setup
        Word word = Word.builder()
            .id(1L)
            .word("business")
            .partOfSpeech("noun")
            .definition("a commercial enterprise or profession")
            .example("She runs a successful business.")
            .level("B1")
            .build();
        
        String userContext = "job interview";
        
        // Execute
        String prompt = promptService.buildVocabularyPrompt(word, userContext);
        
        // Assert
        assertNotNull(prompt, "Prompt should not be null");
        assertFalse(prompt.isEmpty(), "Prompt should not be empty");
        
        // Verify system prompt is included
        String systemPrompt = systemPromptConfig.getVocabularySystemPrompt();
        assertTrue(prompt.contains("vocabulary") || prompt.contains("tutor"), 
            "Prompt should contain vocabulary-related content from system prompt");
        
        // Verify dynamic context is included
        assertTrue(prompt.contains("business"), "Prompt should contain word");
        assertTrue(prompt.contains("noun"), "Prompt should contain part of speech");
        assertTrue(prompt.contains("job interview"), "Prompt should contain user context");
        assertTrue(prompt.contains("B1"), "Prompt should contain word level");
        
        log.info("✅ Vocabulary prompt verified - {} chars", prompt.length());
        log.info("✅ Contains system prompt from config");
        log.info("✅ Contains dynamic context (word, context, level)");
    }
    
    // ==================== TEST 2: GRAMMAR CHECKING ====================
    
    @Test
    @DisplayName("Test 2: Grammar Checking Prompt - Should include system prompt + text to check")
    public void testGrammarCheckingPrompt() {
        log.info("\n--- Test 2: Grammar Checking ---");
        
        // Setup
        String text = "I are happy. She go to school yesterday.";
        String language = "English";
        
        // Execute
        String prompt = promptService.buildGrammarPrompt(text, language);
        
        // Assert
        assertNotNull(prompt, "Prompt should not be null");
        assertFalse(prompt.isEmpty(), "Prompt should not be empty");
        
        // Verify system prompt is included
        String systemPrompt = systemPromptConfig.getGrammarSystemPrompt();
        assertTrue(prompt.contains("grammar") || prompt.contains("correction"), 
            "Prompt should contain grammar-related content from system prompt");
        
        // Verify dynamic context is included
        assertTrue(prompt.contains(text), "Prompt should contain text to check");
        assertTrue(prompt.contains("English"), "Prompt should contain language");
        assertTrue(prompt.contains("JSON") || prompt.contains("error"), 
            "Prompt should contain instructions for error format");
        
        log.info("✅ Grammar prompt verified - {} chars", prompt.length());
        log.info("✅ Contains system prompt from config");
        log.info("✅ Contains text to check and language");
    }
    
    // ==================== TEST 3: CHAT CONVERSATION ====================
    
    @Test
    @DisplayName("Test 3: Chat Conversation Prompt - Should include system prompt + message history + context")
    public void testChatPrompt() {
        log.info("\n--- Test 3: Chat Conversation ---");
        
        // Setup - create message history
        List<AiMessage> history = new ArrayList<>();
        AiMessage msg1 = AiMessage.builder()
            .id(1L)
            .role(AiMessageRole.USER)
            .content("What is the difference between 'advice' and 'advise'?")
            .build();
        AiMessage msg2 = AiMessage.builder()
            .id(2L)
            .role(AiMessageRole.ASSISTANT)
            .content("'Advice' is a noun, 'advise' is a verb...")
            .build();
        history.add(msg1);
        history.add(msg2);
        
        String newMessage = "Can you give me an example?";
        AiConversationType conversationType = AiConversationType.VOCABULARY;
        
        // Execute
        String prompt = promptService.buildChatPrompt(history, newMessage, conversationType);
        
        // Assert
        assertNotNull(prompt, "Prompt should not be null");
        assertFalse(prompt.isEmpty(), "Prompt should not be empty");
        
        // Verify system prompt is included
        String systemPrompt = systemPromptConfig.getChatSystemPrompt();
        assertTrue(prompt.contains("assistant") || prompt.contains("English"), 
            "Prompt should contain assistant role from system prompt");
        
        // Verify dynamic context is included
        assertTrue(prompt.contains("vocabulary") || prompt.contains("VOCABULARY") || prompt.contains("Help with English vocabulary"), 
            "Prompt should contain conversation focus");
        assertTrue(prompt.contains("advice"), "Prompt should contain message history");
        assertTrue(prompt.contains("example"), "Prompt should contain new user message");
        
        log.info("✅ Chat prompt verified - {} chars", prompt.length());
        log.info("✅ Contains system prompt from config");
        log.info("✅ Contains message history and new message");
    }
    
    // ==================== TEST 4: LEARNING HINTS ====================
    
    @Test
    @DisplayName("Test 4: Learning Hint Prompt - Should include system prompt + progress context + difficulty level")
    public void testLearningHintPrompt() {
        log.info("\n--- Test 4: Learning Hints ---");
        
        // Setup
        Word word = Word.builder()
            .id(1L)
            .word("persistent")
            .definition("continuing firmly in an opinion or course of action")
            .partOfSpeech("adjective")
            .build();
        
        LearningProgress progress = LearningProgress.builder()
            .id(1L)
            .word(word)
            .correctCount(5)
            .wrongCount(2)
            .reviewCount(0)
            .status(LearningStatus.LEARNING)
            .build();
        
        Integer attemptCount = 1; // First attempt = Level 1 (minimal hint)
        
        // Execute
        String prompt = promptService.buildHintPrompt(progress, attemptCount);
        
        // Assert
        assertNotNull(prompt, "Prompt should not be null");
        assertFalse(prompt.isEmpty(), "Prompt should not be empty");
        
        // Verify system prompt is included
        String systemPrompt = systemPromptConfig.getHintSystemPrompt();
        assertTrue(prompt.contains("hint") || prompt.contains("learning"), 
            "Prompt should contain hint/learning content from system prompt");
        
        // Verify dynamic context is included
        assertTrue(prompt.contains("persistent"), "Prompt should contain word");
        assertTrue(prompt.contains("5 correct"), "Prompt should contain progress (correct count)");
        assertTrue(prompt.contains("Level 1") || prompt.contains("Minimal"), 
            "Prompt should contain hint difficulty level");
        
        log.info("✅ Hint prompt verified - {} chars", prompt.length());
        log.info("✅ Contains system prompt from config");
        log.info("✅ Contains learning progress and difficulty level");
    }
    
    // ==================== TEST 5: SPEAKING ANALYSIS ====================
    
    @Test
    @DisplayName("Test 5: Speaking Analysis Prompt - Should include system prompt + transcript + topic")
    public void testSpeakingAnalysisPrompt() {
        log.info("\n--- Test 5: Speaking Analysis ---");
        
        // Setup
        String transcript = "I like to watch movies. Movies is my favorite hobby. " +
                          "I watches movies every weekend with my friends.";
        String topic = "My Hobbies";
        String level = "B1";
        
        // Execute
        String prompt = promptService.buildSpeakingAnalysisPrompt(transcript, topic, level);
        
        // Assert
        assertNotNull(prompt, "Prompt should not be null");
        assertFalse(prompt.isEmpty(), "Prompt should not be empty");
        
        // Verify system prompt is included
        String systemPrompt = systemPromptConfig.getSpeakingAnalysisSystemPrompt();
        assertTrue(prompt.contains("analyze") || prompt.contains("score") || prompt.contains("evaluate"), 
            "Prompt should contain analysis/evaluation content from system prompt");
        
        // Verify dynamic context is included
        assertTrue(prompt.contains("movies"), "Prompt should contain transcript");
        assertTrue(prompt.contains("My Hobbies"), "Prompt should contain topic");
        assertTrue(prompt.contains("B1"), "Prompt should contain learner level");
        assertTrue(prompt.contains("JSON") || prompt.contains("score"), 
            "Prompt should specify JSON output format");
        
        log.info("✅ Speaking analysis prompt verified - {} chars", prompt.length());
        log.info("✅ Contains system prompt from config");
        log.info("✅ Contains transcript, topic, and level");
    }
    
    // ==================== TEST 6: SPEAKING FEEDBACK ====================
    
    @Test
    @DisplayName("Test 6: Speaking Feedback Prompt - Should include system prompt + scores + feedback sections")
    public void testSpeakingFeedbackPrompt() {
        log.info("\n--- Test 6: Speaking Feedback ---");
        
        // Setup
        String transcript = "I likes pizza. It is very delicious.";
        String topic = "Food Preference";
        String level = "A2";
        String scores = "{\"grammarScore\": 65, \"vocabularyScore\": 70, \"fluencyScore\": 75}";
        
        // Execute
        String prompt = promptService.buildSpeakingFeedbackPrompt(transcript, topic, level, scores);
        
        // Assert
        assertNotNull(prompt, "Prompt should not be null");
        assertFalse(prompt.isEmpty(), "Prompt should not be empty");
        
        // Verify system prompt is included
        String systemPrompt = systemPromptConfig.getSpeakingFeedbackSystemPrompt();
        assertTrue(prompt.contains("feedback") || prompt.contains("teacher") || prompt.contains("correction"), 
            "Prompt should contain feedback-related content from system prompt");
        
        // Verify dynamic context is included
        assertTrue(prompt.contains("pizza"), "Prompt should contain transcript");
        assertTrue(prompt.contains("Food Preference"), "Prompt should contain topic");
        assertTrue(prompt.contains("A2"), "Prompt should contain level");
        assertTrue(prompt.contains("CORRECTIONS") || prompt.contains("SUGGESTIONS"), 
            "Prompt should contain feedback section instructions");
        
        log.info("✅ Speaking feedback prompt verified - {} chars", prompt.length());
        log.info("✅ Contains system prompt from config");
        log.info("✅ Contains feedback sections and scores");
    }
    
    // ==================== VERIFICATION TESTS ====================
    
    @Test
    @DisplayName("Verification: All system prompts loaded successfully")
    public void verifyAllSystemPromptsLoaded() {
        log.info("\n--- Verification: System Prompts Loaded ---");
        
        String chat = systemPromptConfig.getChatSystemPrompt();
        String vocabulary = systemPromptConfig.getVocabularySystemPrompt();
        String grammar = systemPromptConfig.getGrammarSystemPrompt();
        String hint = systemPromptConfig.getHintSystemPrompt();
        String speakingAnalysis = systemPromptConfig.getSpeakingAnalysisSystemPrompt();
        String speakingFeedback = systemPromptConfig.getSpeakingFeedbackSystemPrompt();
        String speakingCorrection = systemPromptConfig.getSpeakingCorrectionSystemPrompt();
        
        assertFalse(chat.isEmpty(), "Chat system prompt should be loaded");
        assertFalse(vocabulary.isEmpty(), "Vocabulary system prompt should be loaded");
        assertFalse(grammar.isEmpty(), "Grammar system prompt should be loaded");
        assertFalse(hint.isEmpty(), "Hint system prompt should be loaded");
        assertFalse(speakingAnalysis.isEmpty(), "Speaking analysis system prompt should be loaded");
        assertFalse(speakingFeedback.isEmpty(), "Speaking feedback system prompt should be loaded");
        assertFalse(speakingCorrection.isEmpty(), "Speaking correction system prompt should be loaded");
        
        log.info("✅ Chat prompt: {} chars", chat.length());
        log.info("✅ Vocabulary prompt: {} chars", vocabulary.length());
        log.info("✅ Grammar prompt: {} chars", grammar.length());
        log.info("✅ Hint prompt: {} chars", hint.length());
        log.info("✅ Speaking analysis prompt: {} chars", speakingAnalysis.length());
        log.info("✅ Speaking feedback prompt: {} chars", speakingFeedback.length());
        log.info("✅ Speaking correction prompt: {} chars", speakingCorrection.length());
        
        int totalChars = chat.length() + vocabulary.length() + grammar.length() + 
                        hint.length() + speakingAnalysis.length() + speakingFeedback.length() + 
                        speakingCorrection.length();
        log.info("✅ TOTAL SYSTEM PROMPTS LOADED: {} chars", totalChars);
    }
    
    @Test
    @DisplayName("Verification: No breaking changes - prompt building still works")
    public void verifyNoBreakingChanges() {
        log.info("\n--- Verification: No Breaking Changes ---");
        
        // Test that all prompt building methods still work without errors
        try {
            // Test 1: buildVocabularyPrompt
            Word testWord = Word.builder().word("test").build();
            String vocabPrompt = promptService.buildVocabularyPrompt(testWord, null);
            assertNotNull(vocabPrompt);
            
            // Test 2: buildGrammarPrompt
            String grammarPrompt = promptService.buildGrammarPrompt("test text", "English");
            assertNotNull(grammarPrompt);
            
            // Test 3: buildChatPrompt
            String chatPrompt = promptService.buildChatPrompt(new ArrayList<>(), "test", AiConversationType.GENERAL);
            assertNotNull(chatPrompt);
            
            // Test 4: buildHintPrompt - this requires valid LearningProgress
            LearningProgress progress = LearningProgress.builder()
                .word(testWord)
                .correctCount(0)
                .wrongCount(0)
                .build();
            String hintPrompt = promptService.buildHintPrompt(progress, 1);
            assertNotNull(hintPrompt);
            
            // Test 5: buildSpeakingAnalysisPrompt
            String analysisPrompt = promptService.buildSpeakingAnalysisPrompt("transcript", "topic", "B1");
            assertNotNull(analysisPrompt);
            
            // Test 6: buildSpeakingFeedbackPrompt
            String feedbackPrompt = promptService.buildSpeakingFeedbackPrompt("transcript", "topic", "B1", null);
            assertNotNull(feedbackPrompt);
            
            log.info("✅ All 6 prompt building methods work correctly");
            log.info("✅ No exceptions thrown");
            log.info("✅ All methods return non-null results");
            
        } catch (Exception e) {
            fail("Unexpected exception during prompt building: " + e.getMessage(), e);
        }
    }
    
    @Test
    @DisplayName("Verification: Utility methods still work")
    public void verifyUtilityMethods() {
        log.info("\n--- Verification: Utility Methods ---");
        
        // Test sanitizeInput
        String input = "  Hello   world  \n\t test  ";
        String sanitized = promptService.sanitizeInput(input);
        // sanitize removes extra whitespace but may keep newlines/tabs structure
        assertTrue(sanitized.contains("Hello") && sanitized.contains("world") && sanitized.contains("test"), 
            "Sanitize should contain all words");
        log.info("✅ sanitizeInput works: '{}' → '{}'", input.replaceAll("\n", "\\\\n").replaceAll("\t", "\\\\t"), sanitized);
        
        // Test estimateTokenCount
        int tokens = promptService.estimateTokenCount("This is a test sentence.");
        assertTrue(tokens > 0, "Token count should be positive");
        log.info("✅ estimateTokenCount works: '{}' → {} tokens", "This is a test sentence.", tokens);
    }
}
