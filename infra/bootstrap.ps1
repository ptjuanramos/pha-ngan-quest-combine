param(
    [string]$Location = "swedencentral"
)

$ErrorActionPreference = "Stop"

$ResourceGroup   = "rg-kpnquest"
$Container       = "tfstate"

$StorageAccount = "stkpnquest"

Write-Host "Bootstrap -- Terraform backend resources"
Write-Host "  Resource group : $ResourceGroup"
Write-Host "  Storage account: $StorageAccount"
Write-Host "  Location       : $Location"
Write-Host ""

# Resource group
$rgExists = az group show --name $ResourceGroup 2>$null
if ($rgExists) {
    Write-Host "v Resource group already exists"
} else {
    Write-Host "-> Creating resource group..."
    az group create --name $ResourceGroup --location $Location --output none
}

# Storage account
$saExists = az storage account show --name $StorageAccount --resource-group $ResourceGroup 2>$null
if ($saExists) {
    Write-Host "v Storage account already exists"
} else {
    Write-Host "-> Creating storage account..."
    az storage account create `
        --name $StorageAccount `
        --resource-group $ResourceGroup `
        --location $Location `
        --sku Standard_LRS `
        --kind StorageV2 `
        --output none
}

# tfstate container
$containerExists = az storage container show `
    --name $Container `
    --account-name $StorageAccount `
    --auth-mode login 2>$null
if ($containerExists) {
    Write-Host "v tfstate container already exists"
} else {
    Write-Host "-> Creating tfstate container..."
    az storage container create `
        --name $Container `
        --account-name $StorageAccount `
        --auth-mode login `
        --output none
}

Write-Host ""
Write-Host "Done. Before running terraform init, update backend.tf:"
Write-Host ""
Write-Host "  storage_account_name = `"$StorageAccount`""
Write-Host ""
Write-Host "Then initialise Terraform:"
Write-Host "  terraform init -var=`"storage_account_name=$StorageAccount`""