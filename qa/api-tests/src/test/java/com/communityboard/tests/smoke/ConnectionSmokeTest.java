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
 * Single responsibility: verify infrastructure is alive before the full suite
 * runs.
 * Avoids authentication deliberately — a failure here means a connectivity
 * problem,
 * not an auth problem.
 */
@Epic("CommunityBoard API")
@Feature("Connectivity")
@Tag("smoke") // run with: mvn test -Dgroups=smoke
public class ConnectionSmokeTest extends BaseApiTest {

        // ─────────────────────────────────────────────────────────────────────────
        // Test 1 — Happy path: categories endpoint returns 200
        // ─────────────────────────────────────────────────────────────────────────

        @Test
        @DisplayName("GET /api/v1/categories → 200 OK (connectivity smoke check)")
        @Story("API is reachable and categories endpoint is publicly accessible")
        @Severity(SeverityLevel.BLOCKER)
        @Description("Unauthenticated GET to /api/v1/categories. "
                        + "Confirms ngrok tunnel alive, Spring Boot started, and endpoint public.")
        void categoriesEndpoint_shouldReturn200() {

                // ── ACT ────────────────────────────────────────────────────────────────
                // Always log both request and response to stdout so CI pipelines have full
                // visibility even before Allure picks up the results directory.
                Response response = given()
                                .spec(requestSpec)
                                .log().all()
                                .when()
                                .get(TestConfig.CATEGORIES_ENDPOINT)
                                .then()
                                .log().all()
                                .extract()
                                .response();

                // ── REPORT (attach BEFORE asserting — Allure captures data even on failure) ──
                attachToReport("Response Metadata",
                                "Status : " + response.statusCode() + "\n"
                                                + "Time   : " + response.time() + " ms\n"
                                                + "URL    : " + TestConfig.BASE_URL + TestConfig.CATEGORIES_ENDPOINT);
                attachJsonToReport("Categories Response Body", response.body().prettyPrint());

                // ── ASSERT ─────────────────────────────────────────────────────────────

                // 1. HTTP 200 — include a helpful hint in the message pointing to ngrok
                assertThat(
                                "Expected HTTP 200 from " + TestConfig.CATEGORIES_ENDPOINT
                                                + " — check that ngrok tunnel is alive (BASE_URL=" + TestConfig.BASE_URL
                                                + ")",
                                response.statusCode(),
                                equalTo(200));

                // 2. Body must be non-empty and NOT the ngrok browser-warning HTML page
                String body = response.body().asString();
                assertThat("Response body must not be null or empty", body, not(emptyOrNullString()));
                assertThat("Response must not be the ngrok browser-warning HTML page — add ngrok header",
                                body.trim(), not(startsWith("<!DOCTYPE")));

                // 3. Response time within the configured ceiling
                assertThat(
                                "Response time exceeded " + TestConfig.CONNECTION_TIMEOUT_MS + " ms",
                                response.time(),
                                lessThan((long) TestConfig.CONNECTION_TIMEOUT_MS));

                log.info("Smoke test PASSED — {} → {} in {} ms",
                                TestConfig.CATEGORIES_ENDPOINT, response.statusCode(), response.time());
        }

        // ─────────────────────────────────────────────────────────────────────────
        // Test 2 — Guard: unknown route returns a 4xx (not 200)
        // ─────────────────────────────────────────────────────────────────────────

        @Test
        @DisplayName("GET /api/v1/nonexistent → 4xx (routing guard)")
        @Story("API returns a 4xx error for unknown routes")
        @Severity(SeverityLevel.NORMAL)
        void nonExistentEndpoint_shouldReturn4xx() {

                Response response = given()
                                .spec(requestSpec)
                                .when()
                                .get(TestConfig.API_PREFIX + "/this-endpoint-does-not-exist-abc123")
                                .then()
                                .extract()
                                .response();

                // Attach status for Allure visibility
                attachToReport("4xx Guard Status", "HTTP " + response.statusCode());

                // Accept 404 or 405 — Spring Boot and ngrok may differ on unknown paths
                assertThat(
                                "Expected 404 or 405 for an unknown route, got: " + response.statusCode(),
                                response.statusCode(),
                                anyOf(equalTo(404), equalTo(405), equalTo(400)));

                log.info("4xx guard smoke test PASSED — got HTTP {}", response.statusCode());
        }
}
