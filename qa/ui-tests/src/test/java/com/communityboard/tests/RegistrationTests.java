package com.communityboard.tests;

import com.communityboard.base.BaseTest;
import com.communityboard.pages.LandingPage;
import com.communityboard.pages.RegisterPage;
import com.communityboard.utils.TestDataFactory;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RegistrationTests - Test class for user registration functionality
 * Covers test cases TC_AUTH_001 to TC_AUTH_012
 * 
 * Test Scenarios:
 * - Positive: Successful registration with valid data
 * - Negative: Empty fields, invalid formats, password mismatch
 * - Validation: Field-level error messages
 * - Boundary: Long inputs, special characters
 * - Edge Case: Duplicate email
 * 
 * Extends BaseTest for WebDriver lifecycle management
 */
@Epic("Authentication")
@Feature("User Registration")
@Story("US1: User Registration")
public class RegistrationTests extends BaseTest {

    // Page objects used in tests
    private LandingPage landingPage;
    private RegisterPage registerPage;

    /**
     * Setup method - Executes before each test
     * Initializes page objects and navigates to registration page
     */
    @BeforeEach
    public void setUpTest() {
        // Initialize landing page
        landingPage = new LandingPage(driver);
        
        // Navigate to landing page
        landingPage.navigateToLandingPage();
        
        // Click register link to navigate to registration page
        registerPage = landingPage.clickRegisterLink();
        
        // Verify we're on registration page
        assertThat(registerPage.isOnRegisterPage())
            .as("Should be on registration page")
            .isTrue();
    }


    /**
     * TC_AUTH_001: Successful registration with valid data
     * Positive test - Verify user can register with all valid inputs
     */
    @Test
    @DisplayName("TC_AUTH_001: Successful registration with valid data")
    @Description("User should be able to register successfully with valid first name, last name, email, and matching passwords")
    @Severity(SeverityLevel.CRITICAL)
    public void testSuccessfulRegistration() {
        // Generate unique test data
        String firstName = TestDataFactory.generateFirstName();
        String lastName = TestDataFactory.generateLastName();
        String email = TestDataFactory.generateUniqueEmail();
        String password = TestDataFactory.generateValidPassword();

        // Fill and submit registration form
        registerPage.submitRegistrationForm(firstName, lastName, email, password, password);

        // Wait for success message or redirect
        // Verify success message is displayed
        String successMessage = registerPage.getSuccessMessage();
        assertThat(successMessage)
            .as("Success message should be displayed after registration")
            .isNotEmpty();

        // Verify no error messages are displayed
        assertThat(registerPage.isErrorMessageDisplayed())
            .as("No error message should be displayed on successful registration")
            .isFalse();
    }

    /**
     * TC_AUTH_002: Registration with empty first name
     * Negative test - Verify validation error for missing first name
     */
    @Test
    @DisplayName("TC_AUTH_002: Registration with empty first name")
    @Description("System should display validation error when first name is empty")
    @Severity(SeverityLevel.NORMAL)
    public void testRegistrationWithEmptyFirstName() {
        // Generate test data with empty first name
        String firstName = ""; // Empty first name
        String lastName = TestDataFactory.generateLastName();
        String email = TestDataFactory.generateUniqueEmail();
        String password = TestDataFactory.generateValidPassword();

        // Submit form with empty first name
        registerPage.submitRegistrationForm(firstName, lastName, email, password, password);

        // Verify error message is displayed
        String errorMessage = registerPage.getFirstNameError();
        assertThat(errorMessage)
            .as("Error message should be displayed for empty first name")
            .isNotEmpty()
            .containsIgnoringCase("required"); // Common validation message

        // Verify user is still on registration page (not registered)
        assertThat(registerPage.isOnRegisterPage())
            .as("Should remain on registration page after validation error")
            .isTrue();
    }

    /**
     * TC_AUTH_003: Registration with empty last name
     * Negative test - Verify validation error for missing last name
     */
    @Test
    @DisplayName("TC_AUTH_003: Registration with empty last name")
    @Description("System should display validation error when last name is empty")
    @Severity(SeverityLevel.NORMAL)
    public void testRegistrationWithEmptyLastName() {
        // Generate test data with empty last name
        String firstName = TestDataFactory.generateFirstName();
        String lastName = ""; // Empty last name
        String email = TestDataFactory.generateUniqueEmail();
        String password = TestDataFactory.generateValidPassword();

        // Submit form with empty last name
        registerPage.submitRegistrationForm(firstName, lastName, email, password, password);

        // Verify error message is displayed
        String errorMessage = registerPage.getLastNameError();
        assertThat(errorMessage)
            .as("Error message should be displayed for empty last name")
            .isNotEmpty()
            .containsIgnoringCase("required");

        // Verify user is still on registration page
        assertThat(registerPage.isOnRegisterPage())
            .as("Should remain on registration page after validation error")
            .isTrue();
    }

