package com.vocabularyai.admin.service;

import com.vocabularyai.admin.dto.response.SpeakingSessionListResponse;
import com.vocabularyai.admin.dto.response.SpeakingStatsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Admin Speaking Service Interface
 */
public interface AdminSpeakingService {
    
    /**
     * Get paginated list of all speaking sessions
     */
    Page<SpeakingSessionListResponse> getSpeakingSessions(Pageable pageable);
    
    /**
     * Get speaking sessions for specific user
     */
    Page<SpeakingSessionListResponse> getSpeakingSessionsByUser(Long userId, Pageable pageable);
    
    /**
     * Get speaking session detail
     */
    SpeakingSessionListResponse getSpeakingSessionDetail(Long sessionId);
    
    /**
     * Get system-wide speaking statistics
     */
    SpeakingStatsResponse getSpeakingStatistics();
}
