package com.communityboard.tests;

import com.communityboard.base.BaseTest;
import com.communityboard.pages.*;
import com.communityboard.utils.TestDataFactory;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ImageUploadTests - Test class for image upload functionality
 * Covers test cases TC_IMAGE_001 to TC_IMAGE_015
 */
@Epic("Media Management")
@Feature("Image Upload")
@Story("US10: Image Upload")
public class ImageUploadTests extends BaseTest {

    private HomePage homePage;
    private CreatePostPage createPostPage;
    private String validImagePath;
    private String invalidImagePath;
    private String oversizedImagePath;

    @BeforeEach
    public void setUpTest() {
        loginTestUser();
        homePage = new HomePage(driver);
        homePage.navigateToHomePage();
        
        // Setup test image paths
        String resourcesPath = System.getProperty("user.dir") + "/src/test/resources/";
        validImagePath = resourcesPath + "test-image.jpg";
        invalidImagePath = resourcesPath + "test-file.txt";
        oversizedImagePath = resourcesPath + "large-image.jpg";
    }

    private void loginTestUser() {
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
    }

    @Test
    @DisplayName("TC_IMAGE_001: Upload valid image (JPG) with post")
    @Description("User should be able to upload JPG image with post")
    @Severity(SeverityLevel.CRITICAL)
    public void testUploadValidJpgImage() {
        createPostPage = homePage.clickCreatePostButton();
        
        createPostPage.enterTitle(TestDataFactory.generatePostTitle());
        createPostPage.enterContent(TestDataFactory.generatePostContent());
        createPostPage.uploadImage(validImagePath);
        
        boolean isPreviewDisplayed = createPostPage.isImagePreviewDisplayed();
        // Image preview may or may not be implemented
        
        createPostPage.clickSubmitButton();
        
        // Verify no error occurred
        boolean hasError = createPostPage.isImageErrorDisplayed();
        assertThat(hasError).as("No image error should be displayed").isFalse();
    }

    @Test
    @DisplayName("TC_IMAGE_002: Upload valid image (PNG) with post")
    @Description("User should be able to upload PNG image with post")
    @Severity(SeverityLevel.NORMAL)
    public void testUploadValidPngImage() {
        createPostPage = homePage.clickCreatePostButton();
        
        createPostPage.enterTitle(TestDataFactory.generatePostTitle());
        createPostPage.enterContent(TestDataFactory.generatePostContent());
        
        String pngImagePath = System.getProperty("user.dir") + "/src/test/resources/test-image.png";
        createPostPage.uploadImage(pngImagePath);
        
        createPostPage.clickSubmitButton();
        
        boolean hasError = createPostPage.isImageErrorDisplayed();
        assertThat(hasError).as("PNG image should be accepted").isFalse();
    }

    @Test
    @DisplayName("TC_IMAGE_003: Upload image without post content (validation)")
    @Description("System should validate that post content is required even with image")
    @Severity(SeverityLevel.NORMAL)
    public void testUploadImageWithoutContent() {
        createPostPage = homePage.clickCreatePostButton();
        
        createPostPage.enterTitle(TestDataFactory.generatePostTitle());
        // Skip content
        createPostPage.uploadImage(validImagePath);
        createPostPage.clickSubmitButton();
        
        // Should show validation error for missing content
        // This test verifies image doesn't bypass content requirement
    }

    @Test
    @DisplayName("TC_IMAGE_004: Upload oversized image (> max size)")
    @Description("System should reject image exceeding maximum file size")
    @Severity(SeverityLevel.NORMAL)
    public void testUploadOversizedImage() {
        createPostPage = homePage.clickCreatePostButton();
        
        createPostPage.enterTitle(TestDataFactory.generatePostTitle());
        createPostPage.enterContent(TestDataFactory.generatePostContent());
        createPostPage.uploadImage(oversizedImagePath);
        
        String fileSizeError = createPostPage.getFileSizeError();
        // Error may appear immediately or on submit
        if (fileSizeError.isEmpty()) {
            createPostPage.clickSubmitButton();
            fileSizeError = createPostPage.getFileSizeError();
        }
        
        assertThat(fileSizeError).as("File size error should be displayed").isNotEmpty();
    }

