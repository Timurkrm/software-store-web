# SoftStore

Учебное веб-приложение для продажи программного обеспечения: каталог, корзина, заказ, demo-оплата и выдача лицензий.

## Технологии и архитектура

- Java 17, Spring Boot 3, Thymeleaf, Spring Security, JWT и BCrypt;
- PostgreSQL, Flyway, JPA/Hibernate, Docker Compose;
- REST API с OpenAPI/Swagger;
- PCMEF: Presentation → Control → Mediator → Entity → Foundation.

## Возможности

- регистрация и вход, роли `USER` и `ADMIN`;
- публичный каталог товаров и категорий;
- корзина, заказ, demo-оплата и лицензии;
- история заказов и лицензий пользователя;
- административное управление товарами, категориями и заказами.

## Требования

Java 17+, Maven 3.9+ и PostgreSQL 16+ для локального запуска. Для Docker-сценария нужен Docker Desktop.

## Быстрый запуск

### Docker

```bash
docker compose up -d --build
```

Приложение: http://localhost:8080/  
Swagger UI: http://localhost:8080/swagger-ui/index.html

Compose запускает профиль `dev`, PostgreSQL и demo-данные. При необходимости задайте `APP_JWT_SECRET`, `POSTGRES_PASSWORD`, `APP_SEED_ADMIN_EMAIL` и `APP_SEED_ADMIN_PASSWORD` в окружении перед запуском.

### Maven

Настройте `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` и `APP_JWT_SECRET`, затем выполните:

```bash
mvn spring-boot:run
```

## Проверки качества

```bash
mvn clean test
mvn checkstyle:check
mvn clean package
```

JaCoCo: 61.51% instruction coverage и 62.69% line coverage. Отчёт создаётся в `target/site/jacoco/index.html`.

## Deployment

Собирается executable WAR `target/software-store.war`: он запускается через `java -jar` и разворачивается во внешнем Tomcat 10+.

Подробности: [deployment docs](docs/10-deployment/README.md), [REST API](docs/05-design/rest-api.md), [tests](docs/08-testing/README.md), [refactoring](docs/09-refactoring/README.md), [руководство пользователя](docs/12-user-guide/README.md), [руководство администратора](docs/13-admin-guide/README.md) и [материалы к пояснительной записке](docs/14-final-report/course-report.md).
