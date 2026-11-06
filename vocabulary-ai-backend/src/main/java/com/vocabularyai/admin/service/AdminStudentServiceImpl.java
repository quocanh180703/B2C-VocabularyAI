package com.vocabularyai.admin.service;

import com.vocabularyai.admin.dto.request.CreateInstructorRequest;
import com.vocabularyai.admin.dto.response.InstructorResponse;
import com.vocabularyai.admin.dto.response.StudentListResponse;
import com.vocabularyai.admin.dto.response.StudentDetailResponse;
import com.vocabularyai.admin.dto.response.StudentStatsResponse;
import com.vocabularyai.admin.repository.AdminStudentRepository;
import com.vocabularyai.common.exception.DuplicateResourceException;
import com.vocabularyai.user.entity.AuthProvider;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin Student Service Implementation
 * 
 * Transforms repository data to DTOs and implements business logic for students & instructors
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminStudentServiceImpl implements AdminStudentService {
    
    private final AdminStudentRepository studentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    private User.UserRole parseRole(String role) {
        if (role == null || role.isBlank()) {
            return User.UserRole.STUDENT;
        }
        try {
            return User.UserRole.valueOf(role.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return User.UserRole.STUDENT;
        }
    }

    private User.UserStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return User.UserStatus.ACTIVE;
        }
        try {
            return User.UserStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return User.UserStatus.ACTIVE;
        }
    }

    @Override
    public Page<StudentListResponse> getStudentList(Pageable pageable) {
        return getStudentList(null, pageable);
    }
    
    @Override
    public Page<StudentListResponse> getStudentList(String role, Pageable pageable) {
        User.UserRole userRole = parseRole(role);
        log.info("Fetching student list with pagination for role: {}", userRole);
        
        try {
            Page<?> studentPage = studentRepository.getStudentListByRole(userRole, pageable);
            
            List<StudentListResponse> students = studentPage.getContent().stream()
                .map(this::mapToStudentListResponse)
                .collect(Collectors.toList());
            
            log.debug("Student list fetched: {} items, page {}/{}", 
                students.size(), pageable.getPageNumber(), studentPage.getTotalPages());
            
            return new PageImpl<>(students, pageable, studentPage.getTotalElements());
            
        } catch (Exception e) {
            log.error("Error fetching student list", e);
            throw new RuntimeException("Failed to fetch student list: " + e.getMessage());
        }
    }
    
    @Override
    public Page<StudentListResponse> getStudentListByStatus(String status, Pageable pageable) {
        return getStudentListByStatus(null, status, pageable);
    }

    @Override
    public Page<StudentListResponse> getStudentListByStatus(String role, String status, Pageable pageable) {
        User.UserRole userRole = parseRole(role);
        User.UserStatus userStatus = parseStatus(status);
        log.info("Fetching student list by role: {}, status: {}", userRole, userStatus);
        
        try {
            Page<?> studentPage = studentRepository.getStudentListByRoleAndStatus(userRole, userStatus, pageable);
            
            List<StudentListResponse> students = studentPage.getContent().stream()
                .map(this::mapToStudentListResponse)
                .collect(Collectors.toList());
            
            log.debug("Student list by status fetched: {} items", students.size());
            
            return new PageImpl<>(students, pageable, studentPage.getTotalElements());
            
        } catch (Exception e) {
            log.error("Error fetching student list by status", e);
            throw new RuntimeException("Failed to fetch student list by status: " + e.getMessage());
        }
    }
    
    @Override
    public Page<StudentListResponse> searchStudents(String searchTerm, Pageable pageable) {
        return searchStudents(null, searchTerm, pageable);
    }

    @Override
    public Page<StudentListResponse> searchStudents(String role, String searchTerm, Pageable pageable) {
        User.UserRole userRole = parseRole(role);
        log.info("Searching students for role: {}, term: {}", userRole, searchTerm);
        
        try {
            Pageable unsorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
            Page<?> searchResult = studentRepository.searchStudentsByRole(userRole, searchTerm, unsorted);
            
            List<StudentListResponse> students = searchResult.getContent().stream()
                .map(this::mapToStudentListResponse)
                .collect(Collectors.toList());
            
            log.debug("Students found: {} results", students.size());
            
            return new PageImpl<>(students, pageable, searchResult.getTotalElements());
            
        } catch (Exception e) {
            log.error("Error searching students", e);
            throw new RuntimeException("Failed to search students: " + e.getMessage());
        }
    }
    
    @Override
    public StudentDetailResponse getStudentDetail(Long userId) {
        log.info("Fetching student detail: userId={}", userId);
        
        try {
            List<?> studentList = studentRepository.getStudentDetail(userId);
            
            if (studentList.isEmpty()) {
                log.warn("Student not found: userId={}", userId);
                throw new IllegalArgumentException("Student not found: " + userId);
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> studentData = (Map<String, Object>) studentList.get(0);
            
            // Get payment info
            List<?> paymentList = studentRepository.getStudentPaymentInfo(userId);
            BigDecimal totalPaid = BigDecimal.ZERO;
            long paymentCount = 0;
            LocalDateTime lastPaymentAt = null;
            
            if (!paymentList.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> paymentData = (Map<String, Object>) paymentList.get(0);
                totalPaid = (BigDecimal) paymentData.getOrDefault("totalPaid", BigDecimal.ZERO);
                paymentCount = ((Number) paymentData.getOrDefault("paymentCount", 0)).longValue();
                lastPaymentAt = (LocalDateTime) paymentData.get("lastPaymentAt");
            }
            
            // Calculate remaining days
            LocalDateTime subscriptionEndAt = (LocalDateTime) studentData.get("subscriptionEndAt");
            long remainingDays = 0;
            if (subscriptionEndAt != null) {
                remainingDays = Math.max(0, ChronoUnit.DAYS.between(LocalDateTime.now(), subscriptionEndAt));
            }
            
            StudentDetailResponse response = StudentDetailResponse.builder()
                .userId(((Number) studentData.get("userId")).longValue())
                .username((String) studentData.get("username"))
                .email((String) studentData.get("email"))
                .fullName((String) studentData.get("fullName"))
                .role(studentData.get("role") != null ? studentData.get("role").toString() : null)
                .avatarUrl((String) studentData.get("avatarUrl"))
                .status(studentData.get("status") != null ? studentData.get("status").toString() : null)
                .createdAt((LocalDateTime) studentData.get("createdAt"))
                .updatedAt((LocalDateTime) studentData.get("updatedAt"))
                .totalSessions(((Number) studentData.get("totalSessions")).longValue())
                .wordsLearned(((Number) studentData.get("wordsLearned")).longValue())
                .accuracyRate(((Number) studentData.getOrDefault("accuracyRate", 0)).doubleValue())
                .lastActivityAt((LocalDateTime) studentData.get("lastActivityAt"))
                .hasActiveSubscription(((Number) studentData.get("activeSubscriptionId")).longValue() > 0)
                .activeSubscriptionId(((Number) studentData.get("activeSubscriptionId")).longValue() > 0 
                    ? ((Number) studentData.get("activeSubscriptionId")).longValue() : null)
                .packageName((String) studentData.get("packageName"))
                .subscriptionStartAt((LocalDateTime) studentData.get("subscriptionStartAt"))
                .subscriptionEndAt(subscriptionEndAt)
                .remainingDays(remainingDays)
                .totalPaid(totalPaid)
                .paymentCount(paymentCount)
                .lastPaymentAt(lastPaymentAt)
                .build();
            
            log.debug("Student detail fetched: userId={}, email={}", response.getUserId(), response.getEmail());
            
            return response;
            
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching student detail", e);
            throw new RuntimeException("Failed to fetch student detail: " + e.getMessage());
        }
    }
    
    @Override
    public StudentStatsResponse getStudentStats() {
        log.info("Fetching student statistics");
        
        try {
            // Get counts by status
            List<?> statusCounts = studentRepository.getStudentCountByStatus();
            long totalStudents = 0;
            long activeStudents = 0;
            long inactiveStudents = 0;
            long bannedStudents = 0;
            
            for (Object item : statusCounts) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) item;
                String status = data.get("status") != null ? data.get("status").toString() : "";
                long count = ((Number) data.get("count")).longValue();
                
                switch (status) {
                    case "ACTIVE":
                        activeStudents = count;
                        break;
                    case "INACTIVE":
                        inactiveStudents = count;
                        break;
                    case "BANNED":
                        bannedStudents = count;
                        break;
                }
                totalStudents += count;
            }
            
            // Get other statistics
            long activeSubscriptions = studentRepository.countActiveSubscriptions();
            double avgSessions = studentRepository.getAvgSessionsPerStudent();
            long avgWords = studentRepository.getAvgWordsPerStudent();
            double avgAccuracy = studentRepository.getAvgAccuracyRate();
            long activeTodayCount = studentRepository.countActiveTodayStudents();
            long newStudentsThisMonth = studentRepository.countNewStudentsThisMonth();
            
            // Calculate churn rate (students without subscriptions / total)
            double churnRate = totalStudents > 0 
                ? ((double)(totalStudents - activeSubscriptions) / totalStudents) * 100.0 
                : 0.0;
            
            log.debug("Student stats fetched: total={}, active={}, avgSessions={}", 
                totalStudents, activeStudents, avgSessions);
            
            return StudentStatsResponse.builder()
                .totalStudents(totalStudents)
                .activeStudents(activeStudents)
                .inactiveStudents(inactiveStudents)
                .bannedStudents(bannedStudents)
                .avgSessionsPerStudent(avgSessions)
                .avgWordsPerStudent(avgWords)
                .avgAccuracyRate(avgAccuracy)
                .activeSubscriptionsToday(activeSubscriptions)
                .activeTodayCount(activeTodayCount)
                .newStudentsThisMonth(newStudentsThisMonth)
                .churnRate(churnRate)
                .build();
            
        } catch (Exception e) {
            log.error("Error fetching student statistics", e);
            throw new RuntimeException("Failed to fetch student statistics: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public InstructorResponse createInstructor(CreateInstructorRequest request) {
        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase();
        log.info("Creating instructor: username={}, email={}", username, email);
        
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Tên đăng nhập đã tồn tại: " + username);
        }
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email đã tồn tại: " + email);
        }
        
        User instructor = User.builder()
            .username(username)
            .email(email)
            .fullName(request.getFullName().trim())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(User.UserRole.INSTRUCTOR)
            .status(User.UserStatus.ACTIVE)
            .provider(AuthProvider.LOCAL)
            .build();
            
        User saved = userRepository.save(instructor);
        log.info("Instructor created successfully: id={}, username={}", saved.getId(), saved.getUsername());
        
        return InstructorResponse.builder()
            .id(saved.getId())
            .userId(saved.getId())
            .username(saved.getUsername())
            .email(saved.getEmail())
            .fullName(saved.getFullName())
            .role(saved.getRole().name())
            .status(saved.getStatus().name())
            .createdAt(saved.getCreatedAt())
            .build();
    }
    
    /**
     * Map raw query result to StudentListResponse DTO
     */
    private StudentListResponse mapToStudentListResponse(Object item) {
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) item;
        
        return StudentListResponse.builder()
            .userId(((Number) data.get("userId")).longValue())
            .username((String) data.get("username"))
            .email((String) data.get("email"))
            .fullName((String) data.get("fullName"))
            .role(data.get("role") != null ? data.get("role").toString() : null)
            .status(data.get("status") != null ? data.get("status").toString() : null)
            .hasActiveSubscription((Boolean) data.getOrDefault("hasActiveSubscription", false))
            .totalSessions(((Number) data.getOrDefault("totalSessions", 0)).longValue())
            .createdAt((LocalDateTime) data.get("createdAt"))
            .lastLoginAt((LocalDateTime) data.get("lastLoginAt"))
            .build();
    }
}
