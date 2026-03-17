# Project Structure Documentation

## Complete File Tree
```
qa/ui-tests/
├── pom.xml                                    # Maven configuration with all dependencies
├── README.md                                  # Setup and execution instructions
├── PROJECT_STRUCTURE.md                       # This file
├── .gitignore                                 # Git ignore patterns for build artifacts
│
├── src/test/java/com/communityboard/
│   │
│   ├── base/
│   │   └── BaseTest.java                      # Base class for all tests
│   │                                          # - WebDriver initialization
│   │                                          # - Browser configuration (headless for CI)
│   │                                          # - Setup (@BeforeEach) and teardown (@AfterEach)
│   │                                          # - Screenshot capture on failure
│   │
│   ├── pages/                                 # Page Object classes (to be added in feature branches)
│   │   ├── LandingPage.java                   # Root URL page (Welcome/Sign in)
│   │   ├── LoginPage.java                     # Login page (/login)
│   │   ├── RegisterPage.java                  # Registration page (/register)
│   │   ├── HomePage.java / FeedPage.java      # Main feed after login
│   │   ├── CreatePostPage.java                # Create/edit post modal
│   │   ├── PostDetailPage.java                # Individual post view
│   │   ├── SubscriptionSettingsPage.java      # Category subscription settings
│   │   └── AnalyticsDashboardPage.java        # Moderator analytics dashboard
│   │
│   ├── tests/                                 # Test classes organized by feature
│   │   ├── SetupVerificationTest.java         # Infrastructure smoke test
│   │   ├── RegistrationTests.java             # User registration tests (Branch 2)
│   │   ├── LoginTests.java                    # User login tests (Branch 2)
│   │   ├── CreatePostTests.java               # Create post tests (Branch 3)
│   │   ├── ViewPostTests.java                 # View post tests (Branch 3)
│   │   ├── EditPostTests.java                 # Edit post tests (Branch 3)
│   │   ├── DeletePostTests.java               # Delete post tests (Branch 3)
│   │   ├── CommentTests.java                  # Comment tests (Branch 4)
│   │   ├── SearchTests.java                   # Search tests (Branch 4)
│   │   ├── FilterTests.java                   # Filter tests (Branch 4)
│   │   ├── ImageUploadTests.java              # Image upload tests (Branch 5)
│   │   ├── SubscriptionTests.java             # Category subscription tests (Branch 5)
│   │   └── AnalyticsDashboardTests.java       # Analytics tests (Branch 5)
│   │
│   └── utils/                                 # Utility classes
│       ├── WaitUtils.java                     # Explicit wait helper methods
│       │                                      # - waitForElementVisible
│       │                                      # - waitForElementClickable
│       │                                      # - waitForElementToDisappear
│       │                                      # - waitForTextToBePresentInElement
│       │                                      # - waitForUrlContains, etc.
│       │
│       ├── ScreenshotUtils.java               # Screenshot capture utilities
│       │                                      # - captureScreenshot (Allure @Attachment)
│       │                                      # - captureScreenshotWithName
│       │                                      # - captureStepScreenshot
│       │
│       └── TestDataFactory.java               # Test data generation
│                                              # - generateUniqueEmail
│                                              # - generateValidPassword
│                                              # - generatePostTitle/Content
│                                              # - generateInvalidEmail (negative tests)
│                                              # - generateSqlInjectionString (security tests)
│
└── src/test/resources/
    └── allure.properties                      # Allure report configuration

```

## Branch Implementation Plan

### Branch 1: chore/allure-setup-and-base-class ✅ (CURRENT)
**Status**: Complete - Ready for push
**Files Created**:
- pom.xml (all dependencies)
- BaseTest.java (driver lifecycle)
- WaitUtils.java (explicit waits)
- ScreenshotUtils.java (Allure screenshots)
- TestDataFactory.java (test data generation)
- allure.properties (Allure config)
- .gitignore (build artifacts)
- README.md (documentation)
- SetupVerificationTest.java (smoke test)

