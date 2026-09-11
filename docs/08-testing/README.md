# Testing

## Approach

The project uses focused JUnit 5 and Mockito unit tests for mediators and MockMvc tests for the public REST security boundary. Repositories and external infrastructure are mocked in unit tests; no real payment provider is used.

## Covered scenarios

- Registration, duplicate email, successful login and invalid password.
- Product creation, update and archive.
- Category creation, duplicate-name rejection, update and deletion conflict.
- Cart add/update/remove, quantity validation and server-side total calculation.
- Creating an order from a cart, empty cart rejection, server price snapshot, order ownership and duplicate/cancelled payment rejection.
- Successful payment flow and one license per purchased unit.
- MockMvc: public catalog APIs, protected cart, 401/403 JSON responses, invalid DTO and USER-to-ADMIN access denial.

## JaCoCo

The report is generated automatically during the `test` phase at `target/site/jacoco/index.html`.

Current aggregate coverage after `mvn clean test`:

- Instruction coverage: **61.51%** (2,659 / 4,323).
- Line coverage: **62.69%** (289 / 461).

Both figures exceed the course requirement of 40% application-code coverage.

## Commands

```bash
mvn clean test
mvn checkstyle:check
```
