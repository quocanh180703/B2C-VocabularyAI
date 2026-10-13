package com.vocabularyai.admin.service;

import com.vocabularyai.admin.dto.response.SystemHealthResponse;
import com.vocabularyai.admin.dto.response.SystemOverviewResponse;
import com.vocabularyai.admin.dto.response.UserRegistrationTrendResponse;

import java.util.List;

/**
 * Admin System Service Interface
 * 
 * Defines operations for admin system management (dashboard, monitoring, etc.)
 */
public interface AdminSystemService {
    
    /**
     * Get system overview statistics
     * 
     * @return System overview data
     */
    SystemOverviewResponse getSystemOverview();
    
    /**
     * Get user registration trend for last 30 days
     * 
     * @return List of registration trend data points
     */
    List<UserRegistrationTrendResponse> getUserRegistrationTrend();
    
    /**
     * Get system health metrics
     * 
     * @return System health information
     */
    SystemHealthResponse getSystemHealth();
}
