# URL Shortener — Production-Grade Full Stack Application

A Bitly-style URL shortener built with **Spring Boot 3**, **PostgreSQL**, **Redis**, **JWT authentication**, and a **Next.js 15** dashboard with analytics, QR codes, and dark mode.

![Architecture](docs/screenshots/architecture-placeholder.png)

> Screenshot placeholders: add `docs/screenshots/` images after running the app (landing, dashboard, analytics).

---

## Features

### Users
- Register / login with JWT (access + refresh tokens)
- Create shortened URLs with optional custom codes and expiration
- Search, paginate, and sort links
- Update and delete links
- View click analytics (counts, recent clicks, country chart)
- Dashboard: total links, total clicks, top link, recent activity

### Admins
- List all users and URLs
- Disable malicious links

### Platform
- Redis caching for URL lookups
- Rate limiting (Bucket4j)
- Swagger/OpenAPI docs
- Docker Compose for local and production-like runs
- Unit, service, and controller tests (JUnit + Mockito)

---

## Architecture

```
┌─────────────┐     HTTPS/REST      ┌──────────────────┐
│  Next.js 15 │ ◄──────────────────►│  Spring Boot API │
│  Frontend   │                     │  (Java 21)       │
└─────────────┘                     └────────┬───────────┘
                                           │
                         ┌─────────────────┼─────────────────┐
                         ▼                 ▼                 ▼
                   PostgreSQL           Redis          Redirect /{code}
```

### Backend layers (`backend/src/main/java/com/urlshortener/`)

| Package        | Responsibility                          |
|----------------|-----------------------------------------|
| `controller`   | REST endpoints, redirect handler        |
| `service`      | Business logic, caching, analytics      |
| `repository`   | Spring Data JPA                         |
| `entity`       | Users, URLs, UrlAnalytics               |
| `dto`          | Request/response contracts              |
| `security`     | JWT filter, UserDetails, roles          |
| `config`       | Security, Redis, CORS, Swagger, rate limit |
| `exception`    | Global exception handler                |
| `util`         | Short code generator, URL validator     |

### Frontend (`frontend/src/`)

| Path              | Description                    |
|-------------------|--------------------------------|
| `app/`            | Pages (landing, auth, dashboard) |
| `components/`     | UI, navbar, QR, copy button    |
| `lib/api.ts`      | Axios + React Query hooks      |
| `lib/store.ts`    | Zustand auth persistence       |

---

## Database Schema

See [`backend/src/main/resources/schema.sql`](backend/src/main/resources/schema.sql).

| Table           | Key columns                                              |
|-----------------|----------------------------------------------------------|
| `users`         | id, username, email, password, role, created_at          |
| `urls`          | id, original_url, short_code, click_count, created_by, expires_at, disabled |
| `url_analytics` | id, url_id, ip_address, user_agent, country, clicked_at  |

Hibernate `ddl-auto: update` applies schema on startup.

---

## Quick Start (Docker)

**Prerequisites:** Docker & Docker Compose

```bash
git clone <your-repo-url>
cd url-shortener-springboot
cp .env.example .env
docker compose up --build
```

| Service    | URL                                      |
|------------|------------------------------------------|
| Frontend   | http://localhost:3000                    |
| Backend    | http://localhost:8080                    |
| Swagger UI | http://localhost:8080/swagger-ui.html    |
| PostgreSQL | localhost:5432 (user/pass: postgres)   |
| Redis      | localhost:6379                           |

**Default admin** (created on first startup):

- Username: `admin`
- Password: `Admin@12345`

---

## Local Development (without Docker)

### 1. PostgreSQL & Redis

```bash
# PostgreSQL: create database
createdb urlshortener

# Redis: run locally on 6379
redis-server
```

### 2. Backend

```bash
cd backend
# Requires Java 21 and Maven
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/urlshortener
export JWT_SECRET=your-256-bit-secret-key-change-in-production-must-be-at-least-32-chars
mvn spring-boot:run
```

### 3. Frontend

```bash
cd frontend
cp .env.example .env.local
npm install
npm run dev
```

Open http://localhost:3000

---

## Environment Variables

Copy [`.env.example`](.env.example) to `.env` (root) and `frontend/.env.local`.

| Variable | Description | Default |
|----------|-------------|---------|
| `JWT_SECRET` | HMAC signing key (≥32 chars) | (see example) |
| `APP_BASE_URL` | Public API base for short links | `http://localhost:8080` |
| `APP_FRONTEND_URL` | CORS allowed origin | `http://localhost:3000` |
| `NEXT_PUBLIC_API_URL` | Frontend API target | `http://localhost:8080` |
| `ADMIN_USERNAME` / `ADMIN_PASSWORD` | Seed admin account | `admin` / `Admin@12345` |

---

## API Documentation

### Authentication

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/register` | No | Register user |
| POST | `/api/auth/login` | No | Login |
| POST | `/api/auth/refresh` | No | Refresh access token |

### URLs (Bearer token)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/urls/create` | Create short URL |
| GET | `/api/urls/my` | List user URLs (search, page, sort) |
| GET | `/api/urls/dashboard` | Dashboard stats |
| PUT | `/api/urls/{id}` | Update URL |
| DELETE | `/api/urls/{id}` | Delete URL |
| GET | `/api/urls/analytics/{id}` | Analytics for URL |

### Public redirect

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/{shortCode}` | 301 redirect + record analytics |

### Admin (`ROLE_ADMIN`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/users` | All users |
| GET | `/api/admin/urls` | All URLs |
| PATCH | `/api/admin/urls/{id}/disable` | Enable/disable URL |

**Interactive docs:** http://localhost:8080/swagger-ui.html

**Postman:** Import [`postman/Url-Shortener-API.postman_collection.json`](postman/Url-Shortener-API.postman_collection.json)

### Example: Create URL

```bash
curl -X POST http://localhost:8080/api/urls/create \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{"originalUrl":"https://example.com","title":"Example"}'
```

---

## Testing

```bash
cd backend
mvn test
```

Tests include:
- `ShortCodeGeneratorTest`, `UrlValidatorTest` (unit)
- `AuthServiceTest`, `UrlServiceTest` (service)
- `AuthControllerTest` (controller)

---

## Deployment

### Backend — Render / Railway

1. Add **PostgreSQL** and **Redis** add-ons (or external URLs).
2. Set environment variables from `.env.example`.
3. Build command: `cd backend && mvn -DskipTests package`
4. Start command: `java -jar target/url-shortener-1.0.0.jar`
5. Set `APP_BASE_URL` to your public API URL (e.g. `https://api.yourapp.com`).

### Frontend — Vercel

1. Import `frontend/` as the project root.
2. Set `NEXT_PUBLIC_API_URL` to your deployed backend URL.
3. Deploy; enable Node.js 20.

### Docker production tips

- Change `JWT_SECRET` and admin password.
- Use managed PostgreSQL/Redis.
- Put reverse proxy (nginx) in front for TLS.

---

## Screenshots (placeholders)

Add captures to `docs/screenshots/`:

| File | Page |
|------|------|
| `landing-placeholder.png` | Landing page |
| `dashboard-placeholder.png` | Dashboard |
| `analytics-placeholder.png` | Analytics |

---

## Project Structure

```
url-shortener-springboot/
├── backend/          # Spring Boot API
├── frontend/         # Next.js 15 UI
├── postman/          # API collection
├── docker-compose.yml
├── .env.example
└── README.md
```

---

## License

MIT — use freely for learning and production deployments.
