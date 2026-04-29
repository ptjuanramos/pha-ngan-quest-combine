# Ko Pha Ngan Quest

A mobile-first scavenger hunt web app for Ko Pha Ngan, Thailand. Guides one player through 8 missions over 3–4 days — exploration, challenges, and photo uploads validated by AI.

## Architecture

```mermaid
graph TD
    subgraph CICD ["GitHub Actions"]
        GH_APP[deploy-application.yml<br/>test → build JAR → Docker push → deploy]
        GH_INF[deploy-infrastructure.yml<br/>terraform apply]
    end

    subgraph Azure
        ACR[Container Registry<br/>acrkpnquest]
        CA[Container App ca-kpnquest<br/>Micronaut 4 · Java 21]
        SQL[(Azure SQL Basic tier)]
        BLOB[Blob Storage<br/>photos container]
        OAI[Azure OpenAI<br/>gpt-4o-mini]
    end

    Browser["Browser (mobile)"] -->|HTTPS| CA
    CA -->|serves frontend SPA| Browser
    CA -->|JDBC| SQL
    CA -->|upload / SAS URL| BLOB
    CA -->|photo validation| OAI
    GH_APP -->|push image| ACR
    GH_APP -->|az containerapp update| CA
    ACR -->|pull image| CA
    GH_INF -->|terraform apply| Azure
```

## Tech stack

| Layer | Technology |
|---|---|
| Frontend | React 18 · TypeScript · Tailwind CSS |
| Backend | Micronaut 4.x · Java 21 · Micronaut Data JDBC |
| Database | Azure SQL (MSSQL) · Flyway migrations |
| Storage | Azure Blob Storage |
| AI | Azure OpenAI (gpt-4o-mini) — photo validation |
| Hosting | Azure Container Apps (1 replica, always on) |
| IaC | Terraform |
| CI/CD | GitHub Actions + OIDC (no long-lived secrets) |

## Repository structure

```
├── backend/          # Micronaut Java backend + Dockerfile
├── frontend/         # React SPA (git submodule)
├── infra/            # Terraform (Azure infrastructure)
├── local/            # Docker Compose — local MSSQL only
└── .github/workflows/
    ├── deploy-application.yml    # triggered by backend/** or frontend/** changes
    └── deploy-infrastructure.yml # triggered by infra/** changes
```

## Local development

**Start the database:**
```bash
cd local && docker compose up -d
```

**Start the backend** (create `backend/.env` with `SA_PASSWORD`, `JWT_SECRET`, `AZURE_*` vars first):
```bash
cd backend && ./gradlew runWithVars
```

**Start the frontend:**
```bash
cd frontend && npm run dev
```

## CI/CD

Push to `main` automatically:
- triggers `deploy-application.yml` when `backend/**` or `frontend/**` changes
- triggers `deploy-infrastructure.yml` when `infra/**` changes

Documentation-only changes (`.md` files) do not trigger any pipeline.