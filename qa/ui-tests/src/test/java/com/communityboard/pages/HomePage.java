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
 * HomePage (FeedPage) - Page Object for main feed/home page after login
 * URL: /home or /feed
 * 
 * Page Elements:
 * - Post cards (list of posts)
 * - Create Post button
 * - Post titles, content, categories, timestamps
 * - Author names
 * - Search bar
 * - Category filters
 * 
 * Implements Single Responsibility: Handles ONLY home/feed page UI interactions
 * NO assertions - only actions and element state queries
 */
public class HomePage {

    // WebDriver instance passed from test class
    private final WebDriver driver;
    
    // WaitUtils instance for explicit waits
    private final WaitUtils waitUtils;

    // Main page elements
    
    // Create Post button - opens create post modal/page
    @FindBy(css = "[data-testid='create-post-button']")
    private WebElement createPostButton;
    
    // Alternative create post button locators
    @FindBy(xpath = "//button[contains(text(), 'Create Post') or contains(text(), 'New Post')]")
    private WebElement createPostButtonByText;
    
    // List of all post cards in the feed
    @FindBy(css = "[data-testid='post-card']")
    private List<WebElement> postCards;
    
    // Alternative post card locator
    @FindBy(css = ".post-card")
    private List<WebElement> postCardsByClass;


    // Post card child elements (within each post card)
    
    // Post title within card
    @FindBy(css = "[data-testid='post-title']")
    private List<WebElement> postTitles;
    
    // Post content/excerpt within card
    @FindBy(css = "[data-testid='post-content']")
    private List<WebElement> postContents;
    
    // Post category badge within card
    @FindBy(css = "[data-testid='post-category']")
    private List<WebElement> postCategories;
    
    // Post author name within card
    @FindBy(css = "[data-testid='post-author']")
    private List<WebElement> postAuthors;
    
    // Post timestamp within card
    @FindBy(css = "[data-testid='post-timestamp']")
    private List<WebElement> postTimestamps;
    
    // Search and filter elements
    
    // Search input field
    @FindBy(css = "[data-testid='search-input']")
    private WebElement searchInput;
    
    // Search button
    @FindBy(css = "[data-testid='search-button']")
    private WebElement searchButton;
    
    // Category filter dropdown
    @FindBy(css = "[data-testid='category-filter']")
    private WebElement categoryFilter;
    
    // No results message
    @FindBy(css = "[data-testid='no-results-message']")
    private WebElement noResultsMessage;
    
    // Alternative no results locator
    @FindBy(xpath = "//*[contains(text(), 'No posts found') or contains(text(), 'No results')]")
    private WebElement noResultsMessageByText;

    /**
     * Constructor - Initializes page elements using PageFactory
     * @param driver WebDriver instance from test class
     */
    public HomePage(WebDriver driver) {
        this.driver = driver;
        this.waitUtils = new WaitUtils(driver);
        // PageFactory.initElements initializes all @FindBy annotated elements
        PageFactory.initElements(driver, this);
    }

    /**
     * Navigate directly to home/feed page
     * Note: Requires user to be logged in
     */
    @Step("Navigate to home page")
    public void navigateToHomePage() {
        driver.get("http://communityboard-alb-905603474.eu-west-1.elb.amazonaws.com/home");
    }

    /**
     * Click "Create Post" button to open create post modal/page
     * @return CreatePostPage instance for method chaining
     */
    @Step("Click 'Create Post' button")
    public CreatePostPage clickCreatePostButton() {
        // Wait for button to be clickable
        waitUtils.waitForElementClickable(createPostButton);
        // Click the button
        createPostButton.click();
        // Return new CreatePostPage instance
        return new CreatePostPage(driver);
    }

    /**
     * Get total count of posts displayed in feed
     * @return Number of post cards visible
     */
    @Step("Get post count")
    public int getPostCount() {
        try {
            // Wait for at least one post card to be visible
            waitUtils.waitForElementVisible(By.cssSelector("[data-testid='post-card']"));
            // Return count of post cards
            return postCards.size();
        } catch (Exception e) {
            // No posts found
            return 0;
        }
    }

    /**
     * Get post card element by index
     * @param index Index of post card (0-based)
     * @return WebElement of post card
     */
    public WebElement getPostCardByIndex(int index) {
        // Wait for post cards to be visible
        waitUtils.waitForElementVisible(By.cssSelector("[data-testid='post-card']"));
        // Return post card at specified index
        return postCards.get(index);
    }

