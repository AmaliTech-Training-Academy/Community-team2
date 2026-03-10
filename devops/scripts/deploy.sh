#!/bin/bash
# ============================================================================
# CommunityBoard AWS Deployment Script
# ============================================================================
# Simple Terraform deployment wrapper for quick infrastructure provisioning
# Usage: ./deploy.sh [environment]
# Example: ./deploy.sh production
# ============================================================================

set -e  # Exit on any error

# Configuration
ENVIRONMENT=${1:-development}  # Default to development if not specified
AWS_REGION=${AWS_REGION:-us-east-1}  # Default to us-east-1

echo "=== Deploying CommunityBoard to AWS Fargate ==="
echo "Environment: $ENVIRONMENT"
echo "Region: $AWS_REGION"
echo ""

# Navigate to Terraform directory
cd "$(dirname "$0")/../terraform"

# Initialize Terraform (downloads providers, safe to run multiple times)
terraform init

# Apply infrastructure changes and deploy application
terraform apply \
  -var="environment=$ENVIRONMENT" \
  -var="aws_region=$AWS_REGION" \
  -auto-approve  # Skip confirmation prompt

# Display deployment information
echo ""
echo "=== Deployment Complete! ==="
terraform output -json | jq -r '
  "Frontend:   " + .frontend_url.value,
  "Backend:    " + .backend_url.value,
  "Swagger UI: http://" + .alb_dns_name.value + "/swagger-ui.html"
'
