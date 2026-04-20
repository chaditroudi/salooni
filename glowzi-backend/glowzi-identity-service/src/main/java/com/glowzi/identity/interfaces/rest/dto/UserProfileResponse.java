package com.glowzi.identity.interfaces.rest.dto;

/**
 * Response DTO for GET /auth/me.
 * Returns the full user profile for the authenticated user.
 */
public record UserProfileResponse(
        Long userId,
        String fullName,
        String phone,
        String role,
        String preferredLanguage
) {}
