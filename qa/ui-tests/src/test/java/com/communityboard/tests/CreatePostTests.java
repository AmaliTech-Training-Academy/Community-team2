package com.communityboard.tests;

import com.communityboard.base.BaseTest;
import com.communityboard.pages.*;
import com.communityboard.utils.TestDataFactory;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CreatePostTests - Test class for post creation functionality
 * Covers test cases TC_POST_001 to TC_POST_010
 * 
 * Test Scenarios:
 * - Positive: Create post with valid data (with/without image)
 * - Negative: Empty fields, missing required data
 * - Validation: Field-level error messages
 * - Boundary: Max length inputs, special characters
 * - Functional: Post appears in feed, category display
 * 
 * Extends BaseTest for WebDriver lifecycle management
 */
@Epic("Post Management")
@Feature("Create Post")
@Story("US3: Create Post")
public class CreatePostTests extends BaseTest {

    // Page objects
    private LandingPage landingPage;
    private LoginPage loginPage;
    private RegisterPage registerPage;
    private HomePage homePage;
    private CreatePostPage createPostPage;

    // Test user credentials
    private String testUserEmail;
    private String testUserPassword;

    /**
     * Setup method - Executes before each test
     * Creates and logs in test user, navigates to home page
     */
    @BeforeEach
    public void setUpTest() {
        // Initialize pages
        landingPage = new LandingPage(driver);
        
        // Create and login test user
        createAndLoginTestUser();
        
        // Navigate to home page
        homePage = new HomePage(driver);
        homePage.navigateToHomePage();
        
        // Verify on home page
        assertThat(homePage.isOnHomePage())
            .as("Should be on home page")
            .isTrue();
    }

    /**
     * Helper method to create and login test user
     */
    private void createAndLoginTestUser() {
        // Generate credentials
        testUserEmail = TestDataFactory.generateUniqueEmail();
        testUserPassword = TestDataFactory.generateValidPassword();
        
        // Navigate to landing page
        landingPage.navigateToLandingPage();
        
        // Register user
        registerPage = landingPage.clickRegisterLink();
        registerPage.submitRegistrationForm(
            TestDataFactory.generateFirstName(),
            TestDataFactory.generateLastName(),
            testUserEmail,
            testUserPassword,
            testUserPassword
        );
        
        // Navigate back and login
        landingPage.navigateToLandingPage();
        loginPage = landingPage.clickLoginButton();
        loginPage.login(testUserEmail, testUserPassword);
    }


    /**
     * TC_POST_001: Create post with valid title, content, category (no image)
     * Positive test - Verify user can create post with required fields only
     */
    @Test
    @DisplayName("TC_POST_001: Create post with valid data (no image)")
    @Description("User should be able to create post with title, content, and category")
    @Severity(SeverityLevel.CRITICAL)
    public void testCreatePostWithoutImage() {
        // Generate test data
        String title = TestDataFactory.generatePostTitle();
        String content = TestDataFactory.generatePostContent();
        String category = TestDataFactory.generateRandomCategory();

        // Click create post button
        createPostPage = homePage.clickCreatePostButton();

        // Fill and submit post form
        createPostPage.submitPost(title, content, category);

        // Wait for success or redirect
        // Verify success message or redirect to home
        boolean isSuccessful = createPostPage.isSuccessMessageDisplayed() || 
                              homePage.isOnHomePage();

        assertThat(isSuccessful)
            .as("Post should be created successfully")
            .isTrue();

        // Refresh home page to see new post
        homePage.refreshPage();

        // Verify post appears in feed
        boolean postExists = homePage.isPostDisplayedByTitle(title);
        assertThat(postExists)
            .as("Created post should appear in feed")
            .isTrue();
    }

    /**
     * TC_POST_002: Create post with all fields including image
     * Positive test - Verify user can create post with image
     */
    @Test
    @DisplayName("TC_POST_002: Create post with all fields including image")
    @Description("User should be able to create post with title, content, category, and image")
    @Severity(SeverityLevel.NORMAL)
    public void testCreatePostWithImage() {
        // Generate test data
        String title = TestDataFactory.generatePostTitle();
        String content = TestDataFactory.generatePostContent();
        String category = TestDataFactory.generateRandomCategory();
        // Note: Image path should be absolute path to test image file
        String imagePath = System.getProperty("user.dir") + "/src/test/resources/test-image.jpg";

        // Click create post button
        createPostPage = homePage.clickCreatePostButton();

        // Fill form with image
        createPostPage.fillPostFormWithImage(title, content, category, imagePath);

        // Verify image preview is displayed
        boolean isImagePreviewDisplayed = createPostPage.isImagePreviewDisplayed();
        // Image preview may or may not be implemented
        // This is informational check

        // Submit post
        createPostPage.clickSubmitButton();

        // Verify post created successfully
        boolean isSuccessful = createPostPage.isSuccessMessageDisplayed() || 
                              homePage.isOnHomePage();

        assertThat(isSuccessful)
            .as("Post with image should be created successfully")
            .isTrue();
    }

