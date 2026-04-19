# Stelio

**Type:** Backend / Transactional System
**Focus:** Transactional safety, consistency, idempotency, state management

---

## 💡 Project Overview

A real-estate rental platform backend where users can list and rent properties. Built with Spring Boot, it prioritises:

- **Strong consistency** and transactional booking workflows
- **Idempotency** to protect against duplicate rentals and race conditions
- **File storage** via Cloudflare R2 (S3-compatible) for property images
- **JWT-based authentication** with token revocation
- **Messaging** between renters and property owners
- **Real-time updates** via WebSocket (STOMP) for live booking status notifications

---

## 🏗️ System Architecture & Design

![Alt text](./images/High_Level_Diagram.png)

---

## ⚙️ Core Features

### 1. Authentication & User Roles

- Register and login with JWT tokens (24-hour expiry)
- Logout invalidates the token via a server-side blacklist
- Three roles: **ADMIN**, **OWNER** (host), **RENTER**
- Renters can upgrade themselves to owners via `PATCH /api/users/{userId}`

### 2. Property Listings

- Owners can create, update, and delete properties
- Each property includes title, description, price, type (`APARTMENT`, `HOUSE`, `VILLA`, `CABIN`), address, city, guest/bed/bath counts, and multiple images
- Images are uploaded to **Cloudflare R2** and served via a public URL
- Properties have an `ACTIVE` / `INACTIVE` status; only active properties are shown publicly
- Renters can save properties to their **Favourites**

### 3. Transactional Booking Workflow

Booking is handled atomically with full ACID guarantees across all stages:

