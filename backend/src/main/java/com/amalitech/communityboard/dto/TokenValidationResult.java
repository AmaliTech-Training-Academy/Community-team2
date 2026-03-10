package com.amalitech.communityboard.dto;

import io.jsonwebtoken.Claims;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Builder
public class TokenValidationResult {
    private final String subject;
    private Long userId;
    private final List<String> roles;
    private final boolean isValid;
    private final Date expiration;
    private final Claims claims;
    private final Date cachedAt;


    private static final TokenValidationResult INVALID_RESULT = TokenValidationResult.builder()
            .isValid(false)
            .build();

    public static TokenValidationResult invalid() {
        return INVALID_RESULT;
    }

    public boolean isExpired() {
        return cachedAt != null &&
                (System.currentTimeMillis() - cachedAt.getTime()) > 300000;
    }
}