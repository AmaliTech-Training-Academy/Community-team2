package com.communityboard.pages;

import com.communityboard.utils.WaitUtils;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.List;

/**
 * PostDetailPage - Page Object for individual post detail view with comments
 * URL: /post/{id} or /posts/{id}
 * 
 * Page Elements:
 * - Post title, content, author, timestamp, category
 * - Edit/delete buttons (for own posts)
 * - Comment section: input, submit, comment list
 * - Comment display: author, text, timestamp
 * 
 * Implements Single Responsibility: Handles ONLY post detail page UI interactions
 * NO assertions - only actions and element state queries
 */
public class PostDetailPage {

    private final WebDriver driver;
    private final WaitUtils waitUtils;

    // Post detail elements
    @FindBy(css = "[data-testid='post-detail-title']")
    private WebElement postTitle;
    
    @FindBy(css = "[data-testid='post-detail-content']")
    private WebElement postContent;
    
    @FindBy(css = "[data-testid='post-detail-author']")
    private WebElement postAuthor;
    
    @FindBy(css = "[data-testid='post-detail-timestamp']")
    private WebElement postTimestamp;
    
    @FindBy(css = "[data-testid='post-detail-category']")
    private WebElement postCategory;
    
    @FindBy(css = "[data-testid='post-detail-image']")
    private WebElement postImage;

    // Action buttons
    @FindBy(css = "[data-testid='post-edit-button']")
    private WebElement editButton;
    
    @FindBy(css = "[data-testid='post-delete-button']")
    private WebElement deleteButton;
    
    @FindBy(css = "[data-testid='back-button']")
    private WebElement backButton;


    // Delete confirmation dialog
    @FindBy(css = "[data-testid='confirm-delete-button']")
    private WebElement confirmDeleteButton;
    
    @FindBy(css = "[data-testid='cancel-delete-button']")
    private WebElement cancelDeleteButton;
    
    // Comment section elements
    
    // Comment input field
    @FindBy(css = "[data-testid='comment-input']")
    private WebElement commentInput;
    
    // Alternative comment input locators
    @FindBy(name = "comment")
    private WebElement commentInputByName;
    
    @FindBy(css = "textarea[placeholder*='comment' i]")
    private WebElement commentInputByPlaceholder;
    
    // Submit comment button
    @FindBy(css = "[data-testid='comment-submit-button']")
    private WebElement commentSubmitButton;
    
    // Alternative submit button locators
    @FindBy(xpath = "//button[contains(text(), 'Comment') or contains(text(), 'Post Comment') or contains(text(), 'Add Comment')]")
    private WebElement commentSubmitButtonByText;
    
    // List of all comments
    @FindBy(css = "[data-testid='comment-item']")
    private List<WebElement> commentItems;
    
    // Alternative comment list locator
    @FindBy(css = ".comment-item")
    private List<WebElement> commentItemsByClass;
    
    // Comment child elements (within each comment)
    @FindBy(css = "[data-testid='comment-author']")
    private List<WebElement> commentAuthors;
    
    @FindBy(css = "[data-testid='comment-text']")
    private List<WebElement> commentTexts;
    
    @FindBy(css = "[data-testid='comment-timestamp']")
    private List<WebElement> commentTimestamps;
    
    // Comment count display
    @FindBy(css = "[data-testid='comment-count']")
    private WebElement commentCount;
    
    // No comments message
    @FindBy(css = "[data-testid='no-comments-message']")
    private WebElement noCommentsMessage;
    
    // Comment error message
    @FindBy(css = "[data-testid='comment-error']")
    private WebElement commentError;

    /**
     * Constructor - Initializes page elements using PageFactory
     */
    public PostDetailPage(WebDriver driver) {
        this.driver = driver;
        this.waitUtils = new WaitUtils(driver);
        PageFactory.initElements(driver, this);
    }

    // Post detail methods
    
    @Step("Get post title")
    public String getPostTitle() {
        waitUtils.waitForElementVisible(postTitle);
        return postTitle.getText();
    }

    @Step("Get post content")
    public String getPostContent() {
        waitUtils.waitForElementVisible(postContent);
        return postContent.getText();
    }

    @Step("Get post author")
    public String getPostAuthor() {
        try {
            waitUtils.waitForElementVisible(postAuthor);
            return postAuthor.getText();
        } catch (Exception e) {
            return "";
        }
    }

    @Step("Get post category")
    public String getPostCategory() {
        try {
            waitUtils.waitForElementVisible(postCategory);
            return postCategory.getText();
        } catch (Exception e) {
            return "";
        }
    }