1. **Availability check** — overlapping confirmed bookings are blocked with a **pessimistic lock** (`SELECT FOR UPDATE`) on the property row, preventing double-bookings under concurrent load
2. **Reservation created** — status set to `PENDING_PAYMENT` with a **10-minute TTL**; expired reservations are automatically excluded from conflict checks
3. **Payment Intent created** — via `POST /api/payments/{bookingId}` (requires `Idempotency-Key` header); a Stripe `PaymentIntent` is generated and the `client_secret` is returned to the frontend for client-side payment processing
4. **Stripe webhook confirms payment** — Stripe sends a `payment_intent.succeeded` event to `POST /api/webhooks/stripe`; the backend verifies the webhook signature, validates the exact amount received, and atomically transitions the booking to `CONFIRMED`
5. **Real-time notification** — once confirmed, the updated booking is pushed to the renter instantly via WebSocket (see [Real-Time Booking Updates](#6-real-time-booking-updates))

#### Stripe Integration Details

- **PaymentIntent creation** is wrapped in idempotency — retrying with the same `Idempotency-Key` header returns the cached response without creating a duplicate intent
- **Webhook signature verification** uses `Stripe.Webhook.constructEvent()` with the `Stripe-Signature` header and the webhook signing secret, rejecting any tampered or replayed events
- **Amount validation** — the amount received from Stripe is compared against the stored booking balance (converted to the smallest currency unit); a mismatch throws an `IllegalStateException` and halts the confirmation
- **Idempotent webhook handling** — the `payment_intent.succeeded` event is only processed once; duplicate webhook deliveries are safely ignored because the booking's payment status is checked before any mutation

```
POST /api/bookings/{propertyId}/book
  └─ Pessimistic lock on property
  └─ Overlap check (confirmed + pending with valid TTL)
  └─ BookingEntity saved (PENDING_PAYMENT)
  └─ BookingRequestedEvent published (after commit)
       └─ Conversation + initial message created

POST /api/payments/{bookingId}            [Idempotency-Key required]
  └─ Stripe PaymentIntent created
  └─ client_secret returned to frontend

POST /api/webhooks/stripe                 [Stripe-Signature verified]
  └─ payment_intent.succeeded received
  └─ Amount validated
  └─ Booking → CONFIRMED, PaymentStatus → PAID
  └─ WebSocket push to renter
```

### 4. Real-Time Booking Updates

Booking status changes triggered by Stripe are pushed to the client immediately using **STOMP over WebSocket** (via SockJS fallback), eliminating the need for polling.

#### How It Works

1. The frontend connects to the `/ws` endpoint using SockJS + STOMP, authenticating via the `Authorization: Bearer <token>` STOMP header
2. Upon connection, `AuthChannelInterceptor` validates the JWT and attaches the user's identity to the STOMP session
3. The client subscribes to `/user/my-bookings` to receive personal booking updates
4. When Stripe confirms a payment via webhook, `PaymentService` calls `SimpMessagingTemplate.convertAndSendToUser(userId, "/my-bookings", bookingDto)` to deliver the updated `BookingResDto` directly to the authenticated user's session

```
Client                          Server
  │                               │
  ├── CONNECT (Bearer token) ───► AuthChannelInterceptor validates JWT
  │                               │
  ├── SUBSCRIBE /user/my-bookings │
  │                               │
  │   [Stripe webhook fires]      │
  │                               ├── PaymentService confirms booking
  │                               ├── SimpMessagingTemplate.convertAndSendToUser(...)
  │                               │
  ◄── BookingResDto (JSON) ───────┤
```

#### WebSocket Configuration

| Property | Value |
| --- | --- |
| Endpoint | `/ws` (SockJS enabled) |
| App destination prefix | `/app` |
| User destination | `/user/my-bookings` |
| Broker | In-memory simple broker |
| Auth | JWT via STOMP `Authorization` header |
| Allowed origins | `localhost:5173`, production frontend URL |

### 5. Idempotency

- Booking requests and payment intent creation require an **Idempotency-Key** header
- On first receipt, a record is inserted with `PENDING` status; the operation executes and the record is updated to `COMPLETED` with the serialised response
- On retry, `DataIntegrityViolationException` is caught (unique constraint on the key), the existing record is fetched, and the stored response is returned — identical to the original, with no side effects
- Keys expire after 24 hours; stale keys are cleaned up nightly

### 6. Booking Lifecycle (State Machine)

```
PENDING_PAYMENT → PENDING_APPROVAL → CONFIRMED → COMPLETED
                                   ↘ REJECTED
              ↘ CANCELLED
              ↘ EXPIRED
              ↘ NOSHOW
```

- **Owner** can approve, reject, or mark bookings as no-show
- **Renter** can cancel their own bookings
- Expired reservations are released automatically, returning the property to available
- `CONFIRMED` transitions triggered by Stripe are followed immediately by a WebSocket push to the renter

### 7. Messaging

- Renters and owners can exchange messages via conversations
- A conversation is automatically created when a booking request is submitted (published as a `BookingRequestedEvent` after transaction commit, handled by `BookingMessageListener`)
- Conversations track unread counts and mute status per participant
- Conversations may optionally be linked to a review

### 8. Reviews

- Owners can view reviews and star ratings for their properties
- Review statistics (average stars, individual messages) are exposed via the API

### 9. Property Dashboard & Statistics

- Owners get a per-property dashboard: today's earnings, monthly earnings, occupancy rate, upcoming check-ins, pending reviews, booking counts by status
- A booking calendar endpoint returns confirmed/pending bookings as date ranges

### 10. Automated Cleanup

A scheduled task runs daily at **02:30 AM** to:

- Delete expired idempotency records
- Purge expired blacklisted JWT tokens

---

## 🏗️ Architecture

```
com/aaronjosh/real_estate_app/
├── controllers/     # REST API layer (11 controllers)
├── services/        # Business logic
├── repositories/    # Spring Data JPA repositories
├── models/          # JPA entities
├── dto/             # Request / response DTOs
│   ├── auth/
│   ├── booking/
│   ├── message/
│   ├── property/
│   └── payment/
├── security/        # JWT filter, STOMP auth interceptor & Spring Security config
├── config/          # Application configuration beans (incl. WebSocket, Stripe)
├── scheduler/       # Nightly cleanup jobs
├── util/            # Helpers (e.g. CloudflareR2Service)
└── exceptions/      # Custom exception types
```

### Data Model

| Entity              | Key Relationships                                                                                      |
| ------------------- | ------------------------------------------------------------------------------------------------------ |
| `users`             | owns many `property`, has many `bookings`, `favorites`, `reviews`, `messages`                          |
| `property`          | belongs to a `users` (host), has many `bookings`, `files`, `favorites`, `reviews`, one `propertyStats` |
| `bookings`          | belongs to `property` and `users` (renter); indexed on `(property_id, startDateTime, endDateTime)`; holds `stripePaymentIntentId` for webhook reconciliation |
| `conversations`     | has many `participants` and `messages`; optionally linked to one `review`                              |
| `messages`          | sent by a `users`, belongs to a `conversation`, may have `files`                                       |
| `files`             | stored on Cloudflare R2; associated with either a `property` or a `message`                            |
| `propertyStats`     | one-to-one with `property`; aggregates booking and earnings data                                       |
| `blacklistedTokens` | stores revoked JWTs until they expire                                                                  |
| `idempotency`       | stores processed idempotency keys (24-hour TTL)                                                        |

---

## 🔒 Security

| Concern            | Approach                                                           |
| ------------------ | ------------------------------------------------------------------ |
| Authentication     | JWT (HMAC-SHA256), 24-hour expiry, `Authorization: Bearer <token>` |
| Token revocation   | Blacklist table checked on every request                           |
| Password storage   | BCrypt hashing                                                     |
| Session management | Stateless (no server-side sessions)                                |
| Concurrency safety | Pessimistic lock on overlapping booking query                      |
| Idempotency        | Per-user idempotency key with 24-hour TTL                          |
| WebSocket auth     | JWT validated in `AuthChannelInterceptor` on STOMP CONNECT         |
| Stripe webhooks    | Signature verified via `Webhook.constructEvent()` before processing|

**Public endpoints** (no authentication required):

- `POST /api/auth/login`
- `POST /api/auth/register`
- `GET /api/properties/`
- `GET /api/image/**`
- `POST /api/webhooks/stripe`
- `GET /ws/**` (WebSocket handshake; per-connection auth handled by STOMP interceptor)

---

## 🌐 API Reference

### Auth — `/api/auth`

| Method | Path        | Description                          |
| ------ | ----------- | ------------------------------------ |
| `POST` | `/login`    | Authenticate and receive a JWT       |
| `POST` | `/register` | Create a new user account            |
| `POST` | `/logout`   | Revoke the current JWT               |
| `POST` | `/verify`   | Verify token and return user details |

### Users — `/api/users`

| Method  | Path | Description                      |
| ------- | ---- | -------------------------------- |
| `PATCH` | `/`  | Upgrade role from RENTER → OWNER |

### Properties — `/api/properties`

| Method   | Path                     | Role   | Description                             |
| -------- | ------------------------ | ------ | --------------------------------------- |
| `GET`    | `/`                      | Public | List all active properties              |
| `GET`    | `/my-properties`         | OWNER  | List owner's properties                 |
| `GET`    | `/{propertyId}`          | Public | Property details with booking schedule  |
| `GET`    | `/{propertyId}/bookings` | OWNER  | Bookings for a specific property        |
| `POST`   | `/`                      | OWNER  | Create property (`multipart/form-data`) |
| `POST`   | `/{propertyId}`          | OWNER  | Update property details / images        |
| `DELETE` | `/{propertyId}`          | OWNER  | Delete property                         |

### Bookings — `/api/bookings`

| Method  | Path                    | Role   | Description                                             |
| ------- | ----------------------- | ------ | ------------------------------------------------------- |
| `GET`   | `/`                     | Auth   | RENTER: own bookings; OWNER: all property bookings      |
| `GET`   | `/{bookingId}`          | Auth   | Get booking details                                     |
| `POST`  | `/{propertyId}/book`    | RENTER | Request a booking (requires `Idempotency-Key` header)   |
| `POST`  | `/{propertyId}/reserve` | RENTER | Reserve a property (requires `Idempotency-Key` header)  |
| `PATCH` | `/{bookingId}`          | OWNER  | Update booking status                                   |
| `PATCH` | `/{bookingId}/cancel`   | RENTER | Cancel booking                                          |

### Payments — `/api/payments`

| Method | Path           | Role | Description                                                |
| ------ | -------------- | ---- | ---------------------------------------------------------- |
| `POST` | `/{bookingId}` | Auth | Generate Stripe Payment Intent (requires `Idempotency-Key` header) |

### Webhooks — `/api/webhooks`

| Method | Path      | Description                                              |
| ------ | --------- | -------------------------------------------------------- |
| `POST` | `/stripe` | Stripe webhook — confirms booking on `payment_intent.succeeded` and triggers WebSocket push |

### WebSocket — `/ws`

| Channel | Direction | Description |
| ------- | --------- | ----------- |
| `CONNECT` with `Authorization` header | Client → Server | Authenticates the STOMP session via JWT |
| `/user/my-bookings` | Server → Client | Receives `BookingResDto` when a booking transitions to `CONFIRMED` after payment |

### Messages — `/api/messages`

| Method | Path                | Description                     |
| ------ | ------------------- | ------------------------------- |
| `GET`  | `/`                 | List conversations (chat heads) |
| `POST` | `/`                 | Create new conversation         |
| `GET`  | `/{conversationId}` | Get messages in conversation    |
| `POST` | `/{conversationId}` | Send message                    |

### Favourites — `/api/favorite`

| Method   | Path            | Description               |
| -------- | --------------- | ------------------------- |
| `GET`    | `/{favoriteId}` | Check if favourite exists |
| `POST`   | `/{propertyId}` | Add to favourites         |
| `DELETE` | `/{propertyId}` | Remove from favourites    |

### Reviews — `/api/property/review`

| Method | Path                  | Role  | Description                     |
| ------ | --------------------- | ----- | ------------------------------- |
| `GET`  | `/stats/{propertyId}` | OWNER | Get review stats for a property |

### Property Stats — `/api/properties/stats`

| Method | Path                     | Role  | Description                   |
| ------ | ------------------------ | ----- | ----------------------------- |
| `GET`  | `/{propertyId}`          | OWNER | Property dashboard statistics |
| `GET`  | `/calendar/{propertyId}` | OWNER | Booking calendar              |

---

## 💻 Tech Stack

| Layer            | Technology                               |
| ---------------- | ---------------------------------------- |
| Language         | Java 21                                  |
| Framework        | Spring Boot 3.5                          |
| Security         | Spring Security + JJWT 0.12              |
| Persistence      | Spring Data JPA / Hibernate              |
| Database         | PostgreSQL                               |
| File Storage     | Cloudflare R2 (AWS SDK v2)               |
| Payments         | Stripe (stripe-java 32)                  |
| Real-time        | Spring WebSocket + STOMP (SockJS)        |
| Validation       | Jakarta Validation / Hibernate Validator |
| Utilities        | Lombok                                   |
| Containerisation | Docker (multi-stage build)               |

---

## 🚀 Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+
- PostgreSQL database
- Cloudflare R2 bucket (or any S3-compatible storage)
- Stripe account with a webhook endpoint configured to forward `payment_intent.succeeded` events to `POST /api/webhooks/stripe`

### Environment Variables

Create a `.env` file in the project root (it is git-ignored):

```env
# Database
DB_URL=jdbc:postgresql://<host>:<port>/<database>
DB_USERNAME=<username>
DB_PASSWORD=<password>
DB_DRIVER=org.postgresql.Driver

# JWT
JWT_SECRET=<a-long-random-secret>

# Cloudflare R2
CLOUD_BUCKET_NAME=<bucket-name>
CLOUD_ENDPOINT=https://<account-id>.r2.cloudflarestorage.com
CLOUD_PUBLIC_URL=https://pub-<id>.r2.dev
CLOUD_ACCESS_KEY=<r2-access-key>
CLOUD_SECRET_KEY=<r2-secret-key>

# Stripe
STRIPE_SECRET_KEY=<stripe-secret-key>
STRIPE_WEBHOOK_KEY=<stripe-webhook-signing-secret>

# Server (optional, defaults to 8080)
PORT=8080
```

> **Stripe webhook setup:** In your Stripe dashboard, add a webhook endpoint pointing to `https://<your-domain>/api/webhooks/stripe` and subscribe to the `payment_intent.succeeded` event. Copy the signing secret into `STRIPE_WEBHOOK_KEY`. For local development, use the [Stripe CLI](https://stripe.com/docs/stripe-cli): `stripe listen --forward-to localhost:8080/api/webhooks/stripe`.

### Run Locally

```bash
# Clone the repository
git clone https://github.com/shironzi/stelio-backend.git
cd stelio-backend

# Build and run
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.
The WebSocket endpoint will be available at `ws://localhost:8080/ws`.

### Run Tests

```bash
./mvnw test
```
