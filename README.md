# 🍽️ Restaurant Order System

> **Portfolio project** — event-driven order management system built with Spring Boot 3 + React 18, demonstrating Kafka-based async communication, Redis caching, real-time WebSocket updates, and AWS S3 file storage.

![Java](https://img.shields.io/badge/Java_21-ED8B00?style=flat&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3-6DB33F?style=flat&logo=spring&logoColor=white)
![React](https://img.shields.io/badge/React_18-20232A?style=flat&logo=react&logoColor=61DAFB)
![TypeScript](https://img.shields.io/badge/TypeScript-007ACC?style=flat&logo=typescript&logoColor=white)
![Kafka](https://img.shields.io/badge/Apache_Kafka-231F20?style=flat&logo=apachekafka&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat&logo=redis&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=flat&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker&logoColor=white)

## What This Is

A fullstack restaurant order management system where customers place orders, kitchen staff update statuses, and status changes propagate in real time via WebSocket — all powered by Kafka events under the hood.

**Order lifecycle:**
```
PLACED → CONFIRMED → PREPARING → READY → PICKED_UP → DELIVERED
```
Each transition publishes a Kafka event consumed independently by the notification service and WebSocket gateway.

## Architecture

```
┌─────────────────────────────────────────────────────┐
│                   React Frontend                     │
│         (React Query + WebSocket/STOMP)              │
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
│  │           Kafka (order.events topic)          │   │
│  └──┬─────────────────────────────────────────┬─┘   │
│     │                                         │      │
│  ┌──▼──────────────┐              ┌───────────▼──┐  │
│  │ Notification    │              │  WebSocket   │  │
│  │ Consumer        │              │  Publisher   │  │
│  └─────────────────┘              └──────────────┘  │
└──────────────────────────────────────────────────────┘
         │                    │              │
    ┌────▼────┐          ┌────▼────┐   ┌────▼────┐
    │PostgreSQL│          │  Redis  │   │  AWS S3 │
    └──────────┘          └─────────┘   └─────────┘
```

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21 + Spring Boot 3 |
| Auth | Spring Security + JWT (access + refresh via Redis) |
| Database | PostgreSQL + Liquibase migrations |
| Messaging | Apache Kafka |
| Cache | Redis (`@Cacheable` + JWT blacklist) |
| File Storage | AWS S3 (presigned URLs) / MinIO locally |
| Frontend | React 18 + TypeScript + Tailwind CSS |
| Data Fetching | TanStack Query (React Query) |
| Real-time | WebSocket (STOMP) |
| Containerization | Docker Compose |

## Roles

| Role | Capabilities |
|---|---|
| **Customer** | Browse menu, place orders, track status in real time |
| **Kitchen Staff** | View incoming orders, update cooking status |
| **Admin** | CRUD menu items with photo uploads, view all orders |

## Running Locally

**Prerequisites:** Docker + Docker Compose

```bash
git clone https://github.com/madlinTTU/restaurant-order-system.git
cd restaurant-order-system
docker compose up
```

| Service | URL |
|---|---|
| Backend API | http://localhost:8080 |
| Frontend | http://localhost:5173 |
| MinIO Console | http://localhost:9001 |

> MinIO is used locally as a drop-in AWS S3 replacement (AWS SDK compatible).

## Project Structure

```
restaurant-order-system/
├── backend/                          # Spring Boot application
│   └── src/main/java/com/example/orders/
│       ├── auth/                     # JWT + Spring Security
│       ├── menu/                     # Menu CRUD + S3 uploads
│       ├── order/                    # Order lifecycle + Kafka producer
│       ├── kafka/                    # Consumers (notification, websocket)
│       ├── websocket/                # STOMP gateway
│       └── config/
├── frontend/                         # React 18 + TypeScript + Vite
│   └── src/
│       ├── pages/
│       ├── components/
│       ├── hooks/                    # useOrders, useMenu, useWebSocket
│       └── api/                      # React Query functions
├── docker-compose.yml                # PostgreSQL + Redis + Kafka + MinIO
└── docs/
    └── project-plan.md               # Full development plan
```

## Status

> 🚧 **In active development** — Week 1 of 4

| Week | Focus | Status |
|---|---|---|
| 1 | Foundation: Auth, Menu, S3, React setup | 🔄 In progress |
| 2 | Kafka events, WebSocket, Kitchen dashboard | ⏳ Planned |
| 3 | Tests (JUnit + Testcontainers), CI pipeline | ⏳ Planned |
| 4 | Deploy to Railway + Upstash | ⏳ Planned |

## Deploy

| Service | Platform |
|---|---|
| Backend + DB | Railway |
| Redis + Kafka | Upstash |
| File Storage | AWS S3 |
| Frontend | Vercel / Railway Static |

---

*Java · Spring Boot · Kafka · Redis · React · WebSocket · PostgreSQL · Docker*
