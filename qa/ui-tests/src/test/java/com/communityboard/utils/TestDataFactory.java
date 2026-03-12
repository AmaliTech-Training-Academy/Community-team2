package com.communityboard.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;

/**
 * TestDataFactory - Utility class for generating test data
 * Implements Single Responsibility Principle: Handles ONLY test data generation
 * Provides methods to create unique, valid test data for user registration, posts, etc.
 */
public class TestDataFactory {

    // Random instance for generating random values
    private static final Random random = new Random();

    // Common test data constants
    private static final String[] FIRST_NAMES = {"John", "Jane", "Michael", "Sarah", "David", "Emily"};
    private static final String[] LAST_NAMES = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia"};
    private static final String[] CATEGORIES = {"NEWS", "EVENT", "DISCUSSION", "ALERT"};

    /**
     * Generate unique email address using timestamp
     * Ensures no duplicate email conflicts in tests
     * @return Unique email address
     */
    public static String generateUniqueEmail() {
        // Use timestamp + random number for uniqueness
        long timestamp = System.currentTimeMillis();
        int randomNum = random.nextInt(1000);
        return "testuser" + timestamp + randomNum + "@test.com";
    }

    /**
     * Generate unique email with custom prefix
     * Useful for identifying test users by role/scenario
     * @param prefix Custom prefix (e.g., "moderator", "admin")
     * @return Unique email with prefix
     */
    public static String generateUniqueEmailWithPrefix(String prefix) {
        long timestamp = System.currentTimeMillis();
        return prefix + timestamp + "@test.com";
    }

    /**
     * Generate random first name from predefined list
     * @return Random first name
     */
    public static String generateFirstName() {
        // Select random name from array
        return FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
    }

    /**
     * Generate random last name from predefined list
     * @return Random last name
     */
    public static String generateLastName() {
        // Select random name from array
        return LAST_NAMES[random.nextInt(LAST_NAMES.length)];
    }


    /**
     * Generate valid password meeting requirements
     * Assumes minimum 8 characters requirement
     * @return Valid password string
     */
    public static String generateValidPassword() {
        // Password with uppercase, lowercase, number, special char (common requirements)
        return "Test@123" + random.nextInt(1000);
    }

    /**
     * Generate weak password (less than 8 characters)
     * For negative testing scenarios
     * @return Weak password string
     */
    public static String generateWeakPassword() {
        // Password with less than 8 characters
        return "Test" + random.nextInt(100);
    }

    /**
     * Generate random post title
     * @return Random post title
     */
    public static String generatePostTitle() {
        String[] titlePrefixes = {"Important", "Urgent", "Community", "Neighborhood", "Local"};
        String[] titleSuffixes = {"Update", "News", "Announcement", "Alert", "Information"};
        return titlePrefixes[random.nextInt(titlePrefixes.length)] + " " +
               titleSuffixes[random.nextInt(titleSuffixes.length)] + " " +
               System.currentTimeMillis();
    }

    /**
     * Generate random post content
     * @return Random post content
     */
    public static String generatePostContent() {
        return "This is a test post content created at " + 
               LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) +
               ". This content is for automated testing purposes. Random ID: " + 
               UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Generate random category from available options
     * @return Random category (NEWS, EVENT, DISCUSSION, ALERT)
     */
    public static String generateRandomCategory() {
        return CATEGORIES[random.nextInt(CATEGORIES.length)];
    }

    /**
     * Generate long string for boundary testing
     * @param length Desired string length
     * @return String of specified length
     */
    public static String generateLongString(int length) {
        // Create string of specified length using repeated character
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append("a");
        }
        return sb.toString();
    }

    /**
     * Generate string with special characters
     * For testing input validation
     * @return String with special characters
     */
    public static String generateSpecialCharacterString() {
        return "Test!@#$%^&*()_+-=[]{}|;':\",./<>?`~";
    }

    /**
     * Generate SQL injection attempt string
     * For UI-level security testing (verify proper handling)
     * @return SQL injection string
     */
    public static String generateSqlInjectionString() {
        return "' OR '1'='1"; // Classic SQL injection pattern
    }

    /**
     * Generate XSS attempt string
     * For UI-level security testing (verify proper escaping)
     * @return XSS string
     */
    public static String generateXssString() {
        return "<script>alert('XSS')</script>"; // Classic XSS pattern
    }

    /**
     * Generate random comment text
     * @return Random comment text
     */
    public static String generateCommentText() {
        return "Test comment posted at " + 
               LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) +
               " - ID: " + random.nextInt(10000);
    }

    /**
     * Generate invalid email format
     * For negative testing
     * @return Invalid email string
     */
    public static String generateInvalidEmail() {
        String[] invalidEmails = {
            "notanemail",           // Missing @ and domain
            "test@",                // Missing domain
            "@test.com",            // Missing local part
            "test..user@test.com",  // Double dot
            "test@test",            // Missing TLD
            "test user@test.com"    // Space in email
        };
        return invalidEmails[random.nextInt(invalidEmails.length)];
    }

    /**
     * Generate current timestamp string
     * Useful for unique identifiers
     * @return Formatted timestamp
     */
    public static String generateTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
}
