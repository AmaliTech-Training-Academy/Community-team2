# CommunityBoard Data Engineering - Schema Documentation & Data Dictionary

## Overview

This document provides comprehensive documentation for the CommunityBoard data engineering pipeline, including schema definitions, data dictionary, and data lineage information.

## Data Architecture

### Data Flow Pipeline
```
Source System (Backend DB) → Replica/Staging → Analytics Warehouse → KPI Views → Frontend APIs
```

### Database Schemas

#### 1. **Source Schema** (`public`)
- **Purpose**: Production application database
- **Location**: Backend PostgreSQL database
- **Tables**: `users`, `posts`, `comments`, `categories`

#### 2. **Replica Schema** (`replica_raw`)
- **Purpose**: Staging area for raw data extraction
- **Location**: Replica PostgreSQL database
- **Tables**: `users`, `posts`, `comments`

#### 3. **Analytics Schema** (`analytics`)
- **Purpose**: Data warehouse with star schema design
- **Location**: Analytics PostgreSQL database
- **Tables**: `dim_users`, `dim_posts`, `fact_comments`
- **Views**: KPI materialized views

---

## Schema Documentation

### Source System Tables

#### **users** (Source)
Production user data from the application backend.

| Column | Data Type | Constraints | Description |
|--------|-----------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY | Unique user identifier |
| `username` | TEXT | NOT NULL | User display name (legacy field) |
| `email` | TEXT | NOT NULL | User email address |
| `role` | TEXT | | User role (admin, member) |
| `created_at` | TIMESTAMP | NOT NULL | Account creation timestamp |

#### **posts** (Source)
Community posts created by users.

| Column | Data Type | Constraints | Description |
|--------|-----------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY | Unique post identifier |
| `user_id` | BIGINT | NOT NULL, FK → users(id) | Post author reference |
| `title` | TEXT | NOT NULL | Post title |
| `content` | TEXT | NOT NULL | Post content/body |
| `category_id` | BIGINT | FK → categories(id) | Post category reference |
| `created_at` | TIMESTAMP | NOT NULL | Post creation timestamp |

#### **comments** (Source)
User comments on posts.

| Column | Data Type | Constraints | Description |
|--------|-----------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY | Unique comment identifier |
| `post_id` | BIGINT | NOT NULL, FK → posts(id) | Parent post reference |
| `user_id` | BIGINT | NOT NULL, FK → users(id) | Comment author reference |
| `content` | TEXT | NOT NULL | Comment content |
| `created_at` | TIMESTAMP | NOT NULL | Comment creation timestamp |

#### **categories** (Source)
Post categorization lookup table.

| Column | Data Type | Constraints | Description |
|--------|-----------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY | Unique category identifier |
| `name` | TEXT | NOT NULL, UNIQUE | Category name |
| `description` | TEXT | | Category description |

---

### Replica/Staging Tables

#### **replica_raw.users**
Staging copy of user data with transformations applied.

| Column | Data Type | Constraints | Description |
|--------|-----------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY | Source user ID |
| `username` | TEXT | NOT NULL | User display name (renamed to full_name in ETL) |
| `email` | TEXT | NOT NULL | Normalized email address |
| `role` | TEXT | | Standardized role value |
| `created_at` | TIMESTAMP | NOT NULL | Account creation timestamp |

#### **replica_raw.posts**
Staging copy of posts with category names resolved.

| Column | Data Type | Constraints | Description |
|--------|-----------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY | Source post ID |
| `user_id` | BIGINT | NOT NULL, FK → users(id) | Post author reference |
| `title` | TEXT | NOT NULL | Cleaned post title |
| `content` | TEXT | NOT NULL | Sanitized post content |
| `category` | TEXT | NOT NULL | Category name (resolved from category_id) |
| `created_at` | TIMESTAMP | NOT NULL | Post creation timestamp |

#### **replica_raw.comments**
Staging copy of comments data.

| Column | Data Type | Constraints | Description |
|--------|-----------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY | Source comment ID |
| `post_id` | BIGINT | NOT NULL, FK → posts(id) | Parent post reference |
| `user_id` | BIGINT | NOT NULL, FK → users(id) | Comment author reference |
| `content` | TEXT | NOT NULL | Cleaned comment content |
| `created_at` | TIMESTAMP | NOT NULL | Comment creation timestamp |

---

### Analytics Warehouse Tables (Star Schema)

#### **analytics.dim_users** (Dimension)
User dimension table with warehouse surrogate keys.

