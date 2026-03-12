# AWS Services for Metrics & Logs - Detailed Breakdown

## Overview
This document explains exactly which AWS services collect metrics and logs, what data they provide, and how to access them.

---

## 1. Amazon CloudWatch (Core Monitoring Service)

### What It Does
CloudWatch is AWS's central monitoring and observability service that collects, stores, and visualizes metrics and logs from all AWS services.

### What You Get

#### A. CloudWatch Metrics (Automatic)
**Source**: AWS services automatically send metrics to CloudWatch

**ECS Metrics** (No configuration needed):
- `CPUUtilization` - CPU usage percentage
- `MemoryUtilization` - Memory usage percentage
- `NetworkRxBytes` - Network bytes received
- `NetworkTxBytes` - Network bytes transmitted

**How to Access**:
```bash
# View backend CPU metrics
aws cloudwatch get-metric-statistics \
  --namespace AWS/ECS \
  --metric-name CPUUtilization \
  --dimensions Name=ServiceName,Value=communityboard-backend Name=ClusterName,Value=communityboard-cluster \
  --start-time 2024-01-01T00:00:00Z \
  --end-time 2024-01-01T23:59:59Z \
  --period 300 \
  --statistics Average,Maximum \
  --region eu-west-1
```

**Console Access**:
1. AWS Console → CloudWatch
2. Metrics → All metrics
3. Select "ECS" → "ClusterName, ServiceName"
4. Choose your service

---

## 2. CloudWatch Container Insights (Enhanced ECS Monitoring)

### What It Does
Provides deeper visibility into containerized applications with automatic dashboards and enhanced metrics.

### Configuration
**Status**: ✅ Already enabled in your ECS cluster
```hcl
resource "aws_ecs_cluster" "main" {
  name = "communityboard-cluster"
  setting {
    name  = "containerInsights"
    value = "enabled"
  }
}
```

### What You Get

#### Enhanced Metrics (Automatic):
- **Task-level metrics**: CPU, memory, network, disk per task
- **Container-level metrics**: Per-container resource usage
- **Service-level aggregations**: Average across all tasks
- **Performance metrics**: Task count, running count, pending count

#### Pre-built Dashboards:
- ECS Services dashboard
- ECS Tasks dashboard
- ECS Clusters dashboard

**How to Access**:
1. AWS Console → CloudWatch
2. Left menu → "Container Insights"
3. Select view: "ECS Services" or "ECS Tasks"
4. Filter by cluster: `communityboard-cluster`

**What You See**:
- CPU utilization graphs
- Memory utilization graphs
- Network traffic
- Task count over time
- Service map

---

## 3. CloudWatch Logs (Application Logs)

### What It Does
Collects and stores logs from your application containers (stdout/stderr).

### Configuration
**Status**: ✅ Already configured in ECS task definitions

```json
{
  "logConfiguration": {
    "logDriver": "awslogs",
    "options": {
      "awslogs-group": "/ecs/communityboard-backend",
      "awslogs-region": "eu-west-1",
      "awslogs-stream-prefix": "ecs"
    }
  }
}
```

### What You Get

#### Log Groups:
| Service | Log Group | What's Logged |
|---------|-----------|---------------|
| Backend | `/ecs/communityboard-backend` | Spring Boot logs, API requests, errors, SQL queries |
| Frontend | `/ecs/communityboard-frontend` | Nginx access logs, error logs |
| Airflow | `/ecs/communityboard-airflow` | Airflow scheduler, worker, webserver logs |

#### Log Streams:
Each ECS task creates its own log stream:
- Format: `ecs/container-name/task-id`
- Example: `ecs/backend/a1b2c3d4-5678-90ab-cdef-1234567890ab`

### How to Access

#### Real-time Tail:
```bash
# Tail backend logs (live)
aws logs tail /ecs/communityboard-backend --follow --region eu-west-1

# Tail frontend logs
aws logs tail /ecs/communityboard-frontend --follow --region eu-west-1

# Filter for errors only
aws logs tail /ecs/communityboard-backend --follow --filter-pattern "ERROR" --region eu-west-1
```

