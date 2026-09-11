# Docker Compose

## Requirements

Install Docker Desktop and run commands from the project root.

```bash
docker compose up -d --build
docker compose ps
```

The application is available at `http://localhost:8080/`; Swagger UI is at `http://localhost:8080/swagger-ui/index.html`.

Compose exposes PostgreSQL on port `5432` and the application on port `8080` by default. PostgreSQL has a `pg_isready` healthcheck; `app` starts only after it becomes healthy.

## Configuration

Compose defaults are intended for local demonstration. Override them through environment variables before startup:

Copy `.env.example` to `.env` and replace its placeholder values; `.env` is excluded from version control.

```bash
APP_JWT_SECRET=replace-with-a-long-random-secret
POSTGRES_PASSWORD=replace-db-password
APP_SEED_ADMIN_EMAIL=admin@example.test
APP_SEED_ADMIN_PASSWORD=strong-password
docker compose up -d --build
```

`SPRING_PROFILES_ACTIVE` defaults to `dev`; this enables idempotent demo categories and products. Use another profile for production-like deployment. The administrator seed is created only when both seed variables are set.

## Operations

```bash
docker compose logs -f app
docker compose logs -f db
docker compose restart app
docker compose down
```

`docker compose down` preserves the named PostgreSQL volume. Use `docker compose down -v` only when deliberately removing local database data.
