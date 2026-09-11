# Web UI

## Pages and navigation

Thymeleaf pages are available at `/`, `/catalog`, `/product/{id}`, `/cart`, `/auth`, `/profile`, `/orders`, `/licenses` and `/admin`. The common header contains the catalog, cart, user menu, order and license links. The admin link is displayed only when the locally stored authenticated role is `ADMIN`.

## REST integration

The interface uses `Fetch API` and the shared `static/js/api.js` helper. The helper sends `Authorization: Bearer <JWT>`, parses JSON responses and `ApiError`, and clears the session plus redirects to `/auth` after `401`.

JWT and the small user profile are stored in `localStorage`. The cart is not stored in the browser: all cart operations use `/api/v1/cart` and its item endpoints.

## User scenarios

1. A visitor opens the catalog, searches products and opens a product card.
2. A registered user logs in, adds a product to the server-side cart, changes quantity and creates an order.
3. The user pays a `PENDING_PAYMENT` order and receives licenses in the profile.
4. An administrator manages products and categories, archives products, and cancels permitted orders.

## Validation and states

Login and registration validate email and a password of at least six characters in JavaScript. Cart quantity is validated from 1 to 99. API views show loading, empty, successful and error states; messages are displayed inline or in a non-blocking toast.

## Responsive layout and XSS

The UI uses one responsive stylesheet. Product grids and forms become single-column on mobile, and the navigation changes to a menu button.

API data is rendered through `document.createElement` and `textContent`; untrusted API values are not inserted through `innerHTML`. Product image URLs are restricted to `http(s)` or same-origin paths before assignment to `img.src`.
