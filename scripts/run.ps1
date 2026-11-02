# Starts everything for local use: PostgreSQL (Docker) -> build the UI into the backend -> Spring Boot.
# Usage (from anywhere):  powershell -ExecutionPolicy Bypass -File scripts\run.ps1
# Optional overrides live in .env (see .env.example), for example DB_PORT or SERVER_PORT.
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

# Load KEY=VALUE lines from .env into this process (Docker Compose reads .env itself).
if (Test-Path '.env') {
    Get-Content '.env' | ForEach-Object {
        if ($_ -match '^\s*([A-Za-z_][A-Za-z0-9_]*)\s*=\s*(.*?)\s*$') {
            [Environment]::SetEnvironmentVariable($Matches[1], $Matches[2].Trim('"'), 'Process')
        }
    }
}

Write-Host '==> Starting PostgreSQL (docker compose)'
docker compose up -d --wait
if ($LASTEXITCODE -ne 0) { throw 'docker compose failed. Is Docker Desktop running?' }

if (-not (Test-Path 'frontend/node_modules')) {
    Write-Host '==> Installing frontend dependencies'
    npm --prefix frontend ci
    if ($LASTEXITCODE -ne 0) { throw 'npm ci failed' }
}

Write-Host '==> Building the React UI into backend/src/main/resources/static'
npm --prefix frontend run build
if ($LASTEXITCODE -ne 0) { throw 'npm run build failed' }

$port = if ($env:SERVER_PORT) { $env:SERVER_PORT } else { '8180' }
Write-Host "==> Starting Spring Boot - open http://localhost:$port when it says 'Started HubApplication'"
Set-Location (Join-Path $root 'backend')
& .\mvnw.cmd spring-boot:run
