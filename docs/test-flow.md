# PulseHub End-to-End Message Flow

This document verifies the current backend chain:

1. Client calls `POST /api/messages`.
2. `bff-service` reads `userId` and `username` from the JWT.
3. `bff-service` forwards the message to `message-service`.
4. `message-service` saves the original message.
5. `message-service` publishes `message-published` to RabbitMQ.
6. `bot-service` consumes the event from `pulsehub.message-published`.
7. `bot-service` posts a PulseBot response to `message-service`.
8. `message-service` saves the PulseBot response.

## Required Processes

Run these locally before testing:

| Process | Default |
| --- | --- |
| PostgreSQL auth DB | `localhost:5434` |
| PostgreSQL user DB | `localhost:5433` |
| PostgreSQL message DB | `localhost:5435` |
| RabbitMQ | `localhost:5672` |
| RabbitMQ Management UI | `http://localhost:15672` |
| `user-service` | `http://localhost:8081` |
| `auth-service` | `http://localhost:8082` |
| `message-service` | `http://localhost:8083` |
| `bot-service` | `http://localhost:8084` |
| `bff-service` | `http://localhost:8080` |

RabbitMQ uses:

```text
Exchange: pulsehub.messages
Routing key: message.published
Queue: pulsehub.message-published
```

## Start Infrastructure

Start the full Docker Compose environment:

```powershell
docker compose up --build
```

Verify RabbitMQ:

```powershell
Test-NetConnection localhost -Port 5672
```

`TcpTestSucceeded` should be `True`.

If you run without Docker Compose, start each service in its own terminal and set matching ports and service URLs manually.

## Register Or Login Through BFF

Register a test user if needed:

```powershell
$registerBody = @{
  username = "flowtester"
  displayName = "Flow Tester"
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
  username = "flowtester"
  password = "password123"
} | ConvertTo-Json

$login = Invoke-RestMethod `
  -Uri http://localhost:8080/api/auth/login `
  -Method Post `
  -ContentType "application/json; charset=utf-8" `
  -Body ([System.Text.Encoding]::UTF8.GetBytes($loginBody))
```

Check that the token works:

```powershell
Invoke-RestMethod `
  -Uri http://localhost:8080/api/me `
  -Headers @{ Authorization = "Bearer $($login.token)" }
```

Expected result:

```json
{
  "userId": "uuid",
  "username": "flowtester"
}
```

## Trigger The Full Message Flow

Post a message through BFF. The client only sends `channel` and `content`; BFF fills `senderId` and `username` from the JWT.

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

Because the bot flow is asynchronous through RabbitMQ, wait one second before reading messages:

```powershell
Start-Sleep -Seconds 1
```

Get messages through BFF:

```powershell
Invoke-RestMethod `
  -Uri "http://localhost:8080/api/messages?channel=general" `
  -Headers @{ Authorization = "Bearer $($login.token)" }
```

## Expected Logs

`message-service` should show that `POST /messages` was handled twice:

- once for the original user message from BFF
- once for the PulseBot response sent by `bot-service`

`bot-service` should log a consumed event similar to:

```text
Received message-published event: eventId=..., messageId=..., username=flowtester, channel=general
```

After PulseBot posts its own message, `bot-service` may also receive the PulseBot event. It should not create another response because `PulseBotService` ignores events where `username` is `PulseBot`.

RabbitMQ Management UI should show traffic on queue:

```text
pulsehub.message-published
```

## Expected GET Result

The `GET /api/messages?channel=general` response should include at least two messages:

```json
[
  {
    "username": "flowtester",
    "channel": "general",
    "content": "hej bot"
  },
  {
    "username": "PulseBot",
    "channel": "general",
    "content": "Hej! Jag \u00e4r PulseBot. Testa att skriva /help."
  }
]
```

The response also includes IDs and timestamps. Ordering can depend on database ordering and timing, so check that both messages exist.

## Loop Protection

The loop protection is:

```text
If username == PulseBot, bot-service ignores the event.
```

This prevents:

1. PulseBot posts a message.
2. `message-service` publishes another `message-published`.
3. `bot-service` consumes PulseBot's own event.
4. `bot-service` stops and does not post another bot message.

## Troubleshooting

If `POST /api/messages` returns `401`, the token is missing or expired. Login again and use `Bearer $($login.token)`.

If `POST /api/messages` returns `503`, check that `message-service` can reach RabbitMQ on `localhost:5672`.

If the original message appears but the bot response does not, check:

- `bot-service` is running
- RabbitMQ is running
- queue name is `pulsehub.message-published`
- `MESSAGE_SERVICE_URL` for `bot-service` is `http://localhost:8083`
- wait one second and run `GET /api/messages?channel=general` again
