# Task Management API

Simple, production-ready Spring Boot 3.5 REST API to manage Users, Projects, and Tasks. Secured with Keycloak JWT, using Flyway migrations, JPA, and robust validation with consistent error envelopes.

<p align="center">
  <img alt="Java" src="https://img.shields.io/badge/Java-21-007396?logo=java" />
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-3.5.x-6DB33F?logo=springboot" />
  <img alt="Build" src="https://img.shields.io/badge/build-Gradle-green" />
  <a href="https://github.com/OriolJT/task_management_api/actions/workflows/ci.yml"><img alt="CI" src="https://github.com/OriolJT/task_management_api/actions/workflows/ci.yml/badge.svg" /></a>
  <a href="./badges/jacoco.svg"><img alt="Coverage" src="./badges/jacoco.svg" /></a>
  <a href="./DEPLOY.md"><img alt="Deploy Docs" src="https://img.shields.io/badge/Deploy-Docs-blue" /></a>
</p>

## Highlights

- ✨ Ownership enforcement (Users → Projects → Tasks)
- ✨ Keycloak JWT security with fine-grained authorities
- ✨ Flyway migrations + JPA with validation
- ✨ Full CI/CD with coverage badge + Docker + Helm

## Quick start

- Local build and tests
  - Windows PowerShell
    - Build + tests + coverage reports
      ```powershell
      .\gradlew.bat clean test
      ```
    - Reports
      - Unit/integration tests: `build/reports/tests/test/index.html`
      - Coverage (JaCoCo): `build/reports/jacoco/test/html/index.html`
  - Unix/macOS
    - Build + tests
      ```bash
      ./gradlew clean test
      ```

- Run with Docker Compose (app + Postgres + Keycloak)
  ```powershell
  docker-compose up --build
  ```
  - App: http://localhost:8080
  - Keycloak: http://localhost:8081

  Unix/macOS
  ```bash
  docker compose up --build
  ```

- OpenAPI UI
  - Swagger UI: http://localhost:8080/swagger-ui/index.html
  - JSON: http://localhost:8080/v3/api-docs

See also: [Deploy guide](./DEPLOY.md)

## Example

Quick peek at the API without opening Swagger:

```bash
curl -H "Authorization: Bearer <jwt>" \
  http://localhost:8080/api/projects?page=0&size=10
```

Example 200 response:

```json
[
  {
    "id": "a3d1c2b4-1234-4c8a-9ef0-abcde1234567",
    "name": "My First Project",
    "createdAt": "2025-08-01T12:34:56Z"
  }
]
```

Swagger UI preview

<p align="center">
  <img alt="Swagger UI" src="./docs/swagger-ui.svg" width="800" />
  <br/>
  <em>Illustrative preview of endpoints and a sample response</em>
  </p>

## Architecture

- Spring Boot resource server (Keycloak JWT)
- Packages
  - controller: REST endpoints with `@Validated`, pagination (page/size/sort), sanitized sorting, and pagination headers (Link + X-Total-Count)
  - service: business/ownership rules via `CurrentUserProvider` and scoped repository queries
  - repository: Spring Data JPA interfaces with owner/project scoping
  - dto + mapper: request/response payloads and conversions; defaults applied in mappers
  - entity: JPA models with Bean Validation (email, size ranges, `@FutureOrPresent`, etc.)
  - exception: global handler returns `ErrorResponse` with `fieldErrors` for validation and 409 for integrity conflicts
  - security: Keycloak JWT to authorities; controller-level `@PreAuthorize`

## Key behaviors

- Ownership
  - Users own Projects; Projects own Tasks
  - All reads/writes enforce owner/project scope in services

- Pagination & sorting
  - Controllers accept `page`, `size`, and repeated `sort=field,dir`
  - Sort fields are sanitized to an allow-list
  - Responses include RFC-5988 `Link` and `X-Total-Count` headers; body contains only page content

- Validation & errors
  - DTOs and Entities carry constraints
  - 400 with `fieldErrors` for method/param violations
  - 409 for integrity violations (e.g., duplicate email)

## Development

- Java 21 toolchain; Gradle wrapper included
- H2 used in tests; Postgres in Docker
- Minimal JSON logging capability available via Logstash encoder dependency

## Troubleshooting

- Build failures with tests usually include a link to the HTML test report; start there
- If Keycloak is external, set env vars to match your issuer/client:
  - `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI`, `APP_SECURITY_OAUTH2_CLIENT_ID` (the app also accepts `KEYCLOAK_ISSUER_URI`/`KEYCLOAK_CLIENT_ID` as a convenience)
- Database connection for Docker deploy is configured via envs in `docker-compose.yml`

## License

⚠️ This repository is for portfolio demonstration only. No license granted for production use.
