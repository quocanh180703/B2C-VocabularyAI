package com.vocabularyai.auth.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO cho bước 1 của đăng ký: Xác thực thông tin
 * 
 * Bước này kiểm tra email và username có hợp lệ không,
 * sau đó gửi OTP đến email
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidateRegisterRequest {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Username không được để trống")
    @Size(min = 3, max = 100, message = "Username phải từ 3 đến 100 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Username chỉ chứa chữ cái, số, dấu gạch ngang và dấu gạch dưới")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8, max = 255, message = "Mật khẩu phải từ 8 đến 255 ký tự")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$", message = "Mật khẩu phải chứa chữ hoa, chữ thường, số và ký tự đặc biệt (@$!%*?&)")
    private String password;

    @NotBlank(message = "Tên đầy đủ không được để trống")
    @Size(min = 2, max = 150, message = "Tên đầy đủ phải từ 2 đến 150 ký tự")
    private String fullName;
}
