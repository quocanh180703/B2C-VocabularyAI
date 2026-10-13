package com.vocabularyai.admin.service.impl;

import com.vocabularyai.admin.dto.response.SystemHealthResponse;
import com.vocabularyai.admin.dto.response.SystemOverviewResponse;
import com.vocabularyai.admin.dto.response.UserRegistrationTrendResponse;
import com.vocabularyai.admin.repository.AdminSystemRepository;
import com.vocabularyai.admin.service.AdminSystemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin System Service Implementation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminSystemServiceImpl implements AdminSystemService {
    
    private final AdminSystemRepository adminSystemRepository;
    
    @Override
    public SystemOverviewResponse getSystemOverview() {
        log.debug("Fetching system overview");
        
        List<?> results = adminSystemRepository.getSystemOverview();
        
        if (results.isEmpty()) {
            return SystemOverviewResponse.builder()
                    .totalUsers(0L)
                    .activeUsersToday(0L)
                    .totalSessions(0L)
                    .totalQuizzes(0L)
                    .totalVocabularies(0L)
                    .avgUserEngagement(0.0)
                    .build();
        }
        
        Map<String, Object> result = (Map<String, Object>) results.get(0);
        
        return SystemOverviewResponse.builder()
                .totalUsers(result.get("totalUsers") != null ? ((Number) result.get("totalUsers")).longValue() : 0L)
                .activeUsersToday(result.get("activeUsersToday") != null ? ((Number) result.get("activeUsersToday")).longValue() : 0L)
                .totalSessions(result.get("totalSessions") != null ? ((Number) result.get("totalSessions")).longValue() : 0L)
                .totalQuizzes(result.get("totalQuizzes") != null ? ((Number) result.get("totalQuizzes")).longValue() : 0L)
                .totalVocabularies(0L)
                .avgUserEngagement(0.0)
                .build();
    }
    
    @Override
    public List<UserRegistrationTrendResponse> getUserRegistrationTrend() {
        log.debug("Fetching user registration trend");
        
        List<?> results = adminSystemRepository.getUserRegistrationTrend();
        
        return results.stream()
                .map(item -> {
                    Map<String, Object> resultMap = (Map<String, Object>) item;
                    return UserRegistrationTrendResponse.builder()
                            .date((LocalDateTime) resultMap.get("date"))
                            .count(resultMap.get("count") != null ? ((Number) resultMap.get("count")).longValue() : 0L)
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public SystemHealthResponse getSystemHealth() {
        log.debug("Fetching system health");
        
        List<?> results = adminSystemRepository.getSystemHealth();
        
        if (results.isEmpty()) {
            return SystemHealthResponse.builder()
                    .totalUsers(0L)
                    .apiCallsToday(0L)
                    .averageResponseTime(0L)
                    .errorRate(0.0)
                    .status("HEALTHY")
                    .build();
        }
        
        Map<String, Object> result = (Map<String, Object>) results.get(0);
        
        long totalUsers = result.get("totalUsers") != null ? ((Number) result.get("totalUsers")).longValue() : 0L;
        long apiCalls = result.get("apiCallsToday") != null ? ((Number) result.get("apiCallsToday")).longValue() : 0L;
        long avgResponseTime = result.get("averageResponseTime") != null ? ((Number) result.get("averageResponseTime")).longValue() : 0L;
        double errorRate = result.get("errorRate") != null ? ((Number) result.get("errorRate")).doubleValue() : 0.0;
        
        // Determine health status
        String status;
        if (errorRate > 5.0) {
            status = "CRITICAL";
        } else if (errorRate > 2.0) {
            status = "WARNING";
        } else if (avgResponseTime > 1000) {
            status = "DEGRADED";
        } else {
            status = "HEALTHY";
        }
        
        return SystemHealthResponse.builder()
                .totalUsers(totalUsers)
                .apiCallsToday(apiCalls)
                .averageResponseTime(avgResponseTime)
                .errorRate(errorRate)
                .status(status)
                .build();
    }
}
