package com.vocabularyai.notification.controller;

import com.vocabularyai.common.response.ApiResponse;
import com.vocabularyai.common.util.PaginationUtils;
import com.vocabularyai.notification.dto.NotificationPageResponse;
import com.vocabularyai.notification.dto.NotificationResponse;
import com.vocabularyai.notification.service.NotificationService;
import com.vocabularyai.notification.service.ReminderService;
import com.vocabularyai.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping({"/notifications", "/api/notifications", "/api/v1/notifications"})
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notifications", description = "In-app notifications and email reminder preferences")
public class NotificationController {

    private final ReminderService reminderService;
    private final NotificationService notificationService;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lấy danh sách thông báo trong app của người dùng hiện tại")
    public ResponseEntity<ApiResponse<NotificationPageResponse>> getMyNotifications(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PaginationUtils.createPageableDesc(page, size, "createdAt");
        NotificationPageResponse response = notificationService.getUserNotifications(user.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thông báo thành công", response));
    }

    @PostMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Đánh dấu một thông báo là đã đọc")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        NotificationResponse response = notificationService.markAsRead(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu thông báo là đã đọc", response));
    }

    @PostMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Đánh dấu tất cả thông báo là đã đọc")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal User user) {
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu tất cả thông báo là đã đọc", null));
    }

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
