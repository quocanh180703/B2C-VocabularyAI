package com.vocabularyai.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Response DTO after creating or fetching an instructor account.
 * Sensitive data like password or hash is never returned.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Instructor account response")
public class InstructorResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "User ID", example = "42")
    private Long id;

    @Schema(description = "User ID alias", example = "42")
    private Long userId;

    @Schema(description = "Username", example = "teacher_lan")
    private String username;

    @Schema(description = "Email address", example = "teacher@example.com")
    private String email;

    @Schema(description = "Full name", example = "Nguyễn Thị Lan")
    private String fullName;

    @Schema(description = "User role", example = "INSTRUCTOR")
    private String role;

    @Schema(description = "User status", example = "ACTIVE")
    private String status;

    @Schema(description = "Account creation date")
    private LocalDateTime createdAt;
}
