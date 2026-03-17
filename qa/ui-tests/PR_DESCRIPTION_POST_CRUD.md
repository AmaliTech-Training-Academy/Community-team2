# Pull Request: Post CRUD UI Automation Tests

## 📋 Overview
This PR implements comprehensive UI automation tests for Post Management (Create, Read, Update, Delete) functionality in the CommunityBoard application using Selenium WebDriver, JUnit 5, and Page Object Model pattern.

## 🎯 Scope
**Branch**: `feature/post-crud-ui`  
**Test Cases Covered**: TC_POST_001 to TC_POST_028 (28 test cases)  
**User Stories**: US3 (Create Post), US4 (View Posts), US5 (Edit Post), US6 (Delete Post)

## 📦 Files Added

### Page Objects (3 files - 900+ lines)
1. **HomePage.java** (FeedPage)
   - Main feed/home page after login
   - Post list display and interaction
   - Create post button
   - Search and filter functionality
   - Methods: `clickCreatePostButton()`, `getPostCount()`, `clickPostByTitle()`, `searchPosts()`
   - Post card data extraction: title, content, category, author, timestamp

2. **CreatePostPage.java** (PostModal)
   - Create and edit post form
   - Form fields: title input, content textarea, category dropdown, image upload
   - Methods: `fillPostForm()`, `submitPost()`, `uploadImage()`, `removeImage()`
   - Validation error handling: `getTitleError()`, `getContentError()`, `getCategoryError()`
   - Image preview support

3. **PostDetailPage.java**
   - Individual post detail view
   - Display methods: `getPostTitle()`, `getPostContent()`, `getPostAuthor()`, `getPostCategory()`
   - Action buttons: `clickEditButton()`, `clickDeleteButton()`
   - Delete confirmation: `confirmDelete()`, `cancelDelete()`
   - Permission checks: `isEditButtonVisible()`, `isDeleteButtonVisible()`

### Test Classes (4 files - 1,000+ lines)

4. **CreatePostTests.java** - 10 test cases
   - ✅ TC_POST_001: Create post without image (positive)
   - ✅ TC_POST_002: Create post with image (positive)
   - ✅ TC_POST_003: Empty title validation (negative)
   - ✅ TC_POST_004: Empty content validation (negative)
   - ✅ TC_POST_005: Missing category validation (negative)
   - ✅ TC_POST_006: Title max length (boundary)
   - ✅ TC_POST_007: Content max length (boundary)
   - ✅ TC_POST_008: Special characters in title (boundary)
   - ✅ TC_POST_009: Post appears in feed after creation (functional)
   - ✅ TC_POST_010: Category badge displays correctly (UI/UX)

5. **ViewPostTests.java** - 6 test cases
   - ✅ TC_POST_011: View post list in feed
   - ✅ TC_POST_012: Click post to view details
   - ✅ TC_POST_013: Post details display correctly
   - ✅ TC_POST_014: Timestamp format verification
   - ✅ TC_POST_015: Author name displays
   - ✅ TC_POST_016: Category displays on post card

6. **EditPostTests.java** - 6 test cases
   - ✅ TC_POST_017: Edit own post title successfully
   - ✅ TC_POST_018: Edit own post content successfully
   - ✅ TC_POST_019: Edit own post category successfully
   - ✅ TC_POST_020: Edit button visible only for own posts
   - ✅ TC_POST_021: Edit button not visible for others' posts
   - ✅ TC_POST_022: Updated post displays changes in feed

7. **DeletePostTests.java** - 6 test cases
   - ✅ TC_POST_023: Delete own post successfully
   - ✅ TC_POST_024: Delete button visible only for own posts
   - ✅ TC_POST_025: Delete button not visible for others' posts
   - ✅ TC_POST_026: Confirmation dialog on delete
   - ✅ TC_POST_027: Post removed from feed after deletion
   - ✅ TC_POST_028: Cancel delete operation

## 🏗️ Architecture & Design

### SOLID Principles
- **Single Responsibility**: Each page class handles ONLY its page's UI interactions
- **Open/Closed**: Extensible through inheritance from BaseTest
- **Liskov Substitution**: All test classes can substitute BaseTest
- **Interface Segregation**: Focused page interfaces
- **Dependency Inversion**: Tests depend on page abstractions

### Key Features
✅ **Page Object Model** - Clean separation of locators and test logic  
✅ **Page Factory** - @FindBy annotations with data-testid locators  
✅ **Explicit Waits** - WaitUtils used throughout, no Thread.sleep()  
✅ **Allure Reporting** - @Epic, @Feature, @Story, @Step, @Severity annotations  
✅ **Fluent Assertions** - AssertJ for readable test assertions  
✅ **Test Data Factory** - Dynamic data generation for unique posts  
✅ **Method Chaining** - Page navigation returns new page instances  
✅ **Comprehensive Comments** - Every method and action explained  
✅ **Error Handling** - Try-catch for optional elements  
✅ **Image Upload Support** - File path handling for image uploads  
✅ **Dropdown Handling** - Selenium Select for category dropdown  
✅ **Confirmation Dialogs** - Delete confirmation handling  

