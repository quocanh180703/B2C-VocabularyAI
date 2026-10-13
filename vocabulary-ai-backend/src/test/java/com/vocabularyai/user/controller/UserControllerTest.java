package com.vocabularyai.user.controller;

import com.vocabularyai.common.response.ApiResponse;
import com.vocabularyai.user.dto.ChangePasswordRequest;
import com.vocabularyai.user.dto.UpdateUserRequest;
import com.vocabularyai.user.dto.UserResponse;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("UserController Tests")
public class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private UserService userService;
    
    private User testUser;
    private UserResponse userResponse;
    
    @BeforeEach
    public void setUp() {
        testUser = User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .fullName("Test User")
            .build();
        
        userResponse = UserResponse.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .fullName("Test User")
            .build();
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should get current user profile")
    public void testGetCurrentUser_Success() throws Exception {
        when(userService.getCurrentUserResponse()).thenReturn(userResponse);
        
        mockMvc.perform(get("/users/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.username").value("testuser"));
    }
    
    @Test
    @DisplayName("Should require authentication to get profile")
    public void testGetCurrentUser_Unauthenticated() throws Exception {
        mockMvc.perform(get("/users/me"))
            .andExpect(status().isUnauthorized());
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should update user profile")
    public void testUpdateProfile_Success() throws Exception {
        UpdateUserRequest request = UpdateUserRequest.builder()
            .fullName("Updated Name")
            .build();
        
        UserResponse updatedResponse = UserResponse.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .fullName("Updated Name")
            .build();
        
        when(userService.getCurrentUserId()).thenReturn(1L);
        when(userService.updateUserProfile(1L, request)).thenReturn(updatedResponse);
        
        mockMvc.perform(put("/users/me")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.fullName").value("Updated Name"));
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should change password successfully")
    public void testChangePassword_Success() throws Exception {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
            .oldPassword("oldpass123")
            .newPassword("newpass123")
            .build();
        
        when(userService.getCurrentUserId()).thenReturn(1L);
        doNothing().when(userService).changePassword(1L, "oldpass123", "newpass123");
        
        mockMvc.perform(put("/users/me/password")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should reject password change with wrong old password")
    public void testChangePassword_WrongOldPassword() throws Exception {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
            .oldPassword("wrongpass")
            .newPassword("newpass123")
            .build();
        
        when(userService.getCurrentUserId()).thenReturn(1L);
        doThrow(new IllegalArgumentException("Old password is incorrect"))
            .when(userService).changePassword(1L, "wrongpass", "newpass123");
        
        mockMvc.perform(put("/users/me/password")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Should update user status as admin")
    public void testUpdateUserStatus_Success() throws Exception {
        UserResponse updatedResponse = UserResponse.builder()
            .id(2L)
            .username("otheruser")
            .build();
        
        doNothing().when(userService).updateUserStatus(2L, User.UserStatus.INACTIVE);
        when(userService.getUserById(2L)).thenReturn(testUser);
        
        mockMvc.perform(put("/users/2/status")
            .param("status", "INACTIVE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should deny status update for non-admin")
    public void testUpdateUserStatus_Forbidden() throws Exception {
        mockMvc.perform(put("/users/2/status")
            .param("status", "INACTIVE"))
            .andExpect(status().isForbidden());
    }
}
