package com.vocabularyai.payment.service.gateway;

import com.vocabularyai.payment.config.PaymentProperties;
import com.vocabularyai.payment.dto.payos.*;
import com.vocabularyai.payment.exception.InvalidSignatureException;
import com.vocabularyai.payment.exception.PaymentGatewayException;
import com.vocabularyai.payment.exception.PaymentTimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
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
import java.text.Normalizer;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

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
    private static final String PAYMENT_LINK_ENDPOINT = "/v2/payment-requests";
    private static final String PAYMENT_STATUS_ENDPOINT = "/v2/payment-requests/%s";
    private static final String CANCEL_PAYMENT_ENDPOINT = "/v2/payment-requests/%s/cancel";
    
    private static final String HMAC_SHA256 = "HmacSHA256";
    
    @Override
    public PayOSCreatePaymentLinkResponse createPaymentLink(PayOSCreatePaymentLinkRequest request) {
        log.info("Creating payment link with PayOS for order: {}", request.getOrderCode());
        
        try {
            // Retry logic with exponential backoff
            return executeWithRetry(() -> {
                String url = buildUrl(PAYMENT_LINK_ENDPOINT);
                
                // PayOS requires numeric orderCode
                long orderCodeNum;
                try {
                    orderCodeNum = Long.parseLong(request.getOrderCode().replaceAll("\\D", ""));
                } catch (Exception e) {
                    orderCodeNum = System.currentTimeMillis() % 1000000000L;
                }
                
                long amount = request.getAmount().longValue();
                String cancelUrl = request.getCancelUrl();
                String returnUrl = request.getReturnUrl();
                
                // PayOS description: max 25 chars, alphanumeric / unaccented
                String desc = request.getDescription() != null ? request.getDescription() : "Thanh toan VocabAI";
                desc = removeAccents(desc);
                if (desc.length() > 25) {
                    desc = desc.substring(0, 25).trim();
                }
                
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("orderCode", orderCodeNum);
                bodyMap.put("amount", amount);
                bodyMap.put("description", desc);
                bodyMap.put("cancelUrl", cancelUrl);
                bodyMap.put("returnUrl", returnUrl);
                
                String checksumKey = paymentProperties.getPayos().getChecksumKey();
                if (checksumKey != null && !checksumKey.isBlank()) {
                    String dataToSign = String.format("amount=%d&cancelUrl=%s&description=%s&orderCode=%d&returnUrl=%s",
                        amount, cancelUrl, desc, orderCodeNum, returnUrl);
                    String signature = calculateHmacSha256Hex(dataToSign, checksumKey);
                    bodyMap.put("signature", signature);
                } else {
                    log.warn("PayOS Checksum Key is not configured in payment.payos.checksum-key!");
                }
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("x-client-id", paymentProperties.getPayos().getClientId());
                headers.set("x-api-key", paymentProperties.getPayos().getApiKey());
                
                HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(bodyMap, headers);
                
                log.debug("Calling PayOS API: POST {} with orderCode: {}", url, orderCodeNum);
                ResponseEntity<PayOSCreatePaymentLinkResponse> response = restTemplate.postForEntity(
                    url,
                    httpEntity,
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
                    throw new PaymentGatewayException("PayOS API error: " + errorDesc);
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
            throw new PaymentGatewayException("Unexpected error creating payment link: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean verifyWebhookSignature(String orderCode, Long amount, String transactionCode, String signature) {
        try {
            log.debug("Verifying webhook signature for order: {}, transactionCode: {}", orderCode, transactionCode);
            
            String message = String.format("orderCode=%s&amount=%s&reference=%s", 
                orderCode, amount, transactionCode);
            
            String key = paymentProperties.getPayos().getChecksumKey();
            if (key == null || key.isBlank()) {
                key = paymentProperties.getPayos().getApiKey();
            }
            
            String calculatedSignatureHex = calculateHmacSha256Hex(message, key);
            boolean isValid = calculatedSignatureHex.equalsIgnoreCase(signature);
            
            if (!isValid) {
                log.warn("Webhook signature mismatch for order: {}, verifying directly with PayOS API...", orderCode);
                try {
                    PayOSPaymentInfoResponse info = getPaymentInfo(orderCode);
                    if (info != null && info.getData() != null && "PAID".equalsIgnoreCase(info.getData().getStatus())) {
                        log.info("Direct PayOS API verification confirmed order {} is PAID", orderCode);
                        return true;
                    }
                } catch (Exception apiEx) {
                    log.warn("Fallback PayOS API verification failed: {}", apiEx.getMessage());
                }
                
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
    public PayOSPaymentInfoResponse getPaymentInfo(String orderCode) {
        log.info("Getting payment info from PayOS for orderCode: {}", orderCode);
        
        try {
            return executeWithRetry(() -> {
                String cleanOrderCode = orderCode != null ? orderCode.replaceAll("\\D", "") : "";
                if (cleanOrderCode.isBlank()) {
                    cleanOrderCode = orderCode;
                }
                String url = buildUrl(String.format(PAYMENT_STATUS_ENDPOINT, cleanOrderCode));
                
                HttpHeaders headers = new HttpHeaders();
                headers.set("x-client-id", paymentProperties.getPayos().getClientId());
                headers.set("x-api-key", paymentProperties.getPayos().getApiKey());
                
                HttpEntity<?> entity = new HttpEntity<>(headers);
                
                log.debug("Calling PayOS API: GET {}", url);
                ResponseEntity<PayOSPaymentInfoResponse> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    PayOSPaymentInfoResponse.class
                );
                
                if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                    throw new PaymentGatewayException("Failed to get payment info: HTTP " + response.getStatusCode());
                }
                
                PayOSPaymentInfoResponse body = response.getBody();
                if (!body.isSuccess() && body.getData() == null) {
                    throw new PaymentGatewayException("PayOS returned error: " + body.getDesc());
                }
                
                log.info("PayOS payment info retrieved for order {}: status={}", 
                    orderCode, body.getData() != null ? body.getData().getStatus() : "null");
                return body;
            });
        } catch (PaymentTimeoutException | PaymentGatewayException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to get payment info for orderCode: {}", orderCode, e);
            throw new PaymentGatewayException("Failed to get payment info: " + e.getMessage(), e);
        }
    }

    @Override
    public String getPaymentStatus(String transactionCode) {
        log.info("Getting payment status for transactionCode: {}", transactionCode);
        try {
            PayOSPaymentInfoResponse info = getPaymentInfo(transactionCode);
            if (info != null && info.getData() != null && info.getData().getStatus() != null) {
                return info.getData().getStatus();
            }
            return "UNKNOWN";
        } catch (Exception e) {
            log.warn("Could not retrieve status for transactionCode {}: {}", transactionCode, e.getMessage());
            return "UNKNOWN";
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
     * Remove Vietnamese accents and special characters
     */
    private String removeAccents(String text) {
        if (text == null) return "";
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                         .replaceAll("đ", "d")
                         .replaceAll("Đ", "D")
                         .replaceAll("[^a-zA-Z0-9 ]", " ")
                         .trim();
    }

    /**
     * Convert byte array to lowercase hex string
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Calculate HMAC-SHA256 signature in lowercase HEX format (required by PayOS)
     * 
     * @param message Message to sign
     * @param key Secret key (Checksum Key)
     * @return Hex-encoded signature
     */
    private String calculateHmacSha256Hex(String message, String key) throws Exception {
        Mac mac = Mac.getInstance(HMAC_SHA256);
        SecretKeySpec secretKey = new SecretKeySpec(
            key.getBytes(StandardCharsets.UTF_8),
            HMAC_SHA256
        );
        mac.init(secretKey);
        
        byte[] signedBytes = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(signedBytes);
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
                log.error("PayOS operation unexpected error on attempt {}: {}", attempt, e.getMessage(), e);
                throw new PaymentGatewayException("Unexpected error: " + e.getMessage(), e);
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
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://api-merchant.payos.vn";
        }
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        if (!endpoint.startsWith("/")) {
            endpoint = "/" + endpoint;
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
