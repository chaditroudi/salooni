package com.glowzi.identity.application;

import com.glowzi.identity.application.command.RefreshTokenCommand;
import com.glowzi.identity.application.result.TokenResult;
import com.glowzi.identity.domain.exception.InvalidCredentialsException;
import org.springframework.stereotype.Service;

/**
 * Use case: exchange a refresh token for a new access + refresh token pair.
 *
 * Flow:
 * 1. Receive the refresh token from the client
 * 2. Send it to Keycloak's token endpoint
 * 3. Keycloak validates it and returns a fresh access token + new refresh token
 * 4. Return both tokens to the caller
 *
 * Why refresh tokens?
 * Access tokens are short-lived (e.g. 5–15 minutes) for security.
 * Instead of asking the user to log in again, the client silently calls this
 * endpoint with the refresh token to get a new access token.
 */
@Service
public class RefreshTokenUseCase {

    private final IdentityProviderService identityProvider;

    public RefreshTokenUseCase(IdentityProviderService identityProvider) {
        this.identityProvider = identityProvider;
    }

    public TokenResult execute(RefreshTokenCommand command) {
        if (command.refreshToken() == null || command.refreshToken().isBlank()) {
            throw new IllegalArgumentException("Refresh token must not be blank");
        }

        try {
            IdentityProviderService.TokenResponse response =
                    identityProvider.refresh(command.refreshToken());

            return new TokenResult(
                    response.accessToken(),
                    response.refreshToken(),
                    response.expiresIn()
            );
        } catch (IdentityProviderService.IdentityProviderAuthException e) {
            // Refresh token expired or invalid — user must log in again
            throw new InvalidCredentialsException();
        }
    }
}
