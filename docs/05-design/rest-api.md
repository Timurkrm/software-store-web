# REST API

Основной REST API доступен по префиксу `/api/v1`. Он использует JSON и JWT-аутентификацию.

## Authentication and authorization

Регистрация и вход доступны без токена: `POST /api/v1/auth/register` и `POST /api/v1/auth/login`.
После входа клиент передаёт полученный JWT в заголовке:

```http
Authorization: Bearer <token>
```

`ROLE_USER` управляет собственной корзиной, заказами и лицензиями. `ROLE_ADMIN` дополнительно управляет товарами и категориями и просматривает admin-заказы. Публичными являются чтение каталога и категорий.

## Main endpoints

| Group | Endpoints |
|---|---|
| Auth | `POST /auth/register`, `POST /auth/login` |
| Products | `GET /products`, `GET /products/{id}`, `GET /products/search`, `POST/PUT/DELETE /products` (admin) |
| Categories | `GET /categories`, `GET /categories/{id}`, `POST/PUT/DELETE /categories` (admin) |
| Cart | `GET /cart`, `POST /cart/items`, `PUT/DELETE /cart/items/{itemId}`, `DELETE /cart` |
| Orders | `POST /orders`, `GET /orders`, `GET /orders/{id}`, `POST /orders/{id}/pay` |
| Licenses | `GET /licenses` |
| Admin orders | `GET /admin/orders`, `PUT /admin/orders/{id}/status` |

Все пути в таблице имеют префикс `/api/v1`.

## Status codes and errors

Обычные ответы используют `200`, создание — `201`, удаление — `204`. Некорректные DTO возвращают `400`, отсутствующие ресурсы — `404`, конфликт бизнес-правил — `409`.

Security-ошибки всегда JSON: `401 UNAUTHORIZED` при отсутствии или недействительности аутентификации и `403 FORBIDDEN` при недостаточной роли. Ошибка имеет единый вид:

```json
{
  "timestamp": "2026-01-01T10:00:00Z",
  "status": 403,
  "error": "FORBIDDEN",
  "message": "Access is denied",
  "path": "/api/v1/admin/orders"
}
```
