package com.vocabularyai.admin.dto.request;

import com.vocabularyai.common.validation.ValidPassword;
import com.vocabularyai.common.validation.ValidationConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating an instructor account by Admin.
 * Notice: Role is NOT accepted from client; server always assigns INSTRUCTOR.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInstructorRequest {

    @NotBlank(message = ValidationConstants.USERNAME_NOT_BLANK)
    @Size(min = 3, max = 100, message = ValidationConstants.USERNAME_SIZE)
    @Pattern(regexp = ValidationConstants.USERNAME_REGEX, message = ValidationConstants.USERNAME_PATTERN)
    private String username;

    @NotBlank(message = ValidationConstants.EMAIL_NOT_BLANK)
    @Email(message = ValidationConstants.EMAIL_INVALID)
    private String email;

    @NotBlank(message = ValidationConstants.PASSWORD_NOT_BLANK)
    @ValidPassword
    private String password;

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 150, message = ValidationConstants.FULL_NAME_SIZE)
    private String fullName;
}

