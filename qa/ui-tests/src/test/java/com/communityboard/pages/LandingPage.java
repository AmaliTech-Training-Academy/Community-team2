package com.communityboard.pages;

import com.communityboard.utils.WaitUtils;
import io.qameta.allure.Step;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/**
 * LandingPage - Page Object for the root URL landing page
 * Represents the public welcome page before authentication
 * URL: http://communityboard-alb-905603474.eu-west-1.elb.amazonaws.com/
 * 
 * Page Elements:
 * - Header with "Ping – Neighborhood Community"
 * - Welcome message
 * - "Log In" button
 * - "Create one now" registration link
 * 
 * Implements Single Responsibility: Handles ONLY landing page UI interactions
 * NO assertions - only actions and element state queries
 */
public class LandingPage {

    // WebDriver instance passed from test class
    private final WebDriver driver;
    
    // WaitUtils instance for explicit waits
    private final WaitUtils waitUtils;

    // Page element locators using @FindBy annotations (Page Factory pattern)
    
    // Header text element - "Ping – Neighborhood Community"
    @FindBy(css = "[data-testid='landing-header']")
    private WebElement headerText;
    
    // Welcome message - "Welcome back" or "Sign in to your neighborhood community"
    @FindBy(css = "[data-testid='welcome-message']")
    private WebElement welcomeMessage;
    
    // Log In button - navigates to /login page
    @FindBy(css = "[data-testid='login-button']")
    private WebElement loginButton;
    
    // Create account link - "Don't have an account? Create one now"
    @FindBy(css = "[data-testid='register-link']")
    private WebElement registerLink;
    
    // Alternative locators if data-testid not available
    // Log In button by text
    @FindBy(xpath = "//button[contains(text(), 'Log In')]")
    private WebElement loginButtonByText;
    
    // Register link by text
    @FindBy(xpath = "//a[contains(text(), 'Create one now')]")
    private WebElement registerLinkByText;

    /**
     * Constructor - Initializes page elements using PageFactory
     * @param driver WebDriver instance from test class
     */
    public LandingPage(WebDriver driver) {
        this.driver = driver;
        this.waitUtils = new WaitUtils(driver);
        // PageFactory.initElements initializes all @FindBy annotated elements
        PageFactory.initElements(driver, this);
    }

    /**
     * Navigate to landing page
     * @Step annotation adds this action to Allure report
     */
    @Step("Navigate to landing page")
    public void navigateToLandingPage() {
        // Navigate to base URL
        driver.get("http://communityboard-alb-905603474.eu-west-1.elb.amazonaws.com/");
    }


    /**
     * Click "Log In" button to navigate to login page
     * Waits for button to be clickable before clicking
     * @return LoginPage instance for method chaining
     */
    @Step("Click 'Log In' button")
    public LoginPage clickLoginButton() {
        // Wait for login button to be clickable (visible + enabled)
        waitUtils.waitForElementClickable(loginButton);
        // Click the button
        loginButton.click();
        // Return new LoginPage instance for fluent interface pattern
        return new LoginPage(driver);
    }

    /**
     * Click "Create one now" link to navigate to registration page
     * Waits for link to be clickable before clicking
     * @return RegisterPage instance for method chaining
     */
    @Step("Click 'Create one now' registration link")
    public RegisterPage clickRegisterLink() {
        // Wait for register link to be clickable
        waitUtils.waitForElementClickable(registerLink);
        // Click the link
        registerLink.click();
        // Return new RegisterPage instance for fluent interface pattern
        return new RegisterPage(driver);
    }

    /**
     * Get header text content
     * @return Header text (e.g., "Ping – Neighborhood Community")
     */
    @Step("Get landing page header text")
    public String getHeaderText() {
        // Wait for header to be visible before getting text
        waitUtils.waitForElementVisible(headerText);
        // Return text content
        return headerText.getText();
    }

    /**
     * Get welcome message text
     * @return Welcome message text
     */
    @Step("Get welcome message")
    public String getWelcomeMessage() {
        // Wait for welcome message to be visible
        waitUtils.waitForElementVisible(welcomeMessage);
        // Return text content
        return welcomeMessage.getText();
    }

    /**
     * Check if landing page is displayed
     * Verifies presence of key elements (header and login button)
     * @return true if landing page is displayed, false otherwise
     */
    @Step("Verify landing page is displayed")
    public boolean isLandingPageDisplayed() {
        try {
            // Wait for header to be visible (key element of landing page)
            waitUtils.waitForElementVisible(headerText);
            // Check if login button is also visible
            return loginButton.isDisplayed();
        } catch (Exception e) {
            // If any element not found, page is not displayed
            return false;
        }
    }

    /**
     * Check if login button is displayed
     * @return true if login button is visible
     */
    public boolean isLoginButtonDisplayed() {
        try {
            return loginButton.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if register link is displayed
     * @return true if register link is visible
     */
    public boolean isRegisterLinkDisplayed() {
        try {
            return registerLink.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get current page URL
     * Useful for navigation verification
     * @return Current URL
     */
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
