package com.vocabularyai.auth.service;

import com.vocabularyai.auth.dto.request.ValidateRegisterRequest;
import com.vocabularyai.auth.dto.request.VerifyOtpRegisterRequest;
import com.vocabularyai.auth.dto.response.ValidateRegisterResponse;
import com.vocabularyai.auth.dto.response.RegisterSuccessResponse;
import com.vocabularyai.auth.entity.OtpVerification;
import com.vocabularyai.ai.entity.PendingRegistration;
import com.vocabularyai.ai.repository.PendingRegistrationRepository;
import com.vocabularyai.common.exception.BadRequestException;
import com.vocabularyai.common.exception.DuplicateResourceException;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementation của RegisterOtpService
 * 
 * Xử lý quy trình đăng ký 2 bước với OTP
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RegisterOtpServiceImpl implements RegisterOtpService {

    private final OtpService otpService;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.otp.expiry-minutes:5}")
    private int otpExpiryMinutes;

    @Override
    public ValidateRegisterResponse validateRegisterAndSendOtp(ValidateRegisterRequest request) {
        log.info("Validating registration for email: {}", request.getEmail());

        // Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Email already exists: {}", request.getEmail());
            throw DuplicateResourceException.of("Email", request.getEmail());
        }

        // Kiểm tra username đã tồn tại chưa
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Username already exists: {}", request.getUsername());
            throw DuplicateResourceException.of("Username", request.getUsername());
        }

        // Kiểm tra email đã có trong pending registrations chưa
        if (pendingRegistrationRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Email already in pending registration: {}", request.getEmail());
            // Xóa pending registration cũ
            pendingRegistrationRepository.deleteByEmail(request.getEmail());
        }

        // Kiểm tra username đã có trong pending registrations chưa
        if (pendingRegistrationRepository.findByUsername(request.getUsername()).isPresent()) {
            log.warn("Username already in pending registration: {}", request.getUsername());
            throw DuplicateResourceException.of("Username", request.getUsername());
        }

        log.info("Email and username are available. Generating OTP for email: {}", request.getEmail());

        // Tạo OTP 6 chữ số
        OtpVerification otp = otpService.generateAndSaveOtp(
                request.getEmail(),
                OtpVerification.OtpType.REGISTER);

        // Gửi OTP đến email
        try {
            emailService.sendOtpEmail(request.getEmail(), otp.getOtpCode(), "REGISTER");
            log.info("OTP email sent successfully to: {}", request.getEmail());
        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", request.getEmail(), e);
            // Xóa OTP vì không gửi được email
            otpService.invalidateOtp(request.getEmail(), OtpVerification.OtpType.REGISTER);
            throw new RuntimeException("Không thể gửi OTP đến email. Vui lòng thử lại.");
        }

        // Lưu thông tin tạm vào pending_registrations
        PendingRegistration pendingRegistration = PendingRegistration.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .fullName(request.getFullName())
                .password(passwordEncoder.encode(request.getPassword()))
                .expiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes))
                .build();

        pendingRegistrationRepository.save(pendingRegistration);
        log.info("Pending registration saved for email: {}", request.getEmail());

        log.info("Registration validation successful for email: {}", request.getEmail());

        return ValidateRegisterResponse.builder()
                .message("Mã OTP đã được gửi đến email của bạn. Vui lòng kiểm tra email trong 5 phút.")
                .email(request.getEmail())
                .expiresIn(otpExpiryMinutes * 60) // Chuyển đổi thành giây
                .build();
    }

    @Override
    public RegisterSuccessResponse verifyOtpAndCreateAccount(VerifyOtpRegisterRequest request) {
        log.info("Verifying OTP and creating account for email: {}", request.getEmail());

        // Tìm pending registration theo email
        PendingRegistration pendingRegistration = pendingRegistrationRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Pending registration not found for email: {}", request.getEmail());
                    return new BadRequestException(
                            "Không tìm thấy yêu cầu đăng ký. Vui lòng bắt đầu lại quy trình đăng ký.");
                });

        // Kiểm tra pending registration chưa hết hạn
        if (LocalDateTime.now().isAfter(pendingRegistration.getExpiresAt())) {
            log.warn("Pending registration expired for email: {}", request.getEmail());
            pendingRegistrationRepository.delete(pendingRegistration);
            throw new BadRequestException("Yêu cầu đăng ký đã hết hạn. Vui lòng bắt đầu lại quy trình đăng ký.");
        }

        // Xác minh OTP
        boolean isOtpValid = otpService.verifyOtp(
                request.getEmail(),
                request.getOtp(),
                OtpVerification.OtpType.REGISTER);

        if (!isOtpValid) {
            // Lấy số lần thử còn lại
            int remainingAttempts = otpService.getRemainingAttempts(
                    request.getEmail(),
                    OtpVerification.OtpType.REGISTER);

            if (remainingAttempts <= 0) {
                log.warn("Max OTP attempts exceeded for email: {}", request.getEmail());
                otpService.invalidateOtp(request.getEmail(), OtpVerification.OtpType.REGISTER);
                pendingRegistrationRepository.delete(pendingRegistration);
                throw new BadRequestException(
                        "Số lần nhập mã OTP đã vượt quá. Vui lòng bắt đầu lại quy trình đăng ký.");
            }

            log.warn("Invalid OTP for email: {}. Remaining attempts: {}", request.getEmail(), remainingAttempts);
            throw new BadRequestException(
                    "Mã OTP không hợp lệ. Vui lòng thử lại. Còn " + remainingAttempts + " lần thử.");
        }

        log.info("OTP verified successfully for email: {}", request.getEmail());

        // Double check email và username chưa được đăng ký
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Email already registered: {}", request.getEmail());
            pendingRegistrationRepository.delete(pendingRegistration);
            throw DuplicateResourceException.of("Email", request.getEmail());
        }

        if (userRepository.existsByUsername(pendingRegistration.getUsername())) {
            log.warn("Username already registered: {}", pendingRegistration.getUsername());
            pendingRegistrationRepository.delete(pendingRegistration);
            throw DuplicateResourceException.of("Username", pendingRegistration.getUsername());
        }

        // Tạo User mới từ pending registration
        User newUser = User.builder()
                .email(pendingRegistration.getEmail())
                .username(pendingRegistration.getUsername())
                .password(pendingRegistration.getPassword())
                .fullName(pendingRegistration.getFullName())
                .role(User.UserRole.STUDENT)
                .status(User.UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(newUser);
        log.info("User account created successfully. User ID: {}, Email: {}", savedUser.getId(), savedUser.getEmail());

        // Xóa OTP (invalidate) để không thể dùng lại
        otpService.invalidateOtp(request.getEmail(), OtpVerification.OtpType.REGISTER);
        log.info("OTP invalidated for email: {}", request.getEmail());

        // Xóa pending registration
        pendingRegistrationRepository.delete(pendingRegistration);
        log.info("Pending registration deleted for email: {}", request.getEmail());

        // Gửi email xác nhận đăng ký thành công
        try {
            emailService.sendRegistrationSuccessEmail(
                    savedUser.getEmail(),
                    savedUser.getUsername(),
                    savedUser.getFullName());
            log.info("Registration success email sent to: {}", savedUser.getEmail());
        } catch (Exception e) {
            log.warn("Failed to send registration success email to: {}", savedUser.getEmail(), e);
            // Không throw exception vì account đã được tạo
        }

        log.info("Registration process completed successfully for user: {}", savedUser.getUsername());

        return RegisterSuccessResponse.builder()
                .message("Đăng ký tài khoản thành công!")
                .email(savedUser.getEmail())
                .username(savedUser.getUsername())
                .fullName(savedUser.getFullName())
                .status(savedUser.getStatus().name())
                .instruction("Vui lòng quay lại trang đăng nhập để đăng nhập bằng email hoặc username.")
                .build();
    }
}
