package com.glowzi.identity.application;

/**
 * Port interface for external identity provider operations.
 * Application layer depends on this port; infrastructure layer implements it.
 * This preserves hexagonal architecture: application → port ← infrastructure.
 */
public interface IdentityProviderService {

    /**
     * Creates a user in the external identity provider.
     * @return the provider-assigned user ID
     */
    String createUser(String username, String fullName, String phone,
                      String rawPassword, String roleName);

    /**
     * Authenticates a user and returns the full token response (access + refresh token).
     * @throws IdentityProviderAuthException if credentials are invalid
     * @throws IdentityProviderException for other provider errors
     */
    TokenResponse authenticate(String username, String password);

    /**
     * Exchanges a refresh token for a new access token + refresh token pair.
     * @throws IdentityProviderAuthException if the refresh token is expired or invalid
     */
    TokenResponse refresh(String refreshToken);

    /**
     * Revokes a user session by invalidating the refresh token.
     * After logout, the user must log in again to get new tokens.
     *
     * @param refreshToken the refresh token to revoke
     * @throws IdentityProviderException if revocation fails
     */
    void logout(String refreshToken);

    /**
     * Changes a user's password in the identity provider.
     *
     * @param username the user's username (phone number)
     * @param newPassword the new password to set
     * @throws IdentityProviderException if the operation fails
     */
    void changePassword(String username, String newPassword);

    /** Simple holder for token response data returned from the provider. */
    record TokenResponse(String accessToken, String refreshToken, long expiresIn) {}

    // ─── Provider-specific exceptions (application-layer) ────────────

    class IdentityProviderException extends RuntimeException {
        public IdentityProviderException(String message) { super(message); }
        public IdentityProviderException(String message, Throwable cause) { super(message, cause); }
    }

    class IdentityProviderConflictException extends IdentityProviderException {
        public IdentityProviderConflictException(String message) { super(message); }
    }

    class IdentityProviderAuthException extends IdentityProviderException {
        public IdentityProviderAuthException(String message) { super(message); }
        public IdentityProviderAuthException(String message, Throwable cause) { super(message, cause); }
    }
}
