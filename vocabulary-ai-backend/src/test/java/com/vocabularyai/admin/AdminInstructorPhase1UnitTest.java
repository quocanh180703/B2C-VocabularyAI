package com.vocabularyai.admin;

import com.vocabularyai.admin.controller.*;
import com.vocabularyai.admin.dto.request.CreateInstructorRequest;
import com.vocabularyai.admin.dto.response.InstructorResponse;
import com.vocabularyai.admin.repository.AdminStudentRepository;
import com.vocabularyai.admin.service.AdminStudentServiceImpl;
import com.vocabularyai.common.exception.DuplicateResourceException;
import com.vocabularyai.user.entity.AuthProvider;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminInstructorPhase1UnitTest {

    @Mock
    private AdminStudentRepository studentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminStudentServiceImpl studentService;

    private CreateInstructorRequest request;

    @BeforeEach
    void setUp() {
        request = CreateInstructorRequest.builder()
                .username("instructor123")
                .email("instructor@example.com")
                .fullName("Thầy Giáo A")
                .password("Password@123")
                .build();
    }

    @Test
    @DisplayName("UserRole enum should contain INSTRUCTOR")
    void testUserRoleContainsInstructor() {
        assertNotNull(User.UserRole.valueOf("INSTRUCTOR"));
    }

    @Test
    @DisplayName("createInstructor should assign INSTRUCTOR role, ACTIVE status, and LOCAL provider")
    void testCreateInstructorSuccess() {
        when(userRepository.existsByUsername("instructor123")).thenReturn(false);
        when(userRepository.existsByEmail("instructor@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("encodedPassword");

        User savedUser = User.builder()
                .id(99L)
                .username("instructor123")
                .email("instructor@example.com")
                .fullName("Thầy Giáo A")
                .password("encodedPassword")
                .role(User.UserRole.INSTRUCTOR)
                .status(User.UserStatus.ACTIVE)
                .provider(AuthProvider.LOCAL)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        InstructorResponse response = studentService.createInstructor(request);

        assertNotNull(response);
        assertEquals(99L, response.getId());
        assertEquals("instructor123", response.getUsername());
        assertEquals("instructor@example.com", response.getEmail());
        assertEquals("Thầy Giáo A", response.getFullName());
        assertEquals("INSTRUCTOR", response.getRole());
        assertEquals("ACTIVE", response.getStatus());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User captured = captor.getValue();
        assertEquals(User.UserRole.INSTRUCTOR, captured.getRole());
        assertEquals(User.UserStatus.ACTIVE, captured.getStatus());
        assertEquals(AuthProvider.LOCAL, captured.getProvider());
        assertEquals("encodedPassword", captured.getPassword());
    }

    @Test
    @DisplayName("createInstructor should throw DuplicateResourceException on existing username")
    void testCreateInstructorDuplicateUsername() {
        when(userRepository.existsByUsername("instructor123")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> studentService.createInstructor(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("createInstructor should throw DuplicateResourceException on existing email")
    void testCreateInstructorDuplicateEmail() {
        when(userRepository.existsByUsername("instructor123")).thenReturn(false);
        when(userRepository.existsByEmail("instructor@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> studentService.createInstructor(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("All 8 previously unauthenticated admin controllers must have @PreAuthorize with hasRole('ADMIN')")
    void testAdminControllersHavePreAuthorize() {
        Class<?>[] adminControllers = new Class<?>[] {
            AdminAiController.class,
            AdminLearningProgressController.class,
            AdminPackagesController.class,
            AdminQuestionController.class,
            AdminQuizAttemptController.class,
            AdminReportsController.class,
            AdminSpeakingFeedbackController.class,
            AdminSystemController.class
        };

        for (Class<?> clazz : adminControllers) {
            PreAuthorize annotation = clazz.getAnnotation(PreAuthorize.class);
            assertNotNull(annotation, clazz.getSimpleName() + " must have @PreAuthorize annotation");
            assertTrue(annotation.value().contains("ADMIN"),
                    clazz.getSimpleName() + " must enforce ADMIN access");
        }
    }
}

