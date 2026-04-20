package com.glowzi.identity.application.command;

/**
 * Command object for refreshing an access token.
 * The caller provides the refresh token they received at login/register.
 */
public record RefreshTokenCommand(
        String refreshToken
) {
}
