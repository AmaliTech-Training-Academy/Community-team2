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
        aws_secretsmanager_secret.cloudinary_config.arn
      ]
    }]
  })
}

resource "aws_secretsmanager_secret" "cloudinary_config" {
  name                    = "${var.project_name}-cloudinary-config"
  recovery_window_in_days = 7
}

resource "aws_secretsmanager_secret_version" "cloudinary_config" {
  secret_id = aws_secretsmanager_secret.cloudinary_config.id
  secret_string = jsonencode({
    cloud_name = var.cloudinary_cloud_name
    api_key    = var.cloudinary_api_key
    api_secret = var.cloudinary_api_secret
  })
}
