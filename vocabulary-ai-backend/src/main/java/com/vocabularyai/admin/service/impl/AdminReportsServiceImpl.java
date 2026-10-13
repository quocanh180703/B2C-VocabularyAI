package com.vocabularyai.admin.service.impl;

import com.vocabularyai.admin.dto.response.*;
import com.vocabularyai.admin.repository.AdminReportsRepository;
import com.vocabularyai.admin.service.AdminReportsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin Reports Service Implementation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminReportsServiceImpl implements AdminReportsService {
    
    private final AdminReportsRepository adminReportsRepository;
    
    @Override
    public List<UserEngagementReportResponse> getUserEngagementReport() {
        log.debug("Fetching user engagement report");
        
        List<?> results = adminReportsRepository.getUserEngagementReport();
        
        return results.stream()
                .map(item -> {
                    Map<String, Object> resultMap = (Map<String, Object>) item;
                    return UserEngagementReportResponse.builder()
                            .date((LocalDateTime) resultMap.get("date"))
                            .activeUsers(resultMap.get("activeUsers") != null ? ((Number) resultMap.get("activeUsers")).longValue() : 0L)
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public LearningProgressReportResponse getLearningProgressReport() {
        log.debug("Fetching learning progress report");
        
        List<?> results = adminReportsRepository.getLearningProgressReport();
        
        if (results.isEmpty()) {
            return LearningProgressReportResponse.builder()
                    .totalAttempts(0L)
                    .avgScore(0.0)
                    .avgCompletionTime(0L)
                    .uniqueUsers(0L)
                    .build();
        }
        
        Map<String, Object> result = (Map<String, Object>) results.get(0);
        
        return LearningProgressReportResponse.builder()
                .totalAttempts(result.get("totalAttempts") != null ? ((Number) result.get("totalAttempts")).longValue() : 0L)
                .avgScore(result.get("avgScore") != null ? ((Number) result.get("avgScore")).doubleValue() : 0.0)
                .avgCompletionTime(result.get("avgCompletionTime") != null ? ((Number) result.get("avgCompletionTime")).longValue() : 0L)
                .uniqueUsers(result.get("uniqueUsers") != null ? ((Number) result.get("uniqueUsers")).longValue() : 0L)
                .build();
    }
    
    @Override
    public List<ContentPerformanceReportResponse> getContentPerformanceReport() {
        log.debug("Fetching content performance report");
        
        List<?> results = adminReportsRepository.getContentPerformanceReport();
        
        return results.stream()
                .map(item -> {
                    Map<String, Object> resultMap = (Map<String, Object>) item;
                    return ContentPerformanceReportResponse.builder()
                            .quizId(((Number) resultMap.get("quizId")).longValue())
                            .quizTitle((String) resultMap.get("quizTitle"))
                            .level((String) resultMap.get("level"))
                            .attemptCount(resultMap.get("attemptCount") != null ? ((Number) resultMap.get("attemptCount")).intValue() : 0)
                            .avgScore(resultMap.get("avgScore") != null ? ((Number) resultMap.get("avgScore")).doubleValue() : 0.0)
                            .completionRate(resultMap.get("completionRate") != null ? ((Number) resultMap.get("completionRate")).intValue() : 0)
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public RevenueReportResponse getRevenueReport() {
        log.debug("Fetching revenue report");
        
        List<?> results = adminReportsRepository.getRevenueReport();
        
        if (results.isEmpty()) {
            return RevenueReportResponse.builder()
                    .totalTransactions(0L)
                    .totalRevenue(BigDecimal.ZERO)
                    .avgTransactionValue(BigDecimal.ZERO)
                    .uniqueCustomers(0L)
                    .build();
        }
        
        Map<String, Object> result = (Map<String, Object>) results.get(0);
        
        return RevenueReportResponse.builder()
                .totalTransactions(result.get("totalTransactions") != null ? ((Number) result.get("totalTransactions")).longValue() : 0L)
                .totalRevenue(result.get("totalRevenue") != null ? new BigDecimal(((Number) result.get("totalRevenue")).toString()) : BigDecimal.ZERO)
                .avgTransactionValue(result.get("avgTransactionValue") != null ? new BigDecimal(((Number) result.get("avgTransactionValue")).toString()) : BigDecimal.ZERO)
                .uniqueCustomers(result.get("uniqueCustomers") != null ? ((Number) result.get("uniqueCustomers")).longValue() : 0L)
                .build();
    }
}
