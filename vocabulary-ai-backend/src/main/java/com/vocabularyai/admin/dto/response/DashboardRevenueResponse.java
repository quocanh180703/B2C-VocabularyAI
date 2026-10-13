package com.vocabularyai.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Dashboard Revenue Response DTO
 * 
 * Provides detailed revenue metrics and trends
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Revenue metrics including total, today, and breakdown")
public class DashboardRevenueResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "Total revenue all-time in VND", example = "450000000")
    private BigDecimal totalRevenue;
    
    @Schema(description = "Revenue from today in VND", example = "5000000")
    private BigDecimal revenueToday;
    
    @Schema(description = "Revenue from this month in VND", example = "120000000")
    private BigDecimal revenueThisMonth;
    
    @Schema(description = "Number of successful payments all-time", example = "350")
    private long totalPayments;
    
    @Schema(description = "Number of payments today", example = "12")
    private long paymentsToday;
    
    @Schema(description = "Average payment amount in VND", example = "1285714")
    private BigDecimal averagePaymentAmount;
    
    @Schema(description = "Most popular package ID", example = "3")
    private Long popularPackageId;
    
    @Schema(description = "Most popular package name", example = "Premium 3 Months")
    private String popularPackageName;
}
