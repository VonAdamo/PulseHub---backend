3# PulseHub Backend

PulseHub is a distributed chat application built for Laboration 2 Microservices.
This repository contains the backend services as separate Spring Boot projects in one Maven reactor.

## Services

| Service | Port | Purpose |
| --- | --- | --- |
| `bff-service` | `8080` | Frontend entry point. Forwards REST calls to backend services. |
| `auth-service` | `8082` | Local registration, login, password hashing, and JWT generation. |
| `user-service` | `8081` | User profile API. |
| `message-service` | `8083` | Local channel-based chat messages. |

Planned services:

| Service | Status |
| --- | --- |
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
| `pulsehub_messages` | `message-service` |
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

Create `pulsehub_messages` if it does not already exist:

```powershell
docker compose exec postgres createdb -U pulsehub pulsehub_messages
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

```powershell
.\mvnw.cmd -pl message-service spring-boot:run
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

Create a message through BFF:

```powershell
$messageBody = @{
  channel = "general"
  content = "Hej fran frontend!"
} | ConvertTo-Json

Invoke-WebRequest -UseBasicParsing `
  -Uri http://localhost:8080/api/messages `
  -Method Post `
  -ContentType "application/json" `
  -Headers @{ Authorization = "Bearer $($loginResponse.token)" } `
  -Body $messageBody
```

Get messages through BFF:

```powershell
Invoke-RestMethod `
  -Uri http://localhost:8080/api/messages `
  -Headers @{ Authorization = "Bearer $($loginResponse.token)" }
```

Get messages by channel through BFF:

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:8080/api/messages?channel=general" `
  -Headers @{ Authorization = "Bearer $($loginResponse.token)" }
```

## Test Message Service Directly

Message Service can also be tested directly on port `8083`.

Create a message:

```powershell
$body = @{
  senderId = "002a7322-396b-4bf8-9dd6-264779c77fab"
  username = "milla"
  channel = "general"
  content = "Hej fran PulseHub!"
} | ConvertTo-Json

Invoke-WebRequest -UseBasicParsing `
  -Uri http://localhost:8083/messages `
  -Method Post `
  -ContentType "application/json" `
  -Body $body
```

Get all messages:

```powershell
Invoke-RestMethod http://localhost:8083/messages
```

Get messages by channel:

```powershell
Invoke-RestMethod "http://localhost:8083/messages?channel=general"
```

## Environment Variables

| Variable | Default |
| --- | --- |
| `BFF_SERVER_PORT` | `8080` |
| `AUTH_SERVICE_URL` | `http://localhost:8082` |
| `USER_SERVICE_URL` | `http://localhost:8081` |
| `MESSAGE_SERVICE_URL` | `http://localhost:8083` |
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
| `MESSAGE_SERVER_PORT` | `8083` |
| `MESSAGE_DB_URL` | `jdbc:postgresql://localhost:5433/pulsehub_messages` |
| `MESSAGE_DB_USERNAME` | `pulsehub` |
| `MESSAGE_DB_PASSWORD` | `pulsehub` |
| `RABBITMQ_HOST` | `localhost` |
| `RABBITMQ_PORT` | `5672` |
| `RABBITMQ_USERNAME` | `guest` |
| `RABBITMQ_PASSWORD` | `guest` |
| `MESSAGE_EVENTS_EXCHANGE` | `pulsehub.messages` |
| `MESSAGE_PUBLISHED_QUEUE` | `pulsehub.message-published` |
| `MESSAGE_PUBLISHED_ROUTING_KEY` | `message.published` |

`JWT_SECRET` must be the same for `auth-service` and `bff-service`.
`auth-service` signs tokens and `bff-service` validates them.

## Message Events

When `message-service` creates a message through `POST /messages`, it publishes a RabbitMQ event after saving the message.

Default RabbitMQ names:

```text
Exchange: pulsehub.messages
Routing key: message.published
Queue: pulsehub.message-published
Event type: message-published
```

The event contains:

```json
{
  "eventId": "uuid",
  "type": "message-published",
  "messageId": "uuid",
  "senderId": "uuid",
  "username": "milla",
  "channel": "general",
  "content": "Hej",
  "createdAt": "2026-06-09T10:00:00Z"
}
```

## CI

GitHub Actions runs:

```bash
./mvnw -B test
```

The CI workflow starts PostgreSQL, creates `pulsehub_auth`, and runs tests for:

- `auth-service`
- `bff-service`
- `message-service`
- `user-service`
