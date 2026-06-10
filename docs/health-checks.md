# PulseHub Local Health Checks

This document gives a quick way to find which part of the local PulseHub backend is down before testing the full message flow.

Each service exposes Spring Boot Actuator health at `/actuator/health`.
Only the `health` endpoint is exposed over HTTP in this step.

## Expected Local Ports

| Component | Port | Check |
| --- | --- | --- |
| `bff-service` | `8080` | `http://localhost:8080/actuator/health` |
| `user-service` | `8081` | `http://localhost:8081/actuator/health` |
| `auth-service` | `8082` | `http://localhost:8082/actuator/health` |
| `message-service` | `8083` | `http://localhost:8083/actuator/health` |
| `bot-service` | `8084` | `http://localhost:8084/actuator/health` |
| PostgreSQL | `5433` | Docker container and database commands |
| RabbitMQ AMQP | `5672` | `Test-NetConnection localhost -Port 5672` |
| RabbitMQ UI | `15672` | `http://localhost:15672` |

## Infrastructure Checks

Check Docker Compose containers:

```powershell
docker compose ps
```

Check that required databases exist:

```powershell
docker compose exec auth-db psql -U pulsehub -l
docker compose exec user-db psql -U pulsehub -l
docker compose exec message-db psql -U pulsehub -l
```

Expected databases:

```text
pulsehub_auth
pulsehub_messages
pulsehub_users
```

Check RabbitMQ:

```powershell
Test-NetConnection localhost -Port 5672
```

Expected:

```text
TcpTestSucceeded : True
```

RabbitMQ Management UI:

```text
http://localhost:15672
```

Login:

```text
guest
guest
```

Expected queue:

```text
pulsehub.message-published
```

## Service Startup Commands

Start each service in a separate terminal:

```powershell
.\mvnw.cmd -pl auth-service spring-boot:run
```

```powershell
.\mvnw.cmd -pl user-service spring-boot:run
```

```powershell
.\mvnw.cmd -pl message-service spring-boot:run
```

```powershell
.\mvnw.cmd -pl bot-service spring-boot:run
```

```powershell
.\mvnw.cmd -pl bff-service spring-boot:run
```

## REST Checks

Check every service with Actuator:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
Invoke-RestMethod http://localhost:8081/actuator/health
Invoke-RestMethod http://localhost:8082/actuator/health
Invoke-RestMethod http://localhost:8083/actuator/health
Invoke-RestMethod http://localhost:8084/actuator/health
```

Expected response:

```json
{
  "status": "UP"
}
```

Then run functional checks through BFF:

Create or login as a test user through BFF:

```powershell
$registerBody = @{
  username = "healthcheck"
  displayName = "Health Check"
  password = "password123"
} | ConvertTo-Json

Invoke-WebRequest -UseBasicParsing `
  -Uri http://localhost:8080/api/auth/register `
  -Method Post `
  -ContentType "application/json; charset=utf-8" `
  -Body ([System.Text.Encoding]::UTF8.GetBytes($registerBody))
```

Login:

```powershell
$loginBody = @{
  username = "healthcheck"
  password = "password123"
} | ConvertTo-Json

$login = Invoke-RestMethod `
  -Uri http://localhost:8080/api/auth/login `
  -Method Post `
  -ContentType "application/json; charset=utf-8" `
  -Body ([System.Text.Encoding]::UTF8.GetBytes($loginBody))
```

Check BFF JWT validation:

```powershell
Invoke-RestMethod `
  -Uri http://localhost:8080/api/me `
  -Headers @{ Authorization = "Bearer $($login.token)" }
```

Expected:

```json
{
  "userId": "uuid",
  "username": "healthcheck"
}
```

Check BFF to user-service:

```powershell
Invoke-RestMethod `
  -Uri http://localhost:8080/api/users `
  -Headers @{ Authorization = "Bearer $($login.token)" }
```

Check BFF to message-service:

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:8080/api/messages?channel=general" `
  -Headers @{ Authorization = "Bearer $($login.token)" }
```

Check message-service directly:

```powershell
Invoke-RestMethod "http://localhost:8083/messages?channel=general"
```

## Bot Check

`bot-service` has no business REST API in this step. Its functional health check is:

1. The process starts without RabbitMQ connection errors.
2. It logs consumed `message-published` events.
3. It posts a PulseBot response when a message contains `/help` or `hej bot`.

Trigger through BFF:

```powershell
$messageBody = @{
  channel = "general"
  content = "hej bot"
} | ConvertTo-Json

Invoke-WebRequest -UseBasicParsing `
  -Uri http://localhost:8080/api/messages `
  -Method Post `
  -ContentType "application/json; charset=utf-8" `
  -Headers @{ Authorization = "Bearer $($login.token)" } `
  -Body ([System.Text.Encoding]::UTF8.GetBytes($messageBody))
```

Expected `bot-service` log:

```text
Received message-published event: eventId=..., messageId=..., username=healthcheck, channel=general
```

Wait one second and verify both messages:

```powershell
Start-Sleep -Seconds 1

Invoke-RestMethod `
  -Uri "http://localhost:8080/api/messages?channel=general" `
  -Headers @{ Authorization = "Bearer $($login.token)" }
```

Expected result contains:

```text
username: healthcheck
content: hej bot

username: PulseBot
content: Hej! Jag \u00e4r PulseBot. Testa att skriva /help.
```

## Failure Guide

| Symptom | Likely cause |
| --- | --- |
| BFF returns `401` | Missing or expired JWT. Login again. |
| BFF returns `502` or connection error | Target service URL is wrong or target service is down. |
| `message-service` returns `503` on `POST /messages` | RabbitMQ is down or unreachable. |
| Message saves but bot does not answer | `bot-service` is down, RabbitMQ queue mismatch, or async delay. |
| `bot-service` repeatedly posts messages | Loop protection is broken. It must ignore `username = PulseBot`. |
| PostgreSQL connection failure | `docker compose up -d` is missing, wrong port, or missing database. |

## Actuator Scope

In this step, the project exposes only:

```text
/actuator/health
```

Other Actuator endpoints are not exposed yet.
