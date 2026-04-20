package com.glowzi.identity.application.result;

/**
 * Result from the /auth/validate endpoint.
 * Contains the essential user identity extracted from the JWT.
 * Used by the API Gateway to enrich requests with user context.
 */
public record ValidateResult(
        Long userId,
        String role,
        String phone
) {}
