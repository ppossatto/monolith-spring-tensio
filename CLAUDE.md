# CLAUDE.md

Guidelines for Claude when working on this repository.

## Project

Tensio is a monolithic Spring MVC application with server-side rendering.

- Java 25, Spring Boot 4 (Spring Framework 7), Maven (use `./mvnw`)
- Freemarker views (`.ftlh`), HTMX for partial updates, Bootstrap and Chart.js via WebJars
- PostgreSQL with Flyway migrations, Spring Data JPA, MapStruct
- Spring Security with form login

## Language

- All code is written in English: class, method, variable and constant names, Javadoc, comments, log messages, commit messages and pull request descriptions.
- Conversations with the maintainer may be in Portuguese.

## Workflow

1. Never push to `main`. Every change goes to a dedicated branch (`feat/...`, `fix/...`, `refactor/...`, `docs/...`) and is delivered as a pull request against `main`.
2. New features are first delivered **without tests**. The maintainer pulls the diff into IntelliJ and tests it manually.
3. Once the maintainer confirms the feature works, write the tests in a follow-up change:
   - back end: unit tests and integration tests (Testcontainers);
   - front end: Playwright tests.
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
| `mapper`                | MapStruct mappers between entities and domain types.                           |
| `config`                | Spring configuration (security, JPA, controller advice).                       |

Rules:

- Domain code must not depend on `entity`.
- Every entity loaded from the database is converted to a domain object through a MapStruct mapper before any processing in the service. Entities never reach controllers or templates.
- Create domain types and mappers only when a feature actually uses the entity.
- JPQL constructor expressions must use the fully qualified name of a class that exists in `repository/projection`.
- Views live in `src/main/resources/templates`. HTMX partials are prefixed with `_` (for example `_transformers.ftlh`).
- Schema changes go through a new Flyway migration (`V<n>__description.sql`); never edit an applied migration.
