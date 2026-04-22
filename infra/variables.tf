variable "location" {
  description = "Azure region. Southeast Asia (Singapore) is closest to Ko Pha Ngan."
  type        = string
  default     = "southeastasia"
}

variable "environment" {
  description = "Environment name used for resource tagging."
  type        = string
  default     = "prod"
}

variable "sql_admin_username" {
  description = "SQL Server administrator username."
  type        = string
  default     = "sqladmin"
}

variable "sql_admin_password" {
  description = "SQL Server administrator password. Pass via TF_VAR_sql_admin_password."
  type        = string
  sensitive   = true
}

variable "jwt_secret" {
  description = "Secret key used to sign JWTs. Pass via TF_VAR_jwt_secret."
  type        = string
  sensitive   = true
}

variable "app_service_sku" {
  description = "App Service Plan SKU. B1 is the cheapest always-on tier."
  type        = string
  default     = "B1"
}

variable "vision_sku" {
  description = "Azure AI Vision SKU. F0 = free (5 000 calls/month). S1 = pay-per-use."
  type        = string
  default     = "F0"
}