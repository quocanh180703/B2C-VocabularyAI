package com.vocabularyai.vocabulary.entity;

/**
 * Part of Speech Enumeration
 * 
 * Từ loại trong tiếng Anh
 */
public enum PartOfSpeech {
    NOUN("noun", "Danh từ"),
    VERB("verb", "Động từ"),
    ADJECTIVE("adj", "Tính từ"),
    ADVERB("adv", "Trạng từ"),
    PRONOUN("pron", "Đại từ"),
    PREPOSITION("prep", "Giới từ"),
    CONJUNCTION("conj", "Liên từ"),
    DETERMINER("det", "Hạn định từ"),
    INTERJECTION("intj", "Thán từ"),
    AUXILIARY("aux", "Động từ hỗ trợ"),
    OTHER("other", "Khác");

    private final String code;
    private final String displayName;

    PartOfSpeech(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Parse from string (normalize from CSV format)
     * 
     * @param pos "noun", "verb", "adj", "adv", etc.
     * @return PartOfSpeech or OTHER if not found
     */
    public static PartOfSpeech fromString(String pos) {
        if (pos == null || pos.trim().isEmpty()) {
            return OTHER;
        }

        String normalized = pos.trim().toLowerCase();

        for (PartOfSpeech p : PartOfSpeech.values()) {
            if (p.code.equalsIgnoreCase(normalized) || p.name().equalsIgnoreCase(normalized)) {
                return p;
            }
        }

        if (normalized.contains("adj")) return ADJECTIVE;
        if (normalized.contains("adv")) return ADVERB;
        if (normalized.contains("verb")) return VERB;
        if (normalized.contains("noun")) return NOUN;
        if (normalized.contains("prep")) return PREPOSITION;
        if (normalized.contains("conj")) return CONJUNCTION;
        if (normalized.contains("pron")) return PRONOUN;

        return OTHER;
    }
}
