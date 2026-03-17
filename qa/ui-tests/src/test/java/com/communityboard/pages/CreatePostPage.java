package com.communityboard.pages;

import com.communityboard.utils.WaitUtils;
import io.qameta.allure.Step;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

/**
 * CreatePostPage (or PostModal) - Page Object for create/edit post form
 * Can be a modal dialog or separate page
 * 
 * Page Elements:
 * - Title input field
 * - Content textarea
 * - Category dropdown
 * - Image upload input
 * - Submit button
 * - Cancel button
 * - Validation error messages
 * 
 * Implements Single Responsibility: Handles ONLY create post form UI interactions
 * NO assertions - only actions and element state queries
 */
public class CreatePostPage {

    // WebDriver instance passed from test class
    private final WebDriver driver;
    
    // WaitUtils instance for explicit waits
    private final WaitUtils waitUtils;

    // Form input elements
    
    // Post title input field
    @FindBy(css = "[data-testid='post-title-input']")
    private WebElement titleInput;
    
    // Post content textarea
    @FindBy(css = "[data-testid='post-content-input']")
    private WebElement contentInput;
    
    // Category dropdown/select
    @FindBy(css = "[data-testid='post-category-select']")
    private WebElement categorySelect;
    
    // Image upload input
    @FindBy(css = "[data-testid='post-image-upload']")
    private WebElement imageUploadInput;
    
    // Alternative locators by name attribute
    @FindBy(name = "title")
    private WebElement titleInputByName;
    
    @FindBy(name = "content")
    private WebElement contentInputByName;
    
    @FindBy(name = "category")
    private WebElement categorySelectByName;


    // Button elements
    
    // Submit/Create Post button
    @FindBy(css = "[data-testid='post-submit-button']")
    private WebElement submitButton;
    
    // Cancel button
    @FindBy(css = "[data-testid='post-cancel-button']")
    private WebElement cancelButton;
    
    // Alternative button locators by text
    @FindBy(xpath = "//button[contains(text(), 'Submit') or contains(text(), 'Create') or contains(text(), 'Post')]")
    private WebElement submitButtonByText;
    
    @FindBy(xpath = "//button[contains(text(), 'Cancel')]")
    private WebElement cancelButtonByText;
    
    // Error message elements
    
    // Generic error message
    @FindBy(css = "[data-testid='post-error-message']")
    private WebElement errorMessage;
    
    // Field-specific error messages
    @FindBy(css = "[data-testid='title-error']")
    private WebElement titleError;
    
    @FindBy(css = "[data-testid='content-error']")
    private WebElement contentError;
    
    @FindBy(css = "[data-testid='category-error']")
    private WebElement categoryError;
    
    // Success message
    @FindBy(css = "[data-testid='post-success-message']")
    private WebElement successMessage;
    
    // Image preview element
    @FindBy(css = "[data-testid='image-preview']")
    private WebElement imagePreview;
    
    // Remove image button
    @FindBy(css = "[data-testid='remove-image-button']")
    private WebElement removeImageButton;

    /**
     * Constructor - Initializes page elements using PageFactory
     * @param driver WebDriver instance from test class
     */
    public CreatePostPage(WebDriver driver) {
        this.driver = driver;
        this.waitUtils = new WaitUtils(driver);
        // PageFactory.initElements initializes all @FindBy annotated elements
        PageFactory.initElements(driver, this);
    }

    /**
     * Enter post title
     * @param title Post title text
     */
    @Step("Enter post title: {title}")
    public void enterTitle(String title) {
        // Wait for title input to be visible
        waitUtils.waitForElementVisible(titleInput);
        // Clear existing text
        titleInput.clear();
        // Enter title
        titleInput.sendKeys(title);
    }

    /**
     * Enter post content
     * @param content Post content text
     */
    @Step("Enter post content")
    public void enterContent(String content) {
        // Wait for content input to be visible
        waitUtils.waitForElementVisible(contentInput);
        // Clear existing text
        contentInput.clear();
        // Enter content
        contentInput.sendKeys(content);
    }

    /**
     * Select post category from dropdown
     * @param category Category name (NEWS, EVENT, DISCUSSION, ALERT)
     */
    @Step("Select category: {category}")
    public void selectCategory(String category) {
        // Wait for category select to be visible
        waitUtils.waitForElementVisible(categorySelect);
        // Create Select object for dropdown interaction
        Select select = new Select(categorySelect);
        // Select by visible text
        select.selectByVisibleText(category);
    }

