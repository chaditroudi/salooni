package com.glowzi.identity.application.command;

/**
 * Command to log out (revoke a user's refresh token).
 *
 * @param refreshToken the refresh token to revoke
 */
public record LogoutCommand(String refreshToken) {}
