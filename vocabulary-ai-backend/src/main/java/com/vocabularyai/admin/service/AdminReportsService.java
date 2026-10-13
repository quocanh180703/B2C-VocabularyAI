package com.vocabularyai.admin.service;

import com.vocabularyai.admin.dto.response.*;

import java.util.List;

/**
 * Admin Reports Service Interface
 * 
 * Defines operations for reporting and analytics
 */
public interface AdminReportsService {
    
    /**
     * Get user engagement report
     * 
     * @return List of daily engagement data
     */
    List<UserEngagementReportResponse> getUserEngagementReport();
    
    /**
     * Get learning progress report
     * 
     * @return Learning progress statistics
     */
    LearningProgressReportResponse getLearningProgressReport();
    
    /**
     * Get content performance report
     * 
     * @return List of content performance data
     */
    List<ContentPerformanceReportResponse> getContentPerformanceReport();
    
    /**
     * Get revenue report
     * 
     * @return Revenue statistics
     */
    RevenueReportResponse getRevenueReport();
}