### Branch 2: feature/auth-ui-tests
**Files to Create**:
- pages/LandingPage.java
- pages/LoginPage.java
- pages/RegisterPage.java
- tests/RegistrationTests.java (TC_AUTH_001 to TC_AUTH_012)
- tests/LoginTests.java (TC_AUTH_013 to TC_AUTH_025)

### Branch 3: feature/post-crud-ui
**Files to Create**:
- pages/HomePage.java (or FeedPage.java)
- pages/CreatePostPage.java (or PostModal.java)
- pages/PostDetailPage.java
- pages/EditPostPage.java (or EditPostModal.java)
- tests/CreatePostTests.java (TC_POST_001 to TC_POST_010)
- tests/ViewPostTests.java (TC_POST_011 to TC_POST_016)
- tests/EditPostTests.java (TC_POST_017 to TC_POST_022)
- tests/DeletePostTests.java (TC_POST_023 to TC_POST_028)

### Branch 4: feature/comment-and-search-ui
**Files to Create/Update**:
- pages/PostDetailPage.java (update with comment section)
- pages/HomePage.java (update with search/filter)
- tests/CommentTests.java (TC_COMMENT_001 to TC_COMMENT_010)
- tests/SearchTests.java (TC_SEARCH_001 to TC_SEARCH_008)
- tests/FilterTests.java (TC_SEARCH_009 to TC_SEARCH_020)

### Branch 5: feature/image-upload-ui
**Files to Create/Update**:
- pages/CreatePostPage.java (update with image upload focus)
- pages/SubscriptionSettingsPage.java
- pages/AnalyticsDashboardPage.java
- tests/ImageUploadTests.java (TC_IMAGE_001 to TC_IMAGE_015)
- tests/SubscriptionTests.java (TC_NOTIF_001 to TC_NOTIF_010)
- tests/AnalyticsDashboardTests.java (TC_ANAL_001 to TC_ANAL_010)

## Key Design Decisions

### 1. Page Object Model (POM)
- **Separation of Concerns**: Page classes contain ONLY locators and UI actions
- **No Assertions in Pages**: All assertions in test classes
- **Page Factory**: @FindBy annotations for clean locator management
- **Reusability**: Page methods can be reused across multiple tests

### 2. Explicit Waits
- **No Thread.sleep()**: All waits use WebDriverWait + ExpectedConditions
- **WaitUtils**: Centralized wait logic for consistency
- **Reliability**: Waits for actual conditions rather than arbitrary delays

### 3. Test Data Generation
- **Unique Data**: TestDataFactory generates unique emails, timestamps
- **No Hardcoding**: Dynamic test data prevents conflicts
- **Boundary Testing**: Methods for long strings, special characters, etc.

### 4. Allure Reporting
- **Rich Reports**: Screenshots, steps, descriptions
- **Annotations**: @Epic, @Feature, @Story, @Severity for organization
- **Traceability**: Links test cases to user stories

### 5. SOLID Principles
- **Single Responsibility**: Each class has one clear purpose
- **Open/Closed**: Extensible through inheritance
- **Dependency Inversion**: Tests depend on page abstractions

## Running Tests by Branch

### After Branch 1 (Setup Verification)
```bash
mvn clean test -Dtest=SetupVerificationTest
```

### After Branch 2 (Auth Tests)
```bash
mvn clean test -Dtest=RegistrationTests,LoginTests
```

### After Branch 3 (Post CRUD)
```bash
mvn clean test -Dtest=CreatePostTests,ViewPostTests,EditPostTests,DeletePostTests
```

### After Branch 4 (Comments & Search)
```bash
mvn clean test -Dtest=CommentTests,SearchTests,FilterTests
```

### After Branch 5 (Image Upload & More)
```bash
mvn clean test -Dtest=ImageUploadTests,SubscriptionTests,AnalyticsDashboardTests
```

### Run All Tests (After All Branches)
```bash
mvn clean test
mvn allure:serve
```

## Estimated Test Count by Branch
- Branch 1: 2 tests (setup verification)
- Branch 2: ~25 tests (auth module)
- Branch 3: ~28 tests (post CRUD)
- Branch 4: ~30 tests (comments, search, filter)
- Branch 5: ~35 tests (image, subscriptions, analytics)
- **Total**: ~120 UI functional tests
