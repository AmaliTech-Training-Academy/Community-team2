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
 * ViewPostTests - Test class for viewing posts functionality
 * Covers test cases TC_POST_011 to TC_POST_016
 */
@Epic("Post Management")
@Feature("View Posts")
@Story("US4: View Posts")
public class ViewPostTests extends BaseTest {

    private HomePage homePage;
    private PostDetailPage postDetailPage;
    private CreatePostPage createPostPage;
    private String testPostTitle;
    private String testPostContent;
    private String testPostCategory;

    @BeforeEach
    public void setUpTest() {
        // Login and create a test post
        loginAndCreateTestPost();
        homePage = new HomePage(driver);
        homePage.navigateToHomePage();
    }

    private void loginAndCreateTestPost() {
        // Login logic (simplified)
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
        testPostContent = TestDataFactory.generatePostContent();
        testPostCategory = TestDataFactory.generateRandomCategory();
        createPostPage = homePage.clickCreatePostButton();
        createPostPage.submitPost(testPostTitle, testPostContent, testPostCategory);
        homePage.navigateToHomePage();
    }

    @Test
    @DisplayName("TC_POST_011: View post list in feed")
    @Description("User should be able to view list of posts in feed")
    @Severity(SeverityLevel.CRITICAL)
    public void testViewPostListInFeed() {
        int postCount = homePage.getPostCount();
        assertThat(postCount).as("Feed should contain posts").isGreaterThan(0);
    }

    @Test
    @DisplayName("TC_POST_012: Click post to view details")
    @Description("User should be able to click post card to view full details")
    @Severity(SeverityLevel.CRITICAL)
    public void testClickPostToViewDetails() {
        postDetailPage = homePage.clickPostByIndex(0);
        assertThat(postDetailPage.isOnPostDetailPage())
            .as("Should navigate to post detail page")
            .isTrue();
    }

    @Test
    @DisplayName("TC_POST_013: Verify post details display correctly")
    @Description("Post detail page should display title, content, author, category")
    @Severity(SeverityLevel.NORMAL)
    public void testPostDetailsDisplayCorrectly() {
        postDetailPage = homePage.clickPostByTitle(testPostTitle);
        
        String displayedTitle = postDetailPage.getPostTitle();
        assertThat(displayedTitle).as("Title should match").isEqualTo(testPostTitle);
        
        String displayedContent = postDetailPage.getPostContent();
        assertThat(displayedContent).as("Content should match").contains(testPostContent);
    }

    @Test
    @DisplayName("TC_POST_014: Verify timestamp format")
    @Description("Post timestamp should be displayed in readable format")
    @Severity(SeverityLevel.MINOR)
    public void testTimestampFormat() {
        String timestamp = homePage.getPostTimestampByIndex(0);
        assertThat(timestamp).as("Timestamp should be displayed").isNotEmpty();
    }

    @Test
    @DisplayName("TC_POST_015: Verify author name displays")
    @Description("Post author name should be displayed on post card")
    @Severity(SeverityLevel.NORMAL)
    public void testAuthorNameDisplays() {
        String author = homePage.getPostAuthorByIndex(0);
        assertThat(author).as("Author name should be displayed").isNotEmpty();
    }

    @Test
    @DisplayName("TC_POST_016: Verify category displays on post card")
    @Description("Post category badge should be visible on post card")
    @Severity(SeverityLevel.NORMAL)
    public void testCategoryDisplaysOnPostCard() {
        String category = homePage.getPostCategoryByIndex(0);
        assertThat(category).as("Category should be displayed").isNotEmpty();
    }
}
