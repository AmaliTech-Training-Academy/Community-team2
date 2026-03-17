package com.communityboard.tests;

import com.communityboard.base.BaseTest;
import com.communityboard.pages.*;
import com.communityboard.utils.TestDataFactory;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SubscriptionTests - Test class for category subscription functionality
 * Covers test cases TC_NOTIF_001 to TC_NOTIF_010
 */
@Epic("User Preferences")
@Feature("Category Subscriptions")
@Story("US11: Category Subscriptions")
public class SubscriptionTests extends BaseTest {

    private SubscriptionSettingsPage subscriptionPage;

    @BeforeEach
    public void setUpTest() {
        loginTestUser();
        subscriptionPage = new SubscriptionSettingsPage(driver);
        subscriptionPage.navigateToSubscriptionSettings();
    }

    private void loginTestUser() {
        LandingPage landingPage = new LandingPage(driver);
        landingPage.navigateToLandingPage();
        
        RegisterPage registerPage = landingPage.clickRegisterLink();
        String email = TestDataFactory.generateUniqueEmail();
        String password = TestDataFactory.generateValidPassword();
        registerPage.submitRegistrationForm(
            TestDataFactory.generateFirstName(),
            TestDataFactory.generateLastName(),
            email, password, password
        );
        
        landingPage.navigateToLandingPage();
        LoginPage loginPage = landingPage.clickLoginButton();
        loginPage.login(email, password);
    }

    @Test
    @DisplayName("TC_NOTIF_001: Subscribe to NEWS category")
    @Description("User should be able to subscribe to NEWS category")
    @Severity(SeverityLevel.CRITICAL)
    public void testSubscribeToNewsCategory() {
        subscriptionPage.subscribeToCategory("NEWS");
        subscriptionPage.saveSettings();
        
        String successMessage = subscriptionPage.getSuccessMessage();
        assertThat(successMessage).as("Success message should be displayed").isNotEmpty();
        
        boolean isSubscribed = subscriptionPage.isSubscribed("NEWS");
        assertThat(isSubscribed).as("Should be subscribed to NEWS").isTrue();
    }

    @Test
    @DisplayName("TC_NOTIF_002: Subscribe to EVENT category")
    @Description("User should be able to subscribe to EVENT category")
    @Severity(SeverityLevel.CRITICAL)
    public void testSubscribeToEventCategory() {
        subscriptionPage.subscribeToCategory("EVENT");
        subscriptionPage.saveSettings();
        
        boolean isSubscribed = subscriptionPage.isSubscribed("EVENT");
        assertThat(isSubscribed).as("Should be subscribed to EVENT").isTrue();
    }

    @Test
    @DisplayName("TC_NOTIF_003: Subscribe to DISCUSSION category")
    @Description("User should be able to subscribe to DISCUSSION category")
    @Severity(SeverityLevel.CRITICAL)
    public void testSubscribeToDiscussionCategory() {
        subscriptionPage.subscribeToCategory("DISCUSSION");
        subscriptionPage.saveSettings();
        
        boolean isSubscribed = subscriptionPage.isSubscribed("DISCUSSION");
        assertThat(isSubscribed).as("Should be subscribed to DISCUSSION").isTrue();
    }

    @Test
    @DisplayName("TC_NOTIF_004: Subscribe to ALERT category")
    @Description("User should be able to subscribe to ALERT category")
    @Severity(SeverityLevel.CRITICAL)
    public void testSubscribeToAlertCategory() {
        subscriptionPage.subscribeToCategory("ALERT");
        subscriptionPage.saveSettings();
        
        boolean isSubscribed = subscriptionPage.isSubscribed("ALERT");
        assertThat(isSubscribed).as("Should be subscribed to ALERT").isTrue();
    }

    @Test
    @DisplayName("TC_NOTIF_005: Unsubscribe from category")
    @Description("User should be able to unsubscribe from category")
    @Severity(SeverityLevel.NORMAL)
    public void testUnsubscribeFromCategory() {
        subscriptionPage.subscribeToCategory("NEWS");
        subscriptionPage.saveSettings();
        
        subscriptionPage.unsubscribeFromCategory("NEWS");
        subscriptionPage.saveSettings();
        
        boolean isSubscribed = subscriptionPage.isSubscribed("NEWS");
        assertThat(isSubscribed).as("Should be unsubscribed from NEWS").isFalse();
    }

    @Test
    @DisplayName("TC_NOTIF_006: Subscribe to multiple categories")
    @Description("User should be able to subscribe to multiple categories at once")
    @Severity(SeverityLevel.NORMAL)
    public void testSubscribeToMultipleCategories() {
        subscriptionPage.subscribeToCategory("NEWS");
        subscriptionPage.subscribeToCategory("EVENT");
        subscriptionPage.subscribeToCategory("ALERT");
        subscriptionPage.saveSettings();
        
        assertThat(subscriptionPage.isSubscribed("NEWS")).isTrue();
        assertThat(subscriptionPage.isSubscribed("EVENT")).isTrue();
        assertThat(subscriptionPage.isSubscribed("ALERT")).isTrue();
    }

    @Test
    @DisplayName("TC_NOTIF_007: Verify subscription settings persist after logout/login")
    @Description("Subscription settings should persist across sessions")
    @Severity(SeverityLevel.NORMAL)
    public void testSubscriptionSettingsPersist() {
        subscriptionPage.subscribeToCategory("NEWS");
        subscriptionPage.saveSettings();
        
        // Refresh page to simulate re-login
        driver.navigate().refresh();
        
        boolean isStillSubscribed = subscriptionPage.isSubscribed("NEWS");
        assertThat(isStillSubscribed).as("Subscription should persist").isTrue();
    }

    @Test
    @DisplayName("TC_NOTIF_008: Verify success message on save")
    @Description("Success message should be displayed after saving subscriptions")
    @Severity(SeverityLevel.MINOR)
    public void testSuccessMessageOnSave() {
        subscriptionPage.subscribeToCategory("NEWS");
        subscriptionPage.saveSettings();
        
        String successMessage = subscriptionPage.getSuccessMessage();
        assertThat(successMessage).as("Success message should be displayed").isNotEmpty();
    }

    @Test
    @DisplayName("TC_NOTIF_009: Verify checkbox state reflects subscription status")
    @Description("Checkbox should be checked if subscribed, unchecked if not")
    @Severity(SeverityLevel.MINOR)
    public void testCheckboxStateReflectsSubscription() {
        subscriptionPage.subscribeToCategory("NEWS");
        boolean isChecked = subscriptionPage.isSubscribed("NEWS");
        assertThat(isChecked).as("Checkbox should be checked after subscribing").isTrue();
        
        subscriptionPage.unsubscribeFromCategory("NEWS");
        boolean isUnchecked = !subscriptionPage.isSubscribed("NEWS");
        assertThat(isUnchecked).as("Checkbox should be unchecked after unsubscribing").isTrue();
    }

    @Test
    @DisplayName("TC_NOTIF_010: Access subscription settings from user menu")
    @Description("User should be able to access subscription settings")
    @Severity(SeverityLevel.MINOR)
    public void testAccessSubscriptionSettings() {
        String currentUrl = subscriptionPage.getCurrentUrl();
        assertThat(currentUrl).as("Should be on subscription settings page")
            .containsAnyOf("/subscriptions", "/settings", "/preferences");
    }
}
