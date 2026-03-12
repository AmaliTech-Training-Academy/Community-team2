package com.communityboard.pages;

import com.communityboard.utils.WaitUtils;
import io.qameta.allure.Step;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.List;

/**
 * AnalyticsDashboardPage - Page Object for moderator analytics dashboard
 * URL: /analytics or /dashboard/analytics
 * 
 * Page Elements:
 * - Total posts count
 * - Total users count
 * - Top contributors list
 * - Category breakdown
 * - Date range selector
 */
public class AnalyticsDashboardPage {
    private final WebDriver driver;
    private final WaitUtils waitUtils;

    @FindBy(css = "[data-testid='total-posts-count']")
    private WebElement totalPostsCount;
    
    @FindBy(css = "[data-testid='total-users-count']")
    private WebElement totalUsersCount;
    
    @FindBy(css = "[data-testid='top-contributor-item']")
    private List<WebElement> topContributors;
    
    @FindBy(css = "[data-testid='category-breakdown']")
    private WebElement categoryBreakdown;
    
    @FindBy(css = "[data-testid='date-range-selector']")
    private WebElement dateRangeSelector;
    
    @FindBy(css = "[data-testid='analytics-chart']")
    private WebElement analyticsChart;

    public AnalyticsDashboardPage(WebDriver driver) {
        this.driver = driver;
        this.waitUtils = new WaitUtils(driver);
        PageFactory.initElements(driver, this);
    }

    @Step("Navigate to analytics dashboard")
    public void navigateToAnalyticsDashboard() {
        driver.get("http://communityboard-alb-905603474.eu-west-1.elb.amazonaws.com/analytics");
    }

    @Step("Get total posts count")
    public String getTotalPosts() {
        try {
            waitUtils.waitForElementVisible(totalPostsCount);
            return totalPostsCount.getText();
        } catch (Exception e) {
            return "";
        }
    }

    @Step("Get total users count")
    public String getTotalUsers() {
        try {
            waitUtils.waitForElementVisible(totalUsersCount);
            return totalUsersCount.getText();
        } catch (Exception e) {
            return "";
        }
    }

    public int getTopContributorsCount() {
        try {
            return topContributors.size();
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean isCategoryBreakdownDisplayed() {
        try {
            return categoryBreakdown.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isAnalyticsChartDisplayed() {
        try {
            return analyticsChart.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isOnAnalyticsDashboard() {
        String currentUrl = driver.getCurrentUrl();
        return currentUrl.contains("/analytics") || currentUrl.contains("/dashboard");
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
