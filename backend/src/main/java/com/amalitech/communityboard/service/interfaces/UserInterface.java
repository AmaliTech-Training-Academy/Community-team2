package com.amalitech.communityboard.service.interfaces;

import com.amalitech.communityboard.dto.request.AuthRequest;
import com.amalitech.communityboard.dto.request.UserRequest;
import com.amalitech.communityboard.dto.response.AuthResponse;
import com.amalitech.communityboard.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserInterface {
    UserResponse create(UserRequest user);
    UserResponse findByEmail(String email);
    UserResponse findById(Long id);
    Page<UserResponse> findAll(Pageable pageable);
    UserResponse update(Long id, UserRequest user);
    void delete(Long id);
    UserResponse findByUsername(String username);

    AuthResponse login(AuthRequest auth,HttpServletResponse response);

    void clearRefreshCookie(HttpServletResponse response);

    boolean logout(HttpServletRequest request, HttpServletResponse response);

    void setCookie(String token, HttpServletResponse response);
    
    AuthResponse refreshToken(String refresh,HttpServletResponse response);

    UserResponse getCurrentUser(String email);
}
