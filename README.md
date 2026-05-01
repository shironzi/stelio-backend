# Stelio — Real Estate Rental Platform

**Type:** Backend / Transactional System  
**Stack:** Java 21 · Spring Boot 3.5 · PostgreSQL · Cloudflare R2 · Stripe · WebSocket (STOMP)

---

## 📌 Overview

Stelio is a production-grade real estate rental platform backend built with Spring Boot. It supports two booking modes (instant pay and request-to-book), real-time booking status updates via WebSocket, Stripe payment processing, JWT authentication with token revocation, and file storage via Cloudflare R2.

The system is designed with a strong emphasis on:

- **Transactional safety** — pessimistic locking on booking conflicts, atomic status transitions
- **Idempotency** — duplicate-safe booking and payment operations using keyed records with 24-hour TTL
- **Consistency** — webhook signature verification, amount validation, and idempotent Stripe event handling
- **Real-time delivery** — STOMP over WebSocket for instant booking status pushes to renters

---

## 🏗️ Architecture

```
com/aaronjosh/real_estate_app/
├── controllers/          # REST API layer (11 controllers)
├── services/             # Business logic
│   └── listeners/        # Transactional event listeners (BookingMessageListener)
├── repositories/         # Spring Data JPA repositories
├── models/               # JPA entities
│   └── event/            # Application event POJOs (BookingRequestedEvent)
├── dto/                  # Request / response DTOs
│   ├── auth/
│   ├── booking/
│   ├── message/
│   ├── property/
│   ├── stats/
│   └── user/
├── security/             # JWT filter, STOMP auth interceptor, CORS, Spring Security config
├── config/               # Application configuration (WebSocket, Stripe)
├── scheduler/            # Nightly cleanup and booking status update jobs
├── util/                 # Helpers (CloudflareR2Service, PropertyMapper, BookingMessageTemplate)
└── exceptions/           # Custom exceptions and GlobalExceptionHandler
```

![High Level Diagram](./images/High_Level_Diagram.png)

---

## ⚙️ Core Features

### 1. Authentication & Authorization

- Register and log in with JWT tokens (24-hour expiry, HMAC-SHA256)
- Logout invalidates the token via a **server-side blacklist** checked on every request
- Three roles: **`ADMIN`**, **`OWNER`** (host), **`RENTER`**
- Renters can upgrade themselves to the OWNER role via `PATCH /api/users/`
- Stateless session management — no server-side sessions

### 2. Property Listings

- Owners can create, update, and delete properties
- Each property includes: title, description, price, type (`APARTMENT`, `HOUSE`, `VILLA`, `CABIN`), address, city, guest/bed/bath counts, and multiple images
- Images are uploaded to **Cloudflare R2** (S3-compatible) and served via a public CDN URL
- Properties have an `ACTIVE` / `INACTIVE` status; only active properties appear in public listings
- Renters can save properties to a **Favourites** list
- Public property listing supports filtering by address/city, date availability, guest count, and price range
- Paginated results (10 per page)

### 3. Transactional Booking Workflow

Two booking modes are supported with distinct flows:

#### Mode A — Instant Pay (`POST /api/bookings/{propertyId}/book`)

1. **Availability check** — a **pessimistic write lock** (`SELECT FOR UPDATE`) is acquired on the property row. Both `CONFIRMED` bookings and unexpired `PENDING_*` bookings (within their TTL) are treated as conflicts.
2. **Booking created** — status set to `PENDING_PAYMENT` with a **10-minute TTL**. Expired bookings are automatically excluded from future conflict checks.
3. **Payment Intent created** — via `POST /api/payments/{bookingId}` (requires `Idempotency-Key` header). A Stripe `PaymentIntent` is generated and the `client_secret` is returned to the frontend.
4. **Stripe webhook confirms payment** — Stripe sends `payment_intent.succeeded` to `POST /api/webhooks/stripe`. The backend verifies the webhook signature, validates the exact amount received against the stored booking balance, and atomically transitions the booking to `CONFIRMED`.
5. **Real-time notification** — the updated booking status is pushed to the renter instantly via WebSocket.
6. **Conversation created** — after the booking transaction commits, a `BookingRequestedEvent` triggers `BookingMessageListener`, which creates a conversation with an initial booking summary message between the renter and property owner.

#### Stripe Integration

- **PaymentIntent creation** is idempotency-safe — retrying with the same `Idempotency-Key` returns the cached response without creating a duplicate.
- **Webhook signature verification** uses `Webhook.constructEvent()` with the `Stripe-Signature` header and the webhook signing secret, rejecting tampered or replayed events.
- **Amount validation** — the amount received from Stripe (in the smallest currency unit, PHP centavos) is compared against the stored booking balance. A mismatch throws an `IllegalStateException` and halts confirmation.
- **Idempotent webhook handling** — `payment_intent.succeeded` is only processed once. Duplicate deliveries are safe because the booking's `paymentStatus` is checked before any mutation.

