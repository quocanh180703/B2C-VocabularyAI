package com.vocabularyai.admin.service;

import com.vocabularyai.admin.dto.response.StudentListResponse;
import com.vocabularyai.admin.dto.response.StudentDetailResponse;
import com.vocabularyai.admin.dto.response.StudentStatsResponse;
import com.vocabularyai.admin.repository.AdminStudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin Student Service Implementation
 * 
 * Transforms repository data to DTOs and implements business logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminStudentServiceImpl implements AdminStudentService {
    
    private final AdminStudentRepository studentRepository;
    
    @Override
    public Page<StudentListResponse> getStudentList(Pageable pageable) {
        log.info("Fetching student list with pagination");
        
        try {
            Page<?> studentPage = studentRepository.getStudentList(pageable);
            
            List<StudentListResponse> students = studentPage.getContent().stream()
                .map(item -> mapToStudentListResponse(item))
                .collect(Collectors.toList());
            
            log.debug("Student list fetched: {} students, page {}/{}", 
                students.size(), pageable.getPageNumber(), studentPage.getTotalPages());
            
            return new PageImpl<>(students, pageable, studentPage.getTotalElements());
            
        } catch (Exception e) {
            log.error("Error fetching student list", e);
            throw new RuntimeException("Failed to fetch student list: " + e.getMessage());
        }
    }
    
    @Override
    public Page<StudentListResponse> getStudentListByStatus(String status, Pageable pageable) {
        log.info("Fetching student list by status: {}", status);
        
        try {
            Page<?> studentPage = studentRepository.getStudentListByStatus(status, pageable);
            
            List<StudentListResponse> students = studentPage.getContent().stream()
                .map(item -> mapToStudentListResponse(item))
                .collect(Collectors.toList());
            
            log.debug("Student list by status fetched: {} students", students.size());
            
            return new PageImpl<>(students, pageable, studentPage.getTotalElements());
            
        } catch (Exception e) {
            log.error("Error fetching student list by status", e);
            throw new RuntimeException("Failed to fetch student list: " + e.getMessage());
        }
    }
    
    @Override
    public Page<StudentListResponse> searchStudents(String searchTerm, Pageable pageable) {
        log.info("Searching students with term: {}", searchTerm);
        
        try {
            Pageable unsorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
            Page<?> searchResult = studentRepository.searchStudents(searchTerm, unsorted);
            
            List<StudentListResponse> students = searchResult.getContent().stream()
                .map(item -> mapToStudentListResponse(item))
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
            .status(data.get("status") != null ? data.get("status").toString() : null)
            .hasActiveSubscription((Boolean) data.getOrDefault("hasActiveSubscription", false))
            .totalSessions(((Number) data.getOrDefault("totalSessions", 0)).longValue())
            .createdAt((LocalDateTime) data.get("createdAt"))
            .lastLoginAt((LocalDateTime) data.get("lastLoginAt"))
            .build();
    }
}
