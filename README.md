# Task Management API

Simple, production-ready Spring Boot 3.5 REST API to manage Users, Projects, and Tasks. Secured with Keycloak JWT, using Flyway migrations, JPA, and robust validation with consistent error envelopes.

<p align="center">
  <img alt="Java" src="https://img.shields.io/badge/Java-21-007396?logo=java" />
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-3.5.x-6DB33F?logo=springboot" />
  <img alt="Build" src="https://img.shields.io/badge/build-Gradle-green" />
  <a href="https://github.com/OriolJT/task_management_api/actions/workflows/ci.yml"><img alt="CI" src="https://github.com/OriolJT/task_management_api/actions/workflows/ci.yml/badge.svg" /></a>
  <a href="./badges/jacoco.svg"><img alt="Coverage" src="./badges/jacoco.svg" /></a>
  <a href="https://orioljt.github.io/task_management_api/"><img alt="Docs" src="https://img.shields.io/badge/Docs-ReDoc-blue" /></a>
  <a href="./DEPLOY.md"><img alt="Deploy Docs" src="https://img.shields.io/badge/Deploy-Docs-blue" /></a>
</p>

## Business Value

For backend teams and platform engineers who need a secure, production-ready foundation for user/project/task management, this API removes weeks of boilerplate. It delivers JWT-based security (Keycloak), strict ownership enforcement, validation, pagination/sorting, consistent error envelopes, migrations, and deploy-ready artifacts so you can focus on domain features and ship faster with confidence.

## Highlights

- ✨ Ownership enforcement (Users → Projects → Tasks)
- ✨ Keycloak JWT security with fine-grained authorities
- ✨ Flyway migrations + JPA with validation
- ✨ Full CI/CD with coverage badge + Docker + Helm

## Quick start

### One-click run (Docker Compose)

```powershell
docker-compose up --build
```

- App: http://localhost:8080
- Keycloak: http://localhost:8081

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

### Get a JWT (Keycloak)

1) Open Keycloak admin: http://localhost:8081 (admin / admin)
2) Create a realm named `task-realm`.
3) Create a client `task-api`:
  - Client type: Public
  - Direct Access Grants: ON (to allow password grant)
  - Standard Flow: ON (optional, for browser login)
4) Create two users, set Password and turn Temporary off (e.g., `alice` and `bob`). Optionally assign realm role `admin` to `alice` for admin endpoints.
5) Get a token (Direct Access Grant):

```powershell
$body = 'client_id=task-api&grant_type=password&username=alice&password=alice'
$resp = Invoke-RestMethod -Method Post -ContentType 'application/x-www-form-urlencoded' -Uri 'http://localhost:8081/realms/task-realm/protocol/openid-connect/token' -Body $body
$token = $resp.access_token
```

### Try it (PowerShell examples)

Set the Authorization header once:

```powershell
$Headers = @{ Authorization = "Bearer $token" }
```

Create a project:

```powershell
$proj = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/projects' -Headers $Headers -ContentType 'application/json' -Body '{"name":"Quickstart Project"}'
$projectId = $proj.id
```

Create a task in that project:

```powershell
$taskBody = '{"title":"First task","description":"Demo task","priority":1,"dueDate":"2030-01-01"}'
$task = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/projects/$projectId/tasks" -Headers $Headers -ContentType 'application/json' -Body $taskBody
```

List tasks (pagination supported):

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/projects/$projectId/tasks?page=0&size=10" -Headers $Headers
```

Access denied (ownership) example: get Bob's token and try to read Alice's project — expect 404.

```powershell
$bodyBob = 'client_id=task-api&grant_type=password&username=bob&password=bob'
$respBob = Invoke-RestMethod -Method Post -ContentType 'application/x-www-form-urlencoded' -Uri 'http://localhost:8081/realms/task-realm/protocol/openid-connect/token' -Body $bodyBob
$tokenBob = $respBob.access_token
$HeadersBob = @{ Authorization = "Bearer $tokenBob" }

# Bob tries to read Alice's tasks -> 404 (project not found or not owned)
Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/projects/$projectId/tasks" -Headers $HeadersBob
```

### curl (bash) equivalents

Get a token:

```bash
TOKEN=$(curl -s -X POST \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'client_id=task-api&grant_type=password&username=alice&password=alice' \
  http://localhost:8081/realms/task-realm/protocol/openid-connect/token | jq -r .access_token)
```

Create a project:

```bash
PROJ=$(curl -s -X POST 'http://localhost:8080/api/projects' \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"name":"Quickstart Project"}')
PROJECT_ID=$(echo "$PROJ" | jq -r .id)
```

Create a task:

```bash
curl -s -X POST "http://localhost:8080/api/projects/$PROJECT_ID/tasks" \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"title":"First task","description":"Demo task","priority":1,"dueDate":"2030-01-01"}'
```

List tasks:

```bash
curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/projects/$PROJECT_ID/tasks?page=0&size=10"
```

Access denied with another user:

```bash
TOKEN_BOB=$(curl -s -X POST -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'client_id=task-api&grant_type=password&username=bob&password=bob' \
  http://localhost:8081/realms/task-realm/protocol/openid-connect/token | jq -r .access_token)

curl -i -H "Authorization: Bearer $TOKEN_BOB" "http://localhost:8080/api/projects/$PROJECT_ID/tasks" # expect 404
```

Postman: import the OpenAPI from http://localhost:8080/v3/api-docs, set an Authorization Bearer token with the Keycloak JWT, and call the same endpoints.

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

## API Documentation
👉 [View the API Docs](https://orioljt.github.io/task_management_api/)

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

## GitHub presentation (repo metadata)

- Suggested description: "Production-ready Spring Boot 3.5 REST API for users/projects/tasks with Keycloak JWT, Flyway, pagination, validation, and consistent error envelopes."
- Suggested topics/tags: `spring-boot`, `keycloak`, `jwt`, `docker`, `flyway`, `backend-api`

Optional with GitHub CLI:

```bash
gh repo edit --description "Production-ready Spring Boot 3.5 REST API for users/projects/tasks with Keycloak JWT, Flyway, pagination, validation, and consistent error envelopes."
gh repo edit --add-topic spring-boot --add-topic keycloak --add-topic jwt --add-topic docker --add-topic flyway --add-topic backend-api
```