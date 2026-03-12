package com.communityboard.pages;

import com.communityboard.utils.WaitUtils;
import io.qameta.allure.Step;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/**
 * LoginPage - Page Object for user login page
 * URL: /login
 * 
 * Page Elements:
 * - Email input field
 * - Password input field
 * - Login button
 * - Error message display
 * - Forgot password link
 * - Register link
 * 
 * Implements Single Responsibility: Handles ONLY login page UI interactions
 * NO assertions - only actions and element state queries
 */
public class LoginPage {

    // WebDriver instance passed from test class
    private final WebDriver driver;
    
    // WaitUtils instance for explicit waits
    private final WaitUtils waitUtils;

    // Form input field locators using data-testid attributes
    
    // Email input field
    @FindBy(css = "[data-testid='login-email-input']")
    private WebElement emailInput;
    
    // Password input field
    @FindBy(css = "[data-testid='login-password-input']")
    private WebElement passwordInput;
    
    // Login button
    @FindBy(css = "[data-testid='login-submit-button']")
    private WebElement loginButton;
    
    // Alternative locators by name attribute (fallback)
    @FindBy(name = "email")
    private WebElement emailInputByName;
    
    @FindBy(name = "password")
    private WebElement passwordInputByName;
    
    // Alternative login button by text
    @FindBy(xpath = "//button[contains(text(), 'Log In') or contains(text(), 'Login')]")
    private WebElement loginButtonByText;


    // Error message locators
    
    // Generic error message container
    @FindBy(css = "[data-testid='login-error-message']")
    private WebElement errorMessage;
    
    // Field-specific error messages
    @FindBy(css = "[data-testid='email-error']")
    private WebElement emailError;
    
    @FindBy(css = "[data-testid='password-error']")
    private WebElement passwordError;
    
    // Alternative error locator by class
    @FindBy(css = ".error-message")
    private WebElement errorMessageByClass;
    
    // Forgot password link
    @FindBy(css = "[data-testid='forgot-password-link']")
    private WebElement forgotPasswordLink;
    
    // Alternative forgot password link by text
    @FindBy(xpath = "//a[contains(text(), 'Forgot') or contains(text(), 'forgot')]")
    private WebElement forgotPasswordLinkByText;
    
    // Register link (if present on login page)
    @FindBy(css = "[data-testid='register-link']")
    private WebElement registerLink;
    
    // Password visibility toggle (eye icon)
    @FindBy(css = "[data-testid='password-visibility-toggle']")
    private WebElement passwordVisibilityToggle;

    /**
     * Constructor - Initializes page elements using PageFactory
     * @param driver WebDriver instance from test class
     */
    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.waitUtils = new WaitUtils(driver);
        // PageFactory.initElements initializes all @FindBy annotated elements
        PageFactory.initElements(driver, this);
    }

    /**
     * Navigate directly to login page
     */
    @Step("Navigate to login page")
    public void navigateToLoginPage() {
        driver.get("http://communityboard-alb-905603474.eu-west-1.elb.amazonaws.com/login");
    }

    /**
     * Enter email in the input field
     * @param email Email address to enter
     */
    @Step("Enter email: {email}")
    public void enterEmail(String email) {
        // Wait for input field to be visible
        waitUtils.waitForElementVisible(emailInput);
        // Clear any existing text
        emailInput.clear();
        // Enter the email
        emailInput.sendKeys(email);
    }

    /**
     * Enter password in the input field
     * @param password Password to enter
     */
    @Step("Enter password")
    public void enterPassword(String password) {
        // Wait for input field to be visible
        waitUtils.waitForElementVisible(passwordInput);
        // Clear any existing text
        passwordInput.clear();
        // Enter the password (not logged in Allure for security)
        passwordInput.sendKeys(password);
    }

    /**
     * Click login button
     */
    @Step("Click login button")
    public void clickLoginButton() {
        // Wait for button to be clickable
        waitUtils.waitForElementClickable(loginButton);
        // Click the button
        loginButton.click();
    }

    /**
     * Perform complete login action
     * Convenience method combining email, password entry and button click
     * @param email Email address
     * @param password Password
     */
    @Step("Login with email: {email}")
    public void login(String email, String password) {
        // Enter email
        enterEmail(email);
        // Enter password
        enterPassword(password);
        // Click login button
        clickLoginButton();
    }

    /**
     * Get error message text
     * @return Error message text, or empty string if not displayed
     */
    @Step("Get error message")
    public String getErrorMessage() {
        try {
            // Wait for error message to appear (shorter timeout for negative tests)
            waitUtils.waitForElementVisible(errorMessage);
            return errorMessage.getText();
        } catch (Exception e) {
            // If error message not found, return empty string
            return "";
        }
    }

    /**
     * Get email field error message
     * @return Email error text
     */
    public String getEmailError() {
        try {
            waitUtils.waitForElementVisible(emailError);
            return emailError.getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get password field error message
     * @return Password error text
     */
    public String getPasswordError() {
        try {
            waitUtils.waitForElementVisible(passwordError);
            return passwordError.getText();
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
     * Check if login button is enabled
     * @return true if button is enabled
     */
    public boolean isLoginButtonEnabled() {
        try {
            waitUtils.waitForElementVisible(loginButton);
            return loginButton.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if forgot password link is displayed
     * @return true if link is visible
     */
    public boolean isForgotPasswordLinkDisplayed() {
        try {
            return forgotPasswordLink.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Click forgot password link
     */
    @Step("Click forgot password link")
    public void clickForgotPasswordLink() {
        waitUtils.waitForElementClickable(forgotPasswordLink);
        forgotPasswordLink.click();
    }

    /**
     * Toggle password visibility (show/hide password)
     */
    @Step("Toggle password visibility")
    public void togglePasswordVisibility() {
        try {
            waitUtils.waitForElementClickable(passwordVisibilityToggle);
            passwordVisibilityToggle.click();
        } catch (Exception e) {
            // Password toggle may not be present on all implementations
            System.out.println("Password visibility toggle not found");
        }
    }

    /**
     * Check if password field is masked (type="password")
     * @return true if password is masked
     */
    public boolean isPasswordMasked() {
        try {
            String inputType = passwordInput.getAttribute("type");
            return "password".equals(inputType);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Clear all form fields
     * Useful for test cleanup or retry scenarios
     */
    @Step("Clear login form")
    public void clearForm() {
        emailInput.clear();
        passwordInput.clear();
    }

    /**
     * Get current page URL
     * @return Current URL
     */
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    /**
     * Check if on login page
     * @return true if URL contains /login
     */
    public boolean isOnLoginPage() {
        return driver.getCurrentUrl().contains("/login");
    }

    /**
     * Wait for redirect after successful login
     * Waits for URL to change from /login (indicates successful login)
     * @param timeoutSeconds Maximum time to wait
     * @return true if redirected away from login page
     */
    @Step("Wait for login redirect")
    public boolean waitForLoginRedirect(int timeoutSeconds) {
        try {
            // Create custom wait with specified timeout
            WaitUtils customWait = new WaitUtils(driver, timeoutSeconds);
            // Wait for URL to NOT contain /login
            return customWait.waitForUrlContains("/home") || 
                   customWait.waitForUrlContains("/feed") ||
                   customWait.waitForUrlContains("/dashboard");
        } catch (Exception e) {
            return false;
        }
    }
}
