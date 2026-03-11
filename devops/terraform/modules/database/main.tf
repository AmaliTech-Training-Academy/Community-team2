resource "aws_secretsmanager_secret" "db_credentials" {
  name                    = "${var.project_name}-db-credentials"
  recovery_window_in_days = 7
}

resource "aws_secretsmanager_secret_version" "db_credentials" {
  secret_id = aws_secretsmanager_secret.db_credentials.id
  secret_string = jsonencode({
    username     = var.db_username
    password     = var.db_password
    engine       = "postgres"
    host         = aws_db_instance.postgres.address
    port         = 5432
    dbname       = "communityboard"
    replicadb    = "replicadb"
    analyticsdb  = "analyticsdb"
    jdbc_url     = "jdbc:postgresql://${aws_db_instance.postgres.address}:5432/communityboard"
    replica_url  = "jdbc:postgresql://${aws_db_instance.postgres.address}:5432/replicadb"
    analytics_url = "jdbc:postgresql://${aws_db_instance.postgres.address}:5432/analyticsdb"
  })
}

resource "aws_db_subnet_group" "main" {
  name       = "${var.project_name}-db-subnet"
  subnet_ids = var.subnet_ids

  lifecycle {
    ignore_changes = [subnet_ids]
  }
}

resource "aws_db_instance" "postgres" {
  identifier              = "${var.project_name}-db"
  engine                  = "postgres"
  engine_version          = "15"
  instance_class          = var.instance_class
  allocated_storage       = 20
  storage_encrypted       = true
  db_name                 = "communityboard"
  username                = var.db_username
  password                = var.db_password
  db_subnet_group_name    = aws_db_subnet_group.main.name
  vpc_security_group_ids  = [var.security_group_id]
  skip_final_snapshot     = true
  backup_retention_period = 1
  backup_window          = "03:00-04:00"
  maintenance_window     = "mon:04:00-mon:05:00"
  publicly_accessible     = true
  enabled_cloudwatch_logs_exports = ["postgresql", "upgrade"]
  
  tags = {
    Name        = "${var.project_name}-database"
    Environment = var.environment
  }
}

# Create additional databases using local-exec provisioner
resource "null_resource" "create_databases" {
  provisioner "local-exec" {
    command = <<-EOT
      PGPASSWORD='${var.db_password}' psql -h ${aws_db_instance.postgres.address} -U ${var.db_username} -d postgres -c "CREATE DATABASE replicadb;" || true
      PGPASSWORD='${var.db_password}' psql -h ${aws_db_instance.postgres.address} -U ${var.db_username} -d postgres -c "CREATE DATABASE analyticsdb;" || true
    EOT
  }

  depends_on = [aws_db_instance.postgres]

  triggers = {
    db_instance = aws_db_instance.postgres.id
  }
}

# Single RDS instance hosts all databases:
# - communityboard (backend API)
# - replicadb (replica/read operations)
# - analyticsdb (analytics and reporting)
