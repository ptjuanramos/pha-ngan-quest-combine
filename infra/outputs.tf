output "app_url" {
  description = "Public URL of the App Service."
  value       = "https://${azurerm_linux_web_app.main.default_hostname}"
}

output "storage_account_name" {
  description = "Storage account name — needed for Phase 2 Terraform state migration."
  value       = azurerm_storage_account.main.name
}

output "sql_server_fqdn" {
  description = "Fully qualified domain name of the SQL Server."
  value       = azurerm_mssql_server.main.fully_qualified_domain_name
}

output "openai_endpoint" {
  description = "Azure OpenAI endpoint."
  value       = azurerm_cognitive_account.openai.endpoint
}