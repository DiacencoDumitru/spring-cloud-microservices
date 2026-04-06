### Overview

This repository contains a small **Spring Boot** / **Spring Cloud** microservices system that looks like a simple online shop.

Services:
- `gateway` – API gateway based on Spring Cloud Netflix Zuul.
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
  - `/api/order/**` → `order` (`8001`)
  - `/api/users/**` → `users` (`8003`)
  - `/api/inventory/**` → `inventory` (`8004`) — optional direct calls for learning

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

Place order (Saga: inventory → notification → local complete):

```bash
curl "http://localhost:8000/api/order/doOrder?orderName=iphone"
```

Reserve stock via gateway (optional):

```bash
curl -X POST "http://localhost:8000/api/inventory/reservations" -H "Content-Type: application/json" -d "{\"sku\":\"iphone\"}"
```

Users:

```bash
curl "http://localhost:8000/api/users/users"
curl "http://localhost:8000/api/users/users/1"
```

---

### Developer Hub and OpenAPI

- **http://localhost:8000/** — static hub with links to common gateway routes and Swagger UI on each documented service.
- In Developer Hub, there is an **API Playground**: select a predefined gateway GET endpoint, optionally pass a parameter (`id` or `orderName`), run request, and inspect response body/status in-place.
- **SpringDoc** on **users**, **order**, and **notification**: `/swagger-ui/index.html` and `/v3/api-docs`.
- Ports: gateway `8000`, order `8001`, notification `8002`, users `8003`, inventory `8004`.
