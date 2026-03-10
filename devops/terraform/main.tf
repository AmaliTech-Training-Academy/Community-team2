# ============================================================================
# Terraform Configuration
# ============================================================================
# Main infrastructure configuration for CommunityBoard application
# Provisions AWS resources including VPC, RDS, ECS, ALB, and monitoring
# ============================================================================

terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # Remote state backend - uncomment after running backend-setup.tf
  # backend "s3" {
  #   bucket         = "communityboard-terraform-state"
  #   key            = "infrastructure/terraform.tfstate"
  #   region         = "us-east-1"
  #   encrypt        = true
  #   dynamodb_table = "communityboard-terraform-locks"
  # }
}

provider "aws" {
  region = var.aws_region
}

# Get available AZs in the region for multi-AZ deployment
data "aws_availability_zones" "available" {
  state = "available"
}

# ============================================================================
# Container Registries (ECR)
# ============================================================================
# Docker image repositories for application containers

# Backend API container registry
resource "aws_ecr_repository" "backend" {
  name                 = "${var.project_name}-backend"
  image_tag_mutability = "MUTABLE"  # Allow tag overwrites for development
  force_delete         = true        # Allow deletion even with images
}

# Frontend web application container registry
resource "aws_ecr_repository" "frontend" {
  name                 = "${var.project_name}-frontend"
  image_tag_mutability = "MUTABLE"
  force_delete         = true
}

# Airflow data pipeline container registry
resource "aws_ecr_repository" "airflow" {
  name                 = "${var.project_name}-airflow"
  image_tag_mutability = "MUTABLE"
  force_delete         = true
}

# ============================================================================
# Networking Module
# ============================================================================
# VPC, subnets, route tables, NAT gateway, and security groups

module "networking" {
  source = "./modules/networking"

  project_name        = var.project_name
  vpc_cidr            = "10.0.0.0/16"  # VPC IP range
  availability_zones  = data.aws_availability_zones.available.names
}

# ============================================================================
# Database Module
# ============================================================================
# RDS PostgreSQL primary instance and read replica for analytics

module "database" {
  source = "./modules/database"

  project_name       = var.project_name
  environment        = var.environment
  subnet_ids         = module.networking.private_subnet_ids  # Private subnets for security
  security_group_id  = module.networking.rds_security_group_id
  db_username        = var.db_username
  db_password        = var.db_password
}

# ============================================================================
# Security Module
# ============================================================================
# Secrets Manager for JWT tokens and database credentials

module "security" {
  source = "./modules/security"

  project_name                  = var.project_name
  jwt_secret                    = var.jwt_secret
  ecs_task_execution_role_name  = module.compute.ecs_task_execution_role_name
  db_credentials_secret_arn     = module.database.credentials_secret_arn
}

# ============================================================================
# Compute Module
# ============================================================================
# ECS cluster, task definitions, services, ALB, and auto-scaling

module "compute" {
  source = "./modules/compute"

  project_name              = var.project_name
  aws_region                = var.aws_region
  vpc_id                    = module.networking.vpc_id
  public_subnet_ids         = module.networking.public_subnet_ids
  private_subnet_ids        = module.networking.private_subnet_ids  # Pass private subnets for ECS tasks
  alb_security_group_id     = module.networking.alb_security_group_id
  ecs_security_group_id     = module.networking.ecs_security_group_id
  db_endpoint               = module.database.endpoint              # Single RDS for all databases
  db_credentials_secret_arn = module.database.credentials_secret_arn
  jwt_secret_arn            = module.security.jwt_secret_arn
  backend_image_url         = "${aws_ecr_repository.backend.repository_url}:latest"
  frontend_image_url        = "${aws_ecr_repository.frontend.repository_url}:latest"
  airflow_image_url         = "${aws_ecr_repository.airflow.repository_url}:latest"
}

# ============================================================================
# Monitoring Module
# ============================================================================
# CloudWatch alarms and SNS notifications for system health

module "monitoring" {
  source = "./modules/monitoring"

  project_name           = var.project_name
  alert_email            = var.alert_email
  ecs_cluster_name       = module.compute.ecs_cluster_name
  backend_service_name   = module.compute.backend_service_name
  db_instance_id         = module.database.database_name
}

# ============================================================================
# VPC Endpoints Module
# ============================================================================
# Private endpoints for AWS services (ECR, S3, Secrets Manager, CloudWatch, SSM)
# Allows private subnet containers to access AWS services without NAT Gateway

module "vpc_endpoints" {
  source = "./modules/vpc-endpoints"

  project_name                   = var.project_name
  aws_region                     = var.aws_region
  vpc_id                         = module.networking.vpc_id
  private_subnet_ids             = module.networking.private_subnet_ids
  route_table_id                 = module.networking.private_route_table_id
  vpc_endpoint_security_group_id = module.networking.vpc_endpoint_security_group_id
}

# ============================================================================
# CodeDeploy Module (DISABLED)
# ============================================================================
# Blue/green deployment requires separate blue/green target groups
# Currently using ECS circuit breakers for rollback instead
# Uncomment when blue/green target groups are implemented

# module "codedeploy" {
#   source = "./modules/codedeploy"
#
#   project_name            = var.project_name
#   ecs_cluster_name        = module.compute.ecs_cluster_name
#   backend_service_name    = module.compute.backend_service_name
#   alb_listener_arn        = module.compute.alb_listener_arn
#   backend_blue_tg_name    = module.compute.backend_blue_tg_name
#   backend_green_tg_name   = module.compute.backend_green_tg_name
# }
