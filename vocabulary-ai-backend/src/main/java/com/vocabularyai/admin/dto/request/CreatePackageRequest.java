package com.vocabularyai.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating new package
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePackageRequest {
    
    /**
     * Unique package code (e.g., PREMIUM_MONTHLY)
     */
    @NotBlank(message = "Package code is required")
    @Pattern(regexp = "^[A-Z_0-9]+$", message = "Code must contain only uppercase letters, numbers, and underscores")
    private String code;
    
    /**
     * Package name (e.g., Premium Monthly)
     */
    @NotBlank(message = "Package name is required")
    private String name;
    
    /**
     * Package price in VND
     */
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;
    
    /**
     * Subscription duration in days
     */
    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 day")
    private Integer durationDays;
    
    /**
     * Package status: ACTIVE, INACTIVE, ARCHIVED
     */
    @Pattern(regexp = "ACTIVE|INACTIVE|ARCHIVED", message = "Status must be ACTIVE, INACTIVE, or ARCHIVED")
    private String status;
    
    /**
     * Package description (optional)
     */
    private String description;
}
