package com.amalitech.communityboard.controller;



import com.amalitech.communityboard.dto.ResponseDto;
import com.amalitech.communityboard.dto.enums.UserRole;
import com.amalitech.communityboard.dto.request.AuthRequest;
import com.amalitech.communityboard.dto.request.ForgotPasswordRequest;
import com.amalitech.communityboard.dto.request.UserRequest;
import com.amalitech.communityboard.dto.request.UserUpdateRequest;
import com.amalitech.communityboard.dto.response.AuthResponse;
import com.amalitech.communityboard.dto.response.UserResponse;
import com.amalitech.communityboard.exceptions.EntityNotFoundException;
import com.amalitech.communityboard.exceptions.UserExists;
import com.amalitech.communityboard.security.JwtService;
import com.amalitech.communityboard.security.TokenBlacklistService;
import com.amalitech.communityboard.service.interfaces.UserInterface;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Unit Tests")
class UserControllerTest {

    @Mock
    private UserInterface userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private UserController userController;

    private UserResponse sampleUserResponse;
    private UserRequest sampleUserRequest;
    private AuthResponse sampleAuthResponse;

    @BeforeEach
    void setUp() {
        sampleUserResponse = UserResponse.builder()
                .id(1L)
                .username("silas_dev")
                .email("silas@amalitech.com")
                .role(UserRole.MEMBER)
                .createdAt(LocalDateTime.now())
                .build();

        sampleUserRequest = new UserRequest("silas_dev", "silas@amalitech.com", "Pass123", UserRole.MEMBER);

        sampleAuthResponse = new AuthResponse("access-token-abc", "Bearer", sampleUserResponse);
    }

    // ─────────────────────────────────────────────────────────────
    // CREATE USER
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST / – createUser")
    class CreateUser {

        @Test
        @DisplayName("returns 201 and user data when request is valid")
        void createUser_validRequest_returns201() {
            when(userService.createUser(any(UserRequest.class))).thenReturn(sampleUserResponse);

            ResponseDto<UserResponse> response = userController.createUser(sampleUserRequest);

            assertThat(response.status()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.message()).isEqualTo("user created");
            assertThat(response.data()).isEqualTo(sampleUserResponse);
            verify(userService).createUser(sampleUserRequest);
        }

        @Test
        @DisplayName("propagates UserExists when email/username is already taken")
        void createUser_duplicateUser_throwsUserExists() {
            when(userService.createUser(any(UserRequest.class)))
                    .thenThrow(new UserExists("User with given email or username already exists"));

            org.junit.jupiter.api.Assertions.assertThrows(
                    UserExists.class,
                    () -> userController.createUser(sampleUserRequest)
            );
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GET ALL USERS
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET / – getAllUsers")
    class GetAllUsers {

        @Test
        @DisplayName("returns paginated user list with 200 status")
        void getAllUsers_returnsPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<UserResponse> userPage = new PageImpl<>(List.of(sampleUserResponse), pageable, 1);
            when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);

            ResponseDto<Page<UserResponse>> response = userController.getAllUsers(pageable);

            assertThat(response.status()).isEqualTo(HttpStatus.OK);
            assertThat(response.message()).isEqualTo("users retrieved");
            assertThat(response.data().getContent()).hasSize(1);
            assertThat(response.data().getContent().get(0).getEmail()).isEqualTo("silas@amalitech.com");
        }

        @Test
        @DisplayName("returns empty page when no users exist")
        void getAllUsers_noUsers_returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<UserResponse> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            when(userService.getAllUsers(any(Pageable.class))).thenReturn(emptyPage);

            ResponseDto<Page<UserResponse>> response = userController.getAllUsers(pageable);

            assertThat(response.data().getContent()).isEmpty();
            assertThat(response.data().getTotalElements()).isZero();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GET USER BY ID
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /{id} – getUserById")
    class GetUserById {

