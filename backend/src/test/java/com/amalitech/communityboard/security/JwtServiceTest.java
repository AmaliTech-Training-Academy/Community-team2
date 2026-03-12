// src/test/java/com/amalitech/communityboard/security/JwtServiceTest.java
package com.amalitech.communityboard.security;

import com.amalitech.communityboard.dto.TokenValidationResult;
import com.amalitech.communityboard.dto.enums.AccountProvider;
import com.amalitech.communityboard.dto.enums.UserRole;
import com.amalitech.communityboard.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;

    private final String secret = "my-super-secret-key-that-is-at-least-32-bytes-long-for-hs256";
    private final String issuer = "test-issuer";
    private final long jwtExpirationMs = 3600000; // 1 hour
    private final long jwtRefreshExpirationMs = 86400000; // 24 hours

    private User testUser;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(secret, issuer);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", jwtExpirationMs);
        ReflectionTestUtils.setField(jwtService, "jwtRefreshExpirationMs", jwtRefreshExpirationMs);

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("testemail@gmail.com")
                .password("password123")
                .role(UserRole.MEMBER)
                .provider(AccountProvider.LOCAL)
                .build();

        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    @Nested
    @DisplayName("Token Generation Tests")
    class TokenGenerationTests {

        @Test
        @DisplayName("Should generate access and refresh tokens")
        void generateToken_Success() {
            // Act
            Map<String, String> tokens = jwtService.generateToken(testUser);

            // Assert
            assertThat(tokens).containsKeys("access", "refresh");
            assertThat(tokens.get("access")).isNotBlank();
            assertThat(tokens.get("refresh")).isNotBlank();
        }

        @Test
        @DisplayName("Access token should contain correct claims")
        void accessToken_ContainsCorrectClaims() {
            // Act
            Map<String, String> tokens = jwtService.generateToken(testUser);
            String accessToken = tokens.get("access");

            // Parse and verify claims
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload();

            // Assert
            assertThat(claims.getSubject()).isEqualTo(testUser.getEmail());
            assertThat(claims.getIssuer()).isEqualTo(issuer);
            assertThat(claims.get("type")).isEqualTo("access");
            assertThat(claims.get("roles")).asList().contains("ROLE_MEMBER");
            assertThat(claims.get("name")).isEqualTo(testUser.getUsername());
            assertThat(claims.get("userId")).isEqualTo(1);
            assertThat(claims.getExpiration()).isAfter(new Date());
        }

        @Test
        @DisplayName("Refresh token should contain correct claims")
        void refreshToken_ContainsCorrectClaims() {
            // Act
            Map<String, String> tokens = jwtService.generateToken(testUser);
            String refreshToken = tokens.get("refresh");

            // Parse and verify claims
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(refreshToken)
                    .getPayload();

            // Assert
            assertThat(claims.getSubject()).isEqualTo(testUser.getEmail());
            assertThat(claims.getIssuer()).isEqualTo(issuer);
            assertThat(claims.get("type")).isEqualTo("refresh");
            assertThat(claims.get("userId")).isEqualTo(1);
            assertThat(claims.getExpiration()).isAfter(new Date());
            assertThat(claims.get("roles")).isNull(); // Refresh token shouldn't have roles
        }
    }

    @Nested
    @DisplayName("Token Validation Tests")
    class TokenValidationTests {

        @Test
        @DisplayName("Should validate valid access token")
        void validateAndExtract_ValidAccessToken_Success() {
            // Arrange
            Map<String, String> tokens = jwtService.generateToken(testUser);
            String accessToken = tokens.get("access");

            // Act
            TokenValidationResult result = jwtService.validateAndExtract(accessToken);

            // Assert
            assertThat(result.isValid()).isTrue();
            assertThat(result.getSubject()).isEqualTo(testUser.getEmail());
            assertThat(result.getRoles()).contains("ROLE_MEMBER");
            assertThat(result.getUserId()).isEqualTo(testUser.getId());
            assertThat(result.getExpiration()).isAfter(new Date());
            assertThat(result.getClaims()).isNotNull();
        }

        @Test
        @DisplayName("Should validate valid refresh token")
        void validateAndExtract_ValidRefreshToken_Success() {
            // Arrange
            Map<String, String> tokens = jwtService.generateToken(testUser);
            String refreshToken = tokens.get("refresh");

            // Act
            TokenValidationResult result = jwtService.validateAndExtract(refreshToken);

            // Assert
            assertThat(result.isValid()).isTrue();
            assertThat(result.getSubject()).isEqualTo(testUser.getEmail());
            assertThat(result.getRoles()).isEmpty(); // Refresh token has no roles
            assertThat(result.getUserId()).isEqualTo(testUser.getId());
        }

        @Test
        @DisplayName("Should return invalid for expired token")
        void validateAndExtract_ExpiredToken_ReturnsInvalid() throws Exception {
            // Arrange - Create token with very short expiration
            ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", -1000); // Already expired
            Map<String, String> tokens = jwtService.generateToken(testUser);
            String expiredToken = tokens.get("access");

            // Act
            TokenValidationResult result = jwtService.validateAndExtract(expiredToken);

            // Assert
            assertThat(result.isValid()).isFalse();
            assertThat(result.getSubject()).isNull();

        }

        @Test
        @DisplayName("Should return invalid for malformed token")
        void validateAndExtract_MalformedToken_ReturnsInvalid() {
            // Act
            TokenValidationResult result = jwtService.validateAndExtract("malformed.token.string");

            // Assert
            assertThat(result.isValid()).isFalse();
        }

        @Test
        @DisplayName("Should return invalid for token with wrong signature")
        void validateAndExtract_WrongSignature_ReturnsInvalid() {
            // Arrange - Create token with different secret
            String differentSecret = "different-secret-key-that-is-different-from-original";
            byte[] differentKeyBytes = differentSecret.getBytes(StandardCharsets.UTF_8);
            SecretKey differentKey = Keys.hmacShaKeyFor(differentKeyBytes);

            String tamperedToken = Jwts.builder()
                    .subject(testUser.getEmail())
                    .signWith(differentKey)
                    .compact();

            // Act
            TokenValidationResult result = jwtService.validateAndExtract(tamperedToken);

            // Assert
            assertThat(result.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("Cache Tests")
    class CacheTests {

        @Test
        @DisplayName("Should cache validation results")
        void validateAndExtract_CachesResults() {
            // Arrange
            Map<String, String> tokens = jwtService.generateToken(testUser);
            String accessToken = tokens.get("access");

            // Act - First call
            TokenValidationResult firstResult = jwtService.validateAndExtract(accessToken);

            // Second call - should use cache
            TokenValidationResult secondResult = jwtService.validateAndExtract(accessToken);

            // Assert
            assertThat(secondResult).isSameAs(firstResult); // Same cached instance
        }

        @Test
        @DisplayName("Should cleanup expired cache entries")
        @org.junit.jupiter.api.Disabled("Flaky test - timing dependent")
        void cleanupCaches_RemovesExpiredEntries() throws Exception {
            ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 100); // 100ms expiration
            Map<String, String> tokens = jwtService.generateToken(testUser);
            String accessToken = tokens.get("access");

            TokenValidationResult result = jwtService.validateAndExtract(accessToken);

            await().atMost(200, TimeUnit.MILLISECONDS).until(() -> {
                jwtService.cleanupCaches();
                return true;
            });

            TokenValidationResult newResult = jwtService.validateAndExtract(accessToken);
            assertThat(newResult.isValid()).isFalse();

        }
    }

    @Nested
    @DisplayName("Extract Subject Tests")
    class ExtractSubjectTests {

        @Test
        @DisplayName("Should extract subject from valid token")
        void extractSubject_ValidToken_ReturnsEmail() {
            // Arrange
            Map<String, String> tokens = jwtService.generateToken(testUser);
            String accessToken = tokens.get("access");

            // Act
            String subject = jwtService.extractSubject(accessToken);

            // Assert
            assertThat(subject).isEqualTo(testUser.getEmail());
        }

        @Test
        @DisplayName("Should return null for invalid token")
        void extractSubject_InvalidToken_ReturnsNull() {
            // Act
            String subject = jwtService.extractSubject("invalid.token");

            // Assert
            assertThat(subject).isNull();
        }
    }
}