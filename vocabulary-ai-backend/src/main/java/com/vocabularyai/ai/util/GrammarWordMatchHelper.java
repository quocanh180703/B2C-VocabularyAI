package com.vocabularyai.ai.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GrammarWordMatchHelper
 * 
 * Utility to match words/phrases in English text using word boundaries.
 * Accepts common inflections: -s, -es, -d, -ed, -ing.
 * Prevents false positives (e.g. "resiliently" will not match "resilient").
 */
public class GrammarWordMatchHelper {

    /**
     * Checks if targetWord is used in the given text.
     * 
     * @param text The user's input text
     * @param targetWord The target word or short phrase
     * @return true if targetWord is matched, false otherwise
     */
    public static boolean containsWord(String text, String targetWord) {
        if (text == null || targetWord == null) {
            return false;
        }

        String trimmedWord = targetWord.trim().toLowerCase();
        String trimmedText = text.toLowerCase();

        if (trimmedWord.isEmpty() || trimmedText.isEmpty()) {
            return false;
        }

        // If targetWord contains multiple words (e.g. "look forward to"), match exact phrase with boundaries
        if (trimmedWord.contains(" ")) {
            String phraseRegex = "\\b" + Pattern.quote(trimmedWord) + "\\b";
            return Pattern.compile(phraseRegex, Pattern.CASE_INSENSITIVE).matcher(trimmedText).find();
        }

        // Single word regex: match base word with optional inflections: s, es, d, ed, ing
        // Note: For word ending with 'e' (like "take" -> "taking", "taked"), we match base or base minus 'e' + ing/ed
        String baseQuoted = Pattern.quote(trimmedWord);
        String regex;

        if (trimmedWord.endsWith("e") && trimmedWord.length() > 2) {
            String stem = trimmedWord.substring(0, trimmedWord.length() - 1);
            String stemQuoted = Pattern.quote(stem);
            // e.g. "make" -> make, makes, making, maked
            regex = "\\b(?:" + baseQuoted + "(?:s|d|ed)?|" + stemQuoted + "(?:ing|ed|d))\\b";
        } else if (trimmedWord.endsWith("y") && trimmedWord.length() > 2) {
            String stem = trimmedWord.substring(0, trimmedWord.length() - 1);
            String stemQuoted = Pattern.quote(stem);
            // e.g. "study" -> study, studies, studied, studying
            regex = "\\b(?:" + baseQuoted + "(?:ing)?|" + stemQuoted + "(?:ies|ied))\\b";
        } else {
            // General word: base + optional s, es, d, ed, ing
            regex = "\\b" + baseQuoted + "(?:s|es|d|ed|ing)?\\b";
        }

        Matcher matcher = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(trimmedText);
        return matcher.find();
    }
}

