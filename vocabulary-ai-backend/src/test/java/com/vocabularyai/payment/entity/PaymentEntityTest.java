package com.vocabularyai.payment.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Payment entity
 */
public class PaymentEntityTest {
    
    private Payment payment;
    
    @BeforeEach
    public void setUp() {
        payment = new Payment();
    }
    
    /**
     * Test Payment creation with builder
     */
    @Test
    public void testPaymentBuilder() {
        LocalDateTime now = LocalDateTime.now();
        
        Payment testPayment = Payment.builder()
            .id(1L)
            .paymentOrderId(1L)
            .transactionCode("TXN_1694000001_PAYOS")
            .amount(new BigDecimal("99000"))
            .status("SUCCESS")
            .paymentMethod("CREDIT_CARD")
            .paidAt(now)
            .createdAt(now)
            .updatedAt(now)
            .build();
        
        assertEquals(1L, testPayment.getId());
        assertEquals(1L, testPayment.getPaymentOrderId());
        assertEquals("TXN_1694000001_PAYOS", testPayment.getTransactionCode());
        assertEquals(99000, testPayment.getAmount().intValue());
        assertEquals("SUCCESS", testPayment.getStatus());
        assertEquals("CREDIT_CARD", testPayment.getPaymentMethod());
        assertNotNull(testPayment.getPaidAt());
    }
    
    /**
     * Test Payment getters and setters
     */
    @Test
    public void testPaymentGettersSetters() {
        LocalDateTime now = LocalDateTime.now();
        
        payment.setId(1L);
        payment.setPaymentOrderId(1L);
        payment.setTransactionCode("TXN_123456");
        payment.setAmount(new BigDecimal("99000"));
        payment.setStatus("SUCCESS");
        payment.setPaymentMethod("CREDIT_CARD");
        payment.setPaidAt(now);
        
        assertEquals(1L, payment.getId());
        assertEquals(1L, payment.getPaymentOrderId());
        assertEquals("TXN_123456", payment.getTransactionCode());
        assertEquals(99000, payment.getAmount().intValue());
        assertEquals("SUCCESS", payment.getStatus());
        assertEquals("CREDIT_CARD", payment.getPaymentMethod());
        assertEquals(now, payment.getPaidAt());
    }
    
    /**
     * Test Payment lifecycle callbacks
     */
    @Test
    public void testPaymentOnCreateCallback() {
        LocalDateTime beforeCreate = LocalDateTime.now();
        payment.onCreate();
        LocalDateTime afterCreate = LocalDateTime.now();
        
        assertNotNull(payment.getCreatedAt());
        assertNotNull(payment.getUpdatedAt());
        assertTrue(payment.getCreatedAt().isAfter(beforeCreate.minusSeconds(1)));
        assertTrue(payment.getCreatedAt().isBefore(afterCreate.plusSeconds(1)));
        assertEquals(payment.getCreatedAt(), payment.getUpdatedAt());
    }
    
    /**
     * Test Payment onUpdate callback
     */
    @Test
    public void testPaymentOnUpdateCallback() {
        LocalDateTime createdTime = LocalDateTime.now().minusHours(1);
        payment.setCreatedAt(createdTime);
        payment.setUpdatedAt(createdTime);
        
        LocalDateTime beforeUpdate = LocalDateTime.now();
        payment.onUpdate();
        LocalDateTime afterUpdate = LocalDateTime.now();
        
        assertEquals(createdTime, payment.getCreatedAt(), "CreatedAt should not change");
        assertTrue(payment.getUpdatedAt().isAfter(beforeUpdate.minusSeconds(1)));
        assertTrue(payment.getUpdatedAt().isBefore(afterUpdate.plusSeconds(1)));
        assertTrue(payment.getUpdatedAt().isAfter(payment.getCreatedAt()));
    }
    
    /**
     * Test Payment statuses
     */
    @Test
    public void testPaymentStatuses() {
        String[] statuses = {"PENDING", "SUCCESS", "FAILED", "CANCELLED"};
        
        for (String status : statuses) {
            payment.setStatus(status);
            assertEquals(status, payment.getStatus());
        }
    }
    
