# GitHub Secrets Reference

All secrets are configured under **Settings → Secrets and variables → Actions** in the repository.

---

## Azure Identity (OIDC)

Required by both workflows. Set up once via an Azure AD app registration with a federated credential pointing to this repository.

| Secret | Description | Example |
|---|---|---|
| `AZURE_CLIENT_ID` | App registration (service principal) client ID | `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx` |
| `AZURE_TENANT_ID` | Azure Active Directory tenant ID | `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx` |
| `AZURE_SUBSCRIPTION_ID` | Azure subscription ID | `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx` |

**How to obtain:**
```bash
# Create app registration
az ad app create --display-name "kpn-quest-github-actions"

# Note the appId (→ AZURE_CLIENT_ID)
az ad sp create --id <appId>

# Assign Contributor role
az role assignment create \
  --role Contributor \
  --assignee <appId> \
  --scope /subscriptions/<subscriptionId>

# Get tenant ID
az account show --query tenantId -o tsv
```

Then add a federated credential on the app registration in the Azure Portal:
- **Issuer:** `https://token.actions.githubusercontent.com`
- **Subject:** `repo:<org>/<repo>:ref:refs/heads/main`
- **Audience:** `api://AzureADTokenExchange`

---

## Infrastructure (`deploy-infrastructure.yml`)

| Secret | Description | Notes |
|---|---|---|
| `TF_VAR_SQL_ADMIN_PASSWORD` | SQL Server administrator password | Min 8 chars, upper + lower + number + symbol |
| `TF_VAR_JWT_SECRET` | Secret used to sign JWTs | Min 32 chars, random string |

---

## Application (`deploy-application.yml`)

| Secret | Description | Notes |
|---|---|---|
| `TEST_SA_PASSWORD` | SQL SA password used inside the Testcontainers MSSQL container during CI test runs | Only used within Docker's internal network — does not need to match production |
| `AZURE_WEBAPP_NAME` | App Service name to deploy to | Obtained from `terraform output` after first infrastructure apply, e.g. `app-kpnquest-abc123` |

---

## Summary Table

| Secret | Infrastructure workflow | Application workflow |
|---|---|---|
| `AZURE_CLIENT_ID` | ✓ | ✓ |
| `AZURE_TENANT_ID` | ✓ | ✓ |
| `AZURE_SUBSCRIPTION_ID` | ✓ | ✓ |
| `TF_VAR_SQL_ADMIN_PASSWORD` | ✓ | — |
| `TF_VAR_JWT_SECRET` | ✓ | — |
| `TEST_SA_PASSWORD` | — | ✓ |
| `AZURE_WEBAPP_NAME` | — | ✓ |

---

## Setup Order

1. Create Azure AD app registration and federated credential → set `AZURE_CLIENT_ID`, `AZURE_TENANT_ID`, `AZURE_SUBSCRIPTION_ID`
2. Set `TF_VAR_SQL_ADMIN_PASSWORD` and `TF_VAR_JWT_SECRET`
3. Set `TEST_SA_PASSWORD` (any strong password — used only in CI Docker network)
4. Run the infrastructure workflow (`deploy-infrastructure.yml`) manually
5. Copy the `app_url` Terraform output → extract the app name → set `AZURE_WEBAPP_NAME`
6. Push to `main` — the application workflow will now build, test, and deploy