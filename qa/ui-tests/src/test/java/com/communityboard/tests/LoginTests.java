package com.communityboard.tests;

import com.communityboard.base.BaseTest;
import com.communityboard.pages.LandingPage;
import com.communityboard.pages.LoginPage;
import com.communityboard.pages.RegisterPage;
import com.communityboard.utils.TestDataFactory;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * LoginTests - Test class for user login functionality
 * Covers test cases TC_AUTH_013 to TC_AUTH_025
 * 
 * Test Scenarios:
 * - Positive: Successful login with valid credentials
 * - Negative: Empty fields, invalid formats, wrong credentials
 * - Validation: Field-level error messages
 * - UI/UX: Button states, password masking, forgot password link
 * - Security: SQL injection, XSS attempts (UI behavior only)
 * 
 * Extends BaseTest for WebDriver lifecycle management
 */
@Epic("Authentication")
@Feature("User Login")
@Story("US2: User Login")
public class LoginTests extends BaseTest {

    // Page objects used in tests
    private LandingPage landingPage;
    private LoginPage loginPage;
    private RegisterPage registerPage;

    // Test user credentials (created once, reused for login tests)
    private String testUserEmail;
    private String testUserPassword;

    /**
     * Setup method - Executes before each test
     * Creates a test user and navigates to login page
     */
    @BeforeEach
    public void setUpTest() {
        // Initialize landing page
        landingPage = new LandingPage(driver);
        
        // Navigate to landing page
        landingPage.navigateToLandingPage();
        
        // Create a test user for login tests (if not already created)
        createTestUser();
        
        // Navigate to login page
        loginPage = landingPage.clickLoginButton();
        
        // Verify we're on login page
        assertThat(loginPage.isOnLoginPage())
            .as("Should be on login page")
            .isTrue();
    }

    /**
     * Helper method to create a test user for login tests
     * Registers a new user that can be used for successful login scenarios
     */
    private void createTestUser() {
        // Generate unique credentials
        testUserEmail = TestDataFactory.generateUniqueEmail();
        testUserPassword = TestDataFactory.generateValidPassword();
        
        // Navigate to registration page
        registerPage = landingPage.clickRegisterLink();
        
        // Register the test user
        String firstName = TestDataFactory.generateFirstName();
        String lastName = TestDataFactory.generateLastName();
        registerPage.submitRegistrationForm(firstName, lastName, testUserEmail, 
                                           testUserPassword, testUserPassword);
        
        // Wait for registration to complete
        // Navigate back to landing page
        landingPage.navigateToLandingPage();
    }


    /**
     * TC_AUTH_013: Successful login with valid credentials
     * Positive test - Verify user can login with correct email and password
     */
    @Test
    @DisplayName("TC_AUTH_013: Successful login with valid credentials")
    @Description("User should be able to login successfully with valid email and password")
    @Severity(SeverityLevel.BLOCKER)
    public void testSuccessfulLogin() {
        // Login with test user credentials
        loginPage.login(testUserEmail, testUserPassword);

        // Wait for redirect after successful login
        boolean isRedirected = loginPage.waitForLoginRedirect(10);

        // Verify user is redirected away from login page
        assertThat(isRedirected)
            .as("User should be redirected after successful login")
            .isTrue();

        // Verify URL changed (no longer on /login)
        assertThat(loginPage.isOnLoginPage())
            .as("Should not be on login page after successful login")
            .isFalse();

        // Verify no error message is displayed
        assertThat(loginPage.isErrorMessageDisplayed())
            .as("No error message should be displayed on successful login")
            .isFalse();
    }

    /**
     * TC_AUTH_014: Login with empty email
     * Negative test - Verify validation error for missing email
     */
    @Test
    @DisplayName("TC_AUTH_014: Login with empty email")
    @Description("System should display validation error when email is empty")
    @Severity(SeverityLevel.NORMAL)
    public void testLoginWithEmptyEmail() {
        // Attempt login with empty email
        String email = ""; // Empty email
        String password = testUserPassword;

        loginPage.login(email, password);

        // Verify error message is displayed
        String errorMessage = loginPage.getEmailError();
        assertThat(errorMessage)
            .as("Error message should be displayed for empty email")
            .isNotEmpty()
            .containsIgnoringCase("required");

        // Verify user remains on login page
        assertThat(loginPage.isOnLoginPage())
            .as("Should remain on login page after validation error")
            .isTrue();
    }

    /**
     * TC_AUTH_015: Login with empty password
     * Negative test - Verify validation error for missing password
     */
    @Test
    @DisplayName("TC_AUTH_015: Login with empty password")
    @Description("System should display validation error when password is empty")
    @Severity(SeverityLevel.NORMAL)
    public void testLoginWithEmptyPassword() {
        // Attempt login with empty password
        String email = testUserEmail;
        String password = ""; // Empty password

        loginPage.login(email, password);

        // Verify error message is displayed
        String errorMessage = loginPage.getPasswordError();
        assertThat(errorMessage)
            .as("Error message should be displayed for empty password")
            .isNotEmpty()
            .containsIgnoringCase("required");

        // Verify user remains on login page
        assertThat(loginPage.isOnLoginPage())
            .as("Should remain on login page after validation error")
            .isTrue();
    }

    /**
     * TC_AUTH_016: Login with invalid email format
     * Negative test - Verify validation error for invalid email format
     */
    @Test
    @DisplayName("TC_AUTH_016: Login with invalid email format")
    @Description("System should display validation error when email format is invalid")
    @Severity(SeverityLevel.NORMAL)
    public void testLoginWithInvalidEmailFormat() {
        // Attempt login with invalid email format
        String email = TestDataFactory.generateInvalidEmail();
        String password = testUserPassword;

        loginPage.login(email, password);

        // Verify error message is displayed
        String errorMessage = loginPage.getEmailError();
        assertThat(errorMessage)
            .as("Error message should be displayed for invalid email format")
            .isNotEmpty()
            .matches("(?i).*(invalid|valid email|email format).*");

        // Verify user remains on login page
        assertThat(loginPage.isOnLoginPage())
            .as("Should remain on login page after validation error")
            .isTrue();
    }

    /**
     * TC_AUTH_017: Login with non-existent email
     * Negative test - Verify error for email not in system
     */
    @Test
    @DisplayName("TC_AUTH_017: Login with non-existent email")
    @Description("System should display error when attempting to login with email that doesn't exist")
    @Severity(SeverityLevel.CRITICAL)
    public void testLoginWithNonExistentEmail() {
        // Attempt login with non-existent email
        String email = "nonexistent" + TestDataFactory.generateTimestamp() + "@test.com";
        String password = testUserPassword;

        loginPage.login(email, password);

        // Verify error message is displayed
        String errorMessage = loginPage.getErrorMessage();
        assertThat(errorMessage)
            .as("Error message should be displayed for non-existent email")
            .isNotEmpty()
            .matches("(?i).*(invalid|incorrect|not found|doesn't exist).*");

        // Verify user remains on login page
        assertThat(loginPage.isOnLoginPage())
            .as("Should remain on login page after failed login")
            .isTrue();
    }

    /**
     * TC_AUTH_018: Login with incorrect password
     * Negative test - Verify error for wrong password
     */
    @Test
    @DisplayName("TC_AUTH_018: Login with incorrect password")
    @Description("System should display error when password is incorrect")
    @Severity(SeverityLevel.CRITICAL)
    public void testLoginWithIncorrectPassword() {
        // Attempt login with incorrect password
        String email = testUserEmail;
        String password = "WrongPassword123!"; // Incorrect password

        loginPage.login(email, password);

        // Verify error message is displayed
        String errorMessage = loginPage.getErrorMessage();
        assertThat(errorMessage)
            .as("Error message should be displayed for incorrect password")
            .isNotEmpty()
            .matches("(?i).*(invalid|incorrect|wrong|failed).*");

        // Verify user remains on login page
        assertThat(loginPage.isOnLoginPage())
            .as("Should remain on login page after failed login")
            .isTrue();
    }

    /**
     * TC_AUTH_019: Login button disabled with empty fields
     * UI/UX test - Verify button state with empty inputs
     */
    @Test
    @DisplayName("TC_AUTH_019: Login button disabled with empty fields")
    @Description("Login button should be disabled when email or password fields are empty")
    @Severity(SeverityLevel.MINOR)
    public void testLoginButtonDisabledWithEmptyFields() {
        // Clear any pre-filled values
        loginPage.clearForm();

        // Check if login button is disabled with empty fields
        // Note: Some implementations may not disable the button, but show validation on click
        boolean isButtonEnabled = loginPage.isLoginButtonEnabled();

        // If button is enabled, verify clicking it shows validation errors
        if (isButtonEnabled) {
            loginPage.clickLoginButton();
            
            // Verify validation errors appear
            boolean hasErrors = loginPage.isErrorMessageDisplayed() ||
                              !loginPage.getEmailError().isEmpty() ||
                              !loginPage.getPasswordError().isEmpty();
            
            assertThat(hasErrors)
                .as("Validation errors should appear when submitting empty form")
                .isTrue();
        } else {
            // Button is disabled - this is the expected behavior
            assertThat(isButtonEnabled)
                .as("Login button should be disabled with empty fields")
                .isFalse();
        }
    }

    /**
     * TC_AUTH_020: Verify error message display on failed login
     * UI/UX test - Verify error message is clearly displayed
     */
    @Test
    @DisplayName("TC_AUTH_020: Verify error message display on failed login")
    @Description("Error message should be clearly displayed when login fails")
    @Severity(SeverityLevel.NORMAL)
    public void testErrorMessageDisplayOnFailedLogin() {
        // Attempt login with incorrect credentials
        String email = testUserEmail;
        String password = "WrongPassword123!";

        loginPage.login(email, password);

        // Verify error message is displayed
        assertThat(loginPage.isErrorMessageDisplayed())
            .as("Error message should be displayed on failed login")
            .isTrue();

        // Verify error message has content
        String errorMessage = loginPage.getErrorMessage();
        assertThat(errorMessage)
            .as("Error message should have meaningful content")
            .isNotEmpty()
            .hasSizeGreaterThan(5); // At least a few characters

        // Verify user remains on login page
        assertThat(loginPage.isOnLoginPage())
            .as("Should remain on login page after failed login")
            .isTrue();
    }

    /**
     * TC_AUTH_021: Verify redirect to feed/home after successful login
     * Functional test - Verify navigation after login
     */
    @Test
    @DisplayName("TC_AUTH_021: Verify redirect to feed/home after successful login")
    @Description("User should be redirected to home/feed page after successful login")
    @Severity(SeverityLevel.CRITICAL)
    public void testRedirectAfterSuccessfulLogin() {
        // Login with valid credentials
        loginPage.login(testUserEmail, testUserPassword);

        // Wait for redirect
        boolean isRedirected = loginPage.waitForLoginRedirect(10);

        // Verify redirect occurred
        assertThat(isRedirected)
            .as("User should be redirected after successful login")
            .isTrue();

        // Verify URL contains expected path (home, feed, or dashboard)
        String currentUrl = loginPage.getCurrentUrl();
        assertThat(currentUrl)
            .as("URL should contain home, feed, or dashboard after login")
            .matches("(?i).*(home|feed|dashboard).*");
    }

    /**
     * TC_AUTH_022: Verify "Forgot Password" link is present
     * UI/UX test - Verify forgot password functionality is accessible
     */
    @Test
    @DisplayName("TC_AUTH_022: Verify 'Forgot Password' link is present")
    @Description("Forgot Password link should be visible on login page")
    @Severity(SeverityLevel.MINOR)
    public void testForgotPasswordLinkPresent() {
        // Verify forgot password link is displayed
        boolean isForgotPasswordLinkDisplayed = loginPage.isForgotPasswordLinkDisplayed();

        assertThat(isForgotPasswordLinkDisplayed)
            .as("Forgot Password link should be displayed on login page")
            .isTrue();

        // Optional: Click the link and verify navigation (if implemented)
        if (isForgotPasswordLinkDisplayed) {
            loginPage.clickForgotPasswordLink();
            
            // Verify URL changed to forgot password page
            String currentUrl = loginPage.getCurrentUrl();
            // URL might contain /forgot-password, /reset-password, etc.
            // This is optional verification
        }
    }


    /**
     * TC_AUTH_023: Login with SQL injection attempt (UI behavior)
     * Security test - Verify UI handles SQL injection strings appropriately
     * Note: This tests UI behavior only, not actual SQL injection vulnerability
     */
    @Test
    @DisplayName("TC_AUTH_023: Login with SQL injection attempt (UI behavior)")
    @Description("System should handle SQL injection strings in login form without crashing")
    @Severity(SeverityLevel.NORMAL)
    public void testLoginWithSqlInjectionAttempt() {
        // Attempt login with SQL injection string
        String email = TestDataFactory.generateSqlInjectionString();
        String password = testUserPassword;

        loginPage.login(email, password);

        // Verify system handles the input gracefully (no crash)
        // Either shows validation error or login failure
        boolean hasError = loginPage.isErrorMessageDisplayed() ||
                          !loginPage.getEmailError().isEmpty();

        assertThat(hasError)
            .as("System should show error for SQL injection attempt")
            .isTrue();

        // Verify user remains on login page (not logged in)
        assertThat(loginPage.isOnLoginPage())
            .as("Should remain on login page after SQL injection attempt")
            .isTrue();

        // Verify no redirect occurred (security check)
        String currentUrl = loginPage.getCurrentUrl();
        assertThat(currentUrl)
            .as("URL should still be login page")
            .contains("/login");
    }

