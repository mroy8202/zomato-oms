# Zomato OMS

A backend system that handles the order lifecycle for a food delivery app — order creation, payment, and delivery partner assignment. Built it to practice solving real backend problems instead of another CRUD app.

Tech: Java 21, Spring Boot, Apache Kafka, PostgreSQL, Docker

---

## What it does

You place an order, it gets saved, payment kicks off, and once payment succeeds a delivery partner gets assigned. All three steps run independently — they talk to each other through Kafka instead of calling each other directly.

```
POST /orders
   → order saved (status: PAYMENT_PENDING)
   → event published to Kafka topic "order-created"
   → payment service picks it up, processes payment, status: CONFIRMED
   → event published to Kafka topic "payment-success"
   → delivery service picks it up, locks an available partner, assigns it
   → status: OUT_FOR_DELIVERY
```

The client gets a response right after the order is saved. Everything after that happens in the background.

---

## Why Kafka instead of just calling the services directly

First version of this used Spring's `ApplicationEventPublisher`. Worked fine until I realized — if delivery assignment fails, it rolls back the payment too, because they were running in the same transaction. That's wrong. Payment already succeeded, a delivery issue shouldn't undo it.

Switched to Kafka so each step runs in its own transaction. Order service publishes an event and moves on. Payment and delivery react independently.

---

## Problems I had to actually solve

**Two orders grabbing the same delivery partner**

If two orders hit delivery assignment at the same time, both could read the same available partner before either one marks it unavailable. Fixed with `SELECT FOR UPDATE` — locks the row at the DB level so the second request waits until the first one finishes.

**Customer getting charged twice**

Kafka doesn't guarantee a message is delivered only once — it can redeliver after a crash. So every payment request carries an idempotency key. If a payment with that key already exists, return it instead of processing again. Also added a unique constraint on the DB column as backup, in case two requests somehow hit at the exact same time.

**Orders skipping steps in the lifecycle**

Nothing stopped someone from updating an order straight from `CREATED` to `DELIVERED`, or cancelling something that's already delivered. Built a simple state machine — basically a map of what status can go to what — and every status update checks against it first.

**Retry logic silently not working**

Had `@Retryable` and `@Transactional` on the same method in `DeliveryService`. Looked fine, but retries weren't opening new transactions. Took a while to figure out why — turns out when Spring retries internally, it skips its own transaction proxy. Fixed by splitting it into two methods: one public method with `@Retryable` that calls a separate `@Transactional` method.

---

## Design patterns, briefly

Used Strategy pattern for payment methods (UPI, Card) and delivery assignment (Random, Nearest by distance). Point of this — adding a new payment method later means writing one new class, not editing `PaymentService`.

A factory auto-discovers whatever Strategy beans exist in the Spring context, so nothing needs manual wiring when you add a new one.

---

## API

```
POST   /orders                     create an order
GET    /orders/{id}                get order + full status history
DELETE /orders/{id}/cancel         cancel an order
PUT    /orders/{id}/status         manually set status (admin use)
POST   /payments                   manually trigger payment (for testing)
POST   /delivery/assign/{orderId}  manually trigger delivery assignment
```

Example request:

```json
POST /orders
{
  "userId": 1,
  "restaurantId": 101,
  "items": [
    { "itemId": 1, "quantity": 2, "price": 200.0 }
  ]
}
```

---

## Order status flow

```
CREATED → PAYMENT_PENDING → CONFIRMED → PREPARING → OUT_FOR_DELIVERY → DELIVERED
                                  ↓
                              CANCELLED (allowed from most states, not after DELIVERED)
```

---

## Running it locally

Need Kafka and Postgres running first.

```bash
docker-compose up -d
```

Insert a delivery partner manually (nothing seeds this automatically):

```sql
INSERT INTO delivery_partner (name, available, latitude, longitude, version)
VALUES ('Rahul', true, 12.9716, 77.5946, 0);
```

Then:

```bash
mvn spring-boot:run
```

Tests:

```bash
mvn test
```

---

## What's not done yet

- Order save and Kafka publish aren't atomic right now — if Kafka publish fails after the DB commit, that order just sits there. Outbox pattern would fix this, haven't added it yet.
- No dead letter topic. If a message fails all retries, it just gets logged and dropped right now.
- No tracing across the flow — debugging a specific order across logs means grepping by orderId manually.
- No circuit breaker on the DB calls during delivery assignment.

Listing these because I know about them, not because I forgot.

---

## Project layout

```
config/                 kafka + retry config
controller/             REST endpoints
service/                order, payment, delivery logic
repository/             JPA repos
model/                  entities
dto/                    request/response objects
mapper/                 entity <-> dto conversion
kafka/event/            kafka message classes
kafka/producer/         publishing events
kafka/consumer/         consuming events
payment/strategy/       UPI, Card strategies
delivery/assignment/    Random, Nearest strategies
statemachine/           order status validation
exception/              custom exceptions + handler
```

---

Mritunjay Roy
mroy8202@gmail.com