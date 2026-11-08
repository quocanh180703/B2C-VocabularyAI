package com.vocabularyai.user.service;

import com.vocabularyai.common.exception.BadRequestException;
import com.vocabularyai.user.dto.UpdateUserRequest;
import com.vocabularyai.user.dto.UserResponse;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@DisplayName("UserService Tests")
public class UserServiceTest {
    
    @Autowired
    private UserService userService;
    
    @MockBean
    private UserRepository userRepository;
    
    @MockBean
    private PasswordEncoder passwordEncoder;
    
    private User testUser;
    
    @BeforeEach
    public void setUp() {
        testUser = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .fullName("Test User")
            .password("encodedPassword")
            .status(User.UserStatus.ACTIVE)
            .build();
    }
    
    @Test
    @DisplayName("Should get user by ID successfully")
    public void testGetUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        User result = userService.getUserById(1L);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
    }
    
    @Test
    @DisplayName("Should return empty when user not found by ID")
    public void testGetUserById_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        
        assertThrows(com.vocabularyai.common.exception.ResourceNotFoundException.class, () ->
            userService.getUserById(999L));
    }
    
    @Test
    @DisplayName("Should get user by username successfully")
    public void testGetUserByUsername_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        User result = userService.getUserByUsername("testuser");
        
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }
    
    @Test
    @DisplayName("Should update user profile successfully")
    public void testUpdateProfile_Success() {
        UpdateUserRequest request = UpdateUserRequest.builder()
            .fullName("Updated Name")
            .build();
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenReturn(testUser);
        
        UserResponse result = userService.updateUserProfile(1L, request);
        
        assertNotNull(result);
        verify(userRepository).save(any());
    }
    
    @Test
    @DisplayName("Should change password successfully")
    public void testChangePassword_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldpass", testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("newpass")).thenReturn("encodedNewPassword");
        when(userRepository.save(any())).thenReturn(testUser);
        
        assertDoesNotThrow(() ->
            userService.changePassword(1L, "oldpass", "newpass"));
    }
    
    @Test
    @DisplayName("Should reject password change with wrong old password")
    public void testChangePassword_WrongOldPassword() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpass", testUser.getPassword())).thenReturn(false);
        
        assertThrows(BadRequestException.class, () ->
            userService.changePassword(1L, "wrongpass", "newpass"));
    }
    
    @Test
    @DisplayName("Should update user status")
    public void testUpdateUserStatus_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenReturn(testUser);
        
        assertDoesNotThrow(() ->
            userService.updateUserStatus(1L, User.UserStatus.INACTIVE));
    }
}
