## Project overview

A mobile-first web app for an adventure scavenger hunt in Ko Pha Ngan, Thailand.
The app is designed for one player and should guide them through 8 missions over 3–4 days, combining exploration, fun challenges, and two spicy/flirty missions at the end.

## Repository Structure

- `frontend/` — React app (git submodule)
- `backend/` — Micronaut 4 Java backend
- `infra/` — Terraform infrastructure (Azure)
- `local/` — Docker Compose for local development (MSSQL only)
- `.github/workflows/` — CI/CD pipelines

## Tech stack (`frontend/`)
- React JS version 18
- Typescript (strict mode)

Do NOT introduce:
- styled-components or CSS-in-JS


## Commands (run from `backend/`)

```bash
./gradlew runWithVars    # Start backend (port 8080, reads .env for secrets)
```

> **Local database:** start MSSQL first with `docker compose up -d` from `local/`.
>
> **Windows first run:** create `~/.testcontainers.properties` with `testcontainers.reuse.enable=true`
> so the MSSQL container is reused between test runs instead of restarting each time.

## Commands (run from `frontend/`)

```bash
npm run dev        # Start dev server at http://localhost:8080
```

## Backend architecture

**Stack:** Micronaut 4.x · Java 21 · Micronaut Data JDBC · MSSQL · Gradle (Groovy DSL)

### Package layout — vertical feature slices

`com.kpnquest` root, one package per use case. Each slice owns its controller, service, repository, domain record, and DTOs. Cross-cutting code lives in `shared/`.

| Slice | Endpoint |
|---|---|
| `identifyplayer` | `POST /api/v1/players/identify` — create/find player by device token, return JWT |
| `loadmissions` | `GET /api/v1/missions` — return all 8 missions ordered by id |
| `completemission` | `POST /api/v1/missions/{id}/complete` — record completion, 409 if already done |
| `uploadphoto` | `POST /api/v1/missions/{id}/photos` — upsert base64 photo |
| `syncgamestate` | `GET/PUT /api/v1/players/{id}/state` — sync localStorage state to DB |

### Database

Flyway migrations live in `src/main/resources/db/migration/`. Naming: `V{n}__{description}.sql`. All user-facing text columns use `NVARCHAR`; timestamps use `DATETIME2`. Domain entities are Java records annotated with `@MappedEntity`.

Flyway migrations in `application.yml` (production). For local development it can be re-enabled in `application-dev.yml`.

### Azure integrations

- **Blob Storage** (`shared/storage/BlobStorageService`): client initialized lazily on first use
- **OpenAI** (`shared/ai/AiPhotoValidationService`): client initialized lazily on first use

## Deployment

### Infrastructure (Terraform — `infra/`)

All Azure resources are managed by Terraform.

Key resources:
- **Azure Container Apps** (`ca-kpnquest`) — runs the backend Docker image; 1 replica always on
- **Azure Container Registry** (`acrkpnquest`) — stores Docker images
- **Azure SQL** (`sql-kpnquest`) — Basic tier MSSQL database
- **Azure Blob Storage** — photos container (uses existing storage account)
- **Azure OpenAI** (`oai-kpnquest`) — `gpt-4o-mini` deployment in Switzerland North

### Application (`deploy-application.yml`)

Triggered on push to `main` when `backend/**` or `frontend/**` changes:
1. **Test** — runs integration tests against a Testcontainers MSSQL instance
2. **Build** — `./gradlew shadowJar` produces `build/libs/app.jar` (fat JAR with bundled frontend)
3. **Deploy** — builds Docker image, pushes to ACR, updates the Container App to the new image

The app URL is the Container App's FQDN — find it via:
```bash
az containerapp show --name ca-kpnquest --resource-group rg-kpnquest \
  --query "properties.configuration.ingress.fqdn" -o tsv
```

## Frontend architecture

The app is a **mobile-first scavenger hunt** for Ko Pha Ngan. It is a pure client-side SPA — no backend, no API calls.

### State management

All game state lives in `src/pages/Index.tsx` and persists to `localStorage` under the key `kpn-quest`. Photos are stored individually as `kpn-quest-photo-{id}` (compressed JPEG via canvas, max 800px) to work around `localStorage` quota limits.

`GameState` shape:
```ts
{ started: boolean; photos: Record<number, string>; completedCount: number }
```

### Game flow

1. `WelcomeScreen` — shown until `state.started = true`
2. `Index.tsx` renders each mission by status: `CompletedMission`, `ActiveMission`, or `LockedMission`
3. Uploading a photo triggers `SignatureMoment` (a 4-second fullscreen animation overlay)
4. After the animation, `completedCount` increments and the page auto-scrolls to the next mission
5. When all 8 missions are done, `QuestComplete` renders a photo gallery

### Mission data

`src/data/missions.ts` is the single source of truth. Each `Mission` has:
- `id` (1–8), `title`, `clue`, `locationHint`, `challenge`
- `isSpicy: boolean` — missions 7 and 8 are marked spicy; this changes the color scheme to `accent` (warm red/orange) instead of `primary`

### Styling conventions

- **Fonts**: `font-heading` (Krub) for titles/clues; `font-body` (Sarabun) for body text
- **Colors**: regular missions use `primary`; spicy missions (`isSpicy`) use `accent`
- **Background**: `.paper-texture` class applied to the outermost wrapper
- CSS variables drive all colors (defined in `src/index.css`); Tailwind tokens (`primary`, `accent`, `muted`, etc.) map to those variables
- Path alias `@/` resolves to `src/`

### Testing

Unit tests use Vitest + jsdom + `@testing-library/react`. Test files live alongside source in `src/**/*.test.{ts,tsx}`. The setup file is `src/test/setup.ts`.

Playwright is configured (`playwright.config.ts`) via `lovable-agent-playwright-config` for Lovable's CI environment — not intended for local E2E runs.