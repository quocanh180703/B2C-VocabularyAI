package com.vocabularyai.payment.service;

import com.vocabularyai.auth.service.EmailService;
import com.vocabularyai.payment.config.PaymentProperties;
import com.vocabularyai.payment.dto.payos.PayOSCreatePaymentLinkRequest;
import com.vocabularyai.payment.dto.payos.PayOSCreatePaymentLinkResponse;
import com.vocabularyai.payment.dto.payos.PayOSPaymentInfoResponse;
import com.vocabularyai.payment.dto.payos.PayOSWebhookRequest;
import com.vocabularyai.payment.dto.payos.PaymentEmailData;
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
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
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
    private final EmailService emailService;
    private final UserRepository userRepository;

    @Value("${app.frontend-url}")
    private String frontendUrl;

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
                    "User already has active subscription. Cancel existing subscription first.");
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
                    "Payment redirect URLs not configured. Please set payment.return-url and payment.cancel-url in application.yml");
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
            return true;
        }

        // Verify amount matches
        if (amount != null && amount.longValue() != paymentOrder.getAmount().longValue()) {
            log.error("Amount mismatch for orderCode: {}, expected: {}, got: {}",
                    orderCode, paymentOrder.getAmount(), amount);
            throw new IllegalArgumentException("Payment amount mismatch");
        }

        processOrderActivation(paymentOrder, transactionCode, data.getTransferType(), new BigDecimal(amount));
        log.info("Payment webhook processed successfully for orderCode: {}", orderCode);
        return true;
    }

    @Override
    @Transactional
    public PaymentOrder verifyAndActivatePayment(String orderCode, Long currentUserId) {
        log.info("Verifying and activating payment for orderCode: {}, currentUserId: {}", orderCode, currentUserId);

        PaymentOrder paymentOrder = paymentOrderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("PaymentOrder not found: " + orderCode));

        if (currentUserId != null && !paymentOrder.getUserId().equals(currentUserId)) {
            log.warn("User {} is verifying order {} of user {}", currentUserId, orderCode, paymentOrder.getUserId());
        }

        // If already paid and subscription is active, return immediately
        if ("PAID".equals(paymentOrder.getStatus())) {
            boolean active = hasActiveSubscription(paymentOrder.getUserId());
            if (active) {
                log.info("Order {} is already PAID and active subscription exists", orderCode);
                return paymentOrder;
            }
        }

        // Query PayOS API directly
        PayOSPaymentInfoResponse info = paymentGatewayService.getPaymentInfo(orderCode);
        if (info == null || info.getData() == null) {
            log.warn("PayOS returned no data for orderCode: {}", orderCode);
            return paymentOrder;
        }

        String payosStatus = info.getData().getStatus();
        log.info("PayOS status for orderCode {}: {}", orderCode, payosStatus);

        if ("PAID".equalsIgnoreCase(payosStatus)) {
            String txnRef = null;
            if (info.getData().getTransactions() != null && !info.getData().getTransactions().isEmpty()) {
                txnRef = info.getData().getTransactions().get(0).getReference();
            }
            if (txnRef == null || txnRef.isBlank()) {
                txnRef = info.getData().getId();
            }
            if (txnRef == null || txnRef.isBlank()) {
                txnRef = orderCode;
            }

            BigDecimal paidAmount = info.getData().getAmount() != null
                    ? new BigDecimal(info.getData().getAmount())
                    : paymentOrder.getAmount();

            return processOrderActivation(paymentOrder, txnRef, "BANK_TRANSFER", paidAmount);
        } else if ("CANCELLED".equalsIgnoreCase(payosStatus)) {
            paymentOrder.setStatus("CANCELLED");
            return paymentOrderRepository.save(paymentOrder);
        } else if ("EXPIRED".equalsIgnoreCase(payosStatus)) {
            paymentOrder.setStatus("EXPIRED");
            return paymentOrderRepository.save(paymentOrder);
        }

        return paymentOrder;
    }

    /**
     * Common order activation logic:
     * 1. Mark order PAID
     * 2. Save Payment
     * 3. Upsert Subscription (handling uk_subscription_user_id)
     * 4. Send payment-success invoice email
     */
    @Transactional
    public PaymentOrder processOrderActivation(PaymentOrder paymentOrder, String transactionCode,
            String paymentMethod, BigDecimal amount) {
        log.info("Activating order: id={}, orderCode={}, transactionCode={}", 
                paymentOrder.getId(), paymentOrder.getOrderCode(), transactionCode);

        // 1. Update PaymentOrder
        paymentOrder.setStatus("PAID");
        if (paymentOrder.getPaidAt() == null) {
            paymentOrder.setPaidAt(LocalDateTime.now());
        }
        paymentOrder = paymentOrderRepository.save(paymentOrder);

        // 2. Find or create Payment
        String txn = (transactionCode != null && !transactionCode.isBlank()) ? transactionCode : paymentOrder.getOrderCode();
        Payment payment = paymentRepository.findByPaymentOrderId(paymentOrder.getId())
                .orElseGet(() -> paymentRepository.findByTransactionCode(txn).orElse(null));

        if (payment == null) {
            payment = Payment.builder()
                    .paymentOrderId(paymentOrder.getId())
                    .transactionCode(txn)
                    .amount(amount != null ? amount : paymentOrder.getAmount())
                    .status("SUCCESS")
                    .paymentMethod(paymentMethod != null ? paymentMethod : "BANK_TRANSFER")
                    .paidAt(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            payment = paymentRepository.save(payment);
            log.info("Payment record created: id={}, transactionCode={}", payment.getId(), txn);
        }

        // 3. Upsert Subscription
        final Long pkgId = paymentOrder.getPackageId();
        Package pkg = packageRepository.findById(pkgId)
                .orElseThrow(() -> new IllegalArgumentException("Package not found: " + pkgId));

        Optional<Subscription> existingSubOpt = subscriptionRepository.findByUserId(paymentOrder.getUserId());
        Subscription subscription;
        if (existingSubOpt.isPresent()) {
            subscription = existingSubOpt.get();
            LocalDateTime currentEnd = subscription.getEndAt();
            LocalDateTime baseTime = (subscription.isActive() && currentEnd != null && currentEnd.isAfter(LocalDateTime.now()))
                    ? currentEnd
                    : LocalDateTime.now();
            subscription.setPackageId(pkg.getId());
            subscription.setPaymentId(payment.getId());
            subscription.setStatus("ACTIVE");
            subscription.setStartAt(LocalDateTime.now());
            subscription.setEndAt(baseTime.plusDays(pkg.getDurationDays()));
            subscription.setUpdatedAt(LocalDateTime.now());
            log.info("Existing subscription updated/extended: id={}, userId={}, endAt={}", subscription.getId(),
                    paymentOrder.getUserId(), subscription.getEndAt());
        } else {
            LocalDateTime startAt = LocalDateTime.now();
            LocalDateTime endAt = startAt.plusDays(pkg.getDurationDays());
            subscription = Subscription.builder()
                    .userId(paymentOrder.getUserId())
                    .packageId(pkg.getId())
                    .paymentId(payment.getId())
                    .status("ACTIVE")
                    .startAt(startAt)
                    .endAt(endAt)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            log.info("New subscription created: userId={}, endAt={}", paymentOrder.getUserId(), endAt);
        }
        subscription = subscriptionRepository.save(subscription);

        // 4. Send payment success email
        try {
            sendPaymentSuccessEmail(paymentOrder, payment, pkg, subscription);
        } catch (Exception e) {
            log.error("Failed to send payment success email for orderCode: {}", paymentOrder.getOrderCode(), e);
        }

        return paymentOrder;
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
                subscription.getEndAt());
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

    @Override
    public java.util.List<?> getActivePackages() {
        return packageRepository.findByStatus("ACTIVE");
    }

    @Override
    public Subscription getSubscription(Long subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found: " + subscriptionId));
    }

    @Override
    public PaymentService.Page<?> getPaymentHistory(Long userId, int page, int size) {
        java.util.List<PaymentOrder> orders = paymentOrderRepository.findByUserId(userId);

        // Manual pagination
        int totalElements = orders.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        // Sort by created date descending
        orders.sort((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()));

        // Get page
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, orders.size());

        if (fromIndex >= orders.size()) {
            return new PaymentService.Page<>(java.util.Collections.emptyList(), totalElements, totalPages, page, size);
        }

        java.util.List<PaymentOrder> pageContent = orders.subList(fromIndex, toIndex);

        // Enrich with package name
        java.util.List<Map<String, Object>> enrichedContent = pageContent.stream().map(order -> {
            Package pkg = packageRepository.findById(order.getPackageId())
                    .orElse(null);

            Map<String, Object> entry = new java.util.HashMap<>();
            entry.put("id", order.getId());
            entry.put("orderCode", order.getOrderCode());
            entry.put("amount", order.getAmount());
            entry.put("status", order.getStatus());
            entry.put("paidAt", order.getPaidAt());
            entry.put("packageName", pkg != null ? pkg.getName() : "Unknown");
            entry.put("transactionCode",
                    paymentRepository.findByPaymentOrderId(order.getId())
                            .map(Payment::getTransactionCode)
                            .orElse(null));
            entry.put("createdAt", order.getCreatedAt());
            return entry;
        }).collect(java.util.stream.Collectors.toList());

        return new PaymentService.Page<>(enrichedContent, totalElements, totalPages, page, size);
    }

    /**
     * Generate unique numeric order code required by PayOS
     * Example: 438271000512
     */
    private String generateOrderCode() {
        long timestampPart = System.currentTimeMillis() % 1000000000L;
        int randomPart = (int) (Math.random() * 899 + 100);
        return String.valueOf(timestampPart * 1000L + randomPart);
    }

    /**
     * Send payment success email to customer
     * 
     * Extracts customer information from User entity and builds PaymentEmailData
     * to render payment-success.html template via EmailService
     * 
     * @param paymentOrder The payment order containing userId
     * @param payment      The payment record with transaction details
     * @param pkg          The package purchased
     * @param subscription The created subscription with dates
     */
    private void sendPaymentSuccessEmail(PaymentOrder paymentOrder, Payment payment, Package pkg,
            Subscription subscription) {
        log.debug("Preparing payment success email for orderId: {}", paymentOrder.getId());

        // Get user information
        User user = userRepository.findById(paymentOrder.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + paymentOrder.getUserId()));

        // Build payment email data
        PaymentEmailData emailData = PaymentEmailData.builder()
                .customerName(user.getFullName() != null ? user.getFullName() : user.getUsername())
                .email(user.getEmail())
                .orderCode(paymentOrder.getOrderCode())
                .paidDate(payment.getPaidAt())
                .packageName(pkg.getName())
                .duration(pkg.getDurationDays())
                .subscriptionStartDate(subscription.getStartAt())
                .subscriptionEndDate(subscription.getEndAt())
                .amount(payment.getAmount())
                .transactionCode(payment.getTransactionCode())
                .frontendUrl(frontendUrl)
                .build();

        // Send email via EmailService
        emailService.sendPaymentSuccessEmail(emailData);
        log.info("Payment success email sent for orderId: {}, userId: {}", paymentOrder.getId(),
                paymentOrder.getUserId());
    }
}
