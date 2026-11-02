#!/usr/bin/env bash
# Starts everything for local use: PostgreSQL (Docker) -> build the UI into the backend -> Spring Boot.
# Usage (Git Bash, Linux, macOS):  bash scripts/run.sh
# Optional overrides live in .env (see .env.example), for example DB_PORT or SERVER_PORT.
set -euo pipefail
cd "$(dirname "$0")/.."

# Export KEY=VALUE lines from .env (Docker Compose reads .env itself).
if [ -f .env ]; then
  set -a
  # shellcheck disable=SC1091
  . ./.env
  set +a
fi

echo "==> Starting PostgreSQL (docker compose)"
docker compose up -d --wait

if [ ! -d frontend/node_modules ]; then
  echo "==> Installing frontend dependencies"
  npm --prefix frontend ci
fi

echo "==> Building the React UI into backend/src/main/resources/static"
npm --prefix frontend run build

echo "==> Starting Spring Boot - open http://localhost:${SERVER_PORT:-8180} when it says 'Started HubApplication'"
cd backend
./mvnw spring-boot:run
