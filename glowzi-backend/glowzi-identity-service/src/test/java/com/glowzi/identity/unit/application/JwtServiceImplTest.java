package com.glowzi.identity.unit.application;

import com.glowzi.identity.application.IdentityProviderService;
import com.glowzi.identity.infrastructure.security.JwtServiceImpl;
import com.glowzi.identity.infrastructure.security.KeycloakUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.AccessTokenResponse;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for JwtServiceImpl — delegates to KeycloakUserService for tokens.
 */
@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {

    @Mock
    private KeycloakUserService keycloakUserService;

    private JwtServiceImpl jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl(keycloakUserService);
    }

    @Test
    @DisplayName("getToken delegates to KeycloakUserService and returns access token")
    void getToken_delegatesToKeycloak() {
        AccessTokenResponse tokenResponse = new AccessTokenResponse();
        tokenResponse.setToken("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.test.signature");

        when(keycloakUserService.getToken("+966501234567", "SecurePass1"))
                .thenReturn(tokenResponse);

        String token = jwtService.getToken("+966501234567", "SecurePass1");

        assertThat(token).isEqualTo("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.test.signature");
    }

    @Test
    @DisplayName("getToken propagates IdentityProviderAuthException on bad credentials")
    void getToken_propagatesAuthException() {
        when(keycloakUserService.getToken("+966501234567", "wrong"))
                .thenThrow(new IdentityProviderService.IdentityProviderAuthException("Authentication failed"));

        assertThatThrownBy(() -> jwtService.getToken("+966501234567", "wrong"))
                .isInstanceOf(IdentityProviderService.IdentityProviderAuthException.class);
    }
}
