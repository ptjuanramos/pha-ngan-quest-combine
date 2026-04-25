terraform {
  backend "azurerm" {
    resource_group_name  = "rg-kpnquest"
    storage_account_name = "stkpnquest"
    container_name       = "tfstate"
    key                  = "kpnquest.tfstate"
  }
}