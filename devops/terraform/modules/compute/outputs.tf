output "alb_dns_name" {
  value = aws_lb.main.dns_name
}

output "ecs_cluster_name" {
  value = aws_ecs_cluster.main.name
}

output "backend_service_name" {
  value = aws_ecs_service.backend.name
}

output "frontend_service_name" {
  value = aws_ecs_service.frontend.name
}

output "ecs_task_execution_role_name" {
  value = aws_iam_role.ecs_task_execution.name
}

output "alb_listener_arn" {
  value = aws_lb_listener.http.arn
}

output "backend_blue_tg_name" {
  value = aws_lb_target_group.backend.name
}

output "backend_green_tg_name" {
  value = aws_lb_target_group.backend.name
}
