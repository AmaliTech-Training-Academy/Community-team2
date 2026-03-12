package com.communityboard.tests.base;

/**
 * TestConfig — single source of truth for all environment-level constants.
 *
 * <p>
 * Supports three resolution layers (highest priority first):
 * <ol>
 * <li>Maven / JVM system property (-DBASE_URL=...)</li>
 * <li>OS environment variable (BASE_URL=...)</li>
 * <li>Hard-coded default (ngrok tunnel)</li>
 * </ol>
 *
 * <p>
 * SOLID note: This class has a single responsibility — provide config values.
 * No test logic belongs here.
 */
public final class TestConfig {

    // ─── Base URL ────────────────────────────────────────────────────────────

    /**
     * Root URL of the CommunityBoard API.
     * Override at runtime: mvn test -DBASE_URL=https://my-other-host.com
     */
    public static final String BASE_URL = resolveProperty(
            "BASE_URL",
            "https://14c5-154-161-126-54.ngrok-free.app");

    // ─── API Path Prefix ─────────────────────────────────────────────────────

    /** Versioned API prefix prepended to every endpoint path. */
    public static final String API_PREFIX = "/api/v1";

    // ─── Endpoint Paths ──────────────────────────────────────────────────────

    /** POST /api/v1/users/login — exchanges credentials for a JWT token. */
    public static final String LOGIN_ENDPOINT = API_PREFIX + "/users/login";

    /** GET /api/v1/categories — public categories listing (used in smoke test). */
    public static final String CATEGORIES_ENDPOINT = API_PREFIX + "/categories";

    /** POST /api/v1/users — create / register a new user account. */
    public static final String USERS_ENDPOINT = API_PREFIX + "/users";

    /** POST /api/v1/posts — create a new post. */
    public static final String POSTS_ENDPOINT = API_PREFIX + "/posts";

    /** POST /api/v1/comments — create a new comment. */
    public static final String COMMENTS_ENDPOINT = API_PREFIX + "/comments";

    // ─── Default Test-User Credentials ───────────────────────────────────────

    /**
     * E-mail of the pre-seeded "default" test user.
     * Override via TEST_USER_EMAIL env-var or -DTEST_USER_EMAIL=... JVM arg.
     */
    public static final String TEST_USER_EMAIL = resolveProperty(
            "TEST_USER_EMAIL",
            "qatest@communityboard.local");

    /**
     * Password of the pre-seeded test user.
     * Override via TEST_USER_PASSWORD env-var or -DTEST_USER_PASSWORD=... JVM arg.
     */
    public static final String TEST_USER_PASSWORD = resolveProperty(
            "TEST_USER_PASSWORD",
            "QaTest@1234!");

    // ─── HTTP / Timing ───────────────────────────────────────────────────────

    /**
     * Maximum time (in milliseconds) to wait for a single HTTP response.
     * Tests will fail early if the server is unreachable within this window.
     */
    public static final int CONNECTION_TIMEOUT_MS = 10_000;

    /** Ngrok header injected to suppress the browser-warning page. */
    public static final String NGROK_SKIP_HEADER_NAME = "ngrok-skip-browser-warning";
    public static final String NGROK_SKIP_HEADER_VALUE = "true";

    // ─── Private Constructor ──────────────────────────────────────────────────

    /** Utility class — must never be instantiated. */
    private TestConfig() {
        throw new UnsupportedOperationException("TestConfig is a utility class");
    }

    // ─── Helper ──────────────────────────────────────────────────────────────

    /**
     * Resolves a property value using the three-layer priority described above.
     *
     * @param key          the property / environment-variable name
     * @param defaultValue fallback value when neither a JVM property nor env-var
     *                     exists
     * @return the resolved string value
     */
    private static String resolveProperty(String key, String defaultValue) {
        // 1️⃣ Check JVM system property (set by Maven via <systemPropertyVariables> or
        // -D flag)
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.isBlank()) {
            return sysProp;
        }
        // 2️⃣ Check OS environment variable
        String envVar = System.getenv(key);
        if (envVar != null && !envVar.isBlank()) {
            return envVar;
        }
        // 3️⃣ Use the compile-time default
        return defaultValue;
    }
}
