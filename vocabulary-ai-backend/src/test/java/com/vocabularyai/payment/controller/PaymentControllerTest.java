package com.vocabularyai.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabularyai.auth.config.JwtProperties;
import com.vocabularyai.auth.security.JwtProvider;
import com.vocabularyai.payment.dto.payos.PayOSCreatePaymentLinkResponse;
import com.vocabularyai.payment.dto.payos.PayOSWebhookRequest;
import com.vocabularyai.payment.entity.PaymentOrder;
import com.vocabularyai.payment.exception.DuplicatePaymentException;
import com.vocabularyai.payment.exception.InvalidSignatureException;
import com.vocabularyai.payment.service.PaymentService;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.repository.UserRepository;
import com.vocabularyai.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @MockBean
    private UserRepository userRepository;
    
    private User testUser;
    
    @BeforeEach
    public void setUp() {
        testUser = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .build();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    }
    
    // ==================== Initiate Payment Tests ====================
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should initiate payment successfully")
    public void testInitiatePayment_Success() throws Exception {
        PayOSCreatePaymentLinkResponse.PayOSPaymentLinkData linkData = PayOSCreatePaymentLinkResponse.PayOSPaymentLinkData.builder()
            .checkoutUrl("https://checkout.payos.vn/123")
            .orderCode(123L)
            .build();
        PayOSCreatePaymentLinkResponse response = PayOSCreatePaymentLinkResponse.builder()
            .code("00")
            .desc("success")
            .data(linkData)
            .build();
        
        when(paymentService.initiatePayment(anyLong(), anyLong())).thenReturn(response);
        
        mockMvc.perform(post("/api/payments/initiate")
            .param("packageId", "1")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should return 400 when user already has active subscription")
    public void testInitiatePayment_ActiveSubscription() throws Exception {
        when(paymentService.initiatePayment(anyLong(), anyLong()))
            .thenThrow(new IllegalStateException("User already has an active subscription"));
        
        mockMvc.perform(post("/api/payments/initiate")
            .param("packageId", "1")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should return 400 for non-existent package")
    public void testInitiatePayment_PackageNotFound() throws Exception {
        when(paymentService.initiatePayment(anyLong(), anyLong()))
            .thenThrow(new IllegalArgumentException("Package not found"));
        
        mockMvc.perform(post("/api/payments/initiate")
            .param("packageId", "999")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should require authentication to initiate payment")
    public void testInitiatePayment_Unauthenticated() throws Exception {
        mockMvc.perform(post("/api/payments/initiate")
            .param("packageId", "1")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }
    
    // ==================== Webhook Tests ====================
    
    @Test
    @DisplayName("Should process webhook successfully")
    public void testHandleWebhook_Success() throws Exception {
        PayOSWebhookRequest.PayOSWebhookData data = PayOSWebhookRequest.PayOSWebhookData.builder()
            .orderCode("ORD_123")
            .amount(99000L)
            .status("PAID")
            .transactionCode("TXN_123")
            .build();
        
        PayOSWebhookRequest webhook = PayOSWebhookRequest.builder()
            .data(data)
            .build();
        
        when(paymentService.handlePaymentWebhook(any(), anyString())).thenReturn(true);
        
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
        doThrow(new InvalidSignatureException("Signature is required"))
            .when(paymentService).handlePaymentWebhook(any(), eq("direct-payos-verification"));
        
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
            .when(paymentService).handlePaymentWebhook(any(), eq("invalid_signature"));
        
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
            .when(paymentService).handlePaymentWebhook(any(), anyString());
        
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
        PaymentOrder mockOrder = PaymentOrder.builder()
            .id(1L)
            .userId(1L)
            .build();
        when(paymentService.getPaymentOrder(1L)).thenReturn(mockOrder);

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
            .andExpect(status().isNotFound());
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
