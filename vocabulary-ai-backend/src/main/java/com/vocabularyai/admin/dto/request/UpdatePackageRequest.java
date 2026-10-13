package com.vocabularyai.admin.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for updating existing package
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePackageRequest {
    
    /**
     * Package name (optional for update)
     */
    private String name;
    
    /**
     * Package price (optional for update)
     */
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;
    
    /**
     * Duration in days (optional for update)
     */
    @Min(value = 1, message = "Duration must be at least 1 day")
    private Integer durationDays;
    
    /**
     * Package status (optional for update)
     */
    @Pattern(regexp = "ACTIVE|INACTIVE|ARCHIVED", message = "Status must be ACTIVE, INACTIVE, or ARCHIVED")
    private String status;
    
    /**
     * Package description (optional for update)
     */
    private String description;
}
