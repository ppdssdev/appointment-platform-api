# Appointments API

A tenant-aware appointment scheduling backend built as a production-style Java portfolio project. It manages customers, professionals, recurring availability, and appointment lifecycles while enforcing scheduling consistency in both the application and PostgreSQL.

## Architecture

The application uses a pragmatic layered architecture:

```text
REST controllers and validated DTOs
                |
transactional application services
                |
Spring Data JPA repositories
                |
PostgreSQL with Flyway constraints
```

Controllers handle HTTP concerns and derive the tenant from the authenticated JWT. Services own business rules and transaction boundaries. Repositories keep every aggregate lookup tenant-scoped. Flyway is the only mechanism that changes the database schema.

## Domain model

- **Tenant** owns all business data and provides the isolation boundary.
- **Customer** belongs to a tenant and has a tenant-unique phone number.
- **Professional** belongs to a tenant, can be activated or deactivated, and has an IANA time zone.
- **ProfessionalAvailability** defines recurring weekly local-time windows for a professional.
- **Appointment** connects one customer and one professional for an absolute UTC time range. Its status is `PENDING`, `CONFIRMED`, or `CANCELLED`.
- **WhatsAppChannel** maps an inbound destination phone number to a tenant and preserves the original inbound-message workflow.

## Key engineering decisions

- UUID identifiers avoid coordination around numeric sequences.
- API and persistence timestamps represent absolute instants; PostgreSQL stores them as `timestamptz`.
- Availability uses the professional's IANA time zone, making recurring schedules explicit across regions.
- `open-in-view` is disabled, so persistence access happens within explicit service transactions.
- JPA version columns provide optimistic protection for ordinary concurrent updates.
- Bean Validation rejects malformed DTOs before service execution.
- Error responses include a timestamp, HTTP status, stable code, message, request path, and validation details. Internal exception messages are not exposed by the generic handler.
- Appointment creation requires an `Idempotency-Key`. Keys are unique within a tenant. Repeating the same key and payload returns the original appointment; using that key for different data returns HTTP 409.

## Concurrency strategy

Scheduling and rescheduling lock the professional row with a pessimistic write lock. This serializes bookings for one professional while allowing different professionals to be booked concurrently.

PostgreSQL is the final consistency boundary. A GiST exclusion constraint rejects intersecting `[start, end)` ranges for the same professional when either appointment is `PENDING` or `CONFIRMED`. Cancelled appointments do not block time. The application also performs an early overlap query to return a clear conflict before the database constraint is reached.

## Security

The API is stateless and uses HS256 JWT bearer tokens. The token contains the tenant ID used by protected resources. Health and OpenAPI endpoints are public; business endpoints require authentication.

Local defaults are intended only for development:

- username: `portfolio`
- password: `change-me`
- tenant: `00000000-0000-0000-0000-000000000001`

Override `APP_USERNAME`, `APP_PASSWORD`, `APP_TENANT_ID`, and `JWT_SECRET` outside local development. The JWT secret must contain at least 32 bytes.

## API

Swagger UI is available at [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html), and the OpenAPI document is available at `/v3/api-docs`.

Main operations:

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/v1/auth/token` | Issue a JWT |
| `POST` | `/api/v1/customers` | Create a customer |
| `PUT` | `/api/v1/customers/{id}` | Update a customer |
| `POST` | `/api/v1/professionals` | Create a professional |
| `PUT` | `/api/v1/professionals/{id}` | Update a professional |
| `POST` | `/api/v1/professionals/{id}/availability` | Add an availability window |
| `GET` | `/api/v1/professionals/{id}/availability` | List availability windows |
| `POST` | `/api/v1/appointments` | Schedule an appointment |
| `GET` | `/api/v1/appointments/{id}` | Retrieve an appointment |
| `GET` | `/api/v1/appointments` | List appointments with pagination |
| `POST` | `/api/v1/appointments/{id}/confirm` | Confirm a pending appointment |
| `POST` | `/api/v1/appointments/{id}/cancel` | Cancel an appointment idempotently |
| `PUT` | `/api/v1/appointments/{id}/schedule` | Reschedule an active appointment |

### Example flow

Obtain a token:

```bash
curl -X POST http://localhost:8080/api/v1/auth/token \
  -H 'Content-Type: application/json' \
  -d '{"username":"portfolio","password":"change-me"}'
```

Export the returned `accessToken`, then create a professional:

```bash
curl -X POST http://localhost:8080/api/v1/professionals \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"fullName":"Grace Hopper","active":true,"timeZone":"America/New_York"}'
```

Define Monday availability:

```bash
curl -X POST http://localhost:8080/api/v1/professionals/$PROFESSIONAL_ID/availability \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"dayOfWeek":"MONDAY","startTime":"09:00:00","endTime":"17:00:00"}'
```

Schedule using UTC instants:

```bash
curl -X POST http://localhost:8080/api/v1/appointments \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Idempotency-Key: booking-2026-001' \
  -H 'Content-Type: application/json' \
  -d "{\"customerId\":\"$CUSTOMER_ID\",\"professionalId\":\"$PROFESSIONAL_ID\",\"startsAt\":\"2026-10-05T14:00:00Z\",\"endsAt\":\"2026-10-05T15:00:00Z\"}"
```

## Testing

The test suite combines JUnit 5, Mockito, Spring Boot, and PostgreSQL Testcontainers. Integration tests start PostgreSQL 16, run all Flyway migrations, and cover appointment idempotency, overlap rejection, and lifecycle transitions.

Docker must be available to execute the integration tests:

```bash
./mvnw clean verify
```

## Run locally

Requirements: Docker with Compose support.

```bash
docker compose up --build
```

The command starts PostgreSQL and the API. PostgreSQL uses a named volume, waits until it is healthy, and then the application applies Flyway migrations. Useful endpoints:

- API: `http://localhost:8080`
- Health: `http://localhost:8080/actuator/health`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- PostgreSQL: `localhost:5432`

Stop the services with `docker compose down`. Add `-v` only when you intentionally want to remove local database data.

## Technology stack

- Java 21
- Spring Boot 3
- Spring Web and Bean Validation
- Spring Data JPA and Hibernate
- Spring Security and JWT resource server
- PostgreSQL 16 and Flyway
- Springdoc OpenAPI / Swagger UI
- Spring Boot Actuator
- JUnit 5, Mockito, AssertJ, and Testcontainers
- Docker and Docker Compose
