# Community Board Backend

Backend service for a community discussion board, providing RESTful APIs for user management, authentication, posts, comments, categories, and subscriptions.

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Features](#features)
- [Domain Model](#domain-model)
- [Architecture](#architecture)
- [Configuration & Environment](#configuration--environment)
- [Database](#database)
- [API Overview](#api-overview)
- [Getting Started](#getting-started)
- [Testing](#testing)
- [Deployment](#deployment)
- [Contributing](#contributing)
- [License](#license)

## Overview

This project is a Spring Boot–based backend for a **community discussion board**. It exposes a REST API that allows clients to:

- Register and authenticate users using email/username and password.
- Manage user profiles and roles.
- Create, read, update, and delete posts.
- Comment on posts with support for threaded replies.
- Organize content into categories.
- Subscribe to categories and receive notifications.

OpenAPI/Swagger documentation is available via Springdoc:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Tech Stack

- **Language & Runtime:** Java 21
- **Framework:** Spring Boot 4.0.3
- **Core Modules:**
  - `spring-boot-starter-webmvc` – REST API and MVC infrastructure
  - `spring-boot-starter-data-jpa` – ORM and database access
  - `spring-boot-starter-security` – authentication & authorization
  - `spring-boot-starter-validation` – bean validation
  - `spring-boot-starter-cache` with Caffeine – caching
  - `spring-boot-starter-thymeleaf` – server-side templating (used for email templates)
  - `spring-boot-starter-mail` – email delivery
- **Persistence:**
  - PostgreSQL (dev/profile `dev`)
  - H2 in-memory database (tests)
- **Security & Auth:**
  - Spring Security
  - JWT (jjwt-api/jjwt-impl/jjwt-jackson)
- **API Documentation:**
  - Springdoc OpenAPI (`springdoc-openapi-starter-webmvc-ui`)
- **Object Mapping:**
  - MapStruct
  - Jackson (with custom configuration)
- **Other Libraries:**
  - Lombok – boilerplate reduction
  - Cloudinary HTTP5 client – media/image storage
  - Mockito, Spring Boot Test, Spring Security Test – testing
- **Build & Tooling:**
  - Maven
  - Checkstyle (`checkstyle.xml`) with `maven-checkstyle-plugin`

## Features

### User Management & Authentication

- User registration (username, email, password) with validation and uniqueness constraints.
- Email/username-based login with JWT access tokens.
- Refresh token support via HTTP-only cookies.
- Logout endpoint that blacklists tokens and clears refresh cookies.
- Fetch current authenticated user (`/api/v1/users/me`).
- Admin-only listing of all users.
- Update and delete user accounts with owner-or-admin access control.
- Forgot password flow that triggers an email with a reset link (integrates with configurable frontend URL).

### Posts & Content

- Create posts with title, content, category, and optional image upload (stored via Cloudinary).
- View a paginated list of posts with support for filtering via a `PostFilter` DTO (e.g., by category, author).
- Retrieve posts by ID, and by user (`/api/v1/posts/by-user/{userId}`).
- Track post view counts.
- Update and delete posts, restricted to owners or admins via `@PreAuthorize` checks and `UserSecurity` helpers.

### Comments

- Create comments on posts as an authenticated user.
- Retrieve comments in a paginated fashion.
- List comments by post and by user.
- Support for threaded comments via a `parent_comment_id` relationship.
- Update and delete comments with ownership and admin checks.

### Categories

- Public listing of all categories with pagination.
- Retrieve a single category by ID.
- Categories are uniquely named and can include descriptions.

### Subscriptions & Notifications

- Subscribe to a category as an authenticated user.
- Unsubscribe from categories.
- Retrieve all subscriptions for the current user.
- Subscription preferences:
  - Immediate notifications toggle
  - Daily recap toggle
  - Muted flag
- Scheduled daily recap via `DailyRecapScheduler`, using Spring’s scheduling infrastructure and `NotificationService`.

### Security & Authorization

- Stateless JWT-based authentication with a custom `JwtAuthenticationFilter`.
- Spring Security configuration that:
  - Permits unauthenticated access to login, registration, refresh, forgot-password, and public docs.
  - Protects most API endpoints behind `isAuthenticated()`.
  - Uses role-based and ownership-based access (`@PreAuthorize`, `UserSecurity`).
- Custom `RestAuthenticationEntryPoint` and `RestAccessDeniedHandler` for consistent JSON error responses.
- Token blacklist service to revoke JWTs (e.g., on logout).

### Developer Experience

- Rich OpenAPI documentation (`OpenApiConfig`) describing main flows and contact info.
- CORS configured via `CorsConfig` and properties to allow common frontend origins.
- Dedicated `dev` and `test` profiles.
- MapStruct-based DTO mapping for clean separation between entities and API models.
- Caffeine caching (see `CacheConfig` and `PostCacheKeyGenerator`) for performance optimizations.

## Domain Model

### User

- Entity: `com.amalitech.communityboard.models.User`
- Table: `users`
- Key fields:
  - `id` – primary key
  - `username` – unique, 3–20 characters, alphanumeric and spaces
  - `email` – unique, valid email
  - `password` – stored as a bcrypt hash
  - `role` – enum (`UserRole`), defaults to `MEMBER`
  - `provider` – enum (`AccountProvider`), defaults to `LOCAL`
  - `createdAt` – auto-populated

### Post

- Entity: `com.amalitech.communityboard.models.Post`
- Table: `posts`
- Key fields:
  - `id`
  - `title` – required
  - `content` – required, stored as `TEXT`
  - `author` – `ManyToOne` reference to `User`
  - `category` – `ManyToOne` reference to `Category`
  - `createdAt` – timestamp
  - `viewCount` – integer view counter
  - `imageUrl` – optional image URL (Cloudinary)

### Comment

- Entity: `com.amalitech.communityboard.models.Comment`
- Table: `comments`
- Key fields:
  - `id`
  - `post` – `ManyToOne` to `Post`
  - `user` – `ManyToOne` to `User`
  - `content` – required text
  - `createdAt` – timestamp
  - `parent` – optional `ManyToOne` to another `Comment` (threaded replies)

### Category

- Entity: `com.amalitech.communityboard.models.Category`
- Table: `categories`
- Key fields:
  - `id`
  - `name` – unique, minimum length 2
  - `description` – optional
  - `createdAt` – auto-updated timestamp

### Subscription

- Entity: `com.amalitech.communityboard.models.Subscription`
- Table: `subscriptions` with unique constraint on `(user_id, category_id)`
- Key fields:
  - `id`
  - `user` – `ManyToOne` to `User`
  - `category` – `ManyToOne` to `Category`
  - `immediateNotificationsEnabled` – boolean, default `true`
  - `dailyRecapEnabled` – boolean, default `true`
  - `muted` – boolean, default `false`
  - `createdAt`, `updatedAt`

## Architecture

The backend follows a conventional layered Spring Boot architecture:

- **Controller layer** (`controller` package)
  - REST endpoints for users, posts, comments, categories, and subscriptions.
  - Swagger annotations (`@Operation`, `@ApiResponses`, `@Tag`) document endpoints.
- **Service layer** (`service.interfaces`, `service.implementations`)
  - Business logic isolated in services like `UserService`, `PostService`, `CommentService`, `CategoryService`, `SubscriptionServiceImpl`, and `NotificationServiceImpl`.
  - Interfaces for clear contracts and easier testing.
- **Repository layer** (`repository`)
  - Spring Data JPA repositories for each aggregate: `UserRepository`, `PostRepository`, `CommentRepository`, `CategoryRepository`, `SubscriptionRepository`.
  - Specifications (e.g., `PostSpecifications`) to support flexible querying.
- **DTO & Mapping layer**
  - Request/response DTOs in the `dto` package encapsulate API contracts.
  - MapStruct mappers (e.g., `SubscriptionMapper`) transform between entities and DTOs.
- **Security layer** (`security`)
  - Custom user details, JWT utilities, filters, token service, user ownership checks.
- **Configuration** (`config`)
  - `SecurityConfig` – Spring Security filter chain and authorization rules.
  - `OpenApiConfig` – OpenAPI metadata.
  - `CorsConfig` – CORS configuration source.
  - `CacheConfig` – cache manager and related cache settings.
  - `CloudinaryConfig` – Cloudinary client configuration.
  - `AsyncConfig` – asynchronous execution if used by notification/scheduler.
- **Scheduling**
  - `DailyRecapScheduler` – scheduled job to send daily recap notifications to subscribed users.

## Configuration & Environment

### Spring Profiles

- **`dev`** (default via `spring.profiles.active=dev` in `application.properties`)
  - Connects to a PostgreSQL database defined in `application-dev.properties`.
- **`test`**
  - Uses an in-memory H2 database.
  - Enables Swagger/OpenAPI even during tests.
  - Provides safe default values for external services (Cloudinary, SMTP, frontend URLs).

### Key Properties

From `src/main/resources/application.properties`:

- Application
  - `spring.application.name=communityBuilder`
  - `server.port=8080`
  - `spring.jpa.hibernate.ddl-auto=update`
- JWT & Auth
  - `app.jwt.secret` – JWT signing secret (defaulted via `SECRET` env var)
  - `app.jwt.expiration-ms` – access token lifetime (default 1h)
  - `app.jwt.issuer` – token issuer string
  - `app.custom.jwt-refresh.expiration-ms` – refresh token lifetime (1 week)
  - `app.cookie.max-age` – refresh cookie max age (30 days)
- CORS
  - `app.cors.allowed-origins` – comma-separated list of allowed origins
- Mail
  - `spring.mail.host`, `spring.mail.port`, `spring.mail.username`, `spring.mail.password`
  - `spring.mail.properties.mail.smtp.auth`
  - `spring.mail.properties.mail.smtp.starttls.enable`
- Frontend Integration
  - `app.frontend.rest-password` – base URL for password reset links, parameterized by `FRONTEND_URL_RESET`
  - `app.frontend` – frontend URL, configured via `FRONTEND_URL`
- Caching
  - `spring.cache.type=caffeine`
- Cloudinary
  - `cloudinary.cloud-name`, `cloudinary.api-key`, `cloudinary.api-secret`
- Multipart
  - `spring.servlet.multipart.max-file-size=10MB`
  - `spring.servlet.multipart.max-request-size=10MB`

### Required Environment Variables

For non-test environments, you should provide these as environment variables or via a config server/secret store:

- `SECRET` – JWT signing secret (Base64-encoded string recommended)
- `EMAIL` – SMTP username/email
- `EMPASS` – SMTP password
- `CLOUD_NAME` – Cloudinary cloud name
- `CLOUD_API` – Cloudinary API key
- `CLOUD_SECRET` – Cloudinary API secret
- `FRONTEND_URL` – base URL of the frontend
- `FRONTEND_URL_RESET` – base URL for password reset flow

Test profile (`application-test.properties`) provides safe defaults for local automated tests.

## Database

### Dev / Prod

- Uses **PostgreSQL** when the `dev` profile is active, configured via `application-dev.properties`:
  - `spring.datasource.driver-class-name=org.postgresql.Driver`
  - `spring.datasource.url=jdbc:postgresql://communityboard-db-public.cfuou2mwe5a1.eu-west-1.rds.amazonaws.com:5432/communityboard`
  - `spring.datasource.username=postgres`
  - `spring.datasource.password=postgres`
  - `spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect`

> For your own deployments, override the `url`, `username`, and `password` with environment-specific values.

### Testing

- Uses **H2 in-memory** database with schema generated by JPA:
  - `spring.datasource.url=jdbc:h2:mem:testdb`
  - `spring.jpa.hibernate.ddl-auto=create-drop`
  - SQL init disabled (`spring.sql.init.mode=never`).

### Schema & Relationships

- `users` – user accounts with unique username and email.
- `categories` – uniquely named categories.
- `posts` – belong to a user (`author`) and a category.
- `comments` – belong to a post and a user; may have a parent comment.
- `subscriptions` – join table between users and categories with uniqueness constraint and preference flags.

## API Overview

Full, interactive documentation is provided by Springdoc OpenAPI at:

- `GET /swagger-ui/index.html`
- `GET /v3/api-docs`

Below is a high-level summary of key resources.

### Authentication & Users (`/api/v1/users`)

- `POST /api/v1/users` – Register a new user.
- `POST /api/v1/users/login` – Authenticate and receive JWT + refresh token.
- `GET /api/v1/users/me` – Get the currently authenticated user.
- `POST /api/v1/users/refresh` – Refresh access token using refresh token cookie.
- `POST /api/v1/users/logout` – Logout and revoke tokens.
- `POST /api/v1/users/forgot-password` – Trigger password reset email.
- `GET /api/v1/users` – List all users (admin only).
- `GET /api/v1/users/{id}` – Get user by ID.
- `PUT /api/v1/users/{id}` – Update user (admin or owner).
- `DELETE /api/v1/users/{id}` – Delete user (admin or owner).

### Posts (`/api/v1/posts`)

- `POST /api/v1/posts` (multipart/form-data) – Create post with optional image (authenticated).
- `GET /api/v1/posts` – Paginated list of posts with filter support.
- `GET /api/v1/posts/{id}` – Get post by ID.
- `GET /api/v1/posts/by-user/{userId}` – List posts by a specific user.
- `PUT /api/v1/posts/{id}` – Update post (admin or owner).
- `DELETE /api/v1/posts/{id}` – Delete post (admin or owner).

### Comments (`/api/v1/comments`)

- `POST /api/v1/comments` – Create comment on a post (authenticated).
- `GET /api/v1/comments` – Paginated list of comments.
- `GET /api/v1/comments/{id}` – Get comment by ID.
- `GET /api/v1/comments/by-post/{postId}` – List comments for a post.
- `GET /api/v1/comments/by-user/{userId}` – List comments by a user.
- `PUT /api/v1/comments/{id}` – Update comment (admin, member, or owner).
- `DELETE /api/v1/comments/{id}` – Delete comment (admin or owner).

### Categories (`/api/v1/categories`)

- `GET /api/v1/categories` – Public, paginated list of categories.
- `GET /api/v1/categories/{id}` – Get category by ID.

### Subscriptions (`/api/v1/subscriptions`)

- `POST /api/v1/subscriptions/categories/{categoryId}` – Subscribe current user to category.
- `DELETE /api/v1/subscriptions/categories/{categoryId}` – Unsubscribe current user from category.
- `GET /api/v1/subscriptions` – List subscriptions for current user.

### Security Notes

- JWT access tokens are expected in the `Authorization: Bearer <token>` header.
- Refresh tokens are stored in secure, HTTP-only cookies and handled by the backend.
- CORS is configured to allow configured frontend origins via `app.cors.allowed-origins`.

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+
- PostgreSQL (for `dev` profile) or another JDBC-compatible database
- (Optional) Docker

### Clone & Build

```powershell
cd C:\Users\SilasKumi\Documents\final_project\Community-team2\backend
mvn clean package
```

### Configure Environment

For local development, set at minimum:

- `SECRET`
- `EMAIL` and `EMPASS`
- `CLOUD_NAME`, `CLOUD_API`, `CLOUD_SECRET`
- `FRONTEND_URL`, `FRONTEND_URL_RESET`

You can set them as environment variables in your shell or via an `.env`/IDE run configuration.

Ensure `application-dev.properties` is updated to point to your local PostgreSQL instance if you don’t want to use the default connection string.

### Run with Maven

```powershell
mvn spring-boot:run
```

The app will start on `http://localhost:8080` using the `dev` profile by default.

### Run the Built JAR

After `mvn clean package`:

```powershell
java -jar target/communityboard-0.0.1-SNAPSHOT.jar
```

### Run with Docker

A multi-stage `Dockerfile` is provided. To build and run:

```powershell
docker build -t communityboard-backend .
docker run -p 8080:8080 --env-file .env communityboard-backend
```

Your `.env` file should contain the environment variables described in [Configuration & Environment](#configuration--environment) and database connection settings.

## Testing

This project includes unit and integration tests for core services, security, and controllers.

- Test profile uses an in-memory H2 database and safe dummy credentials.
- Swagger docs are enabled during tests to maintain consistency with runtime behavior.

To run the test suite:

```powershell
mvn test
```

Key test classes include:

- `CommunityBoardApplicationTest` – application context & smoke test
- Service tests – `UserServiceTest`, `PostServiceTest`, `CommentServiceTest`, `CategoryServiceTest`, `SubscriptionServiceImplTest`
- Controller tests – `UserControllerTest`, `PostControllerTest`, `CommentControllerTest`, `CategoryControllerTest`, `SubscriptionControllerTest`
- Security tests – `JwtServiceTest`

## Deployment

### Build Artifact

The Maven build produces a fat JAR under `target/`, e.g.:

- `target/communityboard-0.0.1-SNAPSHOT.jar`

### Container Image

The provided `Dockerfile`:

- Uses `maven:3.9.6-eclipse-temurin-21` as a build stage to compile and package the app.
- Uses `eclipse-temurin:21-jdk-jammy` as the runtime base image.
- Exposes port `8080`.
- Runs the jar via:

```dockerfile
ENTRYPOINT ["java","-jar","app.jar"]
```

This image can be deployed to any container-orchestration platform (Kubernetes, ECS, etc.) or used directly on a VM.

### Production Considerations

- Configure an external PostgreSQL database with strong credentials.
- Provide a strong, secret `SECRET` value and disable any default/embedded secrets.
- Point `FRONTEND_URL` and `FRONTEND_URL_RESET` to your production frontend URLs.
- Configure SMTP with a production-ready email provider.
- Use HTTPS termination at a reverse proxy or ingress.

## Contributing

Contributions are welcome. To propose changes:

1. Fork the repository and create a feature branch.
2. Implement your changes, following existing code style and package organization.
3. Ensure all tests pass:

   ```powershell
   mvn test
   ```

4. Run Checkstyle to enforce style rules:

   ```powershell
   mvn checkstyle:check
   ```

5. Update documentation (including this `README.md`) as needed.
6. Open a pull request with a clear description and reference to any related issues.

### Coding Standards

- Java 21, Spring Boot idioms, and conventional layered architecture.
- Use DTOs for request/response payloads instead of exposing entities.
- Keep business logic in services, not in controllers.
- Follow existing validation patterns (`jakarta.validation` annotations).
- Adhere to Checkstyle rules configured in `checkstyle.xml`.

## License

This project currently has no explicit license declared in the `pom.xml`. If you plan to open-source it publicly, consider adding a `LICENSE` file (e.g., MIT, Apache 2.0) and updating the POM license section accordingly.

