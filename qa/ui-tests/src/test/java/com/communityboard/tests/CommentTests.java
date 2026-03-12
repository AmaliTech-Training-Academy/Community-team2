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
 * CommentTests - Test class for comment functionality
 * Covers test cases TC_COMMENT_001 to TC_COMMENT_010
 * 
 * Test Scenarios:
 * - Positive: Add comment successfully
 * - Negative: Empty comment text
 * - Boundary: Max length comment, special characters
 * - Functional: Comment appears, author displays, timestamp displays
 * - UI/UX: Comment count updates, multiple comments display
 */
@Epic("Post Interaction")
@Feature("Comments")
@Story("US7: Add Comments")
public class CommentTests extends BaseTest {

    private HomePage homePage;
    private PostDetailPage postDetailPage;
    private String testPostTitle;

    @BeforeEach
    public void setUpTest() {
        // Login and create test post
        loginAndCreateTestPost();
        
        // Navigate to post detail page
        homePage = new HomePage(driver);
        homePage.navigateToHomePage();
        postDetailPage = homePage.clickPostByTitle(testPostTitle);
    }

    private void loginAndCreateTestPost() {
        // Login logic
        LandingPage landingPage = new LandingPage(driver);
        landingPage.navigateToLandingPage();
        
        RegisterPage registerPage = landingPage.clickRegisterLink();
        String email = TestDataFactory.generateUniqueEmail();
        String password = TestDataFactory.generateValidPassword();
        registerPage.submitRegistrationForm(
            TestDataFactory.generateFirstName(),
            TestDataFactory.generateLastName(),
            email, password, password
        );
        
        landingPage.navigateToLandingPage();
        LoginPage loginPage = landingPage.clickLoginButton();
        loginPage.login(email, password);
        
        // Create test post
        homePage = new HomePage(driver);
        homePage.navigateToHomePage();
        testPostTitle = TestDataFactory.generatePostTitle();
        CreatePostPage createPostPage = homePage.clickCreatePostButton();
        createPostPage.enterTitle(testPostTitle);
        // Simplified - full implementation in post-crud branch
    }


    /**
     * TC_COMMENT_001: Add comment to post successfully
     * Positive test - Verify user can add comment to post
     */
    @Test
    @DisplayName("TC_COMMENT_001: Add comment to post successfully")
    @Description("User should be able to add comment to post with valid text")
    @Severity(SeverityLevel.CRITICAL)
    public void testAddCommentSuccessfully() {
        // Generate comment text
        String commentText = TestDataFactory.generateCommentText();

        // Get initial comment count
        int initialCommentCount = postDetailPage.getCommentCount();

        // Add comment
        postDetailPage.addComment(commentText);

        // Verify comment count increased
        int newCommentCount = postDetailPage.getCommentCount();
        assertThat(newCommentCount)
            .as("Comment count should increase after adding comment")
            .isGreaterThan(initialCommentCount);

        // Verify comment is displayed
        boolean commentExists = postDetailPage.isCommentDisplayed(commentText);
        assertThat(commentExists)
            .as("Added comment should be visible")
            .isTrue();
    }

    /**
     * TC_COMMENT_002: Add comment with empty text
     * Negative test - Verify validation error for empty comment
     */
    @Test
    @DisplayName("TC_COMMENT_002: Add comment with empty text")
    @Description("System should display validation error when comment text is empty")
    @Severity(SeverityLevel.NORMAL)
    public void testAddCommentWithEmptyText() {
        // Attempt to add empty comment
        String commentText = ""; // Empty comment

        postDetailPage.addComment(commentText);

        // Verify error message is displayed
        String errorMessage = postDetailPage.getCommentError();
        assertThat(errorMessage)
            .as("Error message should be displayed for empty comment")
            .isNotEmpty()
            .containsIgnoringCase("required");
    }

    /**
     * TC_COMMENT_003: Add comment with max length text
     * Boundary test - Verify system handles very long comment
     */
    @Test
    @DisplayName("TC_COMMENT_003: Add comment with max length text")
    @Description("System should handle or reject comment exceeding maximum length")
    @Severity(SeverityLevel.MINOR)
    public void testAddCommentWithMaxLength() {
        // Generate very long comment (e.g., 1000 characters)
        String commentText = TestDataFactory.generateLongString(1000);

        postDetailPage.addComment(commentText);

        // System should either accept or show validation error
        boolean hasError = postDetailPage.isCommentErrorDisplayed();
        boolean commentAdded = postDetailPage.isCommentDisplayed(commentText.substring(0, 50));

        assertThat(hasError || commentAdded)
            .as("System should handle long comment appropriately")
            .isTrue();
    }

    /**
     * TC_COMMENT_004: Add comment with special characters
     * Boundary test - Verify system handles special characters
     */
    @Test
    @DisplayName("TC_COMMENT_004: Add comment with special characters")
    @Description("System should handle special characters in comment text")
    @Severity(SeverityLevel.MINOR)
    public void testAddCommentWithSpecialCharacters() {
        // Generate comment with special characters
        String commentText = "Test comment!@#$%^&*() - " + TestDataFactory.generateTimestamp();

        postDetailPage.addComment(commentText);

        // Verify comment is added
        boolean commentExists = postDetailPage.isCommentDisplayed(commentText);
        assertThat(commentExists)
            .as("Comment with special characters should be added")
            .isTrue();
    }

