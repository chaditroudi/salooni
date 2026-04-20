package com.glowzi.identity.application.result;

/**
 * Result returned by the refresh token use case.
 * Contains both a new access token and a new refresh token.
 */
public record TokenResult(
        String accessToken,
        String refreshToken,
        long expiresIn        // seconds until access token expires
) {
}
