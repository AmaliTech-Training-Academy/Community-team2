package com.communityboard.tests.smoke;

import com.communityboard.tests.base.BaseApiTest;
import com.communityboard.tests.base.TestConfig;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * ConnectionSmokeTest — fastest possible sanity check that the API is
 * reachable.
 *
 * <p>
 * This test class serves one purpose only (SRP): verify that the backend is up
 * and the {@code /api/v1/categories} endpoint is responding correctly.
 *
 * <p>
 * It intentionally avoids authentication so failures here point directly to
 * infrastructure / connectivity problems, not auth issues.
 *
 * <p>
 * Allure metadata:
 * <ul>
 * <li>{@code @Epic} → high-level product area grouping in the report</li>
 * <li>{@code @Feature} → feature within that epic</li>
 * <li>{@code @Story} → the user story being validated</li>
 * <li>{@code @Severity}→ how critical this test is if it fails</li>
 * </ul>
 */
@Epic("CommunityBoard API")
@Feature("Connectivity")
@Tag("smoke") // run only smoke tests with: mvn test -Dgroups=smoke
public class ConnectionSmokeTest extends BaseApiTest {

    // ─────────────────────────────────────────────────────────────────────────
    // Tests
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Verifies that GET /api/v1/categories returns HTTP 200 and a non-null body,
     * confirming the backend is reachable at {@link TestConfig#BASE_URL}.
     */
    @Test
    @DisplayName("GET /api/v1/categories → 200 OK (connectivity smoke check)")
    @Story("As QA I can verify the API is reachable before running the full suite")
    @Severity(SeverityLevel.BLOCKER) // If this fails, nothing else will work
    @Description("Smoke test: sends an unauthenticated GET to /api/v1/categories. "
            + "A 200 response confirms: (1) the ngrok tunnel is alive, "
            + "(2) Spring Boot has started, and "
            + "(3) the categories endpoint is publicly accessible (no auth required). "
            + "Allure automatically attaches request and response details.")
    void categoriesEndpoint_shouldReturn200() {
        // ── ARRANGE ──────────────────────────────────────────────────────────
        // requestSpec already has baseUri, content-type, ngrok header, and the
        // AllureRestAssured filter — nothing additional needed for a public endpoint.

        // ── ACT ──────────────────────────────────────────────────────────────
        Response response = given()
                .spec(requestSpec)
                // Enable verbose request/response logging to stdout (helpful during CI
                // debugging)
                .log().ifValidationFails()
                .when()
                .get(TestConfig.CATEGORIES_ENDPOINT) // GET /api/v1/categories
                .then()
                .log().ifValidationFails() // log the response only when assertions fail
                .extract()
                .response();

        // ── ASSERT ───────────────────────────────────────────────────────────

        // 1. Verify HTTP status is exactly 200 OK
        assertThat(
                "Expected HTTP 200 from categories endpoint",
                response.statusCode(),
                equalTo(200));

        // 2. Verify the response body is not empty / null
        assertThat(
                "Response body must not be null or empty",
                response.body().asString(),
                not(emptyOrNullString()));

        // 3. Verify response time is within acceptable bounds (10 s = slowest
        // acceptable for ngrok)
        assertThat(
                "Response time exceeded " + TestConfig.CONNECTION_TIMEOUT_MS + " ms",
                response.time(),
                lessThan((long) TestConfig.CONNECTION_TIMEOUT_MS));

        // ── REPORT ───────────────────────────────────────────────────────────
        // Attach the raw response body to the Allure report for easy inspection
        attachJsonToReport("Categories Response Body", response.body().prettyPrint());

        // Attach key metadata as a plain-text note
        attachToReport("Response Metadata",
                "Status : " + response.statusCode() + "\n" +
                        "Time   : " + response.time() + " ms\n" +
                        "URL    : " + TestConfig.BASE_URL + TestConfig.CATEGORIES_ENDPOINT);

        log.info("Smoke test PASSED — {} responded in {} ms",
                TestConfig.CATEGORIES_ENDPOINT, response.time());
    }

    /**
     * Verifies that the API returns a proper error (4xx) for a non-existent
     * endpoint,
     * confirming that the server is handling routing correctly (not returning 200
     * for everything).
     */
    @Test
    @DisplayName("GET /api/v1/nonexistent → 404 Not Found")
    @Story("API returns 404 for unknown routes")
    @Severity(SeverityLevel.NORMAL)
    void nonExistentEndpoint_shouldReturn4xx() {
        // ── ACT + ASSERT (fluent chaining) ───────────────────────────────────
        given()
                .spec(requestSpec)
                .when()
                .get(TestConfig.API_PREFIX + "/this-endpoint-does-not-exist")
                .then()
                // Accept any 4xx status — different frameworks return 404 vs 405 for unknown
                // paths
                .statusCode(anyOf(
                        equalTo(404),
                        equalTo(405),
                        equalTo(400)));

        log.info("404/4xx guard smoke test PASSED");
    }
}
