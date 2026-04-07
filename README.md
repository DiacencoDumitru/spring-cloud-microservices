### Overview

This repository contains a small **Spring Boot** / **Spring Cloud** microservices system that looks like a simple online shop.

Services:
- `gateway` – API gateway based on Spring Cloud Netflix Zuul with JWT auth and role-based access.
- `order` – order service; runs an **orchestrated Saga** (inventory reserve → notify → complete).
- `inventory` – stock reservations (`POST/DELETE /reservations`) for the Saga demo.
- `notification` – notifications and **compensation** callback for Saga rollback.
- `users` – users API.

---

### Architecture

```text
Client (Postman / browser)
          |
          v
   [ gateway (Zuul) ]
        /       \
       v         v
[ order ]     [ users ]
   |   \
   |    v
   |  [ inventory ]
   v
[ notification ]
```

- `gateway` (port `8000`)
  - `POST /auth/token` — issues JWT token for configured users
  - `GET /api/gateway/me` — returns authenticated user info (`USER` or `ADMIN`)
  - `GET /api/gateway/admin/ping` — admin-only probe endpoint
  - `/api/order/**` → `order` (`8001`)
  - `/api/users/**` → `users` (`8003`)
  - `/api/inventory/**` → `inventory` (`8004`) — `ADMIN` only

- `order` (`8001`)
  - `GET /doOrder?orderName=...` — runs **OrderPlacementSaga** (see below)

- `inventory` (`8004`)
  - `POST /reservations` body `{"sku":"iphone"}` → `{"reservationId":"..."}` or `409` if out of stock
  - `DELETE /reservations/{reservationId}` — release (compensation)

- `notification` (`8002`)
  - `POST /sendNotification`
  - `POST /compensateNotification` — invoked when the Saga must undo the notify step

- `users` (`8003`)
  - `GET /users`, `GET /users/{id}`
  - `GET /users?prefix=...&limit=...` — optional filtering by name prefix and result limit

---

### Saga pattern (orchestration)

This project uses **orchestration**: the `order` service coordinates steps and compensations.

**Forward steps** (happy path):
1. Create local order state (`InMemoryOrderStore`).
2. **Reserve** stock in `inventory` (Feign).
3. **Notify** the customer via `notification` (Feign).
4. Mark order **completed** locally.

**Compensations** (rollback), in reverse order when needed:
- If notify fails after a reservation exists → `DELETE` reservation (release stock), cancel local order.
- If local “complete” fails after notify (rare in this demo) → `compensateNotification`, release reservation, cancel order.
- If reserve returns **409** (no stock) → cancel local order only.

**Not in this repo**: *choreography* (events only, no central coordinator) and durable Saga logs / outbox — you would add those for production.

---

### Spring Cloud usage

- **Zuul** – single entry; routes in `gateway/application.yml`.
- **OpenFeign** – `InventoryFeignClient`, `NotificationServiceFeignClient` for remote steps.
- **Resilience4j** – `@CircuitBreaker` on `OrderService.placeOrder` (whole Saga call; fallback when the guarded path fails too often).
- **Sleuth** – trace ids across services in logs.

---

### Gateway security (JWT + roles)

Gateway validates JWT on each protected request and applies RBAC:

- Public:
  - `POST /auth/token`
  - `/`, `/index.html`
- Requires `USER` or `ADMIN`:
  - `/api/gateway/me`
  - `/api/order/**`
  - `/api/users/**`
- Requires `ADMIN`:
  - `/api/gateway/admin/ping`
  - `/api/inventory/**`

Default demo users (configurable in `gateway/src/main/resources/application.yml`):

- `user` / `user123` → role `USER`
- `admin` / `admin123` → roles `USER`, `ADMIN`

---

### Tech stack

- Java 11, Spring Boot 2.7.1, Spring Cloud 2021.0.3, Maven (one `pom.xml` per service).

---

### How to run

**Prerequisites:** JDK 11+, Maven 3.8+

**Build** (from repo root — there is a root `pom.xml` that aggregates all modules):

```bash
mvn clean install
```

Or build a single service:

```bash
cd gateway && mvn clean package
```

**Run** (five terminals):

```bash
# gateway (8000)
cd gateway && mvn spring-boot:run
```

```bash
cd order && mvn spring-boot:run
```

```bash
cd inventory && mvn spring-boot:run
```

```bash
cd notification && mvn spring-boot:run
```

```bash
cd users && mvn spring-boot:run
```

---

### Example calls (via gateway)

Get JWT:

```bash
curl -X POST "http://localhost:8000/auth/token" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"user\",\"password\":\"user123\"}"
```

Response contains `accessToken`. Use it as:

```bash
Authorization: Bearer <accessToken>
```

Place order (Saga: inventory → notification → local complete):

```bash
curl "http://localhost:8000/api/order/doOrder?orderName=iphone" \
  -H "Authorization: Bearer <accessToken>"
```

Reserve stock via gateway (optional):

```bash
curl -X POST "http://localhost:8000/api/inventory/reservations" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <adminAccessToken>" \
  -d "{\"sku\":\"iphone\"}"
```

Users:

```bash
curl "http://localhost:8000/api/users/users" -H "Authorization: Bearer <accessToken>"
curl "http://localhost:8000/api/users/users/1" -H "Authorization: Bearer <accessToken>"
curl "http://localhost:8000/api/users/users?prefix=a&limit=1" -H "Authorization: Bearer <accessToken>"
curl "http://localhost:8000/api/gateway/me" -H "Authorization: Bearer <accessToken>"
curl "http://localhost:8000/api/gateway/admin/ping" -H "Authorization: Bearer <adminAccessToken>"
```

---

### Developer Hub and OpenAPI

- **http://localhost:8000/** — static hub with links to common gateway routes and Swagger UI on each documented service.
- **SpringDoc** on **users**, **order**, and **notification**: `/swagger-ui/index.html` and `/v3/api-docs`.
- Ports: gateway `8000`, order `8001`, notification `8002`, users `8003`, inventory `8004`.
