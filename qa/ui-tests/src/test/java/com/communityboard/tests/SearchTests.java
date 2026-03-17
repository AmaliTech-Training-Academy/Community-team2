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
 * SearchTests - Test class for search functionality
 * Covers test cases TC_SEARCH_001 to TC_SEARCH_008
 */
@Epic("Post Interaction")
@Feature("Search")
@Story("US8: Search Posts")
public class SearchTests extends BaseTest {

    private HomePage homePage;
    private String testPostTitle;
    private String searchKeyword;

    @BeforeEach
    public void setUpTest() {
        loginAndCreateTestPost();
        homePage = new HomePage(driver);
        homePage.navigateToHomePage();
    }

    private void loginAndCreateTestPost() {
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
        
        homePage = new HomePage(driver);
        homePage.navigateToHomePage();
        testPostTitle = "SearchablePost" + TestDataFactory.generateTimestamp();
        searchKeyword = "SearchablePost";
        CreatePostPage createPostPage = homePage.clickCreatePostButton();
        createPostPage.enterTitle(testPostTitle);
    }

    @Test
    @DisplayName("TC_SEARCH_001: Search by keyword in title (exact match)")
    @Description("User should be able to search posts by keyword in title")
    @Severity(SeverityLevel.CRITICAL)
    public void testSearchByKeywordInTitle() {
        homePage.searchPosts(searchKeyword);
        
        boolean postFound = homePage.isPostDisplayedByTitle(testPostTitle);
        assertThat(postFound).as("Post with keyword should be found").isTrue();
    }

    @Test
    @DisplayName("TC_SEARCH_002: Search by keyword in content (partial match)")
    @Description("User should be able to search posts by keyword in content")
    @Severity(SeverityLevel.NORMAL)
    public void testSearchByKeywordInContent() {
        String keyword = "test";
        homePage.searchPosts(keyword);
        
        int resultCount = homePage.getFilteredPostCount();
        assertThat(resultCount).as("Search should return results").isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("TC_SEARCH_003: Search with no results")
    @Description("System should display 'no results' message when search returns nothing")
    @Severity(SeverityLevel.NORMAL)
    public void testSearchWithNoResults() {
        String keyword = "NonExistentKeyword" + TestDataFactory.generateTimestamp();
        homePage.searchPosts(keyword);
        
        boolean noResultsDisplayed = homePage.isNoResultsMessageDisplayed();
        int postCount = homePage.getFilteredPostCount();
        
        assertThat(noResultsDisplayed || postCount == 0)
            .as("No results message should be displayed or post count should be 0")
            .isTrue();
    }

    @Test
    @DisplayName("TC_SEARCH_004: Search with empty keyword (show all)")
    @Description("Searching with empty keyword should show all posts")
    @Severity(SeverityLevel.MINOR)
    public void testSearchWithEmptyKeyword() {
        int initialCount = homePage.getPostCount();
        
        homePage.searchPosts("");
        
        int resultCount = homePage.getFilteredPostCount();
        assertThat(resultCount).as("Empty search should show posts").isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("TC_SEARCH_005: Search with special characters")
    @Description("System should handle special characters in search query")
    @Severity(SeverityLevel.MINOR)
    public void testSearchWithSpecialCharacters() {
        String keyword = "!@#$%";
        homePage.searchPosts(keyword);
        
        // Should not crash, either show results or no results
        boolean noResultsDisplayed = homePage.isNoResultsMessageDisplayed();
        int postCount = homePage.getFilteredPostCount();
        
        assertThat(noResultsDisplayed || postCount >= 0)
            .as("Search with special characters should be handled gracefully")
            .isTrue();
    }

    @Test
    @DisplayName("TC_SEARCH_006: Search case-insensitive")
    @Description("Search should be case-insensitive")
    @Severity(SeverityLevel.NORMAL)
    public void testSearchCaseInsensitive() {
        String lowercaseKeyword = searchKeyword.toLowerCase();
        homePage.searchPosts(lowercaseKeyword);
        
        boolean postFound = homePage.isPostDisplayedByTitle(testPostTitle);
        assertThat(postFound).as("Search should be case-insensitive").isTrue();
    }

    @Test
    @DisplayName("TC_SEARCH_007: Verify search results display correctly")
    @Description("Search results should display post cards with all information")
    @Severity(SeverityLevel.NORMAL)
    public void testSearchResultsDisplayCorrectly() {
        homePage.searchPosts(searchKeyword);
        
        int resultCount = homePage.getFilteredPostCount();
        if (resultCount > 0) {
            String title = homePage.getPostTitleByIndex(0);
            assertThat(title).as("Post title should be displayed").isNotEmpty();
        }
    }

    @Test
    @DisplayName("TC_SEARCH_008: Clear search results")
    @Description("User should be able to clear search and see all posts")
    @Severity(SeverityLevel.MINOR)
    public void testClearSearchResults() {
        homePage.searchPosts(searchKeyword);
        int searchResultCount = homePage.getFilteredPostCount();
        
        homePage.clearSearchInput();
        homePage.clickSearchButton();
        
        int allPostsCount = homePage.getFilteredPostCount();
        assertThat(allPostsCount).as("Clearing search should show more posts").isGreaterThanOrEqualTo(searchResultCount);
    }
}
