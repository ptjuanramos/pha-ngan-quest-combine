# Storage account and SQL Server names must be globally unique.
resource "random_string" "suffix" {
  length  = 6
  special = false
  upper   = false
}

locals {
  storage_account_name = "stkpnquest${random_string.suffix.result}"
  sql_server_name      = "sql-kpnquest-${random_string.suffix.result}"
  tags = {
    project     = "kpn-quest"
    environment = var.environment
  }
}

resource "azurerm_resource_group" "main" {
  name     = "rg-kpnquest"
  location = var.location
  tags     = local.tags
}

resource "azurerm_storage_account" "main" {
  name                     = local.storage_account_name
  resource_group_name      = azurerm_resource_group.main.name
  location                 = azurerm_resource_group.main.location
  account_tier             = "Standard"
  account_replication_type = "LRS" # cheapest redundancy
  account_kind             = "StorageV2"
  access_tier              = "Hot"
  tags                     = local.tags
}

resource "azurerm_storage_container" "photos" {
  name                  = "photos"
  storage_account_name  = azurerm_storage_account.main.name
  container_access_type = "private"
}

resource "azurerm_storage_container" "tfstate" {
  name                  = "tfstate"
  storage_account_name  = azurerm_storage_account.main.name
  container_access_type = "private"
}

resource "azurerm_mssql_server" "main" {
  name                         = local.sql_server_name
  resource_group_name          = azurerm_resource_group.main.name
  location                     = azurerm_resource_group.main.location
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
  sku_name     = "Basic" # 5 DTUs, 2 GB — cheapest tier (~$5/month)
  max_size_gb  = 2
  tags         = local.tags
}

resource "azurerm_cognitive_account" "vision" {
  name                = "cv-kpnquest-${random_string.suffix.result}"
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location
  kind                = "ComputerVision"
  sku_name            = var.vision_sku # F0 = free / S1 = pay-per-use
  tags                = local.tags
}

resource "azurerm_service_plan" "main" {
  name                = "asp-kpnquest"
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location
  os_type             = "Linux"
  sku_name            = var.app_service_sku # B1 = ~$13/month
  tags                = local.tags
}

resource "azurerm_linux_web_app" "main" {
  name                = "app-kpnquest-${random_string.suffix.result}"
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location
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

  app_settings = {
    "DATASOURCES_DEFAULT_URL"      = "jdbc:sqlserver://${azurerm_mssql_server.main.fully_qualified_domain_name}:1433;database=kpnquest;encrypt=true;trustServerCertificate=false;loginTimeout=30"
    "DATASOURCES_DEFAULT_USERNAME" = var.sql_admin_username
    "DATASOURCES_DEFAULT_PASSWORD" = var.sql_admin_password
    "SA_PASSWORD"                  = var.sql_admin_password

    # Azure Storage
    "AZURE_STORAGE_CONNECTION_STRING" = azurerm_storage_account.main.primary_connection_string
    "AZURE_STORAGE_CORS_ORIGIN"       = "https://${azurerm_linux_web_app.main.default_hostname}"

    # Azure AI Vision
    "AZURE_VISION_ENDPOINT" = azurerm_cognitive_account.vision.endpoint
    "AZURE_VISION_API_KEY"  = azurerm_cognitive_account.vision.primary_access_key

    # JWT
    "JWT_SECRET" = var.jwt_secret

    # Tell App Service which port Micronaut's Netty listens on
    "WEBSITES_PORT" = "8081"

    # Activate Micronaut prod environment
    "MICRONAUT_ENVIRONMENTS" = "prod"
  }
}