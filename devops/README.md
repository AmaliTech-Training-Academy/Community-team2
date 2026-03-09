# DevOps Infrastructure Documentation

## Overview
This directory contains the complete AWS infrastructure as code (IaC) for the CommunityBoard application using Terraform. The infrastructure follows AWS best practices with a focus on security, scalability, and cost optimization.

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Internet                              │
└────────────────────────┬────────────────────────────────────┘
                         │
                    ┌────▼────┐
                    │   ALB   │ (Application Load Balancer)
                    └────┬────┘
                         │
        ┌────────────────┼────────────────┐
        │                │                │
   ┌────▼────┐     ┌────▼────┐     ┌────▼────┐
   │Frontend │     │Backend  │     │Airflow  │
   │  ECS    │     │  ECS    │     │  ECS    │
   └────┬────┘     └────┬────┘     └────┬────┘
        │               │                │
        │          ┌────▼────┐           │
        │          │   RDS   │◄──────────┘
        │          │Primary  │
        │          └────┬────┘
        │               │
        │          ┌────▼────┐
        │          │   RDS   │
        │          │ Replica │
        │          └─────────┘
        │
   ┌────▼──────────────────────────────┐
   │      VPC Endpoints                │
   │  (ECR, S3, Secrets, Logs, SSM)   │
   └───────────────────────────────────┘
