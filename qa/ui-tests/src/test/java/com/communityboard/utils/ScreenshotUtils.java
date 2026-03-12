package com.communityboard.utils;

import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

/**
 * ScreenshotUtils - Utility class for capturing screenshots
 * Implements Single Responsibility Principle: Handles ONLY screenshot operations
 * Integrates with Allure reporting via @Attachment annotation
 */
public class ScreenshotUtils {

    /**
     * Capture screenshot and attach to Allure report
     * @Attachment annotation automatically attaches return value to Allure report
     * @param driver WebDriver instance
     * @return Screenshot as byte array (automatically attached by Allure)
     */
    @Attachment(value = "Screenshot on Failure", type = "image/png")
    public static byte[] captureScreenshot(WebDriver driver) {
        // Cast driver to TakesScreenshot interface
        // OutputType.BYTES returns screenshot as byte array for Allure
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    }

    /**
     * Capture screenshot with custom name
     * Useful for capturing screenshots at specific test steps
     * @param driver WebDriver instance
     * @param screenshotName Custom name for screenshot
     * @return Screenshot as byte array
     */
    @Attachment(value = "{screenshotName}", type = "image/png")
    public static byte[] captureScreenshotWithName(WebDriver driver, String screenshotName) {
        // Same as captureScreenshot but with custom name parameter
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    }

    /**
     * Capture screenshot for specific test step
     * Use this in page classes with @Step annotation for detailed reporting
     * @param driver WebDriver instance
     * @param stepName Name of the test step
     * @return Screenshot as byte array
     */
    @Attachment(value = "Step: {stepName}", type = "image/png")
    public static byte[] captureStepScreenshot(WebDriver driver, String stepName) {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    }
}
