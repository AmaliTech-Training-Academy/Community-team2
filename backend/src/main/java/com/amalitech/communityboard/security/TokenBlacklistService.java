package com.amalitech.communityboard.security;


import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Service to manage token blacklisting for logout functionality.
 * Uses Caffeine cache with TTL matching token expiration time.
 * When a user logs out, their token is added to the blacklist.
 * The JwtAuthenticationFilter checks this blacklist before validating tokens.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final CacheManager cacheManager;
    private final JwtService jwtService;

    @Value("${app.jwt.secret}")
    private String jwtSecret;


    /**
     * Add a token to the blacklist (cache)
     * The cache automatically expires entries based on TTL
     * TTL is calculated from token expiration time
     */
    public void blacklistToken(String token) {
        if (token == null) {
            return;
        }
        try {
            // Normalize token (remove accidental whitespace)
            token = token.trim();

            if (token.isEmpty()) {
                return;
            }

            log.debug("[BLACKLIST] Attempting to blacklist token: {}...", token.substring(0, Math.min(20, token.length())));
            long expirationTime = getTokenExpirationTime(token);
            long currentTime = System.currentTimeMillis();
            long ttlMs = expirationTime - currentTime;


            if (ttlMs > 0) {
                Cache cache = cacheManager.getCache("tokenBlacklist");
                if (cache != null) {
                    cache.put(token, "blacklisted_at_" + System.currentTimeMillis());
                }
            } else {
                log.debug("[BLACKLIST] Token already expired; skipping blacklist");
            }
        } catch (IllegalArgumentException e) {
            // Invalid token format or signature mismatch during logout should not crash the flow
            log.warn("[BLACKLIST] Skipping invalid token during blacklist: {}", e.getMessage());
        } catch (Exception e) {
            log.error("[BLACKLIST ERROR] Failed to blacklist token: {}", e.getMessage(), e);
        }
    }

    /**
     * Check if a token is blacklisted
     * Returns true if token is in blacklist (meaning it's been revoked)
     * Returns false if token is NOT in blacklist (still valid)
     */
    public boolean isTokenBlacklisted(String token) {
        try {
            Cache cache = cacheManager.getCache("tokenBlacklist");

            if (cache == null) {
                return false;
            }

            Object cachedValue = cache.get(token);
            return cachedValue != null;
        } catch (Exception e) {
            log.error("[BLACKLIST CHECK ERROR] Failed to check blacklist: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Extract expiration time from JWT token
     */
    private long getTokenExpirationTime(String token) {
        try {
            Claims claims = jwtService.validateAndExtract(token).getClaims();

            if (claims == null) {
                throw new IllegalArgumentException("Unable to extract claims from token");
            }

            Date expiration = claims.getExpiration();
            if (expiration == null) {
                throw new IllegalArgumentException("Token has no expiration claim");
            }

            return expiration.getTime();
        } catch (Exception e) {
            log.error("[TOKEN PARSING ERROR] Failed to parse token: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid token format", e);
        }
    }
}
