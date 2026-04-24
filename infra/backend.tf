terraform {
  backend "azurerm" {
    resource_group_name  = "rg-kpnquest"
    storage_account_name = "stkpnquest22h7as"
    container_name       = "tfstate"
    key                  = "kpnquest.tfstate"
  }
}