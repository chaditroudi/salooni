package com.glowzi.identity.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.glowzi.identity.application.*;
import com.glowzi.identity.application.command.LoginCommand;
import com.glowzi.identity.application.command.RegisterUserCommand;
import com.glowzi.identity.application.command.RefreshTokenCommand;
import com.glowzi.identity.application.result.AuthResult;
import com.glowzi.identity.application.result.TokenResult;
import com.glowzi.identity.domain.exception.InvalidCredentialsException;
import com.glowzi.identity.domain.exception.PhoneAlreadyRegisteredException;
import com.glowzi.identity.interfaces.rest.AuthController;
import com.glowzi.identity.interfaces.rest.GlobalExceptionHandler;
import com.glowzi.identity.interfaces.rest.dto.LoginRequest;
import com.glowzi.identity.interfaces.rest.dto.RefreshTokenRequest;
import com.glowzi.identity.interfaces.rest.dto.RegisterUserRequest;
import com.glowzi.identity.domain.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller-level tests using @WebMvcTest.
 * Mocks use cases to test HTTP layer in isolation.
 */
@WebMvcTest(
    controllers = AuthController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class
    }
)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean private RegisterUserUseCase registerUserUseCase;
    @MockitoBean private LoginUserUseCase loginUserUseCase;
    @MockitoBean private RefreshTokenUseCase refreshTokenUseCase;
    @MockitoBean private GetCurrentUserUseCase getCurrentUserUseCase;
    @MockitoBean private LogoutUseCase logoutUseCase;
    @MockitoBean private ChangePasswordUseCase changePasswordUseCase;

    // ─── Registration Tests ──────────────────────────────────────────

    @Nested
    @DisplayName("POST /auth/register")
    class RegisterTests {

        @Test
        @DisplayName("returns 201 with auth response on successful registration")
        void register_success() throws Exception {
            AuthResult result = new AuthResult(1L, "CUSTOMER", "jwt-token-123", "refresh-token-123", 300L);
            when(registerUserUseCase.execute(any(RegisterUserCommand.class))).thenReturn(result);

            RegisterUserRequest request = new RegisterUserRequest();
            request.setFullName("Ahmed Ali");
            request.setPhone("+966501234567");
            request.setPassword("SecurePass1");
            request.setRole(UserRole.CUSTOMER);
            request.setPreferredLanguage("ar");

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.userId").value(1))
                    .andExpect(jsonPath("$.role").value("CUSTOMER"))
                    .andExpect(jsonPath("$.accessToken").value("jwt-token-123"))
                    .andExpect(jsonPath("$.refreshToken").value("refresh-token-123"))
                    .andExpect(jsonPath("$.expiresIn").value(300));

            verify(registerUserUseCase).execute(any(RegisterUserCommand.class));
        }

        @Test
        @DisplayName("returns 409 when phone already registered")
        void register_duplicatePhone_returns409() throws Exception {
            when(registerUserUseCase.execute(any(RegisterUserCommand.class)))
                    .thenThrow(new PhoneAlreadyRegisteredException("+966501234567"));

            RegisterUserRequest request = new RegisterUserRequest();
            request.setFullName("Ahmed Ali");
            request.setPhone("+966501234567");
            request.setPassword("SecurePass1");
            request.setRole(UserRole.CUSTOMER);

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value("Phone already registered: +966501234567"));
        }

        @Test
        @DisplayName("returns 400 when fullName is blank")
        void register_blankFullName_returns400() throws Exception {
            RegisterUserRequest request = new RegisterUserRequest();
            request.setFullName("");
            request.setPhone("+966501234567");
            request.setPassword("SecurePass1");
            request.setRole(UserRole.CUSTOMER);

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation failed"))
                    .andExpect(jsonPath("$.fields.fullName").exists());

            verifyNoInteractions(registerUserUseCase);
        }

        @Test
        @DisplayName("returns 400 when phone is missing")
        void register_missingPhone_returns400() throws Exception {
            RegisterUserRequest request = new RegisterUserRequest();
            request.setFullName("Ahmed Ali");
            request.setPassword("SecurePass1");
            request.setRole(UserRole.CUSTOMER);

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fields.phone").exists());

            verifyNoInteractions(registerUserUseCase);
        }

        @Test
        @DisplayName("returns 400 when password is missing")
        void register_missingPassword_returns400() throws Exception {
            RegisterUserRequest request = new RegisterUserRequest();
            request.setFullName("Ahmed Ali");
            request.setPhone("+966501234567");
            request.setRole(UserRole.CUSTOMER);

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fields.password").exists());

            verifyNoInteractions(registerUserUseCase);
        }

        @Test
        @DisplayName("returns 400 when role is null")
        void register_nullRole_returns400() throws Exception {
            String json = """
                {
                    "fullName": "Ahmed Ali",
                    "phone": "+966501234567",
                    "password": "SecurePass1"
                }
                """;

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fields.role").exists());

            verifyNoInteractions(registerUserUseCase);
        }

        @Test
        @DisplayName("returns 400 when phone format is invalid (VO validation)")
        void register_invalidPhoneFormat_returns400() throws Exception {
            RegisterUserRequest request = new RegisterUserRequest();
            request.setFullName("Ahmed Ali");
            request.setPhone("0501234567");
            request.setPassword("SecurePass1");
            request.setRole(UserRole.CUSTOMER);

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fields.phone").exists());

            verifyNoInteractions(registerUserUseCase);
        }

        @Test
        @DisplayName("returns 400 when password is too weak")
        void register_weakPassword_returns400() throws Exception {
            RegisterUserRequest request = new RegisterUserRequest();
            request.setFullName("Ahmed Ali");
            request.setPhone("+966501234567");
            request.setPassword("weak");
            request.setRole(UserRole.CUSTOMER);

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fields.password").exists());

            verifyNoInteractions(registerUserUseCase);
        }

        @Test
        @DisplayName("returns 201 for PROVIDER role registration")
        void register_providerRole_success() throws Exception {
            AuthResult result = new AuthResult(2L, "PROVIDER", "jwt-provider-token", "refresh-provider-token", 300L);
            when(registerUserUseCase.execute(any(RegisterUserCommand.class))).thenReturn(result);

            RegisterUserRequest request = new RegisterUserRequest();
            request.setFullName("Salon Owner");
            request.setPhone("+966509876543");
            request.setPassword("ProviderPass1");
            request.setRole(UserRole.PROVIDER);

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.role").value("PROVIDER"))
                    .andExpect(jsonPath("$.userId").value(2));
        }
    }

    // ─── Login Tests ─────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /auth/login")
    class LoginTests {

        @Test
        @DisplayName("returns 200 with auth response on successful login")
        void login_success() throws Exception {
            AuthResult result = new AuthResult(1L, "CUSTOMER", "jwt-login-token", "refresh-login-token", 300L);
            when(loginUserUseCase.execute(any(LoginCommand.class))).thenReturn(result);

            LoginRequest request = new LoginRequest();
            request.setPhone("+966501234567");
            request.setPassword("SecurePass1");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(1))
                    .andExpect(jsonPath("$.role").value("CUSTOMER"))
                    .andExpect(jsonPath("$.accessToken").value("jwt-login-token"))
                    .andExpect(jsonPath("$.refreshToken").value("refresh-login-token"));
        }

        @Test
        @DisplayName("returns 401 when credentials are invalid")
        void login_invalidCredentials_returns401() throws Exception {
            when(loginUserUseCase.execute(any(LoginCommand.class)))
                    .thenThrow(new InvalidCredentialsException());

            LoginRequest request = new LoginRequest();
            request.setPhone("+966501234567");
            request.setPassword("WrongPassword1");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Invalid credentials"));
        }

        @Test
        @DisplayName("returns 400 when phone is blank")
        void login_blankPhone_returns400() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setPhone("");
            request.setPassword("SecurePass1");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fields.phone").exists());

            verifyNoInteractions(loginUserUseCase);
        }

        @Test
        @DisplayName("returns 400 when password is blank")
        void login_blankPassword_returns400() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setPhone("+966501234567");
            request.setPassword("");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fields.password").exists());

            verifyNoInteractions(loginUserUseCase);
        }

        @Test
        @DisplayName("returns 400 when phone format is invalid")
        void login_invalidPhoneFormat_returns400() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setPhone("0501234567");
            request.setPassword("SecurePass1");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fields.phone").exists());

            verifyNoInteractions(loginUserUseCase);
        }
    }

    // ─── Refresh Token Tests ─────────────────────────────────────────

    @Nested
    @DisplayName("POST /auth/refresh")
    class RefreshTests {

        @Test
        @DisplayName("returns 200 with new tokens on successful refresh")
        void refresh_success() throws Exception {
            TokenResult result = new TokenResult("new-access", "new-refresh", 300L);
            when(refreshTokenUseCase.execute(any(RefreshTokenCommand.class))).thenReturn(result);

            RefreshTokenRequest request = new RefreshTokenRequest();
            request.setRefreshToken("old-refresh-token");

            mockMvc.perform(post("/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("new-access"))
                    .andExpect(jsonPath("$.refreshToken").value("new-refresh"))
                    .andExpect(jsonPath("$.expiresIn").value(300));
        }

        @Test
        @DisplayName("returns 401 when refresh token is expired")
        void refresh_expiredToken_returns401() throws Exception {
            when(refreshTokenUseCase.execute(any(RefreshTokenCommand.class)))
                    .thenThrow(new InvalidCredentialsException());

            RefreshTokenRequest request = new RefreshTokenRequest();
            request.setRefreshToken("expired-refresh-token");

            mockMvc.perform(post("/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Invalid credentials"));
        }

        @Test
        @DisplayName("returns 400 when refresh token is blank")
        void refresh_blankToken_returns400() throws Exception {
            RefreshTokenRequest request = new RefreshTokenRequest();
            request.setRefreshToken("");

            mockMvc.perform(post("/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(refreshTokenUseCase);
        }
    }
}
