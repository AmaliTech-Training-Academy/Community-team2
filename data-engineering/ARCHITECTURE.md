# CommunityBoard Data Engineering - Architecture Diagram

## High-Level Data Architecture

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                           COMMUNITYBOARD DATA ENGINEERING ARCHITECTURE                   │
└─────────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   DATA SOURCES  │    │   EXTRACTION    │    │ TRANSFORMATION  │    │    LOADING      │
└─────────────────┘    └─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │                       │
         ▼                       ▼                       ▼                       ▼

┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  Backend DB     │    │  extract.py     │    │ clean_users.py  │    │ Replica DB      │
│  ┌───────────┐  │───▶│  ┌───────────┐  │───▶│ ┌───────────┐   │───▶│ ┌───────────┐   │
│  │   users   │  │    │  │PostgreSQL │  │    │ │ Validate  │   │    │ │replica_raw│   │
│  │   posts   │  │    │  │ Source    │  │    │ │ Clean     │   │    │ │  .users   │   │
│  │ comments  │  │    │  │ Connector │  │    │ │ Transform │   │    │ │  .posts   │   │
│  │categories │  │    │  └───────────┘  │    │ └───────────┘   │    │ │ .comments │   │
│  └───────────┘  │    └─────────────────┘    └─────────────────┘    │ └───────────┘   │
└─────────────────┘                                                  └─────────────────┘
         │                                                                     │
         │                                                                     │
