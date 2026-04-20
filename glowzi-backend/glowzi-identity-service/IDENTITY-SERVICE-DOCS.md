# Glowzi Identity Service API

## Overview

The Glowzi Identity Service is the authentication and user identity API for the Glowzi platform. It handles:

- user registration
- login
- token refresh
- token validation for downstream services
- current-user profile lookup
- password changes
- logout requests

The service is implemented in Spring Boot and delegates identity operations to Keycloak while keeping application-specific profile data in PostgreSQL.

Base URLs:

- Local service: `http://localhost:8081`
- Swagger UI: `http://localhost:8081/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8081/v3/api-docs`
- Source OpenAPI YAML: [`openapi/identity-service.openapi.yaml`](/c:/Users/DELL/Desktop/Glowzini/glowzi-backend/glowzi-identity-service/openapi/identity-service.openapi.yaml)

## API Surface

### Public endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/auth/register` | Register a new user and return tokens |
| `POST` | `/auth/login` | Authenticate with phone and password |
| `POST` | `/auth/refresh` | Exchange a refresh token for a new token pair |

### Protected endpoints

These require `Authorization: Bearer <accessToken>`.

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/auth/validate` | Validate the bearer token and return identity data |
| `GET` | `/auth/me` | Return the current user profile |
| `POST` | `/auth/logout` | Accept a refresh token for logout handling |
| `POST` | `/auth/change-password` | Verify old password and set a new one |

## Authentication And Authorization

### Supported flow

The service currently supports a username/password style login against Keycloak using the phone number as the username:

1. The client calls `POST /auth/login` with `phone` and `password`.
2. The identity service forwards credentials to Keycloak.
3. Keycloak returns an access token and refresh token.
4. The client sends the access token in the `Authorization` header for protected routes.
5. Spring Security validates the JWT using the configured issuer: `http://localhost:9090/realms/glowzi`.
6. When the access token expires, the client calls `POST /auth/refresh` with the refresh token.

### Token model

- Access tokens are bearer JWTs issued by Keycloak.
- Refresh tokens are opaque to the frontend and should be stored securely.
- `expiresIn` is returned in seconds and represents the access token lifetime.
- Roles are expected in the JWT `roles` claim and are mapped to Spring authorities as `ROLE_<role>`.

### Scopes and roles

The codebase does not currently enforce OAuth scopes at the API layer.

Supported business roles:

- `CUSTOMER`
- `PROVIDER`
- `ADMIN`

Authorization is currently endpoint-level:

- `/auth/register`, `/auth/login`, `/auth/refresh` are public.
- `/auth/validate`, `/auth/me`, `/auth/logout`, `/auth/change-password` require a valid JWT.

### Security considerations

- Use HTTPS in non-local environments.
- Do not log access tokens or refresh tokens.
- Store refresh tokens in secure storage rather than browser local storage when possible.
- Treat `phone` as the identity username and send it in E.164 format.
- Password policy enforced by request validation:
  - 8 to 64 characters
  - at least one uppercase letter
  - at least one lowercase letter
  - at least one digit

### Important implementation note

`POST /auth/logout` is exposed and returns `204 No Content`, but the current `KeycloakUserService.logout(...)` implementation does not yet perform real token/session revocation in Keycloak. Frontends should still clear local tokens immediately after logout, and testers should not assume that an already-issued access token becomes invalid immediately after calling this endpoint.

## Request And Response Contracts

### `POST /auth/register`

Request body:

```json
{
  "fullName": "Ahmed Ali",
  "phone": "+966501234567",
  "password": "SecurePass1",
  "role": "CUSTOMER",
  "preferredLanguage": "en"
}
```

Success response: `201 Created`

```json
{
  "userId": 1,
  "role": "CUSTOMER",
  "accessToken": "eyJ...",
  "refreshToken": "eyJ...",
  "expiresIn": 300
}
```

Validation rules:

- `fullName`: required, non-blank, max 100 chars
- `phone`: required, E.164 format, example `+966501234567`
- `password`: required, 8-64 chars, uppercase + lowercase + digit
- `role`: required, one of `CUSTOMER`, `PROVIDER`, `ADMIN`
- `preferredLanguage`: optional free-text string

Possible responses:

- `201 Created`
- `400 Bad Request`
- `409 Conflict`
- `503 Service Unavailable`

### `POST /auth/login`

Request body:

```json
{
  "phone": "+966501234567",
  "password": "SecurePass1"
}
```

Success response: `200 OK`

```json
{
  "userId": 1,
  "role": "CUSTOMER",
  "accessToken": "eyJ...",
  "refreshToken": "eyJ...",
  "expiresIn": 300
}
```

Possible responses:

- `200 OK`
- `400 Bad Request`
- `401 Unauthorized`
- `503 Service Unavailable`

### `POST /auth/refresh`

Request body:

```json
{
  "refreshToken": "eyJ-refresh-token"
}
```

Success response: `200 OK`

```json
{
  "accessToken": "eyJ-new-access",
  "refreshToken": "eyJ-new-refresh",
  "expiresIn": 300
}
```

Possible responses:

- `200 OK`
- `400 Bad Request`
- `401 Unauthorized`
- `503 Service Unavailable`

