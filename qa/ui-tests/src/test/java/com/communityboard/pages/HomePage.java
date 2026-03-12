package com.communityboard.pages;

import com.communityboard.utils.WaitUtils;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

/**
 * HomePage (FeedPage) - Page Object for main feed with search and filter
 * URL: /home or /feed
 * 
 * Enhanced with:
 * - Advanced search functionality
 * - Category filtering
 * - Date filtering
 * - Clear filters option
 * 
 * Implements Single Responsibility: Handles ONLY home/feed page UI interactions
 */
public class HomePage {

    private final WebDriver driver;
    private final WaitUtils waitUtils;

    // Main page elements
    @FindBy(css = "[data-testid='create-post-button']")
    private WebElement createPostButton;
    
    @FindBy(css = "[data-testid='post-card']")
    private List<WebElement> postCards;
    
    @FindBy(css = "[data-testid='post-title']")
    private List<WebElement> postTitles;
    
    @FindBy(css = "[data-testid='post-content']")
    private List<WebElement> postContents;
    
    @FindBy(css = "[data-testid='post-category']")
    private List<WebElement> postCategories;
    
    @FindBy(css = "[data-testid='post-author']")
    private List<WebElement> postAuthors;


    // Search and filter elements
    
    // Search input field
    @FindBy(css = "[data-testid='search-input']")
    private WebElement searchInput;
    
    // Alternative search input locators
    @FindBy(name = "search")
    private WebElement searchInputByName;
    
    @FindBy(css = "input[placeholder*='search' i]")
    private WebElement searchInputByPlaceholder;
    
    // Search button
    @FindBy(css = "[data-testid='search-button']")
    private WebElement searchButton;
    
    // Alternative search button locator
    @FindBy(xpath = "//button[contains(text(), 'Search')]")
    private WebElement searchButtonByText;
    
    // Category filter dropdown
    @FindBy(css = "[data-testid='category-filter']")
    private WebElement categoryFilter;
    
    // Alternative category filter locators
    @FindBy(name = "category")
    private WebElement categoryFilterByName;
    
    @FindBy(css = "select[aria-label*='category' i]")
    private WebElement categoryFilterByAriaLabel;
    
    // Date filter input
    @FindBy(css = "[data-testid='date-filter']")
    private WebElement dateFilter;
    
    // Date range filters
    @FindBy(css = "[data-testid='start-date-filter']")
    private WebElement startDateFilter;
    
    @FindBy(css = "[data-testid='end-date-filter']")
    private WebElement endDateFilter;
    
    // Clear filters button
    @FindBy(css = "[data-testid='clear-filters-button']")
    private WebElement clearFiltersButton;
    
    // Alternative clear filters locator
    @FindBy(xpath = "//button[contains(text(), 'Clear') or contains(text(), 'Reset')]")
    private WebElement clearFiltersButtonByText;
    
    // Filter results count
    @FindBy(css = "[data-testid='results-count']")
    private WebElement resultsCount;
    
    // No results message
    @FindBy(css = "[data-testid='no-results-message']")
    private WebElement noResultsMessage;
    
    // Alternative no results locator
    @FindBy(xpath = "//*[contains(text(), 'No posts found') or contains(text(), 'No results')]")
    private WebElement noResultsMessageByText;
    
    // Loading indicator
    @FindBy(css = "[data-testid='loading-indicator']")
    private WebElement loadingIndicator;

    /**
     * Constructor - Initializes page elements using PageFactory
     */
    public HomePage(WebDriver driver) {
        this.driver = driver;
        this.waitUtils = new WaitUtils(driver);
        PageFactory.initElements(driver, this);
    }

    @Step("Navigate to home page")
    public void navigateToHomePage() {
        driver.get("http://communityboard-alb-905603474.eu-west-1.elb.amazonaws.com/home");
    }

    @Step("Click 'Create Post' button")
    public CreatePostPage clickCreatePostButton() {
        waitUtils.waitForElementClickable(createPostButton);
        createPostButton.click();
        return new CreatePostPage(driver);
    }

    @Step("Get post count")
    public int getPostCount() {
        try {
            waitUtils.waitForElementVisible(By.cssSelector("[data-testid='post-card']"));
            return postCards.size();
        } catch (Exception e) {
            return 0;
        }
    }

    public String getPostTitleByIndex(int index) {
        try {
            waitUtils.waitForElementVisible(By.cssSelector("[data-testid='post-title']"));
            return postTitles.get(index).getText();
        } catch (Exception e) {
            return "";
        }
    }

    public String getPostCategoryByIndex(int index) {
        try {
            waitUtils.waitForElementVisible(By.cssSelector("[data-testid='post-category']"));
            return postCategories.get(index).getText();
        } catch (Exception e) {
            return "";
        }
    }