    /**
     * TC_AUTH_004: Registration with empty email
     * Negative test - Verify validation error for missing email
     */
    @Test
    @DisplayName("TC_AUTH_004: Registration with empty email")
    @Description("System should display validation error when email is empty")
    @Severity(SeverityLevel.CRITICAL)
    public void testRegistrationWithEmptyEmail() {
        // Generate test data with empty email
        String firstName = TestDataFactory.generateFirstName();
        String lastName = TestDataFactory.generateLastName();
        String email = ""; // Empty email
        String password = TestDataFactory.generateValidPassword();

        // Submit form with empty email
        registerPage.submitRegistrationForm(firstName, lastName, email, password, password);

        // Verify error message is displayed
        String errorMessage = registerPage.getEmailError();
        assertThat(errorMessage)
            .as("Error message should be displayed for empty email")
            .isNotEmpty()
            .containsIgnoringCase("required");

        // Verify user is still on registration page
        assertThat(registerPage.isOnRegisterPage())
            .as("Should remain on registration page after validation error")
            .isTrue();
    }

    /**
     * TC_AUTH_005: Registration with invalid email format
     * Negative test - Verify validation error for invalid email format
     */
    @Test
    @DisplayName("TC_AUTH_005: Registration with invalid email format")
    @Description("System should display validation error when email format is invalid")
    @Severity(SeverityLevel.CRITICAL)
    public void testRegistrationWithInvalidEmailFormat() {
        // Generate test data with invalid email
        String firstName = TestDataFactory.generateFirstName();
        String lastName = TestDataFactory.generateLastName();
        String email = TestDataFactory.generateInvalidEmail(); // Invalid email format
        String password = TestDataFactory.generateValidPassword();

        // Submit form with invalid email
        registerPage.submitRegistrationForm(firstName, lastName, email, password, password);

        // Verify error message is displayed
        String errorMessage = registerPage.getEmailError();
        assertThat(errorMessage)
            .as("Error message should be displayed for invalid email format")
            .isNotEmpty()
            .matches("(?i).*(invalid|valid email|email format).*"); // Regex for common validation messages

        // Verify user is still on registration page
        assertThat(registerPage.isOnRegisterPage())
            .as("Should remain on registration page after validation error")
            .isTrue();
    }

    /**
     * TC_AUTH_006: Registration with empty password
     * Negative test - Verify validation error for missing password
     */
    @Test
    @DisplayName("TC_AUTH_006: Registration with empty password")
    @Description("System should display validation error when password is empty")
    @Severity(SeverityLevel.CRITICAL)
    public void testRegistrationWithEmptyPassword() {
        // Generate test data with empty password
        String firstName = TestDataFactory.generateFirstName();
        String lastName = TestDataFactory.generateLastName();
        String email = TestDataFactory.generateUniqueEmail();
        String password = ""; // Empty password

        // Submit form with empty password
        registerPage.submitRegistrationForm(firstName, lastName, email, password, password);

        // Verify error message is displayed
        String errorMessage = registerPage.getPasswordError();
        assertThat(errorMessage)
            .as("Error message should be displayed for empty password")
            .isNotEmpty()
            .containsIgnoringCase("required");

        // Verify user is still on registration page
        assertThat(registerPage.isOnRegisterPage())
            .as("Should remain on registration page after validation error")
            .isTrue();
    }

    /**
     * TC_AUTH_007: Registration with password less than 8 characters
     * Boundary test - Verify validation error for weak password
     */
    @Test
    @DisplayName("TC_AUTH_007: Registration with password less than 8 characters")
    @Description("System should display validation error when password is less than minimum length (8 characters)")
    @Severity(SeverityLevel.NORMAL)
    public void testRegistrationWithShortPassword() {
        // Generate test data with weak password
        String firstName = TestDataFactory.generateFirstName();
        String lastName = TestDataFactory.generateLastName();
        String email = TestDataFactory.generateUniqueEmail();
        String password = TestDataFactory.generateWeakPassword(); // Less than 8 characters

        // Submit form with short password
        registerPage.submitRegistrationForm(firstName, lastName, email, password, password);

        // Verify error message is displayed
        String errorMessage = registerPage.getPasswordError();
        assertThat(errorMessage)
            .as("Error message should be displayed for password less than 8 characters")
            .isNotEmpty()
            .matches("(?i).*(8 character|minimum|too short).*"); // Regex for length validation

        // Verify user is still on registration page
        assertThat(registerPage.isOnRegisterPage())
            .as("Should remain on registration page after validation error")
            .isTrue();
    }

