package com.vocabularyai.payment.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Subscription entity
 */
public class SubscriptionEntityTest {
    
    private Subscription subscription;
    
    @BeforeEach
    public void setUp() {
        subscription = new Subscription();
    }
    
    /**
     * Test Subscription creation with builder
     */
    @Test
    public void testSubscriptionBuilder() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.plusDays(30);
        
        Subscription testSub = Subscription.builder()
            .id(1L)
            .userId(10L)
            .packageId(1L)
            .paymentId(1L)
            .status("ACTIVE")
            .startAt(now)
            .endAt(endTime)
            .createdAt(now)
            .updatedAt(now)
            .build();
        
        assertEquals(1L, testSub.getId());
        assertEquals(10L, testSub.getUserId());
        assertEquals(1L, testSub.getPackageId());
        assertEquals(1L, testSub.getPaymentId());
        assertEquals("ACTIVE", testSub.getStatus());
        assertEquals(now, testSub.getStartAt());
        assertEquals(endTime, testSub.getEndAt());
    }
    
    /**
     * Test Subscription getters and setters
     */
    @Test
    public void testSubscriptionGettersSetters() {
        LocalDateTime now = LocalDateTime.now();
        
        subscription.setId(1L);
        subscription.setUserId(10L);
        subscription.setPackageId(1L);
        subscription.setPaymentId(1L);
        subscription.setStatus("ACTIVE");
        subscription.setStartAt(now);
        subscription.setEndAt(now.plusDays(30));
        
        assertEquals(1L, subscription.getId());
        assertEquals(10L, subscription.getUserId());
        assertEquals(1L, subscription.getPackageId());
        assertEquals(1L, subscription.getPaymentId());
        assertEquals("ACTIVE", subscription.getStatus());
        assertEquals(now, subscription.getStartAt());
    }
    
    /**
     * Test Subscription lifecycle callbacks
     */
    @Test
    public void testSubscriptionOnCreateCallback() {
        LocalDateTime beforeCreate = LocalDateTime.now();
        subscription.onCreate();
        LocalDateTime afterCreate = LocalDateTime.now();
        
        assertNotNull(subscription.getCreatedAt());
        assertNotNull(subscription.getUpdatedAt());
        assertTrue(subscription.getCreatedAt().isAfter(beforeCreate.minusSeconds(1)));
        assertTrue(subscription.getCreatedAt().isBefore(afterCreate.plusSeconds(1)));
        assertEquals(subscription.getCreatedAt(), subscription.getUpdatedAt());
    }
    
    /**
     * Test Subscription onUpdate callback
     */
    @Test
    public void testSubscriptionOnUpdateCallback() {
        LocalDateTime createdTime = LocalDateTime.now().minusHours(1);
        subscription.setCreatedAt(createdTime);
        subscription.setUpdatedAt(createdTime);
        
        LocalDateTime beforeUpdate = LocalDateTime.now();
        subscription.onUpdate();
        LocalDateTime afterUpdate = LocalDateTime.now();
        
        assertEquals(createdTime, subscription.getCreatedAt(), "CreatedAt should not change");
        assertTrue(subscription.getUpdatedAt().isAfter(beforeUpdate.minusSeconds(1)));
        assertTrue(subscription.getUpdatedAt().isBefore(afterUpdate.plusSeconds(1)));
        assertTrue(subscription.getUpdatedAt().isAfter(subscription.getCreatedAt()));
    }
    
    /**
     * Test Subscription statuses
     */
    @Test
    public void testSubscriptionStatuses() {
        String[] statuses = {"ACTIVE", "EXPIRED", "CANCELLED", "SUSPENDED"};
        
        for (String status : statuses) {
            subscription.setStatus(status);
            assertEquals(status, subscription.getStatus());
        }
    }
    
    /**
     * Test Subscription active check - currently active
     */
    @Test
    public void testSubscriptionIsActive() {
        LocalDateTime now = LocalDateTime.now();
        
        subscription.setStatus("ACTIVE");
        subscription.setStartAt(now.minusHours(1));
        subscription.setEndAt(now.plusDays(30));
        
        assertTrue(subscription.isActive());
    }
    
    /**
     * Test Subscription active check - not yet started
     */
    @Test
    public void testSubscriptionIsActiveNotStarted() {
        LocalDateTime now = LocalDateTime.now();
        
        subscription.setStatus("ACTIVE");
        subscription.setStartAt(now.plusHours(1));
        subscription.setEndAt(now.plusDays(30));
        
        assertFalse(subscription.isActive());
    }
    
    /**
     * Test Subscription active check - already expired
     */
    @Test
    public void testSubscriptionIsActiveExpired() {
        LocalDateTime now = LocalDateTime.now();
        
        subscription.setStatus("ACTIVE");
        subscription.setStartAt(now.minusDays(30));
        subscription.setEndAt(now.minusHours(1));
        
        assertFalse(subscription.isActive());
    }
    
    /**
     * Test Subscription active check - wrong status
     */
    @Test
    public void testSubscriptionIsActiveWrongStatus() {
        LocalDateTime now = LocalDateTime.now();
        
        subscription.setStatus("EXPIRED");
        subscription.setStartAt(now.minusHours(1));
        subscription.setEndAt(now.plusDays(30));
        
        assertFalse(subscription.isActive());
    }
    
    /**
     * Test Subscription with premium monthly package
     */
    @Test
    public void testPremiumMonthlySubscription() {
        LocalDateTime now = LocalDateTime.now();
        
        subscription.setPackageId(1L);
        subscription.setStatus("ACTIVE");
        subscription.setStartAt(now);
        subscription.setEndAt(now.plusDays(30));
        
        assertEquals(1L, subscription.getPackageId());
        assertEquals("ACTIVE", subscription.getStatus());
        
        long durationDays = java.time.temporal.ChronoUnit.DAYS.between(subscription.getStartAt(), subscription.getEndAt());
        assertEquals(30, durationDays);
    }
    
    /**
     * Test Subscription with premium yearly package
     */
    @Test
    public void testPremiumYearlySubscription() {
        LocalDateTime now = LocalDateTime.now();
        
        subscription.setPackageId(2L);
        subscription.setStatus("ACTIVE");
        subscription.setStartAt(now);
        subscription.setEndAt(now.plusDays(365));
        
        assertEquals(2L, subscription.getPackageId());
        assertEquals("ACTIVE", subscription.getStatus());
    }
    
    /**
     * Test Subscription expiration
     */
    @Test
    public void testSubscriptionExpiration() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expired = now.minusDays(1);
        
        subscription.setStatus("ACTIVE");
        subscription.setEndAt(expired);
        
        subscription.setStatus("EXPIRED");
        
        assertEquals("EXPIRED", subscription.getStatus());
    }
    
    /**
     * Test Subscription cancellation
     */
    @Test
    public void testSubscriptionCancellation() {
        LocalDateTime now = LocalDateTime.now();
        
        subscription.setStatus("ACTIVE");
        subscription.setStartAt(now);
        subscription.setEndAt(now.plusDays(30));
        
        subscription.setStatus("CANCELLED");
        
        assertEquals("CANCELLED", subscription.getStatus());
    }
    
    /**
     * Test Subscription suspension
     */
    @Test
    public void testSubscriptionSuspension() {
        subscription.setStatus("ACTIVE");
        subscription.setStatus("SUSPENDED");
        
        assertEquals("SUSPENDED", subscription.getStatus());
    }
    
    /**
     * Test Subscription with different users
     */
    @Test
    public void testSubscriptionMultipleUsers() {
        for (long userId = 1; userId <= 5; userId++) {
            subscription.setUserId(userId);
            assertEquals(userId, subscription.getUserId());
        }
    }
    
    /**
     * Test Subscription linkage to payment
     */
    @Test
    public void testSubscriptionPaymentLink() {
        subscription.setPaymentId(1L);
        subscription.setUserId(10L);
        subscription.setPackageId(1L);
        
        assertEquals(1L, subscription.getPaymentId());
        assertEquals(10L, subscription.getUserId());
        assertEquals(1L, subscription.getPackageId());
    }
    
    /**
     * Test Subscription duration calculation
     */
    @Test
    public void testSubscriptionDuration() {
        LocalDateTime start = LocalDateTime.of(2026, 9, 29, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 10, 29, 0, 0, 0);
        
        subscription.setStartAt(start);
        subscription.setEndAt(end);
        
        long durationDays = java.time.temporal.ChronoUnit.DAYS.between(start, end);
        assertEquals(30, durationDays);
    }
    
    /**
     * Test Subscription one-per-user constraint (UK on user_id)
     */
    @Test
    public void testSubscriptionOnePerUser() {
        Subscription sub1 = Subscription.builder()
            .userId(1L)
            .packageId(1L)
            .paymentId(1L)
            .build();
        
        Subscription sub2 = Subscription.builder()
            .userId(1L)
            .packageId(2L)
            .paymentId(2L)
            .build();
        
        // Same user ID - in database would violate UK constraint
        assertEquals(sub1.getUserId(), sub2.getUserId());
    }
}