```
POST /api/bookings/{propertyId}/book
  └─ Pessimistic lock on property row
  └─ Overlap check (CONFIRMED + unexpired PENDING_* bookings)
  └─ BookingEntity saved → status: PENDING_PAYMENT (TTL: 10 min)
  └─ BookingRequestedEvent published (after commit)
       └─ Conversation + initial summary message created

POST /api/payments/{bookingId}            [Idempotency-Key required]
  └─ Stripe PaymentIntent created
  └─ client_secret returned to frontend

POST /api/webhooks/stripe                 [Stripe-Signature verified]
  └─ payment_intent.succeeded received
  └─ Amount validated against booking balance
  └─ Booking → CONFIRMED, PaymentStatus → PAID
  └─ WebSocket push to renter
```

### 4. Booking Lifecycle (State Machine)

```
book()
  └─ PENDING_PAYMENT ──► CONFIRMED (via Stripe webhook)
                   └────► EXPIRED   (10-min TTL elapsed, auto-excluded)
                   └────► CANCELLED (renter cancels)
  CONFIRMED ──────────► INPROGRESS (scheduler, every 10 min)
  INPROGRESS ─────────► COMPLETED  (scheduler, every 10 min)
```

- **Renters** can cancel their own bookings at any time
- Expired `PENDING_PAYMENT` bookings are automatically excluded from conflict checks (no manual cleanup required)
- A scheduled job runs every 10 minutes to transition `CONFIRMED` → `INPROGRESS` and `INPROGRESS` → `COMPLETED`, pushing WebSocket updates to renters for each transition

### 5. Real-Time Booking Updates (WebSocket)

Booking status changes are pushed to the renter immediately using **STOMP over WebSocket** (with SockJS fallback), eliminating the need for polling.

#### Flow

1. The frontend connects to `/ws` using SockJS + STOMP, passing `Authorization: Bearer <token>` as a STOMP header.
2. `AuthChannelInterceptor` validates the JWT on the `CONNECT` command and attaches the user's identity to the STOMP session.
3. The client subscribes to `/user/my-bookings` to receive personal booking updates.
4. When Stripe confirms a payment (or the scheduler transitions a booking), the backend calls `SimpMessagingTemplate.convertAndSendToUser(userId, "/my-bookings", payload)`.

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
  ◄── { id, status } (JSON) ─────┤
```

| Property | Value |
|---|---|
| Endpoint | `/ws` (SockJS enabled) |
| App destination prefix | `/app` |
| User destination | `/user/my-bookings` |
| Broker destinations | `/my-bookings`, `/user` (in-memory simple broker) |
| Auth | JWT validated in `AuthChannelInterceptor` on STOMP `CONNECT` |
| Allowed origins | `http://localhost:5173`, `https://stelio-frontend.aaronbaon1.workers.dev` |

### 6. Idempotency

All mutating operations that could be retried (booking creation, payment intent generation) require an **`Idempotency-Key`** request header.

- On first receipt, a record is inserted with status `PENDING` and the operation executes. On success, the record is updated to `COMPLETED` with the serialised response stored as JSONB.
- On retry (duplicate key), a `DataIntegrityViolationException` is caught (unique constraint violation), the existing record is fetched, and the stored response is returned — identical to the original response.
- Keys expire after **24 hours** and are purged nightly by `CleanupScheduler`.

### 7. Messaging

- Renters and owners can exchange messages via conversations.
- A conversation with an initial booking summary message is automatically created when a `book()` request commits. This is handled by `BookingRequestedEvent`, published after the transaction commits and consumed by `BookingMessageListener` in a new transaction (`@TransactionalEventListener(phase = AFTER_COMMIT)`).
- Conversations track unread counts and mute status per participant.
- Conversations may optionally be linked to a review.

### 8. Reviews

- Reviews are linked to properties and optionally to a conversation thread.
- Owners can view review stats (average stars, individual messages) for their properties via the API.

### 9. Owner Dashboard & Statistics

The stats endpoints (`/api/stats`) give owners a live overview of their portfolio:

- **Total revenue** (all time, from `COMPLETED` bookings)
- **Current month revenue** with month-over-month comparison percentage
- **Occupancy rate** (nights booked vs. days available since listing)
- **Active bookings** count for the current month
- **Today's check-ins**
- **Top 3 properties** by revenue with per-property occupancy rate
- **Booking stats**: upcoming check-ins, next booking ETA, current guests, check-outs today, and a list of active bookings with renter details

### 10. Automated Scheduled Jobs