#### Search Historical Logs:
```bash
# Search for errors in last hour
aws logs filter-log-events \
  --log-group-name /ecs/communityboard-backend \
  --start-time $(date -u -d '1 hour ago' +%s)000 \
  --filter-pattern "ERROR" \
  --region eu-west-1
```

#### Console Access:
1. AWS Console → CloudWatch
2. Logs → Log groups
3. Click `/ecs/communityboard-backend`
4. Click on a log stream to view logs

### What Backend Logs Contain:
```
2024-03-11 10:15:23.456 INFO  [http-nio-8080-exec-1] c.a.c.controller.PostController : GET /api/v1/posts
2024-03-11 10:15:23.478 DEBUG [http-nio-8080-exec-1] o.h.SQL : select * from posts
2024-03-11 10:15:23.512 INFO  [http-nio-8080-exec-1] c.a.c.controller.PostController : Returned 25 posts
2024-03-11 10:15:30.123 ERROR [http-nio-8080-exec-2] c.a.c.service.PostService : Failed to create post: Database connection timeout
```

### What Frontend Logs Contain:
```
172.31.0.5 - - [11/Mar/2024:10:15:23 +0000] "GET / HTTP/1.1" 200 1234 "-" "Mozilla/5.0"
172.31.0.5 - - [11/Mar/2024:10:15:24 +0000] "GET /api/v1/posts HTTP/1.1" 200 5678 "http://example.com/" "Mozilla/5.0"
2024/03/11 10:15:25 [error] 7#7: *1 connect() failed (111: Connection refused) while connecting to upstream
```

---

## 4. CloudWatch Logs Insights (Log Analysis)

### What It Does
SQL-like query language to analyze logs.

### How to Use

**Console Access**:
1. AWS Console → CloudWatch
2. Logs → Insights
3. Select log group: `/ecs/communityboard-backend`
4. Enter query

**Example Queries**:

```sql
# Find all errors
fields @timestamp, @message
| filter @message like /ERROR/
| sort @timestamp desc
| limit 100

# Count errors by type
fields @message
| filter @message like /ERROR/
| parse @message /ERROR.*: (?<error_type>.*)/
| stats count() by error_type

# Find slow API requests
fields @timestamp, @message
| filter @message like /duration/
| parse @message /duration=(?<duration>\d+)/
| filter duration > 1000
| sort duration desc

# API endpoint usage
fields @timestamp, @message
| filter @message like /GET|POST|PUT|DELETE/
| parse @message /(?<method>GET|POST|PUT|DELETE) (?<endpoint>\/api\/[^ ]*)/
| stats count() by endpoint, method
| sort count desc
```

---

## 5. Application Load Balancer (ALB) Metrics

### What It Does
Automatically sends metrics about HTTP requests to CloudWatch.

### What You Get (Automatic)

#### ALB Metrics:
- `RequestCount` - Total number of requests
- `TargetResponseTime` - Time for targets to respond
- `HTTPCode_Target_2XX_Count` - Successful responses
- `HTTPCode_Target_4XX_Count` - Client errors
- `HTTPCode_Target_5XX_Count` - Server errors
- `ActiveConnectionCount` - Active connections
- `NewConnectionCount` - New connections per minute
- `TargetConnectionErrorCount` - Failed connections

**How to Access**:
```bash
# View response time
aws cloudwatch get-metric-statistics \
  --namespace AWS/ApplicationELB \
  --metric-name TargetResponseTime \
  --dimensions Name=LoadBalancer,Value=app/communityboard-alb/e6a5f90fc4943747 \
  --start-time $(date -u -d '1 hour ago' --iso-8601) \
  --end-time $(date -u --iso-8601) \
  --period 300 \
  --statistics Average,Maximum \
  --region eu-west-1

# View error count
aws cloudwatch get-metric-statistics \
  --namespace AWS/ApplicationELB \
  --metric-name HTTPCode_Target_5XX_Count \
  --dimensions Name=LoadBalancer,Value=app/communityboard-alb/e6a5f90fc4943747 \
  --start-time $(date -u -d '1 hour ago' --iso-8601) \
  --end-time $(date -u --iso-8601) \
  --period 300 \
  --statistics Sum \
  --region eu-west-1
```

