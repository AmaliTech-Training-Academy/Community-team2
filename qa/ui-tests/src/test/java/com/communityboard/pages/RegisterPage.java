package com.communityboard.pages;

import com.communityboard.utils.WaitUtils;
import io.qameta.allure.Step;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/**
 * RegisterPage - Page Object for user registration page
 * URL: /register
 * 
 * Page Elements:
 * - First Name input field
 * - Last Name input field
 * - Email input field
 * - Password input field
 * - Confirm Password input field
 * - Submit/Register button
 * - Validation error messages
 * - Success message
 * 
 * Implements Single Responsibility: Handles ONLY registration page UI interactions
 * NO assertions - only actions and element state queries
 */
public class RegisterPage {

    // WebDriver instance passed from test class
    private final WebDriver driver;
    
    // WaitUtils instance for explicit waits
    private final WaitUtils waitUtils;

    // Form input field locators using data-testid attributes
    
    // First Name input field
    @FindBy(css = "[data-testid='register-firstname-input']")
    private WebElement firstNameInput;
    
    // Last Name input field
    @FindBy(css = "[data-testid='register-lastname-input']")
    private WebElement lastNameInput;
    
    // Email input field
    @FindBy(css = "[data-testid='register-email-input']")
    private WebElement emailInput;
    
    // Password input field
    @FindBy(css = "[data-testid='register-password-input']")
    private WebElement passwordInput;
    
    // Confirm Password input field
    @FindBy(css = "[data-testid='register-confirm-password-input']")
    private WebElement confirmPasswordInput;
    
    // Submit/Register button
    @FindBy(css = "[data-testid='register-submit-button']")
    private WebElement submitButton;
    
    // Alternative locators by name attribute (fallback)
    @FindBy(name = "firstName")
    private WebElement firstNameInputByName;
    
    @FindBy(name = "lastName")
    private WebElement lastNameInputByName;
    
    @FindBy(name = "email")
    private WebElement emailInputByName;
    
    @FindBy(name = "password")
    private WebElement passwordInputByName;
    
    @FindBy(name = "confirmPassword")
    private WebElement confirmPasswordInputByName;


    // Error and success message locators
    
    // Generic error message container
    @FindBy(css = "[data-testid='register-error-message']")
    private WebElement errorMessage;
    
    // Field-specific error messages
    @FindBy(css = "[data-testid='firstname-error']")
    private WebElement firstNameError;
    
    @FindBy(css = "[data-testid='lastname-error']")
    private WebElement lastNameError;
    
    @FindBy(css = "[data-testid='email-error']")
    private WebElement emailError;
    
    @FindBy(css = "[data-testid='password-error']")
    private WebElement passwordError;
    
    @FindBy(css = "[data-testid='confirm-password-error']")
    private WebElement confirmPasswordError;
    
    // Success message after successful registration
    @FindBy(css = "[data-testid='register-success-message']")
    private WebElement successMessage;
    
    // Alternative error locators by class or xpath
    @FindBy(css = ".error-message")
    private WebElement errorMessageByClass;
    
    @FindBy(css = ".success-message")
    private WebElement successMessageByClass;
    
    // Required field indicators (asterisk or label)
    @FindBy(xpath = "//label[contains(text(), 'First Name')]/span[@class='required']")
    private WebElement firstNameRequiredIndicator;

    /**
     * Constructor - Initializes page elements using PageFactory
     * @param driver WebDriver instance from test class
     */
    public RegisterPage(WebDriver driver) {
        this.driver = driver;
        this.waitUtils = new WaitUtils(driver);
        // PageFactory.initElements initializes all @FindBy annotated elements
        PageFactory.initElements(driver, this);
    }

    /**
     * Navigate directly to registration page
     */
    @Step("Navigate to registration page")
    public void navigateToRegisterPage() {
        driver.get("http://communityboard-alb-905603474.eu-west-1.elb.amazonaws.com/register");
    }

