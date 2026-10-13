package com.vocabularyai.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Package Detail Response DTO
 * 
 * Complete package information for admin detail view
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PackageDetailResponse {
    
    private Long id;
    private String code;
    private String name;
    private BigDecimal price;
    private Integer durationDays;
    private String status;
    private String description;
    private Long totalPurchases;
    private Long activeSubscriptions;
    private Double revenueTotal;
    private String createdAt;
    private String updatedAt;
}
