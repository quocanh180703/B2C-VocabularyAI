package com.vocabularyai.speaking.service.stt;

import org.springframework.web.multipart.MultipartFile;

/**
 * SpeechToTextService Interface
 * 
 * Abstraction cho việc chuyển đổi audio thành text (Speech-to-Text / ASR)
 * 
 * Hỗ trợ multiple providers:
 * - Google Cloud Speech-to-Text (via REST API)
 * - Azure Cognitive Services Speech-to-Text
 * - AWS Transcribe
 * - Custom provider (tương lai)
 * 
 * Design Pattern:
 * - Provider-agnostic interface (không phụ thuộc vào provider cụ thể)
 * - Cho phép switch provider thông qua config
 * - Consistent output format (TranscriptResult)
 * 
 * Implementation phải đảm bảo:
 * - Audio file validation
 * - Error handling (timeout, provider error)
 * - Transcript validation
 * - Logging + metrics
 */
public interface SpeechToTextService {
    
    /**
     * Chuyển audio file thành transcript
     * 
     * Flow:
     * 1. Validate audio file
     * 2. Call provider API (Google Cloud Speech-to-Text / Azure / etc)
     * 3. Parse response
     * 4. Validate transcript
     * 5. Return TranscriptResult
     * 
     * @param audioFile MultipartFile audio được upload
     * @return TranscriptResult chứa transcript + confidence + metadata
     * @throws SpeechToTextException nếu transcription thất bại
     * @throws IllegalArgumentException nếu audio file không hợp lệ
     */
    TranscriptResult transcribe(MultipartFile audioFile);
    
    /**
     * Chuyển audio file thành transcript với language code tùy chỉnh
     * 
     * @param audioFile MultipartFile audio
     * @param languageCode Language code (e.g., en-US, vi-VN)
     * @return TranscriptResult
     * @throws SpeechToTextException nếu transcription thất bại
     */
    TranscriptResult transcribe(MultipartFile audioFile, String languageCode);
    
    /**
     * Lấy provider type hiện tại
     * 
     * @return Provider name (google, azure, aws, custom)
     */
    String getProviderType();
    
    /**
     * Check xem service có sẵn sàng không
     * 
     * Dùng để health check, kiểm tra credentials, etc
     * 
     * @return true nếu service ready, false nếu có issue
     */
    boolean isHealthy();
}