**Console Access**:
1. AWS Console → EC2 → Load Balancers
2. Select `communityboard-alb`
3. Click "Monitoring" tab
4. View graphs

---

## 6. ALB Access Logs (Detailed Request Logs)

### What It Does
Stores detailed logs of every HTTP request to S3.

### Configuration
**Status**: ✅ Already enabled
```hcl
resource "aws_lb" "main" {
  access_logs {
    bucket  = "communityboard-alb-logs-eu-west-1"
    enabled = true
  }
}
```

### What You Get

#### Log Format (Each line is one request):
```
http 2024-03-11T10:15:23.456789Z app/communityboard-alb/e6a5f90fc4943747 
172.31.0.5:45678 10.0.1.10:8080 0.001 0.002 0.000 200 200 1234 5678 
"GET http://communityboard-alb-905603474.eu-west-1.elb.amazonaws.com:80/api/v1/posts HTTP/1.1" 
"Mozilla/5.0 (Windows NT 10.0; Win64; x64)" ECDHE-RSA-AES128-GCM-SHA256 TLSv1.2
```

**Fields Include**:
- Timestamp
- Client IP and port
- Target IP and port
- Request processing time
- Target processing time
- Response processing time
- HTTP status codes
- Request URL
- User agent

**How to Access**:
```bash
# Download logs from S3
aws s3 ls s3://communityboard-alb-logs-eu-west-1/ --recursive

# Download specific log file
aws s3 cp s3://communityboard-alb-logs-eu-west-1/AWSLogs/010679547158/elasticloadbalancing/eu-west-1/2024/03/11/... ./

# Analyze with grep
grep "5XX" alb-log-file.log
grep "/api/v1/posts" alb-log-file.log
```

**Use Cases**:
- Identify slow requests
- Track user behavior
- Debug specific requests
- Security analysis
- Compliance auditing

---

## 7. RDS CloudWatch Metrics

### What It Does
RDS automatically sends database metrics to CloudWatch.

### What You Get (Automatic)

#### RDS Metrics:
- `CPUUtilization` - Database CPU usage
- `DatabaseConnections` - Number of connections
- `FreeableMemory` - Available RAM
- `FreeStorageSpace` - Available disk space
- `ReadLatency` - Read operation latency
- `WriteLatency` - Write operation latency
- `ReadIOPS` - Read operations per second
- `WriteIOPS` - Write operations per second
- `NetworkReceiveThroughput` - Network in
- `NetworkTransmitThroughput` - Network out

**How to Access**:
```bash
# View database connections
aws cloudwatch get-metric-statistics \
  --namespace AWS/RDS \
  --metric-name DatabaseConnections \
  --dimensions Name=DBInstanceIdentifier,Value=communityboard-db \
  --start-time $(date -u -d '1 hour ago' --iso-8601) \
  --end-time $(date -u --iso-8601) \
  --period 300 \
  --statistics Average,Maximum \
  --region eu-west-1
```

**Console Access**:
1. AWS Console → RDS
2. Select `communityboard-db`
3. Click "Monitoring" tab

---

## 8. RDS Enhanced Monitoring (Optional)

### What It Does
Provides OS-level metrics from the RDS instance.

### Status
**Not currently enabled** - Can be enabled if needed

### What You Would Get:
- OS processes
- CPU by process
- Memory by process
- File system usage
- Swap usage

---

## 9. CloudWatch Alarms (Alerting)

### What It Does
Monitors metrics and sends notifications when thresholds are breached.

### Configuration
**Status**: ✅ Configured via Terraform

