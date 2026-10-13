package com.vocabularyai.admin.service;

import com.vocabularyai.admin.dto.response.StudentListResponse;
import com.vocabularyai.admin.dto.response.StudentDetailResponse;
import com.vocabularyai.admin.dto.response.StudentStatsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Admin Student Service Interface
 * 
 * Provides business logic for student management
 */
public interface AdminStudentService {
    
    /**
     * Get paginated list of all students
     * 
     * Supports:
     * - Pagination and sorting
     * - Basic student information
     * - Learning session count
     * - Subscription status
     * 
     * @param pageable Pagination parameters
     * @return Paginated list of students
     */
    Page<StudentListResponse> getStudentList(Pageable pageable);
    
    /**
     * Get paginated list of students filtered by status
     * 
     * @param status User status (ACTIVE, INACTIVE, BANNED)
     * @param pageable Pagination parameters
     * @return Paginated filtered list
     */
    Page<StudentListResponse> getStudentListByStatus(String status, Pageable pageable);
    
    /**
     * Search students by email or name
     * 
     * @param searchTerm Email or name fragment to search
     * @param pageable Pagination parameters
     * @return Paginated search results
     */
    Page<StudentListResponse> searchStudents(String searchTerm, Pageable pageable);
    
    /**
     * Get complete student information
     * 
     * Includes:
     * - User details
     * - Learning statistics
     * - Subscription information
     * - Payment history
     * 
     * @param userId Student user ID
     * @return Student detail with all stats
     * @throws IllegalArgumentException if student not found
     */
    StudentDetailResponse getStudentDetail(Long userId);
    
    /**
     * Get aggregate student statistics
     * 
     * Includes:
     * - Total student count by status
     * - Active subscriptions count
     * - Average metrics per student
     * - Activity statistics
     * - Churn rate
     * 
     * @return Student statistics summary
     */
    StudentStatsResponse getStudentStats();
}
