package com.vocabularyai.payment.entity;

import lombok.Getter;

/**
 * FeatureType
 * Enum representing features tracked by daily freemium quotas.
 */
@Getter
public enum FeatureType {
    AI_CHAT("chat", 5, "Chat AI"),
    SPEAKING("speaking", 5, "Luyện nói & Chấm điểm"),
    GRAMMAR("grammar", 5, "Sửa ngữ pháp"),
    WORD_EXPLAIN("explain", 10, "AI giải thích từ & gợi ý"),
    ROADMAP("roadmap", 2, "Lộ trình học AI");

    private final String key;
    private final int defaultFreeLimit;
    private final String displayName;

    FeatureType(String key, int defaultFreeLimit, String displayName) {
        this.key = key;
        this.defaultFreeLimit = defaultFreeLimit;
        this.displayName = displayName;
    }

    public static FeatureType fromKey(String key) {
        for (FeatureType type : values()) {
            if (type.getKey().equalsIgnoreCase(key) || type.name().equalsIgnoreCase(key)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown feature type: " + key);
    }
}
