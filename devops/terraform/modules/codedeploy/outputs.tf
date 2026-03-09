output "codedeploy_app_name" {
  value = aws_codedeploy_app.backend.name
}

output "codedeploy_deployment_group_name" {
  value = aws_codedeploy_deployment_group.backend.deployment_group_name
}

output "codedeploy_role_arn" {
  value = aws_iam_role.codedeploy.arn
}
