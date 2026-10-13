package com.vocabularyai.payment;

import com.vocabularyai.payment.dto.payos.PayOSCreatePaymentLinkRequest;
import com.vocabularyai.payment.dto.payos.PayOSCreatePaymentLinkResponse;
import com.vocabularyai.payment.dto.payos.PayOSWebhookRequest;
import com.vocabularyai.payment.entity.Package;
import com.vocabularyai.payment.entity.PaymentOrder;
import com.vocabularyai.payment.entity.Subscription;
import com.vocabularyai.payment.exception.DuplicatePaymentException;
import com.vocabularyai.payment.exception.InvalidSignatureException;
import com.vocabularyai.payment.repository.PackageRepository;
import com.vocabularyai.payment.repository.SubscriptionRepository;
import com.vocabularyai.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Payment Module
 * Tests the complete payment flow from initiation to subscription creation
 * 
 * NOTE: These tests are disabled pending test data setup (test users in database)
 * TODO: Setup test users and payment properties before enabling these tests
 */
@SpringBootTest
@ActiveProfiles("test")
@Disabled("Requires test database setup with users")
public class PaymentIntegrationTest {
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private PackageRepository packageRepository;
    
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    
    private Long testUserId = 100L;
    private Long testPackageId;
    private Package testPackage;
    
    @BeforeEach
    public void setUp() {
        // Clean up before each test
        subscriptionRepository.deleteAll();
        
        // Create test package if not exists
        testPackage = packageRepository.findByCode("PREMIUM_MONTHLY")
            .orElseGet(() -> {
                Package pkg = Package.builder()
                    .code("PREMIUM_MONTHLY")
                    .name("Premium Monthly")
                    .price(new BigDecimal("99000"))
                    .durationDays(30)
                    .status("ACTIVE")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
                return packageRepository.save(pkg);
            });
        
        testPackageId = testPackage.getId();
    }
    
    /**
     * Test 1: Payment Initiation - Create payment link
     */
    @Test
    public void testPaymentInitiation() {
        // Act
        PayOSCreatePaymentLinkResponse response = paymentService.initiatePayment(
            testUserId,
            testPackageId
        );
        
        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertNotNull(response.getData().getCheckoutUrl());
        assertNotNull(response.getData().getTransactionCode());
        assertTrue(response.getData().getCheckoutUrl().contains("checkout.payos"));
    }
    
    /**
     * Test 2: Duplicate Subscription Prevention
     */
    @Test
    public void testDuplicateSubscriptionPrevention() {
        // Arrange
        paymentService.initiatePayment(
            testUserId,
            testPackageId
        );
        
        // Manually create subscription for test
        Subscription sub = Subscription.builder()
            .userId(testUserId)
            .packageId(testPackageId)
            .paymentId(1L)
            .status("ACTIVE")
            .startAt(LocalDateTime.now())
            .endAt(LocalDateTime.now().plusDays(30))
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        subscriptionRepository.save(sub);
        
        // Act & Assert - Should throw exception
        assertThrows(IllegalStateException.class, () -> {
            paymentService.initiatePayment(
                testUserId,
                testPackageId
            );
        });
    }
    
    /**
     * Test 3: Invalid Package ID
     */
    @Test
    public void testInvalidPackageId() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            paymentService.initiatePayment(
                testUserId,
                999999L
            );
        });
    }
    
    /**
     * Test 4: Get subscription status - No subscription
     */
    @Test
    public void testGetSubscriptionStatusNoSubscription() {
        // Act
        PaymentService.SubscriptionStatus status = paymentService.getSubscriptionStatus(testUserId);
        
        // Assert
        assertNull(status);
    }
    
    /**
     * Test 5: Get subscription status - With subscription
     */
    @Test
    public void testGetSubscriptionStatusWithSubscription() {
        // Arrange
        LocalDateTime startAt = LocalDateTime.now();
        LocalDateTime endAt = startAt.plusDays(30);
        
        Subscription sub = Subscription.builder()
            .userId(testUserId)
            .packageId(testPackageId)
            .paymentId(1L)
            .status("ACTIVE")
            .startAt(startAt)
            .endAt(endAt)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        subscriptionRepository.save(sub);
        
        // Act
        PaymentService.SubscriptionStatus status = paymentService.getSubscriptionStatus(testUserId);
        
        // Assert
        assertNotNull(status);
        assertEquals(testUserId, status.userId);
        assertEquals("Premium Monthly", status.packageName);
        assertEquals("ACTIVE", status.status);
        assertTrue(status.remainingDays >= 29 && status.remainingDays <= 30);
    }
    
    /**
     * Test 6: Check active subscription
     */
    @Test
    public void testHasActiveSubscription() {
        // Arrange
        Subscription sub = Subscription.builder()
            .userId(testUserId)
            .packageId(testPackageId)
            .paymentId(1L)
            .status("ACTIVE")
            .startAt(LocalDateTime.now())
            .endAt(LocalDateTime.now().plusDays(30))
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        subscriptionRepository.save(sub);
        
        // Act & Assert
        assertTrue(paymentService.hasActiveSubscription(testUserId));
    }
    
    /**
     * Test 7: Check active subscription - Expired
     */
    @Test
    public void testHasActiveSubscriptionExpired() {
        // Arrange
        Subscription sub = Subscription.builder()
            .userId(testUserId)
            .packageId(testPackageId)
            .paymentId(1L)
            .status("ACTIVE")
            .startAt(LocalDateTime.now().minusDays(30))
            .endAt(LocalDateTime.now().minusHours(1)) // Expired
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        subscriptionRepository.save(sub);
        
        // Act & Assert
        assertFalse(paymentService.hasActiveSubscription(testUserId));
    }
    
    /**
     * Test 8: Cancel subscription
     */
    @Test
    public void testCancelSubscription() {
        // Arrange
        Subscription sub = Subscription.builder()
            .userId(testUserId)
            .packageId(testPackageId)
            .paymentId(1L)
            .status("ACTIVE")
            .startAt(LocalDateTime.now())
            .endAt(LocalDateTime.now().plusDays(30))
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        sub = subscriptionRepository.save(sub);
        
        // Act
        boolean result = paymentService.cancelSubscription(sub.getId());
        
        // Assert
        assertTrue(result);
        Subscription cancelled = subscriptionRepository.findById(sub.getId()).orElseThrow();
        assertEquals("CANCELLED", cancelled.getStatus());
    }
    
    /**
     * Test 9: Webhook handling - Invalid signature
     */
    @Test
    public void testWebhookInvalidSignature() {
        // Arrange
        PayOSWebhookRequest webhook = PayOSWebhookRequest.builder()
            .code("PAYMENT.RECEIVED")
            .data(PayOSWebhookRequest.PayOSWebhookData.builder()
                .orderCode("ORD_123")
                .amount(99000L)
                .transactionCode("TXN_123")
                .status("PAID")
                .build())
            .build();
        
        // Act & Assert
        assertThrows(InvalidSignatureException.class, () -> {
            paymentService.handlePaymentWebhook(webhook, "invalid_signature");
        });
    }
    
    /**
     * Test 10: Webhook handling - Duplicate payment
     */
    @Test
    public void testWebhookDuplicatePayment() {
        // Note: This test requires mock/stub of PaymentGatewayService
        // In a real scenario, we would verify idempotency
        // This is handled by the PaymentServiceImpl.handlePaymentWebhook()
        // which checks for existing transactionCode
    }
}
