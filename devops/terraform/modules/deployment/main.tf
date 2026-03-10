data "aws_caller_identity" "current" {}

resource "aws_ecr_repository" "backend" {
  name                 = "${var.project_name}-backend"
  image_tag_mutability = "MUTABLE"
  force_delete         = true

  image_scanning_configuration {
    scan_on_push = true
  }
}

resource "aws_ecr_repository" "frontend" {
  name                 = "${var.project_name}-frontend"
  image_tag_mutability = "MUTABLE"
  force_delete         = true

  image_scanning_configuration {
    scan_on_push = true
  }
}

resource "null_resource" "build_and_push_backend" {
  triggers = {
    always_run = timestamp()
  }

  provisioner "local-exec" {
    command = <<-EOT
      aws ecr get-login-password --region ${var.aws_region} | docker login --username AWS --password-stdin ${data.aws_caller_identity.current.account_id}.dkr.ecr.${var.aws_region}.amazonaws.com
      cd ${var.backend_source_path}
      docker build -t ${aws_ecr_repository.backend.repository_url}:latest .
      docker tag ${aws_ecr_repository.backend.repository_url}:latest ${aws_ecr_repository.backend.repository_url}:${var.environment}-${formatdate("YYYYMMDDhhmmss", timestamp())}
      docker push ${aws_ecr_repository.backend.repository_url}:latest
      docker push ${aws_ecr_repository.backend.repository_url}:${var.environment}-${formatdate("YYYYMMDDhhmmss", timestamp())}
    EOT
  }

  depends_on = [aws_ecr_repository.backend]
}

resource "null_resource" "build_and_push_frontend" {
  triggers = {
    always_run = timestamp()
  }

  provisioner "local-exec" {
    command = <<-EOT
      aws ecr get-login-password --region ${var.aws_region} | docker login --username AWS --password-stdin ${data.aws_caller_identity.current.account_id}.dkr.ecr.${var.aws_region}.amazonaws.com
      cd ${var.frontend_source_path}
      docker build -t ${aws_ecr_repository.frontend.repository_url}:latest .
      docker tag ${aws_ecr_repository.frontend.repository_url}:latest ${aws_ecr_repository.frontend.repository_url}:${var.environment}-${formatdate("YYYYMMDDhhmmss", timestamp())}
      docker push ${aws_ecr_repository.frontend.repository_url}:latest
      docker push ${aws_ecr_repository.frontend.repository_url}:${var.environment}-${formatdate("YYYYMMDDhhmmss", timestamp())}
    EOT
  }

  depends_on = [aws_ecr_repository.frontend]
}

resource "null_resource" "update_ecs_services" {
  triggers = {
    backend_image  = null_resource.build_and_push_backend.id
    frontend_image = null_resource.build_and_push_frontend.id
  }

  provisioner "local-exec" {
    command = <<-EOT
      aws ecs update-service --cluster ${var.cluster_name} --service ${var.backend_service_name} --force-new-deployment --region ${var.aws_region}
      aws ecs update-service --cluster ${var.cluster_name} --service ${var.frontend_service_name} --force-new-deployment --region ${var.aws_region}
    EOT
  }

  depends_on = [
    null_resource.build_and_push_backend,
    null_resource.build_and_push_frontend
  ]
}
