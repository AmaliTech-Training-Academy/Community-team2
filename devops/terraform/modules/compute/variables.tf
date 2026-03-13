variable "project_name" {
  type = string
}

variable "aws_region" {
  type = string
}

variable "vpc_id" {
  type = string
}

variable "public_subnet_ids" {
  type = list(string)
}

variable "private_subnet_ids" {
  type = list(string)
}

variable "alb_security_group_id" {
  type = string
}

variable "ecs_security_group_id" {
  type = string
}

variable "backend_task_count" {
  type    = number
  default = 1
}

variable "frontend_task_count" {
  type    = number
  default = 1
}

variable "db_endpoint" {
  type = string
}

variable "db_credentials_secret_arn" {
  type = string
}

variable "jwt_secret_arn" {
  type = string
}

variable "cloudinary_secret_arn" {
  type = string
}

variable "email_secret_arn" {
  type = string
}

variable "frontend_urls_secret_arn" {
  type = string
}

variable "backend_image_url" {
  type = string
}

variable "frontend_image_url" {
  type = string
}

variable "airflow_image_url" {
  type = string
}
