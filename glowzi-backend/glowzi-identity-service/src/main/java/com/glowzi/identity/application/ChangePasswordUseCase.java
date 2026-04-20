package com.glowzi.identity.application;

import com.glowzi.identity.application.command.ChangePasswordCommand;
import com.glowzi.identity.domain.exception.InvalidCredentialsException;
import org.springframework.stereotype.Service;

/**
 * Use case: change the authenticated user's password.
 *
 * Flow:
 * 1. Verify the old password by attempting to authenticate with it
 * 2. If authentication succeeds, change the password in the identity provider
 * 3. If authentication fails, throw InvalidCredentialsException
 *
 * Security: only the authenticated user can change their own password.
 * The controller extracts the username from the JWT and passes it here.
 */
@Service
public class ChangePasswordUseCase {

    private final IdentityProviderService identityProvider;

    public ChangePasswordUseCase(IdentityProviderService identityProvider) {
        this.identityProvider = identityProvider;
    }

    public void execute(ChangePasswordCommand command) {
        if (command.newPassword() == null || command.newPassword().length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters");
        }

        // Verify old password by attempting authentication
        try {
            identityProvider.authenticate(command.username(), command.oldPassword());
        } catch (IdentityProviderService.IdentityProviderAuthException e) {
            throw new InvalidCredentialsException();
        }

        // Old password verified — change to new password
        identityProvider.changePassword(command.username(), command.newPassword());
    }
}
