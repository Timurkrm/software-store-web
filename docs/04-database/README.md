# Database

The schema is owned by Flyway. `V1__initial_schema.sql` creates the complete PostgreSQL schema on an empty database; Hibernate runs with `ddl-auto=validate` and must not create or alter tables.

## Tables and relationships

- `users` holds account identity and the `USER`/`ADMIN` role.
- `categories` normalizes product categories; its optional `description` is category metadata.
- `software_products` belongs to one category and is archived with `status`, rather than physically deleted.
- `carts` is a one-to-one extension of `users`; `cart_items` belongs to one cart and references the current product.
- `orders` belongs to a user. `order_items` references a product and retains only `unit_price` as the deliberate purchase-time price snapshot.
- `payments` references an order. A partial unique index allows at most one successful payment while retaining failed payment attempts for future retries.
- `licenses` ties the issued key to its user, product and order.

See [er-diagram.puml](er-diagram.puml) for the ER diagram.

## Integrity and deletion policy

Every relation has an explicit FK. All business-history relations use `ON DELETE RESTRICT`: deleting a user, category, product or order cannot silently remove orders, payments or licenses. Products must be archived instead of deleted.

Only `cart_items.cart_id` uses `ON DELETE CASCADE`, because a CartItem has no meaning without its Cart. This is consistent with JPA `cascade = ALL` and `orphanRemoval = true` on `Cart.items`.

Unique constraints protect `users.email`, `categories.name`, one cart per user, a single product per cart, a single product per order and every license key. Quantity, amount, price and enum values are protected by CHECK constraints.

## Indexes

- `ux_users_email_lower`, `ux_categories_name_lower`: match case-insensitive repository lookups and prevent case-only duplicates.
- Product indexes support catalog filtering by category/status and name lookup.
- `idx_orders_user_created_at`, `idx_orders_status`: order history and workflow queries.
- Item, license and payment indexes support the repository ownership/history lookups and foreign-key joins.
- `ux_payments_successful_order` is a partial unique index for successful payment idempotency while allowing failed retries.

## Normalization

The model is in 3NF: user and category facts are stored once and referenced by keys; CartItem does not duplicate product price. `order_items.unit_price` is an intentional historical snapshot, so later product-price changes do not change closed orders.

## ORM and time

JPA uses UUID identifiers, `EnumType.STRING`, and LAZY relations for to-one links and collections. `LocalDateTime` maps to PostgreSQL `timestamp without time zone`; the application assumes the deployment/server timezone for these values. A future API time-contract can move public audit timestamps to `Instant`/`timestamptz` without changing the current domain model.

## Flyway and existing data

`V2__migrate_customer_role_to_user.sql` converts legacy `CUSTOMER` values to `USER` without deleting users. It is applied after V1 on Flyway-managed databases. A pre-Flyway Hibernate database must be backed up and reconciled/baselined before enabling Flyway; it must not be pointed at V1 as if it were empty.

## N+1 watch list

`CheckoutFacade.history`, `LicenseMediator.findByUserEmail`, `ProductMediator.all`, and `CartMediator.getCart` traverse lazy associations while building DTOs. They should be reviewed during the REST API/performance stage for EntityGraph, JOIN FETCH or DTO projections when list sizes grow.
