package com.vocabularyai.speaking.service;

import com.vocabularyai.speaking.service.analysis.dto.SpeakingAnalysisResult;
import com.vocabularyai.speaking.dto.response.SpeakingProgressResponse;

/**
 * SpeakingProgressService Interface
 * 
 * Tích hợp Speaking Module với Learning Progress System
 * 
 * Chịu trách nhiệm:
 * - Cập nhật learning statistics sau khi speaking attempt hoàn thành
 * - Track speaking progress (total sessions, best scores, average scores)
 * - Tính toán speaking streaks và improvements
 * - Cập nhật user's overall learning progress
 * 
 * Integration với:
 * - SpeakingFeedbackService (lấy scores)
 * - SpeakingSessionService (lấy session stats)
 * - LearningProgressRepository (cập nhật vocabulary progress nếu có liên quan)
 */
public interface SpeakingProgressService {
    
    /**
     * Update user's speaking progress sau khi attempt hoàn thành
     * 
     * Flow:
     * 1. Lấy session info từ database
     * 2. Lấy feedback scores từ attempt mới nhất
     * 3. Cập nhật speaking statistics (total sessions, average scores, best scores, streaks)
     * 4. Cập nhật learning progress nếu có liên quan (e.g., vocabulary improvements)
     * 5. Calculate improvements so với phiên trước
     * 6. Lưu vào database
     * 
     * @param userId User ID
     * @param sessionId Speaking session ID
     * @param analysisResult AI analysis result từ SpeakingAIAnalysisService
     * @throws IllegalArgumentException nếu session không tồn tại hoặc user không sở hữu
     */
    void updateSpeakingProgress(Long userId, Long sessionId, SpeakingAnalysisResult analysisResult);
    
    /**
     * Lấy speaking progress tổng hợp của user
     * 
     * Bao gồm:
     * - Tổng số sessions đã hoàn thành
     * - Average scores (overall, grammar, vocabulary, fluency, content)
     * - Best scores (overall, grammar, vocabulary, fluency, content)
     * - Current streak (số ngày liên tiếp có speaking practice)
     * - Total time spent on speaking
     * - Improvements so với tuần trước
     * - Recent achievements/milestones
     * 
     * @param userId User ID
     * @return SpeakingProgressResponse chứa toàn bộ progress data
     * @throws IllegalArgumentException nếu user không tồn tại
     */
    SpeakingProgressResponse getSpeakingProgress(Long userId);
    
    /**
     * Calculate speaking score trend (improvement over time)
     * 
     * @param userId User ID
     * @param days Number of days to look back (e.g., 7 for last 7 days, 30 for last month)
     * @return Trend description (e.g., "Improving", "Stable", "Declining")
     */
    String calculateScoreTrend(Long userId, int days);
    
    /**
     * Get milestone achievements for speaking
     * 
     * Milestones:
     * - First session completed
     * - 10 sessions completed
     * - Average score > 80
     * - Best score > 90
     * - 7-day streak
     * - 30-day streak
     * 
     * @param userId User ID
     * @return List of achieved milestones
     */
    java.util.List<String> getMilestones(Long userId);
    
    /**
     * Reset speaking progress (admin use or user request)
     * 
     * @param userId User ID
     */
    void resetSpeakingProgress(Long userId);
}