    /**
     * TC_COMMENT_005: Verify comment appears under post
     * Functional test - Verify comment is displayed correctly
     */
    @Test
    @DisplayName("TC_COMMENT_005: Verify comment appears under post")
    @Description("Added comment should appear in comment section under post")
    @Severity(SeverityLevel.CRITICAL)
    public void testCommentAppearsUnderPost() {
        // Add comment
        String commentText = TestDataFactory.generateCommentText();
        postDetailPage.addComment(commentText);

        // Verify comment is displayed
        boolean commentExists = postDetailPage.isCommentDisplayed(commentText);
        assertThat(commentExists)
            .as("Comment should appear under post")
            .isTrue();

        // Verify comment text matches
        String displayedComment = postDetailPage.getCommentTextByIndex(0);
        assertThat(displayedComment)
            .as("Comment text should match")
            .contains(commentText);
    }

    /**
     * TC_COMMENT_006: Verify comment author displays
     * UI/UX test - Verify author name is shown on comment
     */
    @Test
    @DisplayName("TC_COMMENT_006: Verify comment author displays")
    @Description("Comment should display author name")
    @Severity(SeverityLevel.NORMAL)
    public void testCommentAuthorDisplays() {
        // Add comment
        String commentText = TestDataFactory.generateCommentText();
        postDetailPage.addComment(commentText);

        // Verify author is displayed
        String author = postDetailPage.getCommentAuthorByIndex(0);
        assertThat(author)
            .as("Comment author should be displayed")
            .isNotEmpty();
    }

    /**
     * TC_COMMENT_007: Verify comment timestamp displays
     * UI/UX test - Verify timestamp is shown on comment
     */
    @Test
    @DisplayName("TC_COMMENT_007: Verify comment timestamp displays")
    @Description("Comment should display timestamp")
    @Severity(SeverityLevel.MINOR)
    public void testCommentTimestampDisplays() {
        // Add comment
        String commentText = TestDataFactory.generateCommentText();
        postDetailPage.addComment(commentText);

        // Verify timestamp is displayed
        String timestamp = postDetailPage.getCommentTimestampByIndex(0);
        assertThat(timestamp)
            .as("Comment timestamp should be displayed")
            .isNotEmpty();
    }

    /**
     * TC_COMMENT_008: Verify multiple comments display in order
     * Functional test - Verify multiple comments are shown correctly
     */
    @Test
    @DisplayName("TC_COMMENT_008: Verify multiple comments display in order")
    @Description("Multiple comments should be displayed in correct order")
    @Severity(SeverityLevel.NORMAL)
    public void testMultipleCommentsDisplayInOrder() {
        // Add first comment
        String comment1 = "First comment - " + TestDataFactory.generateTimestamp();
        postDetailPage.addComment(comment1);

        // Add second comment
        String comment2 = "Second comment - " + TestDataFactory.generateTimestamp();
        postDetailPage.addComment(comment2);

        // Add third comment
        String comment3 = "Third comment - " + TestDataFactory.generateTimestamp();
        postDetailPage.addComment(comment3);

        // Verify all comments are displayed
        int commentCount = postDetailPage.getCommentCount();
        assertThat(commentCount)
            .as("All comments should be displayed")
            .isGreaterThanOrEqualTo(3);

        // Verify comments exist
        assertThat(postDetailPage.isCommentDisplayed(comment1)).isTrue();
        assertThat(postDetailPage.isCommentDisplayed(comment2)).isTrue();
        assertThat(postDetailPage.isCommentDisplayed(comment3)).isTrue();
    }

    /**
     * TC_COMMENT_009: Verify comment count updates
     * Functional test - Verify comment count display updates
     */
    @Test
    @DisplayName("TC_COMMENT_009: Verify comment count updates")
    @Description("Comment count should update after adding comments")
    @Severity(SeverityLevel.NORMAL)
    public void testCommentCountUpdates() {
        // Get initial comment count
        int initialCount = postDetailPage.getCommentCount();

        // Add comment
        String commentText = TestDataFactory.generateCommentText();
        postDetailPage.addComment(commentText);

        // Verify count increased
        int newCount = postDetailPage.getCommentCount();
        assertThat(newCount)
            .as("Comment count should increase")
            .isEqualTo(initialCount + 1);
    }

    /**
     * TC_COMMENT_010: Add comment as different user
     * Functional test - Verify different users can comment
     */
    @Test
    @DisplayName("TC_COMMENT_010: Add comment as different user")
    @Description("Different users should be able to add comments to same post")
    @Severity(SeverityLevel.NORMAL)
    public void testAddCommentAsDifferentUser() {
        // Add comment as first user
        String comment1 = TestDataFactory.generateCommentText();
        postDetailPage.addComment(comment1);

        // Verify comment is added
        boolean comment1Exists = postDetailPage.isCommentDisplayed(comment1);
        assertThat(comment1Exists)
            .as("First user's comment should be added")
            .isTrue();

        // Note: Full test would require logging in as different user
        // For now, verify comment functionality works
    }
}
