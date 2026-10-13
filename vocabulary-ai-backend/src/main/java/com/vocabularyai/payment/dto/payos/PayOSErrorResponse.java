package com.vocabularyai.payment.dto.payos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Error response DTO from PayOS API
 * 
 * PayOS returns errors in this format:
 * {
 *   "code": "400",
 *   "desc": "Invalid orderCode",
 *   "data": null
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
public class PayOSErrorResponse {
    
    /**
     * Error code
     * "400" = bad request
     * "401" = unauthorized
     * "403" = forbidden
     * "404" = not found
     * "500" = server error
     * etc.
     */
    private String code;
    
    /**
     * Error description
     */
    private String desc;
    
    /**
     * Additional error data
     */
    private Object data;
    
    /**
     * Check if this is an error response
     */
    public boolean isError() {
        return code != null && !code.equals("00");
    }
}
