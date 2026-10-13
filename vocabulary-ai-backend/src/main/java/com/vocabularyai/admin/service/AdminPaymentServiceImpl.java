package com.vocabularyai.admin.service;

import com.vocabularyai.admin.dto.response.PaymentListResponse;
import com.vocabularyai.admin.dto.response.PaymentDetailResponse;
import com.vocabularyai.admin.repository.AdminPaymentRepository;
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
 * Admin Payment Service Implementation
 * 
 * Transforms repository data to DTOs and implements business logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminPaymentServiceImpl implements AdminPaymentService {
    
    private final AdminPaymentRepository paymentRepository;
    
    @Override
    public Page<PaymentListResponse> getPaymentList(Pageable pageable) {
        log.info("Fetching payment list with pagination");
        
        try {
            Page<?> paymentPage = paymentRepository.getPaymentList(pageable);
            
            List<PaymentListResponse> payments = paymentPage.getContent().stream()
                .map(item -> mapToPaymentListResponse(item))
                .collect(Collectors.toList());
            
            log.debug("Payment list fetched: {} payments, page {}/{}", 
                payments.size(), pageable.getPageNumber(), paymentPage.getTotalPages());
            
            return new PageImpl<>(payments, pageable, paymentPage.getTotalElements());
            
        } catch (Exception e) {
            log.error("Error fetching payment list", e);
            throw new RuntimeException("Failed to fetch payment list: " + e.getMessage());
        }
    }
    
    @Override
    public Page<PaymentListResponse> getPaymentListByStatus(String status, Pageable pageable) {
        log.info("Fetching payment list by status: {}", status);
        
        try {
            Page<?> paymentPage = paymentRepository.getPaymentListByStatus(status, pageable);
            
            List<PaymentListResponse> payments = paymentPage.getContent().stream()
                .map(item -> mapToPaymentListResponse(item))
                .collect(Collectors.toList());
            
            log.debug("Payment list by status fetched: {} payments", payments.size());
            
            return new PageImpl<>(payments, pageable, paymentPage.getTotalElements());
            
        } catch (Exception e) {
            log.error("Error fetching payment list by status", e);
            throw new RuntimeException("Failed to fetch payment list: " + e.getMessage());
        }
    }
    
    @Override
    public Page<PaymentListResponse> getPaymentListByUser(Long userId, Pageable pageable) {
        log.info("Fetching payment list for user: {}", userId);
        
        try {
            Page<?> paymentPage = paymentRepository.getPaymentListByUser(userId, pageable);
            
            List<PaymentListResponse> payments = paymentPage.getContent().stream()
                .map(item -> mapToPaymentListResponse(item))
                .collect(Collectors.toList());
            
            log.debug("User payment list fetched: {} payments", payments.size());
            
            return new PageImpl<>(payments, pageable, paymentPage.getTotalElements());
            
        } catch (Exception e) {
            log.error("Error fetching user payment list", e);
            throw new RuntimeException("Failed to fetch user payment list: " + e.getMessage());
        }
    }
    
    @Override
    public Page<PaymentListResponse> searchPayments(String searchTerm, Pageable pageable) {
        log.info("Searching payments with term: {}", searchTerm);
        
        try {
            Page<?> searchResult = paymentRepository.searchPayments(searchTerm, pageable);
            
            List<PaymentListResponse> payments = searchResult.getContent().stream()
                .map(item -> mapToPaymentListResponse(item))
                .collect(Collectors.toList());
            
            log.debug("Payments found: {} results", payments.size());
            
            return new PageImpl<>(payments, pageable, searchResult.getTotalElements());
            
        } catch (Exception e) {
            log.error("Error searching payments", e);
            throw new RuntimeException("Failed to search payments: " + e.getMessage());
        }
    }
    
    @Override
    public PaymentDetailResponse getPaymentDetail(Long paymentId) {
        log.info("Fetching payment detail: paymentId={}", paymentId);
        
        try {
            List<?> paymentList = paymentRepository.getPaymentDetail(paymentId);
            
            if (paymentList.isEmpty()) {
                log.warn("Payment not found: paymentId={}", paymentId);
                throw new IllegalArgumentException("Payment not found: " + paymentId);
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> paymentData = (Map<String, Object>) paymentList.get(0);
            
            // Calculate remaining days
            LocalDateTime subscriptionEndAt = (LocalDateTime) paymentData.get("subscriptionEndAt");
            long remainingDays = 0;
            if (subscriptionEndAt != null) {
                remainingDays = Math.max(0, ChronoUnit.DAYS.between(LocalDateTime.now(), subscriptionEndAt));
            }
            
            PaymentDetailResponse response = PaymentDetailResponse.builder()
                .paymentId(((Number) paymentData.get("paymentId")).longValue())
                .transactionCode((String) paymentData.get("transactionCode"))
                .status((String) paymentData.get("status"))
                .amount((BigDecimal) paymentData.get("amount"))
                .paymentMethod((String) paymentData.get("paymentMethod"))
                .paidAt((LocalDateTime) paymentData.get("paidAt"))
                .createdAt((LocalDateTime) paymentData.get("createdAt"))
                .orderId(((Number) paymentData.get("orderId")).longValue())
                .orderCode((String) paymentData.get("orderCode"))
                .orderStatus((String) paymentData.get("orderStatus"))
                .orderExpiredAt((LocalDateTime) paymentData.get("orderExpiredAt"))
                .userId(((Number) paymentData.get("userId")).longValue())
                .username((String) paymentData.get("username"))
                .userEmail((String) paymentData.get("userEmail"))
                .userFullName((String) paymentData.get("userFullName"))
                .packageId(((Number) paymentData.get("packageId")).longValue())
                .packageName((String) paymentData.get("packageName"))
                .packageCode((String) paymentData.get("packageCode"))
                .packagePrice((BigDecimal) paymentData.get("packagePrice"))
                .packageDurationDays(((Number) paymentData.get("packageDurationDays")).intValue())
                .subscriptionId(paymentData.get("subscriptionId") != null 
                    ? ((Number) paymentData.get("subscriptionId")).longValue() : null)
                .subscriptionStatus((String) paymentData.get("subscriptionStatus"))
                .subscriptionStartAt((LocalDateTime) paymentData.get("subscriptionStartAt"))
                .subscriptionEndAt(subscriptionEndAt)
                .remainingDays(remainingDays)
                .build();
            
            log.debug("Payment detail fetched: paymentId={}, transactionCode={}", 
                response.getPaymentId(), response.getTransactionCode());
            
            return response;
            
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching payment detail", e);
            throw new RuntimeException("Failed to fetch payment detail: " + e.getMessage());
        }
    }
    
    /**
     * Map raw query result to PaymentListResponse DTO
     */
    private PaymentListResponse mapToPaymentListResponse(Object item) {
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) item;
        
        return PaymentListResponse.builder()
            .paymentId(((Number) data.get("paymentId")).longValue())
            .transactionCode((String) data.get("transactionCode"))
            .orderCode((String) data.get("orderCode"))
            .userId(((Number) data.get("userId")).longValue())
            .userEmail((String) data.get("userEmail"))
            .userFullName((String) data.get("userFullName"))
            .packageName((String) data.get("packageName"))
            .amount((BigDecimal) data.get("amount"))
            .status((String) data.get("status"))
            .paymentMethod((String) data.get("paymentMethod"))
            .paidAt((LocalDateTime) data.get("paidAt"))
            .createdAt((LocalDateTime) data.get("createdAt"))
            .build();
    }
}
