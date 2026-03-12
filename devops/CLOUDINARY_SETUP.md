# Cloudinary Secrets Configuration Guide

## Overview
This guide explains how to configure Cloudinary credentials for the CommunityBoard application in both local development and AWS production environments.

## Local Development Setup

1. **Create .env file** (copy from .env.example):
   ```bash
   cp .env.example .env
   ```

2. **Add your Cloudinary credentials** to `.env`:
   ```
   CLOUD_NAME=dvyax3gbo
   CLOUD_API=955738226168725
   CLOUD_SECRET=TMAJt8Y6WpnBZ0h5Ql4y-1Ni-wg
   ```

3. **Start the application**:
   ```bash
   docker-compose up --build
   ```

## AWS Production Deployment

### Step 1: Update terraform.tfvars

Add Cloudinary credentials to `devops/terraform/terraform.tfvars`:

```hcl
cloudinary_cloud_name  = "dvyax3gbo"
cloudinary_api_key     = "955738226168725"
cloudinary_api_secret  = "TMAJt8Y6WpnBZ0h5Ql4y-1Ni-wg"
```

### Step 2: Deploy Infrastructure

Run the deployment script:

```bash
cd devops
./scripts/deploy-aws.sh production
```

This will:
- Create AWS Secrets Manager secret: `communityboard-cloudinary-config`
- Store credentials as JSON: `{CLOUD_NAME, CLOUD_API, CLOUD_SECRET}`
- Grant ECS task execution role access to the secret
- Inject secrets as environment variables into backend containers

### Step 3: Verify Deployment

1. **Check Secrets Manager**:
   ```bash
   aws secretsmanager get-secret-value \
     --secret-id communityboard-cloudinary-config \
     --region eu-west-1
   ```

2. **Verify ECS Task Definition**:
   ```bash
   aws ecs describe-task-definition \
     --task-definition communityboard-backend \
     --region eu-west-1
   ```

   Look for secrets section with CLOUD_NAME, CLOUD_API, CLOUD_SECRET.

3. **Check Backend Logs**:
   ```bash
   aws logs tail /ecs/communityboard-backend --follow
   ```

## Environment Variables in Backend

The backend ECS task will have these environment variables injected:

- `CLOUD_NAME` - Cloudinary cloud name
- `CLOUD_API` - Cloudinary API key
- `CLOUD_SECRET` - Cloudinary API secret

## Security Notes

- Secrets are stored in AWS Secrets Manager (encrypted at rest)
- ECS tasks retrieve secrets at runtime via IAM role permissions
- Secrets are never logged or exposed in task definitions
- Local .env file should be added to .gitignore

## Troubleshooting

### Issue: Backend container fails to start

**Check**: ECS task execution role has permission to access secrets
```bash
aws iam get-role-policy \
  --role-name communityboard-ecs-task-execution \
  --policy-name communityboard-secrets-access
```

### Issue: Cloudinary upload fails

**Check**: Environment variables are properly injected
```bash
aws ecs execute-command \
  --cluster communityboard-cluster \
  --task <task-id> \
  --container backend \
  --interactive \
  --command "env | grep CLOUD"
```

## References

- [AWS Secrets Manager Documentation](https://docs.aws.amazon.com/secretsmanager/)
- [ECS Secrets Management](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/specifying-sensitive-data-secrets.html)
- [Cloudinary Documentation](https://cloudinary.com/documentation)