┌─────────────────┐                                                           │
│   CSV Files     │                                                           │
│  ┌───────────┐  │                                                           │
│  │users.csv  │  │                                                           │
│  │posts.csv  │  │                                                           │
│  │comments.csv│  │                                                           │
│  └───────────┘  │                                                           │
└─────────────────┘                                                           │
                                                                               │
                                                                               ▼
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                                 ANALYTICS WAREHOUSE                                       │
│                                   (Star Schema)                                          │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                         │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐                     │
│  │  DIMENSION      │    │  DIMENSION      │    │   FACT TABLE    │                     │
│  │   TABLES        │    │   TABLES        │    │                 │                     │
│  │                 │    │                 │    │                 │                     │
│  │ ┌─────────────┐ │    │ ┌─────────────┐ │    │ ┌─────────────┐ │                     │
│  │ │ dim_users   │ │    │ │ dim_posts   │ │    │ │fact_comments│ │                     │
│  │ │             │ │    │ │             │ │    │ │             │ │                     │
│  │ │user_key (PK)│ │    │ │post_key (PK)│ │    │ │comment_id   │ │                     │
│  │ │source_user_id│ │    │ │source_post_id│ │   │ │post_key (FK)│ │                     │
│  │ │full_name    │ │    │ │author_user_key│ │   │ │user_key (FK)│ │                     │
│  │ │email        │ │    │ │title        │ │    │ │created_at   │ │                     │
│  │ │role         │ │    │ │content      │ │    │ │comment_count│ │                     │
│  │ │created_at   │ │    │ │category     │ │    │ │loaded_at    │ │                     │
│  │ │loaded_at    │ │    │ │created_at   │ │    │ └─────────────┘ │                     │
│  │ └─────────────┘ │    │ │loaded_at    │ │    └─────────────────┘                     │
│  └─────────────────┘    │ └─────────────┘ │                                            │
│                         └─────────────────┘                                            │
│                                                                                         │
└─────────────────────────────────────────────────────────────────────────────────────────┘
                                           │
                                           ▼
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                              KPI MATERIALIZED VIEWS                                      │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                         │
│ ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐                          │
│ │kpi_top_contrib  │  │kpi_activity_    │  │kpi_posts_per_   │                          │
│ │   utors_mv      │  │   trends_mv     │  │   category_mv   │                          │
│ │                 │  │                 │  │                 │                          │
│ │activity_date    │  │activity_date    │  │activity_date    │                          │
│ │user_key         │  │posts_count      │  │category         │                          │
│ │full_name        │  │comments_count   │  │posts_count      │                          │
│ │post_count       │  │total_activity   │  │                 │                          │
│ │comment_count    │  │                 │  │                 │                          │
│ │total_contrib    │  │                 │  │                 │                          │
│ └─────────────────┘  └─────────────────┘  └─────────────────┘                          │
│                                                                                         │
└─────────────────────────────────────────────────────────────────────────────────────────┘
```

## Detailed Component Architecture

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                              AIRFLOW ORCHESTRATION LAYER                                 │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                         │
│  ┌─────────────────────────────────────────────────────────────────────────────────┐   │
│  │                        community_etl_dag.py                                     │   │
│  │                                                                                 │   │
│  │  [START] ──▶ [REPLICA_SYNC] ──▶ [ETL_LOAD] ──▶ [FINISH]                       │   │
│  │     │              │                │             │                            │   │
│  │     │              ▼                ▼             │                            │   │
│  │     │    replica_sync_pipeline   etl_pipeline     │                            │   │
│  │     │                                             │                            │   │
│  │     └─────────────── Monitoring & Callbacks ─────┘                            │   │
│  └─────────────────────────────────────────────────────────────────────────────────┘   │
│                                                                                         │
└─────────────────────────────────────────────────────────────────────────────────────────┘
                                           │
                                           ▼
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                                 PIPELINE MODULES                                         │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                         │
│ ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐     │
│ │    SOURCES      │  │   EXTRACTION    │  │ TRANSFORMATION  │  │    LOADING      │     │
│ │                 │  │                 │  │                 │  │                 │     │
│ │ base_source.py  │  │   extract.py    │  │ clean_users.py  │  │load_to_staging/ │     │
│ │      │          │  │       │         │  │ clean_posts.py  │  │load_to_warehouse│     │
│ │      ├──────────┼──┼───────┼─────────┼──┼─clean_comments  │  │                 │     │
│ │      │          │  │       │         │  │                 │  │ ┌─────────────┐ │     │
│ │ postgres_source │  │ Multi-source    │  │ Data Validation │  │ │star_schema  │ │     │
│ │ csv_source.py   │  │ Data Reader     │  │ Business Rules  │  │ │warehouse_ops│ │     │
│ │                 │  │ Error Handling  │  │ Foreign Key     │  │ │replica_ops  │ │     │
│ │                 │  │                 │  │ Validation      │  │ └─────────────┘ │     │
│ └─────────────────┘  └─────────────────┘  └─────────────────┘  └─────────────────┘     │
│                                                                                         │
└─────────────────────────────────────────────────────────────────────────────────────────┘
                                           │
                                           ▼
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                               ANALYTICS & UTILITIES                                      │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                         │
│ ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐     │
│ │   ANALYTICS     │  │     UTILS       │  │     TESTING     │  │   MONITORING    │     │
│ │                 │  │                 │  │                 │  │                 │     │
│ │activity_trends  │  │config_loader.py │  │   run_tests.py  │  │   logging.py    │     │
│ │posts_per_cat    │  │database.py      │  │                 │  │                 │     │
│ │top_contributors │  │data_validation  │  │ ┌─────────────┐ │  │ ┌─────────────┐ │     │
│ │materialized_    │  │settings.py      │  │ │Unit Tests   │ │  │ │Structured   │ │     │
│ │  views.py       │  │                 │  │ │Integration  │ │  │ │Logging      │ │     │
│ │                 │  │                 │  │ │Data Quality │ │  │ │Error Track  │ │     │
│ │                 │  │                 │  │ └─────────────┘ │  │ │Performance  │ │     │
│ └─────────────────┘  └─────────────────┘  └─────────────────┘  │ └─────────────┘ │     │
│                                                                └─────────────────┘     │
└─────────────────────────────────────────────────────────────────────────────────────────┘
```

## Data Flow Sequence Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                              ETL PIPELINE EXECUTION FLOW                                 │
└─────────────────────────────────────────────────────────────────────────────────────────┘

