variable "project_name" {
  type = string
}

variable "alert_email" {
  type    = string
  default = ""
}

variable "ecs_cluster_name" {
  type = string
}

variable "backend_service_name" {
  type = string
}

variable "db_instance_id" {
  type = string
}
