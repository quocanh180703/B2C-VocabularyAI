package com.vocabularyai.payment.dto.payos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO from PayOS when querying payment request details
 * GET /v2/payment-requests/{id}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayOSPaymentInfoResponse {

    private String code;
    private String desc;
    private PayOSPaymentInfoData data;
    private String signature;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PayOSPaymentInfoData {
        private String id;
        private Long orderCode;
        private Long amount;
        private Long amountPaid;
        private Long amountRemaining;
        private String status; // "PAID", "PENDING", "CANCELLED", "EXPIRED"
        private String createdAt;
        private List<PayOSTransaction> transactions;
        private String canceledAt;
        private String cancellationReason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PayOSTransaction {
        private String accountNumber;
        private Long amount;
        private String counterAccountBankId;
        private String counterAccountBankName;
        private String counterAccountName;
        private String counterAccountNumber;
        private String description;
        private String reference;
        private String transactionDateTime;
    }

    public boolean isSuccess() {
        return "00".equals(code) && data != null;
    }
}
