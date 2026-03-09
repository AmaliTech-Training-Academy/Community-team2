# ============================================================================
# Terraform Outputs
# ============================================================================
# Expose key resource identifiers and endpoints for external access
# ============================================================================

# Application Load Balancer DNS for accessing the application
output "alb_dns_name" {
  description = "Application Load Balancer DNS name"
  value       = module.compute.alb_dns_name
}

# ECR repository URLs for pushing Docker images
output "ecr_backend_repository_url" {
  description = "Backend ECR repository URL"
  value       = aws_ecr_repository.backend.repository_url
}

output "ecr_frontend_repository_url" {
  description = "Frontend ECR repository URL"
  value       = aws_ecr_repository.frontend.repository_url
}

output "ecr_airflow_repository_url" {
  description = "Airflow ECR repository URL"
  value       = aws_ecr_repository.airflow.repository_url
}

# Database endpoints for connection strings
output "rds_endpoint" {
  description = "RDS database endpoint"
  value       = module.database.endpoint
}

# ECS cluster name for deployment and management
output "ecs_cluster_name" {
  description = "ECS cluster name"
  value       = module.compute.ecs_cluster_name
}

# Secrets Manager secret name for retrieving database credentials
output "db_credentials_secret_name" {
  description = "Name of the secret containing database credentials"
  value       = "${var.project_name}-db-credentials"
}
