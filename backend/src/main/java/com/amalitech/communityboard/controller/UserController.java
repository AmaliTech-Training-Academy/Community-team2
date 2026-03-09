package com.amalitech.communityboard.controller;

import com.amalitech.communityboard.dto.ResponseDto;
import com.amalitech.communityboard.dto.request.AuthRequest;
import com.amalitech.communityboard.dto.request.UserRequest;
import com.amalitech.communityboard.dto.request.UserUpdateRequest;
import com.amalitech.communityboard.dto.response.AuthResponse;
import com.amalitech.communityboard.dto.response.UserResponse;
import com.amalitech.communityboard.security.JwtService;
import com.amalitech.communityboard.security.JwtUtil;
import com.amalitech.communityboard.security.TokenBlacklistService;
import com.amalitech.communityboard.service.interfaces.UserInterface;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserInterface userService;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    public UserController(
                          UserInterface userService,
                          JwtService jwtService,
                          TokenBlacklistService tokenBlacklistService
        ) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostMapping
    public ResponseDto<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        UserResponse userResponse = userService.createUser(request);
        return new ResponseDto<>(HttpStatus.CREATED, "user created", userResponse);
    }

    @GetMapping
    public ResponseDto<Page<UserResponse>> getAllUsers(@PageableDefault(size = 10) Pageable pageable) {
        Page<UserResponse> users = userService.getAllUsers(pageable);
        return new ResponseDto<>(HttpStatus.OK, "users retrieved", users);
    }

    @GetMapping("/{id}")
    public ResponseDto<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return new ResponseDto<>(HttpStatus.OK, "user retrieved", user);
    }

    @PutMapping("/{id}")
    public ResponseDto<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        UserResponse updated = userService.updateUser(id, request);
        return new ResponseDto<>(HttpStatus.OK, "user updated", updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseDto<AuthResponse> login(@Valid @RequestBody AuthRequest loginRequest,HttpServletResponse response) {
        AuthResponse authResponse = userService.loginUser(loginRequest,response);
        return new ResponseDto<>(HttpStatus.OK, "User logged in", authResponse);
    }
    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    @Operation()
    public ResponseDto<UserResponse> getCurrentUser(HttpServletRequest request) {
        String token = JwtUtil.extractTokenFromRequest(request);
        if (token != null) {
            String email = jwtService.extractSubject(token);
            UserResponse user = userService.getCurrentUser(email);
            return new ResponseDto<>(HttpStatus.OK, "User retrieved successfully", user);
        }
        return null;
    }
    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public ResponseDto<AuthResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        String refreshToken = null;

        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                log.info("Cookie name: {}", cookie.getName());
                if ("refresh".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null || refreshToken.isEmpty()) {
            return new ResponseDto<>(HttpStatus.UNAUTHORIZED, "Refresh token not found in cookies", null);
        }

        if (tokenBlacklistService.isTokenBlacklisted(refreshToken)) {
            return new ResponseDto<>(HttpStatus.UNAUTHORIZED, "Refresh token has been revoked", null);
        }

        try {
            AuthResponse loginResponse = userService.refreshToken(refreshToken, response);
            return new ResponseDto<>(HttpStatus.OK, "Token refreshed successfully", loginResponse);
        } catch (IllegalArgumentException e) {
            return new ResponseDto<>(HttpStatus.BAD_REQUEST, "Invalid or expired refresh token", null);
        } catch (Exception e) {
            return new ResponseDto<>(HttpStatus.INTERNAL_SERVER_ERROR, "Error refreshing token", null);
        }
    }

    @PostMapping("/logout")
    public ResponseDto<Object> logout(HttpServletRequest request, HttpServletResponse response) {
        String token = JwtUtil.extractTokenFromRequest(request);

        if (token == null || token.isEmpty()) {
            return new ResponseDto<>(HttpStatus.BAD_REQUEST, "No token found in request", null);
        }

        try {
            tokenBlacklistService.blacklistToken(token);

            userService.clearRefreshCookie(response);
            SecurityContextHolder.clearContext();

            return new ResponseDto<>(HttpStatus.OK, "Successfully logged out", "Token revoked and cookies cleared");
        } catch (Exception e) {
            return new ResponseDto<>(HttpStatus.INTERNAL_SERVER_ERROR, "Logout failed", e.getMessage());
        }

    }
}
