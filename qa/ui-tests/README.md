# CommunityBoard UI Automation Tests

## Overview
Comprehensive Selenium-based UI automation test suite for the CommunityBoard application (neighborhood bulletin board platform). Built using Page Object Model (POM) pattern with Java 17, JUnit 5, and Allure reporting.

## Technology Stack
- **Language**: Java 17
- **Build Tool**: Maven
- **Test Framework**: JUnit 5 (Jupiter)
- **Browser Automation**: Selenium WebDriver 4.18.1
- **Driver Management**: WebDriverManager (automatic ChromeDriver setup)
- **Assertions**: AssertJ (fluent assertions)
- **Reporting**: Allure Framework
- **Design Pattern**: Page Object Model with Page Factory

## Project Structure
```
qa/ui-tests/
├── src/test/java/com/communityboard/
│   ├── base/
│   │   └── BaseTest.java              # Base test class with driver setup/teardown
│   ├── pages/                         # Page Object classes (one per page)
│   │   ├── LandingPage.java
│   │   ├── LoginPage.java
│   │   ├── RegisterPage.java
│   │   └── ...
│   ├── tests/                         # Test classes organized by feature
│   │   ├── AuthTests.java
│   │   ├── PostTests.java
│   │   └── ...
│   └── utils/                         # Utility classes
│       ├── WaitUtils.java             # Explicit wait helpers
│       ├── ScreenshotUtils.java       # Screenshot capture
│       └── TestDataFactory.java       # Test data generation
├── src/test/resources/
│   └── allure.properties              # Allure configuration
├── pom.xml                            # Maven dependencies and plugins
└── README.md                          # This file
```

## Prerequisites
- Java 17 or higher
- Maven 3.6+
- Chrome browser (latest version)
- Internet connection (for WebDriverManager to download ChromeDriver)


## Setup Instructions

### 1. Clone Repository
```bash
git clone <repository-url>
cd qa/ui-tests
```

### 2. Verify Java Installation
```bash
java -version
# Should show Java 17 or higher
```

### 3. Verify Maven Installation
```bash
mvn -version
# Should show Maven 3.6+ and Java 17+
```

### 4. Install Dependencies
```bash
mvn clean install
```

## Running Tests

### Run All Tests
```bash
mvn clean test
```

### Run Specific Test Class
```bash
mvn clean test -Dtest=RegistrationTests
mvn clean test -Dtest=LoginTests
```

### Run Specific Test Method
```bash
mvn clean test -Dtest=RegistrationTests#testSuccessfulRegistration
```

### Run Tests in Headless Mode (CI)
```bash
export CI=true
mvn clean test
```

### Run Tests with Allure Report Generation
```bash
mvn clean test allure:report
```

## Viewing Allure Reports

### Generate and Open Report (Recommended)
```bash
mvn allure:serve
```
This command generates the report and opens it in your default browser automatically.

### Generate Report Only
```bash
mvn allure:report
```
Report will be generated in `target/site/allure-maven-plugin/` directory.

### View Existing Report
```bash
mvn allure:serve
```

## Test Coverage

### Authentication Module (feature/auth-ui-tests)
- User Registration (positive, negative, validation, boundary)
- User Login (positive, negative, validation)
- Field validation and error messages

### Post Management (feature/post-crud-ui)
- Create posts (with/without images)
- View posts (list and detail)
- Edit own posts
- Delete own posts
- Category display and filtering

### Comments & Search (feature/comment-and-search-ui)
- Add comments to posts
- View comments
- Search posts by keyword
- Filter posts by category and date

### Image Upload (feature/image-upload-ui)
- Upload valid images (JPG, PNG)
- Validate file size limits
- Validate file type restrictions
- Image preview and display

### Subscriptions & Analytics (feature/image-upload-ui)
- Subscribe to categories
- View subscription settings
- Moderator analytics dashboard

## Design Principles

### SOLID Principles
- **Single Responsibility**: Each class has one clear purpose
  - Page classes: Handle ONLY locators and UI actions
  - Test classes: Handle ONLY test logic and assertions
  - Utility classes: Handle ONLY specific utilities (waits, screenshots, data)
- **Open/Closed**: Extensible through inheritance (BaseTest)
- **Liskov Substitution**: All test classes can substitute BaseTest
- **Interface Segregation**: Focused utility interfaces
- **Dependency Inversion**: Tests depend on abstractions (Page Objects)

### Best Practices
- **No Thread.sleep()**: Always use explicit waits (WebDriverWait)
- **Page Factory**: @FindBy annotations for clean locator management
- **Fluent Assertions**: AssertJ for readable test assertions
- **Test Isolation**: Fresh browser instance per test (@BeforeEach/@AfterEach)
- **Screenshot on Failure**: Automatic capture for debugging
- **Comprehensive Comments**: Every non-trivial line explained

## CI/CD Integration

### GitHub Actions Example
```yaml
- name: Run UI Tests
  run: |
    export CI=true
    cd qa/ui-tests
    mvn clean test
    
- name: Generate Allure Report
  run: |
    cd qa/ui-tests
    mvn allure:report
```

### Environment Variables
- `CI=true`: Enables headless mode for CI environments

## Troubleshooting

### ChromeDriver Issues
WebDriverManager handles driver downloads automatically. If issues occur:
```bash
mvn clean test -Dwdm.forceDownload=true
```

### Port Conflicts
If Allure serve fails due to port conflict:
```bash
mvn allure:serve -Dallure.serve.port=8081
```

### Memory Issues
Increase Maven memory:
```bash
export MAVEN_OPTS="-Xmx1024m"
mvn clean test
```

## Contributing
1. Create feature branch from `feature/auth-users`
2. Follow existing code structure and naming conventions
3. Add comprehensive comments to all code
4. Ensure all tests pass before committing
5. Update README if adding new features

## Contact
For questions or issues, contact the QA team.