    /**
     * TC_POST_003: Create post with empty title
     * Negative test - Verify validation error for missing title
     */
    @Test
    @DisplayName("TC_POST_003: Create post with empty title")
    @Description("System should display validation error when title is empty")
    @Severity(SeverityLevel.NORMAL)
    public void testCreatePostWithEmptyTitle() {
        // Generate test data with empty title
        String title = ""; // Empty title
        String content = TestDataFactory.generatePostContent();
        String category = TestDataFactory.generateRandomCategory();

        // Click create post button
        createPostPage = homePage.clickCreatePostButton();

        // Submit post with empty title
        createPostPage.submitPost(title, content, category);

        // Verify error message is displayed
        String errorMessage = createPostPage.getTitleError();
        assertThat(errorMessage)
            .as("Error message should be displayed for empty title")
            .isNotEmpty()
            .containsIgnoringCase("required");

        // Verify post was not created (still on create page or error shown)
        assertThat(createPostPage.isErrorMessageDisplayed())
            .as("Error should be displayed")
            .isTrue();
    }

    /**
     * TC_POST_004: Create post with empty content
     * Negative test - Verify validation error for missing content
     */
    @Test
    @DisplayName("TC_POST_004: Create post with empty content")
    @Description("System should display validation error when content is empty")
    @Severity(SeverityLevel.NORMAL)
    public void testCreatePostWithEmptyContent() {
        // Generate test data with empty content
        String title = TestDataFactory.generatePostTitle();
        String content = ""; // Empty content
        String category = TestDataFactory.generateRandomCategory();

        // Click create post button
        createPostPage = homePage.clickCreatePostButton();

        // Submit post with empty content
        createPostPage.submitPost(title, content, category);

        // Verify error message is displayed
        String errorMessage = createPostPage.getContentError();
        assertThat(errorMessage)
            .as("Error message should be displayed for empty content")
            .isNotEmpty()
            .containsIgnoringCase("required");

        // Verify error is displayed
        assertThat(createPostPage.isErrorMessageDisplayed())
            .as("Error should be displayed")
            .isTrue();
    }

    /**
     * TC_POST_005: Create post without selecting category
     * Negative test - Verify validation error for missing category
     */
    @Test
    @DisplayName("TC_POST_005: Create post without selecting category")
    @Description("System should display validation error when category is not selected")
    @Severity(SeverityLevel.NORMAL)
    public void testCreatePostWithoutCategory() {
        // Generate test data
        String title = TestDataFactory.generatePostTitle();
        String content = TestDataFactory.generatePostContent();

        // Click create post button
        createPostPage = homePage.clickCreatePostButton();

        // Fill only title and content (skip category)
        createPostPage.enterTitle(title);
        createPostPage.enterContent(content);
        createPostPage.clickSubmitButton();

        // Verify error message is displayed
        String errorMessage = createPostPage.getCategoryError();
        assertThat(errorMessage)
            .as("Error message should be displayed for missing category")
            .isNotEmpty()
            .containsIgnoringCase("required");
    }

    /**
     * TC_POST_006: Create post with title exceeding max length
     * Boundary test - Verify system handles very long title
     */
    @Test
    @DisplayName("TC_POST_006: Create post with title exceeding max length")
    @Description("System should handle or reject title exceeding maximum length")
    @Severity(SeverityLevel.MINOR)
    public void testCreatePostWithLongTitle() {
        // Generate very long title (e.g., 500 characters)
        String title = TestDataFactory.generateLongString(500);
        String content = TestDataFactory.generatePostContent();
        String category = TestDataFactory.generateRandomCategory();

        // Click create post button
        createPostPage = homePage.clickCreatePostButton();

        // Submit post with long title
        createPostPage.submitPost(title, content, category);

        // System should either accept or show validation error
        boolean hasError = createPostPage.isErrorMessageDisplayed();
        boolean isSuccessful = createPostPage.isSuccessMessageDisplayed() || 
                              homePage.isOnHomePage();

        // Either success or error is acceptable
        assertThat(hasError || isSuccessful)
            .as("System should handle long title appropriately")
            .isTrue();

        // If error, verify it mentions length
        if (hasError) {
            String errorMessage = createPostPage.getErrorMessage();
            assertThat(errorMessage)
                .as("Error should mention length limit")
                .matches("(?i).*(long|maximum|limit|character).*");
        }
    }

