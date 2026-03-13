terraform {
  required_providers {
    random = {
      source  = "hashicorp/random"
      version = "~> 3.0"
    }
  }
}

# Generate random JWT secret
resource "random_password" "jwt_secret" {
  length  = 64
  special = true
}

resource "aws_secretsmanager_secret" "jwt_secret" {
  name                    = "${var.project_name}-jwt-secret"
  recovery_window_in_days = 7
}

resource "aws_secretsmanager_secret_version" "jwt_secret" {
  secret_id     = aws_secretsmanager_secret.jwt_secret.id
  secret_string = random_password.jwt_secret.result
}

# Cloudinary configuration secret
resource "aws_secretsmanager_secret" "cloudinary_config" {
  name                    = "${var.project_name}-cloudinary-config"
  recovery_window_in_days = 7
}

resource "aws_secretsmanager_secret_version" "cloudinary_config" {
  secret_id = aws_secretsmanager_secret.cloudinary_config.id
  secret_string = jsonencode({
    CLOUD_NAME   = var.cloudinary_cloud_name
    CLOUD_API    = var.cloudinary_api_key
    CLOUD_SECRET = var.cloudinary_api_secret
  })
}

# Email configuration secret
resource "aws_secretsmanager_secret" "email_config" {
  name                    = "${var.project_name}-email-config"
  recovery_window_in_days = 7
}

resource "aws_secretsmanager_secret_version" "email_config" {
  secret_id = aws_secretsmanager_secret.email_config.id
  secret_string = jsonencode({
    EMAIL  = var.email_address
    EMPASS = var.email_password
  })
}

# Frontend URLs secret
resource "aws_secretsmanager_secret" "frontend_urls" {
  name                    = "${var.project_name}-frontend-urls"
  recovery_window_in_days = 7
}

resource "aws_secretsmanager_secret_version" "frontend_urls" {
  secret_id = aws_secretsmanager_secret.frontend_urls.id
  secret_string = jsonencode({
    FRONTEND_URL       = var.frontend_url
    FRONTEND_URL_RESET = var.frontend_url_reset
  })
}

resource "aws_iam_role_policy" "ecs_secrets_access" {
  role = var.ecs_task_execution_role_name
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Action = [
        "secretsmanager:GetSecretValue"
      ]
      Resource = [
        var.db_credentials_secret_arn,
        aws_secretsmanager_secret.jwt_secret.arn,
        aws_secretsmanager_secret.cloudinary_config.arn,
        aws_secretsmanager_secret.email_config.arn,
        aws_secretsmanager_secret.frontend_urls.arn
      ]
    }]
  })
}