| Column | Data Type | Constraints | Description |
|--------|-----------|-------------|-------------|
| `user_key` | BIGSERIAL | PRIMARY KEY | Warehouse surrogate key |
| `source_user_id` | BIGINT | NOT NULL, UNIQUE | Original user ID from source |
| `full_name` | TEXT | NOT NULL | Cleaned and normalized user name |
| `email` | TEXT | NOT NULL | Lowercase normalized email |
| `role` | TEXT | | Standardized role (admin, member, unknown) |
| `created_at` | TIMESTAMP | NOT NULL | Account creation timestamp |
| `loaded_at` | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() | Warehouse load timestamp |

**Business Rules:**
- `full_name` is cleaned to remove invalid characters and normalized
- `email` is converted to lowercase
- `role` defaults to 'unknown' if null/empty
- Missing `created_at` values are backfilled with max timestamp

#### **analytics.dim_posts** (Dimension)
Post dimension table with author relationships.

| Column | Data Type | Constraints | Description |
|--------|-----------|-------------|-------------|
| `post_key` | BIGSERIAL | PRIMARY KEY | Warehouse surrogate key |
| `source_post_id` | BIGINT | NOT NULL, UNIQUE | Original post ID from source |
| `author_user_key` | BIGINT | NOT NULL, FK → dim_users(user_key) | Post author warehouse key |
| `title` | TEXT | NOT NULL | Post title |
| `content` | TEXT | NOT NULL | Post content |
| `category` | TEXT | NOT NULL | Post category name |
| `created_at` | TIMESTAMP | NOT NULL | Post creation timestamp |
| `loaded_at` | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() | Warehouse load timestamp |

**Business Rules:**
- `author_user_key` resolves to warehouse surrogate key
- `category` must be one of: NEWS, EVENT, DISCUSSION, ALERT
- Content is validated for required fields

#### **analytics.fact_comments** (Fact Table)
Comment fact table storing measurable events.

| Column | Data Type | Constraints | Description |
|--------|-----------|-------------|-------------|
| `source_comment_id` | BIGINT | PRIMARY KEY | Original comment ID (natural key) |
| `post_key` | BIGINT | NOT NULL, FK → dim_posts(post_key) | Parent post warehouse key |
| `commenter_user_key` | BIGINT | NOT NULL, FK → dim_users(user_key) | Comment author warehouse key |
| `created_at` | TIMESTAMP | NOT NULL | Comment creation timestamp |
| `comment_count` | INTEGER | NOT NULL, DEFAULT 1 | Aggregatable comment metric |
| `loaded_at` | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() | Warehouse load timestamp |

**Business Rules:**
- Foreign keys resolve to warehouse surrogate keys
- `comment_count` enables aggregation in KPI views
- Only valid comments (with existing post/user references) are loaded

---

### KPI Materialized Views

#### **analytics.kpi_top_contributors_mv**
Daily aggregation of user contributions (posts + comments).

| Column | Data Type | Description |
|--------|-----------|-------------|
| `activity_date` | DATE | Date of activity |
| `user_key` | BIGINT | User warehouse key |
| `source_user_id` | BIGINT | Original user ID |
| `full_name` | TEXT | User display name |
| `role` | TEXT | User role |
| `post_count` | BIGINT | Number of posts created |
| `comment_count` | BIGINT | Number of comments made |
| `total_contributions` | BIGINT | Sum of posts + comments |

**Refresh Strategy:** Refreshed after each ETL run
**Grain:** One row per user per day

#### **analytics.kpi_activity_trends_mv**
Daily platform activity trends.

| Column | Data Type | Description |
|--------|-----------|-------------|
| `activity_date` | DATE | Date of activity |
| `posts_count` | BIGINT | Total posts created |
| `comments_count` | BIGINT | Total comments made |
| `total_activity_count` | BIGINT | Sum of all activity |

**Refresh Strategy:** Refreshed after each ETL run
**Grain:** One row per day

#### **analytics.kpi_posts_per_category_mv**
Daily post counts by category.

| Column | Data Type | Description |
|--------|-----------|-------------|
| `activity_date` | DATE | Date of activity |
| `category` | TEXT | Post category |
| `posts_count` | BIGINT | Number of posts in category |

**Refresh Strategy:** Refreshed after each ETL run
**Grain:** One row per category per day

---

## Data Dictionary

### Data Types & Formats

#### **Timestamps**
- **Format**: ISO 8601 format (`YYYY-MM-DD HH:MM:SS`)
- **Timezone**: UTC (source), Local with timezone (warehouse `loaded_at`)
- **Null Handling**: Backfilled with max timestamp or current time

#### **Text Fields**
- **Encoding**: UTF-8
- **Null Handling**: Empty strings converted to NULL
- **Cleaning**: Whitespace trimmed, special characters normalized

#### **Categories**
- **Valid Values**: `NEWS`, `EVENT`, `DISCUSSION`, `ALERT`
- **Case**: Uppercase
- **Default**: No default (required field)

#### **User Roles**
- **Valid Values**: `admin`, `member`, `unknown`
- **Case**: Lowercase
- **Default**: `unknown` for null/empty values

