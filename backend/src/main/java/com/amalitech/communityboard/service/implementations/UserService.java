package com.amalitech.communityboard.service.implementations;

import com.amalitech.communityboard.dto.request.AuthRequest;
import com.amalitech.communityboard.dto.request.UserRequest;
import com.amalitech.communityboard.dto.request.UserUpdateRequest;
import com.amalitech.communityboard.dto.response.AuthResponse;
import com.amalitech.communityboard.dto.response.UserResponse;
import com.amalitech.communityboard.exceptions.EntityNotFoundException;
import com.amalitech.communityboard.exceptions.UserExists;
import com.amalitech.communityboard.mapping.UserMapper;
import com.amalitech.communityboard.models.User;
import com.amalitech.communityboard.repository.UserRepository;
import com.amalitech.communityboard.security.CustomUserDetails;
import com.amalitech.communityboard.security.JwtService;
import com.amalitech.communityboard.service.interfaces.UserInterface;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService implements UserInterface {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private  final PasswordEncoder passwordEncoder;

    @Value("${app.cookie.max-age}")
    private int cookieMaxAge;

    @Override
    @Transactional
    public UserResponse createUser(UserRequest userrequest) {
        if (userRepository.existsByEmail(userrequest.getEmail()) || userRepository.existsByUsername(userrequest.getUsername())) {
            throw new UserExists("User with given email or username already exists");
        }
        User user = userMapper.toEntity(userrequest);
        String password = passwordEncoder.encode(user.getPassword());
        user.setPassword(password);
        User saved = userRepository.save(user);
        return userMapper.toResponse(saved);
    }



    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("user not found"));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.map(userMapper::toResponse);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest user) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("user not found"));

        if (user.getUsername() != null &&
                userRepository.existsByUsername(user.getUsername())) {
            throw new UserExists("Username already taken");
        }
        existing.setUsername(user.getUsername());
        if (user.getEmail() != null) {
            existing.setEmail(user.getEmail());
        }
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            String encoded = passwordEncoder.encode(user.getPassword());
            existing.setPassword(encoded);
        }
        if (user.getRole() != null) {
            existing.setRole(user.getRole());
        }

        return userMapper.toResponse(existing);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }


    @Override
    public AuthResponse loginUser(AuthRequest auth,HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(auth.getEmail(), auth.getPassword()));

        if (authentication.isAuthenticated()) {

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            if (userDetails == null) {
                throw new BadCredentialsException("Authentication failed: user details unavailable");
            }
            User user = userDetails.getUser();
            Map<String, String> tokens = jwtService.generateToken(user);
            String accessToken = tokens.get("access");
            String refreshToken = tokens.get("refresh");

            setCookie(refreshToken, response);

            return new AuthResponse(accessToken, "Bearer",userMapper.toResponse(user));
        } else {
            throw new BadCredentialsException("Invalid credentials");
        }
    }

    public void clearRefreshCookie(HttpServletResponse response) {
        if (response == null) {
            return;
        }
        try {
            Cookie refreshTokenCookie = new Cookie("refresh", null);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(true);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(0);

            response.addCookie(refreshTokenCookie);
        } catch (Exception e) {
            log.error("[COOKIE] Failed to clear refresh token cookie: {}", e.getMessage(), e);
        }
    }

    @Override
    public void setCookie(String token, HttpServletResponse response) {
        if (response == null || token == null || token.isEmpty()) {
            return;
        }
        try {
            Cookie refreshTokenCookie = new Cookie("refresh", token);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(true);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(cookieMaxAge);
            refreshTokenCookie.setAttribute("SameSite", "Strict");

            response.addCookie(refreshTokenCookie);
            log.debug("[COOKIE] Refresh token cookie set successfully");
        } catch (Exception e) {
            log.error("[COOKIE] Failed to set refresh token cookie: {}", e.getMessage(), e);
        }
    }

    @Override
    public AuthResponse refreshToken(String refresh, HttpServletResponse response) {
        try {

            String subject = jwtService.extractSubject(refresh);
            User user = userRepository.findByEmail(subject).orElseThrow(() -> new EntityNotFoundException("User not found"));

            Map<String, String> newTokens = jwtService.generateToken(user);
            String newAccessToken = newTokens.get("access");
            String newRefreshToken = newTokens.get("refresh");

            setCookie(newRefreshToken, response);
            log.info("[REFRESH] Token refreshed successfully for user: {}", subject);

            return new AuthResponse(newAccessToken, "Bearer",userMapper.toResponse(user));
        } catch (Exception e) {
            log.error("[REFRESH] Token refresh failed: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("User not found"));
        return userMapper.toResponse(user);
    }
}
