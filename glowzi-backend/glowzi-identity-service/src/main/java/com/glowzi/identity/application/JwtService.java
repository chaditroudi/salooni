package com.glowzi.identity.application;

/**
 * Port interface for JWT token operations.
 *
 * In our architecture, Keycloak manages all token issuance.
 * This port abstracts that: the application layer calls getToken(),
 * and the infrastructure adapter delegates to Keycloak.
 */
public interface JwtService {

    /**
     * Obtains an access token for the given credentials from the identity provider.
     *
     * @param username the user's username (phone number in E.164 format)
     * @param password the user's raw password
     * @return the access token string
     * @throws RuntimeException if authentication fails
     */
    String getToken(String username, String password);
}
