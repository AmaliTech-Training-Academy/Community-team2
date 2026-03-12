# RDS Migration to Private Subnet - Instructions

## Overview
This migration creates a new RDS instance in private subnets from a snapshot of your current public RDS instance.

## What This Does

1. **Creates snapshot** of current RDS instance (communityboard-db)
2. **Creates new DB subnet group** with only private subnets
3. **Restores snapshot** to new RDS instance (communityboard-db-private)
4. **Disables public access** on new instance
5. **Updates Secrets Manager** with new private DB endpoint
6. **Keeps old instance** running until you verify and delete it

## Prerequisites

- Existing RDS instance must be healthy
- Private subnets must exist
- VPC endpoints must be configured (already done)

## Migration Steps

### Step 1: Review the Plan
```bash
cd devops/terraform
terraform plan -out=rds-migration.tfplan
```

Review the plan to ensure:
- Snapshot will be created
- New DB subnet group uses private subnets
- New RDS instance has `publicly_accessible = false`

### Step 2: Apply the Migration
```bash
terraform apply rds-migration.tfplan
```

This will take 10-15 minutes:
- Snapshot creation: ~5 minutes
- Instance restoration: ~10 minutes

### Step 3: Verify New Private Instance
```bash
# Get new endpoint
terraform output private_db_endpoint

# Test connection from ECS task (backend container)
aws ecs execute-command \
  --cluster communityboard-cluster \
  --task <TASK_ID> \
  --container backend \
  --command "psql -h <NEW_ENDPOINT> -U postgres -d communityboard -c 'SELECT 1;'" \
  --interactive \
  --region eu-west-1
```

### Step 4: Update Application (Automatic)
The Secrets Manager secret is automatically updated with the new endpoint.
Your backend will use the new private DB on next deployment or restart.

### Step 5: Restart Backend Service
```bash
# Force new deployment to pick up new DB endpoint
aws ecs update-service \
  --cluster communityboard-cluster \
  --service communityboard-backend \
  --force-new-deployment \
  --region eu-west-1
```

### Step 6: Verify Application Works
```bash
# Check backend logs
aws logs tail /ecs/communityboard-backend --follow --region eu-west-1

# Test API endpoint
curl https://<YOUR_ALB_DNS>/api/health
```

### Step 7: Delete Old Public Instance (After Verification)
```bash
# Remove old database module from main.tf or comment it out
# Then apply
terraform apply
```

## Rollback Plan

If something goes wrong:

1. **Revert Secrets Manager** to old endpoint:
```bash
aws secretsmanager put-secret-value \
  --secret-id communityboard-db-credentials \
  --secret-string '{"host":"<OLD_ENDPOINT>",...}' \
  --region eu-west-1
```

2. **Restart backend** to reconnect to old instance

3. **Delete new private instance**:
```bash
terraform destroy -target=aws_db_instance.postgres_private
```

## Security Improvements

After migration:
- ✅ RDS not accessible from internet
- ✅ Only ECS tasks in private subnets can connect
- ✅ All traffic stays within VPC
- ✅ No NAT Gateway needed (VPC endpoints handle AWS services)

## Cost Impact

- **Snapshot storage**: ~$0.095/GB-month (minimal for 20GB database)
- **Running both instances**: 2x RDS cost during migration
- **After deleting old instance**: Same cost as before

## Timeline

- **Snapshot creation**: 5 minutes
- **Instance restoration**: 10 minutes
- **Verification**: 5 minutes
- **Total downtime**: 0 minutes (old instance stays running)

## Notes

- Old instance remains running until you manually delete it
- Snapshot is kept for rollback purposes
- New instance has 7-day backup retention (vs 1-day on old)
- All 3 databases (communityboard, replicadb, analyticsdb) are migrated