### `GET /auth/validate`

Headers:

- `Authorization: Bearer <accessToken>`

Success response: `200 OK`

```json
{
  "userId": 1,
  "role": "CUSTOMER",
  "phone": "+966501234567"
}
```

Possible responses:

- `200 OK`
- `401 Unauthorized`

### `GET /auth/me`

Headers:

- `Authorization: Bearer <accessToken>`

Success response: `200 OK`

```json
{
  "userId": 1,
  "fullName": "Ahmed Ali",
  "phone": "+966501234567",
  "role": "CUSTOMER",
  "preferredLanguage": "en"
}
```

Possible responses:

- `200 OK`
- `401 Unauthorized`

### `POST /auth/logout`

Headers:

- `Authorization: Bearer <accessToken>`

Request body:

```json
{
  "refreshToken": "eyJ-refresh-token"
}
```

Success response: `204 No Content`

Possible responses:

- `204 No Content`
- `400 Bad Request`
- `401 Unauthorized`
- `503 Service Unavailable`

### `POST /auth/change-password`

Headers:

- `Authorization: Bearer <accessToken>`

Request body:

```json
{
  "oldPassword": "SecurePass1",
  "newPassword": "NewSecurePass2"
}
```

Success response: `204 No Content`

Possible responses:

- `204 No Content`
- `400 Bad Request`
- `401 Unauthorized`
- `503 Service Unavailable`

## Error Handling

Application-managed error responses use JSON objects shaped like these:

Validation error:

```json
{
  "error": "Validation failed",
  "fields": {
    "phone": "Phone must be in E.164 format (e.g. +966501234567)"
  }
}
```

Simple error:

```json
{
  "error": "Invalid credentials"
}
```

### Status code reference

| Status | Meaning | Notes |
| --- | --- | --- |
| `400` | Validation or domain error | Returned by controller validation and domain checks |
| `401` | Invalid credentials or invalid refresh token | Application-level auth failures use `{ "error": "Invalid credentials" }` |
| `401` | Missing or invalid bearer token | Generated by Spring Security before controller execution; response body is framework-controlled |
| `409` | Duplicate phone or provider conflict | Registration conflict |
| `503` | Keycloak or identity-provider failure | Provider dependency unavailable or errored |

## Frontend Integration Notes

- The frontend service contract already matches this API in [`src/services/auth-service.ts`](/c:/Users/DELL/Desktop/Glowzini/glowzi-frontend/src/services/auth-service.ts) and [`src/types/auth.ts`](/c:/Users/DELL/Desktop/Glowzini/glowzi-frontend/src/types/auth.ts).
- Use the returned `accessToken` for protected endpoints and retain the `refreshToken` for silent refresh.
- Refresh token rotation is expected because `/auth/refresh` returns a new refresh token.
- If `/auth/refresh` returns `401`, the frontend should clear local tokens and force re-authentication.

## Local Setup

### Prerequisites

- Java 21+
- Docker Desktop

### Start local dependencies

From [`infra/docker-compose.yml`](/c:/Users/DELL/Desktop/Glowzini/glowzi-backend/infra/docker-compose.yml):

```powershell
cd c:\Users\DELL\Desktop\Glowzini\glowzi-backend\infra
docker compose up -d keycloak identity-db
```

Key local ports:

- Keycloak: `9090`
- Identity DB: `5433`
- Identity service: `8081`

### Run the service

```powershell
cd c:\Users\DELL\Desktop\Glowzini\glowzi-backend\glowzi-identity-service
.\mvnw.cmd spring-boot:run
```

### Verify the service

```powershell
Invoke-RestMethod http://localhost:8081/actuator/health
```

## Testing Assets

- OpenAPI YAML: [`openapi/identity-service.openapi.yaml`](/c:/Users/DELL/Desktop/Glowzini/glowzi-backend/glowzi-identity-service/openapi/identity-service.openapi.yaml)
- Postman collection: [`postman/Glowzi-Identity-Service.postman_collection.json`](/c:/Users/DELL/Desktop/Glowzini/glowzi-backend/glowzi-identity-service/postman/Glowzi-Identity-Service.postman_collection.json)
- Step-by-step guide: [`API-TESTING-GUIDE.md`](/c:/Users/DELL/Desktop/Glowzini/glowzi-backend/glowzi-identity-service/API-TESTING-GUIDE.md)

## Versioning Strategy

Current API version: `1.0.0`

The implementation currently uses stable unversioned routes under `/auth`. Versioning is therefore managed through:

- the OpenAPI document version
- release notes and changelog entries
- backward-compatible additions within the same major version

Recommended policy going forward:

- additive response fields and new endpoints: minor version
- documentation clarifications and non-contract changes: patch version
- breaking contract changes: new major API version, ideally exposed as a new gateway route such as `/api/v2/auth/...`

## Changelog

### 1.0.0

- documented all seven implemented `/auth` endpoints
- aligned payload examples with backend DTOs and frontend types
- added a hand-authored OpenAPI 3.0.3 definition
- added local testing instructions and a Postman collection
- recorded the current logout limitation so frontend integration is not misled
