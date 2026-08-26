package com.vocabularyai.speaking.entity;

/**
 * Speaking Session Status Enum
 * 
 * Trạng thái của một phiên luyện nói
 */
public enum SpeakingStatus {
    /**
     * Phiên đang diễn ra
     */
    IN_PROGRESS("Đang luyện tập"),
    
    /**
     * Phiên hoàn thành
     */
    COMPLETED("Hoàn thành"),
    
    /**
     * Phiên bị hủy
     */
    CANCELLED("Bị hủy");
    
    private final String displayName;
    
    SpeakingStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
