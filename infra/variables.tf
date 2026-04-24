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

variable "openai_location" {
  description = "Azure region for the OpenAI account. Must support gpt-4o-mini — may differ from the main location. See https://learn.microsoft.com/azure/ai-services/openai/concepts/models#model-summary-table-and-region-availability"
  type        = string
  default     = "eastus"
}