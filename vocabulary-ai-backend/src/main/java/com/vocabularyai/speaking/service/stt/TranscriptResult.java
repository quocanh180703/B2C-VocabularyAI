package com.vocabularyai.speaking.service.stt;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TranscriptResult
 * 
 * Kết quả từ Speech-to-Text service sau khi convert audio → text
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptResult {
    
    /**
     * Nội dung transcript (text được chuyển từ audio)
     * Ví dụ: "Hello, this is a test recording"
     */
    @JsonProperty("transcript")
    private String transcript;
    
    /**
     * Độ tin cậy của transcript (0-100)
     * Biểu thị độ chính xác của việc recognition
     * Ví dụ: 95.5 = 95.5% confidence
     */
    @JsonProperty("confidence")
    private Double confidence;
    
    /**
     * Language code được detect/sử dụng
     * Ví dụ: en-US, en-GB, vi-VN
     */
    @JsonProperty("languageCode")
    private String languageCode;
    
    /**
     * Duration của audio file (seconds)
     * Nếu provider cung cấp thông tin này
     */
    @JsonProperty("durationSeconds")
    private Integer durationSeconds;
    
    /**
     * Provider được sử dụng để transcribe
     * Ví dụ: google, azure, custom
     */
    @JsonProperty("provider")
    private String provider;
    
    /**
     * Danh sách các alternative transcriptions (nếu provider hỗ trợ)
     * Chứa nhiều giả thuyết transcription theo độ tin cậy
     */
    @JsonProperty("alternatives")
    private java.util.List<AlternativeTranscript> alternatives;
    
    /**
     * Alternative Transcript
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlternativeTranscript {
        private String transcript;
        private Double confidence;
    }
}