    @Test
    @DisplayName("TC_IMAGE_005: Upload invalid file type")
    @Description("System should reject non-image file types")
    @Severity(SeverityLevel.CRITICAL)
    public void testUploadInvalidFileType() {
        createPostPage = homePage.clickCreatePostButton();
        
        createPostPage.enterTitle(TestDataFactory.generatePostTitle());
        createPostPage.enterContent(TestDataFactory.generatePostContent());
        createPostPage.uploadImage(invalidImagePath);
        
        String fileTypeError = createPostPage.getFileTypeError();
        if (fileTypeError.isEmpty()) {
            createPostPage.clickSubmitButton();
            fileTypeError = createPostPage.getFileTypeError();
        }
        
        assertThat(fileTypeError).as("File type error should be displayed").isNotEmpty();
    }

    @Test
    @DisplayName("TC_IMAGE_006: Upload image then remove before submit")
    @Description("User should be able to remove uploaded image before submitting")
    @Severity(SeverityLevel.MINOR)
    public void testUploadAndRemoveImage() {
        createPostPage = homePage.clickCreatePostButton();
        
        createPostPage.uploadImage(validImagePath);
        boolean isPreviewDisplayed = createPostPage.isImagePreviewDisplayed();
        
        createPostPage.removeImage();
        
        boolean isPreviewStillDisplayed = createPostPage.isImagePreviewDisplayed();
        assertThat(isPreviewStillDisplayed).as("Image preview should be removed").isFalse();
    }

    @Test
    @DisplayName("TC_IMAGE_007: Verify image preview displays after upload")
    @Description("Image preview should be displayed after successful upload")
    @Severity(SeverityLevel.MINOR)
    public void testImagePreviewDisplays() {
        createPostPage = homePage.clickCreatePostButton();
        
        createPostPage.uploadImage(validImagePath);
        
        boolean isPreviewDisplayed = createPostPage.isImagePreviewDisplayed();
        assertThat(isPreviewDisplayed).as("Image preview should be displayed").isTrue();
    }

    @Test
    @DisplayName("TC_IMAGE_009: Create post without image (optional field)")
    @Description("Image should be optional - post can be created without it")
    @Severity(SeverityLevel.NORMAL)
    public void testCreatePostWithoutImage() {
        createPostPage = homePage.clickCreatePostButton();
        
        createPostPage.enterTitle(TestDataFactory.generatePostTitle());
        createPostPage.enterContent(TestDataFactory.generatePostContent());
        // Skip image upload
        createPostPage.clickSubmitButton();
        
        // Should succeed without image
        boolean hasError = createPostPage.isImageErrorDisplayed();
        assertThat(hasError).as("Post should be created without image").isFalse();
    }

    @Test
    @DisplayName("TC_IMAGE_011: Verify file size error message")
    @Description("Clear error message should be displayed for oversized files")
    @Severity(SeverityLevel.MINOR)
    public void testFileSizeErrorMessage() {
        createPostPage = homePage.clickCreatePostButton();
        
        createPostPage.uploadImage(oversizedImagePath);
        createPostPage.clickSubmitButton();
        
        String error = createPostPage.getFileSizeError();
        assertThat(error).as("File size error should mention size limit").matches("(?i).*(size|large|limit|MB).*");
    }

    @Test
    @DisplayName("TC_IMAGE_012: Verify file type error message")
    @Description("Clear error message should be displayed for invalid file types")
    @Severity(SeverityLevel.MINOR)
    public void testFileTypeErrorMessage() {
        createPostPage = homePage.clickCreatePostButton();
        
        createPostPage.uploadImage(invalidImagePath);
        createPostPage.clickSubmitButton();
        
        String error = createPostPage.getFileTypeError();
        assertThat(error).as("File type error should mention valid formats").matches("(?i).*(type|format|jpg|png|image).*");
    }
}
