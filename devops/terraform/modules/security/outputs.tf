output "jwt_secret_arn" {
  value = aws_secretsmanager_secret.jwt_secret.arn
}

output "cloudinary_config_arn" {
  value = aws_secretsmanager_secret.cloudinary_config.arn
}
