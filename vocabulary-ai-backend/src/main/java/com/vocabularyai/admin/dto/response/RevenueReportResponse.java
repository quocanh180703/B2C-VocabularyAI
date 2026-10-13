package com.vocabularyai.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Revenue Report Response DTO
 * 
 * Represents financial/revenue metrics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueReportResponse {
    
    private Long totalTransactions;
    
    private BigDecimal totalRevenue;
    
    private BigDecimal avgTransactionValue;
    
    private Long uniqueCustomers;
}
