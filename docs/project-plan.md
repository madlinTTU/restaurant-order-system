# Restaurant Order System — Project Plan

> **Цель:** Портфолио-проект, демонстрирующий event-driven архитектуру, новые технологии (Kafka, Redis, AWS S3) и fullstack разработку (Spring Boot + React). Результат — живая демка на Railway + чистое GitHub репо.

---

## Стек

| Слой | Технология | Статус |
|---|---|---|
| Backend | Java 21 + Spring Boot 3 | ✅ Знаю |
| Auth | Spring Security + JWT | ✅ Знаю |
| Database | PostgreSQL | ✅ Знаю |
| Migrations | Liquibase | ✅ Знаю |
| Messaging | Apache Kafka | 🆕 Новое |
| Cache | Redis | 🆕 Новое |
| File Storage | AWS S3 | 🆕 Новое (есть опыт через работу) |
| Frontend | React 18 + TypeScript | 🔄 Укрепить |
| Data Fetching | React Query (TanStack) | 🆕 Новое |
| Real-time | WebSocket (STOMP) | 🆕 Новое |
| Styling | Tailwind CSS | ✅ Знаю |
| Containerization | Docker Compose | ✅ Знаю |
| CI | GitHub Actions | 🔄 Укрепить |
| Deploy | Railway + Upstash | 🆕 Новое |

---

## Домен и бизнес-логика

**Сценарий:** Система управления заказами для ресторана.

### Роли
- **Customer** — делает заказ, отслеживает статус в реальном времени
- **Kitchen Staff** — видит входящие заказы, меняет статус (готовит, готово)
- **Admin** — управляет меню, видит аналитику

### Жизненный цикл заказа (Kafka events)
```
PLACED → CONFIRMED → PREPARING → READY → PICKED_UP → DELIVERED
```

Каждый переход — событие в Kafka. Разные части системы реагируют независимо:
- **Order Service** — публикует события, хранит состояние в PostgreSQL
- **Notification Service** — слушает события, отправляет уведомления (Telegram Bot)
- **WebSocket Gateway** — слушает события, пушит обновления в браузер

---

## Архитектура (Monolith-first)

```
┌─────────────────────────────────────────────────────┐
│                   React Frontend                     │
│         (React Query + WebSocket client)             │
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
│  │ (Telegram)      │              │  (STOMP)     │  │
│  └─────────────────┘              └──────────────┘  │
└──────────────────────────────────────────────────────┘
         │                    │              │
    ┌────▼────┐          ┌────▼────┐   ┌────▼────┐
    │PostgreSQL│          │  Redis  │   │  AWS S3 │
    └──────────┘          └─────────┘   └─────────┘
```

---

## Redis — что кэшируем

| Ключ | Значение | TTL |
|---|---|---|
| `menu:items` | Список блюд JSON | 10 мин |
| `menu:item:{id}` | Одно блюдо | 10 мин |
| `order:status:{id}` | Текущий статус заказа | 5 мин |
| `jwt:refresh:{userId}` | Refresh token | 7 дней |
| `jwt:blacklist:{jti}` | Отозванный токен | до истечения |

---

## Kafka Topics

| Topic | Producer | Consumers |
|---|---|---|
| `order.events` | Order Service | Notification Consumer, WebSocket Publisher |

### Event Schema (JSON)
```json
{
  "eventId": "uuid",
  "orderId": "uuid",
  "customerId": "uuid",
  "status": "PREPARING",
  "timestamp": "2025-06-07T10:00:00Z",
  "payload": {}
}
```

---

## AWS S3

- Бакет: `restaurant-order-system-{env}`
- Папки: `menu/images/`, `orders/receipts/`
- Upload через presigned URL (фронт грузит напрямую в S3)
- Сжатие изображений перед загрузкой (WebP)

---

## База данных (PostgreSQL)

