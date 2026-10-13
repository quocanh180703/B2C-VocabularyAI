package com.vocabularyai.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Package Statistics Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PackageStatsResponse {
    
    // Package counts
    private Long totalPackages;
    private Long activePackages;
    private Long inactivePackages;
    private Long archivedPackages;
    
    // Subscription metrics
    private Long totalSubscriptions;
    private Long activeSubscriptions;
    
    // Revenue
    private Double totalRevenue;
}
