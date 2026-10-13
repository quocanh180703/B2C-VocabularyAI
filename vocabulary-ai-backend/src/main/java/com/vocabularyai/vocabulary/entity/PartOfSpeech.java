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
        if (pos == null || pos.isEmpty()) {
            return OTHER;
        }

        String normalized = pos.toLowerCase().trim();

        for (PartOfSpeech p : PartOfSpeech.values()) {
            if (p.code.equalsIgnoreCase(normalized)) {
                return p;
            }
        }

        return OTHER;
    }
}
