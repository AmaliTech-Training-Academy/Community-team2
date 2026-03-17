package com.communityboard.pages;

import com.communityboard.utils.WaitUtils;
import io.qameta.allure.Step;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/**
 * PostDetailPage - Page Object for individual post detail view
 * URL: /post/{id} or /posts/{id}
 * 
 * Page Elements:
 * - Post title
 * - Post content (full text)
 * - Post author
 * - Post timestamp
 * - Post category badge
 * - Edit button (for own posts)
 * - Delete button (for own posts)
 * - Comment section
 * - Back/Return button
 * 
 * Implements Single Responsibility: Handles ONLY post detail page UI interactions
 * NO assertions - only actions and element state queries
 */
public class PostDetailPage {

    // WebDriver instance passed from test class
    private final WebDriver driver;
    
    // WaitUtils instance for explicit waits
    private final WaitUtils waitUtils;

    // Post detail elements
    
    // Post title
    @FindBy(css = "[data-testid='post-detail-title']")
    private WebElement postTitle;
    
    // Post content (full text)
    @FindBy(css = "[data-testid='post-detail-content']")
    private WebElement postContent;
    
    // Post author name
    @FindBy(css = "[data-testid='post-detail-author']")
    private WebElement postAuthor;
    
    // Post timestamp
    @FindBy(css = "[data-testid='post-detail-timestamp']")
    private WebElement postTimestamp;
    
    // Post category badge
    @FindBy(css = "[data-testid='post-detail-category']")
    private WebElement postCategory;
    
    // Post image (if present)
    @FindBy(css = "[data-testid='post-detail-image']")
    private WebElement postImage;


    // Action buttons
    
    // Edit button (visible only for own posts)
    @FindBy(css = "[data-testid='post-edit-button']")
    private WebElement editButton;
    
    // Delete button (visible only for own posts)
    @FindBy(css = "[data-testid='post-delete-button']")
    private WebElement deleteButton;
    
    // Alternative button locators by text
    @FindBy(xpath = "//button[contains(text(), 'Edit')]")
    private WebElement editButtonByText;
    
    @FindBy(xpath = "//button[contains(text(), 'Delete')]")
    private WebElement deleteButtonByText;
    
    // Back/Return button
    @FindBy(css = "[data-testid='back-button']")
    private WebElement backButton;
    
    // Delete confirmation dialog elements
    
    // Confirm delete button in dialog
    @FindBy(css = "[data-testid='confirm-delete-button']")
    private WebElement confirmDeleteButton;
    
    // Cancel delete button in dialog
    @FindBy(css = "[data-testid='cancel-delete-button']")
    private WebElement cancelDeleteButton;
    
    // Alternative confirmation dialog locators
    @FindBy(xpath = "//button[contains(text(), 'Confirm') or contains(text(), 'Yes')]")
    private WebElement confirmDeleteButtonByText;
    
    @FindBy(xpath = "//button[contains(text(), 'Cancel') or contains(text(), 'No')]")
    private WebElement cancelDeleteButtonByText;
    
    // Comment section elements (basic - will be expanded in comment tests)
    
    // Comment input field
    @FindBy(css = "[data-testid='comment-input']")
    private WebElement commentInput;
    
    // Submit comment button
    @FindBy(css = "[data-testid='comment-submit-button']")
    private WebElement commentSubmitButton;

    /**
     * Constructor - Initializes page elements using PageFactory
     * @param driver WebDriver instance from test class
     */
    public PostDetailPage(WebDriver driver) {
        this.driver = driver;
        this.waitUtils = new WaitUtils(driver);
        // PageFactory.initElements initializes all @FindBy annotated elements
        PageFactory.initElements(driver, this);
    }

    /**
     * Get post title text
     * @return Post title
     */
    @Step("Get post title")
    public String getPostTitle() {
        // Wait for title to be visible
        waitUtils.waitForElementVisible(postTitle);
        return postTitle.getText();
    }

