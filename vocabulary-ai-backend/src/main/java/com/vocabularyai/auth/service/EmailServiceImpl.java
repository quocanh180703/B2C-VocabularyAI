package com.vocabularyai.auth.service;

import com.vocabularyai.payment.dto.payos.PaymentEmailData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;

/**
 * Implementation of EmailService
 * 
 * Uses Spring Mail to send emails via configured SMTP
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
    
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    
    @Value("${app.email.from:noreply@vocabularyai.com}")
    private String fromEmail;
    
    @Value("${app.name:VocabularyAI}")
    private String appName;
    
    @Override
    public void sendOtpEmail(String email, String otpCode, String otpType) {
        log.info("Sending OTP email to: {}", email);
        
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(email);
            
            String subject = otpType.equalsIgnoreCase("REGISTER") 
                ? "Mã xác thực đăng ký tài khoản" 
                : "Mã xác thực đặt lại mật khẩu";
            
            String body = otpType.equalsIgnoreCase("REGISTER") 
                ? buildRegisterOtpEmailBody(otpCode) 
                : buildForgotPasswordOtpEmailBody(otpCode);
            
            helper.setSubject(subject);
            helper.setText(body, true);
            
            mailSender.send(mimeMessage);
            log.info("OTP email sent successfully to: {}", email);
        } catch (MessagingException e) {
            log.error("Failed to send OTP email to: {}", email, e);
            throw new RuntimeException("Failed to send OTP email: " + e.getMessage());
        }
    }
    
    @Override
    public void sendPasswordResetConfirmationEmail(String email) {
        log.info("Sending password reset confirmation email to: {}", email);
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Mật khẩu của bạn đã được đặt lại thành công");
            message.setText(buildPasswordResetConfirmationEmailBody());
            
            mailSender.send(message);
            log.info("Password reset confirmation email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send password reset confirmation email to: {}", email, e);
            throw new RuntimeException("Failed to send password reset confirmation email: " + e.getMessage());
        }
    }
    
    @Override
    public void sendRegistrationSuccessEmail(String email, String username, String fullName) {
        log.info("Sending registration success email to: {}", email);
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Chào mừng bạn đến với " + appName);
            message.setText(buildRegistrationSuccessEmailBody(username, fullName));
            
            mailSender.send(message);
            log.info("Registration success email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send registration success email to: {}", email, e);
            throw new RuntimeException("Failed to send registration success email: " + e.getMessage());
        }
    }
    
    /**
     * Build HTML email body for registration OTP
     */
    private String buildRegisterOtpEmailBody(String otpCode) {
        return "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "    <meta charset='UTF-8'>" +
            "    <style>" +
            "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
            "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
            "        .header { background-color: #007bff; color: white; padding: 20px; border-radius: 5px 5px 0 0; text-align: center; }" +
            "        .body { background-color: #f9f9f9; padding: 20px; border-radius: 0 0 5px 5px; }" +
            "        .otp-code { font-size: 32px; font-weight: bold; letter-spacing: 5px; text-align: center; background-color: #e9ecef; padding: 15px; border-radius: 5px; margin: 20px 0; }" +
            "        .footer { text-align: center; font-size: 12px; color: #999; margin-top: 20px; }" +
            "    </style>" +
            "</head>" +
            "<body>" +
            "    <div class='container'>" +
            "        <div class='header'>" +
            "            <h1>Xác thực đăng ký tài khoản</h1>" +
            "        </div>" +
            "        <div class='body'>" +
            "            <p>Xin chào,</p>" +
            "            <p>Cảm ơn bạn đã đăng ký tài khoản " + appName + ". Để hoàn tất đăng ký, vui lòng nhập mã xác thực dưới đây:</p>" +
            "            <div class='otp-code'>" + otpCode + "</div>" +
            "            <p><strong>Mã xác thực này sẽ hết hạn trong 5 phút.</strong></p>" +
            "            <p>Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email này.</p>" +
            "            <p>Trân trọng,<br>Đội ngũ " + appName + "</p>" +
            "        </div>" +
            "        <div class='footer'>" +
            "            <p>&copy; 2024 " + appName + ". Tất cả các quyền được bảo lưu.</p>" +
            "        </div>" +
            "    </div>" +
            "</body>" +
            "</html>";
    }
    
    /**
     * Build HTML email body for forgot password OTP
     */
    private String buildForgotPasswordOtpEmailBody(String otpCode) {
        return "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "    <meta charset='UTF-8'>" +
            "    <style>" +
            "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
            "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
            "        .header { background-color: #dc3545; color: white; padding: 20px; border-radius: 5px 5px 0 0; text-align: center; }" +
            "        .body { background-color: #f9f9f9; padding: 20px; border-radius: 0 0 5px 5px; }" +
            "        .otp-code { font-size: 32px; font-weight: bold; letter-spacing: 5px; text-align: center; background-color: #e9ecef; padding: 15px; border-radius: 5px; margin: 20px 0; }" +
            "        .footer { text-align: center; font-size: 12px; color: #999; margin-top: 20px; }" +
            "    </style>" +
            "</head>" +
            "<body>" +
            "    <div class='container'>" +
            "        <div class='header'>" +
            "            <h1>Đặt lại mật khẩu</h1>" +
            "        </div>" +
            "        <div class='body'>" +
            "            <p>Xin chào,</p>" +
            "            <p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản " + appName + " của bạn. Vui lòng nhập mã xác thực dưới đây:</p>" +
            "            <div class='otp-code'>" + otpCode + "</div>" +
            "            <p><strong>Mã xác thực này sẽ hết hạn trong 5 phút.</strong></p>" +
            "            <p>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này và mật khẩu của bạn sẽ vẫn được bảo vệ.</p>" +
            "            <p>Trân trọng,<br>Đội ngũ " + appName + "</p>" +
            "        </div>" +
            "        <div class='footer'>" +
            "            <p>&copy; 2024 " + appName + ". Tất cả các quyền được bảo lưu.</p>" +
            "        </div>" +
            "    </div>" +
            "</body>" +
            "</html>";
    }
    
    /**
     * Build email body for password reset confirmation
     */
    private String buildPasswordResetConfirmationEmailBody() {
        return "Xin chào,\n\n" +
            "Mật khẩu của bạn đã được đặt lại thành công. Bạn hiện có thể đăng nhập bằng mật khẩu mới của bạn.\n\n" +
            "Nếu bạn không thực hiện thay đổi này, vui lòng liên hệ với chúng tôi ngay lập tức.\n\n" +
            "Trân trọng,\n" +
            "Đội ngũ " + appName;
    }
    
    /**
     * Build email body for registration success
     */
    private String buildRegistrationSuccessEmailBody(String username, String fullName) {
        return "Xin chào " + (fullName != null ? fullName : username) + ",\n\n" +
            "Chào mừng bạn đến với " + appName + "!\n\n" +
            "Tài khoản của bạn đã được tạo thành công.\n\n" +
            "Tên đăng nhập: " + username + "\n\n" +
            "Bạn bây giờ có thể đăng nhập vào ứng dụng bằng tên đăng nhập hoặc email của bạn.\n\n" +
            "Chúc bạn có trải nghiệm tuyệt vời!\n\n" +
            "Trân trọng,\n" +
            "Đội ngũ " + appName;
    }
    
    @Override
    public void sendPaymentSuccessEmail(PaymentEmailData paymentData) {
        log.info("Sending payment success email to: {}", paymentData.getEmail());
        
        try {
            // Create Context with payment data for Thymeleaf template rendering
            Context context = new Context();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");
            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String paidDateStr = paymentData.getPaidDate() != null ? paymentData.getPaidDate().format(dtf) : "";
            String startDateStr = paymentData.getSubscriptionStartDate() != null ? paymentData.getSubscriptionStartDate().format(df) : "";
            String endDateStr = paymentData.getSubscriptionEndDate() != null ? paymentData.getSubscriptionEndDate().format(df) : "";
            String amountStr = paymentData.getAmount() != null ? String.format("%,d VNĐ", paymentData.getAmount().longValue()) : "0 VNĐ";
            String durationStr = paymentData.getDuration() != null ? (paymentData.getDuration() + " ngày") : "";

            context.setVariable("customerName", paymentData.getCustomerName());
            context.setVariable("orderCode", paymentData.getOrderCode());
            context.setVariable("paidDate", paidDateStr);
            context.setVariable("packageName", paymentData.getPackageName());
            context.setVariable("duration", durationStr);
            context.setVariable("subscriptionStartDate", startDateStr);
            context.setVariable("subscriptionEndDate", endDateStr);
            context.setVariable("amount", amountStr);
            context.setVariable("transactionCode", paymentData.getTransactionCode());
            context.setVariable("frontendUrl", paymentData.getFrontendUrl());
            
            // Render the Thymeleaf template
            String htmlBody = templateEngine.process("email/payment-success", context);
            
            // Send email with rendered HTML
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(paymentData.getEmail());
            helper.setSubject("Xác nhận thanh toán - Đơn hàng " + paymentData.getOrderCode());
            helper.setText(htmlBody, true);
            
            mailSender.send(mimeMessage);
            log.info("Payment success email sent successfully to: {}", paymentData.getEmail());
            
        } catch (MessagingException e) {
            log.error("Failed to send payment success email to: {}", paymentData.getEmail(), e);
            throw new RuntimeException("Failed to send payment success email: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while sending payment success email to: {}", paymentData.getEmail(), e);
            throw new RuntimeException("Unexpected error while sending payment success email: " + e.getMessage());
        }
    }

    private void sendHtmlEmail(String toEmail, String subject, String templateName, Context context) {
        log.info("Sending {} email to: {}", templateName, toEmail);
        try {
            context.setVariable("appName", appName);
            String htmlBody = templateEngine.process(templateName, context);
            
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            
            mailSender.send(mimeMessage);
            log.info("{} email sent successfully to: {}", templateName, toEmail);
        } catch (Exception e) {
            log.error("Failed to send {} email to: {}", templateName, toEmail, e);
            throw new RuntimeException("Failed to send " + templateName + " email: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendReviewDueEmail(
            String toEmail,
            String recipientName,
            int wordsDueCount,
            java.util.List<com.vocabularyai.notification.dto.OverdueWordDto> overdueWords,
            int currentStreak,
            String actionUrl,
            String unsubscribeUrl
    ) {
        Context context = new Context();
        context.setVariable("recipientName", recipientName != null ? recipientName : "Học viên");
        context.setVariable("wordsDueCount", wordsDueCount);
        context.setVariable("overdueWords", overdueWords);
        context.setVariable("currentStreak", currentStreak);
        context.setVariable("actionUrl", actionUrl);
        context.setVariable("unsubscribeUrl", unsubscribeUrl);
        
        String subject = "Bạn có " + wordsDueCount + " từ đến hạn ôn hôm nay — Vi chờ bạn";
        sendHtmlEmail(toEmail, subject, "email/review-due", context);
    }

    @Override
    public void sendStreakSaveEmail(
            String toEmail,
            String recipientName,
            int streakDays,
            com.vocabularyai.notification.dto.OverdueWordDto suggestedWord,
            String actionUrl,
            String unsubscribeUrl
    ) {
        Context context = new Context();
        context.setVariable("recipientName", recipientName != null ? recipientName : "Học viên");
        context.setVariable("streakDays", streakDays);
        context.setVariable("suggestedWord", suggestedWord);
        context.setVariable("actionUrl", actionUrl);
        context.setVariable("unsubscribeUrl", unsubscribeUrl);
        
        String subject = "Đừng để đứt chuỗi " + streakDays + " ngày học của bạn!";
        sendHtmlEmail(toEmail, subject, "email/streak-save", context);
    }

    @Override
    public void sendRenewal3DaysEmail(
            String toEmail,
            String recipientName,
            String packageName,
            String expireDateStr,
            String renewPriceStr,
            String actionUrl
    ) {
        Context context = new Context();
        context.setVariable("recipientName", recipientName != null ? recipientName : "Học viên");
        context.setVariable("packageName", packageName);
        context.setVariable("expireDateStr", expireDateStr);
        context.setVariable("renewPriceStr", renewPriceStr);
        context.setVariable("actionUrl", actionUrl);
        
        String subject = "Gói Premium của bạn hết hạn sau 3 ngày";
        sendHtmlEmail(toEmail, subject, "email/renewal-3d", context);
    }

    @Override
    public void sendRenewalDueEmail(
            String toEmail,
            String recipientName,
            String packageName,
            String expireDateStr,
            String actionUrl
    ) {
        Context context = new Context();
        context.setVariable("recipientName", recipientName != null ? recipientName : "Học viên");
        context.setVariable("packageName", packageName);
        context.setVariable("expireDateStr", expireDateStr);
        context.setVariable("actionUrl", actionUrl);
        
        String subject = "Hôm nay gói Premium của bạn hết hạn";
        sendHtmlEmail(toEmail, subject, "email/renewal-due", context);
    }

    @Override
    public void sendRenewalExpiredEmail(
            String toEmail,
            String recipientName,
            String packageName,
            String expireDateStr,
            String actionUrl
    ) {
        Context context = new Context();
        context.setVariable("recipientName", recipientName != null ? recipientName : "Học viên");
        context.setVariable("packageName", packageName);
        context.setVariable("expireDateStr", expireDateStr);
        context.setVariable("actionUrl", actionUrl);
        
        String subject = "Dữ liệu học của bạn vẫn còn nguyên — quay lại khi bạn sẵn sàng";
        sendHtmlEmail(toEmail, subject, "email/renewal-expired", context);
    }
}
