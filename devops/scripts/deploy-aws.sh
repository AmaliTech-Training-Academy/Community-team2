#!/bin/bash
# ============================================================================
# CommunityBoard AWS Deployment Script (Production-Ready)
# ============================================================================
# Full-featured deployment script with validation and planning
# Usage: ./deploy-aws.sh [environment]
# Example: ./deploy-aws.sh production
# ============================================================================

set -e  # Exit on any error

# Configuration
ENVIRONMENT=${1:-development}
TERRAFORM_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../terraform" && pwd)"

echo "🚀 Deploying CommunityBoard to AWS ($ENVIRONMENT)"

cd "$TERRAFORM_DIR"

# Validate terraform.tfvars exists
if [ ! -f "terraform.tfvars" ]; then
    echo "❌ terraform.tfvars not found. Copy terraform.tfvars.example and configure it."
    exit 1
fi

# Initialize Terraform (downloads providers and modules)
echo "📦 Initializing Terraform..."
terraform init

# Create execution plan for review
echo "📋 Planning infrastructure changes..."
terraform plan -out=tfplan

# Apply the planned changes
echo "🏗️  Applying infrastructure changes..."
terraform apply tfplan

echo "✅ Infrastructure deployed successfully!"

# Extract outputs for display
ALB_DNS=$(terraform output -raw alb_dns_name)
ECR_BACKEND=$(terraform output -raw ecr_backend_repository_url)
ECR_FRONTEND=$(terraform output -raw ecr_frontend_repository_url)

# Display deployment summary
echo ""
echo "📝 Deployment Summary:"
echo "  Application URL: http://$ALB_DNS"
echo "  Backend API: http://$ALB_DNS/api"
echo "  Swagger UI: http://$ALB_DNS/swagger-ui.html"
echo ""
echo "🐳 ECR Repositories:"
echo "  Backend: $ECR_BACKEND"
echo "  Frontend: $ECR_FRONTEND"
echo ""
echo "💡 Next steps:"
echo "  1. Push Docker images to ECR repositories"
echo "  2. ECS services will automatically pull and deploy the images"
