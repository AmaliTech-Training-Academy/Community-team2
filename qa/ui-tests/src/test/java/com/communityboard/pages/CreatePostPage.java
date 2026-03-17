package com.communityboard.pages;

import com.communityboard.utils.WaitUtils;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/**
 * CreatePostPage - Enhanced with image upload focus
 * Handles post creation with comprehensive image upload functionality
 */
public class CreatePostPage {
    private final WebDriver driver;
    private final WaitUtils waitUtils;

    @FindBy(css = "[data-testid='post-title-input']")
    private WebElement titleInput;
    
    @FindBy(css = "[data-testid='post-content-input']")
    private WebElement contentInput;
    
    @FindBy(css = "[data-testid='post-category-select']")
    private WebElement categorySelect;
    
    @FindBy(css = "[data-testid='post-image-upload']")
    private WebElement imageUploadInput;
    
    @FindBy(css = "[data-testid='post-submit-button']")
    private WebElement submitButton;
    
    @FindBy(css = "[data-testid='image-preview']")
    private WebElement imagePreview;
    
    @FindBy(css = "[data-testid='remove-image-button']")
    private WebElement removeImageButton;
    
    @FindBy(css = "[data-testid='image-error']")
    private WebElement imageError;
    
    @FindBy(css = "[data-testid='file-size-error']")
    private WebElement fileSizeError;
    
    @FindBy(css = "[data-testid='file-type-error']")
    private WebElement fileTypeError;

    public CreatePostPage(WebDriver driver) {
        this.driver = driver;
        this.waitUtils = new WaitUtils(driver);
        PageFactory.initElements(driver, this);
    }

    @Step("Enter title: {title}")
    public void enterTitle(String title) {
        waitUtils.waitForElementVisible(titleInput);
        titleInput.clear();
        titleInput.sendKeys(title);
    }

    @Step("Enter content")
    public void enterContent(String content) {
        waitUtils.waitForElementVisible(contentInput);
        contentInput.clear();
        contentInput.sendKeys(content);
    }

    @Step("Upload image: {imagePath}")
    public void uploadImage(String imagePath) {
        waitUtils.waitForElementPresent(By.cssSelector("[data-testid='post-image-upload']"));
        imageUploadInput.sendKeys(imagePath);
    }

    @Step("Remove image")
    public void removeImage() {
        try {
            waitUtils.waitForElementClickable(removeImageButton);
            removeImageButton.click();
        } catch (Exception e) {
            System.out.println("Remove image button not found");
        }
    }

    @Step("Click submit button")
    public void clickSubmitButton() {
        waitUtils.waitForElementClickable(submitButton);
        submitButton.click();
    }

    public boolean isImagePreviewDisplayed() {
        try {
            return imagePreview.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getImageError() {
        try {
            waitUtils.waitForElementVisible(imageError);
            return imageError.getText();
        } catch (Exception e) {
            return "";
        }
    }

    public String getFileSizeError() {
        try {
            waitUtils.waitForElementVisible(fileSizeError);
            return fileSizeError.getText();
        } catch (Exception e) {
            return "";
        }
    }

    public String getFileTypeError() {
        try {
            waitUtils.waitForElementVisible(fileTypeError);
            return fileTypeError.getText();
        } catch (Exception e) {
            return "";
        }
    }

    public boolean isImageErrorDisplayed() {
        try {
            return imageError.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}