```sql
-- users
id, email, password_hash, role (CUSTOMER/KITCHEN/ADMIN), created_at

-- menu_categories
id, name, display_order

-- menu_items
id, category_id, name, description, price, image_url (S3), available, created_at

-- orders
id, customer_id, status, total_amount, delivery_address, notes, created_at, updated_at

-- order_items
id, order_id, menu_item_id, quantity, unit_price

-- order_events
id, order_id, status, created_at, metadata (JSONB)
```

---

## React Frontend — страницы

| Страница | Роль | Фичи |
|---|---|---|
| `/` | Public | Меню, добавить в корзину |
| `/order/track/:id` | Customer | Real-time статус через WebSocket |
| `/kitchen` | Kitchen | Список активных заказов, смена статуса |
| `/admin/menu` | Admin | CRUD меню + загрузка фото в S3 |
| `/admin/orders` | Admin | Все заказы, фильтры, аналитика |

### React — что отработать
- **React Query** — кэширование запросов, invalidation после мутаций
- **WebSocket** — STOMP подключение, подписка на топик заказа
- **Lazy loading** — `React.lazy` + `Suspense` для роутов
- **Оптимистичные обновления** — UI меняется до ответа сервера
- **Code splitting** — бандл по роутам

---

## GitHub стратегия

### Ветки
```
main          ← только рабочий, задеплоенный код
develop       ← интеграция фич
feature/*     ← feature/kafka-order-events
fix/*         ← fix/redis-cache-invalidation
```

### Коммиты (Conventional Commits)
```
feat: add Kafka producer for order events
fix: resolve Redis TTL misconfiguration
chore: add Docker Compose for local Kafka
docs: update API endpoints in README
test: add integration tests for order service
```

### GitHub Actions
```yaml
# .github/workflows/ci.yml
# Триггер: push в develop, PR в main
# Steps: build → test → docker build
```

### README должен содержать
- Описание проекта (1 абзац)
- Архитектурная схема (ASCII или draw.io)
- Как запустить локально (одна команда: `docker compose up`)
- Стек с иконками
- Скриншоты / GIF демо
- Ссылка на живую демку (Railway)

---

## Бесплатный деплой

| Сервис | Что | Free tier |
|---|---|---|
| Railway | Spring Boot app | $5 кредитов/мес |
| Railway | PostgreSQL | Включено |
| Upstash | Redis | 10K команд/день |
| Upstash | Kafka | 10K сообщений/день |
| AWS S3 | File storage | 5 GB / 12 мес |
| GitHub Actions | CI | 2000 мин/мес |

---

## План по неделям

### Неделя 1 — Foundation
**Цель:** проект запускается, авторизация работает, базовое меню есть

- [ ] Инициализация Spring Boot проекта (Spring Initializr)
- [ ] Docker Compose: PostgreSQL + Redis + Kafka (локально)
- [ ] Liquibase миграции: users, menu_categories, menu_items, orders
- [ ] Spring Security + JWT (access + refresh tokens)
- [ ] Redis для хранения refresh tokens и blacklist
- [ ] REST API: Auth (register/login/logout/refresh)
- [ ] REST API: Menu CRUD (admin)
- [ ] AWS S3: загрузка изображений блюд
- [ ] React: настройка проекта (Vite + TypeScript + Tailwind + React Router)
- [ ] React: страница меню с React Query
- [ ] React: форма логина/регистрации
- [ ] Conventional commits с первого дня

**Milestone:** `docker compose up` → можно залогиниться и посмотреть меню с фото

---

### Неделя 2 — Kafka + Real-time
**Цель:** заказ создаётся, проходит через статусы, фронт обновляется в реальном времени

- [ ] REST API: создать заказ (Order Service)
- [ ] Kafka Producer: публиковать событие при каждом изменении статуса
- [ ] Kafka Consumer: Notification (Telegram Bot — опционально)
- [ ] WebSocket (STOMP): сервер пушит статус заказа
- [ ] React: страница трекинга заказа с WebSocket подпиской
- [ ] React: Kitchen dashboard (список заказов, кнопки смены статуса)
- [ ] Redis: кэш статусов заказов
- [ ] Сохранение всех событий в `order_events` таблицу