    /**
     * Upload image file
     * @param imagePath Absolute path to image file
     */
    @Step("Upload image: {imagePath}")
    public void uploadImage(String imagePath) {
        // Wait for image upload input to be present (may be hidden)
        waitUtils.waitForElementPresent(org.openqa.selenium.By.cssSelector("[data-testid='post-image-upload']"));
        // Send file path to input (works even if input is hidden)
        imageUploadInput.sendKeys(imagePath);
    }

    /**
     * Click submit button to create post
     */
    @Step("Click submit button")
    public void clickSubmitButton() {
        // Wait for button to be clickable
        waitUtils.waitForElementClickable(submitButton);
        // Click the button
        submitButton.click();
    }

    /**
     * Click cancel button to close form
     */
    @Step("Click cancel button")
    public void clickCancelButton() {
        // Wait for button to be clickable
        waitUtils.waitForElementClickable(cancelButton);
        // Click the button
        cancelButton.click();
    }

    /**
     * Fill complete post form (without image)
     * Convenience method for creating posts
     * @param title Post title
     * @param content Post content
     * @param category Post category
     */
    @Step("Fill post form: title={title}, category={category}")
    public void fillPostForm(String title, String content, String category) {
        enterTitle(title);
        enterContent(content);
        selectCategory(category);
    }

    /**
     * Fill complete post form with image
     * @param title Post title
     * @param content Post content
     * @param category Post category
     * @param imagePath Path to image file
     */
    @Step("Fill post form with image: title={title}, category={category}")
    public void fillPostFormWithImage(String title, String content, String category, String imagePath) {
        enterTitle(title);
        enterContent(content);
        selectCategory(category);
        uploadImage(imagePath);
    }

    /**
     * Submit post form
     * Combines form filling and submission
     * @param title Post title
     * @param content Post content
     * @param category Post category
     */
    @Step("Submit post form")
    public void submitPost(String title, String content, String category) {
        fillPostForm(title, content, category);
        clickSubmitButton();
    }

    /**
     * Submit post form with image
     * @param title Post title
     * @param content Post content
     * @param category Post category
     * @param imagePath Path to image file
     */
    @Step("Submit post with image")
    public void submitPostWithImage(String title, String content, String category, String imagePath) {
        fillPostFormWithImage(title, content, category, imagePath);
        clickSubmitButton();
    }

    /**
     * Get generic error message
     * @return Error message text
     */
    @Step("Get error message")
    public String getErrorMessage() {
        try {
            waitUtils.waitForElementVisible(errorMessage);
            return errorMessage.getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get title field error message
     * @return Title error text
     */
    public String getTitleError() {
        try {
            waitUtils.waitForElementVisible(titleError);
            return titleError.getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get content field error message
     * @return Content error text
     */
    public String getContentError() {
        try {
            waitUtils.waitForElementVisible(contentError);
            return contentError.getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get category field error message
     * @return Category error text
     */
    public String getCategoryError() {
        try {
            waitUtils.waitForElementVisible(categoryError);
            return categoryError.getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get success message
     * @return Success message text
     */
    @Step("Get success message")
    public String getSuccessMessage() {
        try {
            waitUtils.waitForElementVisible(successMessage);
            return successMessage.getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Check if error message is displayed
     * @return true if error message is visible
     */
    public boolean isErrorMessageDisplayed() {
        try {
            return errorMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if success message is displayed
     * @return true if success message is visible
     */
    public boolean isSuccessMessageDisplayed() {
        try {
            return successMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if image preview is displayed
     * @return true if image preview is visible
     */
    public boolean isImagePreviewDisplayed() {
        try {
            return imagePreview.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Remove uploaded image
     */
    @Step("Remove uploaded image")
    public void removeImage() {
        try {
            waitUtils.waitForElementClickable(removeImageButton);
            removeImageButton.click();
        } catch (Exception e) {
            // Remove button may not be present
            System.out.println("Remove image button not found");
        }
    }

    /**
     * Check if submit button is enabled
     * @return true if button is enabled
     */
    public boolean isSubmitButtonEnabled() {
        try {
            waitUtils.waitForElementVisible(submitButton);
            return submitButton.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Clear all form fields
     */
    @Step("Clear post form")
    public void clearForm() {
        titleInput.clear();
        contentInput.clear();
    }

    /**
     * Get current page URL
     * @return Current URL
     */
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