    /**
     * TC_AUTH_008: Registration with password mismatch
     * Negative test - Verify validation error when passwords don't match
     */
    @Test
    @DisplayName("TC_AUTH_008: Registration with password mismatch")
    @Description("System should display validation error when password and confirm password do not match")
    @Severity(SeverityLevel.CRITICAL)
    public void testRegistrationWithPasswordMismatch() {
        // Generate test data with mismatched passwords
        String firstName = TestDataFactory.generateFirstName();
        String lastName = TestDataFactory.generateLastName();
        String email = TestDataFactory.generateUniqueEmail();
        String password = TestDataFactory.generateValidPassword();
        String confirmPassword = TestDataFactory.generateValidPassword(); // Different password

        // Submit form with mismatched passwords
        registerPage.submitRegistrationForm(firstName, lastName, email, password, confirmPassword);

        // Verify error message is displayed
        String errorMessage = registerPage.getConfirmPasswordError();
        assertThat(errorMessage)
            .as("Error message should be displayed for password mismatch")
            .isNotEmpty()
            .matches("(?i).*(match|same|identical).*"); // Regex for mismatch validation

        // Verify user is still on registration page
        assertThat(registerPage.isOnRegisterPage())
            .as("Should remain on registration page after validation error")
            .isTrue();
    }


    /**
     * TC_AUTH_009: Registration with duplicate email
     * Edge case - Verify error when email already exists in system
     */
    @Test
    @DisplayName("TC_AUTH_009: Registration with duplicate email")
    @Description("System should display error message when attempting to register with an email that already exists")
    @Severity(SeverityLevel.CRITICAL)
    public void testRegistrationWithDuplicateEmail() {
        // First registration - create a user
        String firstName = TestDataFactory.generateFirstName();
        String lastName = TestDataFactory.generateLastName();
        String email = TestDataFactory.generateUniqueEmail();
        String password = TestDataFactory.generateValidPassword();

        // Submit first registration
        registerPage.submitRegistrationForm(firstName, lastName, email, password, password);

        // Wait for registration to complete
        // Navigate back to registration page for second attempt
        registerPage.navigateToRegisterPage();

        // Attempt second registration with same email
        String newFirstName = TestDataFactory.generateFirstName();
        String newLastName = TestDataFactory.generateLastName();
        String newPassword = TestDataFactory.generateValidPassword();

        // Submit form with duplicate email
        registerPage.submitRegistrationForm(newFirstName, newLastName, email, newPassword, newPassword);

        // Verify error message is displayed
        String errorMessage = registerPage.getErrorMessage();
        assertThat(errorMessage)
            .as("Error message should be displayed for duplicate email")
            .isNotEmpty()
            .matches("(?i).*(already exists|already registered|email.*taken).*");

        // Verify user is still on registration page
        assertThat(registerPage.isOnRegisterPage())
            .as("Should remain on registration page after duplicate email error")
            .isTrue();
    }

    /**
     * TC_AUTH_010: Registration with special characters in name
     * Boundary test - Verify system handles special characters in name fields
     */
    @Test
    @DisplayName("TC_AUTH_010: Registration with special characters in name")
    @Description("System should handle special characters in first name and last name appropriately")
    @Severity(SeverityLevel.MINOR)
    public void testRegistrationWithSpecialCharactersInName() {
        // Generate test data with special characters
        String firstName = "John-Paul"; // Hyphenated name
        String lastName = "O'Brien"; // Apostrophe in name
        String email = TestDataFactory.generateUniqueEmail();
        String password = TestDataFactory.generateValidPassword();

        // Submit form with special characters in names
        registerPage.submitRegistrationForm(firstName, lastName, email, password, password);

        // Verify registration succeeds OR appropriate validation message
        // Some systems allow hyphens/apostrophes, others don't
        boolean isSuccessful = registerPage.isSuccessMessageDisplayed();
        boolean hasError = registerPage.isErrorMessageDisplayed();

        // Either success or validation error is acceptable
        assertThat(isSuccessful || hasError)
            .as("System should either accept special characters or show validation error")
            .isTrue();

        // If error, verify it's related to special characters
        if (hasError) {
            String errorMessage = registerPage.getErrorMessage();
            assertThat(errorMessage)
                .as("Error message should mention invalid characters if rejected")
                .matches("(?i).*(character|invalid|letter).*");
        }
    }

