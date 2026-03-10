variable "project_name" {
  type = string
}

variable "jwt_secret" {
  type      = string
  sensitive = true
}

variable "ecs_task_execution_role_name" {
  type = string
}

variable "db_credentials_secret_arn" {
  type = string
}
