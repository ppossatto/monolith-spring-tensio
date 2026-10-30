# CLAUDE.md

Guidelines for Claude when working on this repository.

## Project

Tensio is a monolithic Spring MVC application with server-side rendering.

- Java 25, Spring Boot 4 (Spring Framework 7), Maven (use `./mvnw`)
- Freemarker views (`.ftlh`), HTMX for partial updates, Bootstrap and Chart.js via WebJars
- PostgreSQL with Flyway migrations, Spring Data JPA, MapStruct
- Spring Security with form login; users in the `app_user` table (BCrypt), `ADMIN` / `USER` roles

## Language

- All code is written in English: class, method, variable and constant names, Javadoc, comments, log messages, commit messages and pull request descriptions.
- Conversations with the maintainer may be in Portuguese.

## Documentation

- Write Javadoc whenever possible: every public and package-private class, record, interface, enum and method, plus non-obvious private methods and constants.
- Use Markdown documentation comments (`///`, JEP 467), as the existing code does; keep `@param`, `@return` and `@throws` tags.
- Document what the code does and why (units, nullability, ordering, side effects), not how it is implemented line by line.
- Record components are documented with `@param` on the record.

## Workflow

1. Never push to `main`. Every change goes to a dedicated branch (`feat/...`, `fix/...`, `refactor/...`, `docs/...`) and is delivered as a pull request against `main`.
2. New features are first delivered **without tests**. The maintainer pulls the diff into IntelliJ and tests it manually.
3. Once the maintainer confirms the feature works, write the tests in a follow-up change:
   - back end: unit tests and integration tests (Testcontainers);
   - front end: Playwright tests.
   - MockMvc integration tests use `@WebIntegrationTest`; Playwright tests extend `PlaywrightSupport` and are named `*BrowserIT`.
4. Keep changes small and focused; one concern per pull request.

## Architecture (MVC layers)

Base package: `com.ppossatto.tensio`

| Package                 | Responsibility                                                                 |
|-------------------------|--------------------------------------------------------------------------------|
| `controller`            | `@Controller` classes. Fill the `Model`, return a view name. No business logic. |
| `service`               | Use cases and orchestration. Concrete classes; no interface for a single implementation. |
| `domain`                | Business rules and domain types (records, enums, evaluators), including the objects placed in the `Model`. No JPA types. |
| `entity`                | JPA entities only (persistence mapping, no business logic).                    |
| `repository`            | Spring Data repositories.                                                      |
| `repository/projection` | Read-only query projections (JPQL constructor expressions).                    |
| `form`                  | Input DTOs of form submissions (JavaBeans with Bean Validation), used by controllers and services. |
| `mapper`                | MapStruct mappers between entities, domain types and forms.                    |
| `config`                | Spring configuration (security, JPA, controller advice).                       |

Rules:

- Domain code must not depend on `entity`.
- Every entity loaded from the database is converted to a domain object through a MapStruct mapper before any processing in the service. Entities never reach controllers or templates.
- Create domain types and mappers only when a feature actually uses the entity.
- No response DTOs: controllers put domain objects (or read-only projections) in the `Model`.
- Form submissions (POST) bind to an input DTO validated with `@Valid` (Bean Validation), never directly to a domain object or entity.
- Exception: if a `@RestController` (JSON endpoint) is added, it uses dedicated response DTOs as its public contract.
- JPQL constructor expressions must use the fully qualified name of a class that exists in `repository/projection`.
- Views live in `src/main/resources/templates`. HTMX partials are prefixed with `_` (for example `_table.ftlh`).
- Schema changes go through a new Flyway migration (`V<n>__description.sql`) in `db/migration`; never edit an applied migration.
- Dummy / sample data lives in `db/dev` and only runs with the `dev` profile. Versions are shared with `db/migration`, so pick a number no schema migration uses.

## Profiles and configuration

- `application.yaml`: settings for every environment (server, DGA limits).
- `dev`: local database and dummy data, including users (`admin@tensio.local` / `admin`).
- `test`: used by the test suite (Testcontainers); no dummy data, only the small fixture in `src/test/resources/db/test` (test users, TT-01..TT-12, TSA/TSB/TSC). Its expected numbers are described at the top of `V1001__test_data.sql` and in `TestData`; keep them in sync when the fixture changes.
- `tls`: HTTPS served by the application; combine with another profile (`dev,tls`). Certificates come from the `TLS_CERTIFICATE` / `TLS_PRIVATE_KEY` environment variables and are never committed.
