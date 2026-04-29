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

resource "azurerm_log_analytics_workspace" "main" {
  name                = "log-kpnquest"
  resource_group_name = data.azurerm_resource_group.main.name
  location            = "switzerlandnorth"
  sku                 = "PerGB2018"
  retention_in_days   = 30
  tags                = local.tags
}

resource "azurerm_container_registry" "main" {
  name                = "acrkpnquest"
  resource_group_name = data.azurerm_resource_group.main.name
  location            = data.azurerm_resource_group.main.location
  sku                 = "Basic"
  admin_enabled       = true
  tags                = local.tags
}

resource "azurerm_container_app_environment" "main" {
  name                       = "cae-kpnquest"
  resource_group_name        = data.azurerm_resource_group.main.name
  location                   = "switzerlandnorth"
  log_analytics_workspace_id = azurerm_log_analytics_workspace.main.id
  tags                       = local.tags
}

resource "azurerm_container_app" "main" {
  name                         = "ca-kpnquest"
  resource_group_name          = data.azurerm_resource_group.main.name
  container_app_environment_id = azurerm_container_app_environment.main.id
  revision_mode                = "Single"
  tags                         = local.tags

  registry {
    server               = azurerm_container_registry.main.login_server
    username             = azurerm_container_registry.main.admin_username
    password_secret_name = "acr-password"
  }

  secret {
    name  = "acr-password"
    value = azurerm_container_registry.main.admin_password
  }
  secret {
    name  = "db-password"
    value = var.sql_admin_password
  }
  secret {
    name  = "jwt-secret"
    value = var.jwt_secret
  }
  secret {
    name  = "storage-connection-string"
    value = data.azurerm_storage_account.main.primary_connection_string
  }
  secret {
    name  = "openai-key"
    value = azurerm_cognitive_account.openai.primary_access_key
  }

  ingress {
    external_enabled = true
    target_port      = 8080
    traffic_weight {
      latest_revision = true
      percentage      = 100
    }
  }

  template {
    min_replicas = 1
    max_replicas = 1

    container {
      name   = "app"
      image  = "mcr.microsoft.com/azuredocs/containerapps-helloworld:latest"
      cpu    = 0.5
      memory = "1Gi"

      env {
        name  = "DATASOURCES_DEFAULT_URL"
        value = "jdbc:sqlserver://${azurerm_mssql_server.main.fully_qualified_domain_name}:1433;database=kpnquest;encrypt=true;trustServerCertificate=false;loginTimeout=30"
      }
      env {
        name  = "DATASOURCES_DEFAULT_USERNAME"
        value = var.sql_admin_username
      }
      env {
        name        = "DATASOURCES_DEFAULT_PASSWORD"
        secret_name = "db-password"
      }
      env {
        name        = "SA_PASSWORD"
        secret_name = "db-password"
      }
      env {
        name        = "AZURE_STORAGE_CONNECTION_STRING"
        secret_name = "storage-connection-string"
      }
      env {
        name  = "AZURE_OPENAI_ENDPOINT"
        value = azurerm_cognitive_account.openai.endpoint
      }
      env {
        name        = "AZURE_OPENAI_API_KEY"
        secret_name = "openai-key"
      }
      env {
        name        = "JWT_SECRET"
        secret_name = "jwt-secret"
      }
      env {
        name  = "MICRONAUT_ENVIRONMENTS"
        value = "prod"
      }
    }
  }

  lifecycle {
    ignore_changes = [
      template[0].container[0].image,
    ]
  }
}