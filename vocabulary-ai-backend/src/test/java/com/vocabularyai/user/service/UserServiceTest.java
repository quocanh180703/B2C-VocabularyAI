package com.vocabularyai.user.service;

import com.vocabularyai.user.dto.UpdateUserRequest;
import com.vocabularyai.user.dto.UserResponse;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("Should get user by ID")
    public void testGetUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        User user = userService.getUserById(1L);
        
        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
    }
    
    @Test
    @DisplayName("Should throw exception for non-existent user")
    public void testGetUserById_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        
        assertThrows(Exception.class, () -> userService.getUserById(999L));
    }
    
    @Test
    @DisplayName("Should get user by username")
    public void testGetUserByUsername_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        User user = userService.getUserByUsername("testuser");
        
        assertNotNull(user);
        assertEquals("test@example.com", user.getEmail());
    }
    
    @Test
    @DisplayName("Should update user profile")
    public void testUpdateUserProfile_Success() {
        UpdateUserRequest request = UpdateUserRequest.builder()
            .fullName("Updated Name")
            .build();
        
        User updatedUser = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .fullName("Updated Name")
            .build();
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenReturn(updatedUser);
        
        UserResponse response = userService.updateUserProfile(1L, request);
        
        assertNotNull(response);
        assertEquals("Updated Name", response.getFullName());
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
        
        assertThrows(IllegalArgumentException.class, () ->
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
