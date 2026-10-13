package com.vocabularyai.admin.service;

import com.vocabularyai.admin.dto.response.SubscriptionListResponse;
import com.vocabularyai.admin.dto.response.SubscriptionDetailResponse;
import com.vocabularyai.admin.repository.AdminSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin Subscription Service Implementation
 * 
 * Transforms repository data to DTOs and implements business logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminSubscriptionServiceImpl implements AdminSubscriptionService {
    
    private final AdminSubscriptionRepository subscriptionRepository;
    
    @Override
    public Page<SubscriptionListResponse> getSubscriptionList(Pageable pageable) {
        log.info("Fetching subscription list with pagination");
        
        try {
            Page<?> subscriptionPage = subscriptionRepository.getSubscriptionList(pageable);
            
            List<SubscriptionListResponse> subscriptions = subscriptionPage.getContent().stream()
                .map(item -> mapToSubscriptionListResponse(item))
                .collect(Collectors.toList());
            
            log.debug("Subscription list fetched: {} subscriptions, page {}/{}", 
                subscriptions.size(), pageable.getPageNumber(), subscriptionPage.getTotalPages());
            
            return new PageImpl<>(subscriptions, pageable, subscriptionPage.getTotalElements());
            
        } catch (Exception e) {
            log.error("Error fetching subscription list", e);
            throw new RuntimeException("Failed to fetch subscription list: " + e.getMessage());
        }
    }
    
    @Override
    public Page<SubscriptionListResponse> getSubscriptionListByStatus(String status, Pageable pageable) {
        log.info("Fetching subscription list by status: {}", status);
        
        try {
            Page<?> subscriptionPage = subscriptionRepository.getSubscriptionListByStatus(status, pageable);
            
            List<SubscriptionListResponse> subscriptions = subscriptionPage.getContent().stream()
                .map(item -> mapToSubscriptionListResponse(item))
                .collect(Collectors.toList());
            
            log.debug("Subscription list by status fetched: {} subscriptions", subscriptions.size());
            
            return new PageImpl<>(subscriptions, pageable, subscriptionPage.getTotalElements());
            
        } catch (Exception e) {
            log.error("Error fetching subscription list by status", e);
            throw new RuntimeException("Failed to fetch subscription list: " + e.getMessage());
        }
    }
    
    @Override
    public Page<SubscriptionListResponse> getActiveSubscriptions(Pageable pageable) {
        log.info("Fetching active subscriptions");
        
        try {
            Page<?> subscriptionPage = subscriptionRepository.getActiveSubscriptions(pageable);
            
            List<SubscriptionListResponse> subscriptions = subscriptionPage.getContent().stream()
                .map(item -> mapToSubscriptionListResponse(item))
                .collect(Collectors.toList());
            
            log.debug("Active subscriptions fetched: {} subscriptions", subscriptions.size());
            
            return new PageImpl<>(subscriptions, pageable, subscriptionPage.getTotalElements());
            
        } catch (Exception e) {
            log.error("Error fetching active subscriptions", e);
            throw new RuntimeException("Failed to fetch active subscriptions: " + e.getMessage());
        }
    }
    
    @Override
    public Page<SubscriptionListResponse> getSubscriptionListByUser(Long userId, Pageable pageable) {
        log.info("Fetching subscription list for user: {}", userId);
        
        try {
            Page<?> subscriptionPage = subscriptionRepository.getSubscriptionListByUser(userId, pageable);
            
            List<SubscriptionListResponse> subscriptions = subscriptionPage.getContent().stream()
                .map(item -> mapToSubscriptionListResponse(item))
                .collect(Collectors.toList());
            
            log.debug("User subscription list fetched: {} subscriptions", subscriptions.size());
            
            return new PageImpl<>(subscriptions, pageable, subscriptionPage.getTotalElements());
            
        } catch (Exception e) {
            log.error("Error fetching user subscription list", e);
            throw new RuntimeException("Failed to fetch user subscription list: " + e.getMessage());
        }
    }
    
    @Override
    public Page<SubscriptionListResponse> searchSubscriptions(String searchTerm, Pageable pageable) {
        log.info("Searching subscriptions with term: {}", searchTerm);
        
        try {
            Page<?> searchResult = subscriptionRepository.searchSubscriptions(searchTerm, pageable);
            
            List<SubscriptionListResponse> subscriptions = searchResult.getContent().stream()
                .map(item -> mapToSubscriptionListResponse(item))
                .collect(Collectors.toList());
            
            log.debug("Subscriptions found: {} results", subscriptions.size());
            
            return new PageImpl<>(subscriptions, pageable, searchResult.getTotalElements());
            
        } catch (Exception e) {
            log.error("Error searching subscriptions", e);
            throw new RuntimeException("Failed to search subscriptions: " + e.getMessage());
        }
    }
    
    @Override
    public SubscriptionDetailResponse getSubscriptionDetail(Long subscriptionId) {
        log.info("Fetching subscription detail: subscriptionId={}", subscriptionId);
        
        try {
            List<?> subscriptionList = subscriptionRepository.getSubscriptionDetail(subscriptionId);
            
            if (subscriptionList.isEmpty()) {
                log.warn("Subscription not found: subscriptionId={}", subscriptionId);
                throw new IllegalArgumentException("Subscription not found: " + subscriptionId);
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> subscriptionData = (Map<String, Object>) subscriptionList.get(0);
            
            // Calculate remaining days
            LocalDateTime endAt = (LocalDateTime) subscriptionData.get("endAt");
            long remainingDays = 0;
            boolean isActive = false;
            if (endAt != null) {
                remainingDays = Math.max(0, ChronoUnit.DAYS.between(LocalDateTime.now(), endAt));
                isActive = remainingDays > 0 && "ACTIVE".equals(subscriptionData.get("status"));
            }
            
            SubscriptionDetailResponse response = SubscriptionDetailResponse.builder()
                .subscriptionId(((Number) subscriptionData.get("subscriptionId")).longValue())
                .status((String) subscriptionData.get("status"))
                .startAt((LocalDateTime) subscriptionData.get("startAt"))
                .endAt(endAt)
                .remainingDays(remainingDays)
                .isActive(isActive)
                .createdAt((LocalDateTime) subscriptionData.get("createdAt"))
                .userId(((Number) subscriptionData.get("userId")).longValue())
                .username((String) subscriptionData.get("username"))
                .userEmail((String) subscriptionData.get("userEmail"))
                .userFullName((String) subscriptionData.get("userFullName"))
                .packageId(((Number) subscriptionData.get("packageId")).longValue())
                .packageName((String) subscriptionData.get("packageName"))
                .packageCode((String) subscriptionData.get("packageCode"))
                .packagePrice((BigDecimal) subscriptionData.get("packagePrice"))
                .packageDurationDays(((Number) subscriptionData.get("packageDurationDays")).intValue())
                .packageDescription((String) subscriptionData.get("packageDescription"))
                .paymentId(subscriptionData.get("paymentId") != null 
                    ? ((Number) subscriptionData.get("paymentId")).longValue() : null)
                .paymentTransactionCode((String) subscriptionData.get("paymentTransactionCode"))
                .paymentAmount((BigDecimal) subscriptionData.get("paymentAmount"))
                .paymentStatus((String) subscriptionData.get("paymentStatus"))
                .paymentPaidAt((LocalDateTime) subscriptionData.get("paymentPaidAt"))
                .build();
            
            log.debug("Subscription detail fetched: subscriptionId={}, userId={}", 
                response.getSubscriptionId(), response.getUserId());
            
            return response;
            
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching subscription detail", e);
            throw new RuntimeException("Failed to fetch subscription detail: " + e.getMessage());
        }
    }
    
    /**
     * Map raw query result to SubscriptionListResponse DTO
     */
    private SubscriptionListResponse mapToSubscriptionListResponse(Object item) {
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) item;
        
        LocalDateTime endAt = (LocalDateTime) data.get("endAt");
        long remainingDays = 0;
        boolean isActive = false;
        if (endAt != null) {
            remainingDays = Math.max(0, ChronoUnit.DAYS.between(LocalDateTime.now(), endAt));
            isActive = remainingDays > 0 && "ACTIVE".equals(data.get("status"));
        }
        
        return SubscriptionListResponse.builder()
            .subscriptionId(((Number) data.get("subscriptionId")).longValue())
            .userId(((Number) data.get("userId")).longValue())
            .userEmail((String) data.get("userEmail"))
            .userFullName((String) data.get("userFullName"))
            .packageName((String) data.get("packageName"))
            .status((String) data.get("status"))
            .startAt((LocalDateTime) data.get("startAt"))
            .endAt(endAt)
            .remainingDays(remainingDays)
            .isActive(isActive)
            .createdAt((LocalDateTime) data.get("createdAt"))
            .build();
    }
}
