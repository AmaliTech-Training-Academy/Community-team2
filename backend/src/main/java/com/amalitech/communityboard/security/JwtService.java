package com.amalitech.communityboard.security;


import com.amalitech.communityboard.dto.TokenValidationResult;
import com.amalitech.communityboard.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class JwtService {

    private final SecretKey secretKey;
    private final String issuer;
    private final JwtParser jwtParser;

    private final Map<String, Claims> claimsCache = new ConcurrentHashMap<>();
    private final Map<String, TokenValidationResult> validationCache = new ConcurrentHashMap<>();

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${app.custom.jwt-refresh.expiration-ms}")
    private long jwtRefreshExpirationMs;


    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.issuer}") String issuer
    ) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.issuer = issuer;
        this.jwtParser = Jwts.parser()
                .verifyWith(secretKey)
                .build();
    }

    public Map<String, String> generateToken(User user) {
        Map<String, String> tokens = new HashMap<>();
        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);
        tokens.put("access", accessToken);
        tokens.put("refresh", refreshToken);
        return tokens;
    }

    private String generateAccessToken(User user) {
        List<String> roles = List.of("ROLE_" + user.getRole().name());

        return Jwts.builder()
                .subject(user.getEmail())
                .issuer(issuer)
                .claim("type", "access")
                .claim("roles", roles)
                .claim("name", user.getUsername())
                .claim("userId",user.getId())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private String generateRefreshToken(User user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .issuer(issuer)
                .claim("userId",user.getId())
                .claim("type", "refresh")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtRefreshExpirationMs))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }



    public TokenValidationResult validateAndExtract(String token) {
        try {
            Date now = new Date();
            return validationCache.compute(token, (key, existing) -> {
                if (existing != null && !existing.isExpired()) {
                    return existing;
                }

                Claims claims = getClaimsFromToken(token);
                Long userId = ((Number) claims.get("userId")).longValue();
                boolean valid = claims.getExpiration().after(now);

                return TokenValidationResult.builder()
                        .subject(claims.getSubject())
                        .roles(extractRolesFromClaims(claims))
                        .isValid(valid)
                        .userId(userId)
                        .expiration(claims.getExpiration())
                        .claims(claims)
                        .cachedAt(now)
                        .build();
            });
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return TokenValidationResult.invalid();
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> extractRolesFromClaims(Claims claims) {
        List<String> roles = (List<String>) claims.get("roles");
        return roles != null ? roles : List.of();
    }
    @Scheduled(fixedDelay = 300000) // Every 5 minutes
    public void cleanupCaches() {
        Date now = new Date();

        claimsCache.entrySet().removeIf(entry ->
                entry.getValue().getExpiration().before(now));

        validationCache.entrySet().removeIf(entry ->
                entry.getValue().isExpired());

        log.debug("Cleaned up JWT caches. Claims cache size: {}, Validation cache size: {}",
                claimsCache.size(), validationCache.size());
    }

    public String extractSubject(String token) {
        TokenValidationResult result = validateAndExtract(token);
        return result.isValid() ? result.getSubject() : null;
    }

    private Claims getClaimsFromToken(String token) {
        Date now = new Date();
        return claimsCache.compute(token, (key, existing) -> {
            if (existing != null && existing.getExpiration().after(now)) {
                return existing;
            }

            Claims parsed = jwtParser.parseSignedClaims(token).getPayload();

            return parsed.getExpiration().after(now) ? parsed : null;
        });
    }
}
