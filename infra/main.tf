locals {
  sql_server_name = "sql-kpnquest"
  tags = {
    project     = "kpn-quest"
    environment = var.environment
  }
}

data "azurerm_resource_group" "main" {
  name = "rg-kpnquest"
}

data "azurerm_storage_account" "main" {
  name                = var.storage_account_name
  resource_group_name = data.azurerm_resource_group.main.name
}

resource "azurerm_storage_container" "photos" {
  name                  = "photos"
  storage_account_name  = data.azurerm_storage_account.main.name
  container_access_type = "private"
}

resource "azurerm_mssql_server" "main" {
  name                         = local.sql_server_name
  resource_group_name          = data.azurerm_resource_group.main.name
  location                     = data.azurerm_resource_group.main.location
  version                      = "12.0"
  administrator_login          = var.sql_admin_username
  administrator_login_password = var.sql_admin_password
  tags                         = local.tags
}

resource "azurerm_mssql_firewall_rule" "azure_services" {
  name             = "AllowAzureServices"
  server_id        = azurerm_mssql_server.main.id
  start_ip_address = "0.0.0.0"
  end_ip_address   = "0.0.0.0"
}

resource "azurerm_mssql_database" "main" {
  name         = "kpnquest"
  server_id    = azurerm_mssql_server.main.id
  collation    = "SQL_Latin1_General_CP1_CI_AS"
  license_type = "LicenseIncluded"
  sku_name     = "Basic"
  max_size_gb  = 2
  tags         = local.tags
}

resource "azurerm_cognitive_account" "openai" {
  name                = "oai-kpnquest"
  resource_group_name = data.azurerm_resource_group.main.name
  location            = var.openai_location
  kind                = "OpenAI"
  sku_name            = "S0"
  tags                = local.tags
}

resource "azurerm_cognitive_deployment" "gpt4o_mini" {
  name                 = "gpt-4o-mini"
  cognitive_account_id = azurerm_cognitive_account.openai.id

  model {
    format  = "OpenAI"
    name    = "gpt-4o-mini"
    version = "2024-07-18"
  }

  sku {
    name     = "GlobalStandard"
    capacity = 1
  }
}

resource "azurerm_service_plan" "main" {
  name                = "asp-kpnquest"
  resource_group_name = data.azurerm_resource_group.main.name
  location            = "switzerlandnorth"
  os_type             = "Linux"
  sku_name            = var.app_service_sku
  tags                = local.tags
}

resource "azurerm_linux_web_app" "main" {
  name                = "app-kpnquest"
  resource_group_name = data.azurerm_resource_group.main.name
  location            = "switzerlandnorth"
  service_plan_id     = azurerm_service_plan.main.id
  tags                = local.tags

  site_config {
    always_on = true

    application_stack {
      java_version        = "21"
      java_server         = "JAVA"
      java_server_version = "21"
    }
  }

  logs {
    application_logs {
      file_system_level = "Information"
    }
    http_logs {
      file_system {
        retention_in_mb   = 35
        retention_in_days = 7
      }
    }
  }

  app_settings = {
    "DATASOURCES_DEFAULT_URL"      = "jdbc:sqlserver://${azurerm_mssql_server.main.fully_qualified_domain_name}:1433;database=kpnquest;encrypt=true;trustServerCertificate=false;loginTimeout=30"
    "DATASOURCES_DEFAULT_USERNAME" = var.sql_admin_username
    "DATASOURCES_DEFAULT_PASSWORD" = var.sql_admin_password
    "SA_PASSWORD"                  = var.sql_admin_password

    "AZURE_STORAGE_CONNECTION_STRING" = data.azurerm_storage_account.main.primary_connection_string

    "AZURE_OPENAI_ENDPOINT" = azurerm_cognitive_account.openai.endpoint
    "AZURE_OPENAI_API_KEY"  = azurerm_cognitive_account.openai.primary_access_key

    "JWT_SECRET" = var.jwt_secret

    "WEBSITES_PORT"                       = "8080"
    "WEBSITES_CONTAINER_START_TIME_LIMIT" = "600"
    "MICRONAUT_ENVIRONMENTS"              = "prod"
  }
}