Time │ Airflow DAG │  Pipeline   │   Sources   │ Transform │  Replica DB │ Warehouse │ KPIs
     │             │             │             │           │             │           │
  1  │   START     │             │             │           │             │           │
     │      │      │             │             │           │             │           │
  2  │      ▼      │             │             │           │             │           │
     │ REPLICA_SYNC│──────────▶  │             │           │             │           │
     │             │ run_replica │             │           │             │           │
  3  │             │ _sync()     │──────────▶  │           │             │           │
     │             │             │ Extract     │           │             │           │
     │             │             │ Data        │           │             │           │
  4  │             │             │      │      │──────────▶│             │           │
     │             │             │      │      │ Clean &   │             │           │
     │             │             │      │      │ Validate  │             │           │
  5  │             │             │      │      │     │     │──────────▶  │           │
     │             │             │      │      │     │     │ Load to     │           │
     │             │             │      │      │     │     │ Staging     │           │
  6  │      ▼      │             │      │      │     │     │      │      │           │
     │  ETL_LOAD   │──────────▶  │      │      │     │     │      │      │           │
     │             │ run_etl()   │      │      │     │     │      │      │           │
  7  │             │             │──────┼──────┼─────┼─────┼──────┼──────┼──────────▶│
     │             │             │ Extract     │     │     │      │      │ Transform │
     │             │             │ Transform   │     │     │      │      │ & Load    │
  8  │             │             │ Load        │     │     │      │      │     │     │
     │             │             │             │     │     │      │      │     ▼     │
     │             │             │             │     │     │      │      │ ┌───────┐ │
  9  │             │             │             │     │     │      │      │ │Star   │ │
     │             │             │             │     │     │      │      │ │Schema │ │
     │             │             │             │     │     │      │      │ │Tables │ │
 10  │             │             │             │     │     │      │      │ └───────┘ │
     │             │             │             │     │     │      │      │     │     │
 11  │             │             │             │     │     │      │      │     ▼     │
     │             │             │             │     │     │      │      │ ┌───────┐ │
 12  │             │             │             │     │     │      │      │ │Refresh│ │
     │             │             │             │     │     │      │      │ │KPI    │ │
     │             │             │             │     │     │      │      │ │Views  │ │
 13  │      ▼      │             │             │     │     │      │      │ └───────┘ │
     │   FINISH    │◀────────────┴─────────────┴─────┴─────┴──────┴──────┴───────────┘
     │             │                    Success/Failure Callbacks
```

## Configuration Architecture

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                              CONFIGURATION MANAGEMENT                                    │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                         │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐                     │
│  │  config.yml     │    │ Environment     │    │ Runtime Config  │                     │
│  │                 │    │ Variables       │    │                 │                     │
│  │ ┌─────────────┐ │    │ ┌─────────────┐ │    │ ┌─────────────┐ │                     │
│  │ │ defaults:   │ │    │ │ DB_HOST     │ │    │ │ Loaded by   │ │                     │
│  │ │ - backend   │ │    │ │ DB_PORT     │ │    │ │ config_     │ │                     │
│  │ │ - staging   │ │    │ │ DB_NAME     │ │    │ │ loader.py   │ │                     │
│  │ │ - warehouse │ │    │ │ DB_USER     │ │    │ │             │ │                     │
│  │ │ - pipeline  │ │    │ │ DB_PASSWORD │ │    │ │ Merged with │ │                     │
│  │ └─────────────┘ │    │ │ ENVIRONMENT │ │    │ │ env vars    │ │                     │
│  │                 │    │ └─────────────┘ │    │ │             │ │                     │
│  │ ┌─────────────┐ │    └─────────────────┘    │ │ Validated   │ │                     │
│  │ │environments:│ │                           │ │ & Ready     │ │                     │
│  │ │ - dev       │ │                           │ └─────────────┘ │                     │
│  │ │ - prod      │ │                           └─────────────────┘                     │
│  │ └─────────────┘ │                                                                   │
│  └─────────────────┘                                                                   │
│                                                                                         │
└─────────────────────────────────────────────────────────────────────────────────────────┘
```

