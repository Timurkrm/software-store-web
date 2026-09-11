# SoftStore — интернет-магазин программного обеспечения

Учебное веб-приложение для продажи программного обеспечения. Пользователь может
просматривать каталог, зарегистрироваться, собрать серверную корзину, оформить
заказ, выполнить демонстрационную оплату и получить лицензии. Администратор
управляет каталогом и просматривает заказы.

Траектория Б — Web-разработка, СКФУ, 2026.

| Параметр | Значение |
| --- | --- |
| Автор | Курбанов Тимур Магомедович |
| Направление | 09.03.04 «Программная инженерия» |
| Архитектура | PCMEF: Presentation → Control → Mediator → Entity → Foundation |
| Поставка | Executable WAR, Docker Compose |

## Технологии

| Компонент | Технология |
| --- | --- |
| Backend | Java 17, Spring Boot 3.2, Spring MVC |
| Web UI | Thymeleaf, HTML, CSS, JavaScript, Fetch API |
| Database | PostgreSQL 16, Flyway |
| ORM | Spring Data JPA, Hibernate |
| Security | Spring Security, JWT, BCrypt, роли `USER` и `ADMIN` |
| API | REST API `/api/v1`, OpenAPI / Swagger UI |
| Качество | JUnit 5, Mockito, MockMvc, JaCoCo, Checkstyle |
| Инфраструктура | Maven, Docker, Docker Compose, WAR / Tomcat 10+ |

## Возможности

- публичный каталог программ, поиск и категории;
- регистрация и вход по JWT;
- роли `USER` и `ADMIN`;
- серверная корзина с расчётом итоговой суммы;
- создание заказа со snapshot-ценой в `OrderItem`;
- демонстрационная оплата и автоматическая выдача лицензий;
- история собственных заказов и лицензий;
- CRUD категорий и программ, архивирование товаров;
- административный просмотр заказов и допустимые смены статуса;
- адаптивные Thymeleaf-страницы с клиентской валидацией и Fetch API.

> Оплата является учебной демонстрацией: внешний платёжный провайдер не подключён.

## Структура проекта

```text
software-store-web/
├── src/
│   ├── main/
│   │   ├── java/ru/skfu/softwarestore/
│   │   │   ├── presentation/    # MVC и REST-контроллеры
│   │   │   ├── control/         # Интерфейсы сервисов
│   │   │   ├── mediator/        # Бизнес-правила и CheckoutFacade
│   │   │   ├── entity/          # JPA-сущности и перечисления
│   │   │   ├── foundation/      # Репозитории и dev seed
│   │   │   ├── security/        # JWT и Spring Security
│   │   │   └── dto/             # Request/response DTO
│   │   ├── resources/
│   │   │   ├── db/migration/    # Flyway SQL-миграции
│   │   │   ├── static/          # CSS и JavaScript
│   │   │   └── templates/       # Thymeleaf-страницы
│   │   └── test/                # Unit и MockMvc-тесты
├── docs/                        # Проектная документация
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

## Быстрый старт

### Docker

1. При необходимости скопируйте `.env.example` в `.env` и замените примерные
   значения секретов.
2. Запустите приложение и PostgreSQL:

```bash
docker compose up -d --build
docker compose ps
```

После запуска:

- приложение: <http://localhost:8080/>;
- каталог: <http://localhost:8080/catalog>;
- Swagger UI: <http://localhost:8080/swagger-ui/index.html>;
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>.

Docker Compose по умолчанию использует профиль `dev`: создаются шесть
категорий и двенадцать демонстрационных продуктов. Администратор создаётся
только если заданы обе переменные `APP_SEED_ADMIN_EMAIL` и
`APP_SEED_ADMIN_PASSWORD`.

Полезные команды:

```bash
docker compose logs -f app
docker compose logs -f db
docker compose down
```

### Maven

Требуются Java 17+, Maven 3.9+ и доступная PostgreSQL 16+. Укажите параметры
подключения и JWT-секрет через окружение:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/software_store"
$env:SPRING_DATASOURCE_USERNAME="software_user"
$env:SPRING_DATASOURCE_PASSWORD="software_pass"
$env:APP_JWT_SECRET="replace-with-a-long-random-secret-at-least-32-characters"
mvn spring-boot:run
```

