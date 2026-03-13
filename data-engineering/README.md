# CommunityBoard Data Engineering

A comprehensive ETL pipeline and analytics system for the CommunityBoard application, built with Python, Apache Airflow, and PostgreSQL.

## Overview

This data engineering module provides:
- **ETL Pipeline**: Extract, transform, and load community data
- **Data Warehouse**: Star schema for analytics and reporting
- **Replica Sync**: Real-time data synchronization from backend
- **Analytics**: KPI calculations and materialized views
- **Orchestration**: Apache Airflow for workflow management

## Architecture

```
Backend DB → Replica DB → Data Warehouse → Analytics
    ↓           ↓            ↓              ↓
  Extract → Transform → Load → Materialize
```

### Data Flow
1. **Extract**: Pull data from PostgreSQL backend or CSV sources
2. **Transform**: Clean and validate data with business rules
3. **Load**: Store in staging replica and analytics warehouse
4. **Analytics**: Generate KPIs and materialized views

## Quick Start

### Prerequisites
- Python 3.8+
- PostgreSQL database
- Apache Airflow (optional, for orchestration)

### Installation
```bash
# Install dependencies
pip install -r requirements.txt

# Install test dependencies
pip install -r requirements-test.txt

# Set up environment
cp .env.example .env
# Edit .env with your database credentials
```

### Configuration
Edit `configs/config.yml` to configure:
- Database connections
- Data sources (CSV or PostgreSQL)
- Pipeline settings
- Environment-specific configurations

### Run Pipeline
```bash
# Run full ETL pipeline
python pipelines/etl_pipeline.py

# Run replica sync only
python pipelines/replica_sync_pipeline.py

# Generate sample data
python scripts/generate_sample_data.py
```

### Run Tests
```bash
# Run all tests with coverage
python run_tests.py

# Run specific test types
pytest -m unit -v
pytest -m integration -v
```

## Project Structure

```
data-engineering/
├── analytics/           # KPI calculations and materialized views
├── configs/            # Configuration files
├── dags/               # Airflow DAG definitions
├── docs/               # Documentation and ERD diagrams
├── extraction/         # Data extraction modules
├── loading/            # Data loading (staging and warehouse)
├── notebooks/          # Jupyter notebooks for analysis
├── pipelines/          # Main ETL pipeline orchestration
├── sample-data/        # CSV sample data files
├── scripts/            # Utility scripts
├── sources/            # Data source abstractions
├── sql/                # SQL scripts for schema management
├── tests/              # Unit and integration tests
├── transformations/    # Data cleaning and transformation
└── utils/              # Shared utilities and helpers
```

## Key Components

### Data Sources
- **PostgreSQL Source**: Live backend database connection
- **CSV Source**: File-based data for development/testing
- **Base Source**: Abstract interface for extensibility

### ETL Pipeline
- **Extract**: Multi-source data extraction with error handling
- **Transform**: Data validation, cleaning, and business rule application
- **Load**: Incremental loading with conflict resolution

### Data Warehouse
- **Star Schema**: Dimensional modeling for analytics
- **Fact Tables**: `fact_posts`, `fact_comments`
- **Dimension Tables**: `dim_users`, `dim_posts`
- **Materialized Views**: Pre-computed KPIs for performance

### Analytics
- **Activity Trends**: User engagement over time
- **Posts per Category**: Content distribution analysis
- **Top Contributors**: Most active community members
- **KPI Dashboard**: Key performance indicators

## Configuration

### Environment Variables
```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=communityboard
DB_USER=postgres
DB_PASSWORD=postgres

# Pipeline Settings
ENVIRONMENT=dev
DATA_SOURCE=csv
LOG_LEVEL=INFO
```

### Config Structure
- **defaults**: Base configuration for all environments
- **dev**: Development environment (CSV sources)
- **prod**: Production environment (PostgreSQL sources)

## Airflow Integration

### DAG Overview
The `community_etl_dag` orchestrates the complete data pipeline:

1. **Start**: Initialize pipeline
2. **Replica Sync**: Sync backend data to replica database
3. **ETL Load**: Transform and load to analytics warehouse
4. **Finish**: Complete pipeline execution

### Schedule
- **Development**: Manual trigger
- **Production**: Hourly execution (`@hourly`)

### Monitoring
- Task success/failure callbacks
- Result logging and metrics
- Error handling with retries

## Testing

### Test Categories
- **Unit Tests**: Individual component testing
- **Integration Tests**: End-to-end pipeline testing
- **Data Quality Tests**: Validation and constraint checking

### Test Execution
```bash
# Run all tests
python run_tests.py

# Run with coverage report
pytest --cov=. --cov-report=html --cov-report=term

# Run specific test markers
pytest -m unit -v --tb=short
pytest -m integration -v --tb=short
```

### Test Configuration
- **pytest.ini**: Test runner configuration
- **conftest.py**: Shared test fixtures
- **requirements-test.txt**: Test-specific dependencies

## Data Quality

### Validation Rules
- **Required Columns**: Ensure essential fields are present
- **Data Types**: Validate column data types
- **Foreign Keys**: Check referential integrity
- **Duplicates**: Remove duplicate records
- **Timestamps**: Validate date/time formats

### Error Handling
- Graceful degradation on missing data
- Detailed logging for debugging
- Retry mechanisms for transient failures
- Data quality reports and alerts

## Sample Data

### Generation
```bash
# Generate sample data
python scripts/generate_sample_data.py

# Configuration in config.yml
sample_data:
  users: 20
  posts: 60
  comments: 220
  active_users_ratio: 0.3
```

### Categories
- **NEWS**: Community news and announcements
- **EVENT**: Upcoming events and activities
- **DISCUSSION**: General community discussions
- **ALERT**: Important alerts and notices

## Development

### Adding New Data Sources
1. Extend `base_source.py` abstract class
2. Implement required methods (`get_users`, `get_posts`, `get_comments`)
3. Register in configuration
4. Add tests for new source

### Adding New Transformations
1. Create transformation function in `transformations/`
2. Follow existing patterns for error handling
3. Add data quality validations
4. Include unit tests

### Adding New Analytics
1. Create analytics module in `analytics/`
2. Define materialized view SQL
3. Add to warehouse loading process
4. Create visualization notebooks

## Monitoring & Observability

### Logging
- Structured logging with configurable levels
- Pipeline execution tracking
- Error details and stack traces
- Performance metrics

### Metrics
- Record counts per table
- Processing times
- Error rates
- Data quality scores

### Alerts
- Pipeline failures
- Data quality issues
- Performance degradation
- Missing data scenarios

## Deployment

### Local Development
```bash
# Start with Docker Compose
docker-compose up airflow-webserver

# Access Airflow UI
http://localhost:8080
```

### Production
- Deploy with containerized Airflow
- Use managed PostgreSQL (AWS RDS)
- Configure monitoring and alerting
- Set up backup and recovery

## Troubleshooting

### Common Issues
- **Database Connection**: Check credentials and network connectivity
- **Missing Dependencies**: Ensure all requirements are installed
- **Configuration Errors**: Validate YAML syntax and required fields
- **Data Quality Failures**: Review validation rules and source data

### Debug Mode
```bash
# Enable debug logging
export LOG_LEVEL=DEBUG

# Run with verbose output
python pipelines/etl_pipeline.py --verbose
```

## Contributing

1. Follow existing code patterns and conventions
2. Add tests for new functionality
3. Update documentation for changes
4. Use type hints and docstrings
5. Run tests before submitting changes

## License

This project is part of the CommunityBoard application developed by AmaliTech Group Project Teams 1-5.