#### **Email Addresses**
- **Format**: Standard email format validation
- **Case**: Converted to lowercase
- **Uniqueness**: Enforced at application level

### Data Quality Rules

#### **Required Fields**
```yaml
users: [id, full_name, role, email, created_at]
posts: [id, user_id, title, content, category, created_at]
comments: [id, post_id, user_id, content, created_at]
```

#### **Deduplication Keys**
```yaml
users: id
posts: id
comments: id
```

#### **Foreign Key Validation**
- Comments must reference existing posts and users
- Posts must reference existing users
- Orphaned records are excluded from warehouse load

#### **Data Cleaning Rules**
- **Names**: Remove non-alphabetic characters except spaces, hyphens, apostrophes, periods
- **Emails**: Convert to lowercase, validate format
- **Content**: Strip leading/trailing whitespace
- **Timestamps**: Convert to UTC, validate format

---

## Data Lineage

### ETL Data Flow

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Source DB     │    │   Replica DB    │    │  Analytics DB   │
│   (Backend)     │    │   (Staging)     │    │  (Warehouse)    │
├─────────────────┤    ├─────────────────┤    ├─────────────────┤
│ public.users    │───▶│ replica.users   │───▶│ analytics.dim_  │
│ public.posts    │───▶│ replica.posts   │───▶│   users         │
│ public.comments │───▶│ replica.comments│───▶│ analytics.dim_  │
│ public.categories│    │                 │    │   posts         │
└─────────────────┘    └─────────────────┘    │ analytics.fact_ │
                                              │   comments      │
                                              └─────────────────┘
                                                       │
                                                       ▼
                                              ┌─────────────────┐
                                              │   KPI Views     │
                                              ├─────────────────┤
                                              │ top_contributors│
                                              │ activity_trends │
                                              │ posts_per_cat   │
                                              └─────────────────┘
```

### Transformation Steps

1. **Extract**: Read from source tables with retry logic
2. **Transform**: 
   - Clean and validate data
   - Resolve foreign key relationships
   - Apply business rules
   - Generate surrogate keys
3. **Load**: 
   - Insert/update warehouse tables
   - Refresh materialized views
   - Update load timestamps

### Data Refresh Schedule

- **Replica Sync**: Hourly (configurable)
- **Warehouse Load**: After replica sync completion
- **KPI Views**: After warehouse load completion
- **Full Refresh**: Weekly (truncate and reload)

---

## Sample Data

### User Data Sample
```csv
id,full_name,email,role,created_at
1,Allison Hill,donaldgarcia@example.net,member,2026-02-22 22:47:46
3,Meredith Barnes,zlawrence@example.org,admin,2026-01-16 12:01:59
```

### Post Data Sample
```csv
id,user_id,title,content,category,created_at
1,14,Human public health tonight later.,Ask again network open according...,NEWS,2026-02-14 14:34:14
5,1,Paper memory history office effort remember.,Understand Mrs rest score provide...,EVENT,2026-02-21 20:22:57
```

### Comment Data Sample
```csv
id,post_id,user_id,content,created_at
1,1,11,Win group important civil nature travel read.,2026-02-27 08:58:50
2,1,8,Doctor actually become book off management.,2026-03-09 22:35:04
```

---

## Performance Considerations

### Indexing Strategy

#### **Source Tables**
- Primary keys: Clustered indexes
- Foreign keys: Non-clustered indexes
- Timestamp columns: Indexes for incremental loading

#### **Warehouse Tables**
- Surrogate keys: Clustered indexes
- Foreign keys: Non-clustered indexes
- Date columns: Partitioning candidates

#### **Materialized Views**
- `activity_date`: Primary filtering column
- Composite indexes on frequently joined columns

### Query Optimization

- **Star Schema**: Optimized for analytical queries
- **Surrogate Keys**: Faster joins than natural keys
- **Pre-aggregation**: KPI views reduce query complexity
- **Partitioning**: Date-based partitioning for large fact tables

### Data Volume Estimates

| Table | Estimated Rows | Growth Rate |
|-------|----------------|-------------|
| dim_users | 1,000s | Low |
| dim_posts | 10,000s | Medium |
| fact_comments | 100,000s | High |
| KPI views | 1,000s-10,000s | Medium |

---

## Data Governance

### Data Quality Monitoring
- Schema validation on all datasets
- Foreign key integrity checks
- Data profiling and anomaly detection
- Load success/failure tracking

### Security & Privacy
- No PII in analytics warehouse
- Email addresses are business identifiers only
- Role-based database access
- Audit logging for all data operations

### Backup & Recovery
- Point-in-time recovery for all databases
- Cross-region backup replication
- Automated backup verification
- Disaster recovery procedures documented