Для PowerShell приведены команды выше; в Bash используйте `export` вместо
`$env:`. Значения по умолчанию предназначены только для локальной разработки.

## REST API

Основной API имеет префикс `/api/v1`. Сущности не возвращаются напрямую:
контроллеры используют DTO и валидацию входных данных.

| Группа | Основные маршруты | Доступ |
| --- | --- | --- |
| Auth | `POST /auth/register`, `POST /auth/login` | Публичный |
| Products | `GET /products`, `GET /products/search`, `GET /products/{id}` | Публичный |
| Products | `POST`, `PUT`, `DELETE /products/{id}` | `ADMIN` |
| Categories | `GET /categories`, `GET /categories/{id}` | Публичный |
| Categories | `POST`, `PUT`, `DELETE /categories/{id}` | `ADMIN` |
| Cart | `GET /cart`, `POST /cart/items`, `PUT` / `DELETE /cart/items/{id}` | JWT |
| Orders | `POST /orders`, `GET /orders`, `GET /orders/{id}`, `POST /orders/{id}/pay` | Владелец + JWT |
| Licenses | `GET /licenses` | Владелец + JWT |
| Admin orders | `GET /admin/orders`, `PUT /admin/orders/{id}/status` | `ADMIN` |

JWT передаётся в заголовке:

```http
Authorization: Bearer <token>
```

Полное описание: [REST API](docs/05-design/rest-api.md) и
[OpenAPI / Swagger](docs/05-design/openapi.md).

## Архитектура PCMEF

```text
Presentation → Control → Mediator → Entity → Foundation
```

- **Presentation** — Thymeleaf и REST-контроллеры, HTTP, DTO, validation;
- **Control** — контракты `IUserService`, `IProductService`, `ICategoryService`,
  `ICartService`, `IOrderService`, `IPaymentService`, `ILicenseService`;
- **Mediator** — сценарии и бизнес-правила; `CheckoutFacade` координирует
  создание заказа, оплату и лицензии;
- **Entity** — доменная JPA-модель: `User`, `Category`, `SoftwareProduct`,
  `Cart`, `CartItem`, `Order`, `OrderItem`, `Payment`, `License`;
- **Foundation** — Spring Data repositories, JPA-запросы, Flyway и dev seed.

Подробнее: [архитектурная документация](docs/09-refactoring/README.md) и
[диаграммы проектирования](docs/05-design/api-sequence.puml).

## Проверки качества

```bash
mvn clean test
mvn checkstyle:check
mvn clean package
```

В последней зафиксированной проверке: 42 теста без failures/errors,
JaCoCo — 61,51% instruction coverage и 62,69% line coverage, Checkstyle —
0 violations. HTML-отчёт JaCoCo создаётся в `target/site/jacoco/index.html`.

## Развёртывание

Сборка создаёт executable WAR:

```bash
mvn clean package
java -jar target/software-store.war
```

Артефакт совместим с внешним Tomcat 10+. Для развёртывания пользовательского
интерфейса как корневого приложения разместите WAR в `webapps` под именем
`ROOT.war`, так как клиент использует абсолютные пути. Подробности:
[Docker](docs/10-deployment/docker.md) и [Tomcat](docs/10-deployment/tomcat.md).

## Документация

| Раздел | Материалы |
| --- | --- |
| База данных | [ER-диаграмма](docs/04-database/er-diagram.puml) |
| API и проектирование | [REST API](docs/05-design/rest-api.md), [OpenAPI](docs/05-design/openapi.md), [sequence diagrams](docs/05-design/api-sequence.puml) |
| Web UI | [описание интерфейса](docs/07-ui/README.md) |
| Тестирование | [подход и покрытие](docs/08-testing/README.md) |
| Рефакторинг | [Facade, ORM и качество](docs/09-refactoring/README.md) |
| Deployment | [общая инструкция](docs/10-deployment/README.md) |
| Управление проектом | [WBS, Gantt, COCOMO](docs/11-project-management/README.md) |
| Руководства | [пользователь](docs/12-user-guide/README.md), [администратор](docs/13-admin-guide/README.md) |
| Итоговые материалы | [пояснительная записка](docs/14-final-report/course-report.md) |

## Лицензия

Учебный проект по дисциплине «Программная инженерия», СКФУ, 2026.
