# ============================================================================
# VPC Endpoints Module
# ============================================================================
# Private endpoints for AWS services to avoid NAT Gateway costs
# Enables secure communication without internet gateway
# Replaces bastion host for database access via ECS Exec
# ============================================================================

# ECR API endpoint for Docker registry operations
resource "aws_vpc_endpoint" "ecr_api" {
  vpc_id              = var.vpc_id
  service_name        = "com.amazonaws.${var.aws_region}.ecr.api"
  vpc_endpoint_type   = "Interface"  # Interface endpoint with ENI
  subnet_ids          = var.private_subnet_ids
  security_group_ids  = [var.vpc_endpoint_security_group_id]
  private_dns_enabled = true  # Enable private DNS resolution
}

# ECR Docker endpoint for pulling container images
resource "aws_vpc_endpoint" "ecr_dkr" {
  vpc_id              = var.vpc_id
  service_name        = "com.amazonaws.${var.aws_region}.ecr.dkr"
  vpc_endpoint_type   = "Interface"
  subnet_ids          = var.private_subnet_ids
  security_group_ids  = [var.vpc_endpoint_security_group_id]
  private_dns_enabled = true
}

# S3 gateway endpoint for ECR image layers (no hourly charge)
resource "aws_vpc_endpoint" "s3" {
  vpc_id            = var.vpc_id
  service_name      = "com.amazonaws.${var.aws_region}.s3"
  vpc_endpoint_type = "Gateway"  # Gateway endpoint (free)
  route_table_ids   = [var.route_table_id]  # Single route table ID
}

# Secrets Manager endpoint for retrieving database credentials and JWT secrets
resource "aws_vpc_endpoint" "secrets_manager" {
  vpc_id              = var.vpc_id
  service_name        = "com.amazonaws.${var.aws_region}.secretsmanager"
  vpc_endpoint_type   = "Interface"
  subnet_ids          = var.private_subnet_ids
  security_group_ids  = [var.vpc_endpoint_security_group_id]
  private_dns_enabled = true
}

# CloudWatch Logs endpoint for ECS container logs
resource "aws_vpc_endpoint" "logs" {
  vpc_id              = var.vpc_id
  service_name        = "com.amazonaws.${var.aws_region}.logs"
  vpc_endpoint_type   = "Interface"
  subnet_ids          = var.private_subnet_ids
  security_group_ids  = [var.vpc_endpoint_security_group_id]
  private_dns_enabled = true
}
