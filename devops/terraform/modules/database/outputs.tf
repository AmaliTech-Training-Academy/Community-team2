output "endpoint" {
  value = aws_db_instance.postgres.endpoint
}

output "credentials_secret_arn" {
  value = aws_secretsmanager_secret.db_credentials.arn
}

output "database_name" {
  value = aws_db_instance.postgres.db_name
}

output "username" {
  value = var.db_username
}
