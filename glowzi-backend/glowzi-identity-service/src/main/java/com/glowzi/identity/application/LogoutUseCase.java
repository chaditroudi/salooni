package com.glowzi.identity.application;

import com.glowzi.identity.application.command.LogoutCommand;
import org.springframework.stereotype.Service;

/**
 * Use case: logout — revoke the user's refresh token.
 *
 * Flow:
 * 1. Receive the refresh token from the client
 * 2. Call the identity provider to revoke/invalidate the session
 * 3. After this, the refresh token can no longer be used to get new access tokens
 *
 * The client should also discard its local copy of both tokens.
 */
@Service
public class LogoutUseCase {

    private final IdentityProviderService identityProvider;

    public LogoutUseCase(IdentityProviderService identityProvider) {
        this.identityProvider = identityProvider;
    }

    public void execute(LogoutCommand command) {
        if (command.refreshToken() == null || command.refreshToken().isBlank()) {
            throw new IllegalArgumentException("Refresh token must not be blank");
        }

        identityProvider.logout(command.refreshToken());
    }
}
