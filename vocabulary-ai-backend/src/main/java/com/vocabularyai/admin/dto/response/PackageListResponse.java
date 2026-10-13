package com.vocabularyai.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Package List Response DTO
 * 
 * Used for admin package list view
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PackageListResponse {
    
    private Long id;
    private String code;
    private String name;
    private BigDecimal price;
    private Integer durationDays;
    private String status;
    private String description;
    private Long purchaseCount;
    private Long activeCount;
    private Double revenue;
    private String createdAt;
    private String updatedAt;
}
