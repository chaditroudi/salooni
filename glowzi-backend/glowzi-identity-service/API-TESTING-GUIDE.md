# Glowzi Identity Service Testing Guide

## Quick Start

### 1. Start infrastructure

```powershell
cd c:\Users\DELL\Desktop\Glowzini\glowzi-backend\infra
docker compose up -d keycloak identity-db
```

### 2. Start the identity service

```powershell
cd c:\Users\DELL\Desktop\Glowzini\glowzi-backend\glowzi-identity-service
.\mvnw.cmd spring-boot:run
```

### 3. Confirm health

```powershell
Invoke-RestMethod http://localhost:8081/actuator/health
```

Expected response:

```json
{
  "status": "UP"
}
```

## Test URLs

- Base URL: `http://localhost:8081`
- Swagger UI: `http://localhost:8081/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8081/v3/api-docs`

## Recommended Manual Test Flow

1. Register a user.
2. Call `GET /auth/me` with the returned access token.
3. Call `GET /auth/validate`.
4. Refresh the token pair.
5. Change the password.
6. Log in using the new password.
7. Call logout and clear local tokens.

## Sample Requests

### Register

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:8081/auth/register `
  -ContentType "application/json" `
  -Body '{"fullName":"Ahmed Ali","phone":"+966501234567","password":"SecurePass1","role":"CUSTOMER","preferredLanguage":"en"}'
```

### Login

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:8081/auth/login `
  -ContentType "application/json" `
  -Body '{"phone":"+966501234567","password":"SecurePass1"}'
```

### Refresh

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:8081/auth/refresh `
  -ContentType "application/json" `
  -Body '{"refreshToken":"<refresh-token>"}'
```

### My Profile

```powershell
Invoke-RestMethod `
  -Method Get `
  -Uri http://localhost:8081/auth/me `
  -Headers @{ Authorization = "Bearer <access-token>" }
```

### Validate Token

```powershell
Invoke-RestMethod `
  -Method Get `
  -Uri http://localhost:8081/auth/validate `
  -Headers @{ Authorization = "Bearer <access-token>" }
```

### Change Password

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:8081/auth/change-password `
  -Headers @{ Authorization = "Bearer <access-token>" } `
  -ContentType "application/json" `
  -Body '{"oldPassword":"SecurePass1","newPassword":"NewSecurePass2"}'
```

### Logout

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:8081/auth/logout `
  -Headers @{ Authorization = "Bearer <access-token>" } `
  -ContentType "application/json" `
  -Body '{"refreshToken":"<refresh-token>"}'
```

## Sample Test Cases

| ID | Scenario | Expected Result |
| --- | --- | --- |
| `AUTH-001` | Register valid customer | `201` and token pair returned |
| `AUTH-002` | Register duplicate phone | `409` with `error` message |
| `AUTH-003` | Register invalid phone | `400` with `fields.phone` |
| `AUTH-004` | Login valid user | `200` and token pair returned |
| `AUTH-005` | Login wrong password | `401` with `Invalid credentials` |
| `AUTH-006` | Refresh valid refresh token | `200` and rotated token pair |
| `AUTH-007` | Refresh invalid token | `401` |
| `AUTH-008` | Get profile with valid bearer token | `200` with profile payload |
| `AUTH-009` | Get profile without bearer token | `401` |
| `AUTH-010` | Change password with valid old password | `204` |
| `AUTH-011` | Change password with wrong old password | `401` |
| `AUTH-012` | Logout request | `204`; client should clear tokens locally |

## Postman

Import:

- [`postman/Glowzi-Identity-Service.postman_collection.json`](/c:/Users/DELL/Desktop/Glowzini/glowzi-backend/glowzi-identity-service/postman/Glowzi-Identity-Service.postman_collection.json)

Suggested collection variables:

- `baseUrl` = `http://localhost:8081`
- `accessToken`
- `refreshToken`

Recommended test script for `register`, `login`, and `refresh` responses:

```javascript
const json = pm.response.json();
if (json.accessToken) pm.collectionVariables.set("accessToken", json.accessToken);
if (json.refreshToken) pm.collectionVariables.set("refreshToken", json.refreshToken);
```

## Automated Tests In Repo

The backend already includes API-focused test coverage:

- controller tests: [`src/test/java/com/glowzi/identity/integration/AuthControllerTest.java`](/c:/Users/DELL/Desktop/Glowzini/glowzi-backend/glowzi-identity-service/src/test/java/com/glowzi/identity/integration/AuthControllerTest.java)
- end-to-end integration tests: [`src/test/java/com/glowzi/identity/integration/AuthIntegrationTest.java`](/c:/Users/DELL/Desktop/Glowzini/glowzi-backend/glowzi-identity-service/src/test/java/com/glowzi/identity/integration/AuthIntegrationTest.java)

Run them with:

```powershell
.\mvnw.cmd test
```

## Known Testing Caveat

`POST /auth/logout` currently behaves as an accepted logout command and returns `204`, but the current Keycloak adapter does not yet revoke existing sessions/tokens. Testers should validate frontend token clearing, not immediate server-side invalidation of already-issued tokens.
