package com.communityboard.tests.base;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * AuthHelper — encapsulates all authentication concerns for the test suite.
 *
 * <p>
 * SOLID notes:
 * <ul>
 * <li><b>SRP</b>: This class is exclusively responsible for obtaining and
 * refreshing bearer tokens. Test logic does not belong here.</li>
 * <li><b>DIP</b>: Accepts a {@link RequestSpecification} so callers can inject
 * any base spec (production vs. staging), keeping this class reusable.</li>
 * </ul>
 */
public class AuthHelper {

    private static final Logger log = LoggerFactory.getLogger(AuthHelper.class);

    /**
     * JSON field name in the login response that holds the bearer token.
     * Adjust this to match the actual API response shape (e.g. "token", "jwt").
     */
    private static final String TOKEN_FIELD = "accessToken";

    /**
     * JSON field name for the refresh token (used by {@link #refreshToken}).
     * Adjust to match the actual API contract.
     */
    private static final String REFRESH_TOKEN_FIELD = "refreshToken";

    /**
     * Shared RequestSpecification injected from BaseApiTest (base URI, headers,
     * etc.)
     */
    private final RequestSpecification baseSpec;

    /** In-memory cache of the last access token to avoid repeated login calls. */
    private String cachedAccessToken;

    /**
     * In-memory cache of the last refresh token.
     * Promoted to a field when POST /api/v1/users/refresh is fully implemented.
     * 
     * @see #refreshToken()
     */
    @SuppressWarnings("FieldCanBeLocal")
    private String lastRefreshToken;

    // ─── Constructor ─────────────────────────────────────────────────────────

    /**
     * @param baseSpec a pre-configured RequestSpecification (base URI + headers).
     *                 Injected by {@link BaseApiTest} so this class stays testable.
     */
    public AuthHelper(RequestSpecification baseSpec) {
        this.baseSpec = baseSpec;
    }

    // ─── Public API ──────────────────────────────────────────────────────────

    /**
     * Authenticates with the given credentials and returns the raw JWT access
     * token.
     *
     * <p>
     * The step is annotated with {@code @Step} so Allure captures it as a
     * named action in the test report.
     *
     * @param email    the user's e-mail address
     * @param password the user's password (plain text — HTTPS only!)
     * @return bearer access token string
     * @throws AssertionError if login returns a non-200 status code
     */
    @Step("Login as {email}")
    public String loginAsUser(String email, String password) {
        log.info("Requesting JWT for user: {}", email);

        // Build the login request body as an inline map — Jackson serialises it
        // automatically
        Map<String, String> credentials = Map.of(
                "email", email,
                "password", password);

        Response response = given()
                .spec(baseSpec) // inherit base URI, content-type, Allure filter
                .body(credentials) // Jackson converts map → JSON
                .when()
                .post(TestConfig.LOGIN_ENDPOINT)
                .then()
                .statusCode(200) // assert HTTP 200 before extracting
                .extract()
                .response();

        // Extract access token; store refresh token for later when the refresh endpoint
        // is ready
        cachedAccessToken = response.jsonPath().getString(TOKEN_FIELD);
        lastRefreshToken = response.jsonPath().getString(REFRESH_TOKEN_FIELD);

        log.info("Successfully obtained access token (truncated): {}...",
                cachedAccessToken != null && cachedAccessToken.length() > 20
                        ? cachedAccessToken.substring(0, 20)
                        : cachedAccessToken);

        return cachedAccessToken;
    }

    /**
     * Convenience method: logs in with the pre-configured default test-user
     * credentials from {@link TestConfig}.
     *
     * @return bearer access token string
     */
    @Step("Login as default test user ({TestConfig.TEST_USER_EMAIL})")
    public String loginAsDefaultTestUser() {
        return loginAsUser(TestConfig.TEST_USER_EMAIL, TestConfig.TEST_USER_PASSWORD);
    }

    /**
     * Exchanges the cached refresh token for a fresh access token.
     *
     * <p>
     * This is a <em>stub</em> — implement when the API exposes a refresh endpoint.
     * Typically: {@code POST /api/v1/users/refresh} with {"refreshToken": "..."}.
     *
     * @return new access token, or the cached one if refresh is not yet implemented
     */
    @Step("Refresh access token")
    public String refreshToken() {
        // ── TODO: Replace this stub with the real refresh call once the endpoint
        // exists ──
        // Example implementation:
        //
        // Map<String, String> body = Map.of(REFRESH_TOKEN_FIELD, cachedRefreshToken);
        // cachedAccessToken = given()
        // .spec(baseSpec)
        // .body(body)
        // .when()
        // .post("/api/v1/users/refresh")
        // .then()
        // .statusCode(200)
        // .extract()
        // .jsonPath()
        // .getString(TOKEN_FIELD);
        // return cachedAccessToken;

        log.warn("refreshToken() is a stub — returning cached access token.");
        return cachedAccessToken;
    }

    // ─── Accessors ───────────────────────────────────────────────────────────

    /**
     * Returns the most recently obtained access token without performing a new
     * login.
     * Returns {@code null} if {@link #loginAsUser} has not been called yet.
     */
    public String getCachedAccessToken() {
        return cachedAccessToken;
    }
}
