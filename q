[33m14f0f19[m[33m ([m[1;36mHEAD[m[33m -> [m[1;32mfeature/backend/crud-operations[m[33m)[m Merge branch 'Develop' of https://github.com/AmaliTech-Training-Academy/Community-team2 into feature/backend/crud-operations
[33m2eb7109[m[33m ([m[1;32mDevelop[m[33m)[m Merge pull request #41 from AmaliTech-Training-Academy/devops/cicd-pipeline-updates
[33m7d30a7a[m[33m ([m[1;31morigin/feature/backend/crud-operations[m[33m)[m feat: Add PostController and PostService for managing posts
[33md6b8d7f[m feat: Implement CRUD operations for Post and Category entities
[33m735c543[m Merge pull request #35 from AmaliTech-Training-Academy/feature/backend/crud-operations
[33m869c607[m Use PostgreSQL-compatible data.sql and disable SQL init for test profile
[33mc89dcb8[m Update data.sql with correct user schema (username, MEMBER role, provider) and use MERGE for H2/PostgreSQL compatibility
[33m78128f2[m Fix critical pipeline issues: backend test profiles, frontend build errors, remove redundant stage, add RDS credentials
[33m4d7b43c[m Update frontend Dockerfile, package.json, backend pom.xml, and CICD workflow
[33me54a96d[m Change spring profile from dev to test
[33m3d57d92[m Fix integration test ordering - run after unit tests
[33me6efc2b[m Remove RDS credentials from tests - use local test database
[33m015c54d[m Fix integration test JDBC URL format
[33m08032fb[m Fix JDBC connection string format for RDS
[33m7079535[m Fix Vitest integration test command - use --include instead of --testPathPattern
[33m7506cd2[m Temporarily disable Checkstyle to unblock pipeline - 589 violations to address
[33m8d17f81[m Enforce test failures in CI/CD pipeline - remove continue-on-error from linting and test steps
[33m92bf0ef[m Remove integration test file
[33md1f0dc8[m Update CI/CD pipeline: remove gitleaks, add integration tests, remove SCA, remove emojis
[33m871c54f[m Merge pull request #22 from AmaliTech-Training-Academy/frontend/feat/initial-setup
[33me2a2c18[m fix: update class name for word wrapping in Badge component
[33mce0a9b8[m Merge branch 'frontend/feat/initial-setup' of https://github.com/AmaliTech-Training-Academy/Community-team2 into frontend/feat/initial-setup
[33m8ccfdf4[m feat: implement initial setup with SVG icons and layout adjustments
[33mabda457[m Merge pull request #36 from AmaliTech-Training-Academy/frontend/feat/unit-testing-setup
[33m86a7936[m feat: add Comments SVG and update empty state messages in HomePage and PostDetailPage
[33mb2db9e8[m feat: add testing setup with vitest and create initial tests for components and stores
[33m6df93e5[m feat: Setup for Category and Post entities
[33m289776d[m refactor: clean up code by removing unnecessary comments and whitespace
[33mbf4109c[m refactor: remove SubscriptionModal component for code cleanup
[33mebec719[m Merge pull request #26 from AmaliTech-Training-Academy/feature/authentication
[33mf201629[m Merge pull request #31 from AmaliTech-Training-Academy/frontend/feat/post-management
[33m8546850[m refactor: remove comments from main.tsx for cleaner code
[33m5fd685e[m Merge pull request #30 from AmaliTech-Training-Academy/frontend/feat/user-registration
[33mb39dbc9[m[33m ([m[1;31morigin/frontend/feat/user-registration[m[33m)[m refactor: remove commented-out validation logic in main.tsx
[33me07741c[m build: update application properties
[33m19003b7[m build: update JAVA_VERSION to 21 in CICD configuration
[33m8e2d75a[m Merge pull request #28 from AmaliTech-Training-Academy/frontend/feat/post-categories
[33m8dea4e9[m build: update maven-compiler-plugin configuration to use Java version properties
[33mb1f8059[m Merge branch 'Develop' of https://github.com/AmaliTech-Training-Academy/Community-team2 into feature/authentication
[33ma56ef38[m test: Remove unused AccountProvider parameter from UserServiceTest
[33mb805400[m[33m ([m[1;31morigin/frontend/feat/post-categories[m[33m)[m refactor: remove comments and streamline JWT validation in main.tsx
[33m74556ca[m Merge pull request #24 from AmaliTech-Training-Academy/frontend/feat/post-management
[33m2c21e3a[m Merge pull request #23 from AmaliTech-Training-Academy/devops/aws-infrastructure-cicd
[33mdbcd843[m[33m ([m[1;31morigin/devops/aws-infrastructure-cicd[m[33m)[m chore: add .gitignore for devops to exclude terraform state files
[33md6356e9[m Merge branch 'Develop' of https://github.com/AmaliTech-Training-Academy/Community-team2 into feature/authentication
[33maa7307b[m[33m ([m[1;31morigin/frontend/feat/post-management[m[33m)[m feat: add PostModal and SubscriptionModal components for post creation/editing and subscription management
[33mf7137fa[m feat: add in-memory mock service for API interactions and implement dashboard, home, and login pages
[33mcbd70a0[m feat: add OFL license file and create TEST_IDS utility for consistent data-testid attributes
[33m2d8025d[m feat: restructure project by removing old components and adding new API and utility files
[33mec5662b[m feat: Enhance JWT authentication to include userId in claims and update authorization checks
[33m925132d[m feat: implement posts API, create post store, and add Badge and CategoryFilter components
[33m9d70ba9[m feat: implement user registration page and authentication API integration
[33mcdf7bf1[m Merge pull request #15 from AmaliTech-Training-Academy/feature/authentication
[33m9992ae2[m Merge branch 'Develop' into feature/authentication
[33m27035af[m feat: add Inter font files and SVG assets; implement Tailwind CSS and Vite configuration
[33m5679f4a[m Merge pull request #19 from AmaliTech-Training-Academy/devops/fix-workflow-trigger
[33me1f5fbf[m fix: use npm install instead of npm ci (missing package-lock.json)
[33m117e39f[m fix: remove npm cache from setup-node to avoid cache warnings
[33m91d4cab[m refactor: remove lint jobs to focus on tests and deployment
[33m008ee9a[m fix: remove gitleaks scan and allow checkstyle to continue on error
[33m3c48842[m fix: correct branch name case from develop to Develop in workflow triggers
[33m469fc5b[m Merge pull request #18 from AmaliTech-Training-Academy/devops/aws-infrastructure-cicd
[33m93fc020[m fix: Correct ELB service account for eu-west-1 region
[33m337be80[m feat: Add custom access denied and authentication entry point handlers
[33m7db855b[m feat: Implement UserController with CRUD operations and authentication endpoints
[33m5ddbff4[m feat: Add update user DTO and  improve on endpoint security
[33m80f668a[m feat: Update password validation regex and enhance user update logic
[33m8ec6913[m feat: Enhance CORS configuration and add OpenAPI documentation
[33m766b21c[m feat: Implement SecurityFilterChain Bean and enhance auth dtos
[33m7e6ad35[m feat: Implement Tokenblacklist service and dto
[33mce1c01c[m feat: Implement JWT authentication filter, service, and utility for token management
[33mda174e8[m feat: Implement custom user details and user details service for authentication
[33ma85dbb6[m feat: Add CORS configuration and implement caching for JWT tokens
[33mb7bfb73[m Merge pull request #17 from AmaliTech-Training-Academy/devops/aws-infrastructure-cicd
[33m30dd823[m fix: Move workflow to correct location and add PR template
[33m2da1a73[m feat: Enhance authentication flow with validation, user service methods, and unit tests
[33m7cc019d[m docs: Update README to reflect actual implementation
[33mf884601[m feat: Add AWS infrastructure with Terraform and production-ready CI/CD pipeline
[33ma422e93[m feat: Add authentication request and response DTOs, user role and account provider enums
[33m815afae[m feat: Add user model, repository, mapper, and interface for authentication
[33m212ca13[m feat: Implement global exception handling and custom exceptions for better error management
[33mccb77da[m chore:Add Docker configuration and update application properties for authentication
[33m4db4a23[m[33m ([m[1;32mmain[m[33m)[m initial commit
