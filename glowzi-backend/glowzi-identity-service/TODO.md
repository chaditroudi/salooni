# Glowzi Identity Service — TODO (Missing Features)

> Features that are **not yet implemented** in the identity service.
> Ordered by priority: MVP → Production → Nice-to-have.

---

## 🔴 MVP Priority

- [ ] **Profile Update Endpoint** — `PATCH /auth/profile`
  - Allow authenticated users to update `fullName` and `preferredLanguage`
  - Currently read-only after registration
  - Needs: `UpdateProfileUseCase`, `UpdateProfileCommand`, `UpdateProfileRequest` DTO

- [ ] **Flyway Migration Scripts V1–V3**
  - Only `V4__.sql` exists; initial schema creation scripts are missing
  - Provide the full migration chain so a fresh DB can be bootstrapped

---

## 🟡 Production Readiness

- [ ] **Forgot Password / Account Recovery** — `POST /auth/forgot-password`, `POST /auth/reset-password`
  - No password reset flow exists
  - Needs: email/SMS delivery integration, reset token generation
  - Can delegate to Keycloak's built-in reset flow

- [ ] **Rate Limiting on Auth Endpoints**
  - `/auth/register`, `/auth/login` are unprotected against brute-force
  - Options: Bucket4j, Spring Cloud Gateway rate limiter, or Resilience4j

- [ ] **Audit Logging**
  - No trail of login attempts, password changes, or registration events
  - Add an `@Aspect` or servlet filter to log auth events to DB or log aggregator

- [ ] **API Documentation (OpenAPI / Swagger)**
  - No Swagger UI or OpenAPI spec
  - Add `springdoc-openapi-starter-webmvc-ui` dependency + annotations

- [ ] **Proper Logout / Token Blacklist**
  - Current logout attempts Keycloak session revocation but may be incomplete
  - Options: Redis-backed token blacklist, or Keycloak admin API session revocation

- [ ] **Refresh Token Rotation**
  - Old refresh tokens are not invalidated after use
  - Implement rotation: each refresh returns a new refresh token and invalidates the old one

---

## 🟢 Nice-to-Have (Post-MVP)

- [ ] **Email / SMS Verification**
  - No OTP or phone verification on registration
  - Add `POST /auth/verify-phone`, `POST /auth/resend-otp`

- [ ] **Two-Factor Authentication (2FA / TOTP)**
  - Keycloak supports this; expose config endpoints

- [ ] **Social Login (Google, Apple)**
  - Keycloak supports OAuth2 identity brokering
  - Expose authorization-code flow instead of ROPC only

- [ ] **Device / Session Management**
  - `GET /auth/sessions` — list active sessions across devices
  - `DELETE /auth/sessions/{id}` — revoke a specific session

- [ ] **Admin: List / Search Users**
  - `GET /admin/users?role=PROVIDER&page=0` — for admin dashboard

- [ ] **Account Deactivation / Soft Delete**
  - `POST /auth/deactivate` — mark account inactive

---

_Last updated: 2026-03-28_