## Testing Architecture

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                                TESTING FRAMEWORK                                         │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                         │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐                     │
│  │   UNIT TESTS    │    │ INTEGRATION     │    │ DATA QUALITY    │                     │
│  │                 │    │ TESTS           │    │ TESTS           │                     │
│  │ ┌─────────────┐ │    │ ┌─────────────┐ │    │ ┌─────────────┐ │                     │
│  │ │test_clean_  │ │    │ │End-to-end   │ │    │ │Schema       │ │                     │
│  │ │users.py     │ │    │ │Pipeline     │ │    │ │Validation   │ │                     │
│  │ │test_clean_  │ │    │ │Testing      │ │    │ │Foreign Key  │ │                     │
│  │ │posts.py     │ │    │ │             │ │    │ │Checks       │ │                     │
│  │ │test_data_   │ │    │ │Database     │ │    │ │Data Types   │ │                     │
│  │ │validation   │ │    │ │Integration  │ │    │ │Constraints  │ │                     │
│  │ └─────────────┘ │    │ └─────────────┘ │    │ └─────────────┘ │                     │
│  └─────────────────┘    └─────────────────┘    └─────────────────┘                     │
│                                   │                                                     │
│                                   ▼                                                     │
│  ┌─────────────────────────────────────────────────────────────────────────────────┐   │
│  │                           run_tests.py                                         │   │
│  │                                                                                 │   │
│  │  pytest -m unit -v --tb=short                                                  │   │
│  │  pytest -m integration -v --tb=short                                           │   │
│  │  pytest --cov=. --cov-report=html --cov-report=term                           │   │
│  └─────────────────────────────────────────────────────────────────────────────────┘   │
│                                                                                         │
└─────────────────────────────────────────────────────────────────────────────────────────┘
```

## Deployment Architecture

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                              DEPLOYMENT ENVIRONMENTS                                     │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                         │
│ ┌─────────────────┐                           ┌─────────────────┐                       │
│ │  DEVELOPMENT    │                           │   PRODUCTION    │                       │
│ │                 │                           │                 │                       │
│ │ ┌─────────────┐ │                           │ ┌─────────────┐ │                       │
│ │ │Data Source: │ │                           │ │Data Source: │ │                       │
│ │ │CSV Files    │ │                           │ │PostgreSQL   │ │                       │
│ │ │             │ │                           │ │Backend DB   │ │                       │
│ │ │Single DB    │ │                           │ │             │ │                       │
│ │ │Instance     │ │                           │ │Multi-DB     │ │                       │
│ │ │             │ │                           │ │Setup:       │ │                       │
│ │ │Manual       │ │                           │ │- Backend    │ │                       │
│ │ │Trigger      │ │                           │ │- Replica    │ │                       │
│ │ └─────────────┘ │                           │ │- Analytics  │ │                       │
│ └─────────────────┘                           │ │             │ │                       │
│                                               │ │Scheduled    │ │                       │
│                                               │ │Execution    │ │                       │
│                                               │ │(@hourly)    │ │                       │
│                                               │ └─────────────┘ │                       │
│                                               └─────────────────┘                       │
│                                                                                         │
└─────────────────────────────────────────────────────────────────────────────────────────┘
```

This architecture diagram illustrates the complete data engineering pipeline for CommunityBoard, showing:

1. **Data Sources**: Backend PostgreSQL and CSV files
2. **ETL Pipeline**: Extract, Transform, Load processes
3. **Star Schema Warehouse**: Dimensional modeling with fact and dimension tables
4. **KPI Views**: Pre-computed analytics for performance
5. **Airflow Orchestration**: Workflow management and scheduling
6. **Configuration Management**: Environment-specific settings
7. **Testing Framework**: Comprehensive test coverage
8. **Deployment Environments**: Development and production setups

The diagram emphasizes the data engineering module's focus on reliable data processing, analytics preparation, and scalable architecture patterns.