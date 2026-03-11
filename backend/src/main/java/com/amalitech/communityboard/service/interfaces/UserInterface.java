package com.amalitech.communityboard.service.interfaces;

import com.amalitech.communityboard.dto.request.AuthRequest;
import com.amalitech.communityboard.dto.request.UserRequest;
import com.amalitech.communityboard.dto.request.UserUpdateRequest;
import com.amalitech.communityboard.dto.response.AuthResponse;
import com.amalitech.communityboard.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserInterface {
    UserResponse createUser(UserRequest user);
    UserResponse getUserById(Long id);
    Page<UserResponse> getAllUsers(Pageable pageable);
    UserResponse updateUser(Long id, UserUpdateRequest user);
    void deleteUser(Long id);

    AuthResponse loginUser(AuthRequest auth, HttpServletResponse response);

    void clearRefreshCookie(HttpServletResponse response);

    void setCookie(String token, HttpServletResponse response);
    
    AuthResponse refreshToken(String refresh, HttpServletResponse response);

    UserResponse getCurrentUser(String email);
}
