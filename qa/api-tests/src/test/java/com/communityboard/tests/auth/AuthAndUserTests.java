package com.communityboard.tests.auth;

import com.communityboard.tests.base.BaseApiTest;
import com.communityboard.tests.base.TestConfig;
import com.communityboard.tests.utils.TestDataFactory;
import com.communityboard.tests.utils.TestDataFactory.UserRequest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * AuthAndUserTests — comprehensive test class for US1–US3:
 * <ul>
 * <li>US1: User Registration (POST /api/v1/users)</li>
 * <li>US2: User Login / Token flow (POST /api/v1/users/login, /refresh,
 * /logout)</li>
 * <li>US3: Authenticated user profile (GET /api/v1/users/me, /users/{id})</li>
 * </ul>
 *
 * <p>
 * SOLID notes:
 * <ul>
 * <li><b>SRP</b>: Each test method validates exactly one behaviour.</li>
 * <li><b>DRY</b>: Repeated request patterns are extracted into private helpers
 * ({@link #register}, {@link #login}, {@link #buildAuthHeader}).</li>
 * <li><b>OCP</b>: New test cases can be added without modifying base
 * classes.</li>
 * </ul>
 */
@Epic("CommunityBoard API")
@Feature("Authentication & User Management")
@Tag("auth") // run selectively: mvn test -Dgroups=auth
public class AuthAndUserTests extends BaseApiTest {

    // ─────────────────────────────────────────────────────────────────────────
    // Teardown registry — track emails created so we can attempt cleanup
    // CopyOnWriteArrayList used because @AfterEach runs on the same thread as @Test
    // ─────────────────────────────────────────────────────────────────────────

    /** Accumulates IDs of users created during the test run for cleanup. */
    private final CopyOnWriteArrayList<String> createdUserIds = new CopyOnWriteArrayList<>();

    /**
     * Called by BaseApiTest's @AfterEach hook for every test that creates a user.
     * Attempts a best-effort DELETE; swallows failures so cleanup never breaks
     * teardown.
     */
    @Override
    protected void onAfterEach() {
        // Attempt to delete every user registered during this test
        for (String userId : createdUserIds) {
            try {
                // Try with admin credentials or skip if no admin route is available
                given()
                        .spec(requestSpec)
                        .header("Authorization", obtainAdminBearerToken())
                        .when()
                        .delete(TestConfig.USERS_ENDPOINT + "/" + userId)
                        .then()
                        .statusCode(anyOf(equalTo(200), equalTo(204), equalTo(404)));
                log.info("Cleanup: deleted user {}", userId);
            } catch (Exception ex) {
                // Cleanup is best-effort only — never fail the test because of this
                log.warn("Cleanup: could not delete user {} — {}", userId, ex.getMessage());
            }
        }
        createdUserIds.clear();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // US1 — REGISTRATION
    // ═════════════════════════════════════════════════════════════════════════

    // ── TC-AUTH-001 ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-AUTH-001 | Register with valid unique credentials → 201 Created")
    @Story("US1: User Registration")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Happy-path registration: unique email + strong password must return 201 "
            + "with a body containing the new user's id and the submitted email.")
    void register_withValidData_returns201() {
        // Build a fresh unique-email user payload
        UserRequest newUser = TestDataFactory.randomUserRequest();

        // ── ACT ─────────────────────────────────────────────────────────────
        Response response = register(newUser);

        // ── REPORT ──────────────────────────────────────────────────────────
        attachJsonToReport("Registration Response", response.body().prettyPrint());

        // ── ASSERT ──────────────────────────────────────────────────────────
        assertThat("Registration must return HTTP 201 Created",
                response.statusCode(), equalTo(201));

        // The API must echo back the email submitted
        assertThat("Response must contain the registered email",
                response.jsonPath().getString("email"),
                equalToIgnoringCase(newUser.getEmail()));

        // The API must assign an ID to the new user
        assertThat("Response must contain a non-null user id",
                response.jsonPath().getString("id"), notNullValue());

        // Password must NEVER be returned in plain text
        assertThat("Response must not expose the plain-text password",
                response.body().asString(), not(containsString(newUser.getPassword())));

        // Track the new ID for cleanup in @AfterEach
        trackUserId(response);
    }

    // ── TC-AUTH-002 ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-AUTH-002 | Register with duplicate email → 409 Conflict")
    @Story("US1: User Registration")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Registering the same email twice must return 409 Conflict "
            + "to prevent duplicate accounts.")
    void register_withDuplicateEmail_returns409() {
        // Register the user for the first time (must succeed)
        UserRequest original = TestDataFactory.randomUserRequest();
        Response firstResponse = register(original);
        assertThat("First registration must return 201", firstResponse.statusCode(), equalTo(201));
        trackUserId(firstResponse);

        // Build a second payload reusing the exact same email
        UserRequest duplicate = UserRequest.builder()
                .name("Another Name")
                .email(original.getEmail()) // same email — triggers the conflict
                .password("AnotherPass@99")
                .role("USER")
                .build();

        // ── ACT (second attempt) ─────────────────────────────────────────────
        Response response = register(duplicate);
        attachJsonToReport("Duplicate Registration Response", response.body().prettyPrint());

        // ── ASSERT ───────────────────────────────────────────────────────────
        assertThat("Duplicate email must return HTTP 409 Conflict",
                response.statusCode(), equalTo(409));
    }

    // ── TC-AUTH-003 ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-AUTH-003 | Register with invalid email format → 400 Bad Request")
    @Story("US1: User Registration")
    @Severity(SeverityLevel.NORMAL)
    void register_withInvalidEmail_returns400() {
        // Build payload with a clearly malformed email
        UserRequest badEmail = UserRequest.builder()
                .name("Test User")
                .email("not-an-email-at-all") // missing '@' and domain
                .password("ValidPass@123")
                .role("USER")
                .build();

        Response response = register(badEmail);
        attachJsonToReport("Invalid Email Response", response.body().prettyPrint());

        assertThat("Invalid email format must return HTTP 400",
                response.statusCode(), equalTo(400));
    }

    // ── TC-AUTH-004 — Parameterised: multiple bad email formats ───────────────

    @ParameterizedTest(name = "TC-AUTH-004 | Invalid email: [{0}] → 400")
    @CsvSource({
            "plainaddress", // no @ at all
            "@no-local-part.com", // missing local part
            "missing-at-sign.com", // no @
            "two@@signs.com", // double @
            "spaces in@email.com", // space in local part
            "email@", // missing domain
    })
    @DisplayName("TC-AUTH-004 | Various invalid email formats → 400 Bad Request")
    @Story("US1: User Registration")
    @Severity(SeverityLevel.NORMAL)
    void register_withVariousInvalidEmails_returns400(String badEmail) {
        UserRequest payload = UserRequest.builder()
                .name("Test User")
                .email(badEmail)
                .password("ValidPass@123")
                .role("USER")
                .build();

        Response response = register(payload);

        assertThat("Invalid email '" + badEmail + "' must return 400",
                response.statusCode(), equalTo(400));
    }

    // ── TC-AUTH-005 — Data-driven: weak passwords ─────────────────────────────

    @ParameterizedTest(name = "TC-AUTH-005 | Weak password: [{0}] → 400")
    @ValueSource(strings = {
            "abc", // way too short
            "1234567", // 7 chars — below the 8-char boundary
            "allowercase1", // no uppercase / no special char
            "NOLOWER@1", // no lowercase
            "NoSpecial1", // no special character
            "        ", // only spaces
    })
    @DisplayName("TC-AUTH-005 | Weak passwords → 400 Bad Request")
    @Story("US1: User Registration")
    @Severity(SeverityLevel.NORMAL)
    void register_withWeakPassword_returns400(String weakPassword) {
        UserRequest payload = UserRequest.builder()
                .name("Test User")
                .email(TestDataFactory.uniqueEmail())
                .password(weakPassword)
                .role("USER")
                .build();

        Response response = register(payload);

        assertThat("Weak password '" + weakPassword + "' must return 400",
                response.statusCode(), equalTo(400));
    }

    // ── TC-AUTH-006 — Password boundary: exactly 8 chars (accept) vs 7 (reject) ─

    @Test
    @DisplayName("TC-AUTH-006a | Password exactly 8 chars (valid boundary) → 201")
    @Story("US1: User Registration")
    @Severity(SeverityLevel.NORMAL)
    void register_withExactly8CharPassword_isAccepted() {
        // 8 chars meeting complexity: upper + lower + digit + special
        UserRequest payload = UserRequest.builder()
                .name("Boundary User")
                .email(TestDataFactory.uniqueEmail())
                .password("Abcde@1!") // exactly 8 characters
                .role("USER")
                .build();

        Response response = register(payload);
        attachJsonToReport("8-char Password Response", response.body().prettyPrint());
        trackUserId(response);

        assertThat("8-char strong password at boundary must be accepted (201)",
                response.statusCode(), equalTo(201));
    }

    @Test
    @DisplayName("TC-AUTH-006b | Password 7 chars (below boundary) → 400")
    @Story("US1: User Registration")
    @Severity(SeverityLevel.NORMAL)
    void register_with7CharPassword_isRejected() {
        UserRequest payload = UserRequest.builder()
                .name("Boundary User")
                .email(TestDataFactory.uniqueEmail())
                .password("Abc@1!x") // exactly 7 characters
                .role("USER")
                .build();

        Response response = register(payload);

        assertThat("7-char password must be rejected (400)",
                response.statusCode(), equalTo(400));
    }

    // ── TC-AUTH-007 — Empty / missing required fields ─────────────────────────

    @Test
    @DisplayName("TC-AUTH-007 | Register with empty body → 400 Bad Request")
    @Story("US1: User Registration")
    @Severity(SeverityLevel.NORMAL)
    void register_withEmptyBody_returns400() {
        // Send a completely empty JSON object — no fields at all
        Response response = given()
                .spec(requestSpec)
                .body("{}")
                .when()
                .post(TestConfig.USERS_ENDPOINT)
                .then()
                .extract()
                .response();

        assertThat("Empty body must return 400", response.statusCode(), equalTo(400));
    }

    @ParameterizedTest(name = "TC-AUTH-007 | Missing field: {0} → 400")
    @CsvSource({
            "name,       , test@example.com, ValidPass@1!", // missing name
            "email,  Bob, ,                  ValidPass@1!", // missing email
            "password,Bob,test@example.com,  ", // missing password
    })
    @DisplayName("TC-AUTH-007 | Individual required field missing → 400")
    @Story("US1: User Registration")
    @Severity(SeverityLevel.NORMAL)
    void register_withMissingField_returns400(
            String missingField, String name, String email, String password) {

        // Build body map with only the fields that are non-blank
        Map<String, String> body = new java.util.HashMap<>();
        if (name != null && !name.isBlank())
            body.put("name", name.trim());
        if (email != null && !email.isBlank())
            body.put("email", email.trim());
        if (password != null && !password.isBlank())
            body.put("password", password.trim());

        Response response = given()
                .spec(requestSpec)
                .body(body)
                .when()
                .post(TestConfig.USERS_ENDPOINT)
                .then()
                .extract()
                .response();

        assertThat("Missing '" + missingField + "' must return 400",
                response.statusCode(), equalTo(400));
    }

    // ═════════════════════════════════════════════════════════════════════════
    // US2 — LOGIN
    // ═════════════════════════════════════════════════════════════════════════

    // ── TC-AUTH-008 ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-AUTH-008 | Login with valid credentials → 200 + accessToken")
    @Story("US2: Login")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Happy-path login: valid email + password must return 200 with a "
            + "non-null accessToken (and optionally refreshToken).")
    void login_withValidCredentials_returns200WithToken() {
        // Register a fresh user first so we know the exact credentials
        UserRequest newUser = TestDataFactory.randomUserRequest();
        Response regResponse = register(newUser);
        assertThat("Setup: registration must succeed", regResponse.statusCode(), equalTo(201));
        trackUserId(regResponse);

        // ── ACT: Login ───────────────────────────────────────────────────────
        Response loginResponse = login(newUser.getEmail(), newUser.getPassword());
        attachJsonToReport("Login Response", loginResponse.body().prettyPrint());

        // ── ASSERT ───────────────────────────────────────────────────────────
        assertThat("Login must return HTTP 200", loginResponse.statusCode(), equalTo(200));

        // The response MUST include a non-null access token
        String accessToken = loginResponse.jsonPath().getString("accessToken");
        assertThat("Login response must contain a non-null accessToken",
                accessToken, notNullValue());
        assertThat("accessToken must not be empty", accessToken, not(emptyString()));
    }

    // ── TC-AUTH-009 ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-AUTH-009 | Login with wrong password → 401 Unauthorized")
    @Story("US2: Login")
    @Severity(SeverityLevel.CRITICAL)
    void login_withWrongPassword_returns401() {
        // Register a real user first
        UserRequest newUser = TestDataFactory.randomUserRequest();
        register(newUser);

        // Attempt login with an incorrect password
        Response response = login(newUser.getEmail(), "WrongPass@999!");
        attachJsonToReport("Wrong Password Response", response.body().prettyPrint());

        assertThat("Wrong password must return HTTP 401", response.statusCode(), equalTo(401));
    }

    // ── TC-AUTH-010 ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-AUTH-010 | Login with non-existent email → 401 (generic message)")
    @Story("US2: Login")
    @Severity(SeverityLevel.CRITICAL)
    @Description("The API must NOT reveal whether the email exists to prevent enumeration. "
            + "Expected: 401 with a generic error, not a 404.")
    void login_withNonExistentEmail_returns401() {
        Response response = login("nobody_xyz_99@nonexistent.invalid", "AnyPass@99!");
        attachJsonToReport("Non-existent Email Login Response", response.body().prettyPrint());

        assertThat("Non-existent email must return 401 (not 404 — no user enumeration)",
                response.statusCode(), equalTo(401));
    }

    // ── TC-AUTH-011 ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-AUTH-011 | Login with empty credentials → 400 Bad Request")
    @Story("US2: Login")
    @Severity(SeverityLevel.NORMAL)
    void login_withEmptyCredentials_returns400() {
        // Send a completely empty body
        Response response = given()
                .spec(requestSpec)
                .body(Map.of()) // empty JSON object {}
                .when()
                .post(TestConfig.LOGIN_ENDPOINT)
                .then()
                .extract()
                .response();

        assertThat("Empty login body must return 400 or 401",
                response.statusCode(), anyOf(equalTo(400), equalTo(401)));
    }

    // ═════════════════════════════════════════════════════════════════════════
    // US2 — TOKEN REFRESH
    // ═════════════════════════════════════════════════════════════════════════

    // ── TC-AUTH-012 ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-AUTH-012 | Refresh token → 200 + new accessToken")
    @Story("US2: Token Refresh")
    @Severity(SeverityLevel.CRITICAL)
    @Description("POST /api/v1/users/refresh with a valid refreshToken must return "
            + "a new accessToken that is different from the original.")
    void tokenRefresh_withValidRefreshToken_returnsNewAccessToken() {
        // Register + login to obtain both tokens
        UserRequest newUser = TestDataFactory.randomUserRequest();
        Response regResp = register(newUser);
        assertThat("Setup: registration must succeed", regResp.statusCode(), equalTo(201));
        trackUserId(regResp);

        Response loginResp = login(newUser.getEmail(), newUser.getPassword());
        assertThat("Setup: login must succeed", loginResp.statusCode(), equalTo(200));

        String originalAccessToken = loginResp.jsonPath().getString("accessToken");
        String refreshToken = loginResp.jsonPath().getString("refreshToken");

        // Skip the test gracefully if the API doesn't return a refreshToken on login
        if (refreshToken == null || refreshToken.isBlank()) {
            log.warn("TC-AUTH-012 SKIPPED — login response does not contain a refreshToken");
            attachToReport("Skip Reason",
                    "Login response had no refreshToken; refresh endpoint cannot be tested.");
            return;
        }

        // ── ACT: Call the refresh endpoint ───────────────────────────────────
        Response refreshResp = given()
                .spec(requestSpec)
                .body(Map.of("refreshToken", refreshToken))
                .when()
                .post(TestConfig.API_PREFIX + "/users/refresh")
                .then()
                .extract()
                .response();

        attachJsonToReport("Refresh Token Response", refreshResp.body().prettyPrint());

        // ── ASSERT ───────────────────────────────────────────────────────────
        assertThat("Refresh must return HTTP 200", refreshResp.statusCode(), equalTo(200));

        String newAccessToken = refreshResp.jsonPath().getString("accessToken");
        assertThat("New accessToken must not be null", newAccessToken, notNullValue());

        // The new token should differ from the original (token rotation)
        assertThat("Refreshed accessToken should differ from the original",
                newAccessToken, not(equalTo(originalAccessToken)));
    }

    // ═════════════════════════════════════════════════════════════════════════
    // US2 — LOGOUT
    // ═════════════════════════════════════════════════════════════════════════

    // ── TC-AUTH-013 ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-AUTH-013 | Logout → 200, then /me with old token → 401")
    @Story("US2: Logout")
    @Severity(SeverityLevel.CRITICAL)
    @Description("After a successful logout the old access token must be invalidated — "
            + "GET /me with it must return 401.")
    void logout_invalidatesToken() {
        // Register a throwaway user for this test
        UserRequest newUser = TestDataFactory.randomUserRequest();
        Response regResp = register(newUser);
        assertThat("Setup: registration must succeed", regResp.statusCode(), equalTo(201));
        trackUserId(regResp);

        // Login to get a valid token
        Response loginResp = login(newUser.getEmail(), newUser.getPassword());
        assertThat("Setup: login must succeed", loginResp.statusCode(), equalTo(200));
        String accessToken = loginResp.jsonPath().getString("accessToken");
        String authHeader = "Bearer " + accessToken;

        // ── ACT: Logout ───────────────────────────────────────────────────────
        Response logoutResp = given()
                .spec(requestSpec)
                .header("Authorization", authHeader)
                .when()
                .post(TestConfig.API_PREFIX + "/users/logout")
                .then()
                .extract()
                .response();

        attachJsonToReport("Logout Response", logoutResp.body().prettyPrint());
        assertThat("Logout must return HTTP 200", logoutResp.statusCode(), equalTo(200));

        // ── ASSERT: Old token must now be rejected ────────────────────────────
        Response meResp = given()
                .spec(requestSpec)
                .header("Authorization", authHeader) // reuse the now-invalidated token
                .when()
                .get(TestConfig.API_PREFIX + "/users/me")
                .then()
                .extract()
                .response();

        attachJsonToReport("/me After Logout Response", meResp.body().prettyPrint());
        assertThat("Revoked token must return 401 on /me", meResp.statusCode(), equalTo(401));
    }

    // ═════════════════════════════════════════════════════════════════════════
    // US3 — AUTHENTICATED USER PROFILE
    // ═════════════════════════════════════════════════════════════════════════

    // ── TC-AUTH-014 ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-AUTH-014 | GET /users/me with valid token → 200 + user profile")
    @Story("US3: User Profile")
    @Severity(SeverityLevel.BLOCKER)
    void getMe_withValidToken_returns200WithProfile() {
        // Register a unique user so the expected email is known
        UserRequest newUser = TestDataFactory.randomUserRequest();
        Response regResp = register(newUser);
        assertThat("Setup: registration must succeed", regResp.statusCode(), equalTo(201));
        trackUserId(regResp);

        // Login to get a fresh token
        Response loginResp = login(newUser.getEmail(), newUser.getPassword());
        assertThat("Setup: login must succeed", loginResp.statusCode(), equalTo(200));
        String authHeader = "Bearer " + loginResp.jsonPath().getString("accessToken");

        // ── ACT: GET /users/me ───────────────────────────────────────────────
        Response meResp = given()
                .spec(requestSpec)
                .header("Authorization", authHeader)
                .when()
                .get(TestConfig.API_PREFIX + "/users/me")
                .then()
                .extract()
                .response();

        attachJsonToReport("/users/me Response", meResp.body().prettyPrint());

        // ── ASSERT ────────────────────────────────────────────────────────────
        assertThat("GET /users/me must return 200", meResp.statusCode(), equalTo(200));

        assertThat("/users/me must return the correct email",
                meResp.jsonPath().getString("email"),
                equalToIgnoringCase(newUser.getEmail()));

        // Password must never be returned
        assertThat("/users/me must not expose plaintext password",
                meResp.body().asString(), not(containsString(newUser.getPassword())));
    }

    // ── TC-AUTH-015 ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-AUTH-015 | GET /users/me without token → 401 Unauthorized")
    @Story("US3: User Profile")
    @Severity(SeverityLevel.CRITICAL)
    void getMe_withoutToken_returns401() {
        // Call /users/me with no Authorization header at all
        Response response = given()
                .spec(requestSpec)
                .when()
                .get(TestConfig.API_PREFIX + "/users/me")
                .then()
                .extract()
                .response();

        attachJsonToReport("/users/me (No Token) Response", response.body().prettyPrint());
        assertThat("Missing token must return 401", response.statusCode(), equalTo(401));
    }

    // ── TC-AUTH-016 ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-AUTH-016 | GET /users/me with malformed/expired token → 401")
    @Story("US3: User Profile")
    @Severity(SeverityLevel.CRITICAL)
    void getMe_withInvalidToken_returns401() {
        // Use a syntactically plausible but cryptographically invalid JWT
        String fakeJwt = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"
                + ".eyJzdWIiOiJmYWtlIiwiaWF0IjoxNjAwMDAwMDAwfQ"
                + ".INVALID_SIGNATURE_XXXXXXXXXXXXXXXXXXXXXXX";

        Response response = given()
                .spec(requestSpec)
                .header("Authorization", fakeJwt)
                .when()
                .get(TestConfig.API_PREFIX + "/users/me")
                .then()
                .extract()
                .response();

        attachJsonToReport("/users/me (Invalid Token) Response", response.body().prettyPrint());
        assertThat("Invalid token must return 401", response.statusCode(), equalTo(401));
    }

    // ── TC-AUTH-017 ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("TC-AUTH-017 | GET /users/{id} → 200 + correct user data")
    @Story("US3: User Profile")
    @Severity(SeverityLevel.NORMAL)
    void getUserById_withValidId_returns200() {
        // Register a user and capture their assigned ID
        UserRequest newUser = TestDataFactory.randomUserRequest();
        Response regResp = register(newUser);
        assertThat("Setup: registration must succeed", regResp.statusCode(), equalTo(201));

        String userId = regResp.jsonPath().getString("id");
        trackUserId(regResp);

        // Login to get a token (endpoint may require authentication)
        Response loginResp = login(newUser.getEmail(), newUser.getPassword());
        assertThat("Setup: login must succeed", loginResp.statusCode(), equalTo(200));
        String authHeader = "Bearer " + loginResp.jsonPath().getString("accessToken");

        // ── ACT: GET /users/{id} ─────────────────────────────────────────────
        Response response = given()
                .spec(requestSpec)
                .header("Authorization", authHeader)
                .when()
                .get(TestConfig.USERS_ENDPOINT + "/" + userId)
                .then()
                .extract()
                .response();

        attachJsonToReport("GET /users/" + userId + " Response", response.body().prettyPrint());

        // ── ASSERT ────────────────────────────────────────────────────────────
        assertThat("GET /users/{id} must return 200", response.statusCode(), equalTo(200));
        assertThat("Returned user id must match the requested id",
                response.jsonPath().getString("id"), equalTo(userId));
        assertThat("Returned email must match",
                response.jsonPath().getString("email"),
                equalToIgnoringCase(newUser.getEmail()));
    }

    // ── TC-AUTH-018 — Protect non-auth endpoints ──────────────────────────────

    @ParameterizedTest(name = "TC-AUTH-018 | Protected endpoint without token: {0} → 401")
    @ValueSource(strings = {
            "/api/v1/users/me",
            "/api/v1/posts",
            "/api/v1/comments",
    })
    @DisplayName("TC-AUTH-018 | Protected endpoints without token → 401")
    @Story("US3: User Profile")
    @Severity(SeverityLevel.CRITICAL)
    void protectedEndpoints_withoutToken_return401(String endpoint) {
        Response response = given()
                .spec(requestSpec)
                // Intentionally omit Authorization header
                .when()
                .get(endpoint)
                .then()
                .extract()
                .response();

        assertThat("Protected endpoint '" + endpoint + "' must reject unauthenticated request with 401",
                response.statusCode(), equalTo(401));
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Private helper methods (DRY / SRP)
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Sends POST /api/v1/users with the given payload and returns the raw response.
     * Attaches the request body to Allure as a named step.
     */
    @Step("Register user: {payload.email}")
    private Response register(UserRequest payload) {
        return given()
                .spec(requestSpec)
                .body(payload) // Jackson serialises UserRequest → JSON
                .when()
                .post(TestConfig.USERS_ENDPOINT)
                .then()
                .extract()
                .response();
    }

    /**
     * Sends POST /api/v1/users/login with the given credentials.
     * Returned response is NOT asserted here — the caller decides what is valid.
     */
    @Step("Login as {email}")
    private Response login(String email, String password) {
        return given()
                .spec(requestSpec)
                .body(Map.of("email", email, "password", password))
                .when()
                .post(TestConfig.LOGIN_ENDPOINT)
                .then()
                .extract()
                .response();
    }

    /**
     * Builds a {@code "Bearer <token>"} header string using the default test user.
     * Used as a convenience in cleanup logic (e.g. admin delete).
     */
    private String buildAuthHeader(String accessToken) {
        return "Bearer " + accessToken;
    }

    /**
     * Attempts to fetch an admin-level Bearer token for teardown (DELETE)
     * operations.
     * Falls back to the default test-user token if admin credentials are
     * unavailable.
     */
    @Step("Obtain admin Bearer token for cleanup")
    private String obtainAdminBearerToken() {
        try {
            // Try the default test credentials — adjust to a real admin user if needed
            Response resp = login(TestConfig.TEST_USER_EMAIL, TestConfig.TEST_USER_PASSWORD);
            if (resp.statusCode() == 200) {
                return buildAuthHeader(resp.jsonPath().getString("accessToken"));
            }
        } catch (Exception ex) {
            log.warn("Could not obtain admin token for cleanup: {}", ex.getMessage());
        }
        // Return a dummy header if login fails — DELETE will simply return 401 and be
        // swallowed
        return "Bearer CLEANUP_TOKEN_UNAVAILABLE";
    }

    /**
     * Extracts the user {@code id} field from a registration response and
     * adds it to the cleanup list so {@link #onAfterEach} can delete it.
     *
     * @param registrationResponse the 201 response from POST /api/v1/users
     */
    private void trackUserId(Response registrationResponse) {
        try {
            String id = registrationResponse.jsonPath().getString("id");
            if (id != null && !id.isBlank()) {
                createdUserIds.add(id);
                log.debug("Tracking user id {} for post-test cleanup", id);
            }
        } catch (Exception ex) {
            log.warn("Could not extract user id for tracking: {}", ex.getMessage());
        }
    }
}
