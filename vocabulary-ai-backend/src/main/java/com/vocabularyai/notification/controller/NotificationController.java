package com.vocabularyai.notification.controller;

import com.vocabularyai.common.response.ApiResponse;
import com.vocabularyai.notification.service.ReminderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping({"/notifications", "/api/notifications", "/api/v1/notifications"})
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notifications", description = "Email notification preferences and unsubscribe endpoints")
public class NotificationController {

    private final ReminderService reminderService;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * 1-Click unsubscribe from email reminders.
     * Accessible without login. Validates HMAC token and redirects user to Profile page.
     */
    @GetMapping("/unsubscribe")
    @Operation(summary = "1-Click unsubscribe from email reminders via HMAC token")
    public ResponseEntity<Void> unsubscribe(
            @RequestParam("userId") Long userId,
            @RequestParam("token") String token,
            @RequestParam(value = "type", defaultValue = "LEARNING") String type
    ) {
        log.info("Processing unsubscribe request for userId: {}, type: {}", userId, type);
        boolean success = reminderService.unsubscribeUser(userId, type, token);
        
        String redirectUrl = frontendUrl + "/profile?unsubscribed=" + success;
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirectUrl)).build();
    }

    /**
     * API version of unsubscribe for programmatic requests
     */
    @PostMapping("/unsubscribe")
    @Operation(summary = "Unsubscribe API endpoint")
    public ResponseEntity<ApiResponse<Boolean>> unsubscribeApi(
            @RequestParam("userId") Long userId,
            @RequestParam("token") String token,
            @RequestParam(value = "type", defaultValue = "LEARNING") String type
    ) {
        boolean success = reminderService.unsubscribeUser(userId, type, token);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("Đã tắt email nhắc học thành công!", true));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Mã xác thực không hợp lệ hoặc liên kết đã hết hạn.", false));
        }
    }
}
