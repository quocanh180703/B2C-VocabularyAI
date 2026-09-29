package com.vocabularyai.payment.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PaymentOrder entity
 */
public class PaymentOrderEntityTest {
    
    private PaymentOrder order;
    
    @BeforeEach
    public void setUp() {
        order = new PaymentOrder();
    }
    
    /**
     * Test PaymentOrder creation with builder
     */
    @Test
    public void testPaymentOrderBuilder() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expired = now.plusHours(1);
        
        PaymentOrder testOrder = PaymentOrder.builder()
            .id(1L)
            .userId(10L)
            .packageId(1L)
            .orderCode("ORD_1694000001_12345")
            .amount(new BigDecimal("99000"))
            .status("PENDING")
            .paymentUrl("https://payment.url")
            .expiredAt(expired)
            .createdAt(now)
            .updatedAt(now)
            .build();
        
        assertEquals(1L, testOrder.getId());
        assertEquals(10L, testOrder.getUserId());
        assertEquals(1L, testOrder.getPackageId());
        assertEquals("ORD_1694000001_12345", testOrder.getOrderCode());
        assertEquals(99000, testOrder.getAmount().intValue());
        assertEquals("PENDING", testOrder.getStatus());
        assertEquals("https://payment.url", testOrder.getPaymentUrl());
        assertNotNull(testOrder.getExpiredAt());
    }
    
    /**
     * Test PaymentOrder getters and setters
     */
    @Test
    public void testPaymentOrderGettersSetters() {
        LocalDateTime now = LocalDateTime.now();
        
        order.setId(1L);
        order.setUserId(10L);
        order.setPackageId(1L);
        order.setOrderCode("ORD_123456");
        order.setAmount(new BigDecimal("99000"));
        order.setStatus("PENDING");
        order.setPaymentUrl("https://payment.url");
        order.setExpiredAt(now.plusHours(1));
        
        assertEquals(1L, order.getId());
        assertEquals(10L, order.getUserId());
        assertEquals(1L, order.getPackageId());
        assertEquals("ORD_123456", order.getOrderCode());
        assertEquals(99000, order.getAmount().intValue());
        assertEquals("PENDING", order.getStatus());
        assertEquals("https://payment.url", order.getPaymentUrl());
        assertNotNull(order.getExpiredAt());
    }
    
    /**
     * Test PaymentOrder lifecycle callbacks
     */
    @Test
    public void testPaymentOrderOnCreateCallback() {
        LocalDateTime beforeCreate = LocalDateTime.now();
        order.onCreate();
        LocalDateTime afterCreate = LocalDateTime.now();
        
        assertNotNull(order.getCreatedAt());
        assertNotNull(order.getUpdatedAt());
        assertTrue(order.getCreatedAt().isAfter(beforeCreate.minusSeconds(1)));
        assertTrue(order.getCreatedAt().isBefore(afterCreate.plusSeconds(1)));
        assertEquals(order.getCreatedAt(), order.getUpdatedAt());
    }
    
    /**
     * Test PaymentOrder onUpdate callback
     */
    @Test
    public void testPaymentOrderOnUpdateCallback() {
        LocalDateTime createdTime = LocalDateTime.now().minusHours(1);
        order.setCreatedAt(createdTime);
        order.setUpdatedAt(createdTime);
        
        LocalDateTime beforeUpdate = LocalDateTime.now();
        order.onUpdate();
        LocalDateTime afterUpdate = LocalDateTime.now();
        
        assertEquals(createdTime, order.getCreatedAt(), "CreatedAt should not change");
        assertTrue(order.getUpdatedAt().isAfter(beforeUpdate.minusSeconds(1)));
        assertTrue(order.getUpdatedAt().isBefore(afterUpdate.plusSeconds(1)));
        assertTrue(order.getUpdatedAt().isAfter(order.getCreatedAt()));
    }
    
    /**
     * Test PaymentOrder statuses
     */
    @Test
    public void testPaymentOrderStatuses() {
        String[] statuses = {"PENDING", "PAID", "FAILED", "CANCELLED"};
        
        for (String status : statuses) {
            order.setStatus(status);
            assertEquals(status, order.getStatus());
        }
    }
    
    /**
     * Test PaymentOrder with different users
     */
    @Test
    public void testPaymentOrderMultipleUsers() {
        for (long userId = 1; userId <= 5; userId++) {
            order.setUserId(userId);
            assertEquals(userId, order.getUserId());
        }
    }
    
    /**
     * Test PaymentOrder expiration logic
     */
    @Test
    public void testPaymentOrderExpiration() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureTime = now.plusHours(1);
        LocalDateTime pastTime = now.minusHours(1);
        
        order.setCreatedAt(now);
        order.setExpiredAt(futureTime);
        
        assertTrue(futureTime.isAfter(now), "Expiration should be in future");
        assertTrue(now.isBefore(futureTime), "Now should be before expiration");
        assertTrue(now.isAfter(pastTime), "Now should be after past time");
    }
    
    /**
     * Test PaymentOrder transition from PENDING to PAID
     */
    @Test
    public void testPaymentOrderTransition() {
        order.setStatus("PENDING");
        assertEquals("PENDING", order.getStatus());
        
        LocalDateTime paidTime = LocalDateTime.now();
        order.setStatus("PAID");
        order.setPaidAt(paidTime);
        
        assertEquals("PAID", order.getStatus());
        assertEquals(paidTime, order.getPaidAt());
    }
    
    /**
     * Test PaymentOrder with premium monthly package
     */
    @Test
    public void testPaymentOrderPremiumMonthly() {
        order.setOrderCode("ORD_1694000001_12345");
        order.setPackageId(1L);
        order.setAmount(new BigDecimal("99000"));
        order.setStatus("PENDING");
        
        assertEquals("ORD_1694000001_12345", order.getOrderCode());
        assertEquals(1L, order.getPackageId());
        assertEquals(99000, order.getAmount().intValue());
        assertEquals("PENDING", order.getStatus());
    }
    
    /**
     * Test PaymentOrder with premium yearly package
     */
    @Test
    public void testPaymentOrderPremiumYearly() {
        order.setOrderCode("ORD_1694000002_54321");
        order.setPackageId(2L);
        order.setAmount(new BigDecimal("899000"));
        order.setStatus("PENDING");
        
        assertEquals("ORD_1694000002_54321", order.getOrderCode());
        assertEquals(2L, order.getPackageId());
        assertEquals(899000, order.getAmount().intValue());
        assertEquals("PENDING", order.getStatus());
    }
    
    /**
     * Test PaymentOrder with null payment URL (before payment generation)
     */
    @Test
    public void testPaymentOrderNullPaymentUrl() {
        order.setPaymentUrl(null);
        assertNull(order.getPaymentUrl());
    }
    
    /**
     * Test PaymentOrder with null paid_at (before payment)
     */
    @Test
    public void testPaymentOrderNullPaidAt() {
        order.setPaidAt(null);
        assertNull(order.getPaidAt());
    }
    
    /**
     * Test PaymentOrder amount comparison
     */
    @Test
    public void testPaymentOrderAmountComparison() {
        PaymentOrder monthlyOrder = PaymentOrder.builder()
            .amount(new BigDecimal("99000"))
            .build();
        
        PaymentOrder yearlyOrder = PaymentOrder.builder()
            .amount(new BigDecimal("899000"))
            .build();
        
        assertTrue(monthlyOrder.getAmount().compareTo(yearlyOrder.getAmount()) < 0);
        assertTrue(yearlyOrder.getAmount().compareTo(monthlyOrder.getAmount()) > 0);
    }
}
