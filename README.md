# Stelio

**Type:** Backend / Transactional System
**Focus:** Transactional safety, consistency, idempotency, state management

---

## 💡 Project Overview

A rental platform where users can list and rent properties while ensuring:

- **Strong consistency**
- **Transactional booking workflows**
- **Protection against duplicate rentals and race conditions**

Designed to demonstrate **robust system design principles** suitable for interviews.

---

## ⚙️ Core Features

### 1. Property Listings

- Owners can list and manage properties
- Each property includes location, price, and availability

### 2. Rent a Property (Transactional Workflow)

- Booking is handled atomically:
  1. Check availability
  2. Create temporary reservation (`PENDING_PAYMENT`)
  3. Simulate payment / coupon application
  4. Confirm booking only if payment succeeds

### 3. Prevent Double-Rent (Idempotency)

- Rent endpoint requires **idempotency key**
- Prevents duplicate bookings, double charges, and race conditions

### 4. Temporary Reservation with TTL

- Reservation expires automatically if payment isn’t confirmed (e.g., 10 minutes)
- Property becomes available again
- Ensures abandoned bookings do not block availability

### 5. Booking Lifecycle (State Machine)

- `PENDING_PAYMENT → CONFIRMED → CANCELLED / EXPIRED`
- Improves validation, auditability, and transactional correctness

### 6. User Roles

- **Owner:** List/manage properties
- **Renter:** Search and book properties

---

## 🔹 System Design Highlights

- **Transactional Integrity:** Booking and payment simulation are atomic
- **Concurrency Safety:** Idempotency and state checks prevent double-booking
- **Expiration Handling:** TTL reservations prevent stale locks
- **State Machine:** Explicit booking states for clarity and auditability

---

## 💻 Tech Stack

- **Backend:** Java / Spring Boot
- **Database:** MySQL / PostgreSQL
- **APIs:** RESTful endpoints for bookings, properties, and users
