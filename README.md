3# PulseHub Backend

PulseHub is a distributed chat application built for Laboration 2 Microservices.
This repository contains the backend services as separate Spring Boot projects in one Maven reactor.

## Services

| Service | Port | Purpose |
| --- | --- | --- |
| `bff-service` | `8080` | Frontend entry point. Forwards REST calls to backend services. |
| `auth-service` | `8082` | Local registration, login, password hashing, and JWT generation. |
| `user-service` | `8081` | User profile API. |

Planned services:

| Service | Status |
| --- | --- |
| `message-service` | Not built yet |
| `bot-service` | Not built yet |

## Requirements

- Java 25
- Docker Desktop
- Maven Wrapper from this repository

Use the wrapper instead of a locally installed Maven:

```powershell
.\mvnw.cmd -B test
```

On Linux/macOS or GitHub Actions:

```bash
./mvnw -B test
```

## Databases

PostgreSQL runs through Docker Compose on host port `5433`.

| Database | Owner |
| --- | --- |
| `pulsehub_auth` | `auth-service` |
| `pulsehub_users` | `user-service` |

Default credentials for local development:

```text
Username: pulsehub
Password: pulsehub
```

Start PostgreSQL:

```powershell
docker compose up -d
```

Create `pulsehub_auth` if it does not already exist:

```powershell
docker compose exec postgres createdb -U pulsehub pulsehub_auth
```

## Run Services

Start each service in a separate terminal:

```powershell
.\mvnw.cmd -pl auth-service spring-boot:run
```

```powershell
.\mvnw.cmd -pl user-service spring-boot:run
```

```powershell
.\mvnw.cmd -pl bff-service spring-boot:run
```

## Test Through BFF

The frontend should call BFF, not the internal services directly.

Register:

```powershell
$body = @{ username = "milla"; displayName = "Milla"; password = "password123" } | ConvertTo-Json

Invoke-WebRequest -UseBasicParsing `
  -Uri http://localhost:8080/api/auth/register `
  -Method Post `
  -ContentType "application/json" `
  -Body $body
```

Login:

```powershell
$body = @{ username = "milla"; password = "password123" } | ConvertTo-Json

Invoke-WebRequest -UseBasicParsing `
  -Uri http://localhost:8080/api/auth/login `
  -Method Post `
  -ContentType "application/json" `
  -Body $body
```

Get users:

```powershell
$loginBody = @{ username = "milla"; password = "password123" } | ConvertTo-Json
$loginResponse = Invoke-RestMethod `
  -Uri http://localhost:8080/api/auth/login `
  -Method Post `
  -ContentType "application/json" `
  -Body $loginBody

Invoke-RestMethod `
  -Uri http://localhost:8080/api/users `
  -Headers @{ Authorization = "Bearer $($loginResponse.token)" }
```

Get current user from JWT claims:

```powershell
Invoke-RestMethod `
  -Uri http://localhost:8080/api/me `
  -Headers @{ Authorization = "Bearer $($loginResponse.token)" }
```

## Environment Variables

| Variable | Default |
| --- | --- |
| `BFF_SERVER_PORT` | `8080` |
| `AUTH_SERVICE_URL` | `http://localhost:8082` |
| `USER_SERVICE_URL` | `http://localhost:8081` |
| `BFF_CORS_ALLOWED_ORIGINS` | `http://localhost:3000` |
| `AUTH_SERVER_PORT` | `8082` |
| `AUTH_DB_URL` | `jdbc:postgresql://localhost:5433/pulsehub_auth` |
| `AUTH_DB_USERNAME` | `pulsehub` |
| `AUTH_DB_PASSWORD` | `pulsehub` |
| `JWT_SECRET` | dev fallback in `auth-service` |
| `JWT_EXPIRATION_SECONDS` | `3600` |
| `SERVER_PORT` | `8081` |
| `DB_URL` | `jdbc:postgresql://localhost:5433/pulsehub_users` |
| `DB_USERNAME` | `pulsehub` |
| `DB_PASSWORD` | `pulsehub` |

`JWT_SECRET` must be the same for `auth-service` and `bff-service`.
`auth-service` signs tokens and `bff-service` validates them.

## CI

GitHub Actions runs:

```bash
./mvnw -B test
```

The CI workflow starts PostgreSQL, creates `pulsehub_auth`, and runs tests for:

- `auth-service`
- `bff-service`
- `user-service`