```

## Infrastructure Components

### 1. Networking (`modules/networking/`)
- **VPC**: 10.0.0.0/16 CIDR block
- **Public Subnets**: For ALB only (2 AZs)
- **Private Subnets**: For ECS tasks and RDS (2 AZs)
- **Security Groups**: Layered security for ALB, ECS, RDS, and VPC endpoints
- **No NAT Gateway**: VPC endpoints provide AWS service access

### 2. Database (`modules/database/`)
- **Primary RDS**: PostgreSQL 15.4 for read/write operations
- **Read Replica**: For analytics and ETL workloads (Airflow)
- **Multi-AZ**: Disabled (cost optimization for dev/test)
- **Encryption**: At-rest encryption enabled
- **Credentials**: Stored in AWS Secrets Manager

### 3. Compute (`modules/compute/`)
- **ECS Cluster**: Fargate launch type (serverless containers)
- **Backend Service**: Spring Boot API (port 8080) in private subnets
- **Frontend Service**: React SPA with Nginx (port 80) in private subnets
- **Airflow Service**: Data pipeline orchestration (port 8080) in private subnets
- **Auto-scaling**: CPU-based scaling (1-4 tasks)
- **ECS Exec**: Enabled for database access and debugging
- **No Public IPs**: All containers in private subnets

### 4. Load Balancing
- **Application Load Balancer**: Internet-facing in public subnets
- **Access Logs**: Enabled to S3 with 90-day retention
- **Target Groups**: Separate for backend, frontend, and Airflow
- **Health Checks**: Custom paths for each service
- **Routing Rules**:
  - `/api/*` → Backend
  - `/swagger-ui/*` → Backend
  - `/airflow/*` → Airflow
  - `/*` → Frontend (default)

### 5. Container Registry (`ECR`)
- **Backend Repository**: Java Spring Boot images
- **Frontend Repository**: React + Nginx images
- **Airflow Repository**: Apache Airflow images
- **Image Tag Mutability**: Mutable (for development)

### 6. Security (`modules/security/`)
- **Secrets Manager**: JWT secrets and database credentials
- **IAM Roles**: ECS task execution and task roles
- **ECS Exec Permissions**: SSM session management
- **Encryption**: All secrets encrypted at rest

### 7. VPC Endpoints (`modules/vpc-endpoints/`)
Eliminates NAT Gateway costs and bastion host:
- **ECR API**: Docker registry operations
- **ECR DKR**: Container image pulls
- **S3 Gateway**: ECR image layers (free)
- **Secrets Manager**: Credential retrieval
- **CloudWatch Logs**: Container logging
- **SSM**: ECS Exec for database access

### 8. Monitoring (`modules/monitoring/`)
- **CloudWatch Alarms**: CPU, memory, database metrics
- **SNS Notifications**: Email alerts for critical issues
- **Container Insights**: ECS cluster metrics
- **RDS Logs**: PostgreSQL and upgrade logs

### 9. Deployment (`modules/codedeploy/`)
- **Blue/Green Deployment**: Zero-downtime updates
- **CodeDeploy**: Automated rollback on failure
- **Target Groups**: Blue and green for traffic shifting

### 10. State Management
- **Remote State**: S3 backend with DynamoDB locking (optional)
- **State Encryption**: AES256 encryption at rest
- **State Versioning**: Full history of infrastructure changes
- **Team Collaboration**: Concurrent access protection

## Directory Structure

```
devops/
├── terraform/                    # Infrastructure as Code
│   ├── main.tf                  # Root module configuration
│   ├── variables.tf             # Input variables
│   ├── outputs.tf               # Output values
│   ├── terraform.tfvars         # Variable values (gitignored)
│   └── modules/                 # Reusable modules
│       ├── networking/          # VPC, subnets, security groups
│       ├── database/            # RDS primary and replica
│       ├── compute/             # ECS cluster and services
│       ├── security/            # Secrets Manager, IAM
│       ├── vpc-endpoints/       # Private AWS service endpoints
│       ├── monitoring/          # CloudWatch alarms
│       └── codedeploy/          # Blue/green deployment
├── scripts/                     # Deployment automation
│   ├── deploy.sh               # Quick deployment script
│   └── deploy-aws.sh           # Production deployment script
├── DATABASE_ACCESS.md          # Database access guide
└── BASTION_REMOVAL.md          # Bastion host migration guide

## Quick Start

### Prerequisites
- AWS CLI configured with credentials
- Terraform >= 1.0 installed
- Session Manager plugin (for database access)

### 1. (Optional) Setup Remote State
```bash
cd terraform

# Create S3 bucket and DynamoDB table
terraform apply backend-setup.tf

# Uncomment backend block in main.tf, then:
terraform init -migrate-state
```

### 2. Configure Variables
```bash
cd terraform
cp terraform.tfvars.example terraform.tfvars
# Edit terraform.tfvars with your values
```

### 3. Deploy Infrastructure
```bash
# Option 1: Quick deployment
../scripts/deploy.sh development

# Option 2: Production deployment with plan review
../scripts/deploy-aws.sh production
```

### 4. Push Docker Images
```bash
# Login to ECR
aws ecr get-login-password --region us-east-1 | \
  docker login --username AWS --password-stdin <ECR_URL>

# Build and push images
docker build -t communityboard-backend:latest ./backend
docker tag communityboard-backend:latest <ECR_BACKEND_URL>:latest
docker push <ECR_BACKEND_URL>:latest

# Repeat for frontend and airflow
```

### 5. Access Application
```bash
# Get ALB DNS name
terraform output alb_dns_name

# Access services
# Frontend: http://<ALB_DNS>/
# Backend API: http://<ALB_DNS>/api
# Swagger UI: http://<ALB_DNS>/swagger-ui.html
```

## Database Access

Since the bastion host has been removed, use ECS Exec for database access:

```bash
# Get cluster and task
CLUSTER=$(terraform output -raw ecs_cluster_name)
TASK_ID=$(aws ecs list-tasks --cluster $CLUSTER \
  --service-name communityboard-backend \
  --query 'taskArns[0]' --output text | cut -d'/' -f3)

# Connect to backend container
aws ecs execute-command \
  --cluster $CLUSTER \
  --task $TASK_ID \
  --container backend \
  --interactive \
  --command "/bin/sh"

# Inside container, connect to database
psql -h $DB_HOST -U $DB_USER -d communityboard
```

See [DATABASE_ACCESS.md](./DATABASE_ACCESS.md) for detailed instructions.

## Cost Optimization

### Current Setup (Development)
- **ECS Fargate**: ~$15-30/month (3 services, minimal tasks)
- **RDS Single-AZ**: ~$15/month (db.t3.micro)
- **RDS Read Replica**: ~$15/month (db.t3.micro)
- **ALB**: ~$16/month
- **VPC Endpoints**: ~$21/month (3 interface endpoints)
- **S3 (ALB Logs)**: ~$0.50-2/month
- **S3 + DynamoDB (State)**: ~$1-3/month (optional)
- **Data Transfer**: Variable
- **Total**: ~$82-102/month

### Cost Savings Applied
- ✅ Removed bastion host (~$3-10/month saved)
- ✅ Disabled Multi-AZ (~$15/month saved)
- ✅ VPC endpoints instead of NAT Gateway (~$32/month saved)
- ✅ ECS in private subnets (no public IPs)
- ✅ S3 lifecycle policies (auto-delete old logs)
- ✅ Fargate Spot (optional, 70% savings on compute)

### Production Recommendations
- Enable Multi-AZ for RDS (`multi_az = true`)
- Enable automated backups (`backup_retention_period = 7`)
- Use Reserved Instances for predictable workloads
- Enable CloudWatch Logs retention policies

## Security Best Practices

### Implemented
- ✅ ECS containers in private subnets (no public IPs)
- ✅ Database in private subnets
- ✅ Secrets in AWS Secrets Manager
- ✅ Encryption at rest (RDS, Secrets, S3)
- ✅ Security groups with least privilege
- ✅ No SSH keys (ECS Exec via IAM)
- ✅ VPC endpoints for private communication
- ✅ CloudWatch logging for audit trails
- ✅ ALB access logs enabled (90-day retention)
- ✅ Remote state with encryption and locking (optional)
- ✅ S3 public access blocked
- ✅ IAM roles instead of access keys

### Recommended for Production
- [ ] Enable AWS WAF on ALB
- [ ] Implement AWS Shield for DDoS protection
- [ ] Use AWS Certificate Manager for HTTPS
- [ ] Enable GuardDuty for threat detection
- [ ] Implement AWS Config for compliance
- [ ] Use AWS Systems Manager Parameter Store for non-secret configs

## Monitoring and Alerts

### CloudWatch Alarms
- **ECS CPU Utilization**: Alert if > 80%
- **ECS Memory Utilization**: Alert if > 80%
- **RDS CPU Utilization**: Alert if > 80%
- **RDS Free Storage**: Alert if < 2GB
- **ALB Target Health**: Alert on unhealthy targets

### Logs
- **ECS Container Logs**: `/ecs/communityboard-{service}`
- **RDS Logs**: PostgreSQL and upgrade logs
- **ALB Access Logs**: S3 bucket with 90-day retention
- **Retention**: 7 days (CloudWatch), 90 days (S3)

## Additional Documentation

- **[DATABASE_ACCESS.md](./DATABASE_ACCESS.md)** - Connect to RDS via ECS Exec
- **[BASTION_REMOVAL.md](./BASTION_REMOVAL.md)** - Migration from bastion to VPC endpoints
- **[ISSUES_AUDIT.md](./ISSUES_AUDIT.md)** - Security and infrastructure fixes
- **[DOCUMENTATION_SUMMARY.md](./DOCUMENTATION_SUMMARY.md)** - Documentation overview

## Troubleshooting

### ECS Tasks Not Starting
```bash
# Check service events
aws ecs describe-services \
  --cluster communityboard-cluster \
  --services communityboard-backend

# Check task logs
aws logs tail /ecs/communityboard-backend --follow
```

### Database Connection Issues
```bash
# Verify security group rules
aws ec2 describe-security-groups \
  --group-ids <RDS_SG_ID>

# Test from ECS task
aws ecs execute-command \
  --cluster $CLUSTER \
  --task $TASK_ID \
  --container backend \
  --command "nc -zv $DB_HOST 5432"
```

### ECS Exec Not Working
```bash
# Verify service has exec enabled
aws ecs describe-services \
  --cluster $CLUSTER \
  --services communityboard-backend \
  --query 'services[0].enableExecuteCommand'

# Update service if needed
aws ecs update-service \
  --cluster $CLUSTER \
  --service communityboard-backend \
  --enable-execute-command \
  --force-new-deployment
```

## Cleanup

To destroy all infrastructure:

```bash
cd terraform
terraform destroy -auto-approve
```

**Warning**: This will delete all resources including databases. Ensure you have backups if needed.

## Additional Resources

- [AWS ECS Best Practices](https://docs.aws.amazon.com/AmazonECS/latest/bestpracticesguide/)
- [Terraform AWS Provider](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
- [AWS VPC Endpoints](https://docs.aws.amazon.com/vpc/latest/privatelink/vpc-endpoints.html)
- [ECS Exec Documentation](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/ecs-exec.html)

## Support

For issues or questions:
1. Check CloudWatch logs for error messages
2. Review Terraform state: `terraform show`
3. Consult AWS documentation
4. Contact DevOps team
