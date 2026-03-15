### Overview

This repository contains a small **Spring Boot** / **Spring Cloud** microservices system that looks like a simple online shop.

Services:
- `gateway` – API gateway based on Spring Cloud Netflix Zuul.
- `order` – order service that calls the notification service using OpenFeign.
- `notification` – notification service.
- `users` – users service.

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
   |
   v
[ notification ]
```

- `gateway` (port `8000`)
  - Routes `/api/order/**` → `order` (port `8001`)
  - Routes `/api/users/**` → `users` (port `8003`)

- `order` (port `8001`)
  - Endpoint `GET /doOrder?orderName=...`
  - Uses `NotificationServiceFeignClient` to call `notification`
  - Call is wrapped with Resilience4j Circuit Breaker

- `notification` (port `8002`)
  - Endpoint `POST /sendNotification`
  - Logs notification text to the console

- `users` (port `8003`)
  - In‑memory list of users
  - `GET /users` and `GET /users/{id}`

---

### Spring Cloud usage

- **Zuul Gateway**
  - Single entry point to the system
  - Route configuration in `gateway/application.yml` (paths mapped to services)

- **OpenFeign**
  - Interface `NotificationServiceFeignClient` describes HTTP API of `notification`
  - `order` calls Java methods instead of building HTTP requests manually

- **Circuit Breaker (Resilience4j)**
  - `OrderService.placeOrder` is annotated with `@CircuitBreaker`
  - Fallback returns a clear message when `notification` is down

- **Sleuth (distributed tracing)**
  - Adds trace/span ids to logs in all services
  - Helps follow a request across `gateway → order → notification` or `gateway → users`

---

### Tech stack

- Java 11
- Spring Boot 2.7.1
- Spring Cloud 2021.0.3 (Zuul, OpenFeign, Circuit Breaker, Sleuth)
- Maven (separate `pom.xml` per service)

---

### How to run

**Prerequisites**
- JDK 11+
- Maven 3.8+

**Build** (from project root):

```bash
cd gateway && mvn clean package
cd ../order && mvn clean package
cd ../notification && mvn clean package
cd ../users && mvn clean package
```

**Run** each service in its own terminal:

```bash
# gateway (8000)
cd gateway
mvn spring-boot:run

# order (8001)
cd ../order
mvn spring-boot:run

# notification (8002)
cd ../notification
mvn spring-boot:run

# users (8003)
cd ../users
mvn spring-boot:run
```

---

### Example calls (via gateway)

Create order (triggers `order` + `notification`):

```bash
curl "http://localhost:8000/api/order/doOrder?orderName=iphone"
```

Get users list:

```bash
curl "http://localhost:8000/api/users/users"
```

Get user by id:

```bash
curl "http://localhost:8000/api/users/users/1"
```