package com.communityboard.tests;

import com.communityboard.base.BaseTest;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SetupVerificationTest - Verifies base test infrastructure is working
 * This test ensures WebDriver, Allure, and base configuration are properly set up
 * Should be run first to validate test environment before running actual test suites
 */
@Epic("Infrastructure")
@Feature("Setup Verification")
public class SetupVerificationTest extends BaseTest {

    /**
     * Test: Verify application is accessible
     * Validates that base URL loads successfully and page title is present
     * This is a smoke test to ensure application is reachable
     */
    @Test
    @DisplayName("Verify CommunityBoard application is accessible")
    @Description("Navigate to base URL and verify page loads successfully with expected title")
    @Severity(SeverityLevel.BLOCKER)
    @Story("Infrastructure Setup")
    public void testApplicationAccessibility() {
        // Navigate to base URL (defined in BaseTest)
        navigateToBaseUrl();

        // Get current page title
        String pageTitle = getPageTitle();

        // Verify page title is not empty (indicates page loaded)
        assertThat(pageTitle)
            .as("Page title should not be empty")
            .isNotEmpty();

        // Verify URL is correct
        String currentUrl = getCurrentUrl();
        assertThat(currentUrl)
            .as("Current URL should match base URL")
            .contains("communityboard-alb");

        // Log success for Allure report
        Allure.step("Application is accessible at: " + BASE_URL);
    }

    /**
     * Test: Verify WebDriver is initialized
     * Validates that driver instance is not null and functional
     */
    @Test
    @DisplayName("Verify WebDriver initialization")
    @Description("Ensure WebDriver is properly initialized and can perform basic operations")
    @Severity(SeverityLevel.BLOCKER)
    @Story("Infrastructure Setup")
    public void testWebDriverInitialization() {
        // Verify driver is not null (initialized in BaseTest @BeforeEach)
        assertThat(driver)
            .as("WebDriver should be initialized")
            .isNotNull();

        // Navigate to verify driver is functional
        navigateToBaseUrl();

        // Verify navigation worked
        assertThat(getCurrentUrl())
            .as("Driver should be able to navigate")
            .isNotEmpty();

        Allure.step("WebDriver is properly initialized and functional");
    }
}
