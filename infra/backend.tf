# ─── Phase 1: Local state (default) ──────────────────────────────────────────
# No backend block = Terraform uses local state (terraform.tfstate).
# This is the starting point. The file is gitignored.

# ─── Phase 2: Migrate to Azure Blob ──────────────────────────────────────────
# After the first `terraform apply` creates the storage account, run:
#   terraform init -migrate-state
# Then uncomment the block below, filling in the storage account name from
# the `storage_account_name` output.
#
# terraform {
#   backend "azurerm" {
#     resource_group_name  = "rg-kpnquest"
#     storage_account_name = "<value from output: storage_account_name>"
#     container_name       = "tfstate"
#     key                  = "kpnquest.tfstate"
#   }
# }