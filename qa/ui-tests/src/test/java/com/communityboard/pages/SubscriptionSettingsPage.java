package com.communityboard.pages;

import com.communityboard.utils.WaitUtils;
import io.qameta.allure.Step;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/**
 * SubscriptionSettingsPage - Page Object for category subscription settings
 * URL: /settings/subscriptions or /profile/subscriptions
 * 
 * Page Elements:
 * - Category checkboxes (NEWS, EVENT, DISCUSSION, ALERT)
 * - Save button
 * - Success message
 */
public class SubscriptionSettingsPage {
    private final WebDriver driver;
    private final WaitUtils waitUtils;

    @FindBy(css = "[data-testid='subscribe-news-checkbox']")
    private WebElement newsCheckbox;
    
    @FindBy(css = "[data-testid='subscribe-event-checkbox']")
    private WebElement eventCheckbox;
    
    @FindBy(css = "[data-testid='subscribe-discussion-checkbox']")
    private WebElement discussionCheckbox;
    
    @FindBy(css = "[data-testid='subscribe-alert-checkbox']")
    private WebElement alertCheckbox;
    
    @FindBy(css = "[data-testid='save-subscriptions-button']")
    private WebElement saveButton;
    
    @FindBy(css = "[data-testid='subscription-success-message']")
    private WebElement successMessage;

    public SubscriptionSettingsPage(WebDriver driver) {
        this.driver = driver;
        this.waitUtils = new WaitUtils(driver);
        PageFactory.initElements(driver, this);
    }

    @Step("Navigate to subscription settings")
    public void navigateToSubscriptionSettings() {
        driver.get("http://communityboard-alb-905603474.eu-west-1.elb.amazonaws.com/settings/subscriptions");
    }

    @Step("Subscribe to category: {category}")
    public void subscribeToCategory(String category) {
        WebElement checkbox = getCategoryCheckbox(category);
        waitUtils.waitForElementVisible(checkbox);
        if (!checkbox.isSelected()) {
            checkbox.click();
        }
    }

    @Step("Unsubscribe from category: {category}")
    public void unsubscribeFromCategory(String category) {
        WebElement checkbox = getCategoryCheckbox(category);
        waitUtils.waitForElementVisible(checkbox);
        if (checkbox.isSelected()) {
            checkbox.click();
        }
    }

    @Step("Save subscription settings")
    public void saveSettings() {
        waitUtils.waitForElementClickable(saveButton);
        saveButton.click();
    }

    public boolean isSubscribed(String category) {
        WebElement checkbox = getCategoryCheckbox(category);
        try {
            waitUtils.waitForElementVisible(checkbox);
            return checkbox.isSelected();
        } catch (Exception e) {
            return false;
        }
    }

    public String getSuccessMessage() {
        try {
            waitUtils.waitForElementVisible(successMessage);
            return successMessage.getText();
        } catch (Exception e) {
            return "";
        }
    }

    private WebElement getCategoryCheckbox(String category) {
        switch (category.toUpperCase()) {
            case "NEWS": return newsCheckbox;
            case "EVENT": return eventCheckbox;
            case "DISCUSSION": return discussionCheckbox;
            case "ALERT": return alertCheckbox;
            default: throw new IllegalArgumentException("Invalid category: " + category);
        }
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
