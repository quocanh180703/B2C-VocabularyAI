package com.vocabularyai.speaking.entity;

/**
 * Speaking Level Enum
 * 
 * Mức độ khó của phiên luyện nói
 * Dựa trên chuẩn CEFR (Common European Framework of Reference)
 */
public enum SpeakingLevel {
    A1("Sơ cấp 1"),
    A2("Sơ cấp 2"),
    B1("Trung cấp 1"),
    B2("Trung cấp 2"),
    C1("Cao cấp 1"),
    C2("Cao cấp 2");
    
    private final String displayName;
    
    SpeakingLevel(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
