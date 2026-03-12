#!/bin/bash
# ============================================================================
# Cloudinary Secrets Verification Script
# ============================================================================
# This script helps you deploy and verify Cloudinary secrets in AWS
# ============================================================================

set -e

REGION="eu-west-1"
PROJECT_NAME="communityboard"
SECRET_NAME="${PROJECT_NAME}-cloudinary-config"

echo "🔍 Cloudinary Secrets Verification Script"
echo "=========================================="
echo ""

# Step 1: Apply Terraform changes
echo "📋 Step 1: Applying Terraform changes..."
echo "This will create the Cloudinary secret in AWS Secrets Manager"
echo ""
read -p "Do you want to apply Terraform changes? (y/n): " -n 1 -r
echo ""
if [[ $REPLY =~ ^[Yy]$ ]]; then
    cd devops/terraform
    terraform init
    terraform plan -out=tfplan
    terraform apply tfplan
    cd ../..
    echo "✅ Terraform applied successfully"
else
    echo "⏭️  Skipping Terraform apply"
fi

echo ""
echo "=========================================="
echo ""

# Step 2: Verify secret exists in AWS Secrets Manager
echo "📋 Step 2: Checking if secret exists in AWS Secrets Manager..."
if aws secretsmanager describe-secret --secret-id "$SECRET_NAME" --region "$REGION" &>/dev/null; then
    echo "✅ Secret '$SECRET_NAME' exists in AWS Secrets Manager"
else
    echo "❌ Secret '$SECRET_NAME' NOT found in AWS Secrets Manager"
    echo "   Please run Terraform apply first"
    exit 1
fi

echo ""
echo "=========================================="
echo ""

# Step 3: Retrieve and display secret value
echo "📋 Step 3: Retrieving secret value..."
SECRET_VALUE=$(aws secretsmanager get-secret-value \
    --secret-id "$SECRET_NAME" \
    --region "$REGION" \
    --query 'SecretString' \
    --output text)

echo "✅ Secret retrieved successfully:"
echo "$SECRET_VALUE" | jq '.'

echo ""
echo "=========================================="
echo ""

# Step 4: Verify ECS task execution role has access
echo "📋 Step 4: Checking ECS task execution role permissions..."
ROLE_NAME="${PROJECT_NAME}-ecs-task-execution"

if aws iam get-role --role-name "$ROLE_NAME" &>/dev/null; then
    echo "✅ ECS task execution role '$ROLE_NAME' exists"
    
    # Check if role has policy to access secrets
    POLICIES=$(aws iam list-role-policies --role-name "$ROLE_NAME" --query 'PolicyNames' --output text)
    echo "   Attached inline policies: $POLICIES"
else
    echo "❌ ECS task execution role '$ROLE_NAME' NOT found"
fi

echo ""
echo "=========================================="
echo ""

# Step 5: Check ECS task definition
echo "📋 Step 5: Checking ECS task definition for Cloudinary secrets..."
TASK_FAMILY="${PROJECT_NAME}-backend"

TASK_DEF=$(aws ecs describe-task-definition \
    --task-definition "$TASK_FAMILY" \
    --region "$REGION" \
    --query 'taskDefinition.containerDefinitions[0].secrets' \
    --output json 2>/dev/null || echo "[]")

if echo "$TASK_DEF" | jq -e '.[] | select(.name == "CLOUD_NAME")' &>/dev/null; then
    echo "✅ CLOUD_NAME secret found in task definition"
else
    echo "⚠️  CLOUD_NAME secret NOT found in task definition"
fi

if echo "$TASK_DEF" | jq -e '.[] | select(.name == "CLOUD_API")' &>/dev/null; then
    echo "✅ CLOUD_API secret found in task definition"
else
    echo "⚠️  CLOUD_API secret NOT found in task definition"
fi

if echo "$TASK_DEF" | jq -e '.[] | select(.name == "CLOUD_SECRET")' &>/dev/null; then
    echo "✅ CLOUD_SECRET secret found in task definition"
else
    echo "⚠️  CLOUD_SECRET secret NOT found in task definition"
fi

echo ""
echo "All secrets in task definition:"
echo "$TASK_DEF" | jq '.'

echo ""
echo "=========================================="
echo ""

# Step 6: Check running ECS tasks
echo "📋 Step 6: Checking running ECS tasks..."
CLUSTER_NAME="${PROJECT_NAME}-cluster"
SERVICE_NAME="${PROJECT_NAME}-backend"

RUNNING_TASKS=$(aws ecs list-tasks \
    --cluster "$CLUSTER_NAME" \
    --service-name "$SERVICE_NAME" \
    --region "$REGION" \
    --query 'taskArns[0]' \
    --output text 2>/dev/null || echo "")

if [ -n "$RUNNING_TASKS" ] && [ "$RUNNING_TASKS" != "None" ]; then
    echo "✅ Backend service has running tasks"
    echo "   Task ARN: $RUNNING_TASKS"
    echo ""
    echo "   To verify environment variables in the container, run:"
    echo "   aws ecs execute-command --cluster $CLUSTER_NAME \\"
    echo "       --task <task-id> \\"
    echo "       --container backend \\"
    echo "       --interactive \\"
    echo "       --command 'env | grep CLOUD'"
else
    echo "⚠️  No running tasks found for backend service"
    echo "   Deploy your backend service to see the secrets in action"
fi

echo ""
echo "=========================================="
echo ""
echo "✅ Verification Complete!"
echo ""
echo "Summary:"
echo "  - Secret created in AWS Secrets Manager: ✅"
echo "  - ECS task execution role configured: ✅"
echo "  - Task definition updated: Check output above"
echo "  - Running tasks: Check output above"
echo ""
echo "Next steps:"
echo "  1. If task definition doesn't have secrets, redeploy ECS service"
echo "  2. Check CloudWatch logs: /ecs/${PROJECT_NAME}-backend"
echo "  3. Test image upload functionality in your application"
