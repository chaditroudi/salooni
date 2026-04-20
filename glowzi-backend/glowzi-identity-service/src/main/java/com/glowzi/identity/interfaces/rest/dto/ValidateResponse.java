package com.glowzi.identity.interfaces.rest.dto;

/**
 * Response DTO for GET /auth/validate.
 * Returns the essential user identity extracted from the JWT.
 * Used by the API Gateway to enrich downstream service requests.
 */
public record ValidateResponse(
        Long userId,
        String role,
        String phone
) {}
