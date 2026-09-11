# Deployment

Приложение использует PostgreSQL, Flyway и executable WAR. Для демонстрации предпочтителен Docker Compose: он поднимает сервисы `app` и `db`, ждёт healthcheck PostgreSQL и запускает профиль `dev` с demo-данными.

- [Docker Compose](docker.md)
- [External Tomcat 10+](tomcat.md)

Основные переменные окружения: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `APP_JWT_SECRET`, `APP_SEED_ADMIN_EMAIL`, `APP_SEED_ADMIN_PASSWORD` и `SPRING_PROFILES_ACTIVE`.