**Milestone:** заказ создан → статус меняется на кухне → браузер клиента обновляется без перезагрузки

---

### Неделя 3 — Polish + Testing
**Цель:** проект готов к демонстрации, тесты написаны, README заполнен

- [ ] Unit тесты: Order Service, Menu Service (JUnit 5 + Mockito)
- [ ] Integration тесты: API endpoints (Spring Boot Test + Testcontainers)
- [ ] Kafka integration тест (Embedded Kafka или Testcontainers)
- [ ] React: Admin страница заказов с фильтрами
- [ ] React: lazy loading роутов
- [ ] React: оптимистичные обновления при смене статуса
- [ ] Error handling: глобальный @ControllerAdvice
- [ ] Логирование: SLF4J + структурированные логи
- [ ] README: финальная версия с архитектурой и скриншотами
- [ ] GitHub Actions: CI pipeline

**Milestone:** `mvn test` — всё зелёное. README объясняет проект за 2 минуты.

---

### Неделя 4 — Deploy
**Цель:** живая демка, проект можно показать работодателю

- [ ] Настройка Railway: Spring Boot + PostgreSQL
- [ ] Настройка Upstash: Redis + Kafka
- [ ] Environment variables: application-prod.yml
- [ ] AWS S3: production bucket + IAM роль с минимальными правами
- [ ] Deploy frontend (Railway Static / Vercel)
- [ ] Финальный прогон: полный flow от регистрации до delivered заказа
- [ ] GIF/видео демо для README
- [ ] Добавить проект в CV и LinkedIn

**Milestone:** живая ссылка, которую можно вставить в резюме

---

## Порядок изучения новых технологий

### Kafka (Неделя 2)
1. Понять: топики, партиции, consumer groups (30 мин теории)
2. Поднять локально через Docker Compose
3. Написать простой Producer → Consumer тест
4. Интегрировать в Order Service

### Redis (Неделя 1)
1. Понять: key-value, TTL, типы данных (String, Hash)
2. Spring Data Redis + RedisTemplate
3. Начать с JWT blacklist (простой String key)
4. Добавить кэш меню через @Cacheable

### AWS S3 (Неделя 1)
1. Создать бакет + IAM user с минимальными правами
2. AWS SDK v2 для Java
3. Presigned URL для загрузки с фронта
4. Локально: MinIO как замена S3 (бесплатно, полностью совместимо)

### React Query (Неделя 1)
1. useQuery для GET запросов
2. useMutation для POST/PUT/DELETE
3. Invalidation: обновить меню после добавления блюда
4. Optimistic updates: неделя 3

---

## Структура репо

```
restaurant-order-system/
├── backend/
│   ├── src/
│   │   ├── main/java/com/example/orders/
│   │   │   ├── auth/
│   │   │   ├── menu/
│   │   │   ├── order/
│   │   │   ├── kafka/
│   │   │   ├── websocket/
│   │   │   ├── storage/          # S3
│   │   │   ├── cache/            # Redis
│   │   │   └── config/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── db/changelog/     # Liquibase
│   ├── Dockerfile
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── pages/
│   │   ├── components/
│   │   ├── hooks/                # useOrders, useMenu, useWebSocket
│   │   ├── api/                  # React Query functions
│   │   └── types/
│   ├── Dockerfile
│   └── package.json
├── docker-compose.yml            # PostgreSQL + Redis + Kafka + Zookeeper
├── docker-compose.prod.yml       # Production overrides
└── README.md
```

---

## Docker Compose (локальная разработка)

```yaml
services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: restaurant
      POSTGRES_USER: app
      POSTGRES_PASSWORD: secret
    ports:
      - "5432:5432"

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    depends_on: [zookeeper]
    ports:
      - "9092:9092"
    environment:
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"

  minio:
    image: minio/minio
    ports:
      - "9000:9000"
      - "9001:9001"
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin
```

> MinIO — локальная замена AWS S3. Полностью совместим с AWS SDK. Используй локально, AWS S3 только на проде.

---

*Создано: 2026-06-07 | Статус: Planning*