    /**
     * Get post title by index
     * @param index Index of post (0-based)
     * @return Post title text
     */
    public String getPostTitleByIndex(int index) {
        try {
            waitUtils.waitForElementVisible(By.cssSelector("[data-testid='post-title']"));
            return postTitles.get(index).getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get post content by index
     * @param index Index of post (0-based)
     * @return Post content text
     */
    public String getPostContentByIndex(int index) {
        try {
            waitUtils.waitForElementVisible(By.cssSelector("[data-testid='post-content']"));
            return postContents.get(index).getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get post category by index
     * @param index Index of post (0-based)
     * @return Post category text (NEWS, EVENT, DISCUSSION, ALERT)
     */
    public String getPostCategoryByIndex(int index) {
        try {
            waitUtils.waitForElementVisible(By.cssSelector("[data-testid='post-category']"));
            return postCategories.get(index).getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get post author by index
     * @param index Index of post (0-based)
     * @return Post author name
     */
    public String getPostAuthorByIndex(int index) {
        try {
            waitUtils.waitForElementVisible(By.cssSelector("[data-testid='post-author']"));
            return postAuthors.get(index).getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get post timestamp by index
     * @param index Index of post (0-based)
     * @return Post timestamp text
     */
    public String getPostTimestampByIndex(int index) {
        try {
            waitUtils.waitForElementVisible(By.cssSelector("[data-testid='post-timestamp']"));
            return postTimestamps.get(index).getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Check if post with specific title exists in feed
     * @param title Post title to search for
     * @return true if post with title exists
     */
    @Step("Check if post with title '{title}' exists")
    public boolean isPostDisplayedByTitle(String title) {
        try {
            // Wait for post titles to be visible
            waitUtils.waitForElementVisible(By.cssSelector("[data-testid='post-title']"));
            // Check if any post title matches
            for (WebElement postTitle : postTitles) {
                if (postTitle.getText().equals(title)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Click on post card to view post details
     * @param index Index of post to click (0-based)
     * @return PostDetailPage instance for method chaining
     */
    @Step("Click on post at index {index}")
    public PostDetailPage clickPostByIndex(int index) {
        // Get post card at index
        WebElement postCard = getPostCardByIndex(index);
        // Wait for post card to be clickable
        waitUtils.waitForElementClickable(postCard);
        // Click the post card
        postCard.click();
        // Return new PostDetailPage instance
        return new PostDetailPage(driver);
    }

    /**
     * Click on post by title
     * @param title Post title to click
     * @return PostDetailPage instance for method chaining
     */
    @Step("Click on post with title '{title}'")
    public PostDetailPage clickPostByTitle(String title) {
        try {
            // Wait for post titles to be visible
            waitUtils.waitForElementVisible(By.cssSelector("[data-testid='post-title']"));
            // Find and click post with matching title
            for (WebElement postTitle : postTitles) {
                if (postTitle.getText().equals(title)) {
                    waitUtils.waitForElementClickable(postTitle);
                    postTitle.click();
                    return new PostDetailPage(driver);
                }
            }
            // If post not found, throw exception
            throw new RuntimeException("Post with title '" + title + "' not found");
        } catch (Exception e) {
            throw new RuntimeException("Failed to click post with title: " + title, e);
        }
    }

    /**
     * Search for posts by keyword
     * @param keyword Search keyword
     */
    @Step("Search for posts with keyword: {keyword}")
    public void searchPosts(String keyword) {
        // Wait for search input to be visible
        waitUtils.waitForElementVisible(searchInput);
        // Clear existing text
        searchInput.clear();
        // Enter search keyword
        searchInput.sendKeys(keyword);
        // Click search button
        waitUtils.waitForElementClickable(searchButton);
        searchButton.click();
        // Wait for search results to load
        try {
            Thread.sleep(1000); // Brief wait for results (replace with better wait condition)
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Check if "no results" message is displayed
     * @return true if no results message is visible
     */
    public boolean isNoResultsMessageDisplayed() {
        try {
            return noResultsMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if on home page
     * @return true if URL contains /home or /feed
     */
    public boolean isOnHomePage() {
        String currentUrl = driver.getCurrentUrl();
        return currentUrl.contains("/home") || currentUrl.contains("/feed");
    }

    /**
     * Get current page URL
     * @return Current URL
     */
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    /**
     * Refresh the page
     * Useful for verifying post updates
     */
    @Step("Refresh home page")
    public void refreshPage() {
        driver.navigate().refresh();
        // Wait for page to reload
        waitUtils.waitForElementVisible(createPostButton);
    }
}
