package com.vocabularyai.ai.service.prompt;

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
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PromptEngineeringServiceImpl implements PromptEngineeringService {
    
    // ==================== System Prompts ====================
    
    private static final String CHAT_SYSTEM_PROMPT = """
        You are a helpful English learning assistant. Your role is to help users learn English in an engaging and supportive way.

        Guidelines:
        - Provide clear, concise explanations
        - Use simple language when explaining complex concepts
        - Encourage the user and provide positive feedback
        - Correct mistakes gently with explanations
        - Tailor responses to the user's learning level
        - Use examples from everyday situations when possible
        """;
    
    private static final String VOCABULARY_SYSTEM_PROMPT = """
        You are an English vocabulary expert. Your task is to provide comprehensive explanations of English words.
        
        For each word explanation, include:
        1. A clear, simple definition appropriate for English learners
        2. Part of speech
        3. 2-3 practical usage examples
        4. Etymology or word origin if interesting
        5. Related words (synonyms or antonyms)
        6. Common phrases using this word
        
        Format the response with clear headings for each section.
        """;
    
    private static final String GRAMMAR_SYSTEM_PROMPT = """
        You are an expert English grammar checker and writing assistant.
        
        Analyze the provided text for:
        - Grammar errors
        - Spelling mistakes
        - Punctuation issues
        - Style and clarity improvements
        
        For each error, provide:
        - The exact position (start and end character index)
        - Type of error (grammar, spelling, punctuation, style)
        - The suggested correction
        - A brief explanation of why it's an error
        
        Also provide the fully corrected version of the entire text.
        """;
    
    private static final String HINT_SYSTEM_PROMPT = """
        You are an expert learning coach specializing in English vocabulary instruction.
        
        Your task is to provide learning hints that help students figure out answers themselves rather than just telling them.
        Adapt hint difficulty based on attempt count:
        - Level 1 (first attempt): Provide minimal hint - just the definition or translation
        - Level 2 (second attempt): Provide moderate hint - definition + 1-2 examples
        - Level 3 (third+ attempt): Provide detailed hint - full explanation + learning strategies
        
        Always encourage students and explain the learning purpose.
        """;
    
    // ==================== Chat Conversation Prompt Building ====================
    
    @Override
    public String buildChatPrompt(List<AiMessage> history, String newMessage, AiConversationType conversationType) {
        StringBuilder prompt = new StringBuilder();
        
        // System prompt
        prompt.append(CHAT_SYSTEM_PROMPT).append("\n\n");
        
        // Add conversation type context
        String typeContext = switch (conversationType) {
            case VOCABULARY -> "Focus: Help with English vocabulary learning. Explain words clearly with examples.";
            case GRAMMAR -> "Focus: Help with grammar and writing corrections. Be detailed in explanations.";
            case LEARNING -> "Focus: Support learning progress review. Reference what the user has learned.";
            default -> "Focus: General English conversation and learning support.";
        };
        prompt.append("Current conversation focus: ").append(typeContext).append("\n\n");
        
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
        
        // System prompt
        prompt.append(VOCABULARY_SYSTEM_PROMPT).append("\n\n");
        
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
        
        // System prompt
        prompt.append(GRAMMAR_SYSTEM_PROMPT).append("\n\n");
        
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
        
        // System prompt
        prompt.append(HINT_SYSTEM_PROMPT).append("\n\n");
        
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
        
        prompt.append("You are an expert English language teacher and speaking assessment specialist.\n");
        prompt.append("Your task is to analyze a speaking transcript and provide comprehensive feedback.\n\n");
        prompt.append("Evaluate the following aspects:\n");
        prompt.append("1. Grammar - Correctness of sentence structure, tense usage, subject-verb agreement\n");
        prompt.append("2. Vocabulary - Range of words used, appropriateness, word choice accuracy\n");
        prompt.append("3. Fluency - Smoothness of speech, pace, natural rhythm (from transcript)\n");
        prompt.append("4. Content - Relevance to topic, completeness of ideas, logical flow\n");
        prompt.append("5. Overall Score - Weighted average of above scores (25% each, except pronunciation 0%)\n\n");
        
        prompt.append("Return ONLY a JSON response with this exact structure:\n");
        prompt.append("{\n");
        prompt.append("  \"grammarScore\": <0-100>,\n");
        prompt.append("  \"vocabularyScore\": <0-100>,\n");
        prompt.append("  \"fluencyScore\": <0-100>,\n");
        prompt.append("  \"contentScore\": <0-100>,\n");
        prompt.append("  \"overallScore\": <0-100>,\n");
        prompt.append("  \"feedback\": \"<brief overall feedback>\",\n");
        prompt.append("  \"strengths\": [\"<strength1>\", \"<strength2>\"],\n");
        prompt.append("  \"weaknesses\": [\"<weakness1>\", \"<weakness2>\"]\n");
        prompt.append("}\n\n");
        
        prompt.append("Important:\n");
        prompt.append("- Be consistent with scoring\n");
        prompt.append("- Scores must be between 0-100\n");
        prompt.append("- Provide constructive feedback\n");
        prompt.append("- Consider the user's level (").append(level).append(")\n\n");
        
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
        
        prompt.append("You are an experienced English language teacher providing detailed feedback to a student.\n\n");
        prompt.append("Generate constructive and encouraging feedback based on:\n");
        prompt.append("- The student's spoken response (transcript)\n");
        prompt.append("- The speaking topic/prompt\n");
        prompt.append("- Their proficiency level\n");
        prompt.append("- Their evaluation scores (if available)\n\n");
        
        prompt.append("Feedback should include:\n");
        prompt.append("1. Corrections: Specific grammar/vocabulary errors with explanations\n");
        prompt.append("2. Suggestions: Improvements they can make\n");
        prompt.append("3. Strengths: What they did well\n");
        prompt.append("4. Next Steps: What to practice\n\n");
        
        prompt.append("Use a supportive, encouraging tone.\n");
        prompt.append("Format with clear sections.\n\n");
        
        prompt.append("Speaking Topic: ").append(topic).append("\n");
        prompt.append("Student Level: ").append(level).append("\n");
        
        if (scores != null && !scores.isEmpty()) {
            prompt.append("Evaluation Scores: ").append(scores).append("\n");
        }
        
        prompt.append("\nStudent's Response:\n");
        prompt.append("---\n");
        prompt.append(sanitizeInput(transcript));
        prompt.append("\n---\n\n");
        
        prompt.append("Provide detailed feedback with:\n");
        prompt.append("- CORRECTIONS: [List grammar/vocabulary errors with fixes]\n");
        prompt.append("- SUGGESTIONS: [Ideas for improvement]\n");
        prompt.append("- STRENGTHS: [What was done well]\n");
        prompt.append("- NEXT STEPS: [Practice recommendations]\n");
        
        log.debug("Built speaking feedback prompt for topic: {}, level: {}", topic, level);
        
        return prompt.toString();
    }
    
    @Override
    public String buildSpeakingCorrectionPrompt(String transcript, String errorType) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are an expert in English language correction and tutoring.\n\n");
        prompt.append("Analyze the following transcript for ").append(errorType).append(" errors.\n");
        prompt.append("For each error found, provide:\n");
        prompt.append("- The original text\n");
        prompt.append("- The correction\n");
        prompt.append("- A brief explanation\n");
        prompt.append("- An alternative expression if applicable\n\n");
        
        prompt.append("Return a JSON response with this structure:\n");
        prompt.append("{\n");
        prompt.append("  \"errors\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"original\": \"<original text>\",\n");
        prompt.append("      \"corrected\": \"<corrected text>\",\n");
        prompt.append("      \"type\": \"grammar|vocabulary|pronunciation|style\",\n");
        prompt.append("      \"explanation\": \"<why it's wrong>\",\n");
        prompt.append("      \"alternative\": \"<optional alternative>\"\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"correctedTranscript\": \"<full corrected version>\"\n");
        prompt.append("}\n\n");
        
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
