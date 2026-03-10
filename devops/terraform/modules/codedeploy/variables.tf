variable "project_name" {
  type = string
}

variable "ecs_cluster_name" {
  type = string
}

variable "backend_service_name" {
  type = string
}

variable "alb_listener_arn" {
  type = string
}

variable "backend_blue_tg_name" {
  type = string
}

variable "backend_green_tg_name" {
  type = string
}
