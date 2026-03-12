package com.amalitech.communityboard.controller;

import com.amalitech.communityboard.dto.ResponseDto;
import com.amalitech.communityboard.dto.request.AuthRequest;
import com.amalitech.communityboard.dto.request.ForgotPasswordRequest;
import com.amalitech.communityboard.dto.request.UserRequest;
import com.amalitech.communityboard.dto.request.UserUpdateRequest;
import com.amalitech.communityboard.dto.response.AuthResponse;
import com.amalitech.communityboard.dto.response.UserResponse;
import com.amalitech.communityboard.security.JwtService;
import com.amalitech.communityboard.security.JwtUtil;
import com.amalitech.communityboard.security.TokenBlacklistService;
import com.amalitech.communityboard.service.interfaces.UserInterface;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User account management endpoints")
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
    @Operation(summary = "Create user", description = "Create a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    public ResponseDto<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        UserResponse userResponse = userService.createUser(request);
        return new ResponseDto<>(HttpStatus.CREATED, "user created", userResponse);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Retrieve a list of all users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserResponse.class))))
    })
    public ResponseDto<Page<UserResponse>> getAllUsers(@PageableDefault(size = 10) Pageable pageable) {
        Page<UserResponse> users = userService.getAllUsers(pageable);
        return new ResponseDto<>(HttpStatus.OK, "users retrieved", users);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by id", description = "Retrieve a single user by its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseDto<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return new ResponseDto<>(HttpStatus.OK, "user retrieved", user);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isOwner(#id,authentication)")
    @Operation(summary = "Update user", description = "Update an existing user's data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    public ResponseDto<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        UserResponse updated = userService.updateUser(id, request);
        return new ResponseDto<>(HttpStatus.OK, "user updated", updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isOwner(#id,authentication)")
    @Operation(summary = "Delete user", description = "Delete a user by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate user with credentials")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = AuthRequest.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseDto<AuthResponse> login(@Valid @RequestBody AuthRequest loginRequest,HttpServletResponse response) {
        AuthResponse authResponse = userService.loginUser(loginRequest,response);
        return new ResponseDto<>(HttpStatus.OK, "User logged in", authResponse);
    }
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get current logged in user ", description = "Retrieve logged in single user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
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
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Refresh JWT token", description = "Generate a new JWT token using refresh token from HttpOnly cookie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid or missing refresh token"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
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
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Logout user", description = "Revoke the current JWT token and invalidate session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful"),
            @ApiResponse(responseCode = "401", description = "No valid token found")
    })
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

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        userService.forgotPassword(request.email());
        return ResponseEntity.ok().build();
    }
}
