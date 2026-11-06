package com.vocabularyai.notification.service;

import com.vocabularyai.notification.dto.NotificationPageResponse;
import com.vocabularyai.notification.dto.NotificationResponse;
import com.vocabularyai.notification.entity.NotificationType;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    NotificationPageResponse getUserNotifications(Long userId, Pageable pageable);

    NotificationResponse markAsRead(Long notificationId, Long userId);

    void markAllAsRead(Long userId);

    void createNotification(Long userId, String title, String content, NotificationType type, String link);

    void notifyInstructorsAndAdmins(String title, String content, NotificationType type, String link);
}

