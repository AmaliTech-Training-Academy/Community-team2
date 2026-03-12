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

variable "cloudinary_cloud_name" {
  type      = string
  sensitive = true
}

variable "cloudinary_api_key" {
  type      = string
  sensitive = true
}

variable "cloudinary_api_secret" {
  type      = string
  sensitive = true
}
