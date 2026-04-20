package com.glowzi.identity.application.result;

/**
 * Result from the /auth/me endpoint.
 * Full user profile from local DB + role from JWT.
 */
public record UserProfileResult(
        Long userId,
        String fullName,
        String phone,
        String role,
        String preferredLanguage
) {}
