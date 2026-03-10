// src/test/java/com/amalitech/communityboard/controller/UserControllerTest.java
package com.amalitech.communityboard.controller;

import com.amalitech.communityboard.dto.ResponseDto;
import com.amalitech.communityboard.dto.enums.UserRole;
import com.amalitech.communityboard.dto.request.AuthRequest;
import com.amalitech.communityboard.dto.request.UserRequest;
import com.amalitech.communityboard.dto.request.UserUpdateRequest;
import com.amalitech.communityboard.dto.response.AuthResponse;
import com.amalitech.communityboard.dto.response.UserResponse;
import com.amalitech.communityboard.security.JwtService;
import com.amalitech.communityboard.security.JwtUtil;
import com.amalitech.communityboard.security.TokenBlacklistService;
import com.amalitech.communityboard.service.interfaces.UserInterface;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserInterface userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper;
    private UserResponse userResponse;
    private UserRequest userRequest;
    private UserUpdateRequest userUpdateRequest;
    private AuthRequest authRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        userResponse = UserResponse.builder()
                .id(1L)
                .email("test@example.com")
                .username("testuser")
                .role(UserRole.MEMBER)
                .build();

        userRequest = new UserRequest(
                "testuser",
        "test@example.com",
        "password123",
        UserRole.MEMBER);

        userUpdateRequest = new UserUpdateRequest();
        userUpdateRequest.setUsername("updateduser");

        authRequest = new AuthRequest(
        "test@example.com",
        "password123");

        authResponse = new AuthResponse(
                "access-token",
                "Bearer",
                null);
    }

    @Nested
    @DisplayName("POST /api/v1/users - Create User")
    class CreateUserTests {

        @Test
        @DisplayName("Should create user successfully")
        void createUser_Success() {
            // Arrange
            when(userService.createUser(any(UserRequest.class))).thenReturn(userResponse);

            // Act
            ResponseDto<UserResponse> result = userController.createUser(userRequest);

            // Assert
            assertThat(result.status()).isEqualTo(HttpStatus.CREATED);
            assertThat(result.message()).isEqualTo("user created");
            assertThat(result.data()).isEqualTo(userResponse);
            verify(userService).createUser(userRequest);
        }

        @Test
        @DisplayName("Should handle validation errors")
        void createUser_ValidationError() {
            // Arrange
            when(userService.createUser(any(UserRequest.class)))
                    .thenThrow(new IllegalArgumentException("Validation failed"));

            // Act & Assert
            org.junit.jupiter.api.Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> userController.createUser(userRequest)
            );
        }
    }

    @Nested
    @DisplayName("GET /api/v1/users - Get All Users")
    class GetAllUsersTests {

        @Test
        @DisplayName("Should return paginated users")
        void getAllUsers_Success() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<UserResponse> userPage = new PageImpl<>(List.of(userResponse));
            when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);

            // Act
            ResponseDto<Page<UserResponse>> result = userController.getAllUsers(pageable);

            // Assert
            assertThat(result.status()).isEqualTo(HttpStatus.OK);
            assertThat(result.message()).isEqualTo("users retrieved");
            assertThat(result.data()).isEqualTo(userPage);
            assertThat(result.data().getContent()).hasSize(1);
            verify(userService).getAllUsers(pageable);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/users/{id} - Get User By ID")
    class GetUserByIdTests {

        @Test
        @DisplayName("Should return user when exists")
        void getUserById_Success() {
            // Arrange
            Long userId = 1L;
            when(userService.getUserById(userId)).thenReturn(userResponse);

            // Act
            ResponseDto<UserResponse> result = userController.getUserById(userId);

            // Assert
            assertThat(result.status()).isEqualTo(HttpStatus.OK);
            assertThat(result.message()).isEqualTo("user retrieved");
            assertThat(result.data()).isEqualTo(userResponse);
            verify(userService).getUserById(userId);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void getUserById_NotFound() {
            // Arrange
            Long userId = 999L;
            when(userService.getUserById(userId))
                    .thenThrow(new RuntimeException("User not found"));

            // Act & Assert
            org.junit.jupiter.api.Assertions.assertThrows(
                    RuntimeException.class,
                    () -> userController.getUserById(userId)
            );
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/users/{id} - Update User")
    class UpdateUserTests {

        @Test
        @DisplayName("Should update user successfully")
        void updateUser_Success() {
            // Arrange
            Long userId = 1L;
            when(userService.updateUser(eq(userId), any(UserUpdateRequest.class)))
                    .thenReturn(userResponse);

            // Act
            ResponseDto<UserResponse> result = userController.updateUser(userId, userUpdateRequest);

            // Assert
            assertThat(result.status()).isEqualTo(HttpStatus.OK);
            assertThat(result.message()).isEqualTo("user updated");
            assertThat(result.data()).isEqualTo(userResponse);
            verify(userService).updateUser(userId, userUpdateRequest);
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/users/{id} - Delete User")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete user successfully")
        void deleteUser_Success() {
            // Arrange
            Long userId = 1L;
            doNothing().when(userService).deleteUser(userId);

            // Act
            ResponseEntity<Void> result = userController.deleteUser(userId);

            // Assert
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(userService).deleteUser(userId);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/users/login - Login")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully")
        void login_Success() {
            // Arrange
            when(userService.loginUser(any(AuthRequest.class), any(HttpServletResponse.class)))
                    .thenReturn(authResponse);

            // Act
            ResponseDto<AuthResponse> result = userController.login(authRequest, response);

            // Assert
            assertThat(result.status()).isEqualTo(HttpStatus.OK);
            assertThat(result.message()).isEqualTo("User logged in");
            assertThat(result.data()).isEqualTo(authResponse);
            verify(userService).loginUser(authRequest, response);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/users/me - Get Current User")
    class GetCurrentUserTests {

        @Test
        @DisplayName("Should return current user when authenticated")
        void getCurrentUser_Success() {
            // Arrange
            String token = "valid-token";
            String email = "test@example.com";

            try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
                jwtUtilMock.when(() -> JwtUtil.extractTokenFromRequest(request)).thenReturn(token);
                when(jwtService.extractSubject(token)).thenReturn(email);
                when(userService.getCurrentUser(email)).thenReturn(userResponse);

                // Act
                ResponseDto<UserResponse> result = userController.getCurrentUser(request);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.status()).isEqualTo(HttpStatus.OK);
                assertThat(result.message()).isEqualTo("User retrieved successfully");
                assertThat(result.data()).isEqualTo(userResponse);
                verify(userService).getCurrentUser(email);
            }
        }

        @Test
        @DisplayName("Should return null when token is missing")
        void getCurrentUser_NoToken() {
            // Arrange
            try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
                jwtUtilMock.when(() -> JwtUtil.extractTokenFromRequest(request)).thenReturn(null);

                // Act
                ResponseDto<UserResponse> result = userController.getCurrentUser(request);

                // Assert
                assertThat(result).isNull();
                verify(userService, never()).getCurrentUser(anyString());
            }
        }
    }

    @Nested
    @DisplayName("POST /api/v1/users/refresh - Refresh Token")
    class RefreshTokenTests {

        @Test
        @DisplayName("Should refresh token successfully")
        void refreshToken_Success() {
            // Arrange
            Cookie refreshCookie = new Cookie("refresh", "valid-refresh-token");
            when(request.getCookies()).thenReturn(new Cookie[]{refreshCookie});
            when(tokenBlacklistService.isTokenBlacklisted("valid-refresh-token")).thenReturn(false);
            when(userService.refreshToken(eq("valid-refresh-token"), any(HttpServletResponse.class)))
                    .thenReturn(authResponse);

            // Act
            ResponseDto<AuthResponse> result = userController.refreshToken(request, response);

            // Assert
            assertThat(result.status()).isEqualTo(HttpStatus.OK);
            assertThat(result.message()).isEqualTo("Token refreshed successfully");
            assertThat(result.data()).isEqualTo(authResponse);
        }

        @Test
        @DisplayName("Should return unauthorized when refresh token missing")
        void refreshToken_MissingToken() {
            // Arrange
            when(request.getCookies()).thenReturn(null);

            // Act
            ResponseDto<AuthResponse> result = userController.refreshToken(request, response);

            // Assert
            assertThat(result.status()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(result.message()).isEqualTo("Refresh token not found in cookies");
            assertThat(result.data()).isNull();
            verify(userService, never()).refreshToken(anyString(), any());
        }

        @Test
        @DisplayName("Should return unauthorized when token is blacklisted")
        void refreshToken_BlacklistedToken() {
            // Arrange
            Cookie refreshCookie = new Cookie("refresh", "blacklisted-token");
            when(request.getCookies()).thenReturn(new Cookie[]{refreshCookie});
            when(tokenBlacklistService.isTokenBlacklisted("blacklisted-token")).thenReturn(true);

            // Act
            ResponseDto<AuthResponse> result = userController.refreshToken(request, response);

            // Assert
            assertThat(result.status()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(result.message()).isEqualTo("Refresh token has been revoked");
            assertThat(result.data()).isNull();
            verify(userService, never()).refreshToken(anyString(), any());
        }

        @Test
        @DisplayName("Should handle invalid token exception")
        void refreshToken_InvalidToken() {
            // Arrange
            Cookie refreshCookie = new Cookie("refresh", "invalid-token");
            when(request.getCookies()).thenReturn(new Cookie[]{refreshCookie});
            when(tokenBlacklistService.isTokenBlacklisted("invalid-token")).thenReturn(false);
            when(userService.refreshToken(anyString(), any()))
                    .thenThrow(new IllegalArgumentException("Invalid token"));

            // Act
            ResponseDto<AuthResponse> result = userController.refreshToken(request, response);

            // Assert
            assertThat(result.status()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(result.message()).isEqualTo("Invalid or expired refresh token");
            assertThat(result.data()).isNull();
        }
    }

    @Nested
    @DisplayName("POST /api/v1/users/logout - Logout")
    class LogoutTests {

        @Test
        @DisplayName("Should logout successfully")
        void logout_Success() {
            // Arrange
            String token = "valid-token";

            try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
                jwtUtilMock.when(() -> JwtUtil.extractTokenFromRequest(request)).thenReturn(token);
                doNothing().when(tokenBlacklistService).blacklistToken(token);
                doNothing().when(userService).clearRefreshCookie(response);

                // Act
                ResponseDto<Object> result = userController.logout(request, response);

                // Assert
                assertThat(result.status()).isEqualTo(HttpStatus.OK);
                assertThat(result.message()).isEqualTo("Successfully logged out");
                assertThat(result.data()).isEqualTo("Token revoked and cookies cleared");
                verify(tokenBlacklistService).blacklistToken(token);
                verify(userService).clearRefreshCookie(response);
                assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            }
        }

        @Test
        @DisplayName("Should return bad request when token missing")
        void logout_MissingToken() {
            // Arrange
            try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
                jwtUtilMock.when(() -> JwtUtil.extractTokenFromRequest(request)).thenReturn(null);

                // Act
                ResponseDto<Object> result = userController.logout(request, response);

                // Assert
                assertThat(result.status()).isEqualTo(HttpStatus.BAD_REQUEST);
                assertThat(result.message()).isEqualTo("No token found in request");
                assertThat(result.data()).isNull();
                verify(tokenBlacklistService, never()).blacklistToken(anyString());
            }
        }

        @Test
        @DisplayName("Should handle logout exception")
        void logout_Exception() {
            // Arrange
            String token = "valid-token";

            try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
                jwtUtilMock.when(() -> JwtUtil.extractTokenFromRequest(request)).thenReturn(token);
                doThrow(new RuntimeException("Blacklist error")).when(tokenBlacklistService).blacklistToken(token);

                // Act
                ResponseDto<Object> result = userController.logout(request, response);

                // Assert
                assertThat(result.status()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                assertThat(result.message()).isEqualTo("Logout failed");
                assertThat(result.data()).isEqualTo("Blacklist error");
            }
        }
    }
}