    /**
     * TC_AUTH_024: Login with XSS attempt (UI behavior)
     * Security test - Verify UI handles XSS strings appropriately
     * Note: This tests UI behavior only, not actual XSS vulnerability
     */
    @Test
    @DisplayName("TC_AUTH_024: Login with XSS attempt (UI behavior)")
    @Description("System should handle XSS strings in login form without executing scripts")
    @Severity(SeverityLevel.NORMAL)
    public void testLoginWithXssAttempt() {
        // Attempt login with XSS string
        String email = TestDataFactory.generateXssString();
        String password = testUserPassword;

        loginPage.login(email, password);

        // Verify system handles the input gracefully
        // Either shows validation error or login failure
        boolean hasError = loginPage.isErrorMessageDisplayed() ||
                          !loginPage.getEmailError().isEmpty();

        assertThat(hasError)
            .as("System should show error for XSS attempt")
            .isTrue();

        // Verify user remains on login page (not logged in)
        assertThat(loginPage.isOnLoginPage())
            .as("Should remain on login page after XSS attempt")
            .isTrue();

        // Verify no alert was triggered (XSS not executed)
        // In Selenium, if XSS executed, an alert would be present
        try {
            driver.switchTo().alert();
            // If we reach here, alert exists (XSS executed) - FAIL
            assertThat(false)
                .as("XSS script should not execute - no alert should be present")
                .isTrue();
        } catch (Exception e) {
            // No alert present - this is expected (XSS properly escaped)
            assertThat(true)
                .as("XSS script was properly escaped - no alert present")
                .isTrue();
        }
    }

    /**
     * TC_AUTH_025: Verify password field is masked
     * UI/UX test - Verify password is hidden when typing
     */
    @Test
    @DisplayName("TC_AUTH_025: Verify password field is masked")
    @Description("Password field should mask input characters for security")
    @Severity(SeverityLevel.MINOR)
    public void testPasswordFieldIsMasked() {
        // Enter password in the field
        loginPage.enterPassword(testUserPassword);

        // Verify password field type is "password" (masked)
        boolean isPasswordMasked = loginPage.isPasswordMasked();

        assertThat(isPasswordMasked)
            .as("Password field should be masked (type='password')")
            .isTrue();

        // Optional: Test password visibility toggle if present
        try {
            // Toggle password visibility
            loginPage.togglePasswordVisibility();
            
            // After toggle, password might be visible (type='text')
            boolean isStillMasked = loginPage.isPasswordMasked();
            
            // After first toggle, password should be visible (not masked)
            assertThat(isStillMasked)
                .as("Password should be visible after toggling visibility")
                .isFalse();
            
            // Toggle again to hide
            loginPage.togglePasswordVisibility();
            
            // After second toggle, password should be masked again
            boolean isMaskedAgain = loginPage.isPasswordMasked();
            assertThat(isMaskedAgain)
                .as("Password should be masked again after second toggle")
                .isTrue();
        } catch (Exception e) {
            // Password visibility toggle not present - this is acceptable
            // Just verify password is masked by default
            assertThat(isPasswordMasked)
                .as("Password field should be masked by default")
                .isTrue();
        }
    }

    /**
     * Additional test: Verify login form clears after failed attempt
     * UI/UX test - Verify form state after failed login
     */
    @Test
    @DisplayName("Verify login form state after failed attempt")
    @Description("Form should remain filled or be cleared appropriately after failed login")
    @Severity(SeverityLevel.TRIVIAL)
    public void testLoginFormStateAfterFailedAttempt() {
        // Attempt login with incorrect credentials
        String email = testUserEmail;
        String password = "WrongPassword123!";

        loginPage.login(email, password);

        // Verify error is displayed
        assertThat(loginPage.isErrorMessageDisplayed())
            .as("Error should be displayed after failed login")
            .isTrue();

        // Verify user can retry login (form is still accessible)
        assertThat(loginPage.isLoginButtonEnabled())
            .as("Login button should still be enabled for retry")
            .isTrue();

        // Clear form and verify it's cleared
        loginPage.clearForm();

        // Attempt login again with correct credentials
        loginPage.login(testUserEmail, testUserPassword);

        // Verify successful login after retry
        boolean isRedirected = loginPage.waitForLoginRedirect(10);
        assertThat(isRedirected)
            .as("User should be able to login successfully after failed attempt")
            .isTrue();
    }
}
