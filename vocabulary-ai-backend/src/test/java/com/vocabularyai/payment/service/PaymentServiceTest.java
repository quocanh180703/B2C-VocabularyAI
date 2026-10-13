package com.vocabularyai.payment.service;

import com.vocabularyai.payment.dto.payos.PayOSCreatePaymentLinkResponse;
import com.vocabularyai.payment.dto.payos.PayOSWebhookRequest;
import com.vocabularyai.payment.entity.Package;
import com.vocabularyai.payment.entity.PaymentOrder;
import com.vocabularyai.payment.entity.Subscription;
import com.vocabularyai.payment.exception.DuplicatePaymentException;
import com.vocabularyai.payment.exception.InvalidSignatureException;
import com.vocabularyai.payment.repository.PackageRepository;
import com.vocabularyai.payment.repository.PaymentOrderRepository;
import com.vocabularyai.payment.repository.SubscriptionRepository;
import com.vocabularyai.payment.service.gateway.PaymentGatewayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@DisplayName("PaymentService Tests")
public class PaymentServiceTest {
    
    @Autowired
    private PaymentService paymentService;
    
    @MockBean
    private PaymentGatewayService paymentGateway;
    
    @MockBean
    private PackageRepository packageRepository;
    
    @MockBean
    private PaymentOrderRepository paymentOrderRepository;
    
    @MockBean
    private SubscriptionRepository subscriptionRepository;
    
    private Package testPackage;
    private PaymentOrder testPaymentOrder;
    
    @BeforeEach
    public void setUp() {
        testPackage = Package.builder()
            .id(1L)
            .code("PREMIUM_MONTHLY")
            .name("Premium Monthly")
            .price(new BigDecimal("99000"))
            .durationDays(30)
            .status("ACTIVE")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        testPaymentOrder = PaymentOrder.builder()
            .id(1L)
            .userId(1L)
            .packageId(1L)
            .orderCode("ORD_123")
            .amount(new BigDecimal("99000"))
            .status("PENDING")
            .paymentUrl("https://checkout.payos.vn/...")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }
    
    @Test
    @DisplayName("Should initiate payment successfully")
    public void testInitiatePayment_Success() {
        PayOSCreatePaymentLinkResponse mockResponse = PayOSCreatePaymentLinkResponse.builder()
            .code("00")
            .desc("success")
            .build();
        
        when(packageRepository.findById(1L)).thenReturn(Optional.of(testPackage));
        when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(paymentGateway.createPaymentLink(any())).thenReturn(mockResponse);
        when(paymentOrderRepository.save(any())).thenReturn(testPaymentOrder);
        
        PayOSCreatePaymentLinkResponse response = paymentService.initiatePayment(1L, 1L);
        
        assertNotNull(response);
        assertEquals("00", response.getCode());
    }
    
    @Test
    @DisplayName("Should reject payment for non-existent package")
    public void testInitiatePayment_PackageNotFound() {
        when(packageRepository.findById(999L)).thenReturn(Optional.empty());
        
        assertThrows(IllegalArgumentException.class, () ->
            paymentService.initiatePayment(1L, 999L));
    }
    
    @Test
    @DisplayName("Should handle payment webhook successfully")
    public void testHandlePaymentWebhook_Success() {
        PayOSWebhookRequest.PayOSWebhookData data = PayOSWebhookRequest.PayOSWebhookData.builder()
            .orderCode("ORD_123")
            .amount(99000L)
            .transactionCode("TXN_123")
            .status("PAID")
            .build();
        
        PayOSWebhookRequest webhook = PayOSWebhookRequest.builder()
            .data(data)
            .build();
        
        when(paymentGateway.verifyWebhookSignature(anyString(), anyLong(), anyString(), anyString()))
            .thenReturn(true);
        when(paymentOrderRepository.findByOrderCode("ORD_123"))
            .thenReturn(Optional.of(testPaymentOrder));
        when(paymentOrderRepository.save(any())).thenReturn(testPaymentOrder);
        when(subscriptionRepository.save(any())).thenReturn(new Subscription());
        
        assertDoesNotThrow(() -> paymentService.handlePaymentWebhook(webhook, "signature"));
    }
    
    @Test
    @DisplayName("Should reject webhook with invalid signature")
    public void testHandlePaymentWebhook_InvalidSignature() {
        PayOSWebhookRequest webhook = new PayOSWebhookRequest();
        
        when(paymentGateway.verifyWebhookSignature(anyString(), anyLong(), anyString(), anyString()))
            .thenThrow(new InvalidSignatureException("Invalid signature"));
        
        assertThrows(InvalidSignatureException.class, () ->
            paymentService.handlePaymentWebhook(webhook, "invalid"));
    }
    
    @Test
    @DisplayName("Should get payment order")
    public void testGetPaymentOrder_Success() {
        when(paymentOrderRepository.findById(1L)).thenReturn(Optional.of(testPaymentOrder));
        
        PaymentOrder order = paymentService.getPaymentOrder(1L);
        
        assertNotNull(order);
        assertEquals("ORD_123", order.getOrderCode());
    }
    
    @Test
    @DisplayName("Should throw exception for non-existent payment order")
    public void testGetPaymentOrder_NotFound() {
        when(paymentOrderRepository.findById(999L)).thenReturn(Optional.empty());
        
        assertThrows(IllegalArgumentException.class, () ->
            paymentService.getPaymentOrder(999L));
    }
    
    @Test
    @DisplayName("Should check active subscription")
    public void testHasActiveSubscription_True() {
        Subscription subscription = Subscription.builder()
            .userId(1L)
            .status("ACTIVE")
            .endAt(LocalDateTime.now().plusDays(10))
            .build();
        
        when(subscriptionRepository.findByUserId(1L))
            .thenReturn(Optional.of(subscription));
        
        assertTrue(paymentService.hasActiveSubscription(1L));
    }
    
    @Test
    @DisplayName("Should return false for no active subscription")
    public void testHasActiveSubscription_False() {
        when(subscriptionRepository.findByUserId(1L))
            .thenReturn(Optional.empty());
        
        assertFalse(paymentService.hasActiveSubscription(1L));
    }
}