### Current Alarms:
```bash
# List all alarms
aws cloudwatch describe-alarms \
  --alarm-name-prefix communityboard \
  --region eu-west-1 \
  --query 'MetricAlarms[*].[AlarmName,StateValue,MetricName,Threshold]' \
  --output table
```

### How Alerts Work:
1. CloudWatch monitors metric (e.g., CPU > 80%)
2. If threshold breached for 2 evaluation periods (10 minutes)
3. Alarm state changes to "ALARM"
4. SNS topic `communityboard-alerts` is triggered
5. Email sent to subscribers

**Subscribe to Alerts**:
```bash
aws sns subscribe \
  --topic-arn arn:aws:sns:eu-west-1:010679547158:communityboard-alerts \
  --protocol email \
  --notification-endpoint your-email@example.com \
  --region eu-west-1
```

---

## 10. AWS X-Ray (Distributed Tracing) - NOT IMPLEMENTED

### What It Would Do
Track requests as they flow through your application.

### Example Flow:
```
User Request → ALB → Frontend → Backend → RDS
     ↓           ↓       ↓          ↓        ↓
  X-Ray      X-Ray   X-Ray      X-Ray    X-Ray
```

### What You Would Get:
- Request trace timeline
- Service map
- Latency breakdown
- Error identification
- Bottleneck detection

**Status**: Not implemented (future enhancement)

---

## Summary Table

| Service | Type | What It Provides | Access Method | Cost |
|---------|------|------------------|---------------|------|
| CloudWatch Metrics | Metrics | CPU, Memory, Network | Console, CLI, API | Free (basic), $0.30/metric |
| Container Insights | Metrics | Enhanced container metrics | Console | ~$0.30/container/month |
| CloudWatch Logs | Logs | Application logs (stdout/stderr) | Console, CLI, Logs Insights | $0.50/GB ingested |
| Logs Insights | Analysis | SQL-like log queries | Console | $0.005/GB scanned |
| ALB Metrics | Metrics | Request count, latency, errors | Console, CLI | Free |
| ALB Access Logs | Logs | Detailed request logs | S3 | S3 storage costs |
| RDS Metrics | Metrics | Database performance | Console, CLI | Free |
| CloudWatch Alarms | Alerts | Threshold-based notifications | Console, CLI, SNS | $0.10/alarm/month |

---

## Quick Access Commands

```bash
# View all metrics for backend service
aws cloudwatch list-metrics \
  --namespace AWS/ECS \
  --dimensions Name=ServiceName,Value=communityboard-backend \
  --region eu-west-1

# Tail backend logs live
aws logs tail /ecs/communityboard-backend --follow --region eu-west-1

# Check alarm status
aws cloudwatch describe-alarms \
  --alarm-names communityboard-backend-cpu-high \
  --region eu-west-1

# View ALB metrics
aws cloudwatch get-metric-statistics \
  --namespace AWS/ApplicationELB \
  --metric-name RequestCount \
  --dimensions Name=LoadBalancer,Value=app/communityboard-alb/e6a5f90fc4943747 \
  --start-time $(date -u -d '1 hour ago' --iso-8601) \
  --end-time $(date -u --iso-8601) \
  --period 300 \
  --statistics Sum \
  --region eu-west-1
```

---

## What Happens Automatically vs. What You Need to Configure

### ✅ Automatic (No Action Needed):
- ECS metrics collection
- Container Insights (already enabled)
- CloudWatch Logs collection
- ALB metrics
- ALB access logs (already enabled)
- RDS metrics
- CloudWatch alarms (already configured)

### 📧 Requires Action:
- Subscribe to SNS alerts (add your email)
- Create custom CloudWatch dashboards (optional)
- Set up log retention policies (optional)
- Enable RDS Enhanced Monitoring (optional)
- Implement AWS X-Ray (future enhancement)

---

**Your monitoring is already 90% configured! Just add your email to SNS alerts and you're fully operational.**
