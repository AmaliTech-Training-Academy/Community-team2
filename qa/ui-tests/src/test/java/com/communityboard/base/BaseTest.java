package com.communityboard.base;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.ByteArrayInputStream;
import java.time.Duration;

/**
 * BaseTest class - Foundation for all UI test classes
 * Implements Single Responsibility Principle: Handles ONLY WebDriver lifecycle management
 * All test classes should extend this to inherit driver setup/teardown
 */
public abstract class BaseTest {

    // Protected driver instance - accessible to all child test classes
    protected WebDriver driver;

    // Base URL for the application under test - centralized configuration
    protected static final String BASE_URL = "http://communityboard-alb-905603474.eu-west-1.elb.amazonaws.com";

    // Default timeout for implicit waits (fallback, explicit waits preferred)
    protected static final int DEFAULT_TIMEOUT_SECONDS = 10;

    /**
     * Setup method - Executes before EACH test method
     * Initializes WebDriver with appropriate configuration
     * @BeforeEach ensures fresh browser instance per test (test isolation)
     */
    @BeforeEach
    public void setUp() {
        // WebDriverManager automatically downloads and configures ChromeDriver
        // No manual driver management needed - handles version compatibility
        WebDriverManager.chromedriver().setup();

        // Configure Chrome options for test execution
        ChromeOptions options = new ChromeOptions();


        // Check if running in CI environment (GitHub Actions, Jenkins, etc.)
        // CI environments typically set CI=true environment variable
        String ciEnv = System.getenv("CI");
        if (ciEnv != null && ciEnv.equalsIgnoreCase("true")) {
            // Headless mode for CI - runs browser without GUI (faster, no display needed)
            options.addArguments("--headless=new"); // New headless mode (Chrome 109+)
            options.addArguments("--disable-gpu"); // Disable GPU acceleration in headless
            options.addArguments("--no-sandbox"); // Bypass OS security model (required in Docker/CI)
            options.addArguments("--disable-dev-shm-usage"); // Overcome limited resource problems
        }

        // Additional Chrome arguments for stability and performance
        options.addArguments("--disable-blink-features=AutomationControlled"); // Hide automation detection
        options.addArguments("--disable-extensions"); // Disable browser extensions
        options.addArguments("--disable-popup-blocking"); // Allow popups if needed
        options.addArguments("--start-maximized"); // Start browser maximized (alternative to driver.manage())

        // Initialize ChromeDriver with configured options
        driver = new ChromeDriver(options);

        // Maximize browser window for consistent element visibility (if not using --start-maximized)
        driver.manage().window().maximize();

        // Set implicit wait as fallback (explicit waits are preferred in page classes)
        // Implicit wait polls DOM for specified duration before throwing NoSuchElementException
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS));

        // Set page load timeout - max time to wait for page to load completely
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));

        // Set script timeout - max time for async JavaScript execution
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(30));
    }

    /**
     * Teardown method - Executes after EACH test method
     * Captures screenshot on failure and closes browser
     * @AfterEach ensures cleanup even if test fails
     */
    @AfterEach
    public void tearDown() {
        // Capture screenshot for Allure report (useful for debugging failures)
        if (driver != null) {
            try {
                // Take screenshot as byte array
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                // Attach to Allure report with descriptive name
                Allure.addAttachment("Final Screenshot", "image/png", 
                    new ByteArrayInputStream(screenshot), "png");
            } catch (Exception e) {
                // Log error but don't fail test if screenshot capture fails
                System.err.println("Failed to capture screenshot: " + e.getMessage());
            }

            // Quit driver - closes all browser windows and ends WebDriver session
            // Always quit to prevent orphaned browser processes
            driver.quit();
        }
    }

    /**
     * Helper method to navigate to base URL
     * Encapsulates navigation logic for reusability
     */
    protected void navigateToBaseUrl() {
        driver.get(BASE_URL);
    }

    /**
     * Helper method to get current page title
     * Useful for page verification in tests
     * @return Current page title
     */
    protected String getPageTitle() {
        return driver.getTitle();
    }

    /**
     * Helper method to get current URL
     * Useful for navigation verification in tests
     * @return Current page URL
     */
    protected String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
