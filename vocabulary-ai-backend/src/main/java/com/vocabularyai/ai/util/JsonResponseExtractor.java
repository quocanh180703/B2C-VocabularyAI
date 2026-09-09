package com.vocabularyai.ai.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for extracting and parsing JSON from AI responses
 * 
 * Handles various response formats from Gemini:
 * - Pure JSON: {"key": "value"}
 * - Markdown wrapped: ```json\n{...}\n```
 * - Text with embedded JSON: "Here is the result:\n{...}\nDone"
 * 
 * Usage:
 * String response = geminiService.generateResponse(prompt, userId);
 * String jsonString = JsonResponseExtractor.extractJsonString(response);
 * MyDto dto = objectMapper.readValue(jsonString, MyDto.class);
 */
@Slf4j
public class JsonResponseExtractor {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    // Pattern to match markdown JSON fences
    private static final Pattern MARKDOWN_JSON_FENCE = Pattern.compile(
        "```(?:json)?\\s*\\n?(.+?)\\n?```",
        Pattern.DOTALL
    );
    
    // Pattern to match JSON object or array (greedy)
    private static final Pattern JSON_OBJECT_ARRAY = Pattern.compile(
        "\\{(?:[^{}]|(?:\\{(?:[^{}]|(?:\\{[^{}]*\\}))*\\}))*\\}" +
        "|\\[(?:[^\\[\\]]|(?:\\[(?:[^\\[\\]]|(?:\\[[^\\[\\]]*\\]))*\\]))*\\]",
        Pattern.DOTALL
    );
    
    /**
     * Extract JSON string from AI response
     * 
     * Tries multiple strategies in order:
     * 1. Direct JSON parsing (if response is pure JSON)
     * 2. Markdown JSON fence extraction (```json...```)
     * 3. Greedy regex extraction (find {...} or [...])
     * 
     * @param response Raw response from Gemini AI
     * @return Extracted JSON string
     * @throws IllegalArgumentException if no valid JSON found
     */
    public static String extractJsonString(String response) {
        if (response == null || response.trim().isEmpty()) {
            throw new IllegalArgumentException("Response is empty or null");
        }
        
        String trimmed = response.trim();
        
        // Strategy 1: Try direct JSON parsing (response is already pure JSON)
        if ((trimmed.startsWith("{") && trimmed.endsWith("}")) ||
            (trimmed.startsWith("[") && trimmed.endsWith("]"))) {
            try {
                objectMapper.readTree(trimmed);
                log.debug("Response is pure JSON, using directly");
                return trimmed;
            } catch (Exception e) {
                log.debug("Direct JSON parsing failed, trying extraction strategies");
            }
        }
        
        // Strategy 2: Extract from markdown JSON fences (```json...```)
        Matcher markdownMatcher = MARKDOWN_JSON_FENCE.matcher(response);
        if (markdownMatcher.find()) {
            String extracted = markdownMatcher.group(1).trim();
            log.debug("Extracted JSON from markdown fences");
            return extracted;
        }
        
        // Strategy 3: Greedy regex extraction for JSON object or array
        Matcher jsonMatcher = JSON_OBJECT_ARRAY.matcher(response);
        if (jsonMatcher.find()) {
            String extracted = jsonMatcher.group();
            log.debug("Extracted JSON using greedy regex pattern");
            return extracted;
        }
        
        // If all strategies fail
        log.error("Failed to extract JSON from response: {}", response.substring(0, Math.min(200, response.length())));
        throw new IllegalArgumentException("No valid JSON found in response");
    }
    
    /**
     * Extract and parse JSON to JsonNode
     * 
     * @param response Raw response from Gemini
     * @return Parsed JsonNode
     * @throws IllegalArgumentException if extraction fails
     * @throws com.fasterxml.jackson.core.JsonProcessingException if parsing fails
     */
    public static JsonNode extractJsonNode(String response) throws Exception {
        String jsonString = extractJsonString(response);
        return objectMapper.readTree(jsonString);
    }
    
    /**
     * Extract JSON and parse to specific class
     * 
     * @param response Raw response from Gemini
     * @param valueType Target class
     * @param <T> Generic type
     * @return Parsed object of type T
     * @throws Exception if extraction or parsing fails
     */
    public static <T> T extractJson(String response, Class<T> valueType) throws Exception {
        String jsonString = extractJsonString(response);
        return objectMapper.readValue(jsonString, valueType);
    }
    
    /**
     * Safely extract JSON string (doesn't throw exception, returns null on failure)
     * 
     * @param response Raw response from Gemini
     * @return Extracted JSON string or null if extraction fails
     */
    public static String extractJsonStringSafe(String response) {
        try {
            return extractJsonString(response);
        } catch (Exception e) {
            log.warn("Safe JSON extraction failed: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Check if response contains valid JSON
     * 
     * @param response Raw response from Gemini
     * @return true if valid JSON can be extracted
     */
    public static boolean hasJson(String response) {
        return extractJsonStringSafe(response) != null;
    }
}
