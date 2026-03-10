package com.amalitech.communityboard.services;


import com.amalitech.communityboard.dto.enums.AccountProvider;
import com.amalitech.communityboard.dto.enums.UserRole;
import com.amalitech.communityboard.dto.request.AuthRequest;
import com.amalitech.communityboard.dto.request.UserRequest;
import com.amalitech.communityboard.dto.response.UserResponse;
import com.amalitech.communityboard.exceptions.EntityNotFoundException;
import com.amalitech.communityboard.mapping.UserMapper;
import com.amalitech.communityboard.models.User;
import com.amalitech.communityboard.repository.UserRepository;
import com.amalitech.communityboard.security.JwtService;
import com.amalitech.communityboard.service.implementations.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldReturnUserFound() {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("username");
        mockUser.setEmail("email@gmail.com");
        mockUser.setPassword("Testpassword");

        UserResponse expectedResponse = new UserResponse(
                1L,
                "username",
                "email@gmail.com",
                UserRole.MEMBER,
                AccountProvider.LOCAL,
                LocalDateTime.now()
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userMapper.toResponse(mockUser)).thenReturn(expectedResponse);

        UserResponse result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("username", result.getUsername());
        assertEquals("email@gmail.com", result.getEmail());
        assertEquals(UserRole.MEMBER, result.getRole());

        verify(userRepository, times(1)).findById(1L);
        verify(userMapper, times(1)).toResponse(mockUser);
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> userService.getUserById(999L));
        verify(userRepository, times(1)).findById(999L);

    }

    @Test
    void shouldReturnPagedUsers() {
        User user = new User();
        user.setId(1L);
        user.setUsername("username");
        user.setPassword("Testpassword");
        user.setRole(UserRole.MEMBER);
        user.setEmail("email@gmail.com");

        int pageSize = 10;
        int pageNumber = 1;
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("username").ascending());
        Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);

        when(userRepository.findAll(pageable)).thenReturn(userPage);

        UserResponse expectedResponse = new UserResponse(
                1L,
                "username",
                "email@gmail.com",
                UserRole.MEMBER,
                AccountProvider.LOCAL,
                LocalDateTime.now()
        );
        when(userMapper.toResponse(user)).thenReturn(expectedResponse);

        Page<UserResponse> result = userService.getAllUsers(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("username", result.getContent().get(0).getUsername());

        verify(userRepository, times(1)).findAll(pageable);
        verify(userMapper, times(1)).toResponse(user);
    }

    @Test
    void shouldDeleteUserWhenExists() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void shouldCreateUser() {
        UserRequest userRequest = new UserRequest(
                "username",
                "email@gmail.com",
                "Testpassword",
                UserRole.MEMBER
        );

        User user =  User.builder()
                        .username("username")
                        .email("email@gmail.com")
                        .password("Testpassowrd")
                .role(UserRole.MEMBER)
                .provider(AccountProvider.LOCAL)
                        .build();

        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(userRequest.getUsername())).thenReturn(false);
        when(userMapper.toEntity(userRequest)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);

        userService.createUser(userRequest);

        verify(userRepository, times(1)).existsByEmail(userRequest.getEmail());
        verify(userRepository, times(1)).existsByUsername(userRequest.getUsername());
        verify(userMapper, times(1)).toEntity(userRequest);
        verify(userRepository, times(2)).save(user); // called in service before mapping to response
    }

    @Test
    void loginUser_shouldThrowBadCredentialsOnAuthenticationFailure() {
        AuthRequest authRequest = new AuthRequest("email@gmail.com", "wrong-password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> userService.loginUser(authRequest, null));

        verify(authenticationManager, times(1))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(jwtService);
    }

    @Test
    void getUserById_usesRepositoryOnce_whenCalledTwice() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setUsername("tester");

        UserResponse response = new UserResponse(1L,
                "tester", "test@example.com",
                UserRole.MEMBER,
                AccountProvider.LOCAL, LocalDateTime.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(response);

        UserResponse first = userService.getUserById(1L);
        UserResponse second = userService.getUserById(1L);

        assertNotNull(first);
        assertNotNull(second);
        assertEquals(first.getId(), second.getId());

        verify(userRepository, times(2)).findById(1L);
    }
}