    public boolean isEditButtonVisible() {
        try {
            return editButton.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isDeleteButtonVisible() {
        try {
            return deleteButton.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    @Step("Click edit button")
    public CreatePostPage clickEditButton() {
        waitUtils.waitForElementClickable(editButton);
        editButton.click();
        return new CreatePostPage(driver);
    }

    @Step("Click delete button")
    public void clickDeleteButton() {
        waitUtils.waitForElementClickable(deleteButton);
        deleteButton.click();
        waitUtils.waitForElementVisible(confirmDeleteButton);
    }

    @Step("Confirm delete")
    public void confirmDelete() {
        waitUtils.waitForElementClickable(confirmDeleteButton);
        confirmDeleteButton.click();
    }

    @Step("Delete post")
    public void deletePost() {
        clickDeleteButton();
        confirmDelete();
    }

    // Comment methods
    
    /**
     * Enter comment text in input field
     * @param commentText Comment text to enter
     */
    @Step("Enter comment: {commentText}")
    public void enterComment(String commentText) {
        // Wait for comment input to be visible
        waitUtils.waitForElementVisible(commentInput);
        // Clear existing text
        commentInput.clear();
        // Enter comment text
        commentInput.sendKeys(commentText);
    }

    /**
     * Click submit comment button
     */
    @Step("Click submit comment button")
    public void clickSubmitCommentButton() {
        // Wait for button to be clickable
        waitUtils.waitForElementClickable(commentSubmitButton);
        // Click the button
        commentSubmitButton.click();
    }

    /**
     * Add comment to post
     * Convenience method combining enter and submit
     * @param commentText Comment text to add
     */
    @Step("Add comment: {commentText}")
    public void addComment(String commentText) {
        enterComment(commentText);
        clickSubmitCommentButton();
        // Wait briefly for comment to be added
        try {
            Thread.sleep(500); // Brief wait for comment to appear
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Get total count of comments
     * @return Number of comments displayed
     */
    @Step("Get comment count")
    public int getCommentCount() {
        try {
            // Wait for comments to be visible
            waitUtils.waitForElementVisible(By.cssSelector("[data-testid='comment-item']"));
            // Return count of comment items
            return commentItems.size();
        } catch (Exception e) {
            // No comments found
            return 0;
        }
    }

    /**
     * Get comment text by index
     * @param index Index of comment (0-based)
     * @return Comment text
     */
    public String getCommentTextByIndex(int index) {
        try {
            waitUtils.waitForElementVisible(By.cssSelector("[data-testid='comment-text']"));
            return commentTexts.get(index).getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get comment author by index
     * @param index Index of comment (0-based)
     * @return Comment author name
     */
    public String getCommentAuthorByIndex(int index) {
        try {
            waitUtils.waitForElementVisible(By.cssSelector("[data-testid='comment-author']"));
            return commentAuthors.get(index).getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get comment timestamp by index
     * @param index Index of comment (0-based)
     * @return Comment timestamp text
     */
    public String getCommentTimestampByIndex(int index) {
        try {
            waitUtils.waitForElementVisible(By.cssSelector("[data-testid='comment-timestamp']"));
            return commentTimestamps.get(index).getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Check if comment with specific text is displayed
     * @param commentText Comment text to search for
     * @return true if comment exists
     */
    @Step("Check if comment '{commentText}' is displayed")
    public boolean isCommentDisplayed(String commentText) {
        try {
            waitUtils.waitForElementVisible(By.cssSelector("[data-testid='comment-text']"));
            for (WebElement comment : commentTexts) {
                if (comment.getText().contains(commentText)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get comment count from display (e.g., "5 Comments")
     * @return Comment count text
     */
    public String getCommentCountDisplay() {
        try {
            waitUtils.waitForElementVisible(commentCount);
            return commentCount.getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Check if "no comments" message is displayed
     * @return true if no comments message is visible
     */
    public boolean isNoCommentsMessageDisplayed() {
        try {
            return noCommentsMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get comment error message
     * @return Error message text
     */
    public String getCommentError() {
        try {
            waitUtils.waitForElementVisible(commentError);
            return commentError.getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Check if comment error is displayed
     * @return true if error is visible
     */
    public boolean isCommentErrorDisplayed() {
        try {
            return commentError.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Clear comment input field
     */
    @Step("Clear comment input")
    public void clearCommentInput() {
        commentInput.clear();
    }

    public boolean isOnPostDetailPage() {
        String currentUrl = driver.getCurrentUrl();
        return currentUrl.contains("/post");
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    @Step("Wait for post detail page to load")
    public void waitForPageLoad() {
        waitUtils.waitForElementVisible(postTitle);
    }
}
