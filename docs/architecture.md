# PulseHub Backend Architecture

This document describes the current backend architecture in this repository.
It reflects the implementation now: Java 25, Spring Boot 4, Maven reactor, REST between services, PostgreSQL per persistent service, and RabbitMQ for message events.

## Repository Layout

```text
pulsehub-backend/
|-- auth-service/
|-- bff-service/
|-- bot-service/
|-- message-service/
|-- user-service/
|-- docs/
|-- requests/
|-- docker-compose.yml
`-- pom.xml
```

The root `pom.xml` is a Maven reactor and builds all services:

- `auth-service`
- `bff-service`
- `bot-service`
- `message-service`
- `user-service`

Each service is its own Spring Boot project in its own directory.

## Services

| Service | Port | Responsibility | Persistent data |
| --- | --- | --- | --- |
| `bff-service` | `8080` | Frontend entry point, JWT validation, REST forwarding | No |
| `user-service` | `8081` | User profile API | Yes |
| `auth-service` | `8082` | Registration, login, password hashing, JWT generation | Yes |
| `message-service` | `8083` | Chat messages, message persistence, RabbitMQ publishing | Yes |
| `bot-service` | `8084` | RabbitMQ consumer, simple PulseBot responses | No |

All services expose:

```text
/actuator/health
```

Only the Actuator `health` endpoint is exposed over HTTP.

## Data Ownership

Each persistent service owns its own database.

| Database | Owner |
| --- | --- |
| `pulsehub_auth` | `auth-service` |
| `pulsehub_users` | `user-service` |
| `pulsehub_messages` | `message-service` |

`bff-service` has no database.
`bot-service` has no database.

Services should not share database tables.
Cross-service access should happen through HTTP or messaging.

## HTTP Routing

The frontend should call only `bff-service`.

Current BFF routes:

| BFF endpoint | Target |
| --- | --- |
| `POST /api/auth/register` | `auth-service POST /auth/register` |
| `POST /api/auth/login` | `auth-service POST /auth/login` |
| `GET /api/me` | BFF reads JWT claims locally |
| `GET /api/users` | `user-service GET /users` |
| `GET /api/users/{id}` | `user-service GET /users/{id}` |
| `POST /api/messages` | `message-service POST /messages` |
| `GET /api/messages` | `message-service GET /messages` |
| `GET /api/messages/{id}` | `message-service GET /messages/{id}` |

`/api/auth/register` and `/api/auth/login` are open.
Other BFF API endpoints require:

```text
Authorization: Bearer <token>
```

## Authentication

`auth-service` creates JWTs.
`bff-service` validates JWTs.

The same `JWT_SECRET` must be configured for both:

```text
auth-service signs tokens
bff-service validates tokens
```

JWT claims currently used by BFF:

- `sub` / subject: user ID
- `username`
- issued time
- expiry time

`message-service`, `user-service`, and `bot-service` do not validate JWTs in the current implementation.

## Message Flow

The current message flow is:

```text
Client
  -> bff-service POST /api/messages
  -> message-service POST /messages
  -> PostgreSQL pulsehub_messages
  -> RabbitMQ exchange pulsehub.messages
  -> RabbitMQ queue pulsehub.message-published
  -> bot-service
  -> message-service POST /messages
  -> PostgreSQL pulsehub_messages
```

When a client sends:

```json
{
  "channel": "general",
  "content": "hej bot"
}
```

BFF reads `userId` and `username` from JWT and forwards:

```json
{
  "senderId": "user-id-from-token",
  "username": "username-from-token",
  "channel": "general",
  "content": "hej bot"
}
```

`message-service` saves the message and publishes `message-published`.

## RabbitMQ

Current RabbitMQ names:

| Type | Name |
| --- | --- |
| Exchange | `pulsehub.messages` |
| Routing key | `message.published` |
| Queue | `pulsehub.message-published` |
| Event type | `message-published` |

Event shape:

```json
{
  "eventId": "uuid",
  "type": "message-published",
  "messageId": "uuid",
  "senderId": "uuid",
  "username": "adam",
  "channel": "general",
  "content": "hej bot",
  "createdAt": "2026-06-10T10:00:00Z"
}
```

`message-service` publishes events.
`bot-service` consumes events.

## Bot Behavior

`bot-service` listens to:

```text
pulsehub.message-published
```

It creates a PulseBot response if message content contains:

- `/help`
- `hej bot`

PulseBot response:

```json
{
  "senderId": "00000000-0000-0000-0000-000000000001",
  "username": "PulseBot",
  "channel": "general",
  "content": "Hej! Jag \u00e4r PulseBot. Testa att skriva /help."
}
```

Loop protection:

```text
If username == PulseBot, bot-service ignores the event.
```

This prevents PulseBot from responding to its own messages.

## Runtime Dependencies

For the full local flow, run:

- PostgreSQL
- RabbitMQ
- `auth-service`
- `user-service`
- `message-service`
- `bot-service`
- `bff-service`

The full backend environment can be started by `docker-compose.yml`.
It starts all backend services, three PostgreSQL containers, and RabbitMQ.

## Current Non-Goals

These are not implemented yet:

- frontend
- gRPC
- Kubernetes
- full Docker Compose for all infrastructure/services
- advanced authorization or roles
- message outbox pattern
- bot AI integration

## Related Docs

- [Health checks](health-checks.md)
- [End-to-end message flow](test-flow.md)