| Schedule | Task |
|---|---|
| Daily at 02:30 AM | Delete expired idempotency records |
| Daily at 02:30 AM | Purge expired blacklisted JWT tokens |
| Every 10 minutes | Transition `CONFIRMED` → `INPROGRESS` and push WebSocket update |
| Every 10 minutes | Transition `INPROGRESS` → `COMPLETED` and push WebSocket update |

### 11. File Storage

- Images and files are uploaded to **Cloudflare R2** using the AWS SDK v2 (S3-compatible API).
- Public files (property images) are served directly via the R2 public CDN URL.
- Private files (profile pictures, message attachments) are served through the backend at `/api/files/{id}`.
- The `CloudflareR2Service` automatically creates the bucket if it does not exist on first upload.

---

## 🗄️ Data Model

| Entity | Key Relationships |
|---|---|
| `users` | owns many `property`, has many `bookings`, `favorites`, `reviews`, `messages` |
| `property` | belongs to a `users` (host); has many `bookings`, `files`, `favorites`, `reviews`; one `propertyStats` |
| `bookings` | belongs to `property` and `users` (renter); indexed on `(property_id, startDateTime, endDateTime)`; holds `stripePaymentIntentId` for webhook reconciliation |
| `conversations` | has many `participants` and `messages`; optionally linked to one `review` |
| `messages` | sent by a `users`, belongs to a `conversation`, may have `files` |
| `files` | stored on Cloudflare R2; associated with either a `property`, a `message`, or a `user` (profile picture) |
| `propertyStats` | one-to-one with `property`; aggregates booking and earnings data |
| `blacklistedTokens` | stores revoked JWTs until their natural expiry |
| `idempotency` | stores processed idempotency keys with a 24-hour TTL (JSONB response cache) |

---

## 🔒 Security

| Concern | Approach |
|---|---|
| Authentication | JWT (HMAC-SHA256), 24-hour expiry, `Authorization: Bearer <token>` |
| Token revocation | Blacklist table checked on every authenticated request |
| Password storage | BCrypt hashing |
| Session management | Stateless (no server-side sessions) |
| Concurrency safety | Pessimistic write lock on overlapping booking query |
| Idempotency | Per-key idempotency record with unique constraint and 24-hour TTL |
| WebSocket auth | JWT validated in `AuthChannelInterceptor` on STOMP `CONNECT` |
| Stripe webhooks | Signature verified via `Webhook.constructEvent()` before processing |
| CORS | Configured for `http://localhost:5173` and the production Cloudflare Workers frontend |

**Public endpoints** (no authentication required):

- `POST /api/auth/login`
- `POST /api/auth/register`
- `GET /api/properties/`
- `GET /api/files/**`
- `POST /api/webhooks/stripe`
- `GET /ws/**` (WebSocket handshake; per-connection auth handled by the STOMP interceptor)

---

## 🌐 API Reference

### Auth — `/api/auth`

| Method | Path | Description |
|---|---|---|
| `POST` | `/login` | Authenticate and receive a JWT |
| `POST` | `/register` | Create a new user account |
| `POST` | `/logout` | Revoke the current JWT (blacklists token) |
| `POST` | `/verify` | Verify token and return user details |

### Users — `/api/users`

| Method | Path | Auth | Description |
|---|---|---|---|
| `PATCH` | `/` | RENTER | Upgrade role from RENTER → OWNER |
| `GET` | `/profile` | Auth | Get user profile and activity summary |
| `POST` | `/profile` | Auth | Upload profile picture (multipart/form-data) |

### Properties — `/api/properties`

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/` | Public | List active properties (paginated, filterable) |
| `GET` | `/my-properties` | OWNER | List the authenticated owner's properties |
| `GET` | `/{propertyId}` | Public | Property details with confirmed booking schedule |
| `GET` | `/{propertyId}/bookings` | OWNER | All bookings for a specific property |
| `POST` | `/` | OWNER | Create property (`multipart/form-data`) |
| `POST` | `/{propertyId}` | OWNER | Update property details and/or images |
| `DELETE` | `/{propertyId}` | OWNER | Delete property and all associated data |

**Query parameters for `GET /api/properties/`:**

| Param | Type | Description |
|---|---|---|
| `page` | Integer | Page number (default: 1) |
| `address` | String | Filter by address or city (partial match) |
| `start` | LocalDateTime | Availability filter start date |
| `end` | LocalDateTime | Availability filter end date |
| `minGuests` | Integer | Minimum guest capacity |
| `minPrice` | BigDecimal | Minimum nightly price |
| `maxPrice` | BigDecimal | Maximum nightly price |

### Bookings — `/api/bookings`

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/` | Auth | RENTER: own bookings; OWNER: all property bookings |
| `GET` | `/{bookingId}` | Auth | Get booking details |
| `POST` | `/{propertyId}/book` | RENTER | Instant pay booking (requires `Idempotency-Key` header) |
| `POST` | `/{propertyId}/reserve` | RENTER | Request-to-book (requires `Idempotency-Key` header) |
| `PATCH` | `/{bookingId}` | OWNER | Update booking status (approve/reject/noshow) |
| `PATCH` | `/{bookingId}/cancel` | RENTER | Cancel booking |