    /**
     * Get post content text
     * @return Post content (full text)
     */
    @Step("Get post content")
    public String getPostContent() {
        // Wait for content to be visible
        waitUtils.waitForElementVisible(postContent);
        return postContent.getText();
    }

    /**
     * Get post author name
     * @return Author name
     */
    @Step("Get post author")
    public String getPostAuthor() {
        try {
            waitUtils.waitForElementVisible(postAuthor);
            return postAuthor.getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get post timestamp
     * @return Timestamp text
     */
    @Step("Get post timestamp")
    public String getPostTimestamp() {
        try {
            waitUtils.waitForElementVisible(postTimestamp);
            return postTimestamp.getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get post category
     * @return Category text (NEWS, EVENT, DISCUSSION, ALERT)
     */
    @Step("Get post category")
    public String getPostCategory() {
        try {
            waitUtils.waitForElementVisible(postCategory);
            return postCategory.getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Check if edit button is visible
     * Edit button should only be visible for own posts
     * @return true if edit button is displayed
     */
    public boolean isEditButtonVisible() {
        try {
            return editButton.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if delete button is visible
     * Delete button should only be visible for own posts
     * @return true if delete button is displayed
     */
    public boolean isDeleteButtonVisible() {
        try {
            return deleteButton.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Click edit button to edit post
     * @return CreatePostPage instance (edit mode) for method chaining
     */
    @Step("Click edit button")
    public CreatePostPage clickEditButton() {
        // Wait for edit button to be clickable
        waitUtils.waitForElementClickable(editButton);
        // Click the button
        editButton.click();
        // Return CreatePostPage instance (same form used for editing)
        return new CreatePostPage(driver);
    }

    /**
     * Click delete button to delete post
     * Opens confirmation dialog
     */
    @Step("Click delete button")
    public void clickDeleteButton() {
        // Wait for delete button to be clickable
        waitUtils.waitForElementClickable(deleteButton);
        // Click the button
        deleteButton.click();
        // Wait for confirmation dialog to appear
        waitUtils.waitForElementVisible(confirmDeleteButton);
    }

    /**
     * Confirm delete action in confirmation dialog
     */
    @Step("Confirm delete")
    public void confirmDelete() {
        // Wait for confirm button to be clickable
        waitUtils.waitForElementClickable(confirmDeleteButton);
        // Click confirm button
        confirmDeleteButton.click();
    }

    /**
     * Cancel delete action in confirmation dialog
     */
    @Step("Cancel delete")
    public void cancelDelete() {
        // Wait for cancel button to be clickable
        waitUtils.waitForElementClickable(cancelDeleteButton);
        // Click cancel button
        cancelDeleteButton.click();
    }

    /**
     * Delete post (click delete button and confirm)
     * Convenience method combining delete click and confirmation
     */
    @Step("Delete post")
    public void deletePost() {
        clickDeleteButton();
        confirmDelete();
    }

    /**
     * Click back button to return to feed
     * @return HomePage instance for method chaining
     */
    @Step("Click back button")
    public HomePage clickBackButton() {
        try {
            waitUtils.waitForElementClickable(backButton);
            backButton.click();
            return new HomePage(driver);
        } catch (Exception e) {
            // If back button not found, use browser back
            driver.navigate().back();
            return new HomePage(driver);
        }
    }

    /**
     * Check if post image is displayed
     * @return true if image is visible
     */
    public boolean isPostImageDisplayed() {
        try {
            return postImage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if on post detail page
     * @return true if URL contains /post or /posts
     */
    public boolean isOnPostDetailPage() {
        String currentUrl = driver.getCurrentUrl();
        return currentUrl.contains("/post");
    }

    /**
     * Get current page URL
     * @return Current URL
     */
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    /**
     * Wait for post detail page to load
     * Waits for post title to be visible
     */
    @Step("Wait for post detail page to load")
    public void waitForPageLoad() {
        waitUtils.waitForElementVisible(postTitle);
    }
}
