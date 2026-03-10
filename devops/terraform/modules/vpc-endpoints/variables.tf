variable "vpc_id" {
  type = string
}

variable "project_name" {
  type = string
}

variable "aws_region" {
  type = string
  default = "us-east-1"
}

variable "private_subnet_ids" {
  type = list(string)
}

variable "route_table_id" {
  type = string
}

variable "vpc_endpoint_security_group_id" {
  type = string
  default = ""
}