    /**
     * Test Payment methods
     */
    @Test
    public void testPaymentMethods() {
        String[] methods = {"CREDIT_CARD", "BANK_TRANSFER", "WALLET", null};
        
        for (String method : methods) {
            payment.setPaymentMethod(method);
            assertEquals(method, payment.getPaymentMethod());
        }
    }
    
    /**
     * Test Payment transition from PENDING to SUCCESS
     */
    @Test
    public void testPaymentTransition() {
        payment.setStatus("PENDING");
        assertEquals("PENDING", payment.getStatus());
        assertNull(payment.getPaidAt());
        
        LocalDateTime paidTime = LocalDateTime.now();
        payment.setStatus("SUCCESS");
        payment.setPaidAt(paidTime);
        
        assertEquals("SUCCESS", payment.getStatus());
        assertEquals(paidTime, payment.getPaidAt());
    }
    
    /**
     * Test Payment successful transaction
     */
    @Test
    public void testSuccessfulPayment() {
        LocalDateTime now = LocalDateTime.now();
        
        payment.setPaymentOrderId(1L);
        payment.setTransactionCode("TXN_PAYOS_1234567890");
        payment.setAmount(new BigDecimal("99000"));
        payment.setStatus("SUCCESS");
        payment.setPaymentMethod("CREDIT_CARD");
        payment.setPaidAt(now);
        
        assertEquals(1L, payment.getPaymentOrderId());
        assertEquals("TXN_PAYOS_1234567890", payment.getTransactionCode());
        assertEquals(99000, payment.getAmount().intValue());
        assertEquals("SUCCESS", payment.getStatus());
        assertEquals("CREDIT_CARD", payment.getPaymentMethod());
        assertEquals(now, payment.getPaidAt());
    }
    
    /**
     * Test Payment failed transaction
     */
    @Test
    public void testFailedPayment() {
        payment.setPaymentOrderId(1L);
        payment.setTransactionCode("TXN_PAYOS_FAILED");
        payment.setAmount(new BigDecimal("99000"));
        payment.setStatus("FAILED");
        payment.setPaymentMethod("CREDIT_CARD");
        payment.setPaidAt(null);
        
        assertEquals(1L, payment.getPaymentOrderId());
        assertEquals("TXN_PAYOS_FAILED", payment.getTransactionCode());
        assertEquals("FAILED", payment.getStatus());
        assertNull(payment.getPaidAt());
    }
    
    /**
     * Test Payment cancelled transaction
     */
    @Test
    public void testCancelledPayment() {
        payment.setPaymentOrderId(1L);
        payment.setTransactionCode("TXN_PAYOS_CANCELLED");
        payment.setAmount(new BigDecimal("99000"));
        payment.setStatus("CANCELLED");
        payment.setPaidAt(null);
        
        assertEquals(1L, payment.getPaymentOrderId());
        assertEquals("CANCELLED", payment.getStatus());
        assertNull(payment.getPaidAt());
    }
    
    /**
     * Test Payment amount matching order
     */
    @Test
    public void testPaymentAmountConsistency() {
        BigDecimal orderAmount = new BigDecimal("99000");
        
        payment.setAmount(orderAmount);
        assertEquals(0, payment.getAmount().compareTo(orderAmount));
    }
    
    /**
     * Test Payment with multiple orders
     */
    @Test
    public void testPaymentMultipleOrders() {
        for (long orderId = 1; orderId <= 5; orderId++) {
            payment.setPaymentOrderId(orderId);
            assertEquals(orderId, payment.getPaymentOrderId());
        }
    }
    
    /**
     * Test Payment transaction code uniqueness validation
     */
    @Test
    public void testPaymentTransactionCodeUniqueness() {
        Payment payment1 = Payment.builder()
            .transactionCode("TXN_UNIQUE_001")
            .build();
        
        Payment payment2 = Payment.builder()
            .transactionCode("TXN_UNIQUE_002")
            .build();
        
        assertNotEquals(payment1.getTransactionCode(), payment2.getTransactionCode());
    }
    
    /**
     * Test Payment with null paidAt (for non-successful statuses)
     */
    @Test
    public void testPaymentNullPaidAt() {
        payment.setStatus("PENDING");
        payment.setPaidAt(null);
        assertNull(payment.getPaidAt());
    }
}
