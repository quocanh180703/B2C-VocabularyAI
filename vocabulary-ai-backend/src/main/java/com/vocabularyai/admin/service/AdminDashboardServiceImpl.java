package com.vocabularyai.admin.service;

import com.vocabularyai.admin.dto.response.DashboardSummaryResponse;
import com.vocabularyai.admin.dto.response.DashboardRevenueResponse;
import com.vocabularyai.admin.dto.response.TopStudentResponse;
import com.vocabularyai.admin.dto.response.ActivityTrendResponse;
import com.vocabularyai.admin.repository.AdminDashboardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin Dashboard Service Implementation
 * 
 * Fetches metrics from repository and transforms to DTOs
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardServiceImpl implements AdminDashboardService {
    
    private final AdminDashboardRepository dashboardRepository;
    
    private static final int TOP_STUDENTS_LIMIT = 5;
    private static final int TREND_DAYS = 30;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    @Override
    public DashboardSummaryResponse getDashboardSummary() {
        log.info("Fetching dashboard summary");
        
        try {
            long totalUsers = dashboardRepository.countTotalStudents();
            long activeSubscriptions = dashboardRepository.countActiveSubscriptions();
            BigDecimal totalRevenue = dashboardRepository.getTotalRevenue();
            long activeLearningToday = dashboardRepository.countActiveLearningSessionsToday();
            
            log.debug("Dashboard summary fetched: users={}, subscriptions={}, revenue={}, learning={}",
                totalUsers, activeSubscriptions, totalRevenue, activeLearningToday);
            
            return DashboardSummaryResponse.builder()
                .totalUsers(totalUsers)
                .activeSubscriptions(activeSubscriptions)
                .totalRevenue(totalRevenue.longValue())
                .activeLearningSessionsToday(activeLearningToday)
                .systemStatus("OK")
                .lastUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))
                .build();
                
        } catch (Exception e) {
            log.error("Error fetching dashboard summary", e);
            throw new RuntimeException("Failed to fetch dashboard summary: " + e.getMessage());
        }
    }
    
    @Override
    public DashboardRevenueResponse getRevenueMetrics() {
        log.info("Fetching revenue metrics");
        
        try {
            BigDecimal totalRevenue = dashboardRepository.getTotalRevenue();
            BigDecimal revenueToday = dashboardRepository.getRevenueToday();
            BigDecimal revenueThisMonth = dashboardRepository.getRevenueThisMonth();
            long totalPayments = dashboardRepository.countTotalPayments();
            long paymentsToday = dashboardRepository.countPaymentsToday();
            BigDecimal averagePayment = dashboardRepository.getAveragePaymentAmount();
            
            // Get most popular package
            Long popularPackageId = null;
            String popularPackageName = null;
            List<?> popularPackageList = dashboardRepository.getMostPopularPackage();
            if (!popularPackageList.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> packageData = (Map<String, Object>) popularPackageList.get(0);
                popularPackageId = ((Number) packageData.get("packageId")).longValue();
                popularPackageName = (String) packageData.get("packageName");
            }
            
            log.debug("Revenue metrics fetched: total={}, today={}, thisMonth={}, avgPayment={}",
                totalRevenue, revenueToday, revenueThisMonth, averagePayment);
            
            return DashboardRevenueResponse.builder()
                .totalRevenue(totalRevenue)
                .revenueToday(revenueToday)
                .revenueThisMonth(revenueThisMonth)
                .totalPayments(totalPayments)
                .paymentsToday(paymentsToday)
                .averagePaymentAmount(averagePayment)
                .popularPackageId(popularPackageId)
                .popularPackageName(popularPackageName)
                .build();
                
        } catch (Exception e) {
            log.error("Error fetching revenue metrics", e);
            throw new RuntimeException("Failed to fetch revenue metrics: " + e.getMessage());
        }
    }
    
    @Override
    public List<TopStudentResponse> getTopStudents(int limit) {
        log.info("Fetching top {} students", limit);
        
        try {
            List<TopStudentResponse> topStudents = new ArrayList<>();
            
            // Get top students by session count
            List<?> topBySessionList = dashboardRepository.getTopStudentsBySessionCount(limit);
            
            int rank = 1;
            for (Object item : topBySessionList) {
                @SuppressWarnings("unchecked")
                Map<String, Object> studentData = (Map<String, Object>) item;
                
                Long userId = ((Number) studentData.get("userId")).longValue();
                String fullName = (String) studentData.get("fullName");
                String email = (String) studentData.get("email");
                long totalSessions = ((Number) studentData.get("totalSessions")).longValue();
                
                TopStudentResponse student = TopStudentResponse.builder()
                    .userId(userId)
                    .fullName(fullName)
                    .email(email)
                    .totalSessions(totalSessions)
                    .rank(rank++)
                    .build();
                
                topStudents.add(student);
            }
            
            log.debug("Top students fetched: count={}", topStudents.size());
            return topStudents;
            
        } catch (Exception e) {
            log.error("Error fetching top students", e);
            throw new RuntimeException("Failed to fetch top students: " + e.getMessage());
        }
    }
    
    @Override
    public ActivityTrendResponse getActivityTrend() {
        log.info("Fetching activity trends for last {} days", TREND_DAYS);
        
        try {
            List<String> dates = new ArrayList<>();
            List<Long> learningSessions = new ArrayList<>();
            List<Long> activeUsers = new ArrayList<>();
            List<Long> dailyRevenue = new ArrayList<>();
            List<Long> paymentCounts = new ArrayList<>();
            
            // Get daily learning sessions
            List<?> sessionList = dashboardRepository.getDailyLearningSessions(TREND_DAYS);
            Map<String, Long> sessionMap = new java.util.HashMap<>();
            for (Object item : sessionList) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) item;
                String date = data.get("date").toString();
                Long count = ((Number) data.get("sessionCount")).longValue();
                sessionMap.put(date, count);
            }
            
            // Get daily active users
            List<?> activeUsersList = dashboardRepository.getDailyActiveUsers(TREND_DAYS);
            Map<String, Long> activeUsersMap = new java.util.HashMap<>();
            for (Object item : activeUsersList) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) item;
                String date = data.get("date").toString();
                Long count = ((Number) data.get("activeUsers")).longValue();
                activeUsersMap.put(date, count);
            }
            
            // Get daily revenue
            List<?> revenueList = dashboardRepository.getDailyRevenue(TREND_DAYS);
            Map<String, Long> revenueMap = new java.util.HashMap<>();
            for (Object item : revenueList) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) item;
                String date = data.get("date").toString();
                Long revenue = ((Number) data.get("dailyRevenue")).longValue();
                revenueMap.put(date, revenue);
            }
            
            // Get daily payment counts
            List<?> paymentList = dashboardRepository.getDailyPaymentCounts(TREND_DAYS);
            Map<String, Long> paymentMap = new java.util.HashMap<>();
            for (Object item : paymentList) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) item;
                String date = data.get("date").toString();
                Long count = ((Number) data.get("paymentCount")).longValue();
                paymentMap.put(date, count);
            }
            
            // Generate date range
            LocalDate today = LocalDate.now();
            for (int i = TREND_DAYS - 1; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                String dateStr = date.format(DATE_FORMATTER);
                
                dates.add(dateStr);
                learningSessions.add(sessionMap.getOrDefault(dateStr, 0L));
                activeUsers.add(activeUsersMap.getOrDefault(dateStr, 0L));
                dailyRevenue.add(revenueMap.getOrDefault(dateStr, 0L));
                paymentCounts.add(paymentMap.getOrDefault(dateStr, 0L));
            }
            
            log.debug("Activity trends fetched: {} days", dates.size());
            
            return ActivityTrendResponse.builder()
                .dates(dates)
                .learningSessions(learningSessions)
                .activeUsers(activeUsers)
                .dailyRevenue(dailyRevenue)
                .paymentCounts(paymentCounts)
                .period("Last " + TREND_DAYS + " days")
                .build();
                
        } catch (Exception e) {
            log.error("Error fetching activity trends", e);
            throw new RuntimeException("Failed to fetch activity trends: " + e.getMessage());
        }
    }
}