    @Step("Check if post with title '{title}' exists")
    public boolean isPostDisplayedByTitle(String title) {
        try {
            waitUtils.waitForElementVisible(By.cssSelector("[data-testid='post-title']"));
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

    @Step("Click on post with title '{title}'")
    public PostDetailPage clickPostByTitle(String title) {
        try {
            waitUtils.waitForElementVisible(By.cssSelector("[data-testid='post-title']"));
            for (WebElement postTitle : postTitles) {
                if (postTitle.getText().equals(title)) {
                    waitUtils.waitForElementClickable(postTitle);
                    postTitle.click();
                    return new PostDetailPage(driver);
                }
            }
            throw new RuntimeException("Post with title '" + title + "' not found");
        } catch (Exception e) {
            throw new RuntimeException("Failed to click post with title: " + title, e);
        }
    }

    @Step("Click on post at index {index}")
    public PostDetailPage clickPostByIndex(int index) {
        WebElement postCard = postCards.get(index);
        waitUtils.waitForElementClickable(postCard);
        postCard.click();
        return new PostDetailPage(driver);
    }

    // Search methods
    
    /**
     * Enter search keyword in search input
     * @param keyword Search keyword
     */
    @Step("Enter search keyword: {keyword}")
    public void enterSearchKeyword(String keyword) {
        // Wait for search input to be visible
        waitUtils.waitForElementVisible(searchInput);
        // Clear existing text
        searchInput.clear();
        // Enter search keyword
        searchInput.sendKeys(keyword);
    }

    /**
     * Click search button
     */
    @Step("Click search button")
    public void clickSearchButton() {
        // Wait for button to be clickable
        waitUtils.waitForElementClickable(searchButton);
        // Click the button
        searchButton.click();
        // Wait for search results to load
        waitForSearchResults();
    }

    /**
     * Search for posts by keyword
     * Convenience method combining enter and click
     * @param keyword Search keyword
     */
    @Step("Search for posts with keyword: {keyword}")
    public void searchPosts(String keyword) {
        enterSearchKeyword(keyword);
        clickSearchButton();
    }

    /**
     * Clear search input
     */
    @Step("Clear search input")
    public void clearSearchInput() {
        searchInput.clear();
    }

    // Filter methods
    
    /**
     * Filter posts by category
     * @param category Category name (NEWS, EVENT, DISCUSSION, ALERT, or "All")
     */
    @Step("Filter by category: {category}")
    public void filterByCategory(String category) {
        // Wait for category filter to be visible
        waitUtils.waitForElementVisible(categoryFilter);
        // Create Select object for dropdown
        Select select = new Select(categoryFilter);
        // Select by visible text
        select.selectByVisibleText(category);
        // Wait for filtered results to load
        waitForSearchResults();
    }

    /**
     * Filter posts by date
     * @param date Date string (format depends on implementation)
     */
    @Step("Filter by date: {date}")
    public void filterByDate(String date) {
        try {
            // Wait for date filter to be visible
            waitUtils.waitForElementVisible(dateFilter);
            // Clear existing date
            dateFilter.clear();
            // Enter date
            dateFilter.sendKeys(date);
            // Wait for filtered results
            waitForSearchResults();
        } catch (Exception e) {
            // Date filter may not be implemented
            System.out.println("Date filter not found or not functional");
        }
    }

    /**
     * Filter posts by date range
     * @param startDate Start date
     * @param endDate End date
     */
    @Step("Filter by date range: {startDate} to {endDate}")
    public void filterByDateRange(String startDate, String endDate) {
        try {
            // Enter start date
            waitUtils.waitForElementVisible(startDateFilter);
            startDateFilter.clear();
            startDateFilter.sendKeys(startDate);
            
            // Enter end date
            waitUtils.waitForElementVisible(endDateFilter);
            endDateFilter.clear();
            endDateFilter.sendKeys(endDate);
            
            // Wait for filtered results
            waitForSearchResults();
        } catch (Exception e) {
            System.out.println("Date range filters not found");
        }
    }

    /**
     * Click clear filters button
     */
    @Step("Clear all filters")
    public void clearFilters() {
        try {
            // Wait for clear button to be clickable
            waitUtils.waitForElementClickable(clearFiltersButton);
            // Click the button
            clearFiltersButton.click();
            // Wait for results to reload
            waitForSearchResults();
        } catch (Exception e) {
            // Clear button may not be present
            System.out.println("Clear filters button not found");
        }
    }

    /**
     * Combine search and category filter
     * @param keyword Search keyword
     * @param category Category to filter
     */
    @Step("Search '{keyword}' and filter by category '{category}'")
    public void searchAndFilterByCategory(String keyword, String category) {
        searchPosts(keyword);
        filterByCategory(category);
    }

    /**
     * Combine search and date filter
     * @param keyword Search keyword
     * @param date Date to filter
     */
    @Step("Search '{keyword}' and filter by date '{date}'")
    public void searchAndFilterByDate(String keyword, String date) {
        searchPosts(keyword);
        filterByDate(date);
    }

    // Results methods
    
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
     * Get results count text
     * @return Results count display text (e.g., "10 results")
     */
    public String getResultsCount() {
        try {
            waitUtils.waitForElementVisible(resultsCount);
            return resultsCount.getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get filtered post count
     * @return Number of posts after filtering
     */
    @Step("Get filtered post count")
    public int getFilteredPostCount() {
        return getPostCount();
    }

    /**
     * Wait for search/filter results to load
     * Waits for loading indicator to disappear or brief delay
     */
    private void waitForSearchResults() {
        try {
            // Try to wait for loading indicator to disappear
            waitUtils.waitForElementToDisappear(By.cssSelector("[data-testid='loading-indicator']"));
        } catch (Exception e) {
            // If no loading indicator, just wait briefly
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public boolean isOnHomePage() {
        String currentUrl = driver.getCurrentUrl();
        return currentUrl.contains("/home") || currentUrl.contains("/feed");
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    @Step("Refresh home page")
    public void refreshPage() {
        driver.navigate().refresh();
        waitUtils.waitForElementVisible(createPostButton);
    }
}