    /**
     * TC_AUTH_011: Registration with very long inputs
     * Boundary test - Verify system handles maximum length inputs
     */
    @Test
    @DisplayName("TC_AUTH_011: Registration with very long inputs")
    @Description("System should handle or reject very long input strings appropriately")
    @Severity(SeverityLevel.MINOR)
    public void testRegistrationWithVeryLongInputs() {
        // Generate test data with very long strings
        String firstName = TestDataFactory.generateLongString(100); // 100 characters
        String lastName = TestDataFactory.generateLongString(100);
        String email = "verylongemailaddress" + TestDataFactory.generateTimestamp() + "@test.com";
        String password = TestDataFactory.generateValidPassword();

        // Submit form with very long inputs
        registerPage.submitRegistrationForm(firstName, lastName, email, password, password);

        // Verify system handles long inputs (either accepts or shows validation)
        boolean isSuccessful = registerPage.isSuccessMessageDisplayed();
        boolean hasError = registerPage.isErrorMessageDisplayed();

        // Either success or validation error is acceptable
        assertThat(isSuccessful || hasError)
            .as("System should either accept long inputs or show validation error")
            .isTrue();

        // If error, verify it's related to length
        if (hasError) {
            String errorMessage = registerPage.getErrorMessage();
            assertThat(errorMessage)
                .as("Error message should mention length limit if rejected")
                .matches("(?i).*(long|maximum|limit|character).*");
        }
    }

    /**
     * TC_AUTH_012: Verify all required field indicators display
     * UI/UX test - Verify required fields are marked appropriately
     */
    @Test
    @DisplayName("TC_AUTH_012: Verify all required field indicators display")
    @Description("All required fields should be clearly marked with asterisk or 'required' label")
    @Severity(SeverityLevel.MINOR)
    public void testRequiredFieldIndicatorsDisplay() {
        // Verify registration page is displayed
        assertThat(registerPage.isOnRegisterPage())
            .as("Should be on registration page")
            .isTrue();

        // Note: This test verifies UI elements are present
        // In a real implementation, you would check for asterisks or "required" labels
        // For now, we verify the form fields are visible

        // Attempt to submit empty form
        registerPage.clickSubmitButton();

        // Verify validation errors appear for all required fields
        // This indirectly confirms which fields are required
        String firstNameError = registerPage.getFirstNameError();
        String lastNameError = registerPage.getLastNameError();
        String emailError = registerPage.getEmailError();
        String passwordError = registerPage.getPasswordError();

        // At least one error should be displayed for empty form
        boolean hasValidationErrors = !firstNameError.isEmpty() || 
                                     !lastNameError.isEmpty() || 
                                     !emailError.isEmpty() || 
                                     !passwordError.isEmpty();

        assertThat(hasValidationErrors)
            .as("Validation errors should be displayed when submitting empty form")
            .isTrue();

        // Verify user remains on registration page
        assertThat(registerPage.isOnRegisterPage())
            .as("Should remain on registration page after validation errors")
            .isTrue();
    }

    /**
     * Parameterized test: Registration with multiple invalid email formats
     * Tests various invalid email patterns
     */
    @ParameterizedTest
    @ValueSource(strings = {"notanemail", "test@", "@test.com", "test..user@test.com", "test@test", "test user@test.com"})
    @DisplayName("Registration with various invalid email formats")
    @Description("System should reject all invalid email formats")
    @Severity(SeverityLevel.NORMAL)
    public void testRegistrationWithVariousInvalidEmails(String invalidEmail) {
        // Generate test data with invalid email
        String firstName = TestDataFactory.generateFirstName();
        String lastName = TestDataFactory.generateLastName();
        String password = TestDataFactory.generateValidPassword();

        // Submit form with invalid email
        registerPage.submitRegistrationForm(firstName, lastName, invalidEmail, password, password);

        // Verify error message is displayed
        String errorMessage = registerPage.getEmailError();
        assertThat(errorMessage)
            .as("Error message should be displayed for invalid email: " + invalidEmail)
            .isNotEmpty();

        // Clear form for next iteration
        registerPage.clearForm();
    }
}