## 🧪 Test Coverage

### Test Types
- **Positive Tests**: 4 (successful create, view, edit, delete)
- **Negative Tests**: 3 (empty fields validation)
- **Validation Tests**: 3 (field-level error messages)
- **Boundary Tests**: 3 (max length, special characters)
- **Functional Tests**: 6 (post appears in feed, updates display, deletion removes)
- **UI/UX Tests**: 5 (button visibility, category display, timestamp format)
- **Permission Tests**: 4 (edit/delete button visibility for own vs others' posts)

### Categories Tested
- NEWS, EVENT, DISCUSSION, ALERT (all 4 categories)

### CRUD Operations
- ✅ **Create**: With/without image, validation, boundary cases
- ✅ **Read**: List view, detail view, data display
- ✅ **Update**: Title, content, category editing
- ✅ **Delete**: Confirmation, cancellation, feed removal

## 🔧 Technical Implementation

### Locator Strategy
- Primary: `data-testid` attributes (e.g., `[data-testid='post-title-input']`)
- Fallback: `name` attributes, CSS classes, XPath by text

### Wait Strategy
- Explicit waits using `WaitUtils` class
- `waitForElementVisible()`, `waitForElementClickable()`
- No `Thread.sleep()` except one brief wait for search results (to be improved)

### Test Data
- Unique post titles using `TestDataFactory.generatePostTitle()`
- Random categories using `TestDataFactory.generateRandomCategory()`
- Long strings for boundary testing using `TestDataFactory.generateLongString()`

### Test Setup
- Each test class has `@BeforeEach` setup creating test user and logging in
- Helper method `loginAndCreateTestPost()` for consistent test data
- Fresh browser instance per test (inherited from BaseTest)

## 📊 Allure Reporting
All tests include:
- `@Epic("Post Management")`
- `@Feature("Create Post" | "View Posts" | "Edit Post" | "Delete Post")`
- `@Story("US3" | "US4" | "US5" | "US6")`
- `@Severity(SeverityLevel.CRITICAL | NORMAL | MINOR)`
- `@Step` annotations for detailed action logging

## 🚀 Running Tests

### Run All Post Tests
```bash
mvn clean test -Dtest=CreatePostTests,ViewPostTests,EditPostTests,DeletePostTests
```

### Run Specific Test Class
```bash
mvn clean test -Dtest=CreatePostTests
```

### Generate Allure Report
```bash
mvn clean test
mvn allure:serve
```

## 📝 Dependencies
- Selenium WebDriver 4.18.1
- JUnit 5.10.2
- AssertJ 3.25.3
- Allure 2.25.0
- WebDriverManager 5.7.0

## ✅ Checklist
- [x] All 28 test cases implemented
- [x] Page Object Model pattern followed
- [x] SOLID principles applied
- [x] Explicit waits used throughout
- [x] Allure annotations added
- [x] Comprehensive comments added
- [x] Test data factory integration
- [x] Error handling implemented
- [x] Method chaining for page navigation
- [x] Image upload support added
- [x] Delete confirmation handling
- [x] Permission checks (own vs others' posts)

## 🔗 Related PRs
- **Branch 1**: `chore/allure-setup-and-base-class` - Foundation and base test infrastructure
- **Branch 2**: `feature/auth-ui-tests` - Authentication tests (registration, login)

## 📌 Notes
- Image upload tests require test image file at `src/test/resources/test-image.jpg`
- Tests assume user must be logged in to access post management features
- Edit/delete button visibility tests verify permission model (own posts only)
- Delete confirmation dialog handling ensures safe deletion workflow

## 🎯 Next Steps
After this PR is merged, the next branch will implement:
- **Branch 4**: `feature/comment-and-search-ui` - Comment and search/filter functionality
- **Branch 5**: `feature/image-upload-ui` - Image upload, subscriptions, analytics

## 👥 Reviewers
Please review:
1. Page Object Model structure and separation of concerns
2. Test coverage completeness (all 28 test cases)
3. Locator strategy (data-testid usage)
4. Wait strategy (explicit waits)
5. Code comments and documentation
6. Allure annotations and reporting setup

---

**Total Lines Added**: ~1,900 lines  
**Test Cases**: 28  
**Page Objects**: 3  
**Test Classes**: 4  
**Coverage**: Create, Read, Update, Delete operations for posts