        @Test
        @DisplayName("returns 200 and user when found")
        void getUserById_found_returnsUser() {
            when(userService.getUserById(1L)).thenReturn(sampleUserResponse);

            ResponseDto<UserResponse> response = userController.getUserById(1L);

            assertThat(response.status()).isEqualTo(HttpStatus.OK);
            assertThat(response.message()).isEqualTo("user retrieved");
            assertThat(response.data().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("propagates EntityNotFoundException when user not found")
        void getUserById_notFound_throwsEntityNotFoundException() {
            when(userService.getUserById(anyLong()))
                    .thenThrow(new EntityNotFoundException("user not found"));

            org.junit.jupiter.api.Assertions.assertThrows(
                    EntityNotFoundException.class,
                    () -> userController.getUserById(99L)
            );
        }
    }

    // ─────────────────────────────────────────────────────────────
    // UPDATE USER
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("PUT /{id} – updateUser")
    class UpdateUser {

        @Test
        @DisplayName("returns 200 and updated user when request is valid")
        void updateUser_validRequest_returnsUpdated() {
            UserUpdateRequest updateRequest = new UserUpdateRequest();
            updateRequest.setUsername("new_username");

            UserResponse updated = UserResponse.builder()
                    .id(1L)
                    .username("new_username")
                    .email("silas@amalitech.com")
                    .role(UserRole.MEMBER)
                    .build();

            when(userService.updateUser(eq(1L), any(UserUpdateRequest.class))).thenReturn(updated);

            ResponseDto<UserResponse> response = userController.updateUser(1L, updateRequest);

            assertThat(response.status()).isEqualTo(HttpStatus.OK);
            assertThat(response.message()).isEqualTo("user updated");
            assertThat(response.data().getUsername()).isEqualTo("new_username");
        }

        @Test
        @DisplayName("propagates EntityNotFoundException when user not found")
        void updateUser_notFound_throwsEntityNotFoundException() {
            UserUpdateRequest updateRequest = new UserUpdateRequest();
            when(userService.updateUser(anyLong(), any(UserUpdateRequest.class)))
                    .thenThrow(new EntityNotFoundException("user not found"));

            org.junit.jupiter.api.Assertions.assertThrows(
                    EntityNotFoundException.class,
                    () -> userController.updateUser(99L, updateRequest)
            );
        }

        @Test
        @DisplayName("propagates UserExists when new username is already taken")
        void updateUser_duplicateUsername_throwsUserExists() {
            UserUpdateRequest updateRequest = new UserUpdateRequest();
            updateRequest.setUsername("taken_username");

            when(userService.updateUser(anyLong(), any(UserUpdateRequest.class)))
                    .thenThrow(new UserExists("Username already taken"));

            org.junit.jupiter.api.Assertions.assertThrows(
                    UserExists.class,
                    () -> userController.updateUser(1L, updateRequest)
            );
        }
    }

    // ─────────────────────────────────────────────────────────────
    // DELETE USER
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /{id} – deleteUser")
    class DeleteUser {

        @Test
        @DisplayName("returns 204 No Content when user is deleted")
        void deleteUser_exists_returns204() {
            doNothing().when(userService).deleteUser(1L);

            ResponseEntity<Void> response = userController.deleteUser(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(userService).deleteUser(1L);
        }

        @Test
        @DisplayName("propagates EntityNotFoundException when user not found")
        void deleteUser_notFound_throwsEntityNotFoundException() {
            doThrow(new EntityNotFoundException("User not found")).when(userService).deleteUser(99L);

            org.junit.jupiter.api.Assertions.assertThrows(
                    EntityNotFoundException.class,
                    () -> userController.deleteUser(99L)
            );
        }
    }

    // ─────────────────────────────────────────────────────────────
    // LOGIN
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /login – login")
    class Login {

        @Test
        @DisplayName("returns 200 and AuthResponse on valid credentials")
        void login_validCredentials_returnsAuthResponse() {
            AuthRequest authRequest = new AuthRequest("silas@amalitech.com", "Pass123");
            MockHttpServletResponse httpResponse = new MockHttpServletResponse();

            when(userService.loginUser(any(AuthRequest.class), any())).thenReturn(sampleAuthResponse);

            ResponseDto<AuthResponse> response = userController.login(authRequest, httpResponse);

            assertThat(response.status()).isEqualTo(HttpStatus.OK);
            assertThat(response.message()).isEqualTo("User logged in");
            assertThat(response.data().getAccessToken()).isEqualTo("access-token-abc");
            assertThat(response.data().getTokenType()).isEqualTo("Bearer");
        }

        @Test
        @DisplayName("propagates BadCredentialsException on invalid credentials")
        void login_invalidCredentials_throwsBadCredentials() {
            AuthRequest authRequest = new AuthRequest("silas@amalitech.com", "wrongpass");
            MockHttpServletResponse httpResponse = new MockHttpServletResponse();

            when(userService.loginUser(any(AuthRequest.class), any()))
                    .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Invalid credentials"));

            org.junit.jupiter.api.Assertions.assertThrows(
                    org.springframework.security.authentication.BadCredentialsException.class,
                    () -> userController.login(authRequest, httpResponse)
            );
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GET CURRENT USER (/me)
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /me – getCurrentUser")
    class GetCurrentUser {

        @Test
        @DisplayName("returns 200 and user when token is valid")
        void getCurrentUser_validToken_returnsUser() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer valid-token");

            when(jwtService.extractSubject("valid-token")).thenReturn("silas@amalitech.com");
            when(userService.getCurrentUser("silas@amalitech.com")).thenReturn(sampleUserResponse);

            ResponseDto<UserResponse> response = userController.getCurrentUser(request);

            assertThat(response.status()).isEqualTo(HttpStatus.OK);
            assertThat(response.data().getEmail()).isEqualTo("silas@amalitech.com");
        }

        @Test
        @DisplayName("returns null when no Authorization header present")
        void getCurrentUser_noToken_returnsNull() {
            MockHttpServletRequest request = new MockHttpServletRequest();

            ResponseDto<UserResponse> response = userController.getCurrentUser(request);

            assertThat(response).isNull();
            verifyNoInteractions(userService);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // REFRESH TOKEN
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /refresh – refreshToken")
    class RefreshToken {

        @Test
        @DisplayName("returns 200 and new tokens when refresh cookie is valid")
        void refreshToken_validCookie_returnsNewTokens() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setCookies(new Cookie("refresh", "valid-refresh-token"));
            MockHttpServletResponse response = new MockHttpServletResponse();

            when(tokenBlacklistService.isTokenBlacklisted("valid-refresh-token")).thenReturn(false);
            when(userService.refreshToken(eq("valid-refresh-token"), any())).thenReturn(sampleAuthResponse);

            ResponseDto<AuthResponse> result = userController.refreshToken(request, response);

            assertThat(result.status()).isEqualTo(HttpStatus.OK);
            assertThat(result.message()).isEqualTo("Token refreshed successfully");
            assertThat(result.data().getAccessToken()).isEqualTo("access-token-abc");
        }

        @Test
        @DisplayName("returns 401 when no refresh cookie present")
        void refreshToken_noCookie_returns401() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();

            ResponseDto<AuthResponse> result = userController.refreshToken(request, response);

            assertThat(result.status()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(result.message()).isEqualTo("Refresh token not found in cookies");
            verifyNoInteractions(userService);
        }

        @Test
        @DisplayName("returns 401 when refresh token is blacklisted")
        void refreshToken_blacklistedToken_returns401() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setCookies(new Cookie("refresh", "revoked-token"));
            MockHttpServletResponse response = new MockHttpServletResponse();

            when(tokenBlacklistService.isTokenBlacklisted("revoked-token")).thenReturn(true);

            ResponseDto<AuthResponse> result = userController.refreshToken(request, response);

            assertThat(result.status()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(result.message()).isEqualTo("Refresh token has been revoked");
            verifyNoInteractions(userService);
        }

        @Test
        @DisplayName("returns 400 when refresh token is invalid or expired")
        void refreshToken_invalidToken_returns400() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setCookies(new Cookie("refresh", "bad-token"));
            MockHttpServletResponse response = new MockHttpServletResponse();

            when(tokenBlacklistService.isTokenBlacklisted("bad-token")).thenReturn(false);
            when(userService.refreshToken(eq("bad-token"), any()))
                    .thenThrow(new IllegalArgumentException("Invalid token"));

            ResponseDto<AuthResponse> result = userController.refreshToken(request, response);

            assertThat(result.status()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("returns 500 when refresh throws unexpected exception")
        void refreshToken_unexpectedException_returns500() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setCookies(new Cookie("refresh", "error-token"));
            MockHttpServletResponse response = new MockHttpServletResponse();

            when(tokenBlacklistService.isTokenBlacklisted("error-token")).thenReturn(false);
            when(userService.refreshToken(eq("error-token"), any()))
                    .thenThrow(new RuntimeException("Unexpected error"));

            ResponseDto<AuthResponse> result = userController.refreshToken(request, response);

            assertThat(result.status()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // LOGOUT
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /logout – logout")
    class Logout {

        @Test
        @DisplayName("returns 200 and revokes token on valid logout")
        void logout_validToken_returns200() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer valid-token");
            MockHttpServletResponse response = new MockHttpServletResponse();

            doNothing().when(tokenBlacklistService).blacklistToken("valid-token");
            doNothing().when(userService).clearRefreshCookie(any());

            ResponseDto<Object> result = userController.logout(request, response);

            assertThat(result.status()).isEqualTo(HttpStatus.OK);
            assertThat(result.message()).isEqualTo("Successfully logged out");
            verify(tokenBlacklistService).blacklistToken("valid-token");
            verify(userService).clearRefreshCookie(any());
        }

        @Test
        @DisplayName("returns 400 when no token found in request")
        void logout_noToken_returns400() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();

            ResponseDto<Object> result = userController.logout(request, response);

            assertThat(result.status()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(result.message()).isEqualTo("No token found in request");
            verifyNoInteractions(tokenBlacklistService);
        }

        @Test
        @DisplayName("returns 500 when blacklisting fails unexpectedly")
        void logout_blacklistFails_returns500() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer some-token");
            MockHttpServletResponse response = new MockHttpServletResponse();

            doThrow(new RuntimeException("Cache failure")).when(tokenBlacklistService).blacklistToken(anyString());

            ResponseDto<Object> result = userController.logout(request, response);

            assertThat(result.status()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // FORGOT PASSWORD
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /forgot-password – forgotPassword")
    class ForgotPassword {

        @Test
        @DisplayName("returns 200 OK when email exists")
        void forgotPassword_validEmail_returns200() {
            ForgotPasswordRequest request = new ForgotPasswordRequest("silas@amalitech.com");
            doNothing().when(userService).forgotPassword("silas@amalitech.com");

            ResponseEntity<Void> response = userController.forgotPassword(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(userService).forgotPassword("silas@amalitech.com");
        }

        @Test
        @DisplayName("propagates EntityNotFoundException when email not found")
        void forgotPassword_unknownEmail_throwsEntityNotFoundException() {
            ForgotPasswordRequest request = new ForgotPasswordRequest("ghost@nowhere.com");
            doThrow(new EntityNotFoundException("User not found")).when(userService).forgotPassword(anyString());

            org.junit.jupiter.api.Assertions.assertThrows(
                    EntityNotFoundException.class,
                    () -> userController.forgotPassword(request)
            );
        }
    }
}