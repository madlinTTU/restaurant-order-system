# Restaurant Order System

> **Portfolio project** - event-driven order management system built with Spring Boot 4 + React 19, demonstrating Kafka-based async communication, Redis caching, real-time WebSocket updates, and S3-compatible file storage.

![Java](https://img.shields.io/badge/Java_21-ED8B00?style=flat&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_4-6DB33F?style=flat&logo=spring&logoColor=white)
![React](https://img.shields.io/badge/React_19-20232A?style=flat&logo=react&logoColor=61DAFB)
![TypeScript](https://img.shields.io/badge/TypeScript-007ACC?style=flat&logo=typescript&logoColor=white)
![Kafka](https://img.shields.io/badge/Apache_Kafka-231F20?style=flat&logo=apachekafka&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat&logo=redis&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=flat&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker&logoColor=white)

## What This Is

A fullstack restaurant order management system where customers place orders, kitchen staff update statuses, and status changes propagate in real time via WebSocket - all powered by Kafka events under the hood.

Built as a portfolio project to work with Kafka, Redis, WebSocket and S3 in one codebase.

**Order lifecycle:**
```
PLACED → CONFIRMED → PREPARING → READY → PICKED_UP → DELIVERED
                                       ↘ CANCELLED
```
Each transition publishes a Kafka event. A consumer picks it up and pushes it out over WebSocket to whoever's subscribed - the customer tracking that order, and the kitchen dashboard watching everything.

## Architecture

```
┌─────────────────────────────────────────────────────┐
│                   React Frontend                     │
│         (TanStack Query + WebSocket/STOMP)           │
└──────────────────────┬──────────────────────────────┘
                       │ HTTP / WebSocket
┌──────────────────────▼──────────────────────────────┐
│              Spring Boot Application                 │
│                                                      │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────┐  │
│  │ Auth Module │  │ Order Module │  │ Menu Module│  │
│  │ JWT + Redis │  │ + Kafka pub  │  │ + S3 imgs  │  │
│  └─────────────┘  └──────┬───────┘  └────────────┘  │
│                          │                           │
│  ┌───────────────────────▼──────────────────────┐   │
│  │           Kafka (order-status-events)         │   │
│  └───────────────────────┬──────────────────────┘   │
│                          │                           │
│              ┌───────────▼───────────┐               │
│              │  WebSocket Publisher  │               │
│              │  (Kafka consumer)     │               │
│              └────────────────────────┘              │
└──────────────────────────────────────────────────────┘
         │                    │              │
    ┌────▼────┐          ┌────▼────┐   ┌────▼────┐
    │PostgreSQL│          │  Redis  │   │ S3/MinIO │
    └──────────┘          └─────────┘   └─────────┘
```

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21 + Spring Boot 4 |
| Auth | Spring Security + JWT (access + refresh via Redis, blacklist on logout) |
| Database | PostgreSQL + Liquibase migrations |
| Messaging | Apache Kafka |
| Cache | Redis (menu cache, refresh tokens, JWT blacklist) |
| File Storage | AWS S3 (presigned URLs) / MinIO locally |
| Frontend | React 19 + TypeScript + Vite + Tailwind CSS |
| UI Components | shadcn/ui |
| Data Fetching | TanStack Query (React Query) |
| Real-time | WebSocket (STOMP) |
| API Docs | springdoc-openapi (Swagger UI) |
| Testing | JUnit 5, Mockito, Testcontainers |
| Containerization | Docker Compose |

Swagger UI, once the backend is running: `http://localhost:8080/swagger-ui/index.html`.

## Roles

| Role | Capabilities |
|---|---|
| **Customer** | Browse menu, place orders, track status in real time |
| **Kitchen Staff** | View incoming orders, update status through the lifecycle |
| **Admin** | Manage menu categories/items with photo uploads and drag-and-drop ordering, manage users |

## Run in One Command

No need to clone anything. Grab `docker-compose.demo.yml` from the [latest release](https://github.com/madlinTTU/restaurant-order-system/releases/latest) and run:

```bash
docker compose -f docker-compose.demo.yml up
```

Postgres, Kafka, Redis, MinIO, backend and frontend all come up as containers, images pulled from GHCR. Once healthy, open **http://localhost:3000**.

Demo accounts (seeded automatically):

| Role | Email | Password |
|---|---|---|
| Admin | admin@demo.com | admin123 |
| Kitchen | kitchen@demo.com | kitchen123 |
| Customer | customer@demo.com | customer123 |

## Available Now

Everything in the [latest release](https://github.com/madlinTTU/restaurant-order-system/releases/latest): auth, menu management with photo uploads and drag-and-drop ordering, cart and checkout, real-time order tracking, kitchen dashboard, admin panel (categories/items/users), Swagger docs, and the one-command demo above.

Desktop only for now - no mobile layouts yet.

## Roadmap

Ideas for where this could go next, not commitments - some go beyond what a single-restaurant app needs, since part of the goal here is practicing the tools involved:

- **Delivery role** - a courier view that moves orders from `READY` to `PICKED_UP`/`DELIVERED`
- **Customer support role** - handles customer issues and order disputes directly, separate from kitchen and admin
- **Multi-restaurant support** - move from one restaurant to many, each with its own menu, staff and orders
- **Notification and payment services** - order notifications (email/push) and payment processing (Stripe/YooKassa), promo codes
- **Splitting into microservices** - `notification-service`, `payment-service`, `delivery-service` once the domain boundaries are proven out inside the monolith
- **Public deploy** - not decided yet whether this needs a live instance beyond the Docker demo

Long-term direction, if pursued: less "order system for one restaurant" and more a small multi-vendor food delivery platform, in the spirit of Wolt or Bolt Food.
