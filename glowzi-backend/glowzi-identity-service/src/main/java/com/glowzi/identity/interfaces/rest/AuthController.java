package com.glowzi.identity.interfaces.rest;

import com.glowzi.identity.application.*;
import com.glowzi.identity.application.command.*;
import com.glowzi.identity.application.result.*;
import com.glowzi.identity.interfaces.rest.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller — the ONLY place that knows about HTTP.
 *
 * Endpoints:
 * PUBLIC (no token required):
 *   POST /auth/register  → register a new user
 *   POST /auth/login     → log in and get tokens
 *   POST /auth/refresh   → exchange refresh token for new tokens
 *
 * AUTHENTICATED (valid JWT required):
 *   GET  /auth/validate        → verify token + return identity (used by API Gateway)
 *   GET  /auth/me              → return full user profile
 *   POST /auth/logout          → revoke refresh token
 *   POST /auth/change-password → change password (requires old password verification)
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final LogoutUseCase logoutUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;

    public AuthController(RegisterUserUseCase registerUserUseCase,
                          LoginUserUseCase loginUserUseCase,
                          RefreshTokenUseCase refreshTokenUseCase,
                          GetCurrentUserUseCase getCurrentUserUseCase,
                          LogoutUseCase logoutUseCase,
                          ChangePasswordUseCase changePasswordUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUserUseCase = loginUserUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.logoutUseCase = logoutUseCase;
        this.changePasswordUseCase = changePasswordUseCase;
    }

    // ─── PUBLIC ENDPOINTS ────────────────────────────────────────────

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterUserRequest request) {
        RegisterUserCommand command = new RegisterUserCommand(
                request.getFullName(),
                request.getPhone(),
                request.getPassword(),
                request.getRole().name(),
                request.getPreferredLanguage()
        );

        AuthResult result = registerUserUseCase.execute(command);

        return new AuthResponse(
                result.userId(),
                result.role(),
                result.accessToken(),
                result.refreshToken(),
                result.expiresIn()
        );
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        LoginCommand command = new LoginCommand(
                request.getPhone(),
                request.getPassword()
        );

        AuthResult result = loginUserUseCase.execute(command);

        return new AuthResponse(
                result.userId(),
                result.role(),
                result.accessToken(),
                result.refreshToken(),
                result.expiresIn()
        );
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public TokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenCommand command = new RefreshTokenCommand(request.getRefreshToken());

        TokenResult result = refreshTokenUseCase.execute(command);

        return new TokenResponse(
                result.accessToken(),
                result.refreshToken(),
                result.expiresIn()
        );
    }

    // ─── AUTHENTICATED ENDPOINTS ─────────────────────────────────────

    /**
     * Validates the JWT and returns the user's identity.
     * Used by the API Gateway to verify tokens and enrich requests.
     *
     * The JWT is automatically validated by Spring Security's OAuth2 resource server.
     * If the token is invalid/expired, Spring returns 401 before this method is called.
     * If valid, we extract user info from the JWT claims + local DB lookup.
     */
    @GetMapping("/validate")
    @ResponseStatus(HttpStatus.OK)
    public ValidateResponse validate(@AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getSubject();

        UserProfileResult profile = getCurrentUserUseCase.execute(username);

        return new ValidateResponse(
                profile.userId(),
                profile.role(),
                profile.phone()
        );
    }

    /**
     * Returns the full profile of the currently authenticated user.
     */
    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public UserProfileResponse me(@AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getSubject();

        UserProfileResult profile = getCurrentUserUseCase.execute(username);

        return new UserProfileResponse(
                profile.userId(),
                profile.fullName(),
                profile.phone(),
                profile.role(),
                profile.preferredLanguage()
        );
    }

    /**
     * Logs out the user by revoking the refresh token.
     * The client should discard both access and refresh tokens after calling this.
     */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody LogoutRequest request) {
        logoutUseCase.execute(new LogoutCommand(request.getRefreshToken()));
    }

    /**
     * Changes the authenticated user's password.
     * Requires the old password for verification.
     */
    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@AuthenticationPrincipal Jwt jwt,
                               @Valid @RequestBody ChangePasswordRequest request) {
        String username = jwt.getSubject();

        ChangePasswordCommand command = new ChangePasswordCommand(
                username,
                request.getOldPassword(),
                request.getNewPassword()
        );

        changePasswordUseCase.execute(command);
    }
}

