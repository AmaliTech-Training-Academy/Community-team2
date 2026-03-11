# Multiple Databases on RDS Instance

## Overview
The RDS instance hosts three PostgreSQL databases:
- **communityboard**: Main application database (backend API)
- **replicadb**: Replica/read operations database
- **analyticsdb**: Analytics and reporting database

## Terraform Deployment

### Apply Changes
```bash
cd devops/terraform
terraform init -upgrade  # Update postgresql provider
terraform plan           # Review changes
terraform apply          # Create databases
```

## Connection Strings

### CommunityBoard (Main)
```
postgresql://postgres:<password>@communityboard-db.cfuou2mwe5a1.eu-west-1.rds.amazonaws.com:5432/communityboard
```

### ReplicaDB
```
postgresql://postgres:<password>@communityboard-db.cfuou2mwe5a1.eu-west-1.rds.amazonaws.com:5432/replicadb
```

### AnalyticsDB
```
postgresql://postgres:<password>@communityboard-db.cfuou2mwe5a1.eu-west-1.rds.amazonaws.com:5432/analyticsdb
```

## Usage Examples

### Python (SQLAlchemy)
```python
from sqlalchemy import create_engine

# ReplicaDB connection
replica_engine = create_engine(
    "postgresql://postgres:password@communityboard-db.cfuou2mwe5a1.eu-west-1.rds.amazonaws.com:5432/replicadb"
)

# AnalyticsDB connection
analytics_engine = create_engine(
    "postgresql://postgres:password@communityboard-db.cfuou2mwe5a1.eu-west-1.rds.amazonaws.com:5432/analyticsdb"
)
```

### Java (Spring Boot)
```yaml
# application.yml
spring:
  datasource:
    replica:
      url: jdbc:postgresql://communityboard-db.cfuou2mwe5a1.eu-west-1.rds.amazonaws.com:5432/replicadb
      username: postgres
      password: ${DB_PASSWORD}
    analytics:
      url: jdbc:postgresql://communityboard-db.cfuou2mwe5a1.eu-west-1.rds.amazonaws.com:5432/analyticsdb
      username: postgres
      password: ${DB_PASSWORD}
```

## AWS Secrets Manager
All connection details are stored in AWS Secrets Manager:
```bash
aws secretsmanager get-secret-value \
  --secret-id communityboard-db-credentials \
  --region eu-west-1
```

## Direct psql Access
```bash
# ReplicaDB
psql -h communityboard-db.cfuou2mwe5a1.eu-west-1.rds.amazonaws.com -U postgres -d replicadb

# AnalyticsDB
psql -h communityboard-db.cfuou2mwe5a1.eu-west-1.rds.amazonaws.com -U postgres -d analyticsdb
```
