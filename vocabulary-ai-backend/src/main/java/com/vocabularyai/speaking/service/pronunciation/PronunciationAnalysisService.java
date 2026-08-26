package com.vocabularyai.speaking.service.pronunciation;

import com.vocabularyai.speaking.service.pronunciation.dto.PronunciationAnalysisResult;

/**
 * PronunciationAnalysisService
 * 
 * Dịch vụ phân tích phát âm cho bài nói
 * 
 * Đánh giá chất lượng phát âm dựa trên:
 * - Audio file và transcript
 * - Pronunciation clarity
 * - Stress và intonation (nếu provider hỗ trợ)
 * - Word-level issues nếu provider hỗ trợ
 * 
 * Abstraction này cho phép thay đổi provider trong tương lai:
 * - Gemini-based analysis (MVP)
 * - Google Cloud Speech-to-Text pronunciation assessment
 * - Azure Speech Services pronunciation assessment
 * - Chuyên dụng pronunciation provider
 */
public interface PronunciationAnalysisService {
    
    /**
     * Phân tích phát âm từ audio và transcript
     * 
     * @param audioFilePath Đường dẫn tới file audio
     * @param transcript Nội dung transcript từ Speech-to-Text
     * @param topic Chủ đề/context bài nói
     * @param level Trình độ tiếng Anh của user (A1-C2)
     * @param userId User ID (cho rate limiting)
     * @return PronunciationAnalysisResult chứa pronunciation score, feedback và word-level issues
     * @throws PronunciationAnalysisException nếu phân tích thất bại
     */
    PronunciationAnalysisResult analyzePronunciation(
            String audioFilePath,
            String transcript,
            String topic,
            String level,
            Long userId
    );
    
    /**
     * Check xem provider có hỗ trợ pronunciation assessment không
     * 
     * @return true nếu provider được enable, false nếu disabled/placeholder
     */
    boolean isProviderAvailable();
    
    /**
     * Lấy tên provider hiện tại
     * 
     * @return Provider name (e.g., "gemini", "google-cloud", "azure")
     */
    String getProviderName();
}
