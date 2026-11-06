package com.vocabularyai.notification.service;

import com.vocabularyai.common.exception.ResourceNotFoundException;
import com.vocabularyai.notification.dto.NotificationPageResponse;
import com.vocabularyai.notification.dto.NotificationResponse;
import com.vocabularyai.notification.entity.Notification;
import com.vocabularyai.notification.entity.NotificationType;
import com.vocabularyai.notification.repository.NotificationRepository;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public NotificationPageResponse getUserNotifications(Long userId, Pageable pageable) {
        Page<Notification> page = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(userId);

        List<NotificationResponse> content = page.getContent().stream()
                .map(NotificationResponse::fromEntity)
                .toList();

        return NotificationPageResponse.builder()
                .content(content)
                .unreadCount(unreadCount)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .build();
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Thông báo không tồn tại hoặc không thuộc quyền sở hữu"));

        if (!notification.isRead()) {
            notification.setRead(true);
            notification = notificationRepository.save(notification);
        }
        return NotificationResponse.fromEntity(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Override
    @Transactional
    public void createNotification(Long userId, String title, String content, NotificationType type, String link) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .content(content)
                .type(type)
                .link(link)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void notifyInstructorsAndAdmins(String title, String content, NotificationType type, String link) {
        List<User> recipients = userRepository.findByRoleIn(List.of(User.UserRole.INSTRUCTOR, User.UserRole.ADMIN));
        if (recipients.isEmpty()) {
            log.warn("Không tìm thấy instructor hoặc admin nào để gửi thông báo");
            return;
        }

        List<Notification> notifications = new ArrayList<>();
        for (User recipient : recipients) {
            notifications.add(Notification.builder()
                    .user(recipient)
                    .title(title)
                    .content(content)
                    .type(type)
                    .link(link)
                    .isRead(false)
                    .build());
        }
        notificationRepository.saveAll(notifications);
    }
}

