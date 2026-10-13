package com.vocabularyai.vocabulary.entity;

/**
 * CEFR Level Enumeration
 * 
 * CEFR: Common European Framework of Reference for Languages
 * 
 * Levels:
 * - A1: Elementary (absolute beginner)
 * - A2: Elementary (beginner)
 * - B1: Intermediate
 * - B2: Upper Intermediate
 * - C1: Advanced
 * - C2: Mastery (not in this dataset)
 */
public enum CEFRLevel {
    A1("A1 - Elementary", 1),
    A2("A2 - Elementary", 2),
    B1("B1 - Intermediate", 3),
    B2("B2 - Upper Intermediate", 4),
    C1("C1 - Advanced", 5);

    private final String displayName;
    private final int levelIndex;

    CEFRLevel(String displayName, int levelIndex) {
        this.displayName = displayName;
        this.levelIndex = levelIndex;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getLevelIndex() {
        return levelIndex;
    }

    /**
     * Parse from string (case-insensitive)
     * 
     * @param level "A1", "a1", "A2", etc.
     * @return CEFRLevel or null
     */
    public static CEFRLevel fromString(String level) {
        if (level == null) {
            return null;
        }
        try {
            return CEFRLevel.valueOf(level.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Check if level is valid
     */
    public static boolean isValid(String level) {
        return fromString(level) != null;
    }
}
