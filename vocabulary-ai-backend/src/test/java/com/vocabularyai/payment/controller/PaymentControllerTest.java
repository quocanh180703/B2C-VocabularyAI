package com.vocabularyai.payment.controller;

import com.vocabularyai.auth.config.JwtProperties;
import com.vocabularyai.auth.security.JwtProvider;
import com.vocabularyai.payment.dto.payos.PayOSCreatePaymentLinkResponse;
import com.vocabularyai.payment.dto.payos.PayOSWebhookRequest;
import com.vocabularyai.payment.exception.DuplicatePaymentException;
import com.vocabularyai.payment.exception.InvalidSignatureException;
import com.vocabularyai.payment.service.PaymentService;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("PaymentController Tests")
public class PaymentControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private PaymentService paymentService;
    
    @MockBean
    private JwtProvider jwtProvider;
    
    @MockBean
    private JwtProperties jwtProperties;
    
    @MockBean
    private UserService userService;
    
    private User testUser;
    private PayOSCreatePaymentLinkResponse paymentResponse;
    
    @BeforeEach
    public void setUp() {
        testUser = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .build();
        
        paymentResponse = PayOSCreatePaymentLinkResponse.builder()
            .code("00")
            .desc("success")
            .build();
    }
    
    // ==================== Initiate Payment Tests ====================
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should initiate payment successfully")
    public void testInitiatePayment_Success() throws Exception {
        when(jwtProvider.getUserId(anyString(), anyString())).thenReturn(1L);
        when(jwtProperties.getAccessToken()).thenReturn(mock(JwtProperties.AccessToken.class));
        when(paymentService.initiatePayment(1L, 1L))
            .thenReturn(paymentResponse);
        
        mockMvc.perform(post("/api/payments/initiate")
            .param("packageId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should reject invalid package ID")
    public void testInitiatePayment_InvalidPackage() throws Exception {
        when(jwtProvider.getUserId(anyString(), anyString())).thenReturn(1L);
        when(jwtProperties.getAccessToken()).thenReturn(mock(JwtProperties.AccessToken.class));
        when(paymentService.initiatePayment(1L, 999L))
            .thenThrow(new IllegalArgumentException("Package not found"));
        
        mockMvc.perform(post("/api/payments/initiate")
            .param("packageId", "999"))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should require authentication to initiate payment")
    public void testInitiatePayment_Unauthenticated() throws Exception {
        mockMvc.perform(post("/api/payments/initiate")
            .param("packageId", "1")
            .param("returnUrl", "https://app.example.com/success")
            .param("cancelUrl", "https://app.example.com/cancel"))
            .andExpect(status().isUnauthorized());
    }
    
    // ==================== Webhook Tests ====================
    
    @Test
    @DisplayName("Should process webhook successfully")
    public void testHandleWebhook_Success() throws Exception {
        PayOSWebhookRequest.PayOSWebhookData data = PayOSWebhookRequest.PayOSWebhookData.builder()
            .orderCode("ORD_123")
            .amount(99000L)
            .transactionCode("TXN_123")
            .status("PAID")
            .build();
        
        PayOSWebhookRequest webhook = PayOSWebhookRequest.builder()
            .data(data)
            .build();
        
        doNothing().when(paymentService).handlePaymentWebhook(webhook, "valid_signature");
        
        mockMvc.perform(post("/api/payments/webhook")
            .header("x-payos-signature", "valid_signature")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(webhook)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    @DisplayName("Should reject webhook without signature")
    public void testHandleWebhook_NoSignature() throws Exception {
        PayOSWebhookRequest webhook = new PayOSWebhookRequest();
        
        mockMvc.perform(post("/api/payments/webhook")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(webhook)))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should reject webhook with invalid signature")
    public void testHandleWebhook_InvalidSignature() throws Exception {
        PayOSWebhookRequest.PayOSWebhookData data = PayOSWebhookRequest.PayOSWebhookData.builder()
            .orderCode("ORD_123")
            .amount(99000L)
            .transactionCode("TXN_123")
            .build();
        
        PayOSWebhookRequest webhook = PayOSWebhookRequest.builder()
            .data(data)
            .build();
        
        doThrow(new InvalidSignatureException("Invalid signature"))
            .when(paymentService).handlePaymentWebhook(webhook, "invalid_signature");
        
        mockMvc.perform(post("/api/payments/webhook")
            .header("x-payos-signature", "invalid_signature")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(webhook)))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should handle duplicate webhook idempotently")
    public void testHandleWebhook_Duplicate() throws Exception {
        PayOSWebhookRequest.PayOSWebhookData data = PayOSWebhookRequest.PayOSWebhookData.builder()
            .orderCode("ORD_123")
            .amount(99000L)
            .transactionCode("TXN_123")
            .build();
        
        PayOSWebhookRequest webhook = PayOSWebhookRequest.builder()
            .data(data)
            .build();
        
        doThrow(new DuplicatePaymentException("Duplicate payment"))
            .when(paymentService).handlePaymentWebhook(webhook, "signature");
        
        mockMvc.perform(post("/api/payments/webhook")
            .header("x-payos-signature", "signature")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(webhook)))
            .andExpect(status().isOk()); // Idempotent - return success
    }
    
    // ==================== Get Payment Order Tests ====================
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should get payment order status")
    public void testGetPaymentOrder_Success() throws Exception {
        mockMvc.perform(get("/api/payments/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should return 404 for non-existent payment order")
    public void testGetPaymentOrder_NotFound() throws Exception {
        when(paymentService.getPaymentOrder(999L))
            .thenThrow(new IllegalArgumentException("Payment order not found"));
        
        mockMvc.perform(get("/api/payments/999"))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should require authentication to get payment order")
    public void testGetPaymentOrder_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/payments/1"))
            .andExpect(status().isUnauthorized());
    }
    
    // ==================== Get Subscription Status Tests ====================
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should get subscription status")
    public void testGetSubscriptionStatus_Success() throws Exception {
        when(jwtProvider.getUserId(anyString(), anyString())).thenReturn(1L);
        when(jwtProperties.getAccessToken()).thenReturn(mock(JwtProperties.AccessToken.class));
        
        mockMvc.perform(get("/api/subscriptions/status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    @DisplayName("Should require authentication to get subscription status")
    public void testGetSubscriptionStatus_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/subscriptions/status"))
            .andExpect(status().isUnauthorized());
    }
}
