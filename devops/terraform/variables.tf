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

# Cloudinary configuration
variable "cloudinary_cloud_name" {
  description = "Cloudinary cloud name"
  type        = string
  sensitive   = true
}

variable "cloudinary_api_key" {
  description = "Cloudinary API key"
  type        = string
  sensitive   = true
}

variable "cloudinary_api_secret" {
  description = "Cloudinary API secret"
  type        = string
  sensitive   = true
}

# Email configuration
variable "email_address" {
  description = "Email address for application notifications"
  type        = string
  sensitive   = true
}

variable "email_password" {
  description = "Email password/app password"
  type        = string
  sensitive   = true
}

# Frontend URLs
variable "frontend_url" {
  description = "Frontend application URL"
  type        = string
}

variable "frontend_url_reset" {
  description = "Frontend password reset URL"
  type        = string
}
