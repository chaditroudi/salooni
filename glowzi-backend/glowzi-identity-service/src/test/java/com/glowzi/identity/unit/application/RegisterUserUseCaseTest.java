package com.glowzi.identity.unit.application;

import com.glowzi.identity.application.DomainEventPublisher;
import com.glowzi.identity.application.IdentityProviderService;
import com.glowzi.identity.application.RegisterUserUseCase;
import com.glowzi.identity.application.command.RegisterUserCommand;
import com.glowzi.identity.application.result.AuthResult;
import com.glowzi.identity.domain.User;
import com.glowzi.identity.domain.UserRepository;
import com.glowzi.identity.domain.enums.UserRole;
import com.glowzi.identity.domain.event.DomainEvent;
import com.glowzi.identity.domain.exception.PhoneAlreadyRegisteredException;
import com.glowzi.identity.domain.vo.Phone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit test for RegisterUserUseCase.
 * Mocks: UserRepository, IdentityProviderService, DomainEventPublisher.
 * No Spring context, no DB.
 */
@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private IdentityProviderService identityProvider;
    @Mock private DomainEventPublisher eventPublisher;

    private RegisterUserUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegisterUserUseCase(userRepository, identityProvider, eventPublisher);
    }

    @Test
    @DisplayName("registers new user: creates in provider → saves locally → publishes events → returns tokens")
    void should_register_new_user_and_return_tokens() {
        RegisterUserCommand cmd = new RegisterUserCommand(
                "Ahmed Ali", "+966501234567", "SecurePass1", "CUSTOMER", "ar");

        when(userRepository.existsByPhone(any(Phone.class))).thenReturn(false);
        when(identityProvider.createUser(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn("kc-user-id-1");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return new User(1L, u.getFullName(), u.getPhone(), u.getPasswordHash(),
                    u.getRole(), u.getPreferredLanguage(), u.getCreatedAt(), u.getUpdatedAt());
        });
        when(identityProvider.authenticate(anyString(), anyString()))
                .thenReturn(new IdentityProviderService.TokenResponse("access-jwt", "refresh-jwt", 300L));

        AuthResult result = useCase.execute(cmd);

        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.role()).isEqualTo("CUSTOMER");
        assertThat(result.accessToken()).isEqualTo("access-jwt");
        assertThat(result.refreshToken()).isEqualTo("refresh-jwt");
        assertThat(result.expiresIn()).isEqualTo(300L);

        // Verify the correct flow
        verify(userRepository).existsByPhone(new Phone("+966501234567"));
        verify(identityProvider).createUser("+966501234567", "Ahmed Ali", "+966501234567", "SecurePass1", "CUSTOMER");
        verify(userRepository).save(any(User.class));
        verify(eventPublisher).publishAll(any(List.class));
    }

    @Test
    @DisplayName("throws PhoneAlreadyRegisteredException when phone exists in local DB")
    void should_throw_when_phone_already_registered() {
        RegisterUserCommand cmd = new RegisterUserCommand(
                "Ahmed", "+966501234567", "pass1234", "CUSTOMER", null);

        when(userRepository.existsByPhone(any(Phone.class))).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(cmd))
                .isInstanceOf(PhoneAlreadyRegisteredException.class)
                .hasMessageContaining("+966501234567");

        verify(userRepository, never()).save(any());
        verify(identityProvider, never()).createUser(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("throws PhoneAlreadyRegisteredException when provider reports conflict")
    void should_throw_when_provider_reports_conflict() {
        RegisterUserCommand cmd = new RegisterUserCommand(
                "Ahmed", "+966501234567", "pass1234", "CUSTOMER", null);

        when(userRepository.existsByPhone(any(Phone.class))).thenReturn(false);
        when(identityProvider.createUser(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new IdentityProviderService.IdentityProviderConflictException("exists"));

        assertThatThrownBy(() -> useCase.execute(cmd))
                .isInstanceOf(PhoneAlreadyRegisteredException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("publishes domain events after saving user")
    void should_publish_domain_events() {
        RegisterUserCommand cmd = new RegisterUserCommand(
                "Ahmed Ali", "+966501234567", "SecurePass1", "CUSTOMER", "ar");

        when(userRepository.existsByPhone(any())).thenReturn(false);
        when(identityProvider.createUser(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn("kc-id");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return new User(1L, u.getFullName(), u.getPhone(), u.getPasswordHash(),
                    u.getRole(), u.getPreferredLanguage(), u.getCreatedAt(), u.getUpdatedAt());
        });
        when(identityProvider.authenticate(anyString(), anyString()))
                .thenReturn(new IdentityProviderService.TokenResponse("a", "r", 300L));

        useCase.execute(cmd);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DomainEvent>> eventsCaptor = ArgumentCaptor.forClass(List.class);
        verify(eventPublisher).publishAll(eventsCaptor.capture());
        assertThat(eventsCaptor.getValue()).isNotEmpty();
    }

    @Test
    @DisplayName("stores KEYCLOAK_MANAGED as password hash in local DB")
    void should_store_keycloak_managed_password() {
        RegisterUserCommand cmd = new RegisterUserCommand(
                "Ahmed", "+966501234567", "rawPassword1", "CUSTOMER", null);

        when(userRepository.existsByPhone(any())).thenReturn(false);
        when(identityProvider.createUser(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn("kc-id");
        when(userRepository.save(any())).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return new User(1L, u.getFullName(), u.getPhone(), u.getPasswordHash(),
                    u.getRole(), u.getPreferredLanguage(), u.getCreatedAt(), u.getUpdatedAt());
        });
        when(identityProvider.authenticate(anyString(), anyString()))
                .thenReturn(new IdentityProviderService.TokenResponse("a", "r", 300L));

        useCase.execute(cmd);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash().value()).isEqualTo("KEYCLOAK_MANAGED");
    }
}
