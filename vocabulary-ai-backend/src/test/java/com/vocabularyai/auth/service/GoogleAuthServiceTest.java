package com.vocabularyai.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.vocabularyai.auth.dto.response.AuthResponse;
import com.vocabularyai.common.exception.BadRequestException;
import com.vocabularyai.common.exception.UnauthorizedException;
import com.vocabularyai.user.entity.AuthProvider;
import com.vocabularyai.user.entity.User;
import com.vocabularyai.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GoogleAuthService Tests")
class GoogleAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthService authService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private GoogleIdTokenVerifier googleIdTokenVerifier;

    @InjectMocks
    private GoogleAuthService googleAuthService;

    private final String clientId = "test-client-id.apps.googleusercontent.com";
    private final String validToken = "valid-google-id-token";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(googleAuthService, "googleClientId", clientId);
    }

    private GoogleIdToken createMockGoogleIdToken(String sub, String email, Boolean emailVerified, String name, String picture) {
        GoogleIdToken idToken = mock(GoogleIdToken.class);
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setSubject(sub);
        payload.setEmail(email);
        payload.setEmailVerified(emailVerified);
        payload.set("name", name);
        payload.set("picture", picture);
        when(idToken.getPayload()).thenReturn(payload);
        return idToken;
    }

    @Test
    @DisplayName("Ném BadRequestException khi client-id chưa được cấu hình")
    void loginWithGoogle_whenClientIdNotConfigured_throwsBadRequest() {
        ReflectionTestUtils.setField(googleAuthService, "googleClientId", "");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> googleAuthService.loginWithGoogle(validToken));
        assertTrue(ex.getMessage().contains("chưa được cấu hình"));
    }

    @Test
    @DisplayName("Ném UnauthorizedException khi ID token không hợp lệ (verifier trả về null)")
    void loginWithGoogle_whenTokenVerifierReturnsNull_throwsUnauthorized() throws Exception {
        when(googleIdTokenVerifier.verify(validToken)).thenReturn(null);

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> googleAuthService.loginWithGoogle(validToken));
        assertTrue(ex.getMessage().contains("không hợp lệ hoặc đã hết hạn"));
    }

    @Test
    @DisplayName("Ném UnauthorizedException khi verifier ném Exception (sai chữ ký, hết hạn)")
    void loginWithGoogle_whenVerifierThrows_throwsUnauthorized() throws Exception {
        when(googleIdTokenVerifier.verify(validToken)).thenThrow(new GeneralSecurityException("Invalid signature"));

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> googleAuthService.loginWithGoogle(validToken));
        assertTrue(ex.getMessage().contains("không hợp lệ hoặc đã hết hạn"));
    }

    @Test
    @DisplayName("Ném UnauthorizedException khi email Google chưa được verified")
    void loginWithGoogle_whenEmailNotVerified_throwsUnauthorized() throws Exception {
        GoogleIdToken mockToken = createMockGoogleIdToken("sub-123", "test@gmail.com", false, "Test", null);
        when(googleIdTokenVerifier.verify(validToken)).thenReturn(mockToken);

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> googleAuthService.loginWithGoogle(validToken));
        assertTrue(ex.getMessage().contains("chưa được xác minh"));
    }

    @Test
    @DisplayName("User mới: Tạo tài khoản STUDENT, ACTIVE, provider GOOGLE và trả về AuthResponse")
    void loginWithGoogle_whenUserNotExists_createsUserAndReturnsTokens() throws Exception {
        GoogleIdToken mockToken = createMockGoogleIdToken("sub-new-123", "newuser@gmail.com", true, "New User", "https://photo.jpg");
        when(googleIdTokenVerifier.verify(validToken)).thenReturn(mockToken);
        when(userRepository.findByProviderId("sub-new-123")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("newuser@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedRandomPassword");

        User savedUser = User.builder()
                .id(10L)
                .username("newuser")
                .email("newuser@gmail.com")
                .fullName("New User")
                .avatarUrl("https://photo.jpg")
                .role(User.UserRole.STUDENT)
                .status(User.UserStatus.ACTIVE)
                .provider(AuthProvider.GOOGLE)
                .providerId("sub-new-123")
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        AuthResponse expectedAuthResponse = AuthResponse.builder()
                .accessToken("access-token-xyz")
                .refreshToken("refresh-token-xyz")
                .build();
        when(authService.generateAuthResponse(savedUser)).thenReturn(expectedAuthResponse);

        AuthResponse result = googleAuthService.loginWithGoogle(validToken);

        assertNotNull(result);
        assertEquals("access-token-xyz", result.getAccessToken());
        verify(userRepository).save(argThat(u ->
                u.getProvider() == AuthProvider.GOOGLE
                && "sub-new-123".equals(u.getProviderId())
                && u.getRole() == User.UserRole.STUDENT
                && u.getStatus() == User.UserStatus.ACTIVE
        ));
        verify(authService).validateUserStatus(savedUser);
    }

    @Test
    @DisplayName("User đã có provider_id (sub): Đăng nhập trực tiếp, không tạo user mới")
    void loginWithGoogle_whenSubExists_logsInExistingUser() throws Exception {
        GoogleIdToken mockToken = createMockGoogleIdToken("sub-existing-456", "existing@gmail.com", true, "Existing User", null);
        when(googleIdTokenVerifier.verify(validToken)).thenReturn(mockToken);

        User existingUser = User.builder()
                .id(20L)
                .username("existing")
                .email("existing@gmail.com")
                .provider(AuthProvider.GOOGLE)
                .providerId("sub-existing-456")
                .status(User.UserStatus.ACTIVE)
                .build();
        when(userRepository.findByProviderId("sub-existing-456")).thenReturn(Optional.of(existingUser));

        AuthResponse expectedResponse = AuthResponse.builder().accessToken("token-456").build();
        when(authService.generateAuthResponse(existingUser)).thenReturn(expectedResponse);

        AuthResponse result = googleAuthService.loginWithGoogle(validToken);

        assertNotNull(result);
        assertEquals("token-456", result.getAccessToken());
        verify(userRepository, never()).save(any());
        verify(authService).validateUserStatus(existingUser);
    }

    @Test
    @DisplayName("User đã có email nhưng chưa có sub (tài khoản LOCAL): Tự động liên kết sub")
    void loginWithGoogle_whenEmailExistsLocal_linksSub() throws Exception {
        GoogleIdToken mockToken = createMockGoogleIdToken("sub-link-789", "localuser@gmail.com", true, "Local User", null);
        when(googleIdTokenVerifier.verify(validToken)).thenReturn(mockToken);
        when(userRepository.findByProviderId("sub-link-789")).thenReturn(Optional.empty());

        User localUser = User.builder()
                .id(30L)
                .username("localuser")
                .email("localuser@gmail.com")
                .password("bcrypt-old-password")
                .provider(AuthProvider.LOCAL)
                .providerId(null)
                .status(User.UserStatus.ACTIVE)
                .build();
        when(userRepository.findByEmail("localuser@gmail.com")).thenReturn(Optional.of(localUser));
        when(userRepository.save(localUser)).thenReturn(localUser);

        AuthResponse expectedResponse = AuthResponse.builder().accessToken("token-789").build();
        when(authService.generateAuthResponse(localUser)).thenReturn(expectedResponse);

        AuthResponse result = googleAuthService.loginWithGoogle(validToken);

        assertNotNull(result);
        assertEquals("sub-link-789", localUser.getProviderId());
        assertEquals(AuthProvider.LOCAL, localUser.getProvider()); // Giữ nguyên LOCAL provider và mật khẩu cũ
        verify(userRepository).save(localUser);
    }

    @Test
    @DisplayName("Trùng username: Tự động thêm hậu tố số tăng dần")
    void loginWithGoogle_whenUsernameExists_incrementsSuffix() {
        when(userRepository.existsByUsername("hoang")).thenReturn(true);
        when(userRepository.existsByUsername("hoang1")).thenReturn(true);
        when(userRepository.existsByUsername("hoang2")).thenReturn(false);

        String username = googleAuthService.generateUniqueUsername("hoang@gmail.com");
        assertEquals("hoang2", username);
    }

    @Test
    @DisplayName("Tài khoản bị BANNED/INACTIVE: Ném UnauthorizedException tương tự login thường")
    void loginWithGoogle_whenUserBanned_throwsUnauthorized() throws Exception {
        GoogleIdToken mockToken = createMockGoogleIdToken("sub-banned", "banned@gmail.com", true, "Banned", null);
        when(googleIdTokenVerifier.verify(validToken)).thenReturn(mockToken);

        User bannedUser = User.builder()
                .id(40L)
                .username("banned")
                .email("banned@gmail.com")
                .provider(AuthProvider.GOOGLE)
                .providerId("sub-banned")
                .status(User.UserStatus.BANNED)
                .build();
        when(userRepository.findByProviderId("sub-banned")).thenReturn(Optional.of(bannedUser));
        doThrow(new UnauthorizedException("Your account is banned. You cannot log in."))
                .when(authService).validateUserStatus(bannedUser);

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> googleAuthService.loginWithGoogle(validToken));
        assertTrue(ex.getMessage().contains("banned"));
    }
}

