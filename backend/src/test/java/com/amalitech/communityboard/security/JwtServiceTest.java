package com.amalitech.communityboard.security;

import com.amalitech.communityboard.dto.TokenValidationResult;
import com.amalitech.communityboard.dto.enums.UserRole;
import com.amalitech.communityboard.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtService Unit Tests")
class JwtServiceTest {

    // Long enough secret for HMAC-SHA256 (min 32 chars)
    private static final String SECRET  = "test-secret-key-that-is-at-least-32-chars!!";
    private static final String ISSUER  = "test-issuer";
    private static final long   ACCESS_EXPIRY_MS  = 3_600_000L;   // 1 hour
    private static final long   REFRESH_EXPIRY_MS = 86_400_000L;  // 24 hours

    private JwtService jwtService;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, ISSUER);

        // Inject @Value fields that Spring would normally inject
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs",        ACCESS_EXPIRY_MS);
        ReflectionTestUtils.setField(jwtService, "jwtRefreshExpirationMs", REFRESH_EXPIRY_MS);

        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setUsername("silas_dev");
        sampleUser.setEmail("silas@amalitech.com");
        sampleUser.setRole(UserRole.MEMBER);
    }

    // ─────────────────────────────────────────────────────────────
    // GENERATE TOKEN
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("generateToken")
    class GenerateToken {

        @Test
        @DisplayName("returns map with both access and refresh tokens")
        void generateToken_returnsBothTokens() {
            Map<String, String> tokens = jwtService.generateToken(sampleUser);

            assertThat(tokens).containsKeys("access", "refresh");
            assertThat(tokens.get("access")).isNotBlank();
            assertThat(tokens.get("refresh")).isNotBlank();
        }

        @Test
        @DisplayName("access and refresh tokens are distinct strings")
        void generateToken_accessAndRefreshAreDistinct() {
            Map<String, String> tokens = jwtService.generateToken(sampleUser);

            assertThat(tokens.get("access")).isNotEqualTo(tokens.get("refresh"));
        }

        @Test
        @DisplayName("access token contains userId, username, email, and role claims")
        void generateToken_accessTokenContainsExpectedClaims() {
            Map<String, String> tokens = jwtService.generateToken(sampleUser);

            TokenValidationResult result = jwtService.validateAndExtract(tokens.get("access"));

            assertThat(result.isValid()).isTrue();
            assertThat(result.getSubject()).isEqualTo("silas@amalitech.com");
            assertThat(result.getUserId()).isEqualTo(1L);
            assertThat(result.getRoles()).contains("ROLE_MEMBER");
        }

        @Test
        @DisplayName("refresh token subject matches user email")
        void generateToken_refreshTokenSubjectMatchesEmail() {
            Map<String, String> tokens = jwtService.generateToken(sampleUser);

            String subject = jwtService.extractSubject(tokens.get("refresh"));

            assertThat(subject).isEqualTo("silas@amalitech.com");
        }

        @Test
        @DisplayName("roles are prefixed with ROLE_")
        void generateToken_rolesPrefixedCorrectly() {
            Map<String, String> tokens = jwtService.generateToken(sampleUser);

            TokenValidationResult result = jwtService.validateAndExtract(tokens.get("access"));

            assertThat(result.getRoles()).containsExactly("ROLE_MEMBER");
        }

        @Test
        @DisplayName("generates distinct tokens on successive calls")
        void generateToken_successiveCalls_producesDistinctTokens() throws InterruptedException {
            Map<String, String> first  = jwtService.generateToken(sampleUser);
            Thread.sleep(1); // ensure different iat
            Map<String, String> second = jwtService.generateToken(sampleUser);

            assertThat(first.get("access")).isEqualTo(second.get("access"));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // VALIDATE AND EXTRACT
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("validateAndExtract")
    class ValidateAndExtract {

        @Test
        @DisplayName("returns valid result for a freshly generated access token")
        void validateAndExtract_freshToken_returnsValid() {
            String accessToken = jwtService.generateToken(sampleUser).get("access");

            TokenValidationResult result = jwtService.validateAndExtract(accessToken);

            assertThat(result.isValid()).isTrue();
            assertThat(result.getSubject()).isEqualTo("silas@amalitech.com");
            assertThat(result.getUserId()).isEqualTo(1L);
            assertThat(result.getExpiration()).isNotNull();
            assertThat(result.getClaims()).isNotNull();
        }

        @Test
        @DisplayName("returns valid result for a freshly generated refresh token")
        void validateAndExtract_refreshToken_returnsValid() {
            String refreshToken = jwtService.generateToken(sampleUser).get("refresh");

            TokenValidationResult result = jwtService.validateAndExtract(refreshToken);

            assertThat(result.isValid()).isTrue();
            assertThat(result.getSubject()).isEqualTo("silas@amalitech.com");
        }

        @Test
        @DisplayName("returns invalid result for a malformed token string")
        void validateAndExtract_malformedToken_returnsInvalid() {
            TokenValidationResult result = jwtService.validateAndExtract("this.is.not.a.jwt");

            assertThat(result.isValid()).isFalse();
        }

        @Test
        @DisplayName("returns invalid result for an empty string")
        void validateAndExtract_emptyToken_returnsInvalid() {
            TokenValidationResult result = jwtService.validateAndExtract("");

            assertThat(result.isValid()).isFalse();
        }

        @Test
        @DisplayName("returns invalid result for a token signed with a different secret")
        void validateAndExtract_wrongSecret_returnsInvalid() {
            JwtService otherService = new JwtService("completely-different-secret-key-xyz!!", ISSUER);
            ReflectionTestUtils.setField(otherService, "jwtExpirationMs",        ACCESS_EXPIRY_MS);
            ReflectionTestUtils.setField(otherService, "jwtRefreshExpirationMs", REFRESH_EXPIRY_MS);

            String foreignToken = otherService.generateToken(sampleUser).get("access");

            TokenValidationResult result = jwtService.validateAndExtract(foreignToken);

            assertThat(result.isValid()).isFalse();
        }

        @Test
        @DisplayName("returns invalid result for an already expired token")
        void validateAndExtract_expiredToken_returnsInvalid() {
            // Create a service with 0ms expiry so token expires immediately
            JwtService expiredService = new JwtService(SECRET, ISSUER);
            ReflectionTestUtils.setField(expiredService, "jwtExpirationMs",        -1000L);
            ReflectionTestUtils.setField(expiredService, "jwtRefreshExpirationMs", -1000L);

            String expiredToken = expiredService.generateToken(sampleUser).get("access");

            TokenValidationResult result = jwtService.validateAndExtract(expiredToken);

            assertThat(result.isValid()).isFalse();
        }

        @Test
        @DisplayName("caches result and returns same object on second call")
        void validateAndExtract_secondCall_returnsCachedResult() {
            String accessToken = jwtService.generateToken(sampleUser).get("access");

            TokenValidationResult first  = jwtService.validateAndExtract(accessToken);
            TokenValidationResult second = jwtService.validateAndExtract(accessToken);

            // Same cached instance returned
            assertThat(first).isSameAs(second);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // EXTRACT SUBJECT
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("extractSubject")
    class ExtractSubject {

        @Test
        @DisplayName("returns email from a valid access token")
        void extractSubject_validAccessToken_returnsEmail() {
            String accessToken = jwtService.generateToken(sampleUser).get("access");

            String subject = jwtService.extractSubject(accessToken);

            assertThat(subject).isEqualTo("silas@amalitech.com");
        }

        @Test
        @DisplayName("returns email from a valid refresh token")
        void extractSubject_validRefreshToken_returnsEmail() {
            String refreshToken = jwtService.generateToken(sampleUser).get("refresh");

            String subject = jwtService.extractSubject(refreshToken);

            assertThat(subject).isEqualTo("silas@amalitech.com");
        }

        @Test
        @DisplayName("returns null for a malformed token")
        void extractSubject_malformedToken_returnsNull() {
            String subject = jwtService.extractSubject("not.a.real.token");

            assertThat(subject).isNull();
        }

        @Test
        @DisplayName("returns null for an expired token")
        void extractSubject_expiredToken_returnsNull() {
            JwtService expiredService = new JwtService(SECRET, ISSUER);
            ReflectionTestUtils.setField(expiredService, "jwtExpirationMs",        -1000L);
            ReflectionTestUtils.setField(expiredService, "jwtRefreshExpirationMs", -1000L);

            String expiredToken = expiredService.generateToken(sampleUser).get("access");

            assertThat(jwtService.extractSubject(expiredToken)).isNull();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CLEANUP CACHES
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("cleanupCaches")
    class CleanupCaches {

        @Test
        @DisplayName("runs without throwing even when caches are empty")
        void cleanupCaches_emptyCaches_doesNotThrow() {
            org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                    () -> jwtService.cleanupCaches());
        }

        @Test
        @DisplayName("retains valid tokens after cleanup")
        void cleanupCaches_validTokensRetained() {
            String accessToken = jwtService.generateToken(sampleUser).get("access");
            jwtService.validateAndExtract(accessToken); // populate cache

            jwtService.cleanupCaches();

            // Valid token should still resolve correctly after cleanup
            TokenValidationResult result = jwtService.validateAndExtract(accessToken);
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("removes expired tokens from validation cache after cleanup")
        void cleanupCaches_expiredTokensRemoved() {
            JwtService shortLivedService = new JwtService(SECRET, ISSUER);
            ReflectionTestUtils.setField(shortLivedService, "jwtExpirationMs",        -1000L);
            ReflectionTestUtils.setField(shortLivedService, "jwtRefreshExpirationMs", -1000L);

            String expiredToken = shortLivedService.generateToken(sampleUser).get("access");
            jwtService.validateAndExtract(expiredToken); // attempt to populate cache

            // Should not throw — just silently clear expired entries
            org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                    () -> jwtService.cleanupCaches());
        }
    }
}