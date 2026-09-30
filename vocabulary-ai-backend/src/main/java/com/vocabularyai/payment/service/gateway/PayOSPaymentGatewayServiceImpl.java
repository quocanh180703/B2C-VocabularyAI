package com.vocabularyai.payment.service.gateway;

import com.vocabularyai.payment.config.PaymentProperties;
import com.vocabularyai.payment.dto.payos.*;
import com.vocabularyai.payment.exception.InvalidSignatureException;
import com.vocabularyai.payment.exception.PaymentGatewayException;
import com.vocabularyai.payment.exception.PaymentTimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * PayOS Payment Gateway Implementation
 * 
 * Implements PaymentGatewayService for PayOS payment platform
 * 
 * Features:
 * - Payment link generation
 * - HMAC-SHA256 webhook signature verification
 * - Retry logic with exponential backoff
 * - Timeout handling
 * - Error recovery
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PayOSPaymentGatewayServiceImpl implements PaymentGatewayService {
    
    private final PaymentProperties paymentProperties;
    private final RestTemplate restTemplate;
    
    private static final String GATEWAY_NAME = "PayOS";
    private static final String PAYMENT_LINK_ENDPOINT = "/api/v1/payment-requests";
    private static final String PAYMENT_STATUS_ENDPOINT = "/api/v1/payment-requests/%s";
    private static final String CANCEL_PAYMENT_ENDPOINT = "/api/v1/payment-requests/%s/cancel";
    
    private static final String HMAC_SHA256 = "HmacSHA256";
    
    @Override
    public PayOSCreatePaymentLinkResponse createPaymentLink(PayOSCreatePaymentLinkRequest request) {
        log.info("Creating payment link with PayOS for order: {}", request.getOrderCode());
        
        try {
            // Retry logic with exponential backoff
            return executeWithRetry(() -> {
                String url = buildUrl(PAYMENT_LINK_ENDPOINT);
                
                log.debug("Calling PayOS API: POST {}", url);
                ResponseEntity<PayOSCreatePaymentLinkResponse> response = restTemplate.postForEntity(
                    url,
                    request,
                    PayOSCreatePaymentLinkResponse.class
                );
                
                if (response.getStatusCode() != HttpStatus.OK) {
                    throw new PaymentGatewayException(
                        "Failed to create payment link: PayOS returned status " + response.getStatusCode()
                    );
                }
                
                PayOSCreatePaymentLinkResponse paymentResponse = response.getBody();
                if (paymentResponse == null || !paymentResponse.isSuccess()) {
                    String errorDesc = paymentResponse != null ? paymentResponse.getDesc() : "Unknown error";
                    throw new PaymentGatewayException(
                        "PayOS API error: " + errorDesc
                    );
                }
                
                log.info("Payment link created successfully for order: {}, checkoutUrl: {}", 
                    request.getOrderCode(), 
                    paymentResponse.getData().getCheckoutUrl());
                
                return paymentResponse;
            });
            
        } catch (PaymentTimeoutException | PaymentGatewayException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to create payment link for order: {}", request.getOrderCode(), e);
            throw new PaymentGatewayException("Unexpected error creating payment link", e);
        }
    }
    
    @Override
    public boolean verifyWebhookSignature(String orderCode, Long amount, String transactionCode, String signature) {
        try {
            log.debug("Verifying webhook signature for order: {}, transactionCode: {}", orderCode, transactionCode);
            
            // Reconstruct message: "orderCode=xxx&amount=yyy&reference=zzz"
            String message = String.format("orderCode=%s&amount=%s&reference=%s", 
                orderCode, amount, transactionCode);
            
            // Calculate HMAC-SHA256
            String calculatedSignature;
            try {
                calculatedSignature = calculateHmacSha256(
                    message,
                    paymentProperties.getPayos().getApiKey()
                );
            } catch (Exception e) {
                log.error("Error calculating HMAC-SHA256 signature", e);
                throw new PaymentGatewayException("Failed to calculate webhook signature", e);
            }
            
            // Compare signatures
            boolean isValid = calculatedSignature.equals(signature);
            
            if (!isValid) {
                log.warn("Webhook signature mismatch for order: {}", orderCode);
                log.debug("Expected: {}, Received: {}", calculatedSignature, signature);
                throw new InvalidSignatureException(
                    "Webhook signature verification failed for order: " + orderCode
                );
            }
            
            log.debug("Webhook signature verified successfully for order: {}", orderCode);
            return true;
            
        } catch (InvalidSignatureException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error verifying webhook signature for order: {}", orderCode, e);
            throw new PaymentGatewayException("Failed to verify webhook signature", e);
        }
    }
    
    @Override
    public String getPaymentStatus(String transactionCode) {
        log.info("Getting payment status for transactionCode: {}", transactionCode);
        
        try {
            return executeWithRetry(() -> {
                String url = buildUrl(String.format(PAYMENT_STATUS_ENDPOINT, transactionCode));
                
                log.debug("Calling PayOS API: GET {}", url);
                ResponseEntity<PayOSCreatePaymentLinkResponse> response = restTemplate.getForEntity(
                    url,
                    PayOSCreatePaymentLinkResponse.class
                );
                
                if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                    throw new PaymentGatewayException("Failed to get payment status");
                }
                
                PayOSCreatePaymentLinkResponse paymentResponse = response.getBody();
                String status = paymentResponse.getData() != null ? "UNKNOWN" : "UNKNOWN";
                
                log.info("Payment status for transactionCode {}: {}", transactionCode, status);
                return status;
            });
            
        } catch (PaymentTimeoutException | PaymentGatewayException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to get payment status for transactionCode: {}", transactionCode, e);
            throw new PaymentGatewayException("Failed to get payment status", e);
        }
    }
    
    @Override
    public boolean cancelPayment(String orderCode) {
        log.info("Cancelling payment for order: {}", orderCode);
        
        try {
            return executeWithRetry(() -> {
                String url = buildUrl(String.format(CANCEL_PAYMENT_ENDPOINT, orderCode));
                
                log.debug("Calling PayOS API: POST {}", url);
                ResponseEntity<PayOSCreatePaymentLinkResponse> response = restTemplate.postForEntity(
                    url,
                    null,
                    PayOSCreatePaymentLinkResponse.class
                );
                
                boolean success = response.getStatusCode() == HttpStatus.OK;
                
                if (success) {
                    log.info("Payment cancelled successfully for order: {}", orderCode);
                } else {
                    log.warn("Failed to cancel payment for order: {}, status: {}", orderCode, response.getStatusCode());
                }
                
                return success;
            });
            
        } catch (PaymentTimeoutException | PaymentGatewayException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to cancel payment for order: {}", orderCode, e);
            throw new PaymentGatewayException("Failed to cancel payment", e);
        }
    }
    
    @Override
    public boolean validateWebhook(PayOSWebhookRequest webhook) {
        if (webhook == null) {
            log.warn("Webhook is null");
            return false;
        }
        
        if (webhook.getData() == null) {
            log.warn("Webhook data is null");
            return false;
        }
        
        PayOSWebhookRequest.PayOSWebhookData data = webhook.getData();
        
        // Check required fields
        if (data.getOrderCode() == null || data.getOrderCode().isEmpty()) {
            log.warn("Webhook orderCode is missing");
            return false;
        }
        
        if (data.getAmount() == null || data.getAmount() <= 0) {
            log.warn("Webhook amount is invalid");
            return false;
        }
        
        if (data.getTransactionCode() == null || data.getTransactionCode().isEmpty()) {
            log.warn("Webhook transactionCode is missing");
            return false;
        }
        
        if (data.getStatus() == null || data.getStatus().isEmpty()) {
            log.warn("Webhook status is missing");
            return false;
        }
        
        log.debug("Webhook validation passed for order: {}", data.getOrderCode());
        return true;
    }
    
    @Override
    public String getGatewayName() {
        return GATEWAY_NAME;
    }
    
    @Override
    public boolean isHealthy() {
        try {
            log.debug("Checking PayOS gateway health");
            
            // Simple health check: verify API key is configured
            if (paymentProperties.getPayos().getApiKey() == null || 
                paymentProperties.getPayos().getApiKey().isEmpty()) {
                log.warn("PayOS API key is not configured");
                return false;
            }
            
            log.debug("PayOS gateway is healthy");
            return true;
            
        } catch (Exception e) {
            log.error("Error checking PayOS gateway health", e);
            return false;
        }
    }
    
    /**
     * Calculate HMAC-SHA256 signature
     * 
     * @param message Message to sign
     * @param key Secret key
     * @return Base64-encoded signature
     */
    private String calculateHmacSha256(String message, String key) throws Exception {
        Mac mac = Mac.getInstance(HMAC_SHA256);
        SecretKeySpec secretKey = new SecretKeySpec(
            key.getBytes(StandardCharsets.UTF_8),
            0,
            key.length(),
            HMAC_SHA256
        );
        mac.init(secretKey);
        
        byte[] signedBytes = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signedBytes);
    }
    
    /**
     * Execute operation with retry logic and exponential backoff
     * 
     * Retry policy:
     * - Max attempts: 3 (from config)
     * - Initial delay: 1000ms (from config)
     * - Backoff multiplier: 2.0 (from config)
     * - Delays: 1s, 2s, 4s
     * 
     * Retryable errors:
     * - Socket timeout (5xx errors)
     * - Connection errors
     * - But NOT 4xx errors (client errors)
     */
    private <T> T executeWithRetry(PayOSOperation<T> operation) throws PaymentTimeoutException, PaymentGatewayException {
        PaymentProperties.Retry retryConfig = paymentProperties.getRetry();
        int maxAttempts = retryConfig.getMaxAttempts();
        long initialDelayMs = retryConfig.getInitialDelayMs();
        double backoffMultiplier = retryConfig.getBackoffMultiplier();
        
        long delay = initialDelayMs;
        PaymentGatewayException lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                log.debug("Attempt {}/{} to execute PayOS operation", attempt, maxAttempts);
                return operation.execute();
                
            } catch (ResourceAccessException e) {
                // Connection timeout or socket timeout - retry
                lastException = new PaymentGatewayException("Connection error: " + e.getMessage(), e);
                
                if (attempt < maxAttempts) {
                    log.warn("Attempt {} failed with connection error, retrying in {}ms: {}", 
                        attempt, delay, e.getMessage());
                    
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new PaymentTimeoutException("Interrupted while retrying", ie);
                    }
                    
                    delay = (long) (delay * backoffMultiplier);
                } else {
                    throw new PaymentTimeoutException("Payment gateway timeout after " + maxAttempts + " attempts", e);
                }
                
            } catch (HttpServerErrorException e) {
                // 5xx errors - retry
                lastException = new PaymentGatewayException(
                    "Server error (HTTP " + e.getRawStatusCode() + "): " + e.getMessage(),
                    e
                );
                
                if (attempt < maxAttempts) {
                    log.warn("Attempt {} failed with server error (HTTP {}), retrying in {}ms", 
                        attempt, e.getRawStatusCode(), delay);
                    
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new PaymentTimeoutException("Interrupted while retrying", ie);
                    }
                    
                    delay = (long) (delay * backoffMultiplier);
                } else {
                    throw lastException;
                }
                
            } catch (HttpClientErrorException e) {
                // 4xx errors - don't retry, throw immediately
                throw new PaymentGatewayException(
                    "Client error (HTTP " + e.getRawStatusCode() + "): " + e.getMessage(),
                    e
                );
            } catch (PaymentTimeoutException | PaymentGatewayException e) {
                // Re-throw payment exceptions as-is
                throw e;
            } catch (Exception e) {
                // Catch any other exceptions from operation.execute()
                lastException = new PaymentGatewayException("Unexpected error: " + e.getMessage(), e);
                
                if (attempt >= maxAttempts) {
                    throw lastException;
                }
            }
        }
        
        // Should not reach here, but just in case
        throw lastException != null ? lastException : 
            new PaymentGatewayException("Failed to execute PayOS operation");
    }
    
    /**
     * Build full API URL
     */
    private String buildUrl(String endpoint) {
        String baseUrl = paymentProperties.getPayos().getBaseUrl();
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        return baseUrl + endpoint;
    }
    
    /**
     * Operation interface for retry logic
     */
    @FunctionalInterface
    private interface PayOSOperation<T> {
        T execute() throws Exception;
    }
}
