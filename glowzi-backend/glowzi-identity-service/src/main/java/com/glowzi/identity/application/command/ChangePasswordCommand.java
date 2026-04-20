package com.glowzi.identity.application.command;

/**
 * Command to change the authenticated user's password.
 *
 * @param username      the user's username (phone number)
 * @param oldPassword   the current password (for verification)
 * @param newPassword   the new password to set
 */
public record ChangePasswordCommand(
        String username,
        String oldPassword,
        String newPassword
) {}
