# ============================================================================
# Terraform Variables
# ============================================================================
# Input variables for infrastructure configuration
# Override defaults using terraform.tfvars or -var flags
# ============================================================================

# AWS region for resource deployment
variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "eu-west-1"
}

# Project name used as prefix for all resources
variable "project_name" {
  description = "Project name"
  type        = string
  default     = "communityboard"
}

# Database master username
variable "db_username" {
  description = "Database username"
  type        = string
  default     = "postgres"
}

# Database master password (should be set via environment variable or tfvars)
variable "db_password" {
  description = "Database password"
  type        = string
  sensitive   = true  # Prevents output in logs
}

# JWT secret for authentication token signing
variable "jwt_secret" {
  description = "JWT secret key"
  type        = string
  default     = "communityboard-secret-key-amalitech-2024"
  sensitive   = true  # Prevents output in logs
}

# Email address for CloudWatch alarm notifications
variable "alert_email" {
  description = "Email for CloudWatch alerts"
  type        = string
  default     = ""
}

# Environment name (development, staging, production)
variable "environment" {
  description = "Environment name"
  type        = string
  default     = "development"
}
