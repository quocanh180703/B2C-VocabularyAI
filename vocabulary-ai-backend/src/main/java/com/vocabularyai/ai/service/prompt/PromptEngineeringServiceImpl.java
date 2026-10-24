package com.vocabularyai.ai.service.prompt;

import com.vocabularyai.ai.config.SystemPromptConfig;
import com.vocabularyai.ai.entity.AiMessage;
import com.vocabularyai.ai.entity.AiConversationType;
import com.vocabularyai.ai.entity.AiMessageRole;
import com.vocabularyai.learning.entity.LearningProgress;
import com.vocabularyai.vocabulary.entity.Word;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.StringJoiner;

/**
 * Implementation of PromptEngineeringService
 * 
 * Builds optimized prompts for different AI use cases:
 * - Chat conversations
 * - Vocabulary explanations
 * - Grammar checking
 * - Learning hints with progressive difficulty
 * 
 * System prompts are loaded from application.yml via SystemPromptConfig
 * (previously hard-coded, now externalized for easier maintenance)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PromptEngineeringServiceImpl implements PromptEngineeringService {
    
    private final SystemPromptConfig systemPromptConfig;
    
    // ==================== Chat Conversation Prompt Building ====================
    
    @Override
    public String buildChatPrompt(List<AiMessage> history, String newMessage, AiConversationType conversationType) {
        StringBuilder prompt = new StringBuilder();
        
        // System prompt (loaded from application.yml)
        String systemPrompt = systemPromptConfig.getChatSystemPrompt();
        prompt.append(systemPrompt).append("\n\n");
        
        // Add conversation type context
        String typeContext = switch (conversationType) {
            case VOCABULARY -> "Focus: Help with English vocabulary learning. Explain words clearly and concisely with practical examples.";
            case GRAMMAR -> "Focus: Help with grammar and writing corrections. Be clear, direct, and concise in explanations.";
            case LEARNING -> "Focus: Support learning progress review. Reference what the user has learned.";
            default -> "Focus: General English conversation and learning support.";
        };
        prompt.append("Current conversation focus: ").append(typeContext).append("\n\n");
        prompt.append("CRITICAL INSTRUCTIONS FOR ASSISTANT:\n");
        prompt.append("1. LANGUAGE MATCHING: Always respond in the EXACT SAME LANGUAGE as the user's latest message (Vietnamese if the user writes in Vietnamese, English if the user writes in English). If user asks for translation/comparison, provide both clearly.\n");
        prompt.append("2. CONCISE & CLEAN: Keep your answer clean, well-spaced, and straight to the point without filler greetings or bloated walls of text. Use clean Markdown (bullet points, bold key terms).\n\n");
        
        // Add message history
        if (!history.isEmpty()) {
            prompt.append("Previous conversation:\n");
            int maxMessages = Math.min(10, history.size()); // Keep last 10 messages for context
            for (int i = history.size() - maxMessages; i < history.size(); i++) {
                AiMessage msg = history.get(i);
                String role = msg.getRole() == AiMessageRole.USER ? "User" : "Assistant";
                prompt.append(role).append(": ").append(msg.getContent()).append("\n");
            }
            prompt.append("\n");
        }
        
        // Add new user message
        prompt.append("User: ").append(sanitizeInput(newMessage)).append("\n");
        prompt.append("Assistant: ");
        
        log.debug("Built chat prompt for conversation type: {} with {} history messages", 
                 conversationType, history.size());
        
        return prompt.toString();
    }
    
    // ==================== Vocabulary Explanation Prompt Building ====================
    
    @Override
    public String buildVocabularyPrompt(Word word, String userContext) {
        StringBuilder prompt = new StringBuilder();
        
        // System prompt (loaded from application.yml)
        String systemPrompt = systemPromptConfig.getVocabularySystemPrompt();
        prompt.append(systemPrompt).append("\n\n");
        
        // Add word details
        prompt.append("Word to explain: ").append(word.getWord()).append("\n");
        if (word.getPartOfSpeech() != null) {
            prompt.append("Part of speech: ").append(word.getPartOfSpeech()).append("\n");
        }
        if (word.getLevel() != null) {
            prompt.append("English level: ").append(word.getLevel()).append("\n");
        }
        
        // Add user context if provided
        if (userContext != null && !userContext.trim().isEmpty()) {
            prompt.append("User context: ").append(sanitizeInput(userContext)).append("\n");
        }
        
        // Add existing definition from database as reference
        if (word.getDefinition() != null && !word.getDefinition().trim().isEmpty()) {
            prompt.append("Reference definition: ").append(word.getDefinition()).append("\n");
        }
        if (word.getExample() != null && !word.getExample().trim().isEmpty()) {
            prompt.append("Reference example: ").append(word.getExample()).append("\n");
        }
        
        prompt.append("\nProvide a comprehensive explanation following the format specified above.");
        
        log.debug("Built vocabulary explanation prompt for word: {}", word.getWord());
        
        return prompt.toString();
    }
    
    // ==================== Grammar Checking Prompt Building ====================
    
    @Override
    public String buildGrammarPrompt(String text, String language) {
        StringBuilder prompt = new StringBuilder();
        
        // System prompt (loaded from application.yml)
        String systemPrompt = systemPromptConfig.getGrammarSystemPrompt();
        prompt.append(systemPrompt).append("\n\n");
        
        // Add language specification
        prompt.append("Language: ").append(language != null ? language : "English").append("\n\n");
        
        // Add text to check
        prompt.append("Text to check:\n");
        prompt.append("---\n");
        prompt.append(sanitizeInput(text));
        prompt.append("\n---\n\n");
        
        // Add expected output format
        prompt.append("Please provide your analysis in the following JSON-like format:\n");
        prompt.append("{\n");
        prompt.append("  \"correctedText\": \"[Full corrected version]\",\n");
        prompt.append("  \"errors\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"startOffset\": [number],\n");
        prompt.append("      \"endOffset\": [number],\n");
        prompt.append("      \"errorType\": \"[type]\",\n");
        prompt.append("      \"suggestion\": \"[correction]\",\n");
        prompt.append("      \"explanation\": \"[why it's wrong]\"\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}");
        
        log.debug("Built grammar checking prompt for text length: {}", text.length());
        
        return prompt.toString();
    }
    
    // ==================== Learning Hint Prompt Building ====================
    
    @Override
    public String buildHintPrompt(LearningProgress progress, Integer attemptCount) {
        if (attemptCount == null) {
            attemptCount = progress.getReviewCount() != null ? progress.getReviewCount() : 0;
        }
        
        // Determine hint difficulty level
        int hintLevel = determineHintLevel(attemptCount);
        
        StringBuilder prompt = new StringBuilder();
        
        // System prompt (loaded from application.yml)
        String systemPrompt = systemPromptConfig.getHintSystemPrompt();
        prompt.append(systemPrompt).append("\n\n");
        
        // Add student progress context
        prompt.append("Student Progress Information:\n");
        prompt.append("- Word: ").append(progress.getWord().getWord()).append("\n");
        prompt.append("- Definition: ").append(progress.getWord().getDefinition()).append("\n");
        if (progress.getWord().getPartOfSpeech() != null) {
            prompt.append("- Part of speech: ").append(progress.getWord().getPartOfSpeech()).append("\n");
        }
        prompt.append("- Current status: ").append(progress.getStatus()).append("\n");
        prompt.append("- Attempts on this review: ").append(attemptCount).append("\n");
        prompt.append("- Overall progress: ").append(progress.getCorrectCount()).append(" correct, ")
              .append(progress.getWrongCount()).append(" wrong\n");
        prompt.append("- Accuracy: ").append(String.format("%.1f%%", progress.getAccuracy())).append("\n\n");
        
        // Add hint level instructions
        prompt.append("Hint Difficulty Level: ").append(hintLevel).append("\n");
        prompt.append(getHintLevelInstructions(hintLevel)).append("\n\n");
        
        // Add hint generation request
        prompt.append("Please provide a learning hint at the specified level that helps the student ")
              .append("understand this word better without directly giving away the answer.");
        
        log.debug("Built learning hint prompt for word: {}, attempt: {}, level: {}", 
                 progress.getWord().getWord(), attemptCount, hintLevel);
        
        return prompt.toString();
    }
    
    // ==================== Utility Methods ====================
    
    @Override
    public String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }
        
        return input
            // Remove null characters
            .replaceAll("\\x00", "")
            // Remove control characters (except newlines and tabs)
            .replaceAll("[\\p{Cc}&&[^\n\t]]", "")
            // Normalize whitespace (multiple spaces to single space)
            .replaceAll(" +", " ")
            // Remove leading/trailing whitespace from lines
            .replaceAll("(?m)^\\s+|\\s+$", "")
            // Trim overall
            .trim();
    }
    
    @Override
    public int estimateTokenCount(String text) {
        if (text == null) {
            return 0;
        }
        
        // Approximation: ~1 token per 4 characters
        // More accurate: use Gemini tokenizer API or more sophisticated counting
        // For now: characters / 4 + word count / 3 (better approximation)
        
        int charCount = text.length();
        int wordCount = text.trim().isEmpty() ? 0 : text.trim().split("\\s+").length;
        
        // Formula: (chars / 4) + (words / 3) gives better estimate
        // Average token = ~3.5 chars, but words vary from 1-5 tokens
        int estimatedTokens = (charCount / 4) + (wordCount / 3);
        
        // Minimum 1 token
        return Math.max(1, estimatedTokens);
    }
    
    /**
     * Determine hint difficulty level based on attempt count
     * 
     * @param attemptCount Number of attempts
     * @return Hint level (1, 2, or 3)
     */
    private int determineHintLevel(Integer attemptCount) {
        if (attemptCount == null || attemptCount <= 1) {
            return 1; // Minimal hint
        } else if (attemptCount == 2) {
            return 2; // Moderate hint
        } else {
            return 3; // Detailed hint
        }
    }
    
    /**
     * Get hint instructions based on difficulty level
     * 
     * @param hintLevel Hint level (1, 2, or 3)
     * @return Instructions for generating hint at this level
     */
    private String getHintLevelInstructions(int hintLevel) {
        return switch (hintLevel) {
            case 1 -> """
                Level 1 - Minimal Hint:
                Provide only a very brief hint - just the basic definition or translation.
                Keep it to 1-2 sentences maximum.
                Goal: Help the student think, not solve directly.""";
            
            case 2 -> """
                Level 2 - Moderate Hint:
                Provide the definition plus 1-2 usage examples.
                Keep it to 3-4 sentences.
                Goal: Guide the student towards the answer.""";
            
            case 3 -> """
                Level 3 - Detailed Hint:
                Provide comprehensive explanation including:
                - Full definition
                - 2-3 usage examples
                - Learning strategies (e.g., memory techniques, etymology)
                - Related words or word families
                Goal: Support deeper understanding if student is struggling.""";
            
            default -> "Provide an appropriate hint for learning.";
        };
    }
    
    // ==================== Speaking Analysis Prompts (Phase 8) ====================
    
    @Override
    public String buildSpeakingAnalysisPrompt(String transcript, String topic, String level) {
        StringBuilder prompt = new StringBuilder();
        
        // System prompt (loaded from application.yml)
        String systemPrompt = systemPromptConfig.getSpeakingAnalysisSystemPrompt();
        prompt.append(systemPrompt).append("\n\n");
        
        prompt.append("Topic: ").append(topic).append("\n");
        prompt.append("User's English Level: ").append(level).append(" (CEFR scale A1-C2)\n\n");
        
        prompt.append("Transcript to analyze:\n");
        prompt.append("---\n");
        prompt.append(sanitizeInput(transcript));
        prompt.append("\n---\n\n");
        
        prompt.append("Provide detailed analysis in the JSON format specified above.");
        
        log.debug("Built speaking analysis prompt for topic: {}, level: {}, transcript length: {}", 
                 topic, level, transcript.length());
        
        return prompt.toString();
    }
    
    @Override
    public String buildSpeakingFeedbackPrompt(String transcript, String topic, String level, String scores) {
        StringBuilder prompt = new StringBuilder();
        
        // System prompt (loaded from application.yml)
        String systemPrompt = systemPromptConfig.getSpeakingFeedbackSystemPrompt();
        prompt.append(systemPrompt).append("\n\n");
        
        prompt.append("Speaking Topic: ").append(topic).append("\n");
        prompt.append("Student Level: ").append(level).append("\n");
        
        if (scores != null && !scores.isEmpty()) {
            prompt.append("Evaluation Scores: ").append(scores).append("\n");
        }
        
        prompt.append("\nStudent's Response:\n");
        prompt.append("---\n");
        prompt.append(sanitizeInput(transcript));
        prompt.append("\n---\n\n");
        
        prompt.append("Analyze thoroughly for all grammar, tense, vocabulary, and phrasing errors. Return ONLY a valid JSON object matching the required schema (no markdown, no extra explanation):\n");
        prompt.append("{\n");
        prompt.append("  \"feedback\": \"Tổng quan nhận xét bằng tiếng Việt...\",\n");
        prompt.append("  \"corrections\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"original\": \"exact wrong phrase\",\n");
        prompt.append("      \"corrected\": \"corrected natural English sentence\",\n");
        prompt.append("      \"errorType\": \"Tense / Grammar / Vocabulary / Preposition\",\n");
        prompt.append("      \"explanation\": \"Giải thích chi tiết tại sao sai và cách dùng đúng bằng tiếng Việt\",\n");
        prompt.append("      \"alternatives\": [\"cách diễn đạt khác\"]\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"strengths\": [\"điểm làm tốt (nếu có)\"],\n");
        prompt.append("  \"weaknesses\": [\"điểm yếu cụ thể cần khắc phục\"],\n");
        prompt.append("  \"suggestions\": [\"gợi ý luyện tập để tiến bộ\"]\n");
        prompt.append("}");
        
        log.debug("Built speaking feedback prompt for topic: {}, level: {}", topic, level);
        
        return prompt.toString();
    }
    
    @Override
    public String buildSpeakingCorrectionPrompt(String transcript, String errorType) {
        StringBuilder prompt = new StringBuilder();
        
        // System prompt (loaded from application.yml)
        String systemPrompt = systemPromptConfig.getSpeakingCorrectionSystemPrompt();
        prompt.append(systemPrompt).append("\n\n");
        
        prompt.append("Error Focus: ").append(errorType).append("\n\n");
        
        prompt.append("Transcript to check:\n");
        prompt.append("---\n");
        prompt.append(sanitizeInput(transcript));
        prompt.append("\n---\n\n");
        
        prompt.append("Please analyze this transcript and provide corrections in the JSON format above.\n");
        prompt.append("Be thorough but also encouraging - frame corrections as learning opportunities.\n");
        
        log.debug("Built speaking correction prompt for error type: {}, transcript length: {}", 
                 errorType, transcript.length());
        
        return prompt.toString();
    }
}
