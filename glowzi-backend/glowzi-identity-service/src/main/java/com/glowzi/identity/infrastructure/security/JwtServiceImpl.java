package com.glowzi.identity.infrastructure.security;

import com.glowzi.identity.application.JwtService;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.stereotype.Component;

/**
 * Delegates token operations to Keycloak.
 * The user's phone number is used as the Keycloak username.
 */
@Component
public class JwtServiceImpl implements JwtService {

    private final KeycloakUserService keycloakUserService;

    public JwtServiceImpl(KeycloakUserService keycloakUserService) {
        this.keycloakUserService = keycloakUserService;
    }

    /**
     * Obtains a Keycloak access token using username + password (ROPC grant).
     */
    @Override
    public String getToken(String username, String password) {
        AccessTokenResponse tokenResponse = keycloakUserService.getToken(username, password);
        return tokenResponse.getToken();
    }
}
