output "app_url" {
  description = "Public URL of the Container App."
  value       = "https://${azurerm_container_app.main.ingress[0].fqdn}"
}

output "sql_server_fqdn" {
  description = "Fully qualified domain name of the SQL Server."
  value       = azurerm_mssql_server.main.fully_qualified_domain_name
}

output "openai_endpoint" {
  description = "Azure OpenAI endpoint ."
  value       = azurerm_cognitive_account.openai.endpoint
}