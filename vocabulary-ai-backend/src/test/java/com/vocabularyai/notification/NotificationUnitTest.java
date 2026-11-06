package com.vocabularyai.notification;

import com.vocabularyai.common.exception.ResourceNotFoundException;
import com.vocabularyai.notification.dto.NotificationPageResponse;
import com.vocabularyai.notification.dto.NotificationResponse;
import com.vocabularyai.notification.entity.Notification;
import com.vocabularyai.notification.entity.NotificationType;
import com.vocabularyai.notification.repository.NotificationRepository;
import com.vocabularyai.notification.service.NotificationServiceImpl;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationUnitTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User student;
    private User instructor;
    private User admin;
    private Notification notification1;

    @BeforeEach
    void setUp() {
        student = User.builder().id(1L).username("student1").role(User.UserRole.STUDENT).build();
        instructor = User.builder().id(2L).username("instructor1").role(User.UserRole.INSTRUCTOR).build();
        admin = User.builder().id(3L).username("admin1").role(User.UserRole.ADMIN).build();

        notification1 = Notification.builder()
                .id(100L)
                .user(student)
                .title("Bài tập đã được phê duyệt!")
                .content("Bài tập của bạn đã được công khai")
                .type(NotificationType.EXERCISE_APPROVED)
                .link("/quiz/5")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("getUserNotifications returns page of notifications and unreadCount")
    void testGetUserNotifications() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> page = new PageImpl<>(List.of(notification1), pageable, 1);
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L, pageable)).thenReturn(page);
        when(notificationRepository.countByUserIdAndIsReadFalse(1L)).thenReturn(1L);

        NotificationPageResponse response = notificationService.getUserNotifications(1L, pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(1L, response.getUnreadCount());
        assertEquals("Bài tập đã được phê duyệt!", response.getContent().get(0).getTitle());
    }

    @Test
    @DisplayName("markAsRead marks unread notification as read")
    void testMarkAsReadSuccess() {
        when(notificationRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(notification1));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        NotificationResponse response = notificationService.markAsRead(100L, 1L);

        assertTrue(response.isRead());
        verify(notificationRepository).save(notification1);
    }

    @Test
    @DisplayName("markAsRead throws ResourceNotFoundException if notification does not exist or wrong user")
    void testMarkAsReadNotFound() {
        when(notificationRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> notificationService.markAsRead(999L, 1L));
    }

    @Test
    @DisplayName("markAllAsRead calls repository markAllAsReadByUserId")
    void testMarkAllAsRead() {
        notificationService.markAllAsRead(1L);
        verify(notificationRepository).markAllAsReadByUserId(1L);
    }

    @Test
    @DisplayName("createNotification creates and saves notification")
    void testCreateNotification() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));

        notificationService.createNotification(1L, "Test", "Content", NotificationType.SYSTEM, "/test");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification saved = captor.getValue();
        assertEquals("Test", saved.getTitle());
        assertEquals("Content", saved.getContent());
        assertEquals(student, saved.getUser());
        assertFalse(saved.isRead());
    }

    @Test
    @DisplayName("notifyInstructorsAndAdmins sends notification to all instructors and admins")
    void testNotifyInstructorsAndAdmins() {
        when(userRepository.findByRoleIn(anyCollection())).thenReturn(List.of(instructor, admin));

        notificationService.notifyInstructorsAndAdmins(
                "Bài tập mới",
                "Chờ duyệt",
                NotificationType.EXERCISE_PENDING_REVIEW,
                "/admin/content-review"
        );

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository).saveAll(captor.capture());

        List<Notification> savedList = captor.getValue();
        assertEquals(2, savedList.size());
        assertEquals(instructor, savedList.get(0).getUser());
        assertEquals(admin, savedList.get(1).getUser());
    }
}

