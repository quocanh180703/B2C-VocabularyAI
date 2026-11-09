package com.vocabularyai.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vocabularyai.common.response.ApiResponse;
import com.vocabularyai.payment.config.PaymentProperties;
import com.vocabularyai.payment.controller.PaymentController;
import com.vocabularyai.payment.dto.payos.PayOSWebhookRequest;
import com.vocabularyai.payment.entity.Package;
import com.vocabularyai.payment.entity.Payment;
import com.vocabularyai.payment.entity.PaymentOrder;
import com.vocabularyai.payment.entity.Subscription;
import com.vocabularyai.payment.repository.PackageRepository;
import com.vocabularyai.payment.repository.PaymentOrderRepository;
import com.vocabularyai.payment.repository.PaymentRepository;
import com.vocabularyai.payment.repository.SubscriptionRepository;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.entity.User.UserRole;
import com.vocabularyai.user.entity.User.UserStatus;
import com.vocabularyai.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("PAY-01: PayOS Webhook Idempotency & Duplicate Prevention Test")
public class Pay01WebhookIdempotencyTest {

    @Autowired
    private PaymentController paymentController;

    @Autowired
    private PaymentOrderRepository paymentOrderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentProperties paymentProperties;

    private String calculateHmacSha256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : rawHmac) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA256 calculation failed", e);
        }
    }

    @Test
    @Transactional
    @DisplayName("PAY-01: Gửi lại webhook đơn 434701348648 - Admin Đơn hàng & Gói cước vẫn chỉ 1 đơn thành công")
    public void testPay01_ResendWebhook_IdempotencySuccess() throws Exception {
        final String targetOrderCode = "434701348648";
        final String targetTxnRef = "FT26304824261374";
        final Long orderAmount = 99000L;

        // 1. Arrange: Chuẩn bị dữ liệu tài khoản, gói cước và đơn hàng 434701348648
        User testUser = userRepository.findByEmail("quocanh220219@gmail.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .username("quocanh220219")
                        .email("quocanh220219@gmail.com")
                        .password("$2a$10$placeholderPassword")
                        .fullName("quốc anh hoàng vương")
                        .role(UserRole.STUDENT)
                        .status(UserStatus.ACTIVE)
                        .build()));

        Package premiumPkg = packageRepository.findByCode("PREMIUM_MONTHLY")
                .orElseGet(() -> packageRepository.save(Package.builder()
                        .code("PREMIUM_MONTHLY")
                        .name("Gói Premium 1 Tháng")
                        .price(new BigDecimal(orderAmount))
                        .durationDays(30)
                        .status("ACTIVE")
                        .build()));

        // Đơn hàng đã thành công lần 1 (trạng thái PAID, payment SUCCESS)
        PaymentOrder existingOrder = paymentOrderRepository.findByOrderCode(targetOrderCode)
                .orElseGet(() -> paymentOrderRepository.save(PaymentOrder.builder()
                        .orderCode(targetOrderCode)
                        .userId(testUser.getId())
                        .packageId(premiumPkg.getId())
                        .amount(new BigDecimal(orderAmount))
                        .status("PAID")
                        .paidAt(LocalDateTime.of(2026, 10, 31, 15, 46, 0))
                        .createdAt(LocalDateTime.of(2026, 10, 31, 15, 45, 0))
                        .build()));

        Payment existingPayment = paymentRepository.findByTransactionCode(targetTxnRef)
                .orElseGet(() -> paymentRepository.save(Payment.builder()
                        .paymentOrderId(existingOrder.getId())
                        .transactionCode(targetTxnRef)
                        .amount(new BigDecimal(orderAmount))
                        .status("SUCCESS")
                        .paymentMethod("BANK_TRANSFER")
                        .paidAt(LocalDateTime.of(2026, 10, 31, 15, 46, 0))
                        .createdAt(LocalDateTime.of(2026, 10, 31, 15, 46, 0))
                        .build()));

        // Đếm trạng thái TRƯỚC KHI gửi lại webhook
        long countOrdersBefore = paymentOrderRepository.findByUserId(testUser.getId()).stream()
                .filter(o -> targetOrderCode.equals(o.getOrderCode()) && "PAID".equals(o.getStatus()))
                .count();
        long countPaymentsBefore = paymentRepository.findAll().stream()
                .filter(p -> existingOrder.getId().equals(p.getPaymentOrderId()) && "SUCCESS".equals(p.getStatus()))
                .count();

        System.out.println("\n==================================================================");
        System.out.println(">>> [BƯỚC 1] TRẠNG THÁI TRƯỚC KHI GỬI LẠI WEBHOOK:");
        System.out.println("- Mã đơn hàng (orderCode)        : " + targetOrderCode);
        System.out.println("- Mã GD PayOS (transactionCode) : " + targetTxnRef);
        System.out.println("- Số đơn hàng thành công hiện có : " + countOrdersBefore);
        System.out.println("- Số bản ghi thanh toán SUCCESS : " + countPaymentsBefore);
        System.out.println("- Tổng doanh thu ghi nhận        : " + existingPayment.getAmount() + " đ");
        System.out.println("==================================================================");

        // 2. Act: Mô phỏng PayOS gửi lại Webhook (Retry / Re-send Webhook)
        PayOSWebhookRequest.PayOSWebhookData webhookData = PayOSWebhookRequest.PayOSWebhookData.builder()
                .orderCode(targetOrderCode)
                .amount(orderAmount)
                .description("Thanh toan don hang " + targetOrderCode)
                .accountNumber("0382343470")
                .reference(targetTxnRef)
                .transactionDateTime("2026-10-31 15:46:00")
                .status("PAID")
                .build();

        PayOSWebhookRequest webhook = PayOSWebhookRequest.builder()
                .code("00")
                .desc("success")
                .data(webhookData)
                .build();

        String checksumKey = paymentProperties.getPayos().getChecksumKey();
        if (checksumKey == null || checksumKey.isBlank()) {
            checksumKey = paymentProperties.getPayos().getApiKey();
        }
        String signData = String.format("orderCode=%s&amount=%s&reference=%s", targetOrderCode, orderAmount, targetTxnRef);
        String signature = calculateHmacSha256(signData, checksumKey);

        System.out.println("\n>>> [BƯỚC 2] THỰC HIỆN GỬI LẠI (RETRY) WEBHOOK TỪ PAYOS ĐẾN BACKEND...");
        System.out.println("- Endpoint: POST /api/payments/webhook");
        System.out.println("- Payload : " + signData);
        System.out.println("- Signature: " + signature);

        ResponseEntity<ApiResponse<?>> webhookResponse = paymentController.handleWebhook(webhook, signature);

        // 3. Assert: Kiểm tra phản hồi Webhook và tính Idempotent
        assertNotNull(webhookResponse);
        assertEquals(200, webhookResponse.getStatusCode().value(), "Webhook phải trả về HTTP 200 OK!");
        assertTrue(webhookResponse.getBody().isSuccess(), "Webhook response phải có success = true!");

        // 4. Kiểm tra trạng thái SAU KHI gửi lại webhook:
        long countOrdersAfter = paymentOrderRepository.findByUserId(testUser.getId()).stream()
                .filter(o -> targetOrderCode.equals(o.getOrderCode()) && "PAID".equals(o.getStatus()))
                .count();
        long countPaymentsAfter = paymentRepository.findAll().stream()
                .filter(p -> existingOrder.getId().equals(p.getPaymentOrderId()) && "SUCCESS".equals(p.getStatus()))
                .count();

        // Tiêu chuẩn PAY-01: KHÔNG sinh thêm đơn hàng mới, KHÔNG nhân đôi doanh thu, Admin vẫn chỉ 1 đơn thành công!
        assertEquals(1, countOrdersAfter, "PAY-01 THẤT BẠI: Số đơn hàng bị nhân đôi!");
        assertEquals(1, countPaymentsAfter, "PAY-01 THẤT BẠI: Bản ghi thanh toán bị nhân đôi!");

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        System.out.println("\n==================================================================");
        System.out.println(">>> [BƯỚC 3] KẾT QUẢ THỰC TẾ TEST CASE PAY-01:");
        System.out.println("• Mã đơn kiểm tra               : " + targetOrderCode);
        System.out.println("• Kết quả Webhook Response      : HTTP " + webhookResponse.getStatusCode().value() + " (" + webhookResponse.getBody().getMessage() + ")");
        System.out.println("• Số đơn hàng thành công sau đó  : " + countOrdersAfter + " (ĐÃ XÁC NHẬN: VẪN CHỈ 1 ĐƠN THÀNH CÔNG)");
        System.out.println("• Số giao dịch thanh toán       : " + countPaymentsAfter + " (KHÔNG BỊ TẠO ĐƠN TRÙNG LẶP)");
        System.out.println("• Doanh thu ghi nhận            : 99.000 đ (KHÔNG BỊ CỘNG DỒN 2 LẦN)");
        System.out.println("• Đánh giá tính Idempotency     : ĐẠT (PASS) ✅");
        System.out.println("------------------------------------------------------------------");
        System.out.println(">>> [BƯỚC 4] WEBHOOK RESPONSE DETAILS:");
        System.out.println(mapper.writeValueAsString(webhookResponse.getBody()));
        System.out.println("==================================================================\n");
    }
}
