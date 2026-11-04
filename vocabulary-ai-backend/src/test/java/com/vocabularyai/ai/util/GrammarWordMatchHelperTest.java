package com.vocabularyai.ai.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GrammarWordMatchHelper Unit Tests")
class GrammarWordMatchHelperTest {

    @Test
    @DisplayName("Should match base word correctly")
    void shouldMatchBaseWord() {
        assertTrue(GrammarWordMatchHelper.containsWord("I have a cat at home.", "cat"));
        assertTrue(GrammarWordMatchHelper.containsWord("Cat is sleeping.", "cat"));
        assertTrue(GrammarWordMatchHelper.containsWord("Do you like cats?", "cat"));
    }

    @Test
    @DisplayName("Should match inflectional forms (-s, -es, -d, -ed, -ing)")
    void shouldMatchInflections() {
        // -s / -es
        assertTrue(GrammarWordMatchHelper.containsWord("He watches television every evening.", "watch"));
        assertTrue(GrammarWordMatchHelper.containsWord("She reads many books.", "book"));

        // -ed / -d
        assertTrue(GrammarWordMatchHelper.containsWord("We walked to the park yesterday.", "walk"));
        assertTrue(GrammarWordMatchHelper.containsWord("She loved that romantic movie.", "love"));

        // -ing
        assertTrue(GrammarWordMatchHelper.containsWord("They are playing football in the garden.", "play"));
        assertTrue(GrammarWordMatchHelper.containsWord("He is taking some notes right now.", "take"));

        // -y to -ies / -ied
        assertTrue(GrammarWordMatchHelper.containsWord("She studies English every morning.", "study"));
        assertTrue(GrammarWordMatchHelper.containsWord("He studied hard for the exam.", "study"));
    }

    @Test
    @DisplayName("Should match multi-word phrases")
    void shouldMatchMultiWordPhrases() {
        assertTrue(GrammarWordMatchHelper.containsWord("I really look forward to seeing you soon.", "look forward to"));
        assertFalse(GrammarWordMatchHelper.containsWord("I look forward into the future.", "look forward to"));
    }

    @Test
    @DisplayName("Should avoid false positive substring matches")
    void shouldAvoidFalsePositives() {
        // "resilient" should NOT match "resiliently"
        assertFalse(GrammarWordMatchHelper.containsWord("She handled the crisis resiliently.", "resilient"));

        // "cat" should NOT match "concatenate" or "certificate"
        assertFalse(GrammarWordMatchHelper.containsWord("Please concatenate the strings.", "cat"));
        assertFalse(GrammarWordMatchHelper.containsWord("He received a certificate of merit.", "cat"));

        // "the" should NOT match "breathe" or "bathe"
        assertFalse(GrammarWordMatchHelper.containsWord("Take a deep breath and breathe.", "the"));
    }

    @Test
    @DisplayName("Should handle null or empty inputs gracefully")
    void shouldHandleNullOrEmpty() {
        assertFalse(GrammarWordMatchHelper.containsWord(null, "cat"));
        assertFalse(GrammarWordMatchHelper.containsWord("Some text", null));
        assertFalse(GrammarWordMatchHelper.containsWord("", "cat"));
        assertFalse(GrammarWordMatchHelper.containsWord("Some text", "   "));
    }
}

