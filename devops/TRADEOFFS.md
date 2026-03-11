# Implementation Trade-offs & Design Decisions

## 1. Database Architecture

### Decision: Single RDS Instance with Multiple Databases + Read Replica

**✅ Pros:**
- **Cost-effective:** ~$45/month vs ~$60+ for separate instances
- **Simplified management:** One instance to monitor, patch, backup
- **Resource sharing:** Efficient use of compute for low-traffic apps
- **Easy setup:** PostgreSQL natively supports multiple databases

**❌ Cons:**
- **Resource contention:** Analytics queries can impact app performance
- **No independent scaling:** Can't scale analytics DB separately
- **Single point of failure:** All databases affected if instance fails (mitigated by Multi-AZ)
- **Backup complexity:** Can't backup databases independently

**Alternative Considered:**
- Separate RDS instances per database (higher cost, better isolation)

---

## 2. Compute: ECS Fargate vs EC2

### Decision: ECS Fargate for all services

**✅ Pros:**
- **No server management:** AWS handles infrastructure
- **Auto-scaling:** Scales containers based on CPU/memory
- **Pay-per-use:** Only pay for running containers
- **Fast deployment:** Containers start in seconds
- **Security:** Isolated execution environment

**❌ Cons:**
- **Higher cost per hour:** ~30% more expensive than EC2 for 24/7 workloads
- **Cold start:** Initial container startup takes 30-60 seconds
- **Limited customization:** Can't access underlying host
- **Resource limits:** Max 4 vCPU, 30GB memory per task

**Alternative Considered:**
- EC2 with Docker Compose (cheaper for 24/7, more management overhead)

---

## 3. Airflow Deployment: Containerized vs Managed (MWAA)

### Decision: Containerized Airflow on ECS Fargate

**✅ Pros:**
- **Cost:** ~$30/month vs ~$300/month for MWAA
- **Full control:** Custom plugins, configurations, versions
- **Integrated:** Uses same infrastructure as other services
- **Simple for small teams:** Single container, LocalExecutor

**❌ Cons:**
- **Manual management:** Updates, scaling, monitoring on you
- **Single executor:** LocalExecutor doesn't scale horizontally
- **No built-in HA:** Single container failure stops all DAGs
- **Limited features:** No managed scheduler, workers, or web auth

**Alternative Considered:**
- Amazon MWAA (fully managed, expensive, overkill for small projects)

---

## 4. Auto-scaling Configuration

### Decision: 1-4 containers, 70% CPU threshold

**✅ Pros:**
- **Cost-optimized:** Scales down to 1 during low traffic
- **Responsive:** Scales up before hitting capacity
- **Simple:** Single metric (CPU) easy to understand
- **Prevents overload:** Max 4 containers caps costs

**❌ Cons:**
- **Slow scale-up:** Takes 2-3 minutes to add containers
- **CPU-only:** Doesn't consider memory, request count, or latency
- **Fixed limits:** Max 4 may not handle viral traffic spikes
- **Cold start impact:** New containers take time to warm up

**Alternative Considered:**
- Target tracking on multiple metrics (complex, better for large scale)

---

## 5. Secrets Management: AWS Secrets Manager vs Parameter Store

### Decision: AWS Secrets Manager

**✅ Pros:**
- **Automatic rotation:** Can rotate DB passwords automatically
- **Versioning:** Track secret changes over time
- **Fine-grained access:** IAM policies per secret
- **Audit logging:** CloudTrail tracks all access
- **JSON support:** Store multiple values in one secret

**❌ Cons:**
- **Cost:** $0.40/secret/month + $0.05 per 10K API calls
- **Overkill for static secrets:** JWT secret doesn't need rotation
- **Complexity:** More setup than environment variables

**Alternative Considered:**
- SSM Parameter Store (free for standard params, less features)
- Environment variables (free, insecure, no rotation)

---

## 6. Monitoring: CloudWatch vs Third-party (Datadog, New Relic)

### Decision: CloudWatch Alarms + SNS

**✅ Pros:**
- **Native integration:** Built into AWS services
- **Cost-effective:** Free tier covers basic monitoring
- **Simple setup:** Terraform-managed alarms
- **Email alerts:** SNS notifications to team

**❌ Cons:**
- **Basic metrics:** Limited to CPU, memory, request count
- **No APM:** Can't trace requests across services
- **Poor visualization:** Basic dashboards vs Grafana/Datadog
- **Alert fatigue:** Email-only, no smart routing or escalation

**Alternative Considered:**
- Datadog/New Relic (better UX, expensive ~$15/host/month)
- Prometheus + Grafana (free, requires self-hosting)

---

## 7. Database Backup Strategy

### Decision: Automated daily backups, 7-day retention

**✅ Pros:**
- **Automated:** No manual intervention needed
- **Point-in-time recovery:** Restore to any time in last 7 days
- **Cost-effective:** Included in RDS pricing
- **Multi-AZ:** Backups stored across availability zones

