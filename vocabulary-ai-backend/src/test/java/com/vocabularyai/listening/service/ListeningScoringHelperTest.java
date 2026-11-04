package com.vocabularyai.listening.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ListeningScoringHelperTest {

    @Test
    void testExactMatch() {
        String passage = "Hello world, my name is John.";
        String user = "Hello world, my name is John.";

        ListeningScoringHelper.ScoringResult res = ListeningScoringHelper.evaluate(passage, user);
        assertEquals(100, res.score);
        assertEquals(6, res.correctWords);
        assertEquals(0, res.nearMissWords);
    }

    @Test
    void testContractionExpansion() {
        String passage = "I don't know what you're doing.";
        String user = "I do not know what you are doing.";

        ListeningScoringHelper.ScoringResult res = ListeningScoringHelper.evaluate(passage, user);
        assertEquals(100, res.score);
    }

    @Test
    void testNearMissForLongWords() {
        String passage = "She is definitely a professional doctor.";
        String user = "She is definitly a profesionel doctor.";

        ListeningScoringHelper.ScoringResult res = ListeningScoringHelper.evaluate(passage, user);
        assertTrue(res.nearMissWords >= 1);
        assertTrue(res.score >= 80);
    }

    @Test
    void testSpamAndEmpty() {
        String passage = "Good morning everyone.";
        String emptyUser = "";

        ListeningScoringHelper.ScoringResult emptyRes = ListeningScoringHelper.evaluate(passage, emptyUser);
        assertEquals(0, emptyRes.score);

        String spamUser = "one two three four five six seven eight nine ten eleven twelve";
        ListeningScoringHelper.ScoringResult spamRes = ListeningScoringHelper.evaluate(passage, spamUser);
        assertTrue(spamRes.score < 20);
    }

    @Test
    void testTenWordsWithTwoMissing() {
        String passage = "one two three four five six seven eight nine ten";
        String user = "one two three four five six seven eight"; // missing nine, ten

        ListeningScoringHelper.ScoringResult res = ListeningScoringHelper.evaluate(passage, user);
        assertEquals(80, res.score);
        assertEquals(8, res.correctWords);

        long missingCount = res.wordResults.stream()
                .filter(w -> "MISSING".equalsIgnoreCase(w.getStatus()))
                .count();
        assertEquals(2, missingCount);
    }

    @Test
    void testTenWordsWithFiveExtraWords() {
        String passage = "one two three four five six seven eight nine ten";
        String user = "one two three four five six seven eight nine ten extra junk words added here";

        ListeningScoringHelper.ScoringResult res = ListeningScoringHelper.evaluate(passage, user);
        assertTrue(res.score < 100, "Score must be penalized when typing extra words");
        assertEquals(10, res.correctWords);
    }
}

