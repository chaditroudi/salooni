package com.glowzi.identity.unit.application;

import com.glowzi.identity.application.IdentityProviderService;
import com.glowzi.identity.application.LoginUserUseCase;
import com.glowzi.identity.application.command.LoginCommand;
import com.glowzi.identity.application.result.AuthResult;
import com.glowzi.identity.domain.User;
import com.glowzi.identity.domain.UserRepository;
import com.glowzi.identity.domain.enums.UserRole;
import com.glowzi.identity.domain.exception.InvalidCredentialsException;
import com.glowzi.identity.domain.vo.FullName;
import com.glowzi.identity.domain.vo.HashedPassword;
import com.glowzi.identity.domain.vo.Phone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit test for LoginUserUseCase.
 * Mocks: UserRepository, IdentityProviderService.
 * Authentication is delegated to Keycloak (via IdentityProviderService).
 */
@ExtendWith(MockitoExtension.class)
class LoginUserUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private IdentityProviderService identityProvider;

    private LoginUserUseCase useCase;

    private static final User EXISTING_USER = new User(
            1L,
            new FullName("Ahmed Ali"),
            new Phone("+966501234567"),
            new HashedPassword("KEYCLOAK_MANAGED"),
            UserRole.CUSTOMER,
            "ar",
            LocalDateTime.now(),
            LocalDateTime.now()
    );

    @BeforeEach
    void setUp() {
        useCase = new LoginUserUseCase(userRepository, identityProvider);
    }

    @Test
    @DisplayName("logs in with correct credentials: authenticates via provider → finds local user → returns tokens")
    void should_login_with_correct_credentials() {
        LoginCommand cmd = new LoginCommand("+966501234567", "correctPass");

        when(identityProvider.authenticate("+966501234567", "correctPass"))
                .thenReturn(new IdentityProviderService.TokenResponse("access-jwt", "refresh-jwt", 300L));
        when(userRepository.findByPhone(new Phone("+966501234567")))
                .thenReturn(Optional.of(EXISTING_USER));

        AuthResult result = useCase.execute(cmd);

        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.role()).isEqualTo("CUSTOMER");
        assertThat(result.accessToken()).isEqualTo("access-jwt");
        assertThat(result.refreshToken()).isEqualTo("refresh-jwt");
        assertThat(result.expiresIn()).isEqualTo(300L);
    }

    @Test
    @DisplayName("throws InvalidCredentialsException when provider rejects credentials")
    void should_throw_when_provider_rejects_credentials() {
        LoginCommand cmd = new LoginCommand("+966501234567", "wrongPass");

        when(identityProvider.authenticate(anyString(), anyString()))
                .thenThrow(new IdentityProviderService.IdentityProviderAuthException("Authentication failed"));

        assertThatThrownBy(() -> useCase.execute(cmd))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials");

        verify(userRepository, never()).findByPhone(any());
    }

    @Test
    @DisplayName("throws InvalidCredentialsException when phone not found in local DB")
    void should_throw_when_phone_not_found_locally() {
        LoginCommand cmd = new LoginCommand("+966509999999", "pass1234A");

        when(identityProvider.authenticate(anyString(), anyString()))
                .thenReturn(new IdentityProviderService.TokenResponse("a", "r", 300L));
        when(userRepository.findByPhone(any(Phone.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(cmd))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