    /**
     * Enter first name in the input field
     * @param firstName First name to enter
     */
    @Step("Enter first name: {firstName}")
    public void enterFirstName(String firstName) {
        // Wait for input field to be visible
        waitUtils.waitForElementVisible(firstNameInput);
        // Clear any existing text
        firstNameInput.clear();
        // Enter the first name
        firstNameInput.sendKeys(firstName);
    }

    /**
     * Enter last name in the input field
     * @param lastName Last name to enter
     */
    @Step("Enter last name: {lastName}")
    public void enterLastName(String lastName) {
        // Wait for input field to be visible
        waitUtils.waitForElementVisible(lastNameInput);
        // Clear any existing text
        lastNameInput.clear();
        // Enter the last name
        lastNameInput.sendKeys(lastName);
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
     * Enter confirm password in the input field
     * @param confirmPassword Confirm password to enter
     */
    @Step("Enter confirm password")
    public void enterConfirmPassword(String confirmPassword) {
        // Wait for input field to be visible
        waitUtils.waitForElementVisible(confirmPasswordInput);
        // Clear any existing text
        confirmPasswordInput.clear();
        // Enter the confirm password
        confirmPasswordInput.sendKeys(confirmPassword);
    }

    /**
     * Click submit/register button
     */
    @Step("Click submit button")
    public void clickSubmitButton() {
        // Wait for button to be clickable
        waitUtils.waitForElementClickable(submitButton);
        // Click the button
        submitButton.click();
    }

    /**
     * Fill complete registration form with all fields
     * Convenience method for positive test scenarios
     * @param firstName First name
     * @param lastName Last name
     * @param email Email address
     * @param password Password
     * @param confirmPassword Confirm password
     */
    @Step("Fill registration form with all fields")
    public void fillRegistrationForm(String firstName, String lastName, String email, 
                                     String password, String confirmPassword) {
        // Fill all fields in sequence
        enterFirstName(firstName);
        enterLastName(lastName);
        enterEmail(email);
        enterPassword(password);
        enterConfirmPassword(confirmPassword);
    }

    /**
     * Submit registration form
     * Combines form filling and submission
     * @param firstName First name
     * @param lastName Last name
     * @param email Email address
     * @param password Password
     * @param confirmPassword Confirm password
     */
    @Step("Submit registration form")
    public void submitRegistrationForm(String firstName, String lastName, String email,
                                       String password, String confirmPassword) {
        // Fill the form
        fillRegistrationForm(firstName, lastName, email, password, confirmPassword);
        // Click submit button
        clickSubmitButton();
    }

    /**
     * Get generic error message text
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
     * Get first name field error message
     * @return First name error text
     */
    public String getFirstNameError() {
        try {
            waitUtils.waitForElementVisible(firstNameError);
            return firstNameError.getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get last name field error message
     * @return Last name error text
     */
    public String getLastNameError() {
        try {
            waitUtils.waitForElementVisible(lastNameError);
            return lastNameError.getText();
        } catch (Exception e) {
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
     * Get confirm password field error message
     * @return Confirm password error text
     */
    public String getConfirmPasswordError() {
        try {
            waitUtils.waitForElementVisible(confirmPasswordError);
            return confirmPasswordError.getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get success message text after successful registration
     * @return Success message text
     */
    @Step("Get success message")
    public String getSuccessMessage() {
        try {
            // Wait for success message to appear
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
     * Useful for test cleanup or retry scenarios
     */
    @Step("Clear registration form")
    public void clearForm() {
        firstNameInput.clear();
        lastNameInput.clear();
        emailInput.clear();
        passwordInput.clear();
        confirmPasswordInput.clear();
    }

    /**
     * Get current page URL
     * @return Current URL
     */
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    /**
     * Check if on registration page
     * @return true if URL contains /register
     */
    public boolean isOnRegisterPage() {
        return driver.getCurrentUrl().contains("/register");
    }
}
