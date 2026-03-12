output "endpoint" {
  value = aws_db_instance.postgres.endpoint
}

output "credentials_secret_arn" {
  value = aws_secretsmanager_secret.db_credentials.arn
}

output "database_name" {
  value = aws_db_instance.postgres.db_name
}

output "db_instance_id" {
  value = aws_db_instance.postgres.id
}

output "username" {
  value = var.db_username
}

output "replicadb_name" {
  value = "replicadb"
}

output "analyticsdb_name" {
  value = "analyticsdb"
}
