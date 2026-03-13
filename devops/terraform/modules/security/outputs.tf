output "jwt_secret_arn" {
  value = aws_secretsmanager_secret.jwt_secret.arn
}

output "cloudinary_secret_arn" {
  value = aws_secretsmanager_secret.cloudinary_config.arn
}

output "email_secret_arn" {
  value = aws_secretsmanager_secret.email_config.arn
}

output "frontend_urls_secret_arn" {
  value = aws_secretsmanager_secret.frontend_urls.arn
}