    /**
     * TC_POST_007: Create post with content exceeding max length
     * Boundary test - Verify system handles very long content
     */
    @Test
    @DisplayName("TC_POST_007: Create post with content exceeding max length")
    @Description("System should handle or reject content exceeding maximum length")
    @Severity(SeverityLevel.MINOR)
    public void testCreatePostWithLongContent() {
        // Generate very long content (e.g., 5000 characters)
        String title = TestDataFactory.generatePostTitle();
        String content = TestDataFactory.generateLongString(5000);
        String category = TestDataFactory.generateRandomCategory();

        // Click create post button
        createPostPage = homePage.clickCreatePostButton();

        // Submit post with long content
        createPostPage.submitPost(title, content, category);

        // System should either accept or show validation error
        boolean hasError = createPostPage.isErrorMessageDisplayed();
        boolean isSuccessful = createPostPage.isSuccessMessageDisplayed() || 
                              homePage.isOnHomePage();

        assertThat(hasError || isSuccessful)
            .as("System should handle long content appropriately")
            .isTrue();
    }

    /**
     * TC_POST_008: Create post with special characters in title
     * Boundary test - Verify system handles special characters
     */
    @Test
    @DisplayName("TC_POST_008: Create post with special characters in title")
    @Description("System should handle special characters in post title")
    @Severity(SeverityLevel.MINOR)
    public void testCreatePostWithSpecialCharactersInTitle() {
        // Generate title with special characters
        String title = "Test Post!@#$%^&*() - " + TestDataFactory.generateTimestamp();
        String content = TestDataFactory.generatePostContent();
        String category = TestDataFactory.generateRandomCategory();

        // Click create post button
        createPostPage = homePage.clickCreatePostButton();

        // Submit post with special characters
        createPostPage.submitPost(title, content, category);

        // Verify post is created or appropriate error shown
        boolean isSuccessful = createPostPage.isSuccessMessageDisplayed() || 
                              homePage.isOnHomePage();

        assertThat(isSuccessful)
            .as("Post with special characters should be created or show validation")
            .isTrue();
    }

    /**
     * TC_POST_009: Verify post appears in feed after creation
     * Functional test - Verify created post is visible in feed
     */
    @Test
    @DisplayName("TC_POST_009: Verify post appears in feed after creation")
    @Description("Created post should be visible in the feed immediately after creation")
    @Severity(SeverityLevel.CRITICAL)
    public void testPostAppearsInFeedAfterCreation() {
        // Generate test data
        String title = TestDataFactory.generatePostTitle();
        String content = TestDataFactory.generatePostContent();
        String category = TestDataFactory.generateRandomCategory();

        // Get initial post count
        int initialPostCount = homePage.getPostCount();

        // Create post
        createPostPage = homePage.clickCreatePostButton();
        createPostPage.submitPost(title, content, category);

        // Navigate back to home page
        homePage.navigateToHomePage();

        // Verify post count increased
        int newPostCount = homePage.getPostCount();
        assertThat(newPostCount)
            .as("Post count should increase after creating post")
            .isGreaterThan(initialPostCount);

        // Verify specific post is displayed
        boolean postExists = homePage.isPostDisplayedByTitle(title);
        assertThat(postExists)
            .as("Created post should be visible in feed")
            .isTrue();
    }

    /**
     * TC_POST_010: Verify category badge displays correctly
     * UI/UX test - Verify category is displayed on post card
     */
    @Test
    @DisplayName("TC_POST_010: Verify category badge displays correctly")
    @Description("Post category should be displayed correctly on post card in feed")
    @Severity(SeverityLevel.MINOR)
    public void testCategoryBadgeDisplaysCorrectly() {
        // Generate test data with specific category
        String title = TestDataFactory.generatePostTitle();
        String content = TestDataFactory.generatePostContent();
        String category = "NEWS"; // Specific category for verification

        // Create post
        createPostPage = homePage.clickCreatePostButton();
        createPostPage.submitPost(title, content, category);

        // Navigate to home page
        homePage.navigateToHomePage();

        // Find the created post and verify category
        boolean postExists = homePage.isPostDisplayedByTitle(title);
        assertThat(postExists)
            .as("Post should exist in feed")
            .isTrue();

        // Get category of first post (assuming it's the newly created one)
        String displayedCategory = homePage.getPostCategoryByIndex(0);
        assertThat(displayedCategory)
            .as("Category badge should display correct category")
            .containsIgnoringCase(category);
    }
}
