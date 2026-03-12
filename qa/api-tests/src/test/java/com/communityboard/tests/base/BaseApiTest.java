package com.communityboard.tests.base;

import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BaseApiTest — abstract base class that every concrete test class must extend.
 *
 * <p>
 * Responsibilities (SRP — each listed item is one concern handled here):
 * <ol>
 * <li>Configure and expose a shared {@link RequestSpecification} with the
 * correct
 * base URI, content type, timeouts, and the Allure logging filter.</li>
 * <li>Provide per-test lifecycle hooks ({@code @BeforeEach / @AfterEach}).</li>
 * <li>Expose a convenience method to fetch a fresh JWT token via
 * {@link AuthHelper}.</li>
 * <li>Provide a cleanup hook ({@code @AfterEach}) for sub-classes to register
 * teardown logic without overriding lifecycle methods directly.</li>
 * </ol>
 *
 * <p>
 * SOLID notes:
 * <ul>
 * <li><b>DIP</b>: Sub-classes depend on the {@code requestSpec} field
 * (abstraction),
 * not on a hard-coded {@link io.restassured.RestAssured} static call.</li>
 * <li><b>OCP</b>: New behaviour (e.g. mTLS, proxy) can be added by extending
 * this class,
 * not by modifying it.</li>
 * </ul>
 */
public abstract class BaseApiTest {

    protected static final Logger log = LoggerFactory.getLogger(BaseApiTest.class);

    /**
     * Shared request specification.
     * {@code protected} so all subclasses can pass it directly to REST Assured
     * calls.
     */
    protected static RequestSpecification requestSpec;

    /**
     * Per-class AuthHelper instance.
     * Declared here so subclasses can call
     * {@code authHelper.loginAsDefaultTestUser()}
     * without constructing their own helper.
     */
    protected AuthHelper authHelper;

    // ─────────────────────────────────────────────────────────────────────────
    // Suite-level setup — runs ONCE before all tests in the suite
    // ─────────────────────────────────────────────────────────────────────────

    @BeforeAll
    static void initSuite() {
        log.info("=== Suite Init — Base URL: {}", TestConfig.BASE_URL);

        // Configure global REST Assured timeouts (prevents tests hanging indefinitely)
        RestAssured.config = RestAssuredConfig.config()
                .httpClient(HttpClientConfig.httpClientConfig()
                        // Time allowed to establish TCP connection
                        .setParam("http.connection.timeout", TestConfig.CONNECTION_TIMEOUT_MS)
                        // Time allowed for the server to send first byte after connection
                        .setParam("http.socket.timeout", TestConfig.CONNECTION_TIMEOUT_MS));

        // Build the shared RequestSpecification — acts as a template for all requests
        requestSpec = new RequestSpecBuilder()
                // Base URI from env/property or default ngrok URL
                .setBaseUri(TestConfig.BASE_URL)
                // All requests send and receive JSON by default
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                // Allure filter: intercepts every req/resp and attaches them as report steps
                .addFilter(new AllureRestAssured()
                        .setRequestTemplate("http-request.ftl") // optional custom Freemarker templates
                        .setResponseTemplate("http-response.ftl"))
                // Ngrok suppresses its browser-warning page when this header is present
                .addHeader(TestConfig.NGROK_SKIP_HEADER_NAME, TestConfig.NGROK_SKIP_HEADER_VALUE)
                // Log ALL request + response details to stdout for local debugging
                .log(LogDetail.ALL)
                .build();

        log.info("=== RequestSpecification built successfully — target: {}", TestConfig.BASE_URL);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test-level setup — runs before EACH test method
    // ─────────────────────────────────────────────────────────────────────────

    @BeforeEach
    void initTest() {
        // Construct a fresh AuthHelper for every test so token state never leaks
        authHelper = new AuthHelper(requestSpec);
        onBeforeEach();
    }

    /**
     * Extension point for subclasses — override to add per-test setup logic
     * without re-declaring {@code @BeforeEach} (which would shadow this one).
     */
    protected void onBeforeEach() {
        // Default: no-op — subclasses override as needed
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Teardown hooks
    // ─────────────────────────────────────────────────────────────────────────

    @AfterEach
    void tearDownTest() {
        log.debug("--- @AfterEach teardown");
        onAfterEach();
    }

    /**
     * Extension point for subclasses — override to delete resources created during
     * a test (e.g., call DELETE /api/v1/posts/{id} for a post created in
     * {@code @Test}).
     */
    protected void onAfterEach() {
        // Default: no-op
    }

    @AfterAll
    static void tearDownSuite() {
        log.info("=== Suite teardown complete");
        onAfterAll();
    }

    /**
     * Extension point for subclasses — override for suite-scope cleanup
     * (e.g., invalidate admin tokens, truncate test data via a maintenance API).
     */
    protected static void onAfterAll() {
        // Default: no-op
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Convenience helpers available to all subclasses
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Obtains a fresh JWT for the default test user and returns an
     * {@code Authorization: Bearer <token>} header string ready for use.
     *
     * <p>
     * Delegates to {@link AuthHelper#loginAsDefaultTestUser()} which
     * annotates the login step in the Allure report automatically.
     *
     * @return "Bearer &lt;accessToken&gt;"
     */
    @Step("Obtain Bearer token for default test user")
    protected String obtainBearerToken() {
        String token = authHelper.loginAsDefaultTestUser();
        return "Bearer " + token;
    }

    /**
     * Attaches arbitrary text to the running Allure report step.
     * Useful for adding contextual notes inline without a full {@link Step}
     * annotation.
     *
     * @param name    label shown in the report
     * @param content text content of the attachment
     */
    protected void attachToReport(String name, String content) {
        Allure.addAttachment(name, "text/plain", content, ".txt");
    }

    /**
     * Attaches a JSON string to the Allure report with proper syntax highlighting.
     *
     * @param name label shown in the report
     * @param json raw JSON string
     */
    protected void attachJsonToReport(String name, String json) {
        Allure.addAttachment(name, "application/json", json, ".json");
    }
}
