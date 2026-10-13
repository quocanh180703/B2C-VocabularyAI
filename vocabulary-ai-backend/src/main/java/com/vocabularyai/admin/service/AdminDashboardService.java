package com.vocabularyai.admin.service;

import com.vocabularyai.admin.dto.response.DashboardSummaryResponse;
import com.vocabularyai.admin.dto.response.DashboardRevenueResponse;
import com.vocabularyai.admin.dto.response.TopStudentResponse;
import com.vocabularyai.admin.dto.response.ActivityTrendResponse;

import java.util.List;

/**
 * Admin Dashboard Service Interface
 * 
 * Provides business logic for dashboard metrics and analytics
 */
public interface AdminDashboardService {
    
    /**
     * Get dashboard summary with key metrics
     * 
     * Includes:
     * - Total users
     * - Active subscriptions
     * - Total revenue
     * - Today's learning sessions
     * - System status
     * 
     * @return Dashboard summary
     */
    DashboardSummaryResponse getDashboardSummary();
    
    /**
     * Get detailed revenue metrics
     * 
     * Includes:
     * - Total revenue
     * - Today's revenue
     * - This month's revenue
     * - Payment counts
     * - Most popular package
     * 
     * @return Revenue details
     */
    DashboardRevenueResponse getRevenueMetrics();
    
    /**
     * Get top performing students
     * 
     * Rankings by:
     * - Session count
     * - Words learned
     * - Accuracy rate
     * 
     * @param limit Number of top students (e.g., 10)
     * @return List of top students
     */
    List<TopStudentResponse> getTopStudents(int limit);
    
    /**
     * Get activity trends for last 30 days
     * 
     * Time-series data:
     * - Daily learning sessions
     * - Daily active users
     * - Daily revenue
     * - Daily payment counts
     * 
     * @return Activity trends
     */
    ActivityTrendResponse getActivityTrend();
}
