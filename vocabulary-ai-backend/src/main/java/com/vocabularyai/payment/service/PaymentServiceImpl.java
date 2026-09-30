package com.vocabularyai.payment.service;

import com.vocabularyai.payment.config.PaymentProperties;
import com.vocabularyai.payment.dto.payos.PayOSCreatePaymentLinkRequest;
import com.vocabularyai.payment.dto.payos.PayOSCreatePaymentLinkResponse;
import com.vocabularyai.payment.dto.payos.PayOSWebhookRequest;
import com.vocabularyai.payment.entity.Package;
import com.vocabularyai.payment.entity.Payment;
import com.vocabularyai.payment.entity.PaymentOrder;
import com.vocabularyai.payment.entity.Subscription;
import com.vocabularyai.payment.exception.DuplicatePaymentException;
import com.vocabularyai.payment.exception.InvalidSignatureException;
import com.vocabularyai.payment.repository.PackageRepository;
import com.vocabularyai.payment.repository.PaymentOrderRepository;
import com.vocabularyai.payment.repository.PaymentRepository;
import com.vocabularyai.payment.repository.SubscriptionRepository;
import com.vocabularyai.payment.service.gateway.PaymentGatewayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Payment Service Implementation
 * 
 * Orchestrates payment operations:
 * - Payment initiation
 * - Webhook handling
 * - Subscription management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    
    private final PaymentGatewayService paymentGatewayService;
    private final PackageRepository packageRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentProperties paymentProperties;
    
    @Override
    @Transactional
    public PayOSCreatePaymentLinkResponse initiatePayment(
            Long userId,
            Long packageId) {
        
        log.info("Initiating payment for userId: {}, packageId: {}", userId, packageId);
        
        // Validate package exists
        Package pkg = packageRepository.findById(packageId)
            .orElseThrow(() -> new IllegalArgumentException("Package not found: " + packageId));
        
        if (!"ACTIVE".equals(pkg.getStatus())) {
            throw new IllegalArgumentException("Package is not available: " + packageId);
        }
        
        log.debug("Package validated: {}", pkg.getCode());
        
        // Check if user already has active subscription
        Optional<Subscription> existingSubscription = subscriptionRepository.findByUserId(userId);
        if (existingSubscription.isPresent() && "ACTIVE".equals(existingSubscription.get().getStatus())) {
            throw new IllegalStateException(
                "User already has active subscription. Cancel existing subscription first."
            );
        }
        
        log.debug("No active subscription found for user: {}", userId);
        
        // Generate unique order code
        String orderCode = generateOrderCode();
        log.debug("Generated order code: {}", orderCode);
        
        // Create PaymentOrder in database
        PaymentOrder paymentOrder = PaymentOrder.builder()
            .userId(userId)
            .packageId(packageId)
            .orderCode(orderCode)
            .amount(pkg.getPrice())
            .status("PENDING")
            .expiredAt(LocalDateTime.now().plusMinutes(15)) // 15 minutes expiration
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        paymentOrder = paymentOrderRepository.save(paymentOrder);
        log.info("PaymentOrder created: id={}, orderCode={}", paymentOrder.getId(), orderCode);
        
        // Get return and cancel URLs from backend configuration
        String returnUrl = paymentProperties.getReturnUrl();
        String cancelUrl = paymentProperties.getCancelUrl();
        
        if (returnUrl == null || returnUrl.isBlank() || cancelUrl == null || cancelUrl.isBlank()) {
            throw new IllegalStateException(
                "Payment redirect URLs not configured. Please set payment.return-url and payment.cancel-url in application.yml"
            );
        }
        
        log.debug("Using configured redirect URLs - returnUrl: {}, cancelUrl: {}", returnUrl, cancelUrl);
        
        // Create payment link with gateway
        PayOSCreatePaymentLinkRequest gatewayRequest = PayOSCreatePaymentLinkRequest.builder()
            .orderCode(orderCode)
            .amount(pkg.getPrice())
            .description(pkg.getName() + " - " + pkg.getPrice().intValue() + " VND")
            .returnUrl(returnUrl)
            .cancelUrl(cancelUrl)
            .expiredAt(paymentOrder.getExpiredAt())
            .build();
        
        log.debug("Calling PaymentGatewayService to create payment link");
        PayOSCreatePaymentLinkResponse gatewayResponse = paymentGatewayService.createPaymentLink(gatewayRequest);
        
        if (!gatewayResponse.isSuccess()) {
            throw new RuntimeException("Failed to create payment link with gateway: " + gatewayResponse.getDesc());
        }
        
        log.debug("Payment link created: checkoutUrl={}", gatewayResponse.getData().getCheckoutUrl());
        
        // Update PaymentOrder with payment URL
        paymentOrder.setPaymentUrl(gatewayResponse.getData().getCheckoutUrl());
        paymentOrderRepository.save(paymentOrder);
        
        log.info("Payment initiation completed successfully for userId: {}, orderCode: {}", userId, orderCode);
        
        return gatewayResponse;
    }
    
    @Override
    @Transactional
    public boolean handlePaymentWebhook(PayOSWebhookRequest webhook, String signature) {
        log.info("Handling payment webhook");
        
        // Validate webhook structure
        if (!paymentGatewayService.validateWebhook(webhook)) {
            log.warn("Webhook validation failed");
            throw new IllegalArgumentException("Invalid webhook structure");
        }
        
        PayOSWebhookRequest.PayOSWebhookData data = webhook.getData();
        String orderCode = data.getOrderCode();
        Long amount = data.getAmount();
        String transactionCode = data.getTransactionCode();
        
        log.debug("Processing webhook for orderCode: {}, transactionCode: {}", orderCode, transactionCode);
        
        // Verify webhook signature
        try {
            paymentGatewayService.verifyWebhookSignature(orderCode, amount, transactionCode, signature);
            log.debug("Webhook signature verified");
        } catch (InvalidSignatureException e) {
            log.error("Webhook signature verification failed for orderCode: {}", orderCode);
            throw e;
        }
        
        // Skip if not a paid webhook
        if (!webhook.isPaymentReceived()) {
            log.info("Webhook is not a payment received event, skipping: code={}, status={}", 
                webhook.getCode(), data.getStatus());
            return true;
        }
        
        // Look up PaymentOrder
        PaymentOrder paymentOrder = paymentOrderRepository.findByOrderCode(orderCode)
            .orElseThrow(() -> {
                log.error("PaymentOrder not found for orderCode: {}", orderCode);
                return new IllegalArgumentException("PaymentOrder not found: " + orderCode);
            });
        
        log.debug("PaymentOrder found: id={}", paymentOrder.getId());
        
        // Check for duplicate (idempotency)
        if (paymentRepository.existsByTransactionCode(transactionCode)) {
            log.warn("Payment already processed for transactionCode: {}, returning success for idempotency", 
                transactionCode);
            throw new DuplicatePaymentException("Payment already processed: " + transactionCode);
        }
        
        // Verify amount matches
        if (amount != null && amount.longValue() != paymentOrder.getAmount().longValue()) {
            log.error("Amount mismatch for orderCode: {}, expected: {}, got: {}", 
                orderCode, paymentOrder.getAmount(), amount);
            throw new IllegalArgumentException("Payment amount mismatch");
        }
        
        // Update PaymentOrder status
        paymentOrder.setStatus("PAID");
        paymentOrder.setPaidAt(LocalDateTime.now());
        paymentOrderRepository.save(paymentOrder);
        
        log.debug("PaymentOrder status updated to PAID");
        
        // Create Payment record
        Payment payment = Payment.builder()
            .paymentOrderId(paymentOrder.getId())
            .transactionCode(transactionCode)
            .amount(new BigDecimal(amount))
            .status("SUCCESS")
            .paymentMethod(data.getTransferType())
            .paidAt(LocalDateTime.now())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        payment = paymentRepository.save(payment);
        log.info("Payment record created: id={}, transactionCode={}", payment.getId(), transactionCode);
        
        // Create Subscription
        Package pkg = packageRepository.findById(paymentOrder.getPackageId())
            .orElseThrow(() -> new IllegalArgumentException("Package not found"));
        
        LocalDateTime startAt = LocalDateTime.now();
        LocalDateTime endAt = startAt.plusDays(pkg.getDurationDays());
        
        Subscription subscription = Subscription.builder()
            .userId(paymentOrder.getUserId())
            .packageId(paymentOrder.getPackageId())
            .paymentId(payment.getId())
            .status("ACTIVE")
            .startAt(startAt)
            .endAt(endAt)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        subscription = subscriptionRepository.save(subscription);
        log.info("Subscription created: id={}, userId={}, endAt={}", subscription.getId(), 
            paymentOrder.getUserId(), endAt);
        
        log.info("Payment webhook processed successfully for orderCode: {}", orderCode);
        
        return true;
    }
    
    @Override
    public PaymentOrder getPaymentOrder(Long orderId) {
        return paymentOrderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("PaymentOrder not found: " + orderId));
    }
    
    @Override
    public Subscription getUserSubscription(Long userId) {
        return subscriptionRepository.findByUserId(userId).orElse(null);
    }
    
    @Override
    public boolean hasActiveSubscription(Long userId) {
        Optional<Subscription> subscription = subscriptionRepository.findByUserId(userId);
        
        if (subscription.isEmpty()) {
            return false;
        }
        
        return subscription.get().isActive();
    }
    
    @Override
    public SubscriptionStatus getSubscriptionStatus(Long userId) {
        Optional<Subscription> subscriptionOpt = subscriptionRepository.findByUserId(userId);
        
        if (subscriptionOpt.isEmpty()) {
            return null;
        }
        
        Subscription subscription = subscriptionOpt.get();
        Package pkg = packageRepository.findById(subscription.getPackageId())
            .orElseThrow(() -> new IllegalArgumentException("Package not found"));
        
        LocalDateTime now = LocalDateTime.now();
        long remainingDays = java.time.temporal.ChronoUnit.DAYS.between(now, subscription.getEndAt());
        
        return new SubscriptionStatus(
            subscription.getId(),
            userId,
            pkg.getName(),
            subscription.getStatus(),
            Math.max(0, remainingDays),
            subscription.getEndAt()
        );
    }
    
    @Override
    @Transactional
    public boolean cancelSubscription(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new IllegalArgumentException("Subscription not found: " + subscriptionId));
        
        subscription.setStatus("CANCELLED");
        subscription.setUpdatedAt(LocalDateTime.now());
        subscriptionRepository.save(subscription);
        
        log.info("Subscription cancelled: id={}", subscriptionId);
        
        return true;
    }
    
    /**
     * Generate unique order code
     * Format: ORD_{timestamp}_{randomId}
     */
    private String generateOrderCode() {
        long timestamp = System.currentTimeMillis();
        int randomId = (int) (Math.random() * 100000);
        return String.format("ORD_%d_%d", timestamp, randomId);
    }
}
