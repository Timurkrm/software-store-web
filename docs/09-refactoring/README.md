# Refactoring and code-quality notes

## Facade

`CheckoutFacade` is the project’s natural GoF Facade. It coordinates cart validation, order creation, server-side total calculation, payment processing and license issuance while preserving the PCMEF mediator boundary. No additional artificial patterns were introduced.

## Persistence patterns supplied by JPA/Hibernate

- **Data Mapper:** JPA entity mappings together with Spring Data repositories map domain objects to PostgreSQL records.
- **Identity Map:** Hibernate’s persistence context (`EntityManager`) guarantees a single managed object instance for an entity identity within a transaction.
- **Lazy Load:** JPA/Hibernate `LAZY` associations defer related-object loading. Mediator read methods that map lazy category or license-product data run in read-only transactions.

No manual `Map<UUID, Entity>` cache or custom persistence implementation is required.

## Checkstyle

The Maven Checkstyle plugin uses a deliberately small configuration (`checkstyle.xml`): no tab characters. It catches a basic, objective formatting defect without imposing enterprise-scale rules or forcing a mechanical rewrite of existing imports in this учебный project.

## Completed minimal refactoring

- Made the no-Bearer branch of `JwtAuthFilter` explicit: public requests pass to authorization rules without a filter-generated 401.
- Kept the existing PCMEF package dependencies unchanged.
- Added focused tests only for core business paths previously not covered by mediator tests.
