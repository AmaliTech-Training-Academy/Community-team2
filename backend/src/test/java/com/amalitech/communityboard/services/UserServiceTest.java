package com.amalitech.communityboard.services;

import com.amalitech.communityboard.dto.enums.UserRole;
import com.amalitech.communityboard.dto.request.AuthRequest;
import com.amalitech.communityboard.dto.request.UserRequest;
import com.amalitech.communityboard.dto.request.UserUpdateRequest;
import com.amalitech.communityboard.dto.response.AuthResponse;
import com.amalitech.communityboard.dto.response.UserResponse;
import com.amalitech.communityboard.exceptions.EntityNotFoundException;
import com.amalitech.communityboard.exceptions.UserExists;
import com.amalitech.communityboard.mapping.UserMapper;
import com.amalitech.communityboard.models.User;
import com.amalitech.communityboard.notification.EmailNotificationService;
import com.amalitech.communityboard.notification.NotificationDto;
import com.amalitech.communityboard.repository.UserRepository;
import com.amalitech.communityboard.security.CustomUserDetails;
import com.amalitech.communityboard.security.JwtService;
import com.amalitech.communityboard.service.implementations.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailNotificationService emailNotificationService;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserService userService;

    private User sampleUser;
    private UserResponse sampleUserResponse;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setUsername("silas_dev");
        sampleUser.setEmail("silas@amalitech.com");
        sampleUser.setPassword("hashed_password");
        sampleUser.setRole(UserRole.MEMBER);

        sampleUserResponse = UserResponse.builder()
                .id(1L)
                .username("silas_dev")
                .email("silas@amalitech.com")
                .role(UserRole.MEMBER)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // CREATE USER
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("createUser")
    class CreateUser {

        private UserRequest userRequest;

        @BeforeEach
        void setUpRequest() {
            userRequest = new UserRequest("silas_dev", "silas@amalitech.com", "Pass123", UserRole.MEMBER);
        }

        @Test
        @DisplayName("saves user with encoded password and returns response")
        void createUser_valid_savesAndReturns() {
            when(userRepository.existsByEmail("silas@amalitech.com")).thenReturn(false);
            when(userRepository.existsByUsername("silas_dev")).thenReturn(false);
            when(userMapper.toEntity(userRequest)).thenReturn(sampleUser);
            when(passwordEncoder.encode(sampleUser.getPassword())).thenReturn("encoded_password");
            when(userRepository.save(sampleUser)).thenReturn(sampleUser);
            when(userMapper.toResponse(sampleUser)).thenReturn(sampleUserResponse);

            UserResponse result = userService.createUser(userRequest);

            assertThat(result).isEqualTo(sampleUserResponse);
            verify(userRepository).save(sampleUser);
        }

        @Test
        @DisplayName("encodes the plain-text password before saving")
        void createUser_encodesPasswordBeforeSave() {
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userMapper.toEntity(userRequest)).thenReturn(sampleUser);
            when(passwordEncoder.encode("hashed_password")).thenReturn("encoded_password");
            when(userRepository.save(any())).thenReturn(sampleUser);
            when(userMapper.toResponse(sampleUser)).thenReturn(sampleUserResponse);

            userService.createUser(userRequest);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getPassword()).isEqualTo("encoded_password");
        }

        @Test
        @DisplayName("publishes UserCreatedEvent after saving")
        void createUser_publishesUserCreatedEvent() {
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userMapper.toEntity(userRequest)).thenReturn(sampleUser);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
            when(userRepository.save(sampleUser)).thenReturn(sampleUser);
            when(userMapper.toResponse(sampleUser)).thenReturn(sampleUserResponse);

            userService.createUser(userRequest);

            verify(eventPublisher).publishEvent(any());
        }

        @Test
        @DisplayName("throws UserExists when email is already taken")
        void createUser_duplicateEmail_throwsUserExists() {
            when(userRepository.existsByEmail("silas@amalitech.com")).thenReturn(true);

            assertThrows(UserExists.class, () -> userService.createUser(userRequest));

            verifyNoInteractions(userMapper, passwordEncoder, eventPublisher);
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws UserExists when username is already taken")
        void createUser_duplicateUsername_throwsUserExists() {
            when(userRepository.existsByEmail("silas@amalitech.com")).thenReturn(false);
            when(userRepository.existsByUsername("silas_dev")).thenReturn(true);

            assertThrows(UserExists.class, () -> userService.createUser(userRequest));

            verify(userRepository, never()).save(any());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GET USER BY ID
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getUserById")
    class GetUserById {

        @Test
        @DisplayName("returns mapped response when user found")
        void getUserById_found_returnsResponse() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
            when(userMapper.toResponse(sampleUser)).thenReturn(sampleUserResponse);

            UserResponse result = userService.getUserById(1L);

            assertThat(result).isEqualTo(sampleUserResponse);
        }

        @Test
        @DisplayName("throws EntityNotFoundException when user not found")
        void getUserById_notFound_throwsEntityNotFoundException() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> userService.getUserById(99L));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GET ALL USERS
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getAllUsers")
    class GetAllUsers {

        @Test
        @DisplayName("returns mapped page of users")
        void getAllUsers_returnsMappedPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> userPage = new PageImpl<>(List.of(sampleUser), pageable, 1);
            when(userRepository.findAll(pageable)).thenReturn(userPage);
            when(userMapper.toResponse(sampleUser)).thenReturn(sampleUserResponse);

            Page<UserResponse> result = userService.getAllUsers(pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getEmail()).isEqualTo("silas@amalitech.com");
        }

        @Test
        @DisplayName("returns empty page when no users exist")
        void getAllUsers_noUsers_returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            when(userRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of()));

            Page<UserResponse> result = userService.getAllUsers(pageable);

            assertThat(result.getContent()).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // UPDATE USER
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("updateUser")
    class UpdateUser {

        @Test
        @DisplayName("updates username, email, password, and role when all fields are set")
        void updateUser_allFields_updatesAll() {
            UserUpdateRequest request = new UserUpdateRequest();
            request.setUsername("new_name");
            request.setEmail("new@amalitech.com");
            request.setPassword("NewPass123");
            request.setRole(UserRole.ADMIN);

            when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
            when(userRepository.existsByUsername("new_name")).thenReturn(false);
            when(passwordEncoder.encode("NewPass123")).thenReturn("encoded_new");
            when(userMapper.toResponse(sampleUser)).thenReturn(sampleUserResponse);

            userService.updateUser(1L, request);

            assertThat(sampleUser.getUsername()).isEqualTo("new_name");
            assertThat(sampleUser.getEmail()).isEqualTo("new@amalitech.com");
            assertThat(sampleUser.getPassword()).isEqualTo("encoded_new");
            assertThat(sampleUser.getRole()).isEqualTo(UserRole.ADMIN);
        }

        @Test
        @DisplayName("does not update email when new email is null")
        void updateUser_nullEmail_keepsExistingEmail() {
            UserUpdateRequest request = new UserUpdateRequest();
            request.setUsername("new_name");

            when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
            when(userRepository.existsByUsername("new_name")).thenReturn(false);
            when(userMapper.toResponse(sampleUser)).thenReturn(sampleUserResponse);

            userService.updateUser(1L, request);

            assertThat(sampleUser.getEmail()).isEqualTo("silas@amalitech.com");
        }

        @Test
        @DisplayName("does not encode or update password when new password is blank")
        void updateUser_blankPassword_keepsExistingPassword() {
            UserUpdateRequest request = new UserUpdateRequest();
            request.setUsername("new_name");
            request.setPassword("   ");

            when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
            when(userRepository.existsByUsername("new_name")).thenReturn(false);
            when(userMapper.toResponse(sampleUser)).thenReturn(sampleUserResponse);

            userService.updateUser(1L, request);

            assertThat(sampleUser.getPassword()).isEqualTo("hashed_password");
            verify(passwordEncoder, never()).encode(anyString());
        }

        @Test
        @DisplayName("does not update role when new role is null")
        void updateUser_nullRole_keepsExistingRole() {
            UserUpdateRequest request = new UserUpdateRequest();
            request.setUsername("new_name");

            when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
            when(userRepository.existsByUsername("new_name")).thenReturn(false);
            when(userMapper.toResponse(sampleUser)).thenReturn(sampleUserResponse);

            userService.updateUser(1L, request);

            assertThat(sampleUser.getRole()).isEqualTo(UserRole.MEMBER);
        }

        @Test
        @DisplayName("throws UserExists when new username is already taken")
        void updateUser_duplicateUsername_throwsUserExists() {
            UserUpdateRequest request = new UserUpdateRequest();
            request.setUsername("taken_name");

            when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
            when(userRepository.existsByUsername("taken_name")).thenReturn(true);

            assertThrows(UserExists.class, () -> userService.updateUser(1L, request));
        }

        @Test
        @DisplayName("throws EntityNotFoundException when user not found")
        void updateUser_notFound_throwsEntityNotFoundException() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> userService.updateUser(99L, new UserUpdateRequest()));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // DELETE USER
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("deleteUser")
    class DeleteUser {

        @Test
        @DisplayName("deletes user when found")
        void deleteUser_exists_deletes() {
            when(userRepository.existsById(1L)).thenReturn(true);
            doNothing().when(userRepository).deleteById(1L);

            userService.deleteUser(1L);

            verify(userRepository).deleteById(1L);
        }

        @Test
        @DisplayName("throws EntityNotFoundException when user not found")
        void deleteUser_notFound_throwsEntityNotFoundException() {
            when(userRepository.existsById(99L)).thenReturn(false);

            assertThrows(EntityNotFoundException.class, () -> userService.deleteUser(99L));

            verify(userRepository, never()).deleteById(anyLong());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // LOGIN USER
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("loginUser")
    class LoginUser {

        @Test
        @DisplayName("returns AuthResponse with access token on valid credentials")
        void loginUser_validCredentials_returnsAuthResponse() {
            AuthRequest authRequest = new AuthRequest("silas@amalitech.com", "Pass123");
            MockHttpServletResponse httpResponse = new MockHttpServletResponse();

            Authentication authentication = mock(Authentication.class);
            CustomUserDetails userDetails = mock(CustomUserDetails.class);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getPrincipal()).thenReturn(userDetails);
            when(userDetails.getUser()).thenReturn(sampleUser);
            when(jwtService.generateToken(sampleUser))
                    .thenReturn(Map.of("access", "access-token", "refresh", "refresh-token"));
            when(userMapper.toResponse(sampleUser)).thenReturn(sampleUserResponse);

            AuthResponse result = userService.loginUser(authRequest, httpResponse);

            assertThat(result.getAccessToken()).isEqualTo("access-token");
            assertThat(result.getTokenType()).isEqualTo("Bearer");
            assertThat(result.getUser()).isEqualTo(sampleUserResponse);
        }

        @Test
        @DisplayName("sets refresh token cookie on successful login")
        void loginUser_validCredentials_setsRefreshCookie() {
            AuthRequest authRequest = new AuthRequest("silas@amalitech.com", "Pass123");
            MockHttpServletResponse httpResponse = new MockHttpServletResponse();

            Authentication authentication = mock(Authentication.class);
            CustomUserDetails userDetails = mock(CustomUserDetails.class);

            when(authenticationManager.authenticate(any())).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getPrincipal()).thenReturn(userDetails);
            when(userDetails.getUser()).thenReturn(sampleUser);
            when(jwtService.generateToken(sampleUser))
                    .thenReturn(Map.of("access", "access-token", "refresh", "refresh-token"));
            when(userMapper.toResponse(sampleUser)).thenReturn(sampleUserResponse);

            userService.loginUser(authRequest, httpResponse);

            // Verify refresh cookie was added to the response
            assertThat(httpResponse.getCookie("refresh")).isNotNull();
            assertThat(httpResponse.getCookie("refresh").getValue()).isEqualTo("refresh-token");
        }

        @Test
        @DisplayName("propagates BadCredentialsException on invalid credentials")
        void loginUser_invalidCredentials_throwsBadCredentialsException() {
            AuthRequest authRequest = new AuthRequest("silas@amalitech.com", "wrongpass");
            MockHttpServletResponse httpResponse = new MockHttpServletResponse();

            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Invalid credentials"));

            assertThrows(BadCredentialsException.class,
                    () -> userService.loginUser(authRequest, httpResponse));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // REFRESH TOKEN
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("refreshToken")
    class RefreshToken {

        @Test
        @DisplayName("returns new AuthResponse and rotates refresh cookie")
        void refreshToken_valid_returnsNewTokens() {
            MockHttpServletResponse httpResponse = new MockHttpServletResponse();

            when(jwtService.extractSubject("old-refresh")).thenReturn("silas@amalitech.com");
            when(userRepository.findByEmail("silas@amalitech.com")).thenReturn(Optional.of(sampleUser));
            when(jwtService.generateToken(sampleUser))
                    .thenReturn(Map.of("access", "new-access", "refresh", "new-refresh"));
            when(userMapper.toResponse(sampleUser)).thenReturn(sampleUserResponse);

            AuthResponse result = userService.refreshToken("old-refresh", httpResponse);

            assertThat(result.getAccessToken()).isEqualTo("new-access");
            assertThat(result.getTokenType()).isEqualTo("Bearer");
        }

        @Test
        @DisplayName("sets new refresh cookie after token rotation")
        void refreshToken_valid_setsNewRefreshCookie() {
            MockHttpServletResponse httpResponse = new MockHttpServletResponse();

            when(jwtService.extractSubject("old-refresh")).thenReturn("silas@amalitech.com");
            when(userRepository.findByEmail("silas@amalitech.com")).thenReturn(Optional.of(sampleUser));
            when(jwtService.generateToken(sampleUser))
                    .thenReturn(Map.of("access", "new-access", "refresh", "new-refresh"));
            when(userMapper.toResponse(sampleUser)).thenReturn(sampleUserResponse);

            userService.refreshToken("old-refresh", httpResponse);

            assertThat(httpResponse.getCookie("refresh")).isNotNull();
            assertThat(httpResponse.getCookie("refresh").getValue()).isEqualTo("new-refresh");
        }

        @Test
        @DisplayName("throws EntityNotFoundException when user email from token not found")
        void refreshToken_userNotFound_throwsEntityNotFoundException() {
            MockHttpServletResponse httpResponse = new MockHttpServletResponse();

            when(jwtService.extractSubject("bad-refresh")).thenReturn("ghost@nowhere.com");
            when(userRepository.findByEmail("ghost@nowhere.com")).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> userService.refreshToken("bad-refresh", httpResponse));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GET CURRENT USER
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getCurrentUser")
    class GetCurrentUser {

        @Test
        @DisplayName("returns mapped response when email found")
        void getCurrentUser_found_returnsResponse() {
            when(userRepository.findByEmail("silas@amalitech.com")).thenReturn(Optional.of(sampleUser));
            when(userMapper.toResponse(sampleUser)).thenReturn(sampleUserResponse);

            UserResponse result = userService.getCurrentUser("silas@amalitech.com");

            assertThat(result).isEqualTo(sampleUserResponse);
        }

        @Test
        @DisplayName("throws EntityNotFoundException when email not found")
        void getCurrentUser_notFound_throwsEntityNotFoundException() {
            when(userRepository.findByEmail("ghost@nowhere.com")).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> userService.getCurrentUser("ghost@nowhere.com"));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // FORGOT PASSWORD
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("forgotPassword")
    class ForgotPassword {

        @Test
        @DisplayName("sends password reset email with token link")
        void forgotPassword_validEmail_sendsEmail() {
            when(userRepository.findByEmail("silas@amalitech.com")).thenReturn(Optional.of(sampleUser));
            when(jwtService.generateToken(sampleUser))
                    .thenReturn(Map.of("access", "reset-token", "refresh", "refresh-token"));

            userService.forgotPassword("silas@amalitech.com");

            verify(emailNotificationService).send(any(NotificationDto.class));
        }

        @Test
        @DisplayName("builds NotificationDto with correct subject and recipient")
        void forgotPassword_validEmail_buildsCorrectNotification() {
            when(userRepository.findByEmail("silas@amalitech.com")).thenReturn(Optional.of(sampleUser));
            when(jwtService.generateToken(sampleUser))
                    .thenReturn(Map.of("access", "reset-token", "refresh", "refresh-token"));

            userService.forgotPassword("silas@amalitech.com");

            ArgumentCaptor<NotificationDto> captor = ArgumentCaptor.forClass(NotificationDto.class);
            verify(emailNotificationService).send(captor.capture());

            NotificationDto sent = captor.getValue();
            assertThat(sent.subject()).isEqualTo("Password Reset Request");
            assertThat(sent.recipient()).isEqualTo("silas@amalitech.com");
            assertThat(sent.link()).contains("reset-token");
            assertThat(sent.templateName()).isEqualTo("general-email-template");
        }

        @Test
        @DisplayName("includes username in the email message body")
        void forgotPassword_validEmail_includesUsernameInMessage() {
            when(userRepository.findByEmail("silas@amalitech.com")).thenReturn(Optional.of(sampleUser));
            when(jwtService.generateToken(sampleUser))
                    .thenReturn(Map.of("access", "reset-token", "refresh", "refresh-token"));

            userService.forgotPassword("silas@amalitech.com");

            ArgumentCaptor<NotificationDto> captor = ArgumentCaptor.forClass(NotificationDto.class);
            verify(emailNotificationService).send(captor.capture());
            assertThat(captor.getValue().message()).contains("silas_dev");
        }

        @Test
        @DisplayName("throws EntityNotFoundException when email not found")
        void forgotPassword_unknownEmail_throwsEntityNotFoundException() {
            when(userRepository.findByEmail("ghost@nowhere.com")).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> userService.forgotPassword("ghost@nowhere.com"));

            verifyNoInteractions(jwtService, emailNotificationService);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // SET COOKIE
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("setCookie")
    class SetCookie {

        @Test
        @DisplayName("adds HttpOnly Secure cookie to response when token and response are valid")
        void setCookie_valid_addsCookieToResponse() {
            MockHttpServletResponse response = new MockHttpServletResponse();

            userService.setCookie("some-token", response);

            var cookie = response.getCookie("refresh");
            assertThat(cookie).isNotNull();
            assertThat(cookie.getValue()).isEqualTo("some-token");
            assertThat(cookie.isHttpOnly()).isTrue();
            assertThat(cookie.getSecure()).isTrue();
            assertThat(cookie.getPath()).isEqualTo("/");
        }

        @Test
        @DisplayName("does nothing when response is null")
        void setCookie_nullResponse_doesNothing() {
            // Should not throw
            userService.setCookie("some-token", null);
        }

        @Test
        @DisplayName("does nothing when token is null")
        void setCookie_nullToken_doesNothing() {
            MockHttpServletResponse response = new MockHttpServletResponse();

            userService.setCookie(null, response);

            assertThat(response.getCookie("refresh")).isNull();
        }

        @Test
        @DisplayName("does nothing when token is empty")
        void setCookie_emptyToken_doesNothing() {
            MockHttpServletResponse response = new MockHttpServletResponse();

            userService.setCookie("", response);

            assertThat(response.getCookie("refresh")).isNull();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CLEAR REFRESH COOKIE
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("clearRefreshCookie")
    class ClearRefreshCookie {

        @Test
        @DisplayName("adds expired cookie with maxAge 0 to clear the refresh token")
        void clearRefreshCookie_valid_addsExpiredCookie() {
            MockHttpServletResponse response = new MockHttpServletResponse();

            userService.clearRefreshCookie(response);

            var cookie = response.getCookie("refresh");
            assertThat(cookie).isNotNull();
            assertThat(cookie.getMaxAge()).isEqualTo(0);
            assertThat(cookie.isHttpOnly()).isTrue();
            assertThat(cookie.getSecure()).isTrue();
        }

        @Test
        @DisplayName("does nothing when response is null")
        void clearRefreshCookie_nullResponse_doesNothing() {
            // Should not throw
            userService.clearRefreshCookie(null);
        }
    }
}