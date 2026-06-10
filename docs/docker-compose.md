# PulseHub Docker Compose

This document describes how to run the full PulseHub backend environment with Docker Compose.

## Services

Docker Compose starts:

| Compose service | Host port | Internal port |
| --- | --- | --- |
| `bff-service` | `8080` | `8080` |
| `user-service` | `8081` | `8081` |
| `auth-service` | `8082` | `8082` |
| `message-service` | `8083` | `8083` |
| `bot-service` | `8084` | `8084` |
| `rabbitmq` | `5672`, `15672` | `5672`, `15672` |
| `auth-db` | `5434` | `5432` |
| `user-db` | `5433` | `5432` |
| `message-db` | `5435` | `5432` |

## Internal Service URLs

Inside the Docker network, services must use Compose service names:

```text
auth-service -> auth-db:5432
user-service -> user-db:5432
message-service -> message-db:5432
message-service -> rabbitmq:5672
bff-service -> http://auth-service:8082
bff-service -> http://user-service:8081
bff-service -> http://message-service:8083
bot-service -> http://message-service:8083
bot-service -> rabbitmq:5672
```

Do not use `localhost` inside containers for another container.

## Start

From the repository root:

```powershell
docker compose up --build
```

Detached mode:

```powershell
docker compose up --build -d
```

Watch logs:

```powershell
docker compose logs -f
```

Watch one service:

```powershell
docker compose logs -f bot-service
```

## Health Checks

After startup, check:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
Invoke-RestMethod http://localhost:8081/actuator/health
Invoke-RestMethod http://localhost:8082/actuator/health
Invoke-RestMethod http://localhost:8083/actuator/health
Invoke-RestMethod http://localhost:8084/actuator/health
```

Expected:

```json
{
  "status": "UP"
}
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

## Test Full Flow

Register through BFF:

```powershell
$registerBody = @{
  username = "dockeruser"
  displayName = "Docker User"
  password = "password123"
} | ConvertTo-Json

Invoke-WebRequest -UseBasicParsing `
  -Uri http://localhost:8080/api/auth/register `
  -Method Post `
  -ContentType "application/json; charset=utf-8" `
  -Body ([System.Text.Encoding]::UTF8.GetBytes($registerBody))
```

Login through BFF:

```powershell
$loginBody = @{
  username = "dockeruser"
  password = "password123"
} | ConvertTo-Json

$login = Invoke-RestMethod `
  -Uri http://localhost:8080/api/auth/login `
  -Method Post `
  -ContentType "application/json; charset=utf-8" `
  -Body ([System.Text.Encoding]::UTF8.GetBytes($loginBody))
```

Post a message that triggers PulseBot:

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

Wait briefly because bot handling is asynchronous:

```powershell
Start-Sleep -Seconds 1
```

Read messages:

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:8080/api/messages?channel=general" `
  -Headers @{ Authorization = "Bearer $($login.token)" }
```

Expected result contains:

```text
username: dockeruser
content: hej bot

username: PulseBot
content: Hej! Jag \u00e4r PulseBot. Testa att skriva /help.
```

## Stop

Stop containers but keep database volumes:

```powershell
docker compose down
```

Stop and remove database volumes:

```powershell
docker compose down -v
```

Remove built images too:

```powershell
docker compose down -v --rmi local
```

## Common Errors

Port already in use:

```text
Ports 8080-8084, 5433-5435, 5672, or 15672 are already used by local services.
```

Stop the local process or change the Compose port mapping.

Database not ready:

```text
Connection refused to auth-db, user-db, or message-db
```

Check:

```powershell
docker compose ps
docker compose logs auth-db
docker compose logs user-db
docker compose logs message-db
```

RabbitMQ not ready:

```text
message-service returns 503 when creating messages
bot-service does not consume events
```

Check:

```powershell
docker compose ps rabbitmq
docker compose logs rabbitmq
```

Wrong internal hostname:

```text
localhost works on the host, but not between containers.
```

Use Compose service names inside containers:

```text
auth-db
user-db
message-db
rabbitmq
auth-service
user-service
message-service
```

JWT mismatch:

```text
BFF returns 401 for tokens created by auth-service.
```

Make sure `JWT_SECRET` is the same for `auth-service` and `bff-service`.
