package com.vocabularyai.vocabulary.entity;

/**
 * WordTopic Enumeration
 * 
 * Fixed 12 topics for CEFR vocabulary classification
 */
public enum WordTopic {
    WORK("Công việc & Văn phòng"),
    TRAVEL("Du lịch & Di chuyển"),
    ACADEMIC("Học thuật & Giáo dục"),
    TECHNOLOGY("Công nghệ"),
    HEALTH("Sức khỏe"),
    FOOD("Ăn uống"),
    MONEY("Mua sắm & Tiền bạc"),
    FAMILY("Gia đình & Bạn bè"),
    SPORTS("Thể thao & Giải trí"),
    ENVIRONMENT("Môi trường"),
    SOCIETY("Xã hội & Pháp luật"),
    ARTS("Nghệ thuật & Văn hóa");

    private final String displayName;

    WordTopic(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Safely parse a topic string (case-insensitive)
     * 
     * @param topic Code string e.g. "WORK", "work"
     * @return WordTopic or null if invalid/null
     */
    public static WordTopic fromString(String topic) {
        if (topic == null || topic.trim().isEmpty()) {
            return null;
        }
        try {
            return WordTopic.valueOf(topic.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
