package com.vocabularyai.admin.service;

import com.vocabularyai.admin.dto.request.CreateInstructorRequest;
import com.vocabularyai.admin.dto.response.InstructorResponse;
import com.vocabularyai.admin.dto.response.StudentListResponse;
import com.vocabularyai.admin.dto.response.StudentDetailResponse;
import com.vocabularyai.admin.dto.response.StudentStatsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Admin Student Service Interface
 * 
 * Provides business logic for student and instructor management
 */
public interface AdminStudentService {
    
    /**
     * Get paginated list of all students (default role = STUDENT)
     */
    Page<StudentListResponse> getStudentList(Pageable pageable);

    /**
     * Get paginated list of users filtered by role (STUDENT, INSTRUCTOR)
     */
    Page<StudentListResponse> getStudentList(String role, Pageable pageable);
    
    /**
     * Get paginated list of students filtered by status
     */
    Page<StudentListResponse> getStudentListByStatus(String status, Pageable pageable);

    /**
     * Get paginated list of users filtered by role and status
     */
    Page<StudentListResponse> getStudentListByStatus(String role, String status, Pageable pageable);
    
    /**
     * Search students by email or name
     */
    Page<StudentListResponse> searchStudents(String searchTerm, Pageable pageable);

    /**
     * Search users by role and search term
     */
    Page<StudentListResponse> searchStudents(String role, String searchTerm, Pageable pageable);
    
    /**
     * Get complete student or instructor information
     */
    StudentDetailResponse getStudentDetail(Long userId);
    
    /**
     * Get aggregate student statistics (only role STUDENT)
     */
    StudentStatsResponse getStudentStats();

    /**
     * Create an instructor account directly by Admin
     */
    InstructorResponse createInstructor(CreateInstructorRequest request);
}
