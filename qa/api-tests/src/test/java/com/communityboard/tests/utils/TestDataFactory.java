package com.communityboard.tests.utils;

import com.github.javafaker.Faker;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.Locale;

/**
 * TestDataFactory — central factory for building request-body POJOs.
 *
 * <p>
 * SOLID notes:
 * <ul>
 * <li><b>SRP</b>: Solely responsible for generating test data. No HTTP /
 * assertion code lives here.</li>
 * <li><b>OCP</b>: New resource types (e.g. AlbumRequest) can be added without
 * modifying existing nested classes.</li>
 * </ul>
 *
 * <p>
 * Lombok annotations used:
 * <ul>
 * <li>{@code @Value} → immutable POJO with final fields, getters,
 * equals/hashCode/toString.</li>
 * <li>{@code @Builder} → fluent builder so tests can override individual fields
 * easily.</li>
 * </ul>
 */
public final class TestDataFactory {

    // JavaFaker with locale en-GB for realistic but predictable data
    private static final Faker FAKER = new Faker(Locale.UK);

    /** Utility class — no instances. */
    private TestDataFactory() {
        throw new UnsupportedOperationException("TestDataFactory is a utility class");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Unique E-mail Generator
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Generates a unique e-mail address guaranteed not to collide across parallel
     * test runs.
     * Format: {@code testuser_<epochMillis>_<4-digit-random>@example.com}
     *
     * @return unique e-mail string
     */
    public static String uniqueEmail() {
        // Combine epoch time + random suffix for uniqueness within and across test runs
        return String.format("testuser_%d_%04d@example.com",
                Instant.now().toEpochMilli(),
                FAKER.number().numberBetween(1000, 9999));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UserRequest
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Payload for POST /api/v1/users (user registration / creation).
     * Jackson serialises this to JSON automatically via REST Assured's object
     * mapper.
     */
    @Value // generates final fields + getters (immutable)
    @Builder // generates UserRequest.builder()...build()
    public static class UserRequest {
        String name;
        String email;
        String password;
        String role; // e.g. "USER" or "ADMIN" — null-safe (Jackson skips nulls by default)
    }

    /**
     * Builds a {@link UserRequest} filled with random but realistic data.
     * Tests that need specific values can call
     * {@code .toBuilder().email("custom@test.com").build()}.
     *
     * @return a randomised UserRequest ready to be sent as a request body
     */
    public static UserRequest randomUserRequest() {
        return UserRequest.builder()
                .name(FAKER.name().fullName()) // e.g. "Alice Carrington"
                .email(uniqueEmail()) // collision-safe unique address
                .password(FAKER.internet().password(10, 20, true, true, true)) // strong password
                .role("USER") // default non-privileged role
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PostRequest
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Payload for POST /api/v1/posts (create a community board post).
     */
    @Value
    @Builder
    public static class PostRequest {
        String title;
        String content;
        Long categoryId; // nullable — omit if the API doesn't require it
    }

    /**
     * Returns a {@link PostRequest} with a realistic title, body text, and a
     * randomly chosen category (1–5, adjust to match seeded categories).
     *
     * @return a randomised PostRequest
     */
    public static PostRequest randomPostRequest() {
        return PostRequest.builder()
                .title(FAKER.book().title()) // e.g. "The Midnight Library"
                .content(FAKER.lorem().paragraph(3)) // ~3 paragraphs of lorem ipsum
                .categoryId((long) FAKER.number().numberBetween(1, 6)) // categories 1–5
                .build();
    }

    /**
     * Returns a {@link PostRequest} with explicit values — useful for
     * assertion-heavy tests
     * where the expected response values must be known upfront.
     *
     * @param title      post title
     * @param content    post body content
     * @param categoryId target category identifier
     * @return a deterministic PostRequest
     */
    public static PostRequest postRequest(String title, String content, Long categoryId) {
        return PostRequest.builder()
                .title(title)
                .content(content)
                .categoryId(categoryId)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CommentRequest
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Payload for POST /api/v1/comments (add a comment to an existing post).
     */
    @Value
    @Builder
    public static class CommentRequest {
        Long postId; // the post this comment belongs to
        String content; // the comment body
    }

    /**
     * Returns a {@link CommentRequest} with a random single-sentence comment.
     *
     * @param postId the post ID to attach the comment to
     * @return a randomised CommentRequest
     */
    public static CommentRequest randomCommentRequest(Long postId) {
        return CommentRequest.builder()
                .postId(postId)
                .content(FAKER.lorem().sentence(10, 5)) // 10-word sentence + up to 5 extra words
                .build();
    }
}
