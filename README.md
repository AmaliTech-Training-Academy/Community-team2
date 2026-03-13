# CommunityBoard
**AmaliTech Group Project – Full-Stack Teams (Teams 1-5)**

A comprehensive community notice board application where users can post announcements, events, and discussions. Built with modern full-stack technologies and enterprise-grade infrastructure, featuring categorized content, user interactions, analytics, and comprehensive search capabilities.

## Quick Start

### Prerequisites
- Docker & Docker Compose
- Node.js 18+ (for local frontend development)
- Java 21+ (for local backend development)
- Python 3.8+ (for data engineering)
- AWS CLI (for production deployment)

### Local Development
```bash
# Clone the repository
git clone <repository-url>
cd Community-team2

# Start all services
docker-compose up --build

# Access the application
# Frontend: http://localhost:3000
# Backend API: http://localhost:8080/swagger-ui.html
# Airflow: http://localhost:8088
```

### Default Users (Pre-seeded)
| Email | Password | Role |
|-------|----------|------|
| admin@amalitech.com | password123 | ADMIN |
| user@amalitech.com | password123 | USER |

## Table of Contents
- [Project Overview](#-project-overview)
- [Architecture](#-architecture)
- [Technology Stack](#-technology-stack)
- [Features](#-features)
- [Project Structure](#-project-structure)
- [Module Documentation](#-module-documentation)
- [Development Setup](#-development-setup)
- [Testing](#-testing)
- [Deployment](#-deployment)
- [API Documentation](#-api-documentation)
- [Contributing](#-contributing)

## Project Overview

CommunityBoard is a full-stack community engagement platform designed to replace scattered social media posts and email chains with a structured, searchable, and categorized system for community communication.

### Key Objectives
- **Centralized Communication**: Single platform for all community announcements
- **Organized Content**: Category-based content organization and discovery
- **User Engagement**: Interactive features with comments and discussions
- **Analytics Insights**: Data-driven community engagement metrics
- **Scalable Architecture**: Enterprise-ready infrastructure on AWS

### Target Users
- **Community Members**: Post announcements, participate in discussions
- **Community Administrators**: Moderate content, manage categories, view analytics
- **Content Contributors**: Share events, announcements, and updates

## Architecture

### High-Level Architecture
```
┌─────────────────────────────────────────────────────────────────┐
│                    COMMUNITYBOARD ARCHITECTURE                   │
└─────────────────────────────────────────────────────────────────┘

┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Frontend  │    │   Backend   │    │    Data     │    │   DevOps    │
│   React 18  │◄──►│Spring Boot  │◄──►│ Engineering │◄──►│AWS/Terraform│
│             │    │   + JWT     │    │   Python    │    │             │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
       │                   │                   │                   │
       ▼                   ▼                   ▼                   ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  Zustand    │    │ PostgreSQL  │    │   Airflow   │    │ ECS Fargate │
│ TailwindCSS │    │   Redis     │    │ Analytics   │    │     RDS     │
│   Vite      │    │ Cloudinary  │    │   ETL       │    │     ALB     │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

### Data Flow
```
User Input → Frontend → Backend API → Database → Data Pipeline → Analytics → Insights
     ↑                                    ↓
     └────────── Notifications ←──────────┘
```

## Technology Stack

### Backend (Spring Boot)
- **Framework**: Spring Boot 4.0.3, Java 21
- **Security**: Spring Security with JWT authentication
- **Database**: PostgreSQL 15 with Spring Data JPA
- **Caching**: Caffeine cache for performance
- **Email**: Spring Mail with Thymeleaf templates
- **File Storage**: Cloudinary for image uploads
- **API Documentation**: OpenAPI 3 (Swagger)
- **Build Tool**: Maven with Checkstyle

### Frontend (React)
- **Framework**: React 18 with TypeScript
- **Routing**: React Router DOM v6
- **State Management**: Zustand for global state
- **Styling**: TailwindCSS v4
- **HTTP Client**: Axios with interceptors
- **Build Tool**: Vite for fast development
- **Testing**: Vitest + React Testing Library

### Data Engineering (Python)
- **Orchestration**: Apache Airflow for workflow management
- **Data Processing**: Pandas for ETL operations
- **Database**: SQLAlchemy ORM with PostgreSQL
- **Analytics**: Star schema data warehouse
- **Configuration**: YAML-based configuration management
- **Testing**: Pytest with coverage reporting

### Quality Assurance
- **API Testing**: REST Assured with JUnit 5
- **UI Testing**: Selenium WebDriver
- **Code Quality**: SonarQube integration
- **Test Organization**: Base classes and utilities

### DevOps & Infrastructure
- **Containerization**: Docker with multi-stage builds
- **Orchestration**: Docker Compose for local development
- **Cloud Platform**: AWS (ECS Fargate, RDS, ALB)
- **Infrastructure as Code**: Terraform with modular design
- **CI/CD**: GitHub Actions for automated deployment
- **Monitoring**: CloudWatch with custom alarms

## Features

### Implemented Features (~30% Complete)
- ✅ **User Authentication**: JWT-based register/login system
- ✅ **Post Management**: Full CRUD operations for community posts
- ✅ **Category System**: Organized content categorization
- ✅ **User Roles**: Admin and User role-based access control
- ✅ **Email Notifications**: Automated email system with templates
- ✅ **Image Upload**: Cloudinary integration for post attachments
- ✅ **Responsive Design**: Mobile-friendly React SPA
- ✅ **API Documentation**: Comprehensive Swagger documentation
- ✅ **Data Pipeline**: ETL pipeline for analytics
- ✅ **Infrastructure**: Production-ready AWS deployment

### Planned Features (Roadmap)
- 🔄 **Comments System**: Threaded discussions on posts
- 🔄 **Search & Filtering**: Advanced content discovery
- 🔄 **User Profiles**: Comprehensive profile management
- 🔄 **Real-time Notifications**: Live notification system
- 🔄 **Analytics Dashboard**: Community engagement metrics
- 🔄 **Subscription System**: User notification preferences
- 🔄 **Mobile App**: React Native mobile application

## Project Structure

```
Community-team2/
├── backend/                    # Spring Boot REST API
│   ├── src/main/java/com/amalitech/communityboard/
│   │   ├── controller/         # REST endpoints
│   │   ├── service/           # Business logic
│   │   ├── repository/        # Data access layer
│   │   ├── models/           # JPA entities
│   │   ├── dto/              # Request/response objects
│   │   ├── security/         # JWT authentication
│   │   ├── config/           # Spring configurations
│   │   └── events/           # Application events
│   ├── src/test/             # Unit and integration tests
│   ├── pom.xml              # Maven dependencies
│   └── Dockerfile           # Container configuration
│
├── frontend/                   # React 18 SPA
│   ├── src/
│   │   ├── components/       # Reusable UI components
│   │   │   ├── atoms/        # Basic UI elements
│   │   │   ├── molecules/    # Composite components
│   │   │   ├── organisms/    # Complex components
│   │   │   └── templates/    # Page layouts
│   │   ├── pages/           # Route-level components
│   │   ├── features/        # Business logic modules
│   │   ├── api/             # HTTP client configuration
│   │   ├── utils/           # Shared utilities
│   │   └── assets/          # Static resources
│   ├── package.json         # npm dependencies
│   └── Dockerfile          # Container configuration
│
├── data-engineering/          # Python ETL & Analytics
│   ├── pipelines/           # Main ETL orchestration
│   ├── sources/             # Data source abstractions
│   ├── extraction/          # Data extraction modules
│   ├── transformations/     # Data cleaning & validation
│   ├── loading/             # Data warehouse loading
│   ├── analytics/           # KPI calculations
│   ├── dags/               # Airflow DAG definitions
│   ├── sql/                # Database schema scripts
│   ├── tests/              # Unit and integration tests
│   └── requirements.txt    # Python dependencies
│
├── qa/                       # Quality Assurance
│   ├── api-tests/          # REST API testing
│   └── ui-tests/           # Selenium UI testing
│
├── devops/                   # Infrastructure & Deployment
│   ├── terraform/          # AWS infrastructure as code
│   │   ├── modules/        # Reusable Terraform modules
│   │   ├── main.tf         # Root configuration
│   │   └── variables.tf    # Input variables
│   └── scripts/            # Deployment automation
│
├── design/                   # UI/UX Design Assets
├── .github/workflows/        # GitHub Actions CI/CD
├── docker-compose.yml        # Local development setup
└── README.md                # This file
```

## Module Documentation

### Backend Module
- **Framework**: Spring Boot 4.0.3 with Java 21
- **Architecture**: Layered architecture with clear separation of concerns
- **Security**: JWT-based authentication with role-based access control
- **Database**: PostgreSQL with JPA entities and repositories
- **Features**: User management, post CRUD, category management, email notifications
- **Testing**: JUnit 5 with Mockito for unit tests

### Frontend Module
- **Framework**: React 18 with TypeScript for type safety
- **Architecture**: Atomic design pattern with feature-based organization
- **State Management**: Zustand for lightweight global state
- **Styling**: TailwindCSS v4 for utility-first styling
- **Testing**: Vitest with React Testing Library
- **Build**: Vite for fast development and optimized production builds

### Data Engineering Module
- **Pipeline**: Comprehensive ETL pipeline with Apache Airflow orchestration
- **Architecture**: Extract → Transform → Load → Analytics workflow
- **Data Sources**: PostgreSQL backend and CSV files for development
- **Warehouse**: Star schema design with fact and dimension tables
- **Analytics**: Materialized views for KPI calculations
- **Testing**: Pytest with unit and integration test coverage

### DevOps Module
- **Infrastructure**: AWS-based with Terraform for infrastructure as code
- **Containers**: Docker with ECS Fargate for serverless container orchestration
- **Database**: RDS PostgreSQL with automated backups
- **Load Balancing**: Application Load Balancer with health checks
- **Security**: VPC with private subnets, Secrets Manager, IAM roles
- **Monitoring**: CloudWatch alarms with SNS notifications

## Development Setup

### Backend Development
```bash
cd backend

# Run with Maven
./mvnw spring-boot:run

# Run tests
./mvnw test

# Build JAR
./mvnw clean package

# Access Swagger UI
http://localhost:8080/swagger-ui.html
```

### Frontend Development
```bash
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev

# Run tests
npm test

# Build for production
npm run build

# Access application
http://localhost:3000
```

### Data Engineering Development
```bash
cd data-engineering

# Install dependencies
pip install -r requirements.txt

# Run tests
python run_tests.py

# Run ETL pipeline
python pipelines/etl_pipeline.py

# Start Airflow (via Docker Compose)
docker-compose up airflow-webserver
# Access: http://localhost:8088
```

### Full Stack Development
```bash
# Start all services
docker-compose up --build

# Services will be available at:
# Frontend: http://localhost:3000
# Backend: http://localhost:8080
# Airflow: http://localhost:8088
# PostgreSQL: localhost:5432
```

## Testing

### Backend Testing
```bash
cd backend
./mvnw test                    # Unit tests
./mvnw verify                  # Integration tests
./mvnw checkstyle:check        # Code style validation
```

### Frontend Testing
```bash
cd frontend
npm test                       # Unit tests
npm run test:coverage          # Coverage report
npm run lint                   # TypeScript validation
```

### Data Engineering Testing
```bash
cd data-engineering
python run_tests.py            # All tests with coverage
pytest -m unit -v              # Unit tests only
pytest -m integration -v       # Integration tests only
```

### API Testing
```bash
cd qa/api-tests
mvn test                       # REST API integration tests
```

### UI Testing
```bash
cd qa/ui-tests
mvn test                       # Selenium UI tests
```

## Deployment

### Local Development
```bash
# Start all services
docker-compose up --build

# Stop services
docker-compose down

# Clean volumes
docker-compose down -v
```

### Production Deployment (AWS)
```bash
cd devops/terraform

# Configure variables
cp terraform.tfvars.example terraform.tfvars
# Edit terraform.tfvars with your AWS settings

# Deploy infrastructure
terraform init
terraform plan
terraform apply

# Build and push Docker images
aws ecr get-login-password --region eu-west-1 | \
  docker login --username AWS --password-stdin <ECR_URL>

# Build and push each service
docker build -t communityboard-backend:latest ./backend
docker tag communityboard-backend:latest <ECR_BACKEND_URL>:latest
docker push <ECR_BACKEND_URL>:latest

# Repeat for frontend and data-engineering
```

### Infrastructure Components
- **ECS Fargate**: Serverless container orchestration
- **RDS PostgreSQL**: Managed database with automated backups
- **Application Load Balancer**: Traffic distribution with health checks
- **VPC**: Private networking with security groups
- **Secrets Manager**: Secure credential storage
- **CloudWatch**: Monitoring and alerting

### Cost Optimization
- **Development**: ~$67/month with cost-optimized settings
- **Production**: Scalable with reserved capacity options
- **Features**: VPC endpoints, single RDS instance, minimal task sizes

## API Documentation

### Authentication Endpoints
```
POST /api/auth/register        # User registration
POST /api/auth/login          # User login
POST /api/auth/logout         # User logout
POST /api/auth/refresh        # Token refresh
```

### Post Management
```
GET    /api/posts             # List posts with pagination
POST   /api/posts             # Create new post
GET    /api/posts/{id}        # Get post by ID
PUT    /api/posts/{id}        # Update post
DELETE /api/posts/{id}        # Delete post
```

### Category Management
```
GET    /api/categories        # List all categories
POST   /api/categories        # Create category (admin only)
PUT    /api/categories/{id}   # Update category (admin only)
DELETE /api/categories/{id}   # Delete category (admin only)
```

### User Management
```
GET    /api/users/profile     # Get current user profile
PUT    /api/users/profile     # Update user profile
GET    /api/users/{id}        # Get user by ID (admin only)
```

### Interactive API Documentation
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/v3/api-docs

## Contributing

### Development Workflow
1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Follow** coding standards and conventions
4. **Write** tests for new functionality
5. **Commit** changes (`git commit -m 'Add amazing feature'`)
6. **Push** to branch (`git push origin feature/amazing-feature`)
7. **Create** a Pull Request

### Code Standards
- **Backend**: Follow Spring Boot best practices, use Checkstyle
- **Frontend**: Use TypeScript, follow React best practices
- **Data Engineering**: Follow PEP 8, use type hints
- **Testing**: Maintain test coverage above 80%
- **Documentation**: Update README and API docs for changes

### Development Guidelines
- **Atomic Commits**: Make small, focused commits
- **Descriptive Messages**: Write clear commit messages
- **Code Review**: All changes require peer review
- **Testing**: Ensure all tests pass before submitting
- **Documentation**: Update relevant documentation

## License

This project is developed as part of the AmaliTech Group Project by Full-Stack Teams 1-5.

## Support & Contact

### Getting Help
1. **Documentation**: Check module-specific README files
2. **Issues**: Create GitHub issues for bugs or feature requests
3. **Discussions**: Use GitHub Discussions for questions
4. **Wiki**: Check project wiki for additional resources

### Team Contacts
- **Backend Team**: Spring Boot API development
- **Frontend Team**: React application development  
- **Data Engineering Team**: ETL pipeline and analytics
- **DevOps Team**: Infrastructure and deployment
- **QA Team**: Testing and quality assurance

### Useful Links
- **Project Repository**: [GitHub Repository]
- **Live Demo**: [Production URL]
- **API Documentation**: [Swagger UI]
- **Project Board**: [GitHub Projects]
- **Wiki**: [Project Wiki]

---

**Built by AmaliTech Group Project Teams 1-5**