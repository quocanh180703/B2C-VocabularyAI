package com.vocabularyai.ai.service.prompt;

import com.vocabularyai.ai.entity.AiMessage;
import com.vocabularyai.ai.entity.AiConversationType;
import com.vocabularyai.learning.entity.LearningProgress;
import com.vocabularyai.vocabulary.entity.Word;

import java.util.List;

/**
 * Interface for prompt engineering and optimization
 * 
 * Responsible for:
 * - Building optimized prompts for different AI use cases
 * - Injecting context from user history and preferences
 * - Formatting prompts for best Gemini performance
 * - Sanitizing user input
 * - Estimating token usage
 */
public interface PromptEngineeringService {
    
    /**
     * Build chat prompt with conversation history
     * 
     * Creates a prompt that includes:
     * - System role and instructions
     * - Conversation history for context
     * - New user message
     * - Conversation type hints (GENERAL, VOCABULARY, GRAMMAR, LEARNING)
     * @param history Message history from conversation
     * @param newMessage New message from user
     * @param conversationType Type of conversation
     * @return Complete prompt ready to send to Gemini API
     */
    default String buildChatPrompt(List<AiMessage> history, String newMessage, AiConversationType conversationType) {
        return buildChatPrompt(history, newMessage, conversationType, null);
    }

    /**
     * Build chat prompt with conversation context and factual user snapshot
     * 
     * @param history Message history from conversation
     * @param newMessage New message from user
     * @param conversationType Type of conversation
     * @param snapshotText Formatted text of verified user learning & account snapshot (optional, can be null)
     * @return Complete prompt ready to send to Gemini API
     */
    String buildChatPrompt(List<AiMessage> history, String newMessage, AiConversationType conversationType, String snapshotText);
    
    /**
     * Build vocabulary explanation prompt
     * 
     * Creates a prompt for explaining a word with:
     * - Clear definition
     * - Usage examples
     * - Etymology
     * - Related words (synonyms/antonyms)
     * - Part of speech
     * - Formatted as structured text
     * 
     * @param word Word entity with definition and metadata
     * @param userContext Optional context about user (learning level, progress)
     * @return Prompt for vocabulary explanation
     */
    String buildVocabularyPrompt(Word word, String userContext);
    
    /**
     * Build grammar checking prompt
     * 
     * Creates a prompt for grammar analysis:
     * - Identifies grammar, spelling, style, punctuation errors
     * - Returns error positions and suggestions
     * - Provides corrected text
     * - Includes explanations for each error
     * 
     * @param text Text to check
     * @param language Language code (default: English)
     * @return Prompt for grammar checking
     */
    String buildGrammarPrompt(String text, String language);
    
    /**
     * Build learning hint prompt with progressive difficulty
     * 
     * Difficulty levels:
     * - Level 1 (attemptCount ≤ 1): Definition only, very minimal
     * - Level 2 (attemptCount == 2): Definition + 1 example
     * - Level 3 (attemptCount ≥ 3): Full explanation + learning strategies
     * 
     * @param progress Learning progress record
     * @param attemptCount Number of attempts (determines difficulty level)
     * @return Prompt for generating learning hint
     */
    String buildHintPrompt(LearningProgress progress, Integer attemptCount);
    
    /**
     * Sanitize user input
     * 
     * Removes:
     * - Control characters
     * - Excessive whitespace
     * - Null bytes
     * 
     * @param input Raw user input
     * @return Sanitized input safe for use in prompts
     */
    String sanitizeInput(String input);
    
    /**
     * Estimate token count for a text
     * 
     * Approximation: ~1 token per 4 characters
     * More accurate estimation could use Gemini tokenizer API
     * 
     * @param text Text to estimate tokens for
     * @return Estimated token count
     */
    int estimateTokenCount(String text);
    
    // ==================== Speaking Analysis Prompts (Phase 8) ====================
    
    /**
     * Build speaking analysis prompt for AI evaluation
     * 
     * Creates a prompt for Gemini to analyze speaking transcript:
     * - Grammar quality
     * - Vocabulary range
     * - Fluency assessment
     * - Content relevance
     * - Overall score
     * 
     * Input: transcript (text from Speech-to-Text service)
     * Output: JSON structured response with scores and feedback
     * 
     * @param transcript Transcript from speech (Speech-to-Text result)
     * @param topic Speaking topic/theme
     * @param level User's English level (A1-C2)
     * @return Prompt for speaking analysis
     */
    String buildSpeakingAnalysisPrompt(String transcript, String topic, String level);
    
    /**
     * Build speaking feedback prompt for detailed feedback generation
     * 
     * Creates a prompt for Gemini to generate detailed feedback:
     * - Corrections for errors found
     * - Suggestions for improvement
     * - Strengths to build upon
     * - Weaknesses to focus on
     * 
     * @param transcript User's spoken text
     * @param topic Speaking topic
     * @param level User's English level
     * @param scores Existing scores (for context)
     * @return Prompt for generating detailed feedback
     */
    String buildSpeakingFeedbackPrompt(String transcript, String topic, String level, String scores);
    
    /**
     * Build speaking correction prompt for identifying and fixing errors
     * 
     * Creates a prompt for Gemini to:
     * - Identify specific grammar/vocabulary/fluency errors
     * - Provide corrections with explanations
     * - Suggest alternative expressions
     * 
     * @param transcript User's spoken text
     * @param errorType Type of error to focus on (grammar, vocabulary, pronunciation)
     * @return Prompt for error correction
     */
    String buildSpeakingCorrectionPrompt(String transcript, String errorType);
}
