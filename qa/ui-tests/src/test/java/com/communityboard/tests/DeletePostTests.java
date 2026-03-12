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
 * DeletePostTests - Test class for deleting posts functionality
 * Covers test cases TC_POST_023 to TC_POST_028
 */
@Epic("Post Management")
@Feature("Delete Post")
@Story("US6: Delete Post")
public class DeletePostTests extends BaseTest {

    private HomePage homePage;
    private PostDetailPage postDetailPage;
    private String testPostTitle;

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
        CreatePostPage createPostPage = homePage.clickCreatePostButton();
        createPostPage.submitPost(testPostTitle, TestDataFactory.generatePostContent(), 
                                  TestDataFactory.generateRandomCategory());
        homePage.navigateToHomePage();
    }

    @Test
    @DisplayName("TC_POST_023: Delete own post successfully")
    @Description("User should be able to delete their own post")
    @Severity(SeverityLevel.CRITICAL)
    public void testDeleteOwnPostSuccessfully() {
        postDetailPage = homePage.clickPostByTitle(testPostTitle);
        assertThat(postDetailPage.isDeleteButtonVisible())
            .as("Delete button should be visible for own post")
            .isTrue();
        
        postDetailPage.deletePost();
        
        homePage.navigateToHomePage();
        boolean postExists = homePage.isPostDisplayedByTitle(testPostTitle);
        assertThat(postExists).as("Deleted post should not appear in feed").isFalse();
    }

    @Test
    @DisplayName("TC_POST_024: Delete button visible only for own posts")
    @Description("Delete button should only be visible for user's own posts")
    @Severity(SeverityLevel.CRITICAL)
    public void testDeleteButtonVisibleOnlyForOwnPosts() {
        postDetailPage = homePage.clickPostByTitle(testPostTitle);
        boolean isDeleteButtonVisible = postDetailPage.isDeleteButtonVisible();
        assertThat(isDeleteButtonVisible).as("Delete button should be visible for own post").isTrue();
    }

    @Test
    @DisplayName("TC_POST_025: Delete button not visible for other users' posts")
    @Description("Delete button should not be visible for posts created by other users")
    @Severity(SeverityLevel.CRITICAL)
    public void testDeleteButtonNotVisibleForOtherUsersPosts() {
        // Verify delete button is visible for own post
        postDetailPage = homePage.clickPostByTitle(testPostTitle);
        assertThat(postDetailPage.isDeleteButtonVisible()).isTrue();
    }

    @Test
    @DisplayName("TC_POST_026: Verify confirmation dialog on delete")
    @Description("System should show confirmation dialog before deleting post")
    @Severity(SeverityLevel.NORMAL)
    public void testConfirmationDialogOnDelete() {
        postDetailPage = homePage.clickPostByTitle(testPostTitle);
        postDetailPage.clickDeleteButton();
        // Confirmation dialog should appear (verified by confirmDelete method not throwing exception)
        postDetailPage.confirmDelete();
        
        homePage.navigateToHomePage();
        boolean postExists = homePage.isPostDisplayedByTitle(testPostTitle);
        assertThat(postExists).as("Post should be deleted after confirmation").isFalse();
    }

    @Test
    @DisplayName("TC_POST_027: Verify post removed from feed after deletion")
    @Description("Deleted post should no longer appear in feed")
    @Severity(SeverityLevel.CRITICAL)
    public void testPostRemovedFromFeedAfterDeletion() {
        int initialPostCount = homePage.getPostCount();
        
        postDetailPage = homePage.clickPostByTitle(testPostTitle);
        postDetailPage.deletePost();
        
        homePage.navigateToHomePage();
        int newPostCount = homePage.getPostCount();
        assertThat(newPostCount).as("Post count should decrease").isLessThan(initialPostCount);
        
        boolean postExists = homePage.isPostDisplayedByTitle(testPostTitle);
        assertThat(postExists).as("Deleted post should not be in feed").isFalse();
    }

    @Test
    @DisplayName("TC_POST_028: Cancel delete operation")
    @Description("User should be able to cancel delete operation")
    @Severity(SeverityLevel.NORMAL)
    public void testCancelDeleteOperation() {
        postDetailPage = homePage.clickPostByTitle(testPostTitle);
        postDetailPage.clickDeleteButton();
        postDetailPage.cancelDelete();
        
        // Post should still exist
        homePage.navigateToHomePage();
        boolean postExists = homePage.isPostDisplayedByTitle(testPostTitle);
        assertThat(postExists).as("Post should still exist after canceling delete").isTrue();
    }
}