### Payments — `/api/payments`

| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/{bookingId}` | Auth | Generate Stripe PaymentIntent (requires `Idempotency-Key` header) |

### Webhooks — `/api/webhooks`

| Method | Path | Description |
|---|---|---|
| `POST` | `/stripe` | Receive Stripe events; confirms booking on `payment_intent.succeeded` and triggers WebSocket push |

### WebSocket — `/ws`

| Channel | Direction | Description |
|---|---|---|
| `CONNECT` with `Authorization` header | Client → Server | Authenticates the STOMP session via JWT |
| `/user/my-bookings` | Server → Client | Receives booking status updates (`{ id, status }`) on payment confirmation or scheduler transitions |

### Messages — `/api/messages`

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/` | Auth | List conversations (chat heads with latest message preview) |
| `POST` | `/` | Auth | Create a new conversation with another user |
| `GET` | `/{conversationId}` | Auth | Get all messages in a conversation |
| `POST` | `/{conversationId}` | Auth | Send a message in a conversation |

### Favourites — `/api/favorite`

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/{favoriteId}` | Auth | Check if a favourite record exists |
| `POST` | `/{propertyId}` | Auth | Add property to favourites |
| `DELETE` | `/{propertyId}` | Auth | Remove property from favourites |

### Reviews — `/api/property/review`

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/stats/{propertyId}` | OWNER | Get review stats and messages for a property |

### Stats — `/api/stats`

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/` | OWNER | Portfolio overview: revenue, occupancy, check-ins, top properties |
| `GET` | `/bookings` | OWNER | Booking stats: upcoming check-ins, current guests, checkout today, active bookings |

### Files — `/api/files`

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/{id}` | Public | Stream a private file from Cloudflare R2 by file ID |

---

## 💻 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5 |
| Security | Spring Security + JJWT 0.12.6 |
| Persistence | Spring Data JPA / Hibernate |
| Database | PostgreSQL |
| File Storage | Cloudflare R2 (AWS SDK v2 2.27.14) |
| Payments | Stripe (stripe-java 32.0.0) |
| Real-time | Spring WebSocket + STOMP (SockJS) |
| Validation | Jakarta Validation / Hibernate Validator |
| Utilities | Lombok, spring-dotenv |
| Build | Maven 3.9 (Maven Wrapper included) |

---

## 🚀 Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+
- PostgreSQL database
- Cloudflare R2 bucket (or any S3-compatible storage)
- Stripe account with a webhook endpoint configured to forward `payment_intent.succeeded` events to `POST /api/webhooks/stripe`

### Environment Variables

Create a `.env` file in the project root (already git-ignored):

```env
# Database
DB_URL=jdbc:postgresql://<host>:<port>/<database>
DB_USERNAME=<username>
DB_PASSWORD=<password>
DB_DRIVER=org.postgresql.Driver

# JWT
JWT_SECRET=<a-long-random-secret-at-least-32-chars>

# Cloudflare R2
CLOUDFLARE_R2_BUCKET_NAME=<bucket-name>
CLOUDFLARE_R2_ENDPOINT=https://<account-id>.r2.cloudflarestorage.com
CLOUDFLARE_R2_PUBLIC_URL=https://pub-<id>.r2.dev
CLOUDFLARE_R2_ACCESS_KEY=<r2-access-key>
CLOUDFLARE_R2_SECRET_KEY=<r2-secret-key>

# Stripe
STRIPE_SECRET_KEY=<stripe-secret-key>
STRIPE_WEBHOOK_KEY=<stripe-webhook-signing-secret>

# Server (optional, defaults to 8080)
PORT=8080
```

> **Stripe webhook setup:** In your Stripe dashboard, add a webhook endpoint pointing to `https://<your-domain>/api/webhooks/stripe` and subscribe to the `payment_intent.succeeded` event. Copy the signing secret into `STRIPE_WEBHOOK_KEY`.  
> For local development, use the [Stripe CLI](https://stripe.com/docs/stripe-cli):
> ```bash
> stripe listen --forward-to localhost:8080/api/webhooks/stripe
> ```

### Run Locally

```bash
# Clone the repository
git clone https://github.com/shironzi/stelio-backend.git
cd stelio-backend

# Build and run
./mvnw spring-boot:run
```

The REST API will be available at `http://localhost:8080`.  
The WebSocket endpoint will be available at `ws://localhost:8080/ws`.

### Run Tests

```bash
./mvnw test
```

---
