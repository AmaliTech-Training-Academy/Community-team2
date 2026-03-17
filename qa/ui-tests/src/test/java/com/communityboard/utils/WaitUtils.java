package com.communityboard.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * WaitUtils - Utility class for explicit wait operations
 * Implements Single Responsibility Principle: Handles ONLY wait logic
 * Provides reusable wait methods to avoid Thread.sleep() anti-pattern
 * All methods use WebDriverWait with ExpectedConditions for reliability
 */
public class WaitUtils {

    // Default explicit wait timeout in seconds
    private static final int DEFAULT_WAIT_TIMEOUT = 15;

    // WebDriver instance for wait operations
    private final WebDriver driver;

    // WebDriverWait instance with default timeout
    private final WebDriverWait wait;

    /**
     * Constructor - Initializes WaitUtils with WebDriver instance
     * @param driver WebDriver instance from test class
     */
    public WaitUtils(WebDriver driver) {
        this.driver = driver;
        // Initialize WebDriverWait with default timeout duration
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_WAIT_TIMEOUT));
    }

    /**
     * Constructor with custom timeout
     * @param driver WebDriver instance from test class
     * @param timeoutInSeconds Custom timeout duration
     */
    public WaitUtils(WebDriver driver, int timeoutInSeconds) {
        this.driver = driver;
        // Initialize WebDriverWait with custom timeout duration
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
    }

    /**
     * Wait for element to be visible on the page
     * Element must be present in DOM AND have height/width > 0
     * @param locator By locator strategy (id, css, xpath, etc.)
     * @return WebElement once visible
     */
    public WebElement waitForElementVisible(By locator) {
        // ExpectedConditions.visibilityOfElementLocated checks both presence and visibility
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }


    /**
     * Wait for element to be visible (WebElement overload)
     * Useful when you already have WebElement reference
     * @param element WebElement to wait for
     * @return WebElement once visible
     */
    public WebElement waitForElementVisible(WebElement element) {
        // ExpectedConditions.visibilityOf checks visibility of already located element
        return wait.until(ExpectedConditions.visibilityOf(element));
    }

    /**
     * Wait for element to be clickable (visible AND enabled)
     * Best practice before performing click actions
     * @param locator By locator strategy
     * @return WebElement once clickable
     */
    public WebElement waitForElementClickable(By locator) {
        // ExpectedConditions.elementToBeClickable checks visibility + enabled state
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    /**
     * Wait for element to be clickable (WebElement overload)
     * @param element WebElement to wait for
     * @return WebElement once clickable
     */
    public WebElement waitForElementClickable(WebElement element) {
        return wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    /**
     * Wait for element to be present in DOM (not necessarily visible)
     * Useful for hidden elements or elements loaded via AJAX
     * @param locator By locator strategy
     * @return WebElement once present in DOM
     */
    public WebElement waitForElementPresent(By locator) {
        // ExpectedConditions.presenceOfElementLocated checks only DOM presence
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    /**
     * Wait for element to disappear from page
     * Useful for waiting for loading spinners, modals, or deleted elements
     * @param locator By locator strategy
     * @return true if element is invisible or not present
     */
    public boolean waitForElementToDisappear(By locator) {
        // ExpectedConditions.invisibilityOfElementLocated waits until element is not visible
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    /**
     * Wait for specific text to be present in element
     * Useful for dynamic content updates (e.g., success messages)
     * @param locator By locator strategy
     * @param text Expected text content
     * @return true if text is present in element
     */
    public boolean waitForTextToBePresentInElement(By locator, String text) {
        // ExpectedConditions.textToBePresentInElementLocated checks element's text content
        return wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }

    /**
     * Wait for element's attribute to contain specific value
     * Useful for checking dynamic attribute changes (e.g., class, disabled)
     * @param locator By locator strategy
     * @param attribute Attribute name (e.g., "class", "value")
     * @param value Expected attribute value
     * @return true if attribute contains value
     */
    public boolean waitForAttributeContains(By locator, String attribute, String value) {
        // ExpectedConditions.attributeContains checks if attribute contains substring
        return wait.until(ExpectedConditions.attributeContains(locator, attribute, value));
    }

    /**
     * Wait for URL to contain specific text
     * Useful for navigation verification after login/redirect
     * @param urlFragment Expected URL fragment (e.g., "/dashboard")
     * @return true if URL contains fragment
     */
    public boolean waitForUrlContains(String urlFragment) {
        // ExpectedConditions.urlContains checks current URL
        return wait.until(ExpectedConditions.urlContains(urlFragment));
    }

    /**
     * Wait for URL to be exactly as expected
     * Stricter check than urlContains
     * @param url Expected full URL
     * @return true if URL matches exactly
     */
    public boolean waitForUrlToBe(String url) {
        // ExpectedConditions.urlToBe checks exact URL match
        return wait.until(ExpectedConditions.urlToBe(url));
    }

    /**
     * Wait for page title to contain specific text
     * Useful for page load verification
     * @param title Expected title text
     * @return true if title contains text
     */
    public boolean waitForTitleContains(String title) {
        // ExpectedConditions.titleContains checks page title
        return wait.until(ExpectedConditions.titleContains(title));
    }

    /**
     * Wait for alert to be present
     * Useful for handling JavaScript alerts/confirms
     * @return Alert object once present
     */
    public org.openqa.selenium.Alert waitForAlertPresent() {
        // ExpectedConditions.alertIsPresent waits for JavaScript alert
        return wait.until(ExpectedConditions.alertIsPresent());
    }

    /**
     * Custom wait with polling interval
     * For advanced scenarios requiring custom polling
     * @param locator By locator strategy
     * @param timeoutSeconds Custom timeout
     * @param pollingIntervalMillis Polling interval in milliseconds
     * @return WebElement once visible
     */
    public WebElement waitForElementWithPolling(By locator, int timeoutSeconds, long pollingIntervalMillis) {
        // Create custom WebDriverWait with polling interval
        WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        customWait.pollingEvery(Duration.ofMillis(pollingIntervalMillis));
        return customWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
}
