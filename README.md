# Productivity Hub

A proof of concept that shows how a **Spring Boot + React + PostgreSQL** application is enhanced step by step
through **Git branches and merges**. Each phase adds new features, new database tables and new UI, and reaches the
`main` branch through a merge.

**Status: Phase 1 — 3 features** (Tasks, Notes, Bookmarks). Version `1.0.0`.

Everything runs on **one URL**: Spring Boot serves the REST API (`/api/**`) and the compiled React app on the same
port — **http://localhost:8180**.

## Features

| # | Feature | Added in | Tables | API |
|---|---|---|---|---|
| 1 | Tasks | Phase 1 | `tasks` | `/api/tasks` |
| 2 | Notes | Phase 1 | `notes` | `/api/notes` |
| 3 | Bookmarks | Phase 1 | `bookmarks` | `/api/bookmarks` |

The home page shows one card per feature module found in the build; `GET /api/features` lists the modules present in
the backend. If the two disagree (for example a half-merged phase) the home page shows a warning banner.

## Prerequisites

| Tool | Version |
|---|---|
| JDK | 17 or newer (built and tested with 17) |
| Node.js | current LTS (built and tested with 24) |
| Docker Desktop | running (PostgreSQL and the tests use it) |
| Git | 2.38 or newer recommended |

Maven is **not** required — use the included Maven Wrapper.

## Quick start

PowerShell (Windows):
```powershell
powershell -ExecutionPolicy Bypass -File scripts\run.ps1
```
Git Bash / Linux / macOS:
```bash
bash scripts/run.sh
```
The script starts PostgreSQL (`docker compose up -d --wait`), builds the UI into `backend/src/main/resources/static`
and starts Spring Boot. When it prints `Started HubApplication`, open **http://localhost:8180**.

Verify a running instance (expects the number of features of your phase):
```bash
node scripts/smoke.mjs --expect 3
```

### Manual steps
```powershell
docker compose up -d --wait
npm --prefix frontend ci
npm --prefix frontend run build
cd backend
.\mvnw.cmd spring-boot:run      # Git Bash: ./mvnw spring-boot:run
```
Restart the backend after every `npm run build` so the new static files are served.

### Development mode with hot reload
Run the backend as above, then in a second terminal `npm --prefix frontend run dev` and open
http://localhost:5173 (Vite proxies `/api` to the backend on port 8180). The acceptance URL stays http://localhost:8180.

## Configuration

Everything has a local default, so no setup is needed. To override, copy `.env.example` to `.env` (git-ignored):

| Variable | Default | Used by |
|---|---|---|
| `SERVER_PORT` | `8180` | Spring Boot |
| `DB_HOST` / `DB_PORT` | `localhost` / `5432` | Spring Boot, Docker Compose (host port) |
| `DB_NAME` / `DB_USER` / `DB_PASSWORD` | `hubdb` / `hub` / `hub_local_pw` | Spring Boot, Docker Compose |

The default password is a local-only development value. Never commit real secrets.

### Option B: use an already-installed local PostgreSQL
Skip Docker and create the user and database yourself (as a superuser, for example `psql -U postgres`):
```sql
CREATE USER hub WITH PASSWORD 'hub_local_pw';
CREATE DATABASE hubdb OWNER hub;
```
Then start only the app (`cd backend` and `.\mvnw.cmd spring-boot:run`). Flyway creates the tables on first start.
Note: the automated tests still use Docker (Testcontainers).

## Tests

```powershell
cd backend
.\mvnw.cmd -B verify            # API tests run against a real PostgreSQL started by Testcontainers (needs Docker)
cd ..
npm --prefix frontend test      # Vitest + React Testing Library
```
Backend tests are skipped (not failed) when Docker is not available.

## Database

Flyway owns the schema (`backend/src/main/resources/db/migration`, named `V<phase>_<n>__<what>.sql`). Hibernate only
validates it (`ddl-auto=validate`). Open a SQL shell:
```bash
docker compose exec postgres psql -U hub -d hubdb
```
Reset the development database (deletes all data): `docker compose down -v`.

## Troubleshooting

- **`docker compose` fails / cannot connect to the Docker API** — start Docker Desktop and wait until it is running.
- **Port already in use** — `8180` (app), `5432` (PostgreSQL), `5173` (Vite). Override `SERVER_PORT` / `DB_PORT` in `.env`
  (for example `DB_PORT=55432` when a local PostgreSQL service already uses 5432; the run scripts load `.env`).
- **`FATAL: invalid value for parameter "TimeZone": "Asia/Calcutta"`** — the JDBC driver sends your JVM's time zone and
  PostgreSQL rejects legacy zone names that the image does not contain. This project therefore uses
  `postgres:17-bookworm` (Compose and tests), which keeps them. If you use your own PostgreSQL and see this, start the
  JVM with a current zone name, for example `JAVA_TOOL_OPTIONS=-Duser.timezone=Asia/Kolkata`.
- **`Detected applied migration not resolved locally` (Flyway)** — the database is newer than the code you checked out
  (for example after switching to an older tag). Run `docker compose down -v` and start again.
- **Checksum mismatch (Flyway)** — an applied migration was edited. Never edit migrations; add a new one.
- **PowerShell blocks the script** — run it with `powershell -ExecutionPolicy Bypass -File scripts\run.ps1`.
- **UI shows old content** — run `npm --prefix frontend run build` and restart the backend.

## How to add a feature (the merge-friendly recipe)

Features are discovered automatically, so a new feature is *only new files* — no shared file is edited:

1. **Database:** add `backend/src/main/resources/db/migration/V<phase>_<n>__create_<feature>.sql` (new tables only).
2. **Backend:** add a package `com.poc.hub.features.<key>` with entity, repository, service, controller, DTO records
   and one `@Component <Key>Feature implements FeatureDescriptor` (key, name, description, route, apiPath, phase,
   order, itemCount). It then appears in `GET /api/features` by itself.
3. **Frontend:** add `frontend/src/features/<key>/` with `index.js` (default-exports `{ key, name, description,
   route, phase, order, component }`), the page component, `api.js` and a test. `core/registry.js` finds it with
   `import.meta.glob`, so the navigation, routes and home card appear by themselves.
4. **Tests:** one API test per feature plus a `Phase<N>FeaturesTest`; one page test per feature.
5. Add a row to the Features table above and bump the versions in `backend/pom.xml` and `frontend/package.json`.

Rules: never edit or rename a migration of an earlier phase (Flyway checksum); later phases only add tables; use
`V<phase>_9__seed_demo_data.sql` for demo data.

## Project layout

```text
backend/    Spring Boot (Java, Maven Wrapper), Flyway migrations, tests
frontend/   React + Vite (JavaScript), Vitest tests
scripts/    run.ps1, run.sh, smoke.mjs
docs/git/   Git commands for each phase
docker-compose.yml   PostgreSQL 17 for local development
```

## Git workflow

Phase 1 is committed to `main`; Phases 2 and 3 are built on feature branches and merged with `--no-ff`. The exact
commands for each phase are in [docs/git/](docs/git/) — start with [PHASE-1-GIT.md](docs/git/PHASE-1-GIT.md).
