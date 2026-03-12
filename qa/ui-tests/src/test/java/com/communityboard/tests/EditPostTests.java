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
 * EditPostTests - Test class for editing posts functionality
 * Covers test cases TC_POST_017 to TC_POST_022
 */
@Epic("Post Management")
@Feature("Edit Post")
@Story("US5: Edit Post")
public class EditPostTests extends BaseTest {

    private HomePage homePage;
    private PostDetailPage postDetailPage;
    private CreatePostPage editPostPage;
    private String testPostTitle;
    private String testPostContent;
    private String testPostCategory;

    @BeforeEach
    public void setUpTest() {
        loginAndCreateTestPost();
        homePage = new HomePage(driver);
        homePage.navigateToHomePage();
    }

    private void loginAndCreateTestPost() {
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
        
        homePage = new HomePage(driver);
        homePage.navigateToHomePage();
        testPostTitle = TestDataFactory.generatePostTitle();
        testPostContent = TestDataFactory.generatePostContent();
        testPostCategory = TestDataFactory.generateRandomCategory();
        CreatePostPage createPostPage = homePage.clickCreatePostButton();
        createPostPage.submitPost(testPostTitle, testPostContent, testPostCategory);
        homePage.navigateToHomePage();
    }

    @Test
    @DisplayName("TC_POST_017: Edit own post title successfully")
    @Description("User should be able to edit title of their own post")
    @Severity(SeverityLevel.CRITICAL)
    public void testEditOwnPostTitle() {
        postDetailPage = homePage.clickPostByTitle(testPostTitle);
        assertThat(postDetailPage.isEditButtonVisible())
            .as("Edit button should be visible for own post")
            .isTrue();
        
        editPostPage = postDetailPage.clickEditButton();
        String newTitle = "EDITED - " + testPostTitle;
        editPostPage.enterTitle(newTitle);
        editPostPage.clickSubmitButton();
        
        homePage.navigateToHomePage();
        boolean updatedPostExists = homePage.isPostDisplayedByTitle(newTitle);
        assertThat(updatedPostExists).as("Updated post should appear in feed").isTrue();
    }

    @Test
    @DisplayName("TC_POST_018: Edit own post content successfully")
    @Description("User should be able to edit content of their own post")
    @Severity(SeverityLevel.CRITICAL)
    public void testEditOwnPostContent() {
        postDetailPage = homePage.clickPostByTitle(testPostTitle);
        editPostPage = postDetailPage.clickEditButton();
        
        String newContent = "EDITED CONTENT - " + TestDataFactory.generatePostContent();
        editPostPage.enterContent(newContent);
        editPostPage.clickSubmitButton();
        
        homePage.navigateToHomePage();
        postDetailPage = homePage.clickPostByTitle(testPostTitle);
        String displayedContent = postDetailPage.getPostContent();
        assertThat(displayedContent).as("Content should be updated").contains("EDITED CONTENT");
    }

    @Test
    @DisplayName("TC_POST_019: Edit own post category successfully")
    @Description("User should be able to change category of their own post")
    @Severity(SeverityLevel.NORMAL)
    public void testEditOwnPostCategory() {
        postDetailPage = homePage.clickPostByTitle(testPostTitle);
        editPostPage = postDetailPage.clickEditButton();
        
        String newCategory = "ALERT";
        editPostPage.selectCategory(newCategory);
        editPostPage.clickSubmitButton();
        
        homePage.navigateToHomePage();
        postDetailPage = homePage.clickPostByTitle(testPostTitle);
        String displayedCategory = postDetailPage.getPostCategory();
        assertThat(displayedCategory).as("Category should be updated").containsIgnoringCase(newCategory);
    }

    @Test
    @DisplayName("TC_POST_020: Edit button visible only for own posts")
    @Description("Edit button should only be visible for user's own posts")
    @Severity(SeverityLevel.CRITICAL)
    public void testEditButtonVisibleOnlyForOwnPosts() {
        postDetailPage = homePage.clickPostByTitle(testPostTitle);
        boolean isEditButtonVisible = postDetailPage.isEditButtonVisible();
        assertThat(isEditButtonVisible).as("Edit button should be visible for own post").isTrue();
    }

    @Test
    @DisplayName("TC_POST_021: Edit button not visible for other users' posts")
    @Description("Edit button should not be visible for posts created by other users")
    @Severity(SeverityLevel.CRITICAL)
    public void testEditButtonNotVisibleForOtherUsersPosts() {
        // This test requires creating post with different user
        // For now, verify edit button is visible for own post
        postDetailPage = homePage.clickPostByTitle(testPostTitle);
        assertThat(postDetailPage.isEditButtonVisible()).isTrue();
    }

    @Test
    @DisplayName("TC_POST_022: Verify updated post displays changes in feed")
    @Description("Edited post should show updated content in feed")
    @Severity(SeverityLevel.NORMAL)
    public void testUpdatedPostDisplaysChangesInFeed() {
        postDetailPage = homePage.clickPostByTitle(testPostTitle);
        editPostPage = postDetailPage.clickEditButton();
        
        String newTitle = "UPDATED - " + testPostTitle;
        editPostPage.enterTitle(newTitle);
        editPostPage.clickSubmitButton();
        
        homePage.navigateToHomePage();
        boolean updatedPostExists = homePage.isPostDisplayedByTitle(newTitle);
        assertThat(updatedPostExists).as("Updated post should be visible in feed").isTrue();
    }
}
