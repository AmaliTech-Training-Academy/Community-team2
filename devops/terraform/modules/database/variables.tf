variable "project_name" {
  type = string
}

variable "environment" {
  type = string
}

variable "subnet_ids" {
  type = list(string)
}

variable "security_group_id" {
  type = string
}

variable "instance_class" {
  type    = string
  default = "db.t3.micro"
}

variable "db_username" {
  type      = string
  default   = "postgres"
}

variable "db_password" {
  type      = string
  sensitive = true
}
