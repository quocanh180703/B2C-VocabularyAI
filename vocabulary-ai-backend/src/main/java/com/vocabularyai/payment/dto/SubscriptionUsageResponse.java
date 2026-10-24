package com.vocabularyai.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionUsageResponse {

    @JsonProperty("isVip")
    private boolean isVip;

    @JsonProperty("isFreeUsageAtLeastHalf")
    private boolean isFreeUsageAtLeastHalf;

    private String resetAt;

    private Map<String, FeatureQuotaDto> features;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeatureQuotaDto {
        private Integer limit;     // null if VIP
        private int used;
        private Integer remaining; // null if VIP
    }
}