**❌ Cons:**
- **Short retention:** Only 7 days (compliance may need 30+)
- **No cross-region:** Backups in same region (disaster recovery risk)
- **Restore time:** Can take 10-30 minutes
- **No table-level restore:** Must restore entire database

**Alternative Considered:**
- Manual snapshots (flexible retention, manual process)
- Cross-region replication (expensive, better DR)

---

## 8. Load Balancer: ALB vs NLB

### Decision: Application Load Balancer (ALB)

**✅ Pros:**
- **Path-based routing:** `/api/*` → backend, `/` → frontend
- **HTTP/HTTPS:** Layer 7 features (headers, cookies)
- **Health checks:** Application-level checks
- **WebSocket support:** For real-time features

**❌ Cons:**
- **Cost:** ~$16/month + $0.008/LCU-hour
- **Latency:** Slight overhead vs NLB (~1-2ms)
- **Overkill for simple apps:** Could use CloudFront + S3 for frontend

**Alternative Considered:**
- Network Load Balancer (faster, no path routing)
- API Gateway (serverless, expensive for high traffic)

---

## 9. CI/CD: GitHub Actions vs Jenkins/GitLab CI

### Decision: GitHub Actions

**✅ Pros:**
- **Integrated:** Built into GitHub
- **Free tier:** 2,000 minutes/month for private repos
- **Simple YAML:** Easy to understand and maintain
- **Marketplace:** Pre-built actions for AWS, Docker, etc.

**❌ Cons:**
- **Vendor lock-in:** Tied to GitHub
- **Limited runners:** 2-core, 7GB RAM max
- **Cost at scale:** $0.008/minute after free tier
- **No self-hosted caching:** Slower builds than Jenkins

**Alternative Considered:**
- Jenkins (free, self-hosted, complex setup)
- GitLab CI (integrated, better for GitLab users)

---

## 10. Infrastructure as Code: Terraform vs CloudFormation

### Decision: Terraform with Modules

**✅ Pros:**
- **Multi-cloud:** Can manage AWS, Azure, GCP
- **Modular:** Reusable modules for networking, compute, etc.
- **State management:** Tracks infrastructure changes
- **Large community:** Extensive documentation and examples

**❌ Cons:**
- **State file management:** Must secure and backup state
- **Learning curve:** HCL syntax different from YAML
- **Drift detection:** Manual, not automatic like CloudFormation
- **No native AWS support:** Third-party provider

**Alternative Considered:**
- CloudFormation (native AWS, verbose YAML)
- CDK (code-based, steeper learning curve)
- Pulumi (real programming languages, newer)

---

## 11. Read Replica Usage

### Decision: Dedicated read replica for ETL workloads

**✅ Pros:**
- **Performance isolation:** ETL doesn't impact production
- **Scalability:** Can add more replicas for read scaling
- **Disaster recovery:** Can promote to primary if needed
- **Cost-effective:** Cheaper than separate analytics DB

**❌ Cons:**
- **Replication lag:** 1-5 seconds behind primary
- **Extra cost:** ~$15/month for replica instance
- **Stale data:** Analytics may not reflect latest changes
- **Complexity:** Two endpoints to manage

**Alternative Considered:**
- Query primary directly (simpler, performance risk)
- Separate analytics DB (no lag, higher cost)

---

## 12. Container Registry: ECR vs Docker Hub

### Decision: Amazon ECR

**✅ Pros:**
- **Integrated:** Native AWS service, IAM authentication
- **Private:** Images not publicly accessible
- **Scanning:** Vulnerability scanning built-in
- **Fast:** Low latency to ECS in same region

**❌ Cons:**
- **Cost:** $0.10/GB/month storage
- **Vendor lock-in:** AWS-specific
- **No public images:** Can't share publicly like Docker Hub

**Alternative Considered:**
- Docker Hub (free public, rate limits, slower pulls)
- GitHub Container Registry (integrated with GitHub)

---

## Summary: Cost vs Performance vs Complexity

| Aspect | Cost | Performance | Complexity | Choice |
|--------|------|-------------|------------|--------|
| Database | Low | Medium | Low | Single RDS + Replica |
| Compute | Medium | High | Low | ECS Fargate |
| Airflow | Low | Low | Medium | Containerized |
| Monitoring | Low | Low | Low | CloudWatch |
| Secrets | Low | High | Medium | Secrets Manager |
| CI/CD | Low | Medium | Low | GitHub Actions |
| IaC | Free | N/A | Medium | Terraform |

**Total Monthly Cost Estimate:** ~$60-80/month
- RDS Multi-AZ: ~$30
- Read Replica: ~$15
- ECS Fargate: ~$20-30 (depends on usage)
- ALB: ~$16
- Secrets Manager: ~$2
- CloudWatch: Free tier

**Recommended for:**
- Small to medium teams (5-50 users)
- MVP/early-stage products
- Budget-conscious projects
- Learning/educational purposes

**Not recommended for:**
- High-traffic production (>10K requests/min)
- Mission-critical systems requiring 99.99% uptime
- Compliance-heavy industries (finance, healthcare)
- Real-time analytics